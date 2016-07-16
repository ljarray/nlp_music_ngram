package nlp.music;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jfugue.Player;

public class Experiment {
    Corpus training = new Corpus();
    Corpus cases = new Corpus();

    public Experiment(Corpus t) throws Exception {
        training = t;
        t.makeTest(50);
        cases = new Corpus();
        cases.makeChordBank(new File("Corpus/test/"));

        //runTests();
    }

    public void runTests() throws Exception {
        Integer correctCountb = 0;
        Integer correctCountt = 0;

        File[] f = new File("Corpus/test/").listFiles();
        assert f != null;
        for (int ff = 2; ff < f.length; ff += 2){
            ArrayList<String> test1 = new ArrayList<>();
            ArrayList<String> test2 = new ArrayList<>();
            Pattern notePattern = Pattern.compile("[A-G]([#|b| ])\\d");

            Matcher notes1 = notePattern.matcher(new String(Files.readAllBytes(Paths.get(f[ff-1].getAbsolutePath()))));
            while(notes1.find()){
                test1.add(notes1.group());
            }

            Matcher notes2 = notePattern.matcher(new String(Files.readAllBytes(Paths.get(f[ff].getAbsolutePath()))));
            while(notes2.find()) {
                test2.add(notes2.group());
            }

            Boolean correct = compareBigram(test1, test2);
            if(correct){
                correctCountb++;
            }
            correct = compareTrigram(test1, test2);
            if(correct){
                correctCountt++;
            }
        }

        Double precisionb = correctCountb/(50*1.0);
        System.out.println("\n\n---------------------------------------------------");
        System.out.println("Precision for Bigram Back-off model: " + precisionb);
        System.out.println("---------------------------------------------------");

        Double precisiont = correctCountt/(50*1.0);
        System.out.println("\n\n---------------------------------------------------");
        System.out.println("Precision for Trigram Back-off model: " + precisiont);
        System.out.println("---------------------------------------------------");

        if(precisiont > precisionb)
            System.out.println("The trigram model is more precise.");
        else if (precisionb > precisiont)
            System.out.println("The bigram model is more precise.");
        else
            System.out.println("Both models are equally precise.");
    }

    public void demo() throws Exception{
        String a = "";
        String b = "";

        Player player = new Player();

        a = "C 7 E 6 C 6 E 5 C 5 C 5 E 5 F 5 D 2 E 5";
        player.play("C7q E6q C6q E5q C5q C5q E5q F5q D2q E5q");

        b = "D 6 D 6 C 6 B 5 A 5 G 5 F#5 E 5 D 5 B 6";
        player.play("D6q D6q C6q B5q A5q G5q F#5q E5q D5q B6q");

        System.out.println("\n\n\nBigrams: Test Run 1");
        this.compareBigram(a, b);
        System.out.println("\n\n\nTrigrams: Test Run 1");
        this.compareTrigram(a, b);

        a = "Eb5 C 5 Bb5 F#5 F 5 Eb5 F 5 Bb5 F#5 F 5 Eb5 F 5 D 5 Bb5 Eb5 F#5";
        player.play("Eb5q C5q Bb5q F#5q F5q Eb5q F5q Bb5q F#5q F5q Eb5q F5q D#5q Bb5q Eb5q F#5q");

        b = "B 5 E 6 B 5 G#5 E 5 F#5 B 5 F#6 B 5 F#5 Eb5 G#5 B 5 E 6 B 5 G#5 E 5";
        player.play("B5q E6q B5q G#5q E5q F#5q B5q F#6q B5q F#5q Eb5q G#5q B5q E6q B5q G#5q E5q");

        System.out.println("\n\n\nBigrams: Test Run 2");
        this.compareBigram(a, b);
        System.out.println("\n\n\nTrigrams: Test Run 2");
        this.compareTrigram(a, b);
    }

    Boolean compareBigram(String a, String b) throws Exception {
        ArrayList<String> sa = parse(a);
        ArrayList<String> sb = parse(b);

        return compareBigram(sa, sb);
    }

    Boolean compareTrigram(String a, String b) throws Exception {
        ArrayList<String> sa = parse(a);
        ArrayList<String> sb = parse(b);

        return compareTrigram(sa, sb);
    }

