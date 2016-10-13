package com.atithi.rest.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import oracle.jdbc.proxy.annotation.Post;
//http://localhost:8081/ImageManager/rest/toRaspberry/command/{RASPID}/{test}
@Path("/toRaspberry/")
public interface TestWebSocket {

	@Post
	@Path("/command/{RASPID}/{myCommand}")
	@Consumes(MediaType.TEXT_PLAIN)
	void sendCommand(@PathParam("RASPID")final String raspID, @PathParam("myCommand")final String command);
}
