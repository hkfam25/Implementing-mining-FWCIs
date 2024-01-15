import java.util.*;

class WNAlgorithm {

    public WNAlgorithm() {
    }

    public List<List<Object>> generateWeightedData(int numTransactions, int numItems) {
        List<List<Object>> weightedData = new ArrayList<>();
        Map<String, Double> itemWeights = new HashMap<>();

        Random random = new Random();

        for (int i = 0; i < numTransactions; i++) {
            List<Object> transaction = new ArrayList<>();
            Set<String> usedItems = new HashSet<>();

            while (transaction.size() < random.nextInt(numItems - 1) + 2) {
                char item = (char) (random.nextInt(11) + 65); // Generate items from A to K

                if (!usedItems.contains(String.valueOf(item))) {
                    usedItems.add(String.valueOf(item));

                    if (!itemWeights.containsKey(String.valueOf(item))) {
                        // Assign a permanent weight for the item within the range of 0.1 to 0.9
                        int randomWeightInt = random.nextInt(9) + 1;  // Random integer between 1 and 9
                        double itemWeight = randomWeightInt / 10.0;   // Convert to double and scale to range [0.1, 0.9]
                        itemWeights.put(String.valueOf(item), itemWeight);
                    }
                    
                    // Check if item already exists in the transaction
                    if (!transaction.contains(new AbstractMap.SimpleEntry<>(String.valueOf(item), itemWeights.get(String.valueOf(item))))) {
                        transaction.add(new AbstractMap.SimpleEntry<>(String.valueOf(item), itemWeights.get(String.valueOf(item))));
                    }
                }
            }

            weightedData.add(transaction);
        }

        return weightedData;
    }

    public static List<Double> calculateTransactionWeights(List<List<Object>> weightedData) {
        List<Double> transactionWeights = new ArrayList<>();

        for (List<Object> transaction : weightedData) {
            double transactionWeight = 0;
            int itemCount = 0;

            for (Object itemWeightPair : transaction) {
                @SuppressWarnings("unchecked")
                AbstractMap.SimpleEntry<String, Double> pair = (AbstractMap.SimpleEntry<String, Double>) itemWeightPair;
                transactionWeight += pair.getValue();
                itemCount++;
            }

            transactionWeight = Math.round((transactionWeight / itemCount) * 10000.0) / 10000.0;
            transactionWeights.add(transactionWeight);
        }

        return transactionWeights;
    }

    public static List<Map.Entry<String, Double>> calculateAndSortItemSupports(List<List<Object>> weightedData,
                                                                              List<Double> transactionWeights) {
        Map<String, Double> itemSupports = new HashMap<>();

        for (int i = 0; i < weightedData.size(); i++) {
            List<Object> transaction = weightedData.get(i);
            double transactionWeight = transactionWeights.get(i);

            for (Object itemWeightPair : transaction) {
                @SuppressWarnings("unchecked")
                AbstractMap.SimpleEntry<String, Double> pair = (AbstractMap.SimpleEntry<String, Double>) itemWeightPair;
                String item = pair.getKey();

                itemSupports.putIfAbsent(item, 0.0);
                itemSupports.put(item, itemSupports.get(item) + transactionWeight);
            }
        }

        for (Map.Entry<String, Double> entry : itemSupports.entrySet()) {
            itemSupports.put(entry.getKey(), entry.getValue() / transactionWeights.stream().mapToDouble(Double::doubleValue).sum());
        }

        List<Map.Entry<String, Double>> sortedItemSupports = new ArrayList<>(itemSupports.entrySet());
        sortedItemSupports.sort((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()));

        return sortedItemSupports;
    }


    public static List<List<Object>> removeItemsByWeightThreshold(
        List<List<Object>> weightedData,
        List<Map.Entry<String, Double>> sortedItemSupports,
        double minWs) {

        List<List<Object>> updatedWeightedData = new ArrayList<>();

        for (List<Object> transaction : weightedData) {
            List<Object> updatedTransaction = new ArrayList<>();

            for (Object itemWeightPair : transaction) {
                @SuppressWarnings("unchecked")
                AbstractMap.SimpleEntry<String, Double> pair = (AbstractMap.SimpleEntry<String, Double>) itemWeightPair;
                String item = pair.getKey();

                // Find the item in sortedItemSupports
                boolean shouldIncludeItem = false;
                for (Map.Entry<String, Double> entry : sortedItemSupports) {
                    if (entry.getKey().equals(item) && entry.getValue() >= minWs) {
                        shouldIncludeItem = true;
                        break;
                    }
                }

                if (shouldIncludeItem) {
                    updatedTransaction.add(itemWeightPair);
                }
            }

            updatedWeightedData.add(updatedTransaction);
        }

        return updatedWeightedData;
    }


