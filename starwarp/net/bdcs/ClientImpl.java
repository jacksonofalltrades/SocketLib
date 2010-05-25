package starwarp.net.bdcs;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import starwarp.net.Client;
import starwarp.net.ClosedForSendingException;
import starwarp.net.IdentityPacket;
import starwarp.net.NetStats;
import starwarp.net.Packet;
import starwarp.net.DataPacket;
import starwarp.net.PacketHandler;
import starwarp.net.ShutdownPacket;
import starwarp.net.SocketUtil;

public class ClientImpl extends Thread implements Client
{
	private static final long serialVersionUID = 1L;
	
	public static boolean DEBUG = true;
	
	static {
		String debug = System.getProperty("DEBUG");
		if (debug != null) {
			DEBUG = true;
		}
		else {
			DEBUG = false;
		}
	}
	
	private static final int SOCKET_SHUTDOWN_WAIT = 1000;
	protected static final int DEFAULT_PORT = 3333;
	protected boolean m_isConnected;
	protected boolean m_isRunning;
	protected String m_clientId;
	protected Socket m_sendSocket;
	protected Socket m_receiveSocket;
	protected InetSocketAddress m_serverSocketAddress;
		
	protected boolean m_isReadyForSending;
	
	protected ObjectInputStream m_inStream;
	
	protected PrintStream m_outLogWriter;
	protected PrintStream m_errLogWriter;
	
	protected PacketHandler m_packetHandler;
	
	// String=>String
	protected HashMap<String,String> m_extraIdentityDataMap;
					
	protected boolean sendIdentity()
	{
		IdentityPacket l_sip = new IdentityPacket(m_clientId, IdentityPacket.TYPE_SEND);
		l_sip.setExtraData(m_extraIdentityDataMap);
		boolean l_sStatus = SocketUtil.send(false, m_clientId, m_sendSocket, l_sip);

		boolean l_rStatus = false;
		if (l_sStatus) {
			IdentityPacket l_rip = new IdentityPacket(m_clientId, IdentityPacket.TYPE_RECV);
			l_rip.setExtraData(m_extraIdentityDataMap);
			l_rStatus = SocketUtil.send(false, m_clientId, m_receiveSocket, l_rip);
		}
		
		boolean ok = (l_sStatus && l_rStatus);
		if (!ok) {
			System.err.println("ClientImpl::sendIdentity: problem sending identity packet");
			shutdown();
			return false;
		}
		
		NetStats.packetInitiated(m_clientId, false);
		NetStats.packetInitiated(m_clientId, false);
		
		return true;
	}
	
	public void addIdentityData(String key, String value)
	{
		m_extraIdentityDataMap.put(key, value);
	}

	public void setPacketHandler(starwarp.net.PacketHandler ph)
	{
		m_packetHandler = ph;
	}
	
	public synchronized boolean send(Packet p) throws ClosedForSendingException
	{
		// If a ShutdownPacket is sent from outside,
		// we want to prevent any further packets from being
		// sent from outside
		if (Packet.TYPE_SDR == p.type()) {
			System.err.println("ClientImpl::send: ready for sending= false!");
			m_isReadyForSending = false;
			
			return internalSend(p);
		}
		
		if (m_isReadyForSending) {
			return internalSend(p);
		}
		
		throw new ClosedForSendingException();
	}

	protected synchronized boolean internalSend(Packet p)
	{
		
		if (!SocketUtil.send(false, m_clientId, m_sendSocket, p)) {
			shutdown();
			return false;
		}
				
		return true;
	}
	
	public String getClientId()
	{
		return m_clientId;
	}
	
	public boolean isReadyForSending()
	{
		return m_isReadyForSending;
	}
	
	public boolean isConnected()
	{
		return m_isConnected;
	}
	
	public void handleDataPacket(DataPacket p)
	{
		m_packetHandler.handleDataPacket(p);
	}

	public ClientImpl(String clientId, String serverHost) {
		this(clientId, serverHost, DEFAULT_PORT, System.out, System.err);
	}
	
	public ClientImpl(String clientId, 
			String serverHost,
			int port,
			PrintStream outLogWriter,
			PrintStream errLogWriter)
	{	
		m_outLogWriter = outLogWriter;
		m_errLogWriter = errLogWriter;
		
		m_isRunning = true;
		m_isConnected = false;
		m_clientId = clientId;
					
		m_serverSocketAddress = new InetSocketAddress(serverHost, port);

		m_sendSocket = new Socket();
		m_receiveSocket = new Socket();		
		
		m_extraIdentityDataMap = new HashMap<String,String>();
	}
	
