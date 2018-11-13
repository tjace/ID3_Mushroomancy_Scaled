package ID3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Mushroom {
    public static ArrayList<String> featureList;
    public HashMap<String, Integer> features;
    private boolean label;


    Mushroom(String fullLine) {
        String[] pieces = fullLine.split(" ");
        features = new HashMap<>();

        for (String each : pieces) {
            switch (each) {
                case "-1":
                    label = false;
                    break;
                case "+1":
                    label = true;
                    break;
                default:
                    String[] splits = each.split(":");
                    features.put(splits[0], Integer.parseInt(splits[1]));

            }
        }
    }

    public int getAtt(String feature) {
        return features.get(feature);
    }

    Set<String> getAllKeys() {
        return features.keySet();
    }

    boolean getLabel() {
        return label;
    }

    @Override
    public String toString() {
        String ret = "Shroom:\n";
        for (String feat : featureList) {
            ret += feat + ": " + features.get(feat) + "\n";
        }

        return ret;
    }
}
