import java.util.*;
import WNAlgorithm;
public class Main {
    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        WNAlgorithm newWNA = new WNAlgorithm();

        List<List<Object>> weightedData = newWNA.generateWeightedData(20, 7);
        double minWS = 0.2 ;
        System.out.println("weightedData: "+  weightedData);


        List<Double> transactionWeights = newWNA.calculateTransactionWeights(weightedData);

        System.out.println("transactionWeights: "+ transactionWeights);

        double sumTW=transactionWeights.stream().mapToDouble(Double::doubleValue).sum();

        System.out.println("=> " +sumTW);

        List<Map.Entry<String, Double>> sortedItemSupports = newWNA.calculateAndSortItemSupports(weightedData, transactionWeights);
        System.out.println("\nSorted Item Supports:");
        for (Map.Entry<String, Double> entry : sortedItemSupports) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        weightedData = newWNA.removeItemsByWeightThreshold(weightedData, sortedItemSupports, minWS);


        List<AbstractMap.SimpleEntry<List<String>, Double>> sortedTransactionsAndWeights =
                newWNA.sortTransactionsAndWeights(weightedData, sortedItemSupports,transactionWeights);
        System.out.println("\nSorted Transactions and Weights based on Sorted Item Supports:");
        for (AbstractMap.SimpleEntry<List<String>, Double> entry : sortedTransactionsAndWeights) {
            System.out.println("Transaction: " + entry.getKey() + ", Weight: " + entry.getValue());
        }
        
        List<String> sortedItems = new ArrayList<>();  // New list to store extracted strings
        for (Map.Entry<String, Double> entry : sortedItemSupports) {
            String itemName = entry.getKey();
            sortedItems.add(itemName);
        }
        
        WNTreeNode wnTree = newWNA.buildWNTree(sortedTransactionsAndWeights);
        wnTree.assignPrePos();
        System.out.println("WNTree:");
        WNAlgorithm.printWNTree(wnTree, "", true);


        Map<String, List<WNTreeNode>> allWL = wnTree.calculateAllWL();
        allWL.remove("");
        // Print WL for each item
        for (Map.Entry<String, List<WNTreeNode>> entry : allWL.entrySet()) {
            String itemName = entry.getKey();
            List<WNTreeNode> itemWL = entry.getValue();

            System.out.println("WL(" + itemName + "):");
            for (WNTreeNode node : itemWL) {
                System.out.println("(" + node.pre + ", " + node.pos + ", " + node.weight + ")");
            }
            System.out.println();
        }
        //System.out.println(allWL);

        List<String>  FWCIs = new ArrayList<>();

        List<String> result = newWNA.NFWCI(FWCIs,allWL,minWS,sumTW );
        System.out.println("Frequent Weighted Closed Itemsets: " + result);

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("Elapsed time in milliseconds: " + elapsedTime);
        
    }

}
