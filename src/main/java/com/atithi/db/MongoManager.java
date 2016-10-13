package com.atithi.db;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.MacSpi;
import javax.swing.plaf.synth.SynthSplitPaneUI;

import org.bson.BSON;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jivesoftware.smack.Manager;

import com.atithi.rest.dto.Customer;
import com.atithi.rest.dto.History;
import com.atithi.rest.dto.UserDetails;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoCommandException;
import com.mongodb.WriteResult;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;
//mongod --auth
//mongo Rasp -u test1 -p test1
public class MongoManager {
	
	 private MongoClient mongoClient;
	 private MongoDatabase db;
	 private final static String DATABASE_NAME = "Rasp";
	 private final static String DB_IP = "54.186.160.186:27017";
	 private static final String URL = "mongodb://localhost:27017/Rasp";
			 //"mongodb://RASP_DB_USER:RASP_DB_USER@"+DB_IP+"/"+DATABASE_NAME;
			 //"mongodb://test1:test1@localhost:27017/Rasp"; 
			 ////"mongodb://myUserAdmin:abc123@54.186.113.130:27017/RASP";
			 //"mongodb://RASP_DB_USER:RASP_DB_USER@54.186.113.130:27017/RASP";
			 
	 //mongodb://test1:test1@localhost:27017/Rasp
	 public static enum Collections {
		
		USER_DETAILS("UserDetails"),Customer("Customer"),HISTORY("HISTORY");
		 
		private String collectionName;
		
		public String getCollectionName() {
			return collectionName;
		}

		public void setCollectionName(String collectionName) {
			this.collectionName = collectionName;
		}

		private Collections(String name) {
			this.collectionName =name;
		}
	 };
	
	  public MongoManager() {
		 
		mongoClient = new MongoClient( new MongoClientURI(URL));
		db = mongoClient.getDatabase(DATABASE_NAME);
	}
	
	
	public boolean store(final Object user, Collections type) {		
		
		boolean res = true;
		final Gson gson = new Gson();
		final String jsonString = gson.toJson(user);
		
		final Document doc1 = Document.parse(jsonString);
		
        //Worked   
		if(!this.collectionExists(type.getCollectionName())) {
		db.createCollection(type.getCollectionName());
		}
		
		final MongoCollection<Document> userTable= db.getCollection(type.getCollectionName());
		//Validation 1 if Object already in Table/Collection
		if(!this.isExist(userTable, user, type) && !type.equals(Collections.HISTORY) ) {
			//means object does not exist in type table
			if(Collections.Customer.equals(type)) {
				
				//Validation 2 Customer has valid UserDetails
			    //it means client wants to store Customer entity
				if(!this.isAuthenticateUserDetails(((Customer)user).getDetails())) {
				 //it means UserDetails inside Customer are invalid
				   System.out.println("it means UserDetails inside Customer are invalid");
				   return false;
				}
				
			}
			
			userTable.insertOne(doc1);
			//if store is on Customer then update UserDetails
			if(Collections.Customer.equals(type)) {
				//Refactored
				this.updateUserCustomerRelation(doc1, (Customer)user);

			}
		}
		else if(Collections.HISTORY.equals(type)){
			userTable.insertOne(doc1);
		}
		else{
			System.out.println("User Name Taken");//Should actually return some reason for Not storing	
			res=false;
		}
		
		mongoClient.close();
		return res;				
	}
	
	private void updateUserCustomerRelation(Document customerDoc,Customer customer) {
		
		MongoCollection<Document>userTable =db.getCollection(Collections.Customer.getCollectionName());
		ObjectId customerID =(ObjectId)userTable.find(customerDoc).first().get("_id");
		System.out.println(customerID.toString()+"----"+customerID.toHexString());
		System.out.println("this is CustomerID"+customerID);
		//update UserDetails with token		 
		customer.getDetails().setCustID(new String[]{customerID.toHexString()});
		this.update(customer.getDetails(), Collections.USER_DETAILS);		
	}
	
	
	
	/*
	 * checks if Rasp-Pi credentials are valid
	 */
	private boolean isAuthenticateUserDetails(final UserDetails userDetails) {
	  boolean result = false;
	  
	  if(userDetails==null)
	  return result;
	  
	  final MongoCollection<Document> collection = db.getCollection(Collections.USER_DETAILS.getCollectionName());
	 
	  Map<String, Object> map = new HashMap<String, Object>();
	  map.put("name", userDetails.getName());
	  map.put("pass", userDetails.getPass());
	  long cnt = collection.count(new Document(map));
	  System.out.println("cnt is"+cnt);
	  if(cnt==0) 
		  return result; //credentials of Rasp_pi are wrong or not present
	  //Now UserDetails are validated
	  result= true;
	  return result;
	}
	
