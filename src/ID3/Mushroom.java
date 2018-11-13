package ID3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Mushroom {
    private static ArrayList<String> featureList;
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
                    String[] splits = each.split(":"); //splits[0] == feature, splits[1] == value of that feature
                    String feature = splits[0];
                    int value = Integer.parseInt(splits[1]);

                    features.put(feature, value);
                    if (!featureList.contains(feature))
                        featureList.add(feature);
            }
        }
    }

    public int getAtt(String feature) {
        return features.get(feature);
    }

    static ArrayList<String> getFeatureList() {
        return featureList;
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