    public static List<AbstractMap.SimpleEntry<List<String>, Double>> sortTransactionsAndWeights(List<List<Object>> weightedData,
                                                                                             List<Map.Entry<String, Double>> sortedItemSupports,
                                                                                             List<Double> transactionWeights) {

        Map<String, Integer> itemIndexMap = new HashMap<>();

        for (int i = 0; i < sortedItemSupports.size(); i++) {
            itemIndexMap.put(sortedItemSupports.get(i).getKey(), i);
        }

        List<AbstractMap.SimpleEntry<List<String>, Double>> sortedTransactionsAndWeights = new ArrayList<>();
        int index = 0;
        for (List<Object> transaction : weightedData) {
            List<AbstractMap.SimpleEntry<String, Double>> transactionItemsAndWeights = new ArrayList<>();
            for (Object itemWeightPair : transaction) {
                @SuppressWarnings("unchecked")
                AbstractMap.SimpleEntry<String, Double> pair = (AbstractMap.SimpleEntry<String, Double>) itemWeightPair;
                String item = pair.getKey();
                double itemWeight = pair.getValue();

                transactionItemsAndWeights.add(new AbstractMap.SimpleEntry<>(item, itemWeight));
            }
            transactionItemsAndWeights.sort((pair1, pair2) -> Double.compare(
                    sortedItemSupports.get(itemIndexMap.get(pair2.getKey())).getValue(),
                    sortedItemSupports.get(itemIndexMap.get(pair1.getKey())).getValue()));

            List<String> sortedTransactionItems = new ArrayList<>();
            //double transactionWeight = 0.0;

            for (AbstractMap.SimpleEntry<String, Double> transactionItemAndWeight : transactionItemsAndWeights) {
                sortedTransactionItems.add(transactionItemAndWeight.getKey());
            }

            sortedTransactionsAndWeights.add(new AbstractMap.SimpleEntry<>(sortedTransactionItems, transactionWeights.get(index)));
            index++;
            
        }
        return sortedTransactionsAndWeights;
    }


    public static WNTreeNode buildWNTree(List<AbstractMap.SimpleEntry<List<String>, Double>> sortedTransactionsAndWeights) {
        WNTreeNode wnTree = new WNTreeNode("", 0.0);

        for (AbstractMap.SimpleEntry<List<String>, Double> transaction : sortedTransactionsAndWeights) {
            insertTree(transaction, wnTree);
            //System.out.println(transaction);
        }

        return wnTree;
    }


