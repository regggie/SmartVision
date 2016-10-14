package com.atithi.feedback;
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
//import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
//import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@ApplicationScoped
@ServerEndpoint(value="/feedback/{RASPID}")
public class Feedback {

	//@Inject
	
	private static SessionHandler sessionHandler = new SessionHandler();
	
	@OnOpen
	public void onOpen(@PathParam("RASPID") final String raspID, final Session session) {
		
		System.out.println("Server onOpen");
		System.out.println("Server : Wesocket is open and SessionID is "+session.getId());
		System.out.println("PATH PARAMETER"+raspID);
		//Establish Correlation in memory
		sessionHandler.addSession(raspID, session);
	
	}
	
	@OnMessage
	public void onMessage(final String message, final Session session) throws IOException {
		
		System.out.println("Reading the message:"+message);
		System.out.println("------------->"+session.getId());
		
		if(message.equals("quit")) {
			session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE,"BYE From Server"));
		}
		else {
			session.getBasicRemote().sendText("Server said"+message);
		}
			
	}
	
	@OnClose
    public void onClose(final Session session){
		final String raspID = session.getPathParameters().get("RASPID");
		sessionHandler.removeSession(raspID);
		System.out.println("Session " +session.getId()+" has ended");
    }
	
	@OnError
	public void onError(final Throwable err) {
		err.printStackTrace();
	}
	
	/**
	 * This is called when need to Android app invokes REST API
	 * @param raspID
	 * @param message
	 */
	public static void sendMessage(final String raspID, final String message) {
		
		final Session session = sessionHandler.getSession(raspID);
		try {
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
}
