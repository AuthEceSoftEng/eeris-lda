package models;

public class WordPdfElement {

	private String text;
	private double weight;
	
	public WordPdfElement(){
		
	}
	
	public String getText() {
		return text;
	}
	public void setText(String term) {
		this.text = term;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double p) {
		this.weight = p;
	}
	
}
