package com.atithi.rest.services.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.apache.cxf.helpers.FileUtils;
import org.bytedeco.javacpp.FlyCapture2.TimeStamp;
import org.zeroturnaround.zip.ZipUtil;

import com.atithi.db.DBUtil;
import com.atithi.db.MongoManager;
import com.atithi.db.MongoManager.Collections;
import com.atithi.gcm.GCMClient;
import com.atithi.rest.dto.History;
import com.atithi.rest.services.ImageService;
import com.atithi.vision.MyFaceRecognizer;

public class ImageServiceImpl implements ImageService {

	//
	private static final String SERVER_IP ="http://192.168.0.24:8081";
	//server Path
	public static final String PATH_TO_STORE ="E:/Sunbeam/apache-tomcat-8.0.33/webapps/ImageManager/imageStore/";
	public static final String ZIP_PATH_STORE ="C:/Users/Gaurav/Pictures/Amdocs_LastDays/SERVER_TRAIN_DIR/";
			//
			//"/opt/apache-tomcat-8.0.33/webapps/ImageManager/imageStore/";
			
			
	public static final String NOTIFICATION_URL_BASE =SERVER_IP+"/ImageManager/imageStore/";
	
			//"E:/Sunbeam/apache-tomcat-8.0.33/webapps/ImageManager/imageStore/";//
	
	public static  Map<Integer, String> MAPPING;
	
	
	
	
	static{
		MAPPING= new HashMap<Integer, String>();
		MAPPING.put(0, "OMKAR");
		MAPPING.put(1, "Chao");
		MAPPING.put(2, "Murtaza");
	}
	
	
    @Override
	public Response storeImage(final InputStream istream,final String fileName, final String userID) throws IOException {
		// TODO Auto-generated method stub
    	
    	System.out.println("--------------->"+fileName);
    	String filepath = null;
    	String fileExtension =fileName.substring(fileName.lastIndexOf(".")+1);
    	System.out.println("file Extension :"+fileExtension);
    	if(fileExtension.equals("zip")) {
	     filepath = ZIP_PATH_STORE+fileName;
    	}
    	else{
    	filepath = 	PATH_TO_STORE+fileName;
    	}
	 System.out.println("file path is :---"+filepath );
	  writeFile(istream, filepath);
	  if(fileExtension.equals("zip")) {
		  //unpack
		  final File zip = new File(filepath); 
		  ZipUtil.unpack(zip , new File(ZIP_PATH_STORE));
		  zip.delete();
	  }
		
	  final Response resp =Response.created(URI.create("/imageService/store/"+fileName)).build();
      if(!fileExtension.equals("zip")) {
	  //Call GCM Client
    	
		
		
		
	    System.out.println("UserID----------> " + userID);
	    final String file_url = NOTIFICATION_URL_BASE+fileName; 
	    final List<String> tokens= DBUtil.getTokensFromUserName(userID);
		System.out.println("Got the token------>"+tokens.get(0));
	    //Call GCM Client with list of tokens
		final GCMClient client = new GCMClient();
		
		/*
		 * Call to Vision Code with go here before sending Smart Notification
		 */
		
		MyFaceRecognizer f = new MyFaceRecognizer();
		f.train();
		Map<String, String> result= f.predict(filepath);
		int label = Integer.parseInt(result.get("Label"));
		double confidence = Double.parseDouble(result.get("Confidence"));
		String name = MAPPING.get(label);
		
		//maintain History DB
		History hist = new History();
		hist.setUser(userID);
		hist.setConfidence(confidence);
		hist.setPrediction(name);
		hist.setFileName(file_url);
		Date time = new Date();
		Timestamp timeStamp = new Timestamp(time.getTime());
		hist.setTimeStamp(timeStamp.toString());
		final MongoManager manager = new MongoManager();
		manager.store(hist, Collections.HISTORY);
		
		if(name!=null && confidence < 500) {
			System.out.println(name+"is at your door steps");
			
		}
		else {
			name = "Unknown Person";
		}
		
		//try sending it to fist token
		//file url
		if(tokens!=null && !tokens.isEmpty()) {
			for (String string : tokens) {
				    client.connect();
					System.out.println("SHOULD SEND AT LEAST ONE MESSAGE");
					System.out.println("file URL-------->"+file_url);
					client.send(string, file_url,name+" is at your door! ");
				
			}
		}
      }
            
		return resp;
	}
	
	public static void writeFile(InputStream istream, String filepath) throws IOException {
		System.out.println("Watch--------------->"+filepath);
		final File file = new File(filepath);
		if(!file.createNewFile()) {
			throw new WebApplicationException("Unable To Create File");
		}
		final OutputStream ostream = new FileOutputStream(file);
		int read =0;
		byte[] bytes = new byte[1024];
		while((read=istream.read(bytes))!=-1) {
			
			ostream.write(bytes, 0, read);
		}
		
		ostream.flush();
		ostream.close();
		istream.close();
		System.out.println("-------------File has been Stored--------------");
	}
	
	public static void main(String[] args) {
		
		ImageServiceImpl  a = new  ImageServiceImpl();
		
		File r = new File("C:\\Users\\Gaurav\\Pictures\\Amdocs_LastDays\\Murtaza_Cropped\\M2.png");
		FileInputStream stream=null;
		try {
			 stream = new FileInputStream(r);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			a.storeImage(stream, "M1.png", "Rasp1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
