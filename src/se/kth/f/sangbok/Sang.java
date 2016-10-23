package se.kth.f.sangbok;

import java.io.Serializable;
import java.util.Comparator;

/*
 Class to hold all the relevant information about a song!
 */
public class Sang implements Serializable {
	//Serial number to verify that sender and receiver of serialized objects have loaded the same class
	private static final long serialVersionUID = 12345666133789L;
	
	
	//Private variables defining a song
	private String title;
	private  String melody;
	private String text;
	private String author;
	private int chapter;
	private int number;
	
	
	//Set and get functions
	public String getTitle(){
		return title;
	}
	
	public String getMelody(){
		return melody;
	}
	
	public String getText(){
		return text;
	}
	
	public String getAuthor(){
		return author;
	}
	
	public int getChapter(){
		return chapter;
	}
	
	public int getNumber(){
		return number;
	}
	
	public void setTitle(String ti){
		title = ti;
	}
	
	public void setMelody(String mel){
		melody = mel;
	}
	
	public void setText(String tex){
		text = tex;
	}
	
	public void setAuthor(String auth){
		author = auth;
	}
	
	public void setChapter(int chpt){
		chapter = chpt;
	}
	
	public void setNumber(int numb){
		number = numb;
	}
	
	
	//If a method tries to evoke toString, return the title
	public  String toString() {
		return title;
	}
	
	
	//Default constructor. Initialize the variables.
	public Sang() {
		title = "";
		melody = "";
		text = "";
		author = "";
		chapter = -1;
		number = -1;
	}
	//Constructor with input-strings
	public Sang(String ti, String mel, String tex, String auth, int chpt, int numb) {
		title = ti;
		melody = mel;
		text = tex;
		author = auth;
		chapter = chpt;
		number = numb;
	}
	
	
	//Comparators for sorting songs
	//Based on Chapter and Number
    static Comparator<Sang> getChapterComparator() {
        return new Comparator<Sang>() {
			@Override
			public int compare(Sang arg0, Sang arg1) {
				if( arg0.getChapter() < arg1.getChapter() ) return -1;
				if( arg0.getChapter() > arg1.getChapter() ) return 1;
				if( arg0.getNumber() < arg1.getNumber() ) return -1;
				if( arg0.getNumber() > arg1.getNumber() ) return 1;
				return 0;
			}
        };
    }  
    //Based on Alphabetic order of the titles
    static Comparator<Sang> getTitleComparator() {
        return new Comparator<Sang>() {
			@Override
			public int compare(Sang lhs, Sang rhs) {
				return lhs.getTitle().compareTo(rhs.getTitle());
			}
        };
    }
    //Reversed Alphabetic order of the titles
    static Comparator<Sang> getRevTitleComparator() {
        return new Comparator<Sang>() {
			@Override
			public int compare(Sang lhs, Sang rhs) {
				return -(lhs.getTitle().compareTo(rhs.getTitle()));
			}
        };
    }
}
