package com.atithi.MessageListener;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpoint;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;


import org.glassfish.tyrus.client.ClientManager;

/**
 * WebSocket Client
 *
 */

@ClientEndpoint
public class WebSocketClient 
{
	private  Session session = null;
	public static final String IMAGE_DIR_UNIX ="/home/pi/face_image/";
	public static final String IMAGE_DIR_WINDOWS ="C:/Users/Gaurav/Pictures/Amdocs_LastDays/testing/";
	
	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}



	private static CountDownLatch latch;
	
	private static String URL = "ws://192.168.0.24:8081/ImageManager/feedback/test";
	
	
	@OnOpen
	public void onOpen(final Session session) {
		//putting RaspBerryID for correlation
		
		System.out.println("Client Side: Open Socket with SessionID"+session.getId());
		try {
			session.getBasicRemote().sendText("Client Sent Start");
		} catch (IOException e) {			
			e.printStackTrace();
		}
	}
	
	@OnMessage
	public void onMessage(final String message,final Session session) throws IOException {
		
		System.out.println("Message Received from Server "+message);
        /*BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
			String next_message =reader.readLine();
			session.getBasicRemote().sendText(next_message);
		} catch (IOException e) {
			
			e.printStackTrace();
		}*/
		if(!message.equals("Server saidClient Sent Start")) {
		File f = new File(IMAGE_DIR_UNIX+"Messages.txt");
		if(!f.exists()) {
			f.createNewFile();
		}
		PrintWriter writer = new PrintWriter(f);
		writer.write(message);
		writer.flush();
		writer.close();
		 
		session.close();
		latch.countDown();
		System.out.println("Coubt is: "+latch.getCount());
		return;
		}
					
	}
	
	@OnClose
	public void onClose(final Session session){
		
		System.out.println("Session with session ID"+session.getId()+"is terminated");
		latch.countDown();
	}
	
	public void sendMessge(final String message) {
		
		try {
			this.session.getBasicRemote().sendText(message);
		} catch (IOException e) {			
			e.printStackTrace();
		}
	
	}
	
	
	
    public static void main( String[] args ) throws DeploymentException, IOException, URISyntaxException
    {
    /*	
    	final ClientManager manager = ClientManager.createClient();
    	final WebSocketClient client = new WebSocketClient();
    	
    	latch = new CountDownLatch(1); 
    	
    	try {
    		
    	    final Session session = manager.connectToServer(WebSocketClient.class, new URI(URL));
    		client.setSession(session);
    	    latch.await();
			
		} catch (final InterruptedException e ) {
			e.printStackTrace();
		}*/
    	
    	
    	
    	System.out.println("Exceuting WebSocket first time");
    	
				
		final ClientManager manager = ClientManager.createClient();
		final WebSocketClient client = new WebSocketClient();
		 		
		WebSocketClient.latch = new CountDownLatch(1); 
		    	
		 try {
		    		
		  Session session = null;
			try {
						
		  session = manager.connectToServer(WebSocketClient.class, new URI(WebSocketClient.URL));
						
		   } 
		   catch (final DeploymentException e) {
						
			e.printStackTrace();
		    } catch (URISyntaxException e) {
						
			  e.printStackTrace();
			}
		    client.setSession(session);
		                		
		    WebSocketClient.latch.await();
			return;
			} catch (final InterruptedException e ) {
			 e.printStackTrace();
			}
				
			}
         	    	
      
    }

