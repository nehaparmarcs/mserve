package com.service;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;





@Path("/store")
public class FacebookServiceAdapter {

	@GET
	@Path("/data")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response pushData() {
		FaceBookVO rdetails = new FaceBookVO();
		try {	
			String token= "EAACEdEose0cBAMSLSPaqdbX3GLgBpGmb7iRU01QHtoZBwZBP8jACOrSqluPDm7du2rmn3iUB0VAaJ2raFUDdnZAtsZB9hIEpwOHlZC9lJLb8gsAOmJOLeQkL6bNKQttDwrWacNjzRiGbzTbnOMDzgkARh9SwqVBFVMlbi13ZC3nwZDZD";
			
			String[] allUrl = {"https://graph.facebook.com/v2.6/search?access_token="+token+"&pretty=1&q=san+jose&type=event&limit=25&after=NDkZD",
					"https://graph.facebook.com/v2.6/search?q=san%20jose&type=event&access_token="+token,
					"https://graph.facebook.com/v2.6/search?access_token="+token+"&pretty=1&q=san+jose&type=event&limit=25&after=MjQZD"
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
			
			searchData("World");
			
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
			
						mongoClient = new MongoClient("54.165.58.89", 27017);
					
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
	public Response searchData(@PathParam("searchText") String searchText){
		
		MongoClient mongoClient;
		FaceBookVO fbResObject = new FaceBookVO();
		List<Data> dataList = new ArrayList<Data>();
		String response = "";
		try {
				
				mongoClient = new MongoClient("54.165.58.89", 27017);
			
				@SuppressWarnings("deprecation")
				DB db = mongoClient.getDB("ibm");
				DBCollection table = db.getCollection("fbeventdata");
				
				BasicDBObject regexQuery = new BasicDBObject();
				//Pattern searchTxt = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
				  regexQuery.put("description",
					new BasicDBObject("$regex", URLDecoder.decode(searchText,"UTF-8")));
			                    //.append("$options", "m"));

				  System.out.println(regexQuery.toString());
				  ObjectMapper mapper = new ObjectMapper();
					
				  
				  DBCursor cursor8 = table.find(regexQuery);
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
		return Response.status(201).entity(fbResObject).build();
		
	}
	

	public static void findFromMongo(){

		// Creating serverside mongoDB which is listening on port 27017
			MongoClient mongoClient;
			try {
			
				mongoClient = new MongoClient("54.165.58.89", 27017);
				
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
