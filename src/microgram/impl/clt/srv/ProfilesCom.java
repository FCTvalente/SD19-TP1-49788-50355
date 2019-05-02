package microgram.impl.clt.srv;

import java.util.List;

import microgram.api.Profile;
import microgram.api.java.Profiles;
import microgram.api.java.Result;
import microgram.impl.clt.rest.RestProfilesClient;
import microgram.impl.clt.soap.SoapProfilesClient;

public class ProfilesCom extends ServerCom implements Profiles {
	
	static final String SERVICE = "Microgram-Profiles";
	
	static Profiles impl;
	
	public ProfilesCom() {
		super(SERVICE);
		if(type.equalsIgnoreCase(REST)) {
			impl = new RestProfilesClient(remoteServer);
		} else {
			impl = new SoapProfilesClient(remoteServer);
		}
	}

	@Override
	public Result<Profile> getProfile(String userId) {
		return impl.getProfile(userId);
	}

	@Override
	public Result<Void> createProfile(Profile profile) {
		return impl.createProfile(profile);
	}

	@Override
	public Result<Void> deleteProfile(String userId) {
		return impl.deleteProfile(userId);
	}

	@Override
	public Result<List<Profile>> search(String prefix) {
		return impl.search(prefix);
	}

	@Override
	public Result<Void> follow(String userId1, String userId2, boolean isFollowing) {
		return impl.follow(userId1, userId2, isFollowing);
	}

	@Override
	public Result<Boolean> isFollowing(String userId1, String userId2) {
		return impl.isFollowing(userId1, userId2);
	}
	
	@Override
	public Result<List<String>> followingList(String userId) {
		return impl.followingList(userId);
	}
}
