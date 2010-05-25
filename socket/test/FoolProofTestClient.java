package socket.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FoolProofTestClient 
{
	public static void main(String[] args)
	{
		if (args.length < 1) {
			System.out.println("usage: FoolProofTestClient <server host>");
			System.exit(0);
		}
		String l_host = args[0];
		
		//InetSocketAddress l_clientSocketAddress = new InetSocketAddress("localhost", 3333);
		InetSocketAddress l_serverSocketAddress = new InetSocketAddress(l_host, 3333);

		// Bind to localhost
		Socket l_socket = new Socket();

		/*
		try
		{
			l_socket.bind(l_clientSocketAddress);
		}
		catch(BindException be)
		{
			be.printStackTrace(System.err);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(System.err);
		}
		*/
		
		try
		{
			l_socket.connect(l_serverSocketAddress);
		}
		catch(ConnectException ce)
		{
			ce.printStackTrace(System.err);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(System.err);
		}
		
		try {
			OutputStream l_os = l_socket.getOutputStream();
			
			ObjectOutputStream l_oos = new ObjectOutputStream(l_os);
			
			String l_pack = "Hello server!";
			l_oos.writeObject(l_pack);
						
			InputStream l_is = l_socket.getInputStream();
			
			ObjectInputStream l_ois = new ObjectInputStream(l_is);
			String l_fromServer = (String)l_ois.readObject();
						
			System.out.println("Got packet: ["+l_fromServer+"]");
		}
		catch(IOException io) {
			io.printStackTrace(System.err);
		}
		catch(ClassNotFoundException cnfe) {
			cnfe.printStackTrace(System.err);
		}
	}
}
