package microgram.impl.clt.java;

import microgram.api.java.Media;
import microgram.api.java.Result;

public class RetryMediaClient extends RetryClient implements Media {
	
	final Media impl;

	public RetryMediaClient(Media impl) {
		this.impl = impl;
	}

	@Override
	public Result<String> upload(byte[] bytes) {
		return reTry( () -> impl.upload(bytes));
	}

	@Override
	public Result<byte[]> download(String url) {
		return reTry( () -> impl.download(url));
	}
	
	@Override
	public Result<Void> delete(String url) {
		return reTry( () -> impl.delete(url));
	}
}