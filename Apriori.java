package data.mining;

import java.sql.*;
import java.util.*;

/**
 * This is a implementation of Apiroi Algorithm and association results were 
 * generated based on this
 * @author huahan
 */
public class Apriori {

    String database; //the database that the algorithms will work on
    double support; //the support value used in the algorithm
    double confidence; // the confidence value for gtenerate association
    double cutoff; // the cutoff value to select frequent item sets
    DbScanning scan = new DbScanning(); //database scanning instance
    Integer countTransaction = 0; //total no. of transactions in the database
    
   // Set<String> totalItems = new TreeSet(); // total no. of items in the database
    Set<String> items = new TreeSet<>();  //item set contaning all items in the table
    

    //frequent item sets selected based on support value
    Set<Set<String>> freqItemSets = new TreeSet<>(); 
    //a map storing the no. of items of the frequent item sets
    Map<Integer, Set<Set<String>>> levelFreqItemMap = new LinkedHashMap<>();
    //a map storing all frequent item sets as keys and their support values as values
    Map<Set<String>, Integer> resultFreqItemMap = new LinkedHashMap<>();
    
    // resultCorrelation for {A} => {B} [support,  confidence]
    Map<Map<Map<Set<String>, Set<String>>, Float>, Float> resultCorrelation = new LinkedHashMap<>();

    
    public Apriori(String db, double support, double confidence) {
        database = db;
        this.support = support;
        this.confidence = confidence;

    }

    /**
     * totalItems count all items in the given database and generate a set
     * contains all items in the database
     * @throws SQLException
     */
    public void totalItems() throws SQLException {
        String transaction[] = new String[10];
        scan.connect();
        ResultSet rs = scan.transaction(database);
        while (rs.next()) {
            for (int i = 2; i <= 10; i++) {
                transaction[i] = rs.getString(i);
                if (transaction[i] == null) {
                    break;
                }
                items.add(transaction[i]);                    
            }
            countTransaction++;
        }
        scan.disconnect();
    }
    
    /**
     * frequent item set containing only one item generated 
     * @throws SQLException 
     */

    public void oneItemSet() throws SQLException {
            
        Set<String> tempfreqItemSet;
        Set<Set<String>> tempfreqItemSets = new HashSet<>();
        freqItemSets = new HashSet<>();
        Map<Set<String>, Integer> tempFreqItemMap = new LinkedHashMap<>();
        String transaction[] = new String[10];
        
        /*Read from database and generate tempfreqItemSets and
         * tempfreqItemMap*/
        
        scan.connect();
        ResultSet rs = scan.transaction(database);
        while (rs.next()) {
            for (int i = 1; i <= 10; i++) {
                transaction[i] = rs.getString(i);
                if (transaction[i] == null) {
                    break;
                }
                if (i >= 2) {
                    tempfreqItemSet = new HashSet<>();
                    tempfreqItemSet.add(transaction[i]);
                    // System.out.println(tempfreqItemSet);
                    if (!tempFreqItemMap.containsKey(tempfreqItemSet)) {
                        tempFreqItemMap.put(tempfreqItemSet, 1);
                        tempfreqItemSets.add(tempfreqItemSet);
                    } else {
                        tempFreqItemMap.put(tempfreqItemSet, tempFreqItemMap.get(tempfreqItemSet) + 1);
                    }

                }
            }
        }
        scan.disconnect();
        
/*Select for freqItemSets based on cutoff and put them in freItemItemSets
 and freqItemMap*/
        cutoff = countTransaction * support;
        //System.out.println("cutoff" + cutoff);
        Iterator<Set<String>> it = tempfreqItemSets.iterator();
        while (it.hasNext()) {
            Set<String> itemSet = it.next();
            if (tempFreqItemMap.get(itemSet) >= cutoff) {
                freqItemSets.add(itemSet);
                resultFreqItemMap.put(itemSet, tempFreqItemMap.get(itemSet));
            }
        }
        levelFreqItemMap.put(0, null);
        levelFreqItemMap.put(1, freqItemSets);
    }

