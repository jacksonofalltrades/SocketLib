package starwarp.net.bdcs;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import starwarp.net.ClientProxyCreationListener;
import starwarp.net.ClientShutdownObserver;
import starwarp.net.ClosedForSendingException;
import starwarp.net.IdentityPacket;
import starwarp.net.ClientProxy;
import starwarp.net.NetStats;
import starwarp.net.PacketHandler;
import starwarp.net.Server;
import starwarp.net.ShutdownPacket;

public class ServerImpl extends Thread implements Server, IdentityPacketHandler, ClientShutdownObserver
{
	private static final long serialVersionUID = 1L;
	protected static final int DEFAULT_BACKLOG = 100;
	protected static final int DEFAULT_PORT = 3333;
	
	protected int m_maxClients;
	protected boolean m_isRunning;
	protected int m_backlog;
	protected ServerSocket m_socket;
	protected ArrayList<ServerListener> m_serverListeners;
	protected PacketHandler m_packetHandler;
	protected HashMap<String,ClientProxy> m_clientProxyMap;
	
	protected ClientProxyCreationListener m_cppListener;
	protected ClientShutdownObserver m_csObserver;
	
	protected PrintStream m_outLogWriter;
	protected PrintStream m_errLogWriter;

	public ServerImpl(int maxClients,
			String host,
			int port)
	{
		this(maxClients, host, port, System.out, System.err);
	}
			
	
	public ServerImpl(int maxClients, 
			String host, 
			int port,
			PrintStream outLogWriter, 
			PrintStream errLogWriter)
	{
		m_outLogWriter = outLogWriter;
		m_errLogWriter = errLogWriter;
		
		try
		{	
			m_isRunning = true;
			m_backlog = DEFAULT_BACKLOG;
			m_socket = new ServerSocket(port);
			m_socket.setReuseAddress(true);

			m_maxClients = maxClients;
			m_serverListeners = new ArrayList<ServerListener>(maxClients);
			m_clientProxyMap = new HashMap<String,ClientProxy>();			
		}
		catch(UnknownHostException uhe)
		{
			uhe.printStackTrace(m_errLogWriter);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(m_errLogWriter);
		}
	}
	
	public void setPacketHandler(PacketHandler ph)
	{
		m_packetHandler = ph;
	}
	
	public void setClientProxyCreationListener(ClientProxyCreationListener cppListener)
	{
		m_cppListener = cppListener;
	}
	
	public void setClientShutdownObserver(ClientShutdownObserver csObserver)
	{
		m_csObserver = csObserver;
	}
	
	public void notifyClientShutdown(String clientId)
	{
		this.m_clientProxyMap.remove(clientId);
		
		m_csObserver.notifyClientShutdown(clientId);
	}
	
	public ClientProxy getClientProxy(String clientId)
	{
		return m_clientProxyMap.get(clientId);
	}
	
	public void handleIdentityPacket(ServerListener sl, Socket s, IdentityPacket ip)
	{		
		synchronized (this) {
			m_serverListeners.remove(sl);

			String l_clientId = ip.getSenderId();
			int type = ip.getType();
			if (!m_clientProxyMap.containsKey(l_clientId)) {
				ClientProxy l_cp = new ClientProxy(m_packetHandler, this, l_clientId, ip.getExtraData());
				m_clientProxyMap.put(l_clientId, l_cp);
				
				if (null != m_cppListener) {
					m_cppListener.notifyNewClientProxy(l_cp);
				}
			}
			
			ClientProxy l_cp = m_clientProxyMap.get(l_clientId);
			if (IdentityPacket.TYPE_SEND == type) {
				l_cp.setSendSocket(s);
				l_cp.start();
			}
			else if (IdentityPacket.TYPE_RECV == type) {
				l_cp.setReceiveSocket(s);
			}
			else {
				m_errLogWriter.println("Attempting to create ClientProxy with unknown identity packet type!!");
			}
		}
	}
		
	public void shutdown()
	{
		m_isRunning = false;
		try {
			m_socket.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(m_errLogWriter);
		}
		for(int i = 0; i < m_serverListeners.size(); i++) {
			ServerListener l_sl = (ServerListener)m_serverListeners.get(i);
			l_sl.shutdown();
		}
		
		java.util.Collection<ClientProxy> l_proxies = m_clientProxyMap.values();
		Object[] cpArr = l_proxies.toArray();

		for(int i = 0; i < cpArr.length; i++) {
			ClientProxy l_cp = (ClientProxy)cpArr[i];
			try {
				if (l_cp.isReadyForSending()) {
					l_cp.send(new ShutdownPacket(l_cp.getClientId(), ShutdownPacket.TYPE_SDR));
				}
			}
			catch(ClosedForSendingException cfse)
			{
				this.m_outLogWriter.println("Client "+l_cp.getClientId()+"[proxy] already shutdown.");
			}
		}
	}
	
	public void run()
	{
		while(m_isRunning)
		{
			try {
				Socket l_clientSock = m_socket.accept();
				
				// Wait on an initial packet to know what the type is (send or receive)
				// and the id of the client
					
				if (m_serverListeners.size() < m_maxClients)
				{
					ServerListener l_listener = new ServerListener(this, l_clientSock);
					l_listener.start();
						
					m_serverListeners.add(l_listener);
				}
				else
				{
					m_outLogWriter.println("Max clients ["+m_maxClients+"] are connected to the server");
				}
			}
			catch(SocketException se)
			{
				if (m_isRunning)
				{
					se.printStackTrace(m_errLogWriter);					
				}
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace(m_errLogWriter);
			}
		}
						
		if (!m_socket.isClosed()) {
			try
			{
				m_socket.close();
			
				m_outLogWriter.println("Shutting down server");
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace(m_errLogWriter);
			}
		}
				
		// Print stats
		Set<String> l_keys = m_clientProxyMap.keySet(); 
		java.util.Iterator<String> l_iter = l_keys.iterator();
		while(l_iter.hasNext()) {
			String l_key = l_iter.next();
			String l_stats = NetStats.printStats(l_key+" [proxy]");
			
			m_outLogWriter.println(l_stats);
			m_outLogWriter.println();
		}
	}
}
