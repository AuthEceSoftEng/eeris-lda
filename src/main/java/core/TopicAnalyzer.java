package core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import core.jgibblda.Estimator;
import core.jgibblda.LDACmdOption;
import models.Topic;
import models.PdfElement;
import models.WordPdfElement;

public class TopicAnalyzer {
	
	private int K;
	private int twords;
	private double[][] theta;
	private List<Map<String, Double>> topWordProbList;
	private String[][] topicTopWordClouds;
	String typeName;
	
	public TopicAnalyzer(String typeName){
		this.topWordProbList = new ArrayList<Map<String, Double>>();
		this.typeName = typeName;
	}
	
	public List<Topic> estimateLDAModel(){
		
		/*Perform document-level lda estimation*/
		File file = new File(TopicAnalyzer.class.getClassLoader().getResource("estimation.properties").getFile());
		estLDA(file.getAbsolutePath());
		
		ElasticsearchManager elasticManager = new ElasticsearchManager();
		int N = elasticManager.getDocumentsCount(typeName);
		
		PdfElement pdfElement;
		WordPdfElement wordPdfElement;
		List<Topic> topicList = new ArrayList<Topic>();
		Topic topic;
		
		/*Topic attributes*/
		for(int k=0; k<K; k++){					
			topic = new Topic();
			topic.setId(k);
			for(int i=0; i<N; i++){
				pdfElement = new PdfElement();
				pdfElement.setId(i);
				pdfElement.setValue(theta[i][k]);
				topic.getTopicDocumentDistribution().add(pdfElement);
			}
			
			Map<String, Double> topicTopWordMap = new HashMap<String, Double>();
			topicTopWordMap = topWordProbList.get(k);
	
			for(int j=0; j<twords; j++){
				wordPdfElement = new WordPdfElement();
				wordPdfElement.setText(topicTopWordClouds[k][j]);
				wordPdfElement.setWeight(topicTopWordMap.get(topicTopWordClouds[k][j]));
				topic.getTopWordsList().add(wordPdfElement);	
			}
			
			topicList.add(topic);
		}
		
		elasticManager.indexTopicList(topicList);
		
		System.out.println("Indexed "+topicList.size()+" topics");
		return topicList;
	}
	
	private void estLDA(String propertiesFileName){
		
		ElasticsearchManager indexManager = new ElasticsearchManager();
		Properties prop = indexManager.readProperties(propertiesFileName);
		LDACmdOption ldaOption = new LDACmdOption();

		File folder = new File("dataset");
		
		ldaOption.est = true;
		ldaOption.alpha = Double.parseDouble(prop.getProperty("alpha"));
		ldaOption.beta = Double.parseDouble(prop.getProperty("beta"));
		ldaOption.K = Integer.parseInt(prop.getProperty("K"));
		ldaOption.niters = Integer.parseInt(prop.getProperty("niters"));
		ldaOption.savestep = Integer.parseInt(prop.getProperty("savestep"));
		ldaOption.dir = folder.getAbsolutePath();
		ldaOption.dfile = prop.getProperty("dfile");
		ldaOption.twords = Integer.parseInt(prop.getProperty("twords"));
		
		this.K = ldaOption.K;
		this.twords = ldaOption.twords;
		
		System.out.println("LDA Analysis: alpha="+ldaOption.alpha+", beta="+ldaOption.beta+", K="+ldaOption.K+", twords="+ldaOption.twords+", iters="+ldaOption.niters);
		
		Estimator estimator = new Estimator();
		estimator.init(ldaOption);
		estimator.estimate();
		
		this.theta = estimator.trnModel.theta;
		this.topWordProbList = estimator.trnModel.topicWordProbList;
		this.topicTopWordClouds = estimator.trnModel.wordClouds;
		
	}

}
