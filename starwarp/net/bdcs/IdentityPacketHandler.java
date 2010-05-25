package starwarp.net.bdcs;

import starwarp.net.IdentityPacket;


public interface IdentityPacketHandler {
	/**
	 * Handle packets for initiating a client's send and receive sockets
	 * @param sl ServerListener - 
	 * @param s
	 * @param ip
	 */
	public void handleIdentityPacket(ServerListener sl, java.net.Socket s, IdentityPacket ip);
}
