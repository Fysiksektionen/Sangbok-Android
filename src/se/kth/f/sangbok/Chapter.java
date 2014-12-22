package se.kth.f.sangbok;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/*
 Class to hold all the relevant information about a song!
 */
public class Chapter implements Serializable {
	//Serial number to verify that sender and receiver of serialized objects have loaded the same class
	private static final long serialVersionUID = 12245666133789L;
	
	
	//Private variables defining a song
	private List<Sang> sangsInThisChapter;
	private  String chapterName;
	private int chapterNumber;
	
	
	//Set and get functions
	public String getName(){
		return chapterName;
	}
	
	public int getNumber(){
		return chapterNumber;
	}
	
	public List<Sang> getSangs(){
		return sangsInThisChapter;
	}
	
	public void setName(String chptNm){
		chapterName = chptNm;
	}
	
	public void setNumber(int chptNum){
		chapterNumber = chptNum;
	}
	
	public void setSangList(List<Sang> chptSangs){
		sangsInThisChapter = chptSangs;
	}
	
	//If a method tries to evoke toString, return the title
	public  String toString() {
		return chapterName;
	}
	
	
	//Default constructor. Initialize the variables.
	public Chapter() {
		sangsInThisChapter = null;
		chapterName = "";
		chapterNumber = -1;
	}
	//Constructor with input-strings
	public Chapter(String chptName, int chptNum, List<Sang> chptSangs) {
		sangsInThisChapter = chptSangs;
		chapterName = chptName;
		chapterNumber = chptNum;
	}
	
	//Comparators for sorting chapters
    //Based on Alphabetic order of the titles
    static Comparator<Chapter> getNumberComparator() {
        return new Comparator<Chapter>() {
			@Override
			public int compare(Chapter lhs, Chapter rhs) {
				if( lhs.getNumber() < rhs.getNumber() ){ return -1; }
				if( lhs.getNumber() > rhs.getNumber() ){ return 1; }
				return 0;
			}
        };
    }
}