	//Customer should have proper UserDetails and Customer
	private boolean isAuthenticateCustomer(final Customer customer) {
		boolean result = false;
		final MongoCollection<Document> collection = db.getCollection(Collections.Customer.getCollectionName());
		Map<String, Object> filterMap = new HashMap<String, Object>();
		filterMap.put("name", customer.getName());
		filterMap.put("pass", customer.getPass());
		
		if(collection.count(new Document(filterMap))!=0) {
			result=true;
			
		}
		
		return result;
	}
	
	
	
	/*
	 * check if UserName is taken
	 * 1. check if object is already there
	 * 2. if obj is customer then check if he/she has proper UserDetails
	 */
	private boolean isExist (final MongoCollection<Document> table, final Object obj, Collections type) {
		
		boolean present = false;
		long cnt=0;
		if(table!=null) {
			
			if(Collections.USER_DETAILS.equals(type)) {
			Document findName = new Document("name" ,((UserDetails) obj).getName());
			cnt = table.count(findName);
			
			}
			else if(Collections.Customer.equals(type)) {
				Document findName = new Document("name" ,((Customer)obj).getName());
				cnt = table.count(findName);
				table.count(findName);
			}
		}
		if(cnt>=1)
			present=true; // if we get same user name we should use different one
		
		return present;
	}
	
	
	
	public boolean update(final Object user, final Collections type) {
		
		boolean res = false;
		final MongoCollection<Document> userTable= db.getCollection(type.getCollectionName());
		if (this.isExist(userTable, user,type)) {
			
			if(Collections.USER_DETAILS.equals(type)) {
				UserDetails usr = (UserDetails) user;
				HashMap<String, Object> filterMap = new HashMap<String, Object>();
				//if username and pass matches then update
				filterMap.put("name", usr.getName());
				filterMap.put("pass", usr.getPass());
				final Document filter = new Document(filterMap);
				
				// To Update
				// 1st get the Object
				  FindIterable<Document> doc = userTable.find(filter);
				  Document found = doc.first();
				  //should check what has changed and reconstruct
				  String jsonString = found.toJson();
				  UserDetails original = new Gson().fromJson(jsonString, UserDetails.class);
				//call ObjectMapper
				  UserDetailsMapper.update(original, usr);
				  
				// 2nd check what to 
				final Gson gson = new Gson();
				final String json = gson.toJson(original);
				final Document update = Document.parse(json);
				final Document doc_2 = new Document("$set", update);
								
				final UpdateResult updateResult = userTable.updateOne(filter, doc_2);
				//should be 1 ideally since no Bulk option provided and username is unique
				System.out.println(updateResult.getModifiedCount());
				
			}
			else if(Collections.Customer.equals(type)) {
				//1 get the Object from DB
				Customer cust = (Customer) user;
				final MongoCollection<Document> table=db.getCollection(type.getCollectionName());
				HashMap<String, Object> filterMap = new HashMap<String, Object>();
				//if username and pass matches then update
				filterMap.put("name", cust.getName());
				filterMap.put("pass", cust.getPass());
				final Document filter = new Document(filterMap);
				final Document found = table.find(filter).first();
				String json = found.toJson();
				Gson gson = new Gson();
				final Customer original = gson.fromJson(json, Customer.class);
				//2 Call the mapper
				CustomerMapper.update(original, cust);
				
				//3 Update the Custome mostly for Token
				final String js = gson.toJson(original);
				final Document update = Document.parse(js);
				final Document doc_2 = new Document("$set", update);
				
				ObjectId custID = found.getObjectId("_id");
				Document filter_2 = new Document("_id", custID);
				
				final UpdateResult updateResult = userTable.updateOne(filter_2, doc_2);
				System.out.println(updateResult.getModifiedCount());
				
			}
			
		}
				
		this.mongoClient.close();
		return res;
	}
	
	public boolean collectionExists(final String collectionName) {
	    MongoIterable<String> coll= db.listCollectionNames();
	    
	    final MongoCursor<String> iter = coll.iterator();
	     while(iter.hasNext()) {
	    	 String name= iter.next();
	    	 if(collectionName.equals(name)) {
	    		 //System.out.println("----->"+ name);
	    		 return true;
	    	 }
	     
	     }
	    
	    
	   	    return false;
	}
	
