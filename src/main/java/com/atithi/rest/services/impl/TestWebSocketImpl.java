package com.atithi.rest.services.impl;

import com.atithi.feedback.Feedback;
import com.atithi.rest.services.TestWebSocket;

public class TestWebSocketImpl implements TestWebSocket {

	@Override
	public void sendCommand(String raspID, String command) {
	  System.out.println("RaspID is-----> :"+raspID);
	  System.out.println("Command is "+command);
	  Feedback.sendMessage(raspID, command);
		
	}

}
