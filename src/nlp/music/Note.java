package nlp.music;

import java.util.ArrayList;
import java.util.HashMap;

public class Note
{
    Character note;
    Character mod;
    Byte octave;
    HashMap<Note, ArrayList<String>> followers = new HashMap<>();

    public ArrayList<String> positions = new ArrayList<>();

    public Note(String file, String s)
    {
        note = s.charAt(0);
        if (s.charAt(1) == 'b' || s.charAt(1) == '#')
        {
            mod = s.charAt(1);
            octave = Byte.parseByte(s.charAt(2) + "");
        }
        else
        {
            mod = ' ';
            octave = Byte.parseByte(s.charAt(1) + "");
        }
        positions.add(file + "|" + String.format("%08d", Integer.parseInt(s.split("@")[1])));
    }

    public void setFollowers(Note f, String pos){
        if (!followers.containsKey(f)){
            ArrayList p = new ArrayList();
            p.add(pos);
            followers.put(f, p);
        } else {
            followers.get(f).add(pos);
        }
    }

    public String toString()
    {
        return note + "" + mod + "" + octave;
    }
}
