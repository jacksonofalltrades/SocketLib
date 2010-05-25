package socket.test;

import starwarp.net.DataPacket;
import starwarp.net.PacketIdGenerator;

public class TestPacket implements DataPacket
{
	private static final long serialVersionUID = 1L;
	
	public static final int TYPE_CALL = 1;
	public static final int TYPE_RESPONSE = 2;
	
	protected String m_id;
	protected String m_username;
	protected int m_type;
	protected String m_methodName;
	protected boolean m_requiresResponse;
	
	public TestPacket(String username, int type, String methodName, boolean requiresResponse)
	{
		m_username = username;
		m_type = type;
		m_methodName = methodName;
		
		if (TYPE_CALL == m_type) {
			m_requiresResponse = requiresResponse;
		}
		else {
			m_requiresResponse = false;
		}
		
		m_id = PacketIdGenerator.newId(m_type, m_username);	
	}
	
	public int type()
	{
		return starwarp.net.Packet.TYPE_CUST;
	}
	
	public String getPacketId()
	{
		return m_id;
	}
	
	public String getSenderId()
	{
		return m_username;
	}
	
	public String getMethodName()
	{
		return m_methodName;
	}
	
	public boolean requiresResponse()
	{
		return m_requiresResponse;
	}
	
	public
	String
	toString()
	{
		StringBuffer l_sb = new StringBuffer();
		l_sb.append("TestPacket {\n");
		l_sb.append("\tid=");
		l_sb.append(m_id);
		l_sb.append("\tusername=");
		l_sb.append(m_username);
		l_sb.append("\n\ttype=");
		l_sb.append(m_type);
		l_sb.append("\n\tmethodName=");
		l_sb.append(m_methodName);
		l_sb.append("\n\trequires response=");
		l_sb.append(m_requiresResponse);
		l_sb.append("\n}\n");
		
		return l_sb.toString();
	}
}
