package socket.test;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.UIManager;

import starwarp.net.Client;
import starwarp.net.ClosedForSendingException;
import starwarp.net.NetStats;
import starwarp.net.DataPacket;
import starwarp.net.PacketHandler;
import starwarp.net.ShutdownPacket;


public class TestClient extends JFrame implements ActionListener, PacketHandler
{
	private static final long serialVersionUID = 1L;
	protected String m_username;

	protected Client m_client;
	protected ClientSender m_sender;
	
	protected JButton m_exitButton;
					
	public void handleDataPacket(DataPacket p)
	{		
		TestPacket tp = (TestPacket)p;
		
		if (tp.requiresResponse()) {
			TestPacket l_tpRet = new TestPacket(m_username, TestPacket.TYPE_RESPONSE, "response"+tp.getMethodName(), false);
			try {
				if (m_client.send(l_tpRet)) {
					NetStats.packetResponseSent(m_username);
				}
			}
			catch(ClosedForSendingException cfse)
			{
			}
		}
	}
		
	public TestClient(String username, String serverHost)
	{
		super("Client ["+username+"]");
		
		try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		} catch(Exception xcp) {
		}

		m_exitButton = new JButton("Exit Client ["+username+"]");
		m_exitButton.addActionListener(this);
		m_exitButton.setPreferredSize(new Dimension(200, 50));
		
		getContentPane().add(m_exitButton);

		pack();

		setVisible(true);
					
		m_username = username;
		
		m_client = new starwarp.net.bdcs.ClientImpl(m_username, serverHost);
		m_client.setPacketHandler(this);

		m_sender = new ClientSender(m_client, 20);
	}
	
	public void shutdown()
	{
		m_sender.shutdown();
		try {
			m_client.send(new ShutdownPacket(this.m_username, ShutdownPacket.TYPE_SDR));
		}
		catch(ClosedForSendingException cfse) {
			System.out.println("Client ["+this.m_username+"] is already shutdown.");
		}
	}
	
	public void run()
	{
		m_client.start();
		m_sender.start();

		try {
			m_sender.join(15000);
			m_client.join(0);
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
		if (args.length < 2)
		{
			System.out.println("Usage: ClientImpl <username> <server hostname>");
			System.exit(0);
		}
		
		TestClient l_tc = new TestClient(args[0], args[1]);
		
		l_tc.run();
		
		System.exit(0);
	}
}
