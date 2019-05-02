package microgram.impl.clt.srv;

import java.net.URI;

import discovery.Discovery;

public abstract class ServerCom {

	static final String REST = "/rest";
	
	static URI remoteServer;
	static String type;
	
	public ServerCom(String service) {
		URI[] uris = new URI[0];
		while(uris.length == 0) {
			uris = Discovery.findUrisOf(service, 1);
		}
		remoteServer = uris[0];
		type = remoteServer.getPath();
	}
}