    Boolean compareBigram(ArrayList<String> a, ArrayList<String> b) throws Exception {
        Double pa = bigramChain(a);
        Double pb = bigramChain(b);

        System.out.println("The bigram chain probability of the first notes is " + pa );
        System.out.println("The bigram chain probability of the second notes is " + pb );

        if (pa > pb) {
            System.out.println("The first notes are more likely.");
            return true;
        } else if (pa == pb){
            System.out.println("Their probabilities are equal.");
            return false;
        } else {
            System.out.println("The second notes are more likely.");
            return false;
        }
    }

    Boolean compareTrigram(ArrayList<String> a, ArrayList<String> b) throws Exception {
        Double pa = trigramChain(a);
        Double pb = trigramChain(b);

        System.out.println("The trigram chain probability of the first notes is " + pa );
        System.out.println("The trigram chain probability of the second notes is " + pb );

        if (pa > pb) {
            System.out.println("The first notes are more likely.");
            return true;
        } else if (pa == pb){
            System.out.println("Their probabilities are equal.");
            return false;
        } else {
            System.out.println("The second notes are more likely.");
            return false;
        }
    }

    Double bigramChain(ArrayList<String> sequence) throws Exception {

        Double totalp = training.getCount(training.byExactNote.get(sequence.get(0)))/(training.totalNotes*1.0);

        for (Integer i = 1; i < sequence.size(); i++){
            Note n0 = training.byExactNote.get(sequence.get(i-1));
            Note n1 = training.byExactNote.get(sequence.get(i));

            totalp *= bigram(n0, n1);
        }

        return totalp;
    }

    Double trigramChain(ArrayList<String> sequence) throws Exception {

        Double totalp = training.getCount(training.byExactNote.get(sequence.get(0)))/(training.totalNotes*1.0);

        for (Integer i = 2; i < sequence.size(); i++){
            Note n0 = training.byExactNote.get(sequence.get(i-2));
            Note n1 = training.byExactNote.get(sequence.get(i-1));
            Note n2 = training.byExactNote.get(sequence.get(i));

            totalp *= trigramBackoff(n0, n1, n2);
        }

        return totalp;
    }

    ArrayList<String> parse(String s){
        Pattern notePattern = Pattern.compile("[A-G]([#|b| ])\\d");
        Matcher n = notePattern.matcher(s);
        ArrayList<String> notes = new ArrayList<>();

        while(n.find()){
            notes.add(n.group());
        }

        return notes;
    }


    Double bigram(Note n0, Note n1) throws Exception {
        Double p = 0.0;
        Integer n0c = training.getCount(n0);
        Integer n0n1c = training.getNextCount(n0, n1);

        if (n0n1c != 0){
            p = n0n1c/(n0c*1.0);
            System.out.println("P(" + n1 + "|" + n0 + ") is: " + p);
        } else{
            p = (1 - training.getCount(n1)/(training.totalNotes*1.0))*(training.getCount(n1)/(training.totalNotes*1.0));
            System.out.println("P(" + n1 + ") is: " + p);
        }

        return p;
    }

    Double trigramBackoff(Note n0, Note n1, Note n2) throws Exception {
        Double p = 0.0;
        Integer n0n1c = training.getNextCount(n0, n1);
        Integer n0n1n2c = 0;

        for (Integer i = 2; i < n0.followers.get(n1).size(); i++){
            String nextPos = training.positionStream.get((training.positionStream.indexOf(n1.positions.get(i))));
            if(n1.followers.get(n2).contains(nextPos)){
                n0n1n2c++;
            }
        }
        if (n0n1n2c == 0){
            p = aplha(training.getCount(n0), n0n1c)*bigram(n1,n2);
        } else {
            p = n0n1n2c/(n0n1c*1.0);
            System.out.println("P(" + n2 + "|" + n1 + "," + n0 + ") is: " + p);
        }

        return p;
    }

    Double aplha(Integer n0c, Integer n1c){
        Double beta = 1 - n1c/(n0c*1.0);
        return beta;
    }

    void goodTuring(Corpus c) throws Exception {
        c.makeBins();
    }

}


//cases.printNoteGrouping("G 4");
//cases.printPositions(10, 50);
