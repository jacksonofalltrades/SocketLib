package socket.test;

import java.util.ArrayList;
import java.util.Random;

import starwarp.net.ClientProxy;
import starwarp.net.ClosedForSendingException;
import starwarp.net.NetStats;

public class ServerSender extends Thread {

	protected static final String[] SF_METHODS = new String[]{"P","Q","R","S","T","U","V","W","X","Y","Z"};
	protected static final int[] SF_PAUSE_TIMES = new int[]{1000,2000,3000,4000,5000};
	protected static final boolean[] SF_NEEDS_RETVAL = new boolean[]{false, true, true, false, true, true,false,false,true,true,false};
	
	protected boolean m_isRunning;
	protected ArrayList<ClientProxy> m_clients;
	
	protected int m_maxSend;
	protected int m_sendCount;
	
	public
	void
	shutdown()
	{
		m_isRunning = false;
	}

	protected 
	TestPacket
	makeRandomPacket()
	{
		Random l_rand = new Random();
		
		int l_index1 = l_rand.nextInt(SF_METHODS.length);
		int l_index2 = l_rand.nextInt(SF_NEEDS_RETVAL.length);

		String l_meth = SF_METHODS[l_index1];
		boolean l_needsRetval = SF_NEEDS_RETVAL[l_index2];		
		TestPacket l_tp = new TestPacket("SERVER", TestPacket.TYPE_CALL, l_meth, l_needsRetval);
		
		return l_tp;
	}
		
	public ServerSender(int maxSend)
	{
		m_isRunning = true;
		m_clients = new ArrayList<ClientProxy>();
		
		m_maxSend = maxSend;
		m_sendCount = 0;
	}
	
	public void addClient(ClientProxy cp)
	{
		m_clients.add(cp);
	}

	public void run()
	{		
		while(m_isRunning) {
			Random l_rand = new Random();

			for(int i = 0; i < m_clients.size(); i++)
			{
				ClientProxy cp = m_clients.get(i);
				
				TestPacket tp = this.makeRandomPacket();
				
				try {
					boolean status = cp.send(tp);
					if (!status) {
						this.shutdown();
					}
					else {
						NetStats.packetInitiated(cp.getClientId()+" [proxy]", tp.requiresResponse());
						m_sendCount++;
					}
				}
				catch(ClosedForSendingException cfse)
				{					
				}
			}
			
			int l_pauseTime = SF_PAUSE_TIMES[l_rand.nextInt(SF_PAUSE_TIMES.length)];
			try {
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
