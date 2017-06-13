//LFU FileCache and AccessCounter

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.*;

public abstract class FileCache {
	HashMap<String, String> cache = new LinkedHashMap<>();
	protected int cacheCapacity = 20;
	public ReentrantLock lock = new ReentrantLock();
	protected abstract String replace(String targetFile);

	private String cacheFile(String targetFile){
		String contents=loadFromDisk(targetFile);
		try{
			lock.lock();
			cache.put(targetFile, contents);
		} finally {
			lock.unlock();
		}
		
		return contents;
	}

	public String fetch(String targetFile){
		try{		
			lock.lock();
			if (cache.containsKey(targetFile)) {
				return cache.get(targetFile);
			} else {
				if (cache.isEmpty() || cache.size() < cacheCapacity)
					return cacheFile(targetFile);
				else return replace(targetFile);
			}
		} finally {
			lock.unlock();
		}
	}

	protected String loadFromDisk(String targetFile){
		lock.lock();
		Path path = Paths.get(targetFile);
		List<String> contents = new ArrayList<>();
		try {
			contents = Files.readAllLines(path);
			System.out.println("\n"+contents.get(0));

		} catch (IOException e) {
			System.out.println("\nReadfile error! Check your input and try again.\n");
			//e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return contents.get(0);
	}
}
