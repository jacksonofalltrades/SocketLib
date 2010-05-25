package socket.test;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FoolProofTestServer 
{
	public FoolProofTestServer()
	{		
	}
	
	public static void main(String[] args)
	{		
		int port = 3333;
		
		ServerSocket l_serverSock = null;
		try {
			l_serverSock = new ServerSocket(port);
			l_serverSock.setReuseAddress(true);

			Socket l_clientSock = l_serverSock.accept();
			
			// Now listen for a packet
			InputStream l_is = l_clientSock.getInputStream();
			
			ObjectInputStream l_ois = null;
			try
			{
				l_ois = new ObjectInputStream(l_is);
			}
			catch(EOFException eofe)
			{
				eofe.printStackTrace(System.err);
			}
			
			try {
				String l_packet = (String)l_ois.readObject();
				
				System.out.println("Got packet: ["+l_packet+"]");
				
				try {
					Thread.sleep(1000);
				}
				catch(InterruptedException ie)
				{					
				}
				
				// Now send one back
				OutputStream l_os = l_clientSock.getOutputStream();
				ObjectOutputStream l_oos = null;
				try {
					l_oos = new ObjectOutputStream(l_os);
				}
				catch(EOFException eofe)
				{
					eofe.printStackTrace(System.err);
				}
				
				String l_toClient = "Hello Client!";
				l_oos.writeObject(l_toClient);
				
				try {
					Thread.sleep(1000);
				}
				catch(InterruptedException ie)
				{	
				}
			}
			catch(ClassNotFoundException cnfe)
			{
				cnfe.printStackTrace(System.err);
			}
		}
		catch(IOException ie)
		{
			ie.printStackTrace(System.err);
		}
		
	}
}