	public List<Object> get(final List<String> names,final Collections type) {
		
		List<Object> result = new ArrayList<Object>();
	    
	    
	    final MongoCollection<Document> table= db.getCollection(type.getCollectionName());;
	    final Gson gson = new Gson(); 
	    if(Collections.USER_DETAILS.equals(type)) {
			
	   
	   final Document inClause = new Document("$in", names);
	   final Document userSelectClause = new Document("name",inClause);	
	   //get all objects where name in  {}
	    final FindIterable<Document> res = table.find(userSelectClause);
	   // final MongoCursor<Document > cursor = res.iterator();
	    //for user for get just get first User since we are not querrying for multiple
	    final Document userDetails = res.first();
	    final UserDetails user = gson.fromJson(userDetails.toJson(), UserDetails.class);
	    result.add(user);
	    
	   
		}
	    else if(Collections.Customer.equals(type)) {
	    	
	    List<ObjectId> objectIDs = this.stringListToIDList(names);
	    
	    final Document inCustomer = new Document("$in",objectIDs);
	    final Document customerSelectClause = new Document("_id", inCustomer);	    	
	    final FindIterable<Document> res =table.find(customerSelectClause);
	    final MongoCursor<Document > cursor = res.iterator();
	      
	      while(cursor.hasNext()) {
	    	  final Document doc = cursor.next();
	    	  final Customer customer = gson.fromJson(doc.toJson(), Customer.class);
	    	  result.add(customer);
	      }
	      
	    }
	    else if(Collections.HISTORY.equals(type)) {
	    	
	    	
	    }
				
		return result;
	}
	
	public List<History> getHistory(String user){
		
		final List<History> histList = new ArrayList<History>();
		final MongoCollection<Document> table= db.getCollection(Collections.HISTORY.getCollectionName());;
	    final Gson gson = new Gson();
	    final Document inClause = new Document("$eq", user);
 	    final Document userSelectClause = new Document("user",inClause);
    	final FindIterable<Document> results = table.find(userSelectClause);
    	final MongoCursor<Document> cursor = results.iterator();
    	
    	while(cursor.hasNext()) {
    		final Document doc = cursor.next();
	    	final History h = gson.fromJson(doc.toJson(), History.class);
    		histList.add(h);	    	
    	}
		
		return histList;
	}
	
	
	private List<ObjectId> stringListToIDList(final List<String> stringList) {
		final List<ObjectId> idList = new ArrayList<ObjectId>();
		if(stringList!=null && !stringList.isEmpty()) {
			
			for (String ids : stringList) {
			
				final ObjectId objectID = new ObjectId(ids);
				idList.add(objectID);
			}
		
		}
		
		return idList;
	}
	
	
	
	public static void main(String[] args) {
		History hist = new History();
		hist.setConfidence(100);
		hist.setFileName("http://test");
		hist.setPrediction("User at door");
		hist.setTimeStamp("10");
		hist.setUser("TestUser1");
		final MongoManager manager  = new MongoManager();
		manager.store(hist, Collections.HISTORY);
		
//		MongoManager manager = new MongoManager();
//		List<History> res =manager.getHistory("TestUser");
//		System.out.println(res.size());
		
//		final MongoManager manager  = new MongoManager();
//		final List<String> test = new ArrayList<String>();
//		test.add("RASP1");
//		List<Object> t = manager.get(test, Collections.USER_DETAILS);
//		System.out.println(t.size());		
		
		//1 Test 1 check if normal store works for USERDETAILS
		//PASEED
	   /*MongoManager manager = new MongoManager();
		UserDetails userDetails = new UserDetails();
		userDetails.setName("RASP2");
		userDetails.setPass("RASP2");
		manager.store(userDetails, Collections.USER_DETAILS);*/
		
		
	/*//2. Test 2 Create a Customer all valid
		MongoManager manager = new MongoManager();
		Customer customer = new Customer();
		customer.setName("RUN1");
		customer.setPass("abc");
		UserDetails userDetails = new UserDetails();
		userDetails.setName("RUN1");
		userDetails.setPass("abc");
		customer.setDetails(userDetails);
		manager.store(customer, Collections.Customer);*/
		
	/*	//3. Test 3 Update Customer with Tokens
		MongoManager manager = new MongoManager();
		Customer cust = new Customer();
		cust.setName("RUN1");
		cust.setPass("abc");
		cust.setToken("My TOKEN");
		manager.update(cust, Collections.Customer);*/
		
		/*
	// check get works or not
		MongoManager manager = new MongoManager();
		List<String> filter = new ArrayList<String>();
		//filter.add("571d421c6b13443becfca3b4");
		filter.add("571d43796b13443becfca3b6");
		//UserDetails usd=(UserDetails)manager.get(filter, Collections.USER_DETAILS).get(0);
		List<Object> custs= manager.get(filter, Collections.Customer); 
		System.out.println(custs.size());
		*/
	}
	
	
}
