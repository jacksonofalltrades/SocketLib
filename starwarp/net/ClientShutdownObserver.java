package starwarp.net;

public interface ClientShutdownObserver 
{
	public void notifyClientShutdown(String clientId);
}
