package starwarp.net.bdcs;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;

import starwarp.net.IdentityPacket;
import starwarp.net.NetStats;
import starwarp.net.Packet;


class ServerListener
extends Thread
{
	protected static final int DEFAULT_BACKLOG = 100;
	protected IdentityPacketHandler m_handler;
	protected Socket m_clientSocket;
	protected boolean m_isRunning;
	
	public ServerListener(IdentityPacketHandler ph, Socket clientSocket)
	{
		m_handler = ph;
		m_isRunning = true;
		m_clientSocket = clientSocket;
	}
	
	public void shutdown()
	{
		m_isRunning = false;
	}
	
	public void run()
	{
		while(m_isRunning)
		{
			if (!m_clientSocket.isConnected())
			{
				shutdown();
				break;
			}
			try
			{
				if (m_clientSocket.isInputShutdown())
				{
					shutdown();
					break;
				}
				InputStream l_is = m_clientSocket.getInputStream();
				
				ObjectInputStream l_ois = null;
				try
				{
					l_ois = new ObjectInputStream(l_is);
				}
				catch(EOFException eofe)
				{
					System.out.println("Client seems to have shutdown.");
					shutdown();
					break;
				}
				
				try
				{
					Packet l_p = (Packet)l_ois.readObject();
					if (l_p.type() == Packet.TYPE_ID) {
						IdentityPacket l_ip = (IdentityPacket)l_p;
						m_handler.handleIdentityPacket(this, m_clientSocket, l_ip);
						this.shutdown();
						
						NetStats.packetReceived(l_ip.getSenderId()+" [proxy]");
					}
					else {
						System.err.println("Received non-identity packet from client "+l_p.getSenderId());
					}
				}
				catch(ClassNotFoundException cnfe)
				{
					cnfe.printStackTrace(System.err);
				}
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace(System.err);
			}
		}
	}
}
