package starwarp.net;

public class InvalidTypeException extends RuntimeException 
{
	private static final long serialVersionUID = 1L;
	
	protected int m_wrongType;
	
	public InvalidTypeException(int type)
	{
		m_wrongType = type;
	}

	public String getMessage()
	{
		return "Please provide a type other than ["+m_wrongType+"]";
	}
}
