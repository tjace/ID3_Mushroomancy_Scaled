package ID3;

import java.util.HashMap;

class Node {
    private int depth;

    //If leaf, name = +1 or -1 for label.
    //Name is the attribute to be tested at this node/step
    String name;
    private boolean leaf;
    private HashMap<Integer, Node> children;

    Node(String _name, int _depth, boolean _leaf) {

        leaf = _leaf;
        name = _name;
        depth = _depth;

        children = new HashMap<>();
    }

    //Adds a child. Check == which answer is given to this node to go to that child
    void add(int check, Node child) {
        children.put(check, child);
    }

    boolean isLeaf() {
        return leaf;
    }

    Node followPath(int path) {
        return children.get(path);
    }

    int findMaxDepth() {
        if (leaf)
            return depth;

        int max = 0;
        for (Node each : children.values()) {
            int eachDepth = each.findMaxDepth();

            if (eachDepth > max)
                max = eachDepth;
        }

        return max;
    }

    boolean getLabel() throws Exception {
        if (!leaf)
            throw new Exception("This is not a leaf node!\n");

        switch (name) {
            case "+1":
                return true;
            case "-1":
                return false;
            default:
                throw new Exception("Failed to get label: bad node name: " + name);
        }
    }
}
