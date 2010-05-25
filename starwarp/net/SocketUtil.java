package starwarp.net;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public abstract class SocketUtil {
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
	
	public static boolean send(boolean isServer, String clientId, Socket sock, Packet p)
	{
		String who = (isServer?"Server":"Client ["+clientId+"]");
		String to = (isServer?" to ["+clientId+"]":"");
		
		if (sock.isOutputShutdown())
		{
			return false;
		}
		try
		{
			OutputStream l_os = sock.getOutputStream();

			ObjectOutputStream l_oos;
			try {
				l_oos = new ObjectOutputStream(l_os);
				l_oos.writeObject(p);
			}
			catch(SocketException se)
			{
				se.printStackTrace(System.err);
				System.err.println(who+" cannot be reached.");
				return false;
			}
			
			if (DEBUG) {
				System.out.println(who+" sending packet"+to+":\n"+p.toString());
			}
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace(System.err);
			return false;
		}
		
		return true;
	}
}
