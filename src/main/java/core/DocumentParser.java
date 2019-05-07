package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import models.Document;

public class DocumentParser {

	String typeName;
	int docCounter;
	ElasticsearchManager indexManager;
	
	public DocumentParser(String typeName) {
		
		this.typeName = typeName;
		indexManager = new ElasticsearchManager();
		docCounter = indexManager.getDocumentsCount(typeName);
		
	}
	
	void parseDocuments(String directory) throws IOException {
		
		// Create variables
		File folder = new File(directory);
		File file = new File(folder.getPath()+"\\dataset.ris"); 
    	File[] listOfFiles = folder.listFiles();
		@SuppressWarnings("resource")
		BufferedReader br = new BufferedReader(new FileReader(file));
		PrintWriter out = new PrintWriter(folder.getAbsolutePath()+"\\processed.txt");
		String st;
		int renameCounter = 0;
		int countAbstract = 0;
		int countReferences = 0;
		Document doc = new Document();
		
		// Parse citation contents
		while ((st = br.readLine()) != null) {
			
			// Type (Journal vs Conference)
			if(st.startsWith("TY  - ")) {
				st = st.replace("TY  - ", "");
				doc.setType(st);
			}
			// Title
			else if(st.contains("TI  - ")) {
				st = st.replace("TI  - ", "");
				doc.setTitle(st);
			}
			// Author(s)
			else if(st.contains("AU  - ")) {
				st = st.replace("AU  - ", "");
				doc.addAuthor(st);
			}
			// Date year
			else if(st.contains("PY  - ")) {
				st = st.replace("PY  - ", "");
				doc.setYear(Integer.parseInt(st));
			}
			// Journal name
			else if(st.contains("T2  - ")) {
				st = st.replace("T2  - ", "");
				doc.setTypeName(st);
			}
			// Search and parse pdf document
			else if(st.contains("ER  - ")) {
				if(!indexManager.checkDocument(typeName, doc.getTitle())){
					for(int i=0; i<listOfFiles.length;i++) {
						if(listOfFiles[i].getName().replace(".pdf","").replaceAll("[^a-zA-Z ]", "").replace(" ","").toLowerCase().equals(doc.getTitle().replaceAll("[^a-zA-Z ]", "").replace(" ","").toLowerCase())){
							// Rename the file according to title
							File newFile = new File(folder.getPath()+"\\"+doc.getTitle().replace(":","").replace("/", "")+".pdf");
							if(!newFile.exists()) {
								File oldFile = new File(folder.getPath()+"\\"+listOfFiles[i].getName());
								if(oldFile.renameTo(newFile)) {
									renameCounter+=1;
								}
								else
									out.println("ERROR RENAMING: "+oldFile.getName());
							}
							// Search pdf document and parse content
							try(PDDocument document = PDDocument.load(newFile)){	  
								if (!document.isEncrypted()) {
									Boolean abstractRemoved = false;
									Boolean referencesRemoved = false;
									PDFTextStripperByArea stripper = new PDFTextStripperByArea();		        		   
									stripper.setSortByPosition(true);		   
									PDFTextStripper tStripper = new PDFTextStripper();  		   
									String pdfFileInText = tStripper.getText(document);
									String finalText = pdfFileInText;
									Pattern p1 = Pattern.compile("\\bAbstract\\b|\\bABSTRACT\\b");
									Pattern p2 = Pattern.compile("\\bReferences\\b|\\bREFERENCES\\b");
									Matcher m1 = p1.matcher(pdfFileInText);
									Matcher m2 = p2.matcher(pdfFileInText);
									int count = 0;
									if (m1.find()){
										System.out.println("Removed text before abstract!");
										finalText = pdfFileInText.split("Abstract|ABSTRACT", 2)[1];
										countAbstract+=1;
										abstractRemoved = true;
									}
									while (m2.find()){
										count++;
									}
									if(count==1) {
										System.out.println("Removed text after references!");
										finalText = finalText.split("References|REFERENCES")[0];
										countReferences+=1;
										referencesRemoved = true;
									}
									doc.setText(finalText);
									System.out.println("Creating document, id: "+docCounter);
			    					System.out.println(doc.getTitle());
			    					doc.setId(docCounter++);
			    					doc.setAnalyzedText(indexManager.analyzeText(doc.getText()).trim().replaceAll("[^a-zA-Z ]", " ").replaceAll(" +", " ").replace("smart meter","smart_meter").replace("smart grid", "smart_grid").replace("smart cit", "smart_cit").replaceAll("\\b\\w{1,3}\\b\\s?", ""));		   
			    					indexManager.indexDocument(typeName, doc);
			    					// Append processed
			    					out.println(newFile.getName()+","+abstractRemoved+","+referencesRemoved);
			    					// Clear document fields
			    					doc.clearFields();
			    					// Break loop
			    					break;
								}
								else {
									System.out.println("Document with title: '"+doc.getTitle()+"' is encrypted!");
				        	   }
							}
							catch (Exception e){
								System.out.println("Could not open document: '"+doc.getTitle());
							}
						}
					}
				}
				else {
					System.out.println("Document: ["+doc.getTitle()+"] already exists in Elasticsearch!");
				}
			}
		}
		// Write stats
		out.println("Renamed: "+renameCounter+" out of "+docCounter+" files");
		out.println("Removed abstract from: "+countAbstract+" out of "+docCounter+" files");
		out.println("Removed references from: "+countReferences+" out of "+docCounter+" files");
		// Close writer
		out.close();
	}

}