    /**
     * use the frequent item set generated during i-1 cycle to generate the new
     * frequent item set at the i cycle
     * @param i the number of items in a set
     * @return resultSet the result item sets containing i items
     */
    public Set<Set<String>> itemSetGen(Integer i) {
        Set<Set<String>> baseSet = levelFreqItemMap.get(i - 1);
        //System.out.println(baseSet);
        Set<Set<String>> baseSet2 = levelFreqItemMap.get(i - 1);
        Set<String> joinSet;
        Set<Set<String>> resultSet = new HashSet<>();
        if (i <= 1) {
            System.out.println("Cannot Join");
        } else {
            for (Set<String> itemSet : baseSet) {
                for (Set<String> itemSet2 : baseSet2) {
                    joinSet = new TreeSet<>();
                    if (!(itemSet.equals(itemSet2))) {

                        joinSet.addAll(itemSet);
                        joinSet.addAll(itemSet2);
                    }
                    if (joinSet.size() == i) {
                        resultSet.add(joinSet);
                    }
                }
            }
        }
        return resultSet;
    }

    /**
     * candidate itemSets selection based on whether all subsets were belong to
     * the resultFreqItemMap
     * @param resultSet results from item set creation from itemSetGen
     * @throws Exception
     * @return candidateSet 
     */
    public Set<Set<String>> canItemSet(Set<Set<String>> resultSet) {

        Set<Set<String>> candidateSet = new HashSet<>();

        for (Set<String> itemSet : resultSet) {
            if (testSubSets(itemSet)) {
                candidateSet.add(itemSet);
            }
        } 
        return candidateSet;
    }
    
    

    /**
     * scan the database and find the candidiateSet with frequencies higher than
     * support and put the scannedItemSet into Map
     * @param candidateSet candidates selected from canItemSet
     * @param x the no. of items in the candidateSet
     * @return
     * @throws SQLException 
     */
    

    public boolean scannedItemSet(Set<Set<String>> candidateSet, int x)
            throws SQLException {

        Set<Set<String>> tempfreqItemSets = new HashSet<>();
        Set<String> aLineSet;
        freqItemSets = new HashSet<>();
        Map<Set<String>, Integer> tempFreqItemMap = new LinkedHashMap<>();
        String transaction[] = new String[10];
        scan.connect();
        ResultSet rs = scan.transaction(database);
        while (rs.next()) {
            aLineSet = new HashSet<>();
            for (int i = 2; i <= 10; i++) {
                transaction[i] = rs.getString(i);
                if (transaction[i] == null) {
                    break;
                }
                aLineSet.add(transaction[i]);
            }
            for (Set<String> itemSet : candidateSet) {
                if (aLineSet.containsAll(itemSet)) {
                    if (!tempFreqItemMap.containsKey(itemSet)) {
                        tempFreqItemMap.put(itemSet, 1);
                        tempfreqItemSets.add(itemSet);
                    } else {
                        tempFreqItemMap.put(itemSet,
                                tempFreqItemMap.get(itemSet) + 1);
                    }
                }

            }
        }
        scan.disconnect();

        for (Set<String> itemSet : tempfreqItemSets) {
            if (tempFreqItemMap.get(itemSet) >= cutoff) {
                freqItemSets.add(itemSet);
                resultFreqItemMap.put(itemSet, tempFreqItemMap.get(itemSet));
            }
        }

        if (!freqItemSets.isEmpty()) {
            levelFreqItemMap.put(x, freqItemSets);
            // System.out.println(freqItemSets);
            return true;
        } else {
            return false;
        }
    }
/**
 * test whether allSubSets of an itemSet belong to the freqitemMap
 * @param itemSet
 * @return 
 */
    public boolean testSubSets(Set<String> itemSet) {
        Set<Set<String>> setOfSubSets = new HashSet<>();
        Set<Set<String>> tempSetOfSubSets = new HashSet<>();
        Set<Set<String>> testLevel = levelFreqItemMap.get(1);
        Set<String> aItemSet;
        int size = itemSet.size();
        if (size >= 2) {
            for (String aItem : itemSet) {
                aItemSet = new HashSet<>();
                aItemSet.add(aItem);
                setOfSubSets.add(aItemSet);
                //       System.out.println(testLevel.contains(aItemSet));
                if (!testLevel.contains(aItemSet)) {
                    return false;
                }
            }
            for (int i = 2; i < size; i++) {
                testLevel = levelFreqItemMap.get(i);

                for (Set<String> subSet1 : setOfSubSets) {
                    for (Set<String> subSet2 : setOfSubSets) {

                        aItemSet = new HashSet<>();
                        aItemSet.addAll(subSet1);
                        aItemSet.addAll(subSet2);
                        if (aItemSet.size() == i) {
                            if (!testLevel.contains(aItemSet)) {
                                return false;
                            }
                            tempSetOfSubSets.add(aItemSet);
                        }
                    }
                }
                setOfSubSets.clear();
                setOfSubSets.addAll(tempSetOfSubSets);
            }

        }
        return true;
    }

