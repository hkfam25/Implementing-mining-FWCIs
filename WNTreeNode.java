import java.util.*;

class WNTreeNode {
    String item;
    Map<String, WNTreeNode> children;
    int pre;
    int pos;
    double weight;

    public WNTreeNode(String item, double weight) {
        this.item = item;
        this.weight = weight;
        this.children = new LinkedHashMap<>(); 
        this.pre = -1;
        this.pos = -1;
    }
    public WNTreeNode(String item, double weight, int pre, int pos) {
        this(item, weight);
        this.pre = pre;
        this.pos = pos;
    }
    public Map<String, WNTreeNode> getChildren() {
        return this.children;
    }

    public double getWeight() {
        return this.weight;
    }

    public double getPre() {
        return this.pre;
    }

    public double getPos() {
        return this.pos;
    }



    public String getName() {
        return this.item;
    }

    public void addChild(String childItem, double childWeight) {
        this.children.put(childItem, new WNTreeNode(childItem, childWeight));
    }

    public void updateWeight(double transactionWeight) {
        this.weight += transactionWeight;
    }

    public void assignPrePos() {
        int[] countPre = {0}; // Counter to track pre and pos values
        int[] countPos = {0};
        dfsAssignPrePos(this, countPre, countPos);
   

    }

    private void dfsAssignPrePos(WNTreeNode node, int[] countPre, int[]countPos) {
        node.pre = countPre[0]++;
        
        // Traverse children and recursively assign pre and pos values
        for (WNTreeNode child : node.children.values()) {
            dfsAssignPrePos(child, countPre, countPos);
        }
        
        node.pos = countPos[0]++;
    }
    
    public Map<String, List<WNTreeNode>> calculateAllWL() {
        Map<String, List<WNTreeNode>> itemToWL = new LinkedHashMap<>();
        traverseAndCalculateWL(this, itemToWL);
        return itemToWL;
    }
    // Helper method for traversing the tree and calculating WL for each item
    private void traverseAndCalculateWL(WNTreeNode node, Map<String, List<WNTreeNode>> itemToWL) {
        if (node == null) {
            return;
        }

        String itemName = node.getName();
        if (!itemToWL.containsKey(itemName)) {
            itemToWL.put(itemName, new ArrayList<>());
        }

        itemToWL.get(itemName).add(node);

        for (WNTreeNode child : node.getChildren().values()) {
            traverseAndCalculateWL(child, itemToWL);
        }
    }
    
    public boolean isAncestor(WNTreeNode otherNode) {
        return this.pre < otherNode.pre && this.pos > otherNode.pos;
    }
}