package nlp.music;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Corpus
{
    Integer totalNotes = 0;
    Pattern notePattern = Pattern.compile("[A-G]([#|b]?)\\d[/|w|h|q|i|t|s|x|o][^\\s]+\\s+@[^\\s]*", Pattern.DOTALL);

    public HashMap<Character, ArrayList<Note>> byNote = new HashMap<>();
    public HashMap<String, Note> byExactNote = new HashMap<>();
    public HashMap<Byte, ArrayList<Note>> byOctave = new HashMap<>();
    public HashMap<String, ArrayList<Note>> byPosition = new HashMap<>();
    HashMap<Integer, Integer> bins = new HashMap<>();
    public ArrayList<String> positionStream = new ArrayList<>();

    public void makeChordBank(File f) throws Exception
    {
        if (f.isDirectory())
            for (File ff : f.listFiles())
                makeChordBank(ff);
        else
        {
            Matcher notes = notePattern.matcher(new String(Files.readAllBytes(Paths.get(f.getAbsolutePath()))));
            while(notes.find())
            {
                totalNotes++;
                Note n = new Note(f.getName(), notes.group());
                if (byExactNote.containsKey(n.toString()))
                {
                    byExactNote.get(n.toString()).positions.add(n.positions.get(0));
                    if (!byPosition.containsKey(n.positions.get(0))) {
                        byPosition.put(n.positions.get(0), new ArrayList<>());
                        positionStream.add(n.positions.get(0));
                    }
                    byPosition.get(n.positions.get(0)).add(byExactNote.get(n.toString()));
                }
                else
                {
                    byExactNote.put(n.toString(), n);

                    if (!byNote.containsKey(n.note))
                        byNote.put(n.note, new ArrayList());
                    if (!byNote.get(n.note).contains(n))
                        byNote.get(n.note).add(n);

                    if (!byOctave.containsKey(n.octave))
                        byOctave.put(n.octave, new ArrayList());
                    if (!byOctave.get(n.octave).contains(n))
                        byOctave.get(n.octave).add(n);

                    for (String pos : n.positions)
                    {
                        if (!byPosition.containsKey(pos)) {
                            byPosition.put(pos, new ArrayList());
                            positionStream.add(pos);
                        }
                        if (!byPosition.get(pos).contains(n))
                            byPosition.get(pos).add(n);
                    }
                }
            }
        }
        Collections.sort(positionStream);
    }

    public void makeFollowerBank() {
        for (Integer i = 0; i < positionStream.size() - 1; i++){
            for (Note n : byPosition.get(positionStream.get(i))) {
                for (Note f : byPosition.get(positionStream.get(i+1))){
                    n.setFollowers(f, positionStream.get(i+1));
                }
            }
        }
        //System.out.println("Follower bank made!");
    }

    public void makeBins() throws Exception {
        for (HashMap.Entry<String, Note> n : this.byExactNote.entrySet())
        {
            Integer count = this.getCount(n.getValue());
            if (bins.containsKey(count))
                bins.put(count, bins.get(count) + 1);
            else
                bins.put(count, 1);
        }
    }

    Integer getCount(Note n) throws Exception {
        if (n != null) {
            return n.positions.size();
        } else {
            return 0;
        }
    }

    Integer getNextCount(Note n0, Note n1){
        if(n0 != null && n1 != null){
            if (n0.followers.get(n1) != null)
                return n0.followers.get(n1).size();
            else
                return 0;
        } else {
            return 0;
        }
    }

    void printNoteGrouping()
    {
        byExactNote.keySet().forEach(this::printNoteGrouping);
    }

    void printNoteGrouping(String note)
    {
        Note n = byExactNote.get(note);
        System.out.println("Note '" + n + "' found grouped with:");
        for (int i = 0; i < n.positions.size(); i++)
        {
            ArrayList<Note> group = byPosition.get(n.positions.get(i));
            if (group.size() > 1)
            {
                String pos = "  " + n.positions.get(i);
                while (pos.length() < 50)
                    pos += " ";
                System.out.print(pos);
                for (int j = 0; j < group.size(); j++)
                {
                    System.out.print(group.get(j));
                    if (j != group.size() - 1)
                        System.out.print(", ");
                }
                System.out.println();
            }
        }
        System.out.println();
    }

    void makeTest(Integer cases) throws Exception {
        for (Integer i = 1; i <= cases; i++ ){
            String fileName = String.format("%03d", i) + "_right.txt";
            File test = new File("Corpus/test/" + fileName );
            if (!test.exists()){
                test.createNewFile();
            }
            PrintWriter writer = new PrintWriter(test, "UTF-8");
            for (Integer j = 0; j < 12; j++){
                String[] pos = positionStream.get(i * 20 + j).split("\\|");
                String notes = byPosition.get(positionStream.get(i*20 + j)).get(0).toString() + "/0.0 @" + pos[1];
                writer.println(notes);
            }
            writer.close();

            fileName = String.format("%03d", i) + "_wrong.txt";
            File wrong = new File("Corpus/test/" + fileName );
            if (!wrong.exists()){
                wrong.createNewFile();
            }

            PrintWriter writer1 = new PrintWriter(wrong, "UTF-8");
            for (Integer j = 0; j < 12; j++){
                Integer rando = (int)(Math.random() * positionStream.size());
                String[] pos = positionStream.get(rando).split("\\|");
                String notes = byPosition.get(positionStream.get(rando)).get(0).toString() + "/0.0 @" + pos[1];
                writer1.println(notes);
            }
            writer1.close();

        }

    }

    void printPositions()
    {
        for (String p : positionStream){
            System.out.println(p);
        }
    }

    void printPositions(int start, int end)
    {
        for (String p : positionStream.subList(start,end)){
            System.out.println(p);
        }
    }
}


