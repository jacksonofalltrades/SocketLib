package starwarp.net;

/**
 * This exception is used to signal to the caller of a network node
 * that the specified node is closed for sending data.
 * @author dej
 *
 */
public class ClosedForSendingException extends NetworkException 
{
	private static final long serialVersionUID = 1L;

	public ClosedForSendingException()
	{		
	}
}
