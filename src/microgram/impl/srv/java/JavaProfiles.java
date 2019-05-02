package microgram.impl.srv.java;

import static microgram.api.java.Result.error;
import static microgram.api.java.Result.ok;
import static microgram.api.java.Result.ErrorCode.CONFLICT;
import static microgram.api.java.Result.ErrorCode.NOT_FOUND;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import microgram.api.Profile;
import microgram.api.java.Posts;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.srv.PostsCom;

public class JavaProfiles implements Profiles {

	protected Map<String, Profile> users = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> followers = new ConcurrentHashMap<>();
	protected Map<String, Set<String>> following = new ConcurrentHashMap<>();
	
	protected final Posts posts;
	
	public JavaProfiles() {
		posts = new PostsCom();
	}
	
	@Override
	public Result<Profile> getProfile(String userId) {
		Profile res = users.get( userId );
		if( res == null ) 
			return error(NOT_FOUND);

		res.setFollowers( followers.get(userId).size() );
		res.setFollowing( following.get(userId).size() );
		Result<List<String>> t = posts.getPosts(userId);
		res.setPosts(t.isOK() ? t.value().size() : 0);
		return ok(res);
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		Profile res = users.putIfAbsent( profile.getUserId(), profile );
		if( res != null ) 
			return error(CONFLICT);
		
		followers.put( profile.getUserId(), ConcurrentHashMap.newKeySet());
		following.put( profile.getUserId(), ConcurrentHashMap.newKeySet());
		return ok();
	}
	
	@Override
	public Result<Void> deleteProfile(String userId) {
		Profile user = users.remove(userId);
		if(user == null)
			return error(NOT_FOUND);
		
		following.remove(userId);
		followers.remove(userId);
		
		for (Map.Entry<String, Set<String>> s : following.entrySet()) {
			s.getValue().remove(userId);
		}
		for (Map.Entry<String, Set<String>> s : followers.entrySet()) {
			s.getValue().remove(userId);
		}
		
		posts.deleteAll(userId);
		
		return ok();
	}
	
	@Override
	public Result<List<Profile>> search(String prefix) {
		return ok(users.values().stream()
				.filter( p -> p.getUserId().startsWith( prefix ) )
				.collect( Collectors.toList()));
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {		
		Set<String> s1 = following.get( userId1 );
		Set<String> s2 = followers.get( userId2 );
		
		if( s1 == null || s2 == null)
			return error(NOT_FOUND);
		
		if( isFollowing ) {
			boolean added1 = s1.add(userId2 ), added2 = s2.add( userId1 );
			if( ! added1 || ! added2 )
				return error(CONFLICT);		
		} else {
			boolean removed1 = s1.remove(userId2), removed2 = s2.remove( userId1);
			if( ! removed1 || ! removed2 )
				return error(NOT_FOUND);					
		}
		return ok();
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {

		Set<String> s1 = following.get( userId1 );
		Set<String> s2 = followers.get( userId2 );
		
		if( s1 == null || s2 == null)
			return error(NOT_FOUND);
		else
			return ok(s1.contains( userId2 ) && s2.contains( userId1 ));
	}

	@Override
	public Result<List<String>> followingList(String userId) {
		Set<String> res = following.get(userId);
		if(res == null) 
			return error(NOT_FOUND);
		
		return ok(new ArrayList<>(res));
	}
}
