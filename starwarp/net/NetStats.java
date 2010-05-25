package starwarp.net;

import java.util.HashMap;

public class NetStats {
	protected static HashMap<String,NetStats> sm_nodeStatsMap;
	
	static {
		sm_nodeStatsMap = new HashMap<String,NetStats>();
	}
	
	String m_nodeName;
	
	// Total packets sent
	int m_tps;
	
	// Total packets received
	int m_tpr;
	
	// Packets initiated requiring no response
	int m_pin;

	// Packets initiated requiring a response
	int m_pir;
	
	// Packets sent as responses
	int m_pr;
		
	NetStats(String nodeName)
	{
		m_nodeName = nodeName;
		m_tps = 0;
		m_tpr = 0;
		m_pin = 0;
		m_pir = 0;
		m_pr = 0;
	}
	
	public String toString()
	{
		StringBuffer l_sb = new StringBuffer();
		l_sb.append("Node Name: ");
		l_sb.append(m_nodeName);
		l_sb.append("\nTotal Packets Sent: ");
		l_sb.append(m_tps);
		l_sb.append("\nTotal Packets Received: ");
		l_sb.append(m_tpr);
		l_sb.append("\nPackets initiated requiring NO response: ");
		l_sb.append(m_pin);
		l_sb.append("\nPackets initiated requiring response: ");
		l_sb.append(m_pir);
		l_sb.append("\nPackets sent as responses: ");
		l_sb.append(m_pr);
		
		return l_sb.toString();
	}
	
	protected static NetStats getStats(String nodeName)
	{
		NetStats l_ns = null;
		if (!sm_nodeStatsMap.containsKey(nodeName)) {
			l_ns = new NetStats(nodeName);
			
			sm_nodeStatsMap.put(nodeName, l_ns);
		}
		else {
			l_ns = sm_nodeStatsMap.get(nodeName);
		}
		
		return l_ns;
	}
	
	public static void packetReceived(String nodeName)
	{
		NetStats l_ns = getStats(nodeName);
		l_ns.m_tpr++;
	}
	
	public static void packetInitiated(String nodeName, boolean requiresResponse)
	{
		NetStats l_ns = getStats(nodeName);
		if (requiresResponse) {
			l_ns.m_pir++;
		}
		else {
			l_ns.m_pin++;
		}
		l_ns.m_tps++;
	}
	
	public static void packetResponseSent(String nodeName)
	{
		NetStats l_ns = getStats(nodeName);
		l_ns.m_pr++;
		l_ns.m_tps++;
	}
	
	public static String printStats(String nodeName)
	{
		NetStats l_ns = getStats(nodeName);
		return l_ns.toString();
	}
}
