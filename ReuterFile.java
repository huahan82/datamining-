package data.mining;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReuterFile {

	int fileId;
	List<String> topicList = new ArrayList<String>();
	List<String> placeList = new ArrayList<String>();
	Map<String, Integer> frequencyMap = new HashMap<String, Integer>();
	
	public List<String> getPlaceList() {
		return placeList;
	}

	public void setPlaceList(List<String> placeList) {
		this.placeList = placeList;
	}

	
	
	public void printFrequencyMap(){
		System.out.print(fileId + " ");
		for(String s : this.frequencyMap.keySet()){ 
			int frequency = this.frequencyMap.get(s);
			System.out.print(s + ":" + frequency +" ");
		}
		System.out.println();
		System.out.println();
	}
	
	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public List<String> getTopicList() {
		return topicList;
	}

	public void setTopicList(List<String> topicList) {
		this.topicList = topicList;
	}



	public Map<String, Integer> getFrequencyMap() {
		return frequencyMap;
	}

	public void setFrequencyMap(Map<String, Integer> frequencyMap) {
		this.frequencyMap = new HashMap<String, Integer>(frequencyMap);
	}


}
