package ID3;

import java.util.*;

public class Main {
    //The list of shrooms.  Constantly queried.

    //A list of features mapped to possible answers
    private static HashMap<String, ArrayList<Integer>> featureList;


    private static boolean DEBUG = true;


    public static void main(String[] args) throws Exception {
        String fileName;
        if (args.length == 0)
            fileName = "src/diabetes.train";
        else
            fileName = args[0];

        //Add the many lists to featureList
        featureList = new HashMap<>();
        for (String feat : Mushroom.getFeatureList()) {
            featureList.put(feat, new ArrayList<>());
        }

        //createShrooms("src/train.csv");
        ArrayList<Mushroom> trainShrooms = Utils.readExamples(fileName);

        ArrayList<Mushroom> shrooms = new ArrayList<>(trainShrooms);

        for (Mushroom mush : trainShrooms) {

            //Grab each unique value for each feature, and add it to featureList
            for (String eachFeature : mush.features.keySet()) {

                if (!featureList.get(eachFeature).contains(mush.getAtt(eachFeature)))
                    featureList.get(eachFeature).add(mush.getAtt(eachFeature));
            }
        }

        HashSet<String> feats = new HashSet<>(featureList.keySet());
        feats.remove("label");

        Node root = ID3(shrooms, feats, 1, -1);

        if (DEBUG) System.out.println(root.name);

        int maxDepth = root.findMaxDepth();

        double error = shroomError(root, fileName);
        System.out.println("Error: " + error);
        System.out.println("Max depth: " + maxDepth);


    }

    /**
     * if maxDepth == -1, there is no max.
     */
    private static Node ID3(ArrayList<Mushroom> shrooms, HashSet<String> features, int depth, int maxDepth) throws Exception {
        //If all shrooms have the same label, return a leaf with that label
        int sameyLabel = checkAllSameLabel(shrooms);
        if (sameyLabel == 1)
            return new Node("+1", depth, true);
        if (sameyLabel == -1)
            return new Node("-1", depth, true);
        if (sameyLabel != 0)
            throw new Exception("Samey label not 0, 1 or -1: " + sameyLabel);


        //If this is the lowest a node can be, it'll have to be a leaf.
        if (maxDepth != -1 && depth == maxDepth) {
            String commonLabel = findCommonLabel(shrooms);
            return new Node(commonLabel, depth, true);
        }


        //If this is as far as the features go (al have been checked already), make leaf node using the most common label.
        if (features.isEmpty()) {
            String commonLabel = findCommonLabel(shrooms);
            return new Node(commonLabel, depth, true);
        }

        //Determine best feature to discriminate by at this point
        //Using InfoGain
        String bestFeature = "";

        double maxGain = 0;
        for (String eachFeat : features) {
            double infoGain = infoGain(eachFeat, "label", shrooms);

            if (infoGain > maxGain) {
                maxGain = infoGain;
                bestFeature = eachFeat;
            }
        }


        //Remove the used feature from later branches
        HashSet<String> nextFeatures = new HashSet<>(features);
        nextFeatures.remove(bestFeature);

        Node thisNode = new Node(bestFeature, depth, false);

        //For each value of the feature (e.g. sweet, spicy, mild)
        //enact again ID3 using only the surviving shrooms

        for (String nextAtt : featureList.get(bestFeature)) {
            //Construct Sv (subset of shrooms that have the specified value/attribute of the bestFeature)
            ArrayList<Mushroom> nextShrooms = new ArrayList<Mushroom>();

            for (Mushroom shroom : shrooms) {
                if (shroom.getAtt(bestFeature).equals(nextAtt)) {
                    nextShrooms.add(shroom);
                }
            }

            Node nextNode;
            if (nextShrooms.size() == 0) {
                String commonLabel = findCommonLabel(shrooms);
                nextNode = new Node(commonLabel, depth + 1, true);
            } else {
                nextNode = ID3(nextShrooms, nextFeatures, depth + 1, maxDepth);
            }

            thisNode.add(nextAtt, nextNode);

        }

        return thisNode;
    }

    private static double infoGain(String feature, String label, ArrayList<Mushroom> shrooms) {
        double bigEntropy = entropy(label, shrooms);

        ArrayList<String> labelValues = new ArrayList<>(featureList.get(label));

        double expectedEntropy = 0;

        for (String att : featureList.get(feature)) {
            //get the subset of shrooms with each value of the feature
            ArrayList<Mushroom> nextShrooms = new ArrayList<Mushroom>();
            for (Mushroom shroom : shrooms) {
                if (shroom.getAtt(feature).equals(att)) {
                    nextShrooms.add(shroom);
                }
            }

            double thisEntropy = entropy(label, nextShrooms);

            expectedEntropy += (((double) (nextShrooms.size()) / (double) (shrooms.size())) * thisEntropy);


        }

        return bigEntropy - expectedEntropy;
    }