    /**
     * all subsets generation
     * @param aSet a set which contains at leat 2 items
     * @return a set containing all subsets of a given set
     */
    public Set getSubsets(Set<String> aSet) {
        Set<Set<String>> allsubSets = new HashSet<>();
        TreeSet<String> emptySet = new TreeSet<>();
        TreeSet<String> reducedSet = new TreeSet<String>();
        String firstEle;
        reducedSet.addAll(aSet);

        if (reducedSet.size() == 1) {
            allsubSets.add(emptySet);
            allsubSets.add(reducedSet);
        } else {
            firstEle = reducedSet.pollFirst();
            Set<String> aSubSet = new TreeSet<>();

            aSubSet.add(firstEle);
            allsubSets.add(aSubSet);
            Set<Set<String>> partSubSets = getSubsets(reducedSet);
            allsubSets.addAll(partSubSets);
            for (Set<String> subSet : partSubSets) {
                Set<String> tempSubSet = new TreeSet<>();
                tempSubSet.addAll(subSet);
                tempSubSet.add(firstEle);
                allsubSets.add(tempSubSet);
            }
        }
        return allsubSets;
    }
    
    /**
     * Apriori algorithm to find of frequent item sets
     * @throws SQLException 
     */

    public void runApriori() throws SQLException {
        totalItems();
        oneItemSet();
        for (int i = 2; i <= items.size(); i++) {
            Set<Set<String>> temp1 = itemSetGen(i);
            // System.out.println("temp1" + temp1);
            Set<Set<String>> temp = canItemSet(temp1);
            if (!scannedItemSet(temp, i)) {
                break;
            }
        }
    }
    /**
     * association results based on all frequent item sets
     */

    public void association() {
        int limit = levelFreqItemMap.size();
        for (int i = 2; i < limit; i++) {
            for (Set aSet : levelFreqItemMap.get(i)) {
                Set<Set> subSets = getSubsets(aSet);
                subSets.remove(aSet);                      //remove the set itself
                subSets.remove(levelFreqItemMap.get(0));    //remove the empty set
                for (Set bSet : subSets) {
                    Set causeSet = new HashSet();
                    causeSet.addAll(bSet);
                    Set resultSet = new HashSet();
                    resultSet.addAll(aSet);
                    resultSet.removeAll(bSet);
                    if (resultFreqItemMap.containsKey(causeSet)
                            && resultFreqItemMap.containsKey(resultSet)) {
                        int sup1 = resultFreqItemMap.get(aSet);
                        int sup2 = resultFreqItemMap.get(causeSet);
                        float supp = (float) sup1 / countTransaction;

                        float conf = (float) sup1 / sup2;
                        if (conf >= confidence) {
                            Map<Set<String>, Set<String>> correlation = new HashMap<>();
                            Map<Map<Set<String>, Set<String>>, Float> correlation2 = new HashMap<>();
                            correlation.put(causeSet, resultSet);
                            correlation2.put(correlation, supp);
                            resultCorrelation.put(correlation2, conf);
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Apriori test = new Apriori("target", 0.2, 0.7);
        test.runApriori();
        test.association();

        System.out.println("All items in database: " + test.items); //all the items in the database
        System.out.println("cutoff frequency " + test.cutoff);
        System.out.println("Selected frequency sets and their frequency value");
        System.out.println(test.resultFreqItemMap);
        System.out.println("frequency sets are classified different number of items");       
        System.out.println(test.levelFreqItemMap);
        System.out.println("Association generated from frequence sets:");
        System.out.println("causetset=resultset=support(in percentage)=confidence");
        System.out.println(test.resultCorrelation);
    }
}
