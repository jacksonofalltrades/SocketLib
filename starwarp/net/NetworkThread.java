package starwarp.net;

public interface NetworkThread {
	public void start();
	public void shutdown();
	public void join(long millis) throws InterruptedException;
}