    public static void printWNTree(WNTreeNode root, String prefix, boolean isTail) {
        // Print the structure of the tree with pre, pos values, and other details
        System.out.println(prefix + (isTail ? "└── " : "├── ") + root.getName() + " (Pre: " + root.pre + ", Pos: " + root.pos + ", Weight: " + root.getWeight() + ")");
        List<WNTreeNode> children = new ArrayList<>(root.getChildren().values());
        for (int i = 0; i < children.size() - 1; i++) {
            printWNTree(children.get(i), prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            printWNTree(children.get(children.size() - 1), prefix + (isTail ? "    " : "│   "), true);
        }
    }


    public static void insertTree(AbstractMap.SimpleEntry<List<String>, Double> transaction, WNTreeNode tree) {
        WNTreeNode currentNode = tree;
        List<String> itemPair = transaction.getKey();
        double transactionWeight = transaction.getValue();

        for (String item : itemPair) {
            boolean found = false;
            for (Map.Entry<String, WNTreeNode> child : currentNode.children.entrySet()) {
                if (item.equals(child.getKey())) {
                    found = true; 
                    currentNode = child.getValue();
                    currentNode.weight += transactionWeight;
                    break;
                }
                
            }

            if (!found) {
                WNTreeNode newChild = new WNTreeNode(item, transactionWeight);
                currentNode.children.put(item, newChild);
                currentNode = newChild;
            }
        }
    }

    public static String union(String s1, String s2) {
        
        Set<Character> mergedSet = new LinkedHashSet<>();

        // Add characters from s1
        for (char ch : s1.toCharArray()) {
            mergedSet.add(ch);
        }

        // Add characters from s2 only if they are not already in the set
        for (char ch : s2.toCharArray()) {
            if (!mergedSet.contains(ch)) {
                mergedSet.add(ch);
            }
        }

        // Convert the set to a string
        StringBuilder mergedString = new StringBuilder();
        for (Character ch : mergedSet) {
            mergedString.append(ch);
        }

        return mergedString.toString();
    }


    public static List<WNTreeNode> WL_Intersection(List<WNTreeNode> wl1, List<WNTreeNode> wl2) {
        List<WNTreeNode> wl3 = new ArrayList<>();
        int s = 0;
        int i = 0;
        int j = 0;
        int m = wl1.size();
        int n = wl2.size();

        while (i < m && j < n) {
            int pre1i = wl1.get(i).pre;
            int pos1i = wl1.get(i).pos;
            double w1i = wl1.get(i).weight;

            int pre2j = wl2.get(j).pre;
            int pos2j = wl2.get(j).pos;

            if (pre2j < pre1i) {
                if (pos2j > pos1i) {
                    if (s > 0 && pre2j == wl3.get(s - 1).pre) {
                        wl3.get(s - 1).weight += w1i;
                    } else {
                        s++;
                        WNTreeNode newNode = new WNTreeNode(union(wl1.get(i).item,wl2.get(j).item),w1i,pre2j, pos2j);
                        wl3.add(newNode);
                    }
                    i++;
                } else {
                    j++;
                }
            } else {
                i++;
            }
        }
        return wl3;
    }

    public static double calculateWeightedSupport(List<WNTreeNode> nodes, double sumTransactionWeights) {
        double weightSum = 0.0;
        
        for (WNTreeNode node : nodes) {
            weightSum += node.weight;
        }
        
        return weightSum / sumTransactionWeights;
    }

    public boolean isAncestor(List<WNTreeNode> wlPa1, List<WNTreeNode> wlPa2) {
        int count = 0;
        int check = wlPa1.size();
        boolean ancestorFound = false;

        for (WNTreeNode ci : wlPa2) {
            for (WNTreeNode cj : wlPa1) {
                if (ci.getPre() < cj.getPre() && ci.getPos() > cj.getPos()) {
                    count++;
                }
            }
        }

        if (count == check) {
            ancestorFound = true;
        } 
        else {
            ancestorFound = false;
        }

        return ancestorFound;
    }


    public List<String> NFWCI(List<String> FWCIs, Map<String, List<WNTreeNode>> calculatedWLs,double minws, double sumTW) {
    
        //Set<List<String>> FWCIs = new HashSet<>();
        

        return findFWCI(FWCIs,calculatedWLs, minws, sumTW);
    }

    // Main recursive method of the NFWCI algorithm
    public List<String> findFWCI(List<String> FWCIs, Map<String, List<WNTreeNode>> calculatedWls,
     double minws, double sumTW) {
        //List<String> FWCIs = new ArrayList<>();

        List<String> Is = new ArrayList<>(calculatedWls.keySet());
        for (int i = Is.size() - 1; i >= 0; i--) {
            Map<String, List<WNTreeNode>> Inext = new LinkedHashMap<>();
            //Map<String, List<WNTreeNode>> InextClone = new LinkedHashMap<>();
            String Xi = Is.get(i);
            List<WNTreeNode> wlXi = calculatedWls.getOrDefault(Xi, new ArrayList<>());

            for (int j = i - 1; j >= 0; j--) {
                String Xj = Is.get(j);
                List<WNTreeNode> wlXj = calculatedWls.getOrDefault(Xj, new ArrayList<>());

                if (isAncestor(wlXi, wlXj)) {
                    wlXi = WL_Intersection(wlXi, wlXj);
                    Xi = union(Xi, Xj);

                    for (String Xk : Inext.keySet()) {
                        String unionXkXj = union(Xk, Xj);
                        if (!Inext.containsKey(unionXkXj)) {
                            Inext.put(unionXkXj, new ArrayList<>());
                        }
                        //InextClone.put(unionXkXj, Inext.getOrDefault(Xk, new ArrayList<>()));
                    }
                    //InextClone = new LinkedHashMap<>(reverseOrder(InextClone));

                    if (calculateWeightedSupport(wlXi,sumTW) == calculateWeightedSupport(wlXj,sumTW)) {
                        Is.remove(j);
                        i--;
                    }
                    //Inext.clear();
                    //Inext.putAll(InextClone);
                } else {
                    List<WNTreeNode> wlXiXj = WL_Intersection(wlXi, wlXj);
                    if (calculateWeightedSupport(wlXiXj,sumTW) >= minws && !FWCIs.contains(Set.copyOf(wlXiXj))) {
                        String unionKey = union(Xi, Xj);
                        if (!Inext.containsKey(unionKey)) {
                            Inext.put(unionKey, wlXiXj);
                        }
                    }
                }
            }
            findFWCI(FWCIs,Inext, minws, sumTW);
            FWCIs.add(Xi);
        }
        return FWCIs;
    }

}