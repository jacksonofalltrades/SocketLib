package socket.test;

import java.util.Random;

import starwarp.net.Client;
import starwarp.net.ClosedForSendingException;
import starwarp.net.NetworkThread;
import starwarp.net.NetStats;

public class ClientSender extends Thread implements NetworkThread {
	protected static final String[] SF_METHODS = new String[]{"A","B","C","D","E","F","G","H","I","J","K"};
	protected static final boolean[] SF_NEEDS_RETVAL = new boolean[]{false, true, true, false, true, true,false,false,true,true,false};
	protected static final int[] SF_PAUSE_TIMES = new int[]{1000,2000,3000,4000,5000};
	protected boolean m_isRunning;
	protected String m_clientId;
	protected Client m_client;
	
	protected int m_maxSend;
	protected int m_sendCount;

	protected 
	TestPacket
	makeRandomPacket()
	{
		Random l_rand = new Random();
		
		int l_index1 = l_rand.nextInt(SF_METHODS.length);
		int l_index2 = l_rand.nextInt(SF_NEEDS_RETVAL.length);
		
		String l_meth = SF_METHODS[l_index1];
		boolean l_needsRetval = SF_NEEDS_RETVAL[l_index2];
		
		TestPacket l_tp = new TestPacket(m_clientId, TestPacket.TYPE_CALL, l_meth, l_needsRetval);
		
		return l_tp;
	}
	
	public ClientSender(Client client, int maxSend)
	{
		m_client = client;
		m_clientId = client.getClientId();
		m_isRunning = true;
		m_maxSend = maxSend;
		m_sendCount = 0;
	}

	public void shutdown()
	{
		m_isRunning = false;
	}
	
	public void run()
	{
		while(!m_client.isReadyForSending())
		{
			int l_pauseTime = 50;
			try
			{
				Thread.sleep(l_pauseTime);
			}
			catch(InterruptedException ie)
			{
			}
		}
		
		Random l_rand = new Random();

		while(m_isRunning)
		{			
			TestPacket l_tp = makeRandomPacket();
			
			try {
				if (m_client.isReadyForSending()) {
					if (!m_client.send(l_tp)) {
						shutdown();
					}
					else {
						NetStats.packetInitiated(m_clientId, l_tp.requiresResponse());
						m_sendCount++;
					}
				}
			}
			catch(ClosedForSendingException cfse)
			{			
			}
			
			// Pause at random intervals bet 1 and 5 seconds
			// and then do stuff
			int l_pauseTime = SF_PAUSE_TIMES[l_rand.nextInt(SF_PAUSE_TIMES.length)];
			try
			{
				Thread.sleep(l_pauseTime);
			}
			catch(InterruptedException ie)
			{
			}
			
			if (m_maxSend > 0) {
				if (m_sendCount >= m_maxSend) {
					shutdown();
				}
			}
		}
	}
}
