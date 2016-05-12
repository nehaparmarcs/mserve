package com.service;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;


/**
 * 
 * db.fbeventdata.find({$or:[{"place_name" : "Miami Beach Club"},{"id" : "436934323166014"}]})
 * @author neha
 *
 */


@Path("/store")
public class FacebookServiceAdapter {

	@GET
	@Path("/data")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response pushData() {
		FaceBookVO rdetails = new FaceBookVO();
		try {	
			String token= "EAACEdEose0cBAEvKkGi94BZBT2yE2MjCggAVI9nmwLqHhJEBt4udQyZCTa1MiuAzWgqXFs5qE63GZAVlaHcZAWRGZCYM3uMnAVrU2bhO0g8JVeSsRRZBUzH4wwTbGy0nCjkZBcU1VCmZBBjZAkZBaz37XLsavax6OIS3PfI41wZBNe1ZAwZDZD";
			
			String[] allUrl = {//"https://graph.facebook.com/v2.6/search?access_token="+token+"&pretty=1&q=san+jose&type=event&limit=25&after=NDkZD",
					"https://graph.facebook.com/v2.6/search?q=texas&type=event&access_token="+token
					//,"https://graph.facebook.com/v2.6/search?access_token="+token+"&pretty=1&q=san+jose&type=event&limit=25&after=MjQZD"
			};
			for(String url :allUrl){
				Client client = Client.create();
	
				WebResource webResource = client
						.resource(url);
				
				ClientResponse response = webResource.type("application/json")
						.get(ClientResponse.class);
	
				if( response.getStatus() != 200) {
					throw new RuntimeException("Failed : HTTP error code : "
							+ response.getStatus());
				}
	
				ObjectMapper mapper = new ObjectMapper();
				
				rdetails = mapper.readValue(response.getEntity(String.class), FaceBookVO.class);
				
				
				insertIntoMongo(rdetails);
			}
			
			//searchData("World");
			
			System.out.println(rdetails);

		} catch (Exception e) {

			e.printStackTrace();

		}

		
		return Response.status(201).entity(rdetails).build();
		
	}
	

