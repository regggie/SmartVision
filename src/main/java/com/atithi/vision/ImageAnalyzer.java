package com.atithi.vision;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.common.collect.ImmutableList;
import com.mongodb.annotations.Immutable;

public class ImageAnalyzer {
	
	final static String APPLICATION_NAME ="CHIRONS-ATITHI/1.0";
	final static String IMAGE_PATH ="E:/Relive_Cdac/Workspace_Mars/FaceDetection/image_sutraction.jpg";
			//"E:/MS/SUMMER/CMPE297/Project/Vision/SampleImages/image3-min.JPG";
	final static int maxResults =3;

	private Vision vision;
		
	public ImageAnalyzer(Vision vision){
		this.vision=vision;
	}
	  /**
	   * Connects to the Vision API using Application Default Credentials.
	   */
	  public static Vision getVisionService() throws IOException, GeneralSecurityException {
	    GoogleCredential credential =
	        GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
	    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
	    return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
	            .setApplicationName(APPLICATION_NAME)
	            .build();
	  }
	  
	  public Map<String, List<String>> labelImage(String imagePath){
		  
		  final Map<String,List<String>> labels = new HashMap<String, List<String>>();
		  
		  final Path path = Paths.get(imagePath);  
		  
		  try {
			
			final byte[] raw = Files.readAllBytes(path);
			
			/*final AnnotateImageRequest request =new AnnotateImageRequest().setImage(new Image().encodeContent(raw))
			.setFeatures(ImmutableList.of(new Feature().setType("LABEL_DETECTION").setMaxResults(3)
					, new Feature().setType("LOGO_DETECTION").setMaxResults(3)));*/
			
			AnnotateImageRequest request =
		            new AnnotateImageRequest()
		                .setImage(new Image().encodeContent(raw)).setFeatures(ImmutableList.of(
                    new Feature()
                        .setType("LABEL_DETECTION")
                        .setMaxResults(maxResults),
                    new Feature()
                        .setType("LOGO_DETECTION")
                        .setMaxResults(1),
                    new Feature()
                        .setType("TEXT_DETECTION")
                        .setMaxResults(maxResults),
                    new Feature()
                        .setType("LANDMARK_DETECTION")
                        .setMaxResults(1)));
			
			
			final Vision.Images.Annotate annotate = vision.images().annotate(
					new BatchAnnotateImagesRequest().
					setRequests(ImmutableList.of(request)));
			
			final BatchAnnotateImagesResponse batchResponse = annotate.execute();
		    //assert batchResponse.getResponses().size() == 1;
			
			System.out.println("Size of searches"+batchResponse.getResponses().size());
			
		    //final AnnotateImageResponse response = batchResponse.getResponses().get(0);
		    if (batchResponse.getResponses().get(0).getLabelAnnotations() != null) {
	        	
		    	final List<String> label = new ArrayList<String>();
	        	for (EntityAnnotation ea: batchResponse.getResponses().get(0).getLabelAnnotations()) {
	        		label.add(ea.getDescription());
	        	}
	        	labels.put("LABEL_ANNOTATION", label);
	        }
	        
	        if (batchResponse.getResponses().get(0).getLandmarkAnnotations() != null) {
	        	final List<String> landMark = new ArrayList<String>();
	        	for (EntityAnnotation ea : batchResponse.getResponses().get(0).getLandmarkAnnotations()) {
	        		landMark.add(ea.getDescription());
	    	    }
	        	labels.put("LANDMARK_ANNOTATION", landMark);
	        }
	        
	        if (batchResponse.getResponses().get(0).getLogoAnnotations() != null) {
	        	final List<String> logo = new ArrayList<String>();
	        	for (EntityAnnotation ea : batchResponse.getResponses().get(0).getLogoAnnotations()) {
	    	    	logo.add(ea.getDescription());
	    	    }
	        	labels.put("LOGO_ANNOTATION", logo);
	        }
	        
	        if (batchResponse.getResponses().get(0).getTextAnnotations() != null) {
	        	
	        	List<String> text = new ArrayList<String>();
	    	    for (EntityAnnotation ea : batchResponse.getResponses().get(0).getTextAnnotations()) {
		    	    text.add(ea.getDescription());
	    	    }
	    	    labels.put("TEXT_ANNOTATION", text);
	        }
		    
		    
		    return labels;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		  return null;
		  
	  }
	  
	  public static void printAnnotations(final List<EntityAnnotation> entityAnnotations) {
		  
		  if(entityAnnotations!=null && !entityAnnotations.isEmpty()) {
			  for (final EntityAnnotation entityAnnotation : entityAnnotations) {
				  final String desc = entityAnnotation.getDescription();
				  final Float score = entityAnnotation.getScore();
				  System.out.println(desc+"     "+score);
			  }
			  
		  }
	  }
	  
	  public static void main(String[] args) throws IOException, GeneralSecurityException {
			// TODO Auto-generated method stub
		    final ImageAnalyzer analyzer = new ImageAnalyzer(getVisionService());
		    final Map<String, List<String>> labels = analyzer.labelImage(IMAGE_PATH);
		    
		    for (Entry<String, List<String>> entry : labels.entrySet()) {
		    	
				 final String key = entry.getKey();
				 final List<String> value = entry.getValue();
				 if(value!=null && !value.isEmpty()) {
		    	 
				 System.out.println("Printing for key"+key);
		    	 for (final String myLebel : value) {
					
		    		 System.out.println(" "+myLebel);
		    	   }
				 
				 }
			}
		    
		    //System.out.println(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))		    
		}

}
