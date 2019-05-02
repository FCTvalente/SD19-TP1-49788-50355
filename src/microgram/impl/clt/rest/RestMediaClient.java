package microgram.impl.clt.rest;

import java.net.URI;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import microgram.api.java.Media;
import microgram.api.java.Result;
import microgram.api.rest.RestMediaStorage;

public class RestMediaClient extends RestClient implements Media {

	public RestMediaClient(URI serverUri) {
		super(serverUri, RestMediaStorage.PATH);
	}

	@Override
	public Result<String> upload(byte[] bytes) {
		Response r = super.target.request().accept(MediaType.APPLICATION_JSON)
				.post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM));

		return super.responseContents(r, Status.OK, new GenericType<String>() {});
	}

	@Override
	public Result<byte[]> download(String url) {
		Response r = super.target.path(url)
				.request()
				.accept(MediaType.APPLICATION_OCTET_STREAM)
				.get();

		return super.responseContents(r, Status.OK, new GenericType<byte[]>() {});
	}
	
	@Override
	public Result<Void> delete(String url) {
		Response r = super.target.path(url)
				.request()
				.delete();

		return super.responseContents(r, Status.OK, new GenericType<Void>() {});
	}
}