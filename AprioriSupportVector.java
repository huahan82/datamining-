
package data.mining;
import java.util.*;
/**
 * run Apriori alg on supportVectors generated from text mining
 * @author hua
 */
public class AprioriSupportVector {
        Vector<String> allKeyWords; //all keywords in selected files
        Map<Integer, Vector<Integer>> supportVectorMap = new HashMap<>();
        double support;
        double confidence;
        double cutoff;
        int size; 
        //the counts of the articles containing all keywords
        Vector <Integer> count = new Vector();
        //the map of all freqItem sets at levels defined by the size of the sets
        Map<Integer, Set<Set<String>>> levelFreqItemMap = new HashMap<>();
        //a map storing all frequent item sets as keys and their support values as values
        Map<Set<String>, Integer> resultFreqItemMap = new LinkedHashMap<>();
        //the set containing finally generated correlation rules
        Set<Vector> correlationSet = new HashSet<>();
        
        /*constructor */
        public AprioriSupportVector(Vector<String> allKeyWords, double support, 
                double confidence, Map<Integer, Vector<Integer>> supportVectorMap) {
            this.allKeyWords = allKeyWords;
            this.confidence = confidence;
            this.supportVectorMap = supportVectorMap;
            this.support = support;
        }
        
        /*
         * All  sets containing oneItem selected with support value
         */
        public void oneItem() {
            Set<Set<String>> oneKeyWordSet = new HashSet<>();
            cutoff = support * supportVectorMap.size();
            for( int i = 0; i<allKeyWords.size(); i++) {
            count.add(0) ;           
            }
            for (int i: supportVectorMap.keySet()) {
                Vector<Integer> vector =supportVectorMap.get(i);
                for(int j = 0; j< vector.size(); j++) {
                    count.set(j, count.elementAt(j)+vector.elementAt(j));
                }                
            }

            for (int j =0; j<count.size(); j++) {
                if (count.elementAt(j)>= cutoff) {
                    Set<String> oneWord = new HashSet<>();
                    oneWord.add(allKeyWords.elementAt(j));
                    oneKeyWordSet.add(oneWord);
                    resultFreqItemMap.put(oneWord,count.elementAt(j) );
                }       
            }

            //put all one keyword set in levleFreqMap
        levelFreqItemMap.put(0, null);
        levelFreqItemMap.put(1, oneKeyWordSet);  
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
     * scan the supportVectorMap and find the candidiateSet with frequencies higher than
     * support and put the scannedItemSet into Map
     * @param candidateSet candidates selected from canItemSet
     * @param x the no. of items in the candidateSet
     * @return
     * @throws SQLException 
     */
    
    public boolean scannedItemSet(Set<Set<String>> candidateSet, int x) {
        Set<Set<String>> freqItemSets = new HashSet<>();
        for (Set<String> itemSet : candidateSet) {
            int countSet = 0;
            for(int id : supportVectorMap.keySet()) {
                int containsSet = 1;
                for (String keyWord: itemSet) {
                    containsSet *= supportVectorMap.get(id).elementAt(allKeyWords.indexOf(keyWord));
                    if (containsSet == 0)
                        break;
                }
                countSet += containsSet;
            }
            if (countSet >= cutoff) {
                freqItemSets.add(itemSet);
                resultFreqItemMap.put(itemSet,countSet);
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
        int setSize = itemSet.size();
        if (setSize >= 2) {
            for (String aItem : itemSet) {
                aItemSet = new HashSet<>();
                aItemSet.add(aItem);
                setOfSubSets.add(aItemSet);
                //       System.out.println(testLevel.contains(aItemSet));
                if (!testLevel.contains(aItemSet)) {
                    return false;
                }
            }
            for (int i = 2; i < setSize; i++) {
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
    
        public void runApriori() {
       // size = supportVectorMap.size(); 
       // cutoff = support*size;
        oneItem();
       // int i =2;
       for (int i = 2; i <= allKeyWords.size(); i++) {
            Set<Set<String>> temp1 = itemSetGen(i);
            // System.out.println("temp1" + temp1);
            Set<Set<String>> temp = canItemSet(temp1);
          //  scannedItemSet(temp,i);
            if (!scannedItemSet(temp, i)) {
                break;
            }
        }
    }
        /*
         * Generate all association rules from Apriori algorithm
         * 
         */
    public void associationPrint() {
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
                        float supp = (float) sup1 / supportVectorMap.size();
                        float conf = (float) sup1 / sup2;
                        if (conf >= confidence) {
                           Vector correlation = new Vector();

                            correlation.add(causeSet);
                            correlation.add(resultSet);
                            correlation.add(supp);
                            correlation.add(conf);
                            correlationSet.add(correlation);
                        }
                    }
                }
            }
        }
    }
   
}
