package core;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse.AnalyzeToken;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.exists.ExistsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import com.google.gson.Gson;

import models.Document;
import models.Topic;

public class ElasticsearchManager {

	String indexName;
	String documentsTypeName;
	String topicsTypeName;
	String analyzerName;
	
	public ElasticsearchManager(){
		File file = new File(ElasticsearchManager.class.getClassLoader().getResource("elasticsearch.properties").getFile());
		Properties prop = readProperties(file.getAbsolutePath());
		this.indexName = prop.getProperty("index");
		this.documentsTypeName = prop.getProperty("documentsType");
		this.topicsTypeName = prop.getProperty("documentTopicsType");
		this.analyzerName = prop.getProperty("analyzer");
	}
	
	public void createMappings(){
		
		if(checkIndex(indexName)){
			System.out.println("Index _"+indexName+" already exists!");
			return;
		}
		else{
			try {
				if(createIndex(indexName).isAcknowledged()){	
					if(createDocumentMapping(indexName, documentsTypeName).isAcknowledged()){
						System.out.println("Document mapping created succesfully");
					}
					if(createTopicMapping(indexName, topicsTypeName).isAcknowledged()){
						System.out.println("Topic mapping created succesfully");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Boolean checkIndex(String indexName){
		
		@SuppressWarnings("resource")	
		Client client = new TransportClient()  	
			.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		
		boolean hasIndex = client
				.admin()
				.indices()
				.exists(new IndicesExistsRequest(indexName))
				.actionGet()
				.isExists();
		
		client.close();
		return hasIndex;
	}
	
	private PutMappingResponse createDocumentMapping(String indexName, String typeName) throws IOException{
		
		@SuppressWarnings("resource")	
		Client client = new TransportClient()  	
			.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
	
			XContentBuilder mapping = jsonBuilder()
			         .startObject()
			              .startObject(typeName)// index type
			              		.startObject("properties")
			              			.startObject("id")
			                           .field("type", "integer")
			                        .endObject()
			                        .startObject("title")
			                        	.field("type","string")
			                        	.field("index","not_analyzed")
			                        .endObject()
			                        .startObject("type")
		                        		.field("type","string")
		                        		.field("index","not_analyzed")
		                        	.endObject()
			                        .startObject("typeName")
	                        			.field("type","string")
	                        			.field("index","not_analyzed")
	                        		.endObject()
			                        .startObject("authors")
		                        		.field("type","string")
		                        		.field("index","not_analyzed")
		                        	.endObject()
			                         .startObject("year")
			                        	.field("type","integer")
			                        .endObject()
			                         .startObject("text")
			                        	.field("type","string")
			                        	.field("analyzer", "standard")
			                        .endObject()
			                         .startObject("analyzedText")
			                        	.field("type","string")
			                        	.field("index", "no")
			                        .endObject()
			                   .endObject()
			               .endObject()
			            .endObject();
			
			PutMappingResponse response = client
				  	.admin()
				  	.indices()
	                .preparePutMapping(indexName)
	                .setType(typeName)
	                .setSource(mapping)
	                .execute()
	                .actionGet();
		  
		client.close();
		return response;
	}
	
	private PutMappingResponse createTopicMapping(String indexName, String typeName) throws IOException{
		
		@SuppressWarnings("resource")	
		Client client = new TransportClient()  	
			.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
	
			XContentBuilder mapping = jsonBuilder()
			         .startObject()
			              .startObject(typeName)
			              		.startObject("properties")
			                   		.startObject("id")
			                           .field("type", "integer")
			                        .endObject()
			                        .startObject("topWordList")
			                        	.field("type", "nested")//!!!
			                        	.startObject("properties")
			                        		.startObject("text")
			                        			.field("type","string")
			                        			.field("index", "not_analyzed")
			                        			//.field("search_analyzer", analyzerName)//!!!
			                        		.endObject()
			                        		.startObject("weight")
			                        			.field("type","double")
			                        		.endObject()
			                        	.endObject()
			                        .endObject()
			                        .startObject("topicDocumentDistribution")
			                        	.field("type", "nested")
			                        	.startObject("properties")
			                        		.startObject("id")
			                        			.field("type", "integer")
			                        		.endObject()
			                        		.startObject("value")
			                        			.field("type", "double")
			                        		.endObject()
			                        	.endObject()
			                        .endObject()
			                   .endObject()
			               .endObject()
			            .endObject();
		
		  PutMappingResponse response = client
				  	.admin()
				  	.indices()
	                .preparePutMapping(indexName)
	                .setType(typeName)
	                .setSource(mapping)
	                .execute()
	                .actionGet();
		  
		  client.close();
		  return response;
	}
	
	private CreateIndexResponse createIndex(String indexName) throws IOException{
		
		@SuppressWarnings("resource")	
		Client client = new TransportClient()  	
			.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		
		XContentBuilder settings = jsonBuilder()
				.startObject()
					.startObject("analysis")
						.startObject("filter")
							.startObject("my_stop")
								.field("type", "stop")
								.field("stopwords", "_english_")
							.endObject()
							.startObject("my_stop2")
								.field("type", "stop")
								.field("stopwords_path", "stopwords/stopwords.txt")
							.endObject()
						.endObject()
						.startObject("char_filter")
							.startObject("quotes")
								.field("type", "mapping")
								.field("mappings", new String[]{
										"\\u0091=>\\u0027", 
										"\\u0092=>\\u0027", 
										"\\u2018=>\\u0027", 
										"\\u2019=>\\u0027",
										"\\u201B=>\\u0027"})
							.endObject()
						.endObject()
						.startObject("analyzer")
							.startObject("my_analyzer")
								.field("type", "custom")
								.field("tokenizer", "standard")
								.field("char_filter","quotes")
								.field("filter", new String[]{"standard", "asciifolding", "lowercase","my_stop2","snowball"})
							.endObject()
						.endObject()
					.endObject()
				.endObject();
		
		CreateIndexResponse createResponse = client
				.admin()
				.indices()
				.prepareCreate(indexName)
				.setSettings(settings)
				.execute()
				.actionGet();
		
		client.close();
		return createResponse;
	}
	
	public Boolean checkDocument(String typeName, String title){
		
		@SuppressWarnings("resource")	
		Client client = new TransportClient()  	
			.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
	
		ExistsResponse sr = client.prepareExists(indexName)
		        .setTypes(typeName)  
		        .setQuery(QueryBuilders.termQuery("title", title))
		        .execute()
		        .actionGet();
		
		client.close();
		return sr.exists();
	}
	
	public void indexDocument(String typeName, Document doc){
		
		@SuppressWarnings("resource")	
		Client client = new TransportClient()  	
			.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
	
		Gson gson = new Gson();	
		String json = gson.toJson(doc);		
			
		client.prepareIndex(indexName, typeName,
				Integer.toString(doc.getId()))	    							
		.setSource(json)								
		.execute()								
		.actionGet();			
		
		System.out.println("Indexed document: "+doc.getId());
		
		client.close();
	}

	public int getDocumentsCount(String typeName){
		
		@SuppressWarnings("resource")
		Client client = new TransportClient()  	
		.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));

		CountResponse response = client.prepareCount(indexName)
				.setTypes(typeName)
		        .execute()
		        .actionGet();
		
		client.close();
		return (int) response.getCount();
	}

	public int getTopicsCount() {
		
		@SuppressWarnings("resource")
		Client client = new TransportClient()  	
		.addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));

		CountResponse response = client.prepareCount(indexName)
				.setTypes(topicsTypeName)
		        .execute()
		        .actionGet();
		
		client.close();
		return (int) response.getCount();
	}

	public void deleteDocuments(String typeName){
		
		int N = getDocumentsCount(typeName);
		
		@SuppressWarnings("resource")
		Client client = new TransportClient()
		   .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		
		BulkRequestBuilder bulkRequest = client.prepareBulk();

		// either use client#prepare, or use Requests# to directly build index/delete requests
		for(int i=0; i<N; i++){
			bulkRequest.add(client.prepareDelete(indexName, typeName, Integer.toString(i)));
		}
		
		bulkRequest.execute().actionGet();
		client.close();
	}
	
	public void deleteTopics(){
	
		@SuppressWarnings("resource")
		Client client = new TransportClient()
		   .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		
		ExistsResponse sr = client.prepareExists(indexName)
		        .setTypes(topicsTypeName)  
		        .execute()
		        .actionGet();
		
		if(sr.exists()) {
			int k = getTopicsCount();

			// either use client#prepare, or use Requests# to directly build index/delete requests
			for(int i=0; i<k; i++){
				bulkRequest.add(client.prepareDelete(indexName, topicsTypeName, Integer.toString(i)));
			}
		
			bulkRequest.execute().actionGet();
		}
		
		client.close();
	}
	
	public void indexTopicList(List<Topic> topicList) {

		@SuppressWarnings("resource")
		Client client = new TransportClient()
		   .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		
		BulkRequestBuilder bulkRequest = client.prepareBulk();
		Gson gson = new Gson();
		// either use client#prepare, or use Requests# to directly build index/delete requests
		for(Topic topic: topicList){
			bulkRequest.add(client.prepareIndex(indexName, topicsTypeName, Integer.toString(topic.getId()))
		        .setSource(gson.toJson(topic)));
		}
		
		bulkRequest.execute().actionGet();
		
		client.close();
	}

	public List<Document> getDocuments(String typeName){
		
		@SuppressWarnings("resource")
		Client client = new TransportClient()
	    .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		
		int N = getDocumentsCount(typeName);
		
		SearchResponse sr = client.prepareSearch(indexName)
				.setTypes(typeName)
				.addSort("id", SortOrder.ASC)
				.setSize(N)
				.execute()
				.actionGet();
		
		client.close();
		
		List<Document> documentList = new ArrayList<Document>();
		Document doc;
		Gson gson = new Gson();
		
		for(SearchHit hit: sr.getHits()){
			doc = new Document();
			String source=hit.sourceAsString();
			doc = gson.fromJson(source, Document.class);
			documentList.add(doc);
		}
		
		return documentList;	
	}
	
	public List<Topic> getTopics(){
		
		List<Topic> topicList = new ArrayList<Topic>();
		
		@SuppressWarnings("resource")
		Client client = new TransportClient()
        .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		
		SearchResponse sr = client.prepareSearch(indexName)
		        .setTypes(topicsTypeName)  
		        .setSize(getTopicsCount())
		        .execute()
		        .actionGet();
		
		client.close();
		
		//System.out.println(sr.toString());
		
		Gson gson = new Gson();
		Topic topic;
		for(SearchHit hit: sr.getHits()){
			topic = new Topic();
			String source=hit.sourceAsString();
			topic = gson.fromJson(source, Topic.class);
			topicList.add(topic);	
		}
		
		return topicList;
	}
	
	public String analyzeText(String text){

		@SuppressWarnings("resource")
		Client client = new TransportClient()
	    .addTransportAddress(new InetSocketTransportAddress("127.0.0.1", 9300));
		
		StringBuilder builder = new StringBuilder();		
			AnalyzeResponse analyzeResponse = client.admin()
					.indices()
					.prepareAnalyze(text)
					.setIndex(indexName)
					.setAnalyzer(analyzerName)
					.execute()
					.actionGet();
			for(AnalyzeToken token: analyzeResponse.getTokens()){		
				builder.append(token.getTerm()+" ");
			}	
		
		client.close();
		return builder.toString();	
	}
	
	public Properties readProperties(String fileName) {
		
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			
			input = new FileInputStream(fileName);
			prop.load(input);	
			
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();}}}
		
		return prop;
	}
}
