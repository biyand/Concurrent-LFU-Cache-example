//LFU FileCache and AccessCounter

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class LFUFileCacheAccessCounterTest{
	public static void main(String[] args) throws Exception{
		final int total=40;
		RequestHandler handler;
		ExecutorService executor = Executors.newWorkStealingPool();
		ArrayList<RequestHandler> handlers = new ArrayList<>();
		LFUFileCache lfu = new LFUFileCache();
		AccessCounter ac = new AccessCounter();
		try{
			for(int i=0;i<total;i++){
			handler = new RequestHandler(ac,lfu);
			handlers.add(handler);
			executor.execute(handler);			
			}
			TimeUnit.SECONDS.sleep(5);
		}catch (Exception e){
			e.printStackTrace();
		} finally {
			executor.shutdown();
		}
	}
}
