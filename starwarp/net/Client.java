package starwarp.net;


/**
 * Represents a client in a client-server network architecture.
 * This type of client is one that has a representative on the server
 * among many other client reps on the server.
 * 
 * It is assumed that any communication between clients will be
 * mediated by the server.
 * 
 * @author dej
 *
 */
public interface Client extends NetworkThread, NetworkNode, PacketSender
{			
	public void connect();
	
	public boolean isConnected();
	
	/**
	 * Send a packet to the client rep which will get sent to the real client
	 * @param p packet to send
	 * @return true if the send succeeded, false otherwise
	 */
	public boolean send(Packet p) throws ClosedForSendingException;
	public String getClientId();
	public boolean isReadyForSending();
}