    private static double entropy(String label, ArrayList<Mushroom> shrooms) {
        ArrayList<String> labelValues = new ArrayList<>(featureList.get(label));
        int[] valueCounts = new int[labelValues.size()];
        Arrays.fill(valueCounts, 0);

        double total = shrooms.size();

        for (Mushroom shroom : shrooms) {
            boolean value = shroom.getLabel();
            valueCounts[labelValues.indexOf(value)]++;
        }


        double entropy = 0;

        for (double eachP : valueCounts) {
            if (eachP == 0.0)
                continue;

            double proportion = (eachP / total);
            double logResult = logBase2(proportion);
            entropy -= (proportion * logResult);
        }

        return entropy;
    }

    private static double logBase2(double input) {
        return Math.log(input) / Math.log(2);
    }

    /**
     * Returns the most common label.
     *
     * @param shrooms the set of examples to check for common label
     * @return either "+1" or "-1"
     */
    private static String findCommonLabel(ArrayList<Mushroom> shrooms) {
        int pos = 0;
        int neg = 0;

        for (Mushroom mush : shrooms) {
            boolean label = mush.getLabel();

            if (label)
                pos++;
            else
                neg++;
        }

        if (neg > pos)
            return "-1";
        else
            return "+1";
    }

    /**
     * Check if all examples remaining are the same label (+ or -)
     *
     * @param shrooms The list of examples to compare labels with
     * @return 1 if all true, -1 if all false, 0 if not all same.
     */
    private static int checkAllSameLabel(ArrayList<Mushroom> shrooms) {

        //find the label of the first example in the list
        boolean label = shrooms.get(0).getLabel();

        //If any following don't match the first example, return 0
        for (Mushroom shroom : shrooms) {
            if (!(shroom.getLabel() == label))
                return 0;
        }

        //If all are the same, return +1/-1 depending on if all are true/false
        if (label)
            return 1;
        else
            return -1;

    }

    private static double shroomError(Node root, String fileName) throws Exception {
        int fail = 0;

        ArrayList<Mushroom> testShrooms = Utils.readExamples(fileName);

        for (Mushroom shroom : testShrooms) {
            Node currentNode = root;
            boolean expected = shroom.getLabel();  //Actual label for this example

            if (DEBUG) {
                System.out.println(shroom);
            }

            StringBuilder debug = new StringBuilder();

            while (!currentNode.isLeaf()) {
                int nextPath = shroom.getAtt(currentNode.name);

                debug.append(currentNode.name).append(": ").append(nextPath).append(" ==> ");

                currentNode = currentNode.followPath(nextPath);
            }

            debug.append(currentNode.name);
            System.out.println(debug + "");

//            if (!expected.equals(currentNode.name))
//                fail++;
            if (expected != currentNode.getLabel())
                fail++;

        }

        return (double) fail / (double) (testShrooms.size());
    }


    /*
     * Creates one array of shrooms from multiple files.
     */
    /*
    private static ArrayList<Mushroom> createShrooms(Collection<String> fileNames) {
        ArrayList<Mushroom> shrooms = new ArrayList<Mushroom>();

        boolean init = true;
        for (String fileName : fileNames) {
            shrooms.addAll(createShrooms(fileName, init));
            init = false;
        }

        return shrooms;
    }
    */

/*
    private static ArrayList<Mushroom> createShrooms(String fileName, boolean initializeFeatures) {
        ArrayList<Mushroom> shrooms = new ArrayList<Mushroom>();

        BufferedReader reader = null;
        String line = "";

        try {
            reader = new BufferedReader(new FileReader(fileName));

            // First, grab the features out.
            line = reader.readLine();

            if (initializeFeatures) {
                Mushroom.featureList = new ArrayList<String>();
                featureList = new HashMap<>();
                for (String eachFeat : line.split(",")) {
                    Mushroom.featureList.add(eachFeat);
                    featureList.put(eachFeat, new ArrayList<>());
                }
            }

            while ((line = reader.readLine()) != null) {
                Mushroom next = new Mushroom(line);
                shrooms.add(next);
            }
        } catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " not found.");
        } catch (IOException e) {
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

        return shrooms;
    }
    */
}
