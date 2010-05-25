package starwarp.net;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

public class ClientProxy extends Thread implements Client {
	protected Socket m_remoteSendSocket;
	protected Socket m_remoteReceiveSocket;
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
	
	protected PacketHandler m_handler;
	protected String m_clientId;
	protected boolean m_isReadyForSending;
	protected boolean m_isRunning;
	
	protected HashMap<String,String> m_extraDataMap;
	
	protected ClientShutdownObserver m_csObserver;
	
	protected PrintStream m_outLogWriter;
	protected PrintStream m_errLogWriter;
	
	public ClientProxy(PacketHandler ph, ClientShutdownObserver csObserver, String clientId, HashMap<String,String> extraData)
	{
		this(ph, csObserver, clientId, extraData, System.out, System.err);
	}

	public ClientProxy(PacketHandler ph, ClientShutdownObserver csObserver, String clientId, HashMap<String,String> extraData, PrintStream out, PrintStream err)
	{
		m_handler = ph;
		m_clientId = clientId;
		m_remoteSendSocket = null;
		m_remoteReceiveSocket = null;
		m_isRunning = true;
		
		m_extraDataMap = extraData;
		m_csObserver = csObserver;
		
		m_outLogWriter = out;
		m_errLogWriter = err;
	}
	
	public void connect()
	{
		
	}
	
	public boolean isConnected()
	{
		return true;
	}
	
	public void setPacketHandler(PacketHandler ph)
	{
		m_handler = ph;
	}
	
	public String getClientId()
	{
		return m_clientId;
	}
	
	public String getExtraDataValue(String key)
	{
		return m_extraDataMap.get(key);
	}
	
	public boolean isReadyForSending()
	{
		return m_isReadyForSending;
	}
	
	public void setSendSocket(Socket s)
	{
		if (null != s) {
			m_remoteSendSocket = s;
		}
	}
	
	public void setReceiveSocket(Socket s)
	{
		if (null != s) {
			m_remoteReceiveSocket = s;
			m_isReadyForSending = true;
		}
	}
	
	public void shutdown()
	{
		m_isRunning = false;
		try {
			m_remoteSendSocket.close();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(m_errLogWriter);
		}
		finally {
			m_remoteSendSocket = null;
			
			m_csObserver.notifyClientShutdown(this.m_clientId);
		}
	}
	
	public synchronized boolean send(Packet p) throws ClosedForSendingException
	{
		// If a ShutdownPacket is sent from outside,
		// we want to prevent any further packets from being
		// sent from outside
		if (Packet.TYPE_SDR == p.type()) {
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
		if (!isInitialized()) {
			m_errLogWriter.println("Attempt to send data to client ["+m_clientId+"] when not fully initialized.");
			return false;
		}

		if (!SocketUtil.send(true, m_clientId, m_remoteReceiveSocket, p)) {
			shutdown();
			return false;
		}	
		
		return true;
	}
	
	public boolean isInitialized()
	{
		if ((null != m_remoteSendSocket) && (null != m_remoteReceiveSocket)) {
			return true;
		}
		return false;
	}
	
	public void run()
	{
		while(m_isRunning)
		{
			try
			{
				if (m_remoteSendSocket.isInputShutdown())
				{
					shutdown();
					break;
				}
				InputStream l_is = m_remoteSendSocket.getInputStream();
				
				ObjectInputStream l_ois = null;
				try
				{
					l_ois = new ObjectInputStream(l_is);
				}
				catch(SocketException se)
				{
					m_outLogWriter.println("Client ["+m_clientId+"] seems to have shutdown.");
					shutdown();
					break;					
				}
				catch(EOFException eofe)
				{
					m_outLogWriter.println("Client ["+m_clientId+"] seems to have shutdown.");
					shutdown();
					break;
				}
				
				try
				{
					DataPacket l_dp = (DataPacket)l_ois.readObject();
					
					if (DEBUG) {
						m_outLogWriter.println("Server receiving packet from ["+m_clientId+"]: \n"+l_dp.toString());
					}
					
					NetStats.packetReceived(m_clientId+" [proxy]");
					
					if ((Packet.TYPE_SD&l_dp.type()) == l_dp.type()) {
						// If ClientProxy on server is requesting shutdown
						// we need to disable external sends from this client
						if (Packet.TYPE_SDR == l_dp.type()) {
							m_isReadyForSending = false;
							internalSend(new ShutdownPacket(this.m_clientId, Packet.TYPE_SDA));
						}
						
						// If ClientProxy on server has accepted our request
						// for shutdown, it means we can safely shutdown
						// so we must send our final shut down packet
						// before doing so
						if (Packet.TYPE_SDA == l_dp.type()) {
							m_isReadyForSending = false;
							internalSend(new ShutdownPacket(this.m_clientId, Packet.TYPE_SDF));
							shutdown();
						}	
					}
					else {
						m_handler.handleDataPacket(l_dp);
					}
				}
				catch(ClassNotFoundException cnfe)
				{
					cnfe.printStackTrace(m_errLogWriter);
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
	}
}