	public void connect()
	{		
		if (!m_isConnected)
		{
			try
			{
				m_sendSocket.connect(m_serverSocketAddress);
				m_receiveSocket.connect(m_serverSocketAddress);
				m_isConnected = true;
			}
			catch(ConnectException ce)
			{
				//ce.printStackTrace(m_errLogWriter);
				m_errLogWriter.println("Server cannot be reached. Shutting down.");
				m_sendSocket = new Socket();
				m_receiveSocket = new Socket();	
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace(m_errLogWriter);
			}
		}
	}
	
	public void shutdown()
	{
		m_isRunning = false;
		try {
			Thread.sleep(1000);
			
			m_sendSocket.close();
			m_receiveSocket.close();
			m_isConnected = false;
		}
		catch(InterruptedException ie)
		{			
		}
		catch(IOException ioe)
		{
		}
	}
	
	public void run()
	{		
		boolean l_ok = this.sendIdentity();
		
		if (l_ok)
		{
			m_isReadyForSending = true;
		}
		else {
			m_isRunning = false;
		}
		
		while(m_isRunning)
		{
			if (!m_receiveSocket.isConnected())
			{
				shutdown();
				break;
			}
			try
			{
				if (m_receiveSocket.isInputShutdown())
				{
					shutdown();
					break;
				}
				
				InputStream l_is = m_receiveSocket.getInputStream();
				
				try
				{
					m_inStream = new ObjectInputStream(l_is);
				}
				catch(SocketException se)
				{
					if (m_isRunning)
					{
						se.printStackTrace(m_errLogWriter);
					}
					break;
				}
				catch(EOFException eofe)
				{
					m_errLogWriter.println("Server seems to have shutdown.");
					shutdown();
					break;
				}
				
				try
				{
					DataPacket l_tp = (DataPacket)m_inStream.readObject();
					
					if (DEBUG) {
						m_outLogWriter.println("Client ["+m_clientId+"] receiving packet: \n"+l_tp.toString());
					}

					NetStats.packetReceived(m_clientId);					
										
					if ((Packet.TYPE_SD&l_tp.type()) == l_tp.type()) {
						// If ClientProxy on server is requesting shutdown
						// we need to disable external sends from this client
						if (Packet.TYPE_SDR == l_tp.type()) {
							System.err.println("ClientImpl::run: received SDR packet, ready for sending == false!");
							m_isReadyForSending = false;
							internalSend(new ShutdownPacket(this.m_clientId, Packet.TYPE_SDA));
						}
						
						// If ClientProxy on server has accepted our request
						// for shutdown, it means we can safely shutdown
						// so we must send our final shut down packet
						// before doing so
						if (Packet.TYPE_SDA == l_tp.type()) {
							System.err.println("ClientImpl::run: received SDA packet, ready for sending == false!");
							m_isReadyForSending = false;
							internalSend(new ShutdownPacket(this.m_clientId, Packet.TYPE_SDF));
							shutdown();
						}
					}
					else {										
						handleDataPacket(l_tp);
					}
				}
				catch(ClassNotFoundException cnfe)
				{
					cnfe.printStackTrace(m_errLogWriter);
				}
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace(m_errLogWriter);
			}
		}

		try
		{
			if ((null != m_sendSocket) && !m_sendSocket.isClosed()) {
				m_sendSocket.shutdownInput();
				m_sendSocket.shutdownOutput();
			
				try
				{
					Thread.sleep(SOCKET_SHUTDOWN_WAIT);
				}
				catch(InterruptedException ie)
				{
				}

				m_sendSocket.close();
			}
			
			if ((null != m_receiveSocket) && !m_receiveSocket.isClosed()) {
				m_receiveSocket.shutdownInput();
				m_receiveSocket.shutdownOutput();
			
				try
				{
					Thread.sleep(SOCKET_SHUTDOWN_WAIT);
				}
				catch(InterruptedException ie)
				{
				}

				m_receiveSocket.close();
			
			}
			m_outLogWriter.println("Shutting down client ["+m_clientId+"]");
			m_outLogWriter.println(NetStats.printStats(m_clientId));
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(m_errLogWriter);
		}
	}
}
