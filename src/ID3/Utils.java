package ID3;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class Utils {

    private static boolean DEBUG = true;

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
        int count = 0;


        try {
            reader = new BufferedReader(new FileReader(fileName));

            while ((line = reader.readLine()) != null) {
                Mushroom next = new Mushroom(line);
                ret.add(next);
                //count++;
                //System.out.println(count);
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
     */
    static ArrayList<Mushroom> readExamples(Collection<String> fileNames) throws Exception {
        ArrayList<Mushroom> shroomList = new ArrayList<>();

        for (String file : fileNames)
            shroomList.addAll(readExamples(file));

        return shroomList;
    }

    /**
     * Returns the tree's guess for the example.
     */

    static boolean sgn(Node root, Mushroom shroom, ArrayList<Mushroom> testShrooms) throws Exception {
        StringBuilder debug = new StringBuilder();
        Node currentNode = root;

        while (!currentNode.isLeaf()) {
            int nextPath;
            if (shroom.hasFeature(currentNode.name))
                nextPath = shroom.getAtt(currentNode.name);
            else
                nextPath = mostCommonAttribute(currentNode.name, testShrooms);

            debug.append(currentNode.name).append(": ").append(nextPath).append(" ==> ");

            currentNode = currentNode.followPath(nextPath);
        }

        debug.append(currentNode.name);
        System.out.println(debug + "");
        return currentNode.getLabel();
    }

    /**
     * Given a feature, returns the most common attribute for that feature in the given list of examples.
     *
     * @param feature     The feature to check most common attribute
     * @param testShrooms Set of shrooms to test for commonality among
     * @return the most common attribute of the feature
     */
    private static int mostCommonAttribute(String feature, ArrayList<Mushroom> testShrooms) {
        int max = -1;
        int commonAtt = -1;
        HashMap<Integer, Integer> counts = new HashMap<>();

        for (Mushroom mush : testShrooms) {

            if (mush.hasFeature(feature)) {

                int thisAtt = mush.getAtt(feature);
                //Increment count in counts for this attribute, if it's already mapped
                if (counts.containsKey(thisAtt))
                    counts.put(thisAtt, counts.get(thisAtt) + 1);

                else  //If not already mapped, map it
                    counts.put(mush.getAtt(feature), 1);
            }
        }

        for (int attribute : counts.keySet()) {
            if (counts.get(attribute) > max) {
                commonAtt = attribute;
                max = counts.get(attribute);
            }
        }

        return commonAtt;
    }

    @SuppressWarnings("Duplicates")
    public static void oldStuff(HashMap<String, ArrayList<Integer>> featureList) throws Exception {
        String fileName;
        fileName = "src/Mango.csv";

        //Create the list of examples
        ArrayList<Mushroom> trainShrooms = Utils.readExamples(fileName);

        //Add the many, many features to featureList
        for (String feat : Mushroom.getFeatureList()) {
            featureList.put(feat, new ArrayList<>());
        }

        //Create a separate list with the same training examples?
        ArrayList<Mushroom> shrooms = new ArrayList<>(trainShrooms);

        for (Mushroom mush : trainShrooms) {

            //Grab each unique value for each feature, and add it to featureList
            //This is needed for use in the ID3 algorithm/method in the form of "feats" below.
            for (String eachFeature : mush.features.keySet()) {
                ArrayList<Integer> test = featureList.get(eachFeature);

                if (!featureList.get(eachFeature).contains(mush.getAtt(eachFeature)))
                    featureList.get(eachFeature).add(mush.getAtt(eachFeature));
            }
        }

        //This travel-size list of features is used by ID3
        HashSet<String> feats = new HashSet<>(featureList.keySet());

        //Construct the tree and return the root
        Node root = Main.ID3(shrooms, feats, 1, -1);

        if (DEBUG) System.out.println(root.name);

        int maxDepth = root.findMaxDepth();

        double error = shroomError(root, fileName);
        System.out.println("Error: " + error);
        System.out.println("Max depth: " + maxDepth);
    }


    static double shroomError(Node root, String fileName) throws Exception {
        int fail = 0;

        ArrayList<Mushroom> testShrooms = Utils.readExamples(fileName);

        for (Mushroom shroom : testShrooms) {
            boolean actual = shroom.getLabel();  //Actual label for this example

            if (DEBUG) {
                System.out.println(shroom);
            }

            boolean guess = Utils.sgn(root, shroom, testShrooms);

            if (actual != guess)
                fail++;
        }

        return (double) fail / (double) (testShrooms.size());
    }

    static void printTestGuesses(Node root, ArrayList<Mushroom> evalExamples, String evalIDFile, String outputFile) throws Exception {

        //This is what will write the output.
        PrintWriter writer = new PrintWriter(outputFile, StandardCharsets.UTF_8);
        writer.println("example_id,label");

        //This is for reading the IDs file
        BufferedReader evalReader = null;
        String IDLine;
        int lineNumber = 1;

        try {
            evalReader = new BufferedReader(new FileReader(evalIDFile));

            for (Mushroom ex : evalExamples) {
                //Grab the example's ID (they are in order)
                IDLine = evalReader.readLine();
                String postLine;
                lineNumber++;

                boolean guess = sgn(root, ex, evalExamples);
                if (guess)
                    postLine = IDLine + "," + "1";
                else
                    postLine = IDLine + "," + "0";
                System.out.println("Line " + lineNumber + ": " + postLine);
                writer.println(postLine);
            }

        } catch (
                FileNotFoundException e) {
            System.out.println("File " + evalIDFile + " not found.");
        } catch (
                IOException e) {
            e.printStackTrace();
        } finally {
            writer.close();

            if (evalReader != null) {
                try {
                    evalReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
