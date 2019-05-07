package models;

import java.util.ArrayList;
import java.util.List;

public class Document {

	private int id;
	private String title;
	private String type;
	private String typeName;
	private List<String> authors;
	private int year;
	private String text;
	private String analyzedText;
	
	public Document(){
		this.authors = new ArrayList<String>();
	}
	
	public List<String> getAuthors() {
		return authors;
	}

	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
	
	public void addAuthor(String author) {
		this.authors.add(author);
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getId(){
		return this.id;
		}
	
	public void setId(int id){
		this.id = id;
		}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getYear(){
		return this.year;
	}
	
	public void setYear(int year){
		this.year = year;
	}

	public String getAnalyzedText() {
		return analyzedText;
	}

	public void setAnalyzedText(String analyzedText) {
		this.analyzedText = analyzedText;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void clearFields() {
		this.id = -1;
		this.title = "";
		this.type = "";
		this.typeName = "";
		this.authors = new ArrayList<String>();
		this.year = -1;
		this.text = "";
		this.analyzedText = "";
		
	}
	
}