	public static void insertIntoMongo(FaceBookVO fbVO){

				MongoClient mongoClient;
				try {
						for(Data data : fbVO.getData()){
			
						mongoClient = new MongoClient("54.186.78.152", 27017);
					
						@SuppressWarnings("deprecation")
						DB db = mongoClient.getDB("ibm");
						DBCollection table = db.getCollection("fbeventdata");
						
						
						BasicDBObject dataDetails = new BasicDBObject();
						BasicDBObject placeDetails = new BasicDBObject();
						BasicDBObject locDetails = new BasicDBObject();
						
						dataDetails.put("description", data.getDescription());
						dataDetails.put("id", data.getId());
						dataDetails.put("name", data.getName());
						dataDetails.put("start_time", data.getStart_time());
						dataDetails.put("end_time", data.getEnd_time());
						
							
							if(data.getPlace()!=null){
								dataDetails.put("place_id", data.getPlace().getId());
								dataDetails.put("place_name", data.getPlace().getName());
							
								if(data.getPlace().getLocation()!=null){
									dataDetails.put("place_location_city", data.getPlace().getLocation().getCity());
									dataDetails.put("place_location_country", data.getPlace().getLocation().getCountry());
									dataDetails.put("place_location_latitude", data.getPlace().getLocation().getLatitude());
									dataDetails.put("place_location_longitude", data.getPlace().getLocation().getLongitude());
									dataDetails.put("place_location_state", data.getPlace().getLocation().getState());
									dataDetails.put("place_location_street", data.getPlace().getLocation().getStreet());
									dataDetails.put("place_location_zip", data.getPlace().getLocation().getZip());
							//placeDetails.put("location", locDetails);
							}
							
							//dataDetails.put("place", placeDetails);
							}
						
						table.insert(dataDetails);
			
					}
				}catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	@GET
	@Path("/search/{searchText}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String searchData(@PathParam("searchText") String searchText, 
			@QueryParam("callback") String callback){
		
		MongoClient mongoClient;
		FaceBookVO fbResObject = new FaceBookVO();
		List<Data> dataList = new ArrayList<Data>();
		//String response = "";
		try {
				String inputText = URLDecoder.decode(searchText.trim(),"UTF-8");
				mongoClient = new MongoClient("54.186.78.152", 27017);
			
				DB db = mongoClient.getDB("ibm");
				DBCollection table = db.getCollection("fbeventdata");
				
				BasicDBObject regexQuery = new BasicDBObject();
				//Pattern searchTxt = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
				  regexQuery.put("description",new BasicDBObject("$regex", inputText));
				  
				  
				  //create regex to with or params
				  DBObject clause1 = new BasicDBObject("description", new BasicDBObject("$regex", inputText).append("$options","i"));  
				  DBObject clause2 = new BasicDBObject("name", new BasicDBObject("$regex", inputText).append("$options","i"));
				  DBObject clause3 = new BasicDBObject("place_location_city", new BasicDBObject("$regex", inputText).append("$options","i"));
				  BasicDBList or = new BasicDBList();
				  or.add(clause1);
				  or.add(clause2);
				  or.add(clause3);
				  
				  DBObject query = new BasicDBObject("$or", or);
				  System.out.println(query.toString());
				  
				  
			                    //.append("$options", "m"));

				  //System.out.println(regexQuery.toString());
			
				  
				  DBCursor cursor8 = table.find(query);
				  while (cursor8.hasNext()) {
					  DBObject nextDocument = cursor8.next();
					  
					  Data tempData = new Data();
					  tempData.setDescription((String)nextDocument.get("description"));
					  tempData.setId((String)nextDocument.get("id"));
					  tempData.setName((String)nextDocument.get("name"));
					  tempData.setStart_time((String)nextDocument.get("start_time"));
					  tempData.setEnd_time((String)nextDocument.get("end_time"));
					  if((String)nextDocument.get("place_id") != null || !"".equals((String)nextDocument.get("place_id"))){
						  
						  Place place = new Place();
						  place.setId((String)nextDocument.get("place_id"));
						  place.setName((String)nextDocument.get("place_name"));
						  
						  if((String)nextDocument.get("place_location_city") != null || !"".equals((String)nextDocument.get("place_location_city"))){
						  
							  	Location loc = new Location();
						  		loc.setCity((String)nextDocument.get("place_location_city"));
						  		loc.setCountry((String)nextDocument.get("place_location_country"));
						  		loc.setLatitude((String)nextDocument.get("place_location_latitude"));
						  		loc.setLongitude((String)nextDocument.get("place_location_longitude"));
						  		loc.setState((String)nextDocument.get("place_location_longitude"));
						  		loc.setStreet((String)nextDocument.get("place_location_street"));
						  		loc.setZip((String)nextDocument.get("place_location_zip"));
						  		place.setLocation(loc);
						  }
						  
						  tempData.setPlace(place);
					  }
					
					dataList.add(tempData);
				  }
				  if(!dataList.isEmpty()){
					  //Restrict no. of elements
					  if(dataList.size()>20){
						  Data[] arr = (Data[]) dataList.toArray(new Data[dataList.size()]);
						  Data newArray[] = new Data[20];
						  {
							  for(int i = 0; i < 20; ++i) {
								  newArray[i] = arr[i];
							    }
						  }
						  fbResObject.setData(newArray);
					  }else {
						  fbResObject.setData((Data[]) dataList.toArray(new Data[dataList.size()]));
					  }
					  
				  }
				
				
				
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		
		
		  ObjectMapper mapper = new ObjectMapper();
		  String jsonInString = "";
		  try {
		jsonInString = mapper.writeValueAsString(fbResObject);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(callback);
		String response = "callbackMethod"+"("+jsonInString+");";
		
		return response;
		
	}
	


	public static void findFromMongo(){

		// Creating serverside mongoDB which is listening on port 27017
			MongoClient mongoClient;
			try {
			
				mongoClient = new MongoClient("54.186.78.152", 27017);
				
				@SuppressWarnings("deprecation")
				DB db = mongoClient.getDB("ibm");
				DBCollection table = db.getCollection("fbeventdata");
	
			
		
				BasicDBObject searchQuery = new BasicDBObject();
			
			//searchQuery.put("Id", "757463417723708");
				
				
			DBCursor cursor = table.find(searchQuery);
			while (cursor.hasNext()) {
				DBObject nextDocument = cursor.next();
				System.out.println(nextDocument.toString());
				String details = nextDocument.toString();
				
			}
	
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
	}
	
	
	
	
}
