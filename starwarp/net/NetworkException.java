package starwarp.net;

public class NetworkException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public NetworkException()
	{
		
	}
	
	public NetworkException(String message)
	{
		super(message);
	}
}
