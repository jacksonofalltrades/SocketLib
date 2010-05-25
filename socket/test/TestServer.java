package socket.test;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

import starwarp.net.ClientProxy;
import starwarp.net.ClosedForSendingException;
import starwarp.net.DataPacket;
import starwarp.net.NetStats;
import starwarp.net.PacketHandler;
import starwarp.net.Server;
import starwarp.net.bdcs.ServerImpl;
//import starwarp.net.ServerListener;

public class TestServer extends JFrame implements ActionListener, PacketHandler
{
	private static final long serialVersionUID = 1L;

	protected JButton m_exitButton;
		
	protected Server m_server;
		
	protected ServerSender m_sender;
		
	public TestServer(int maxClients, String host, int port)
	{
		super("Test Server");
		
		try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch(Exception xcp) {
		}

		m_exitButton = new JButton("Exit Server");
		m_exitButton.addActionListener(this);
		m_exitButton.setPreferredSize(new Dimension(200, 50));
		
		getContentPane().add(m_exitButton);

		pack();

		setVisible(true);
				
		m_server = new ServerImpl(maxClients, host, port);
		m_server.setPacketHandler(this);
		
		m_sender = new ServerSender(20);
	}
		
	public void handleDataPacket(DataPacket p)
	{
		TestPacket tp = (TestPacket)p;
		
		if (tp.requiresResponse()) {
			String l_clientId = tp.getSenderId();
			
			ClientProxy cp = m_server.getClientProxy(l_clientId);
			if (null != cp) {
				m_sender.addClient(cp);			
				
				TestPacket l_tpRet = new TestPacket(l_clientId, TestPacket.TYPE_RESPONSE, "response"+tp.getMethodName(), false);
				
				try {
					cp.send(l_tpRet);
					
					NetStats.packetResponseSent(l_clientId+" [proxy]");
				}
				catch(ClosedForSendingException cfse)
				{				
				}
			}
		}
	}
	
	public void shutdown()
	{
		m_sender.shutdown();
		m_server.shutdown();
	}
	
	public void run()
	{
		m_sender.start();		
		m_server.start();
		
		try {
			m_sender.join(15000);
			m_server.join(0);
		}
		catch(InterruptedException ie)
		{
		}
	}
	
	public void actionPerformed(ActionEvent event)
	{
		Object src = event.getSource();
		if (src == m_exitButton)
		{
			this.shutdown();
		}
	}

	public static void main(String[] args)
	{
		TestServer l_ts = new TestServer(20, "localhost", 3333);
		l_ts.run();
		System.exit(0);
	}
}
