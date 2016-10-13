package com.atithi.feedback;

import java.util.HashMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;
import javax.websocket.Session;

@ApplicationScoped
@Singleton
public class SessionHandler {

	//stores correlation between raspID and Session 
	private HashMap<String, Session> sessions = new HashMap<String, Session>();
	
	/**
	 * This method is first called by Feedback.java whenever client
	 * opens a session with Tomcat Server
	 * @param raspID
	 * @param raspSession
	 */
	public void addSession(final String raspID, final Session raspSession) {
		
		sessions.put(raspID, raspSession);
	}
	
	public Session getSession(String raspID) {
		return this.sessions.get(raspID);
	}
	
	/**
	 * This method is first called by Feedback.java whenever client
	 * closes a session with Tomcat Server 
	 */
	public void removeSession(final String raspID) {
		sessions.remove(raspID);
	}
	
	
}
