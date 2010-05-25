package starwarp.net;

import java.util.HashMap;

public class ShutdownPacket implements DataPacket 
{
	private static final long serialVersionUID = 1L;
	
	private static final HashMap<Integer,Boolean> sm_validTypes;
	
	static {
		sm_validTypes = new HashMap<Integer,Boolean>();
		sm_validTypes.put(Integer.valueOf(Packet.TYPE_SDF), Boolean.valueOf(true));
		sm_validTypes.put(Integer.valueOf(Packet.TYPE_SDR), Boolean.valueOf(true));
		sm_validTypes.put(Integer.valueOf(Packet.TYPE_SDA), Boolean.valueOf(true));		
	}
		
	protected String m_id;
	protected String m_senderId;
	protected int m_type;
	
	public ShutdownPacket(String senderId, int type)
	{
		m_senderId = senderId;
		
		if (sm_validTypes.containsKey(type)) {
			m_type = type;
		}
		else {
			throw new InvalidTypeException(type);
		}
		
		m_id = PacketIdGenerator.newId(m_type, m_senderId);		
	}

	public int type()
	{
		return m_type;
	}
	
	public String getPacketId()
	{
		return m_id;
	}

	public String getSenderId() 
	{
		return m_senderId;
	}
	
	public String toString()
	{
		StringBuffer l_sb = new StringBuffer();
		l_sb.append("ShutdownPacket {\n");
		l_sb.append("\tid=");
		l_sb.append(m_id);
		l_sb.append("\tclientId=");
		l_sb.append(m_senderId);
		l_sb.append("\n\ttype=");
		l_sb.append(m_type);
		l_sb.append("\n}\n");
		
		return l_sb.toString();
	}
}
