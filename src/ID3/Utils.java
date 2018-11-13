package ID3;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Utils {

    /**
     * Creates an ArrayList full of examples, as read in from a given file.
     *
     * @param fileName where the Example lines are read from
     * @return an ArrayList of read examples
     */
    static ArrayList<Mushroom> readExamples(String fileName) throws Exception {
        ArrayList<Mushroom> ret = new ArrayList<>();

        BufferedReader reader = null;
        String line;


        try {
            reader = new BufferedReader(new FileReader(fileName));

            while ((line = reader.readLine()) != null) {
                Mushroom next = new Mushroom(line);
                ret.add(next);
            }


        } catch (
                FileNotFoundException e) {
            System.out.println("File " + fileName + " not found.");
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    /**
     * Returns a constructed set of examples, including all examples from all files listed.
     *
     * @param fileNames
     * @return
     * @throws Exception
     */
    static ArrayList<Mushroom> readExamples(Collection<String> fileNames) throws Exception {
        ArrayList<Mushroom> shroomList = new ArrayList<>();

        for (String file : fileNames)
            shroomList.addAll(readExamples(file));

        return shroomList;
    }
}
