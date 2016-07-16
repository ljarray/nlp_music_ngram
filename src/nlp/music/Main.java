package nlp.music;

import java.io.File;


public class Main {
    public static void main(String[] args) throws Exception {

        System.out.println("Please wait while I make the corpus.");
        Corpus classics = new Corpus();
        System.out.println("I am now making the chord bank.");
        classics.makeChordBank(new File("Corpus/txt/"));
        System.out.println("I am now making the follower bank.");
        classics.makeFollowerBank();
        //Experiment run = new Experiment(classics);

        Experiment run = new Experiment(classics);

        run.demo();

    }
}

//classics.printNoteGrouping("G 4");
//classics.printPositions(10, 50);