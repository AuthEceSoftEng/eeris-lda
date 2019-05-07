package core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import models.Document;

public class DocumentAnalyzer {

	String typeName;
	
	public DocumentAnalyzer(String typeName) {
		this.typeName = typeName;
	}
	
	public void analyzeDocuments(String directory, String fileName){
		
		List<String> analyzedDocumentList = new ArrayList<String>();
		ElasticsearchManager indexManager = new ElasticsearchManager();
		List<Document> documentList = indexManager.getDocuments(typeName);
		
		for(Document doc: documentList){
			analyzedDocumentList.add(doc.getAnalyzedText());
		}
		
		File folder = new File(directory);
		System.out.println("Writing "+documentList.size()+" analyzed data in "+folder.getAbsolutePath()+"\\"+fileName);
		exportAnalyzedData(analyzedDocumentList, folder.getAbsolutePath()+"\\"+fileName);
	}

	private void exportAnalyzedData(List<String> analyzedList, String fileName){
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		out.println(analyzedList.size());
		for(String content: analyzedList){
			out.println(content);
		}
		
		out.close();
	}

}
