
package data.mining;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
/**
 * This is a method to find association rules in all articles from a sgm file
 * stanford coreNLP was used to create tokens and predefined stop words was read
 * from a file stopWords.txt. The keywords were then selected with tf-idf method.
 * an support vector of keywords was created for each article in the sgm file.
 * The association rules among all the articles were generated with Apriori method. 
 * @author hua
 */
public class TextMining {
    
    ArrayList<String> stopwords = new ArrayList<>(); // all stopwords read from a file
    String fileName; // the sgm file to be analyzed
    String outPutFile; // the output xml file
    double threshold;  // the threshold to select keywords
    Integer totalWords; //total no. of words in an article
    // The Map of all articles' frequency map <Id, Map<keyword, count>>
    Map<Integer, Map<String,Integer>> frequencyMap = new HashMap<>();
    // The Map of all articles' totalWords <Id, total no. of words>
    Map<Integer,Integer> totalWordsMap = new HashMap<>();
    Vector<String> allKeyWords = new Vector<>(); //all keywords in selected files
    Map<Integer, Vector<String>> keyWordMap = new HashMap<>(); //the map of each file's keywords
    Map<Integer, Vector<Integer>> supportVectorMap = new HashMap<>(); //the map of each file's keyword vector
  
    
    /*
     * constructor 
     * @param fileName the the name of the file to be analyzed
     * @Param threshold the threshold in tf-idf alg to select keywords
     */
    public TextMining(String fileName, double threshold){
        this.fileName = fileName;
        this.threshold=threshold;
    }
     
        
    //get articles out from the sgm file
    public StringBuffer read(String fileName) throws FileNotFoundException, IOException {
        File inputFile = new File(fileName);
        FileReader in = new FileReader(inputFile);
        StringBuffer strBuffer;
        strBuffer = new StringBuffer();
        if (inputFile.canRead()) {
			BufferedReader buffer = new BufferedReader(in);
			String line = null;
			while ((line = buffer.readLine()) != null) {
				strBuffer.append(line).append("\t");
			}
			buffer.close();
			in.close();  
		}
        return strBuffer;
    }
    
    
    /*
     * extract articles from content and seperate the article Id and article contents
     * the articles can be optionally saved to txt files.
     * @param strBuffer the stringBuffer of all contents of the file after reading.
     */ 
    
    public void extract(StringBuffer strBuffer) throws SAXException, 
            IOException, ParserConfigurationException {
        this.getstopWords();      
     	Pattern reutPattern = Pattern.compile("<REUTERS.+?</REUTERS>");
	Matcher reutMatcher = reutPattern.matcher(strBuffer);
        while (reutMatcher.find()) {
            String reutStr = strBuffer.substring(reutMatcher.start(), reutMatcher.end());
            String newIdStr = "";
            String fileStr = "";
        Pattern filePattern = Pattern.compile("<BODY>(.*?)</BODY>");
        Matcher fileMatcher = filePattern.matcher(reutStr);
        
        Pattern idPattern = Pattern.compile("NEWID=\".+?\"");
        Matcher idMatcher = idPattern.matcher(reutStr);

        if (idMatcher.find()) {
		newIdStr = reutStr.substring( idMatcher.start() + "NEWID=\"".length(),
                        idMatcher.end() - "\"".length());
				}
        if (fileMatcher.find()) {
		fileStr = reutStr.substring(fileMatcher.start() + "<BODY>".length(), fileMatcher.end() - "</BODY>".length());
         }
        frequencyMap.put(Integer.parseInt(newIdStr), this.preprocess(newIdStr, fileStr));
        totalWordsMap.put(Integer.parseInt(newIdStr), totalWords);
        
        /* an optional save of all articles*/
        File dir = new File("C:\\Users\\huahan\\Documents\\NetBeansProjects\\reuters") ;
        String outFileName = newIdStr + ".txt";
        this.save(dir, outFileName, fileStr);
        }
    }
        
