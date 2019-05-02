package discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Discovery {

	private static Logger Log = Logger.getLogger(Discovery.class.getName());

	static {
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
	}
	
	
	static final InetSocketAddress DISCOVERY_ADDR = new InetSocketAddress("226.226.226.226", 2266);
	static final int DISCOVERY_PERIOD = 1000;
	static final int DISCOVERY_TIMEOUT = 30000;

	private static final String DELIMITER = "\t";

	/**
	 * 
	 * Announces periodically a service in a separate thread .
	 * 
	 * @param serviceName the name of the service being announced.
	 * @param serviceURI the location of the service
	 */
	public static void announce(String serviceName, String serviceURI) {
		Log.info(String.format("Starting Discovery announcements on: %s for: %s -> %s", DISCOVERY_ADDR, serviceName, serviceURI));
		
		byte[] pktBytes = String.format("%s%s%s", serviceName, DELIMITER, serviceURI).getBytes();

		DatagramPacket pkt = new DatagramPacket(pktBytes, pktBytes.length, DISCOVERY_ADDR);
		new Thread(() -> {
			try (DatagramSocket ms = new DatagramSocket()) {
				for (;;) {
					ms.send(pkt);
					Thread.sleep(DISCOVERY_PERIOD);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}


	/**
	 * Performs discovery of instances of the service with the given name.
	 * 
	 * @param  serviceName the name of the service being discovered
	 * @param  minRepliesNeeded the required number of service replicas to find. 
	 * @return an array of URI with the service instances discovered. Returns an empty, 0-length, array if the service is not found within the alloted time.
	 * 
	 */
	public static URI[] findUrisOf(String serviceName, int minRepliesNeeded) {
		URI[] urls;
		Set<URI> seturi = new HashSet<URI>();
		int entries = 0;
		String message;
		String name;
		
		long timeremaining = DISCOVERY_TIMEOUT;
		long start;
		try(MulticastSocket socket = new MulticastSocket(DISCOVERY_ADDR.getPort())) {
			socket.joinGroup(DISCOVERY_ADDR.getAddress());
			socket.setSoTimeout(DISCOVERY_TIMEOUT);
			while(entries < minRepliesNeeded) {
				byte[] buffer = new byte[65536];
		        DatagramPacket request = new DatagramPacket(buffer, buffer.length);
		        start = System.currentTimeMillis();
				try {
					socket.receive(request);
					message = new String(request.getData(), 0, request.getLength());
					name = message.substring(0, message.indexOf(DELIMITER));
					if(serviceName.equals(name)) {
		        		seturi.add(URI.create(message.substring(message.indexOf(DELIMITER) + 1)));
		        		entries++;
		        	}
				} catch (SocketTimeoutException e) {
					
				} finally {
					timeremaining -= System.currentTimeMillis() - start;
					if(timeremaining <= 0) {
						break;
					}
					socket.setSoTimeout((int)timeremaining);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		urls = new URI[seturi.size()];
		seturi.toArray(urls);
		return urls;
	}	
}
