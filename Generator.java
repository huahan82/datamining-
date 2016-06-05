/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data.mining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Generator {
	//cutoff threshold
	int highFrequency = 10000;
	int lowFrequency = 100;
	
	UEALite stemmer = new UEALite();
	Map<String, Integer> dictionaryMap = new HashMap<String, Integer>();
	List<ReuterFile> fileList = new ArrayList<ReuterFile>();
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		double startTime, endTime;
		double time;
		
		Generator gen = new Generator();
		
		System.out.println("Generating dictionary map and fileList...");
		startTime = System.currentTimeMillis();
		gen.mapGenerator();
		endTime = System.currentTimeMillis();
		time = (endTime - startTime) / 1000;
		System.out.println("Time for generating dictionary map and fileList: " + time + "s");
		
//		for(ReutersFile rf : gen.fileList){
//			rf.printFrequencyMap();
//		}
		
		System.out.println("Generating class labels...");
		startTime = System.currentTimeMillis();
		gen.generateClassLabels();
		endTime = System.currentTimeMillis();
		time = (endTime - startTime) / 1000;
		System.out.println("Time for generating class labels: " + time + "s");
		
		
		//gen.printDictionaryMap();
		System.out.println("Generating transaction data...");
		startTime = System.currentTimeMillis();
		gen.generateTransaction();
		endTime = System.currentTimeMillis();
		time = (endTime - startTime) / 1000;
		System.out.println("Time for generating transaction data: " + time + "s");
		
		System.out.println("Generating data matrix...");
		startTime = System.currentTimeMillis();
		gen.generateDataMatrix();
		endTime = System.currentTimeMillis();
		time = (endTime - startTime) / 1000;
		System.out.println("Time for generating data matrix: " + time + "s");
        
	}
	
	//generate the dictionary map and fileList
	public void mapGenerator() throws Exception{
		for(int i = 0; i <= 21; i++){
			String filePath = this.generateFilePath(i);
			StringBuffer sbr = this.readToStringBuffer(filePath);
			
			Pattern reutPattern = Pattern.compile("<REUTERS.+?</REUTERS>");
			Matcher reutMatcher = reutPattern.matcher(sbr);
			
			while (reutMatcher.find()) {

				String reutStr = sbr.substring(reutMatcher.start(), reutMatcher.end());
				//System.out.print(reutString);
				
				//getId
				Pattern idPattern = Pattern.compile("NEWID=\".+?\"");
				Matcher idMatcher = idPattern.matcher(reutStr);
				if (idMatcher.find()) {
					String newIdStr = reutStr.substring( idMatcher.start() + "NEWID=\"".length(), idMatcher.end() - "\"".length());
					reutFile.setFileId(Integer.parseInt(newIdStr));
				}
				
				//get topics
				List<String> topicList = new ArrayList<String>();
				String topicStr = "";
				
				Pattern topicsPattern = Pattern.compile("<TOPICS>.*?</TOPICS>");
				Matcher topicsMatcher = topicsPattern.matcher(reutStr);
				
				if (topicsMatcher.find()) {
					topicStr = reutStr.substring(topicsMatcher.start()
							+ "<TOPICS>".length(), topicsMatcher.end()
							- "</TOPICS>".length());

					// extract the d part from the topics string
					Pattern dPattern = Pattern.compile("<D>.+?</D>");
					Matcher dMatcher = dPattern.matcher(topicStr);
					String dString;
					while (dMatcher.find()) {
						dString = topicStr.substring( dMatcher.start() + "<D>".length(), dMatcher.end() - "</D>".length() );
						topicList.add(dString);
					}
					reutFile.setTopicList(topicList);
				}
				
				//get places
				List<String> placeList = new ArrayList<String>();
				String placeStr = "";
				
				Pattern placesPattern = Pattern.compile("<PLACES>.*?</PLACES>");
				Matcher placesMatcher = placesPattern.matcher(reutStr);
				if (placesMatcher.find()) {
					placeStr = reutStr.substring(placesMatcher.start()
							+ "<PLACES>".length(), placesMatcher.end()
							- "</PLACES>".length());

					// extract the d part from the topics string
					Pattern dPattern = Pattern.compile("<D>.+?</D>");
					Matcher dMatcher = dPattern.matcher(placeStr);
					String dString;
					while (dMatcher.find()) {
						dString = placeStr.substring( dMatcher.start() + "<D>".length(), dMatcher.end() - "</D>".length() );
						placeList.add(dString);
					}
					reutFile.setPlaceList(placeList);
				}
				
				//get body
				String bodyStr = "";
				Pattern bodyPattern = Pattern.compile("<BODY>.+?</BODY>");
				Matcher bodyMatcher = bodyPattern.matcher(reutStr);
				
				if (bodyMatcher.find()) {
					bodyStr = reutStr.substring(bodyMatcher.start() + "<BODY>".length(), bodyMatcher.end() - "</BODY>".length());
					// System.out.println(bodyStr);
				}
				
				//add words to dictionary and file 
				Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
				Scanner scanner = new Scanner(bodyStr);
				while (scanner.hasNext()) {
					String word = scanner.next().toLowerCase();
					word = this.deleteNonLetters(word);
					if(word.length() == 0) continue;
					
					word = this.stemmer.stem(word).getWord();

					if (!"".equals(word) && !Stopwords.isStopword(word)) {
						if(this.dictionaryMap.containsKey(word)){
							int count = this.dictionaryMap.get(word);
							this.dictionaryMap.put(word, count+1);
						}
						else{
							this.dictionaryMap.put(word, 1);
						}
						
						if(frequencyMap.containsKey(word)){
							int count = frequencyMap.get(word);
							frequencyMap.put(word, count+1);
						}
						else{
							frequencyMap.put(word, 1);
						}						
					}
				}
				reutFile.setFrequencyMap(frequencyMap);
				this.fileList.add(reutFile);
			}
		}
		
		//remove the words with frequency higher or lower than our preset threshold
		Map<String, Integer> newDictMap = new HashMap<String, Integer>();
		for (String s : this.dictionaryMap.keySet()) {
			int f = this.dictionaryMap.get(s);
			if (f > this.highFrequency || f < this.lowFrequency) {
				for (ReuterFile rf : this.fileList) {
					rf.getFrequencyMap().remove(s);
				}
			} else
				newDictMap.put(s, f);
		}
		this.dictionaryMap = newDictMap;
	}
	
	//generate the filepath according to the number
	public String generateFilePath(int i){
			// construct the 3-digit file number
			String num = "";
			if (i <= 9) num = "00" + i;
			else num = "0" + i;

			// construct the file path
			return "reuters/reut2-" + num + ".sgm";
	}
	
	//read file to StringBuffer
	public StringBuffer readToStringBuffer(String filePath) throws Exception {
		StringBuffer strBuffer = new StringBuffer();
		
		File inputFile = new File(filePath);
		FileReader in = new FileReader(inputFile);

		if (inputFile.canRead()) {
			BufferedReader buffer = new BufferedReader(in);
			String line = null;

			while ((line = buffer.readLine()) != null) {
				strBuffer.append(line + "\t");
			}

			buffer.close();
			in.close();
		}
		
		return strBuffer;
	}

	//delete non-letter characters in a word
	public String deleteNonLetters(String word){
		String newWord = "";
		for(int i = 0; i < word.length(); i++){
			char c = word.charAt(i);
			if(Character.isLetter(c)){
				newWord = newWord.concat(Character.toString(c));
			}
		}
		return newWord;
	}
	
	public void printDictionaryMap(){
		for(String s : this.dictionaryMap.keySet()){ 
			int frequency = this.dictionaryMap.get(s);
			System.out.print(s + ":" + frequency +" ");
		}
		System.out.println();
		System.out.println();
	}
	
	//write dataMatrix
	public void generateDataMatrix() throws Exception{
		File outFile = new File("output/DataMatrix.csv");
		FileOutputStream fl = new FileOutputStream(outFile);
		fl.write("id\\Words".getBytes());
		for(String s : this.dictionaryMap.keySet()){ 
			fl.write(("," + s).getBytes());
		}
		fl.write("\n".getBytes());
		for(ReuterFile rf : this.fileList){
			fl.write(Integer.toString(rf.getFileId()).getBytes());
			for(String s: this.dictionaryMap.keySet()){
				if(rf.getFrequencyMap().containsKey(s)){
					fl.write(("," + Integer.toString(rf.getFrequencyMap().get(s))).getBytes());
				}
				else fl.write(",0".getBytes());
			}
			fl.write("\n".getBytes());
		}
		fl.close();
	}
	
	//write transaction
	public void generateTransaction() throws Exception{
		File outFile = new File("output/Transactions.csv");
		FileOutputStream fl = new FileOutputStream(outFile);
		//fl.write("\n".getBytes());
		fl.write("id,words,places,topics\n".getBytes());
		for(ReuterFile rf : this.fileList){
			fl.write(Integer.toString(rf.getFileId()).getBytes());
			fl.write(",".getBytes());
			//items
			for(String s : rf.frequencyMap.keySet()){
				fl.write((s+" ").getBytes());
			}
			fl.write(",".getBytes());
			//places
			for(String s : rf.placeList){
				fl.write((s+" ").getBytes());
			}
			fl.write(",".getBytes());
			//topics
			for(String s : rf.topicList){
				fl.write((s+" ").getBytes());
			}
			fl.write("\n".getBytes());
		}
		fl.close();
	}
	
	//write class labels
	public void generateClassLabels() throws Exception{
		File outFile = new File("output/ClassLabels.txt");
		FileOutputStream fl = new FileOutputStream(outFile);
		//fl.write("\n".getBytes());
		for(ReuterFile rf : this.fileList){
			fl.write(Integer.toString(rf.getFileId()).getBytes());
			fl.write(":".getBytes());
			for(String s : rf.getTopicList()){
				fl.write((" " + s).getBytes());
			}
			fl.write("\n".getBytes());
		}
		fl.close();
	}
}