/*
    Double first(String s){
        Integer nc = 0;
        Integer fc = 1;

        if(this.byPosition.get(positionStream.get(0)).contains(this.byExactNote.get(s))){
            nc++;
        }

        String file = positionStream.get(0).split("|")[0];
        for (Integer i = 1; i < positionStream.size(); i++){
            if (!file.equals(positionStream.get(i).split("|")[0])){
                fc++;
                file = positionStream.get(i).split("|")[0];
                if(this.byPosition.get(positionStream.get(i)).contains(this.byExactNote.get(s))){
                    nc++;
                }
            }
        }

        return nc/(fc*1.0);
    }
*/

/*
    ArrayList<Note> getNextNotes(String pos){
        Integer here = positionStream.indexOf(pos);
        return byPosition.get(positionStream.get(here + 1));
    }
*/

/*
    //This comment block contains the code which generated the txt files in the Corpus folder

    public Corpus() throws IOException, InvalidMidiDataException {

        Player player = new Player();
        Pattern pattern = null;

        String songName;
        String artistName;

        File[] artists = new File("Corpus/midi/").listFiles();

        for (File artist : artists) {

            artistName = artist.getName();
            File folder = new File("Corpus/txt/" + artistName);

            if (!folder.exists()) {
                folder.mkdir();
            }

            File[] tunes = new File("Corpus/midi/" + artistName).listFiles();

            for (File tune : tunes) {
                songName = tune.getName().replace(".mid","");

                System.out.println(artistName + "/" + songName);

                 try{
                 pattern = player.loadMidi(new File("Corpus/midi/" + artistName + "/" + songName + ".mid"));
                 } catch (IOException e) {

                 } catch (InvalidMidiDataException e) {

                 }

                 File jfugTune = new File("Corpus/txt/" + artistName + "/" + songName + ".txt");

                 if (!jfugTune.exists()) {
                    try {
                        jfugTune.createNewFile();
                    } catch (IOException e) {

                    }
                 }

                 if (pattern != null) {
                    pattern.savePattern(jfugTune);
                 }

                 //player.play(pattern);
            }

        }

        System.out.println("Corpus created, boss.");
    }
 */
