package starwarp.net;

public interface PacketSender {
	public boolean send(Packet p) throws ClosedForSendingException;
}
