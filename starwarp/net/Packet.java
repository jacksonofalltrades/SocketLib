package starwarp.net;

import java.io.Serializable;

public interface Packet extends Serializable 
{
	public static final int TYPE_SD   = 0x000f;
	public static final int TYPE_SDR  = 0x0001;
	public static final int TYPE_SDA  = 0x0002;
	public static final int TYPE_SDF  = 0x0004;
	public static final int TYPE_ID   = 0x0010;
	public static final int TYPE_CUST = 0x0100;
	
	public int type();
	
	public String getPacketId();
	
	public String getSenderId();
	
	public String toString();
}
