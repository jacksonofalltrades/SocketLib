package starwarp.net;

import java.util.HashMap;


public class IdentityPacket implements Packet
{
	private static final long serialVersionUID = 1L;
	
	public static int TYPE_SEND = 1;
	public static int TYPE_RECV = 2;
	
	protected String m_id;
	protected String m_clientId;
	protected int m_type;
	
	protected HashMap<String,String> m_extraDataMap;

	public IdentityPacket(String clientId, int type)
	{
		m_clientId = clientId;
		m_type = type;

		m_id = PacketIdGenerator.newId(m_type, m_clientId);
	}
	
	public void setExtraData(HashMap<String,String> data)
	{
		m_extraDataMap = data;
	}
	
	public HashMap<String,String>
	getExtraData()
	{
		return m_extraDataMap;
	}
	
	public int type()
	{
		return Packet.TYPE_ID;
	}
	
	public String getPacketId()
	{
		return m_id;
	}
	
	public String getSenderId()
	{
		return m_clientId;
	}
	
	public int getType()
	{
		return m_type;
	}
	
	public String toString()
	{
		StringBuffer l_sb = new StringBuffer();
		l_sb.append("IdentityPacket {\n");
		l_sb.append("\tid=");
		l_sb.append(m_id);
		l_sb.append("\tclientId=");
		l_sb.append(m_clientId);
		l_sb.append("\n\ttype=");
		l_sb.append(m_type);
		l_sb.append("\n\textraData=");
		l_sb.append(m_extraDataMap);
		l_sb.append("\n}\n");
		
		return l_sb.toString();
	}
}
