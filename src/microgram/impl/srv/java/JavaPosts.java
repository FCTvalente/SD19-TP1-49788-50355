package microgram.impl.srv.java;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import microgram.api.Post;
import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.srv.ProfilesCom;
import utils.Hash;

public class JavaPosts implements Posts {

	protected Map<String, Post> posts = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> likes = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> userPosts = new ConcurrentHashMap<>();

	protected final Profiles profiles;
	
	public JavaPosts() {
		profiles = new ProfilesCom();
	}
	
	@Override
	public Result<Post> getPost(String postId) {
		Post res = posts.get(postId);
		if (res != null)
			return ok(res);
		else
			return error(NOT_FOUND);
	}

	@Override
	public Result<Void> deletePost(String postId) {
		Post p = posts.remove(postId);
		if(p == null) 
			return error(NOT_FOUND);
		
		likes.remove(postId);
		userPosts.get(p.getOwnerId()).remove(postId);
		
		return ok();
	}

	@Override
	public Result<String> createPost(Post post) {
		
		Result<Profile> p = profiles.getProfile(post.getOwnerId());
		if(!p.isOK())
			return error(NOT_FOUND);
		
		String postId = Hash.of(post.getOwnerId(), post.getMediaUrl());
		if (posts.putIfAbsent(postId, post) == null) {

			likes.put(postId, ConcurrentHashMap.newKeySet());

			Set<String> posts = userPosts.get(post.getOwnerId());
			if (posts == null)
				userPosts.put(post.getOwnerId(), posts = ConcurrentHashMap.newKeySet());

			posts.add(postId);
		}
		return ok(postId);
	}

	@Override
	public Result<Void> like(String postId, String userId, boolean isLiked) {

		Set<String> res = likes.get(postId);
		if (res == null)
			return error(NOT_FOUND);

		if (isLiked) {
			if (!res.add(userId))
				return error(CONFLICT);
		} else {
			if (!res.remove(userId))
				return error(NOT_FOUND);
		}

		getPost(postId).value().setLikes(res.size());
		return ok();
	}

	@Override
	public Result<Boolean> isLiked(String postId, String userId) {
		Set<String> res = likes.get(postId);

		if (res != null)
			return ok(res.contains(userId));
		else
			return error(NOT_FOUND);
	}

	@Override
	public Result<List<String>> getPosts(String userId) {
		Set<String> res = userPosts.get(userId);
		if (res != null)
			return ok(new ArrayList<>(res));
		else
			return error(NOT_FOUND);
	}

	@Override
	public Result<List<String>> getFeed(String userId) {
		Result<List<String>> res = profiles.followingList(userId);
		if(!res.isOK())
			return error(NOT_FOUND);
		
		Set<String> postIds = new LinkedHashSet<String>();
		List<String> following = res.value();
		for (String followId : following) {
			Set<String> s = userPosts.get(followId);
			if(s != null)
				postIds.addAll(s);
		}
		
		return ok(new ArrayList<>(postIds));
	}

	@Override
	public Result<Void> deleteAll(String userId) {
		Set<String> up = userPosts.remove(userId);
		if(up != null)
			for (String postId : up) {
				posts.remove(postId);
				likes.remove(postId);
			}
		
		for (Map.Entry<String, Set<String>> post : likes.entrySet()) {
			Set<String> t = post.getValue();
			t.remove(userId);
			posts.get(post.getKey()).setLikes(t.size());;
		}
		
		return ok();
	}
}
