package starwarp.net;


public interface Server extends NetworkNode, NetworkThread
{
	public ClientProxy getClientProxy(String clientId);
	
	public void setClientProxyCreationListener(ClientProxyCreationListener cppListener);
	public void setClientShutdownObserver(ClientShutdownObserver csObserver);
}
