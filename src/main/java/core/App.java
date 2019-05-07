package core;

import models.Document;
import models.PdfElement;
import models.Topic;
import models.WordPdfElement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.opencsv.CSVWriter;

public class App {

    public static void main(String[] args) throws IOException, InterruptedException {

    	// Create Elasticsearch manager instance
		ElasticsearchManager elasticManager = new ElasticsearchManager();
		
		// Once
		elasticManager.createMappings();
		parseDocuments();
		
		// LDA parameter values
		int[] K = {7};
		double[] alpha = {0.1};
		double[] beta = {0.1};
		int twords = 10;
		int niters = 1000;
		int savestep = 1000;
		
		// Parameters files
		File estPropertiesFile = new File(App.class.getClassLoader().getResource("estimation.properties").getFile());
		FileWriter estPropertiesFileOut = new FileWriter(estPropertiesFile,false);
		
		for(int k=0; k<K.length; k++) {
			for(int i=0; i<alpha.length; i++) {
				for(int j=0; j<beta.length; j++) {

					// Estimation properties
					Properties estProps = new Properties();
					estProps.setProperty("K", Integer.toString(K[k]));
					estProps.setProperty("alpha", Double.toString(alpha[i]));
					estProps.setProperty("beta", Double.toString(beta[j]));
					estProps.setProperty("twords", Integer.toString(twords));
					estProps.setProperty("niters", Integer.toString(niters));
					estProps.setProperty("savestep", Integer.toString(savestep));
					estProps.setProperty("dfile", "analyzedData.txt");
					estProps.store(estPropertiesFileOut, null);
					
					// Estimate LDA model
					estimate(alpha[i], beta[j], K[k]);
					System.out.println("LDA model estimation completed");
					
					// Delete topics before testing
					elasticManager.deleteTopics();

					// Wait for the topics deletion completion 
					Thread.sleep(2000);
					
				}
			}
		}
		
		// Close writers
		estPropertiesFileOut.close();

    }

	private static void parseDocuments() throws InterruptedException, IOException {

		// Parse and index pdf documents for training
		DocumentParser parser = new DocumentParser("documents");
		parser.parseDocuments("dataset");
		
		// Wait for the document indexing completion 
		Thread.sleep(2000);
	
		// Pre-process training dataset
		DocumentAnalyzer docAnalyzer = new DocumentAnalyzer("documents");
		docAnalyzer.analyzeDocuments("dataset","analyzedData.txt");
		
	}

	private static void estimate(double alpha, double beta, int k) throws IOException, InterruptedException {
		
    	// Create Elasticsearch manager instance
		ElasticsearchManager elasticManager = new ElasticsearchManager();
				
		// Delete existing topics
		elasticManager.deleteTopics();
					
		// Wait for the document deletion completion 
		Thread.sleep(2000);
			
		// Create output directory
		new File("output\\"+k+"_"+alpha+"_"+beta).mkdirs();
		
		// Perform LDA estimation
		TopicAnalyzer estimator = new TopicAnalyzer("documents");
		estimator.estimateLDAModel();
			
		// Wait for the topic indexing completion
		Thread.sleep(2000);
					
		// Fetch topic list
		List<Topic> topicList = elasticManager.getTopics();
		//Fetch document list
		List<Document> docList = elasticManager.getDocuments("documents");
					
		// Save to csv
		File folder = new File("output\\"+k+"_"+alpha+"_"+beta);
		File file = new File(folder.getAbsolutePath()+"\\document-class.csv");
		FileWriter outputfile = new FileWriter(file); 
		CSVWriter writer = new CSVWriter(outputfile); 
				    
		// Adding header to csv 
		String[] header = { "Document ID", "Document Title", "Publication Type", "Publication Year", "Topic ID", "Topic Probability" }; 
		writer.writeNext(header); 
					
		// Extract topic assignment for each document 
		for (Document doc : docList) {
			int id = doc.getId();
			int assignedTopicId=0;
			double topPdfValue = 0;
			for (Topic topic : topicList) {
				for(PdfElement pdfElement: topic.getTopicDocumentDistribution()) {
					if(pdfElement.getId() == id && pdfElement.getValue()>topPdfValue) {
						assignedTopicId = topic.getId();
						topPdfValue = pdfElement.getValue();
					}
				}
			}
						
			String[] body = {Integer.toString(doc.getId()), doc.getTitle(), doc.getType(), Integer.toString(doc.getYear()), Integer.toString(assignedTopicId), Double.toString(topPdfValue) };
			writer.writeNext(body);
		}
		writer.close();
					
					
		// Topic-document distribution csv file
		File file1 = new File(folder.getAbsolutePath()+"\\topic-document.csv");
		File file3 = new File(folder.getAbsolutePath()+"\\topic-probability.csv");
		FileWriter writer1 = new FileWriter(file1);
		FileWriter writer3 = new FileWriter(file3);
		CSVWriter csvWriter1 = new CSVWriter(writer1);
					
		// Header
		StringBuilder sbHead = new StringBuilder();
		for(int n=0; n<docList.size(); n++) {
			sbHead.append(","+Integer.toString(n));
		}
					
		csvWriter1.writeNext(sbHead.toString().split(","));
					
		// Topic-Document Distribution
		StringBuilder sbBody = new StringBuilder();
		//System.out.println("Topic-Document Distribution");
		for(Topic topic: topicList) {
			double topicWeight = 0;
			sbBody.append(Integer.toString(topic.getId()));
			//System.out.println("\nTopic "+topic.getId());
			for(PdfElement pdfElement: topic.getTopicDocumentDistribution()) {
				//System.out.print("{"+pdfElement.getId()+" ,"+pdfElement.getValue()+"} ");
				sbBody.append(","+Double.toString(pdfElement.getValue()));
				topicWeight+=pdfElement.getValue();
			}
			csvWriter1.writeNext(sbBody.toString().split(","));
			sbBody.setLength(0);
			topicWeight = (double) (topicWeight / docList.size());
			writer3.write("\nTopic "+topic.getId()+"," + Double.toString(topicWeight));
		}
		// Close FileWriter objects
		csvWriter1.close();
		writer3.close();
		writer1.close();
					
		// csv file
		File file2 = new File(folder.getAbsolutePath()+"\\word-topic.csv");
		FileWriter writer2 = new FileWriter(file2); 
		CSVWriter csvWriter2 = new CSVWriter(writer2);
				    
		// Topic-Word Distribution
		StringBuilder sbBody1 = new StringBuilder();
		//System.out.println("\nTopic-Word Distribution");
		for(Topic topic: topicList) {
			sbBody1.append(Integer.toString(topic.getId()));
			//System.out.println("\nTopic "+topic.getId());
			for(WordPdfElement pdfElement: topic.getTopWordsList()) {
				//System.out.print("{"+pdfElement.getText()+"} ");
				sbBody1.append(","+pdfElement.getText());
			}
			csvWriter2.writeNext(sbBody1.toString().split(","));
			sbBody1.setLength(0);
		}
		// Close FileWriter objects
		csvWriter2.close();
		writer2.close();

	}
}