package microgram.impl.clt.srv;

import java.util.List;

import microgram.api.Post;
import microgram.api.java.Posts;
import microgram.api.java.Result;
import microgram.impl.clt.rest.RestPostsClient;
import microgram.impl.clt.soap.SoapPostsClient;

public class PostsCom extends ServerCom implements Posts{

	static final String SERVICE = "Microgram-Posts";
	
	static Posts impl;
	
	public PostsCom() {
		super(SERVICE);
		if(type.equalsIgnoreCase(REST)) {
			impl = new RestPostsClient(remoteServer);
		} else {
			impl = new SoapPostsClient(remoteServer);
		}
	}

	@Override
	public Result<Post> getPost(String postId) {
		return impl.getPost(postId);
	}

	@Override
	public Result<String> createPost(Post post) {
		return impl.createPost(post);
	}

	@Override
	public Result<Void> deletePost(String postId) {
		return impl.deletePost(postId);
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {
		return impl.like(postId, userId, isLiked);
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		return impl.isLiked(postId, userId);
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		return impl.getPosts(userId);
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		return impl.getFeed(userId);
	}
	
	@Override
	public Result<Void> deleteAll(String userId) {
		return impl.deleteAll(userId);
	}
}
