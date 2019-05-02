package microgram.impl.srv.rest;

import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;

import microgram.api.java.Result;
import microgram.api.rest.RestMediaStorage;
import microgram.impl.srv.java.JavaMedia;

public class RestMediaResources extends RestResource implements RestMediaStorage {
	private static Logger Log = Logger.getLogger(RestMediaResources.class.getName());

	
	final String baseUri;
	final JavaMedia impl;
	
	public RestMediaResources(String baseUri ) {
		this.baseUri = baseUri + RestMediaStorage.PATH;
		this.impl = new JavaMedia();
	}
	
	@Override
	public String upload(byte[] bytes) {
		return baseUri + "/" + super.resultOrThrow(impl.upload(bytes));
	}

	@Override
	public byte[] download(String id) {
		return super.resultOrThrow(impl.download(id));
 	}
	
	@Override
	public void delete(String id) {
		super.resultOrThrow(impl.delete(id));
	}
}
