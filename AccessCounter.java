//LFU FileCache and AccessCounter

import java.util.*;
import java.nio.file.*;
import java.util.concurrent.locks.*;

public class AccessCounter {
	private static Map<Path, Integer> accessCountMap = new HashMap<>();

	private ReentrantReadWriteLock rwLock;

	public AccessCounter(){
		rwLock = new ReentrantReadWriteLock();
	}

	public void increment(Path path){
		rwLock.writeLock().lock();
		try{
			if(accessCountMap.containsKey(path))
				accessCountMap.put(path,accessCountMap.get(path)+1);
			else
				{accessCountMap.put(path,1);}
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	public int getCount(Path path){
		rwLock.readLock().lock();
		int count=0;
		try{
			if(accessCountMap.containsKey(path))
				count= accessCountMap.get(path);
		} finally {
			rwLock.readLock().unlock();
		}
		return count;
	}

}
