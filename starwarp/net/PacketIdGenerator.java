package starwarp.net;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;

import starwarp.util.Base64;

public class PacketIdGenerator
{	
	public static String newId(int type, String senderId)
	{
		MessageDigest l_md;
		String l_hashStr;
		try {
			l_md = MessageDigest.getInstance("MD5");			
			Date l_date = new Date();
			long l_timeval = l_date.getTime();
			l_md.update(String.valueOf(type).getBytes());
			l_md.update(senderId.getBytes());			
			l_md.update(String.valueOf(l_timeval).getBytes());
			byte[] l_hash = l_md.digest();
						
			l_hashStr = Base64.byteArrayToBase64(l_hash);
			return l_hashStr;
		}
		catch(NoSuchAlgorithmException nsae)
		{
			nsae.printStackTrace(System.err);
			return null;
		}
	}
	
	public static String returnId(String origId)
	{
		Random l_rand = new Random();
		return origId+String.valueOf(l_rand.nextLong());
	}
}