  /*
   * remove stopwords and numbers and generate a frequency map of lemmas of a file
   * @param Id is the newId of an article
   * @param fileStr is the content of an article
   */
    public Map<String, Integer> preprocess(String Id, String fileStr) throws IOException, SAXException, ParserConfigurationException {
        Map<String, Integer> singleMap = new HashMap<>();
        totalWords = 0;
 //       File dic =new File( "C:\\Users/huahan\\Documents\\NetBeansProjects\\reuters\\");
  //      String name =Id + "Output.xml";
 //       File outFile = new File(dic, name); 
 //       PrintWriter xmlOut = new PrintWriter(outFile);
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotation = new Annotation(fileStr);
        pipeline.annotate(annotation);
//        pipeline.xmlPrint(annotation, xmlOut);
          List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
            for (CoreLabel token : tokens) {
                if(!isNum(token.word().substring(0,1)) &&!stopwords.contains(token.word().toLowerCase())){  
                    if(singleMap.containsKey(token.lemma())){
				int count = singleMap.get(token.lemma());
				singleMap.put(token.lemma(), count+1);
						}
                    else{
				singleMap.put(token.lemma(), 1);
						}
                    totalWords ++;
                }
            }   
  return singleMap;
       }
    
/*Get stopwords from stop_words.txt and stored in arrayList stopwords*/
    public void getstopWords(){
       String file = "C://Users//huahan//Documents//NetBeansProjects//stop_words.txt";
           try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
            stopwords.add(line);     
            }
            br.close();
           } catch (IOException ex) {
               Logger.getLogger(TextMining.class.getName()).log(Level.SEVERE, null, ex);
           } 
        } 
    
    /*
     * verify if a lemma is a number or not
     * @param string a word generated by tokenization with preprocess()
     */
    public boolean isNum(String string) {
        try {
		Integer value = Integer.parseInt(string);
		return true;
	} catch (NumberFormatException e) {
            return false;
	}
    }
    

    
    /*
     * identify keywords of each file with if-idf algorithm
     * @param threshold is predefined by user 
     */
    public void keywordsGen (double threshold) {
        double tf;
        double idf;
        int totalWordInFile = 0;
        int totalFileNum = frequencyMap.size();
        for (int i: frequencyMap.keySet()) {
            Map<String,Integer> singleMap = frequencyMap.get(i);
            Vector<String> keyWords = new Vector<>();
            totalWordInFile= totalWordsMap.get(i);
            for(String lemma: singleMap.keySet()){
                //System.out.println(singleMap.get(lemma));
                tf = (double)singleMap.get(lemma)/(double)totalWordInFile;
                int countFile = 0; //the no. of articles containing the keyword
                for(Map<String, Integer> fileWithWord: frequencyMap.values()) {
                    if (fileWithWord.containsKey(lemma))
                            countFile++;
                }
                idf = Math.log10(totalFileNum/countFile);
                //System.out.print(tf +" "+ idf +" "+ tf*idf +"\n");
                if (tf*idf >= threshold) {
                    keyWords.add(lemma);
                    if (!allKeyWords.contains(lemma))
                    allKeyWords.add(lemma);  
                }
            }
            keyWordMap.put(i, keyWords); 
        }     
    }
    

    
    /*
     * the method to read a file and create support vectors for each articles in the file
     */
    public void TextMining() 
            throws FileNotFoundException, IOException, SAXException, ParserConfigurationException{
        this.extract(this.read(fileName));
        this.keywordsGen(threshold);
        for (int id : keyWordMap.keySet()) {
            Vector<Integer> supportVector = new Vector<>(); 
            for (String keyWord: allKeyWords) {
                supportVector.add((keyWordMap.get(id)).contains(keyWord)? 1: 0);
            }
            supportVectorMap.put(id, supportVector);               
        }
    }
    
    /*
     * save an artile from  a string
     * @param dic the dictionary of the newly created file
     * @param outFileName the name of the newly created file
     * @param the content of the newly created file
     */
    public void save(File dir, String outFileName, String fileStr){
        try {
            File outFile = new File(dir, outFileName);
            FileWriter out = new FileWriter(outFile);
            out.write(fileStr);
            out.close();         
             } catch (IOException ex) {
                 Logger.getLogger(TextMining.class.getName()).log(Level.SEVERE, null, ex);
             } 
        }

    public static void main(String[] args) throws FileNotFoundException, 
            IOException, SAXException, ParserConfigurationException{
        String dir1 = "C:/Users/huahan/Documents/NetBeansProjects/reuters21578/";
        String fileName = "reut2-001.sgm";
        double threshold = 0.1;
        double support = 0.03;
        double confidence = 0.6;

        TextMining tm = new TextMining(dir1+ fileName,threshold);
        tm.TextMining();
        AprioriSupportVector as = new AprioriSupportVector(tm.allKeyWords, support, 
                confidence, tm.supportVectorMap);
        as.runApriori();
        as.associationPrint();
        System.out.println("the threshold to select keywords: " + tm.threshold);
        System.out.println("total number of keywords selected:" + tm.allKeyWords.size());
        System.out.println("Support: " + support + "; Confidence: "+ confidence);
        System.out.println();
        System.out.println("Selected frequent set: number of files containing the sets");
        for (Set<String> freqItem: as.resultFreqItemMap.keySet()) 
            System.out.println(freqItem + ":" + as.resultFreqItemMap.get(freqItem));
        System.out.println();
        System.out.println("All associations in files:");
        System.out.println("[Cause]==>[Result], Support, Confidence");
        for (Vector correlation : as.correlationSet) {
            System.out.println(correlation.get(0)+ "==>" +correlation.get(1)+ ", "
                    + correlation.get(2)
                    +", "+ correlation.get(3));
        }         
    }
}
