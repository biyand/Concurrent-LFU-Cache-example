//LFU FileCache and AccessCounter

import java.util.*;
import java.util.concurrent.locks.*;

public class LFUFileCache extends FileCache {
	private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
	//least frequently used cache replacement strategy: remove least used,
	//if there's a tie, remove oldest one
	static HashMap<String, String> cache = new LinkedHashMap<>();
	//BucketNode is the bucket container for all the keys with the same use frequency
	//keys are stored in the HashSet attribute of the BucketNode
	//freq is the count of the keys has been referenced
	private HashMap<String, BucketNode> bucket = new LinkedHashMap<>();
	private BucketNode head=null;

	@Override
	protected String replace(String targetFile) {
		rwLock.writeLock().lock();
		try{
			Iterator<String> it = head.fileKeys.iterator();
			String toBeRemoved = it.next();
			cache.remove(toBeRemoved);
			head.fileKeys.remove(toBeRemoved);
			bucket.remove(toBeRemoved);
			if(head.fileKeys.isEmpty())
				removeNode(head);		
		} finally{
			rwLock.writeLock().unlock();
		}
		return cacheFile(targetFile);
	}

	protected String cacheFile(String targetFile){
		String contents=loadFromDisk(targetFile);
		cache.put(targetFile, contents);
		if(head==null||head.freq>1){
			BucketNode newNode = new BucketNode(1);
			if(head != null)
				head.prev = newNode;
			newNode.next = head;
			head=newNode;
		}
		head.fileKeys.add(targetFile);
		bucket.put(targetFile, head);
		return contents;
	}

	private void rearrangeCache(String targetFile){
		BucketNode node =bucket.get(targetFile);
		node.fileKeys.remove(targetFile);
		if(node.next==null||node.next.freq != node.freq+1){
			BucketNode newNode = new BucketNode(node.freq+1);
			newNode.next = node.next;
			if(node.next != null) node.next.prev = newNode;
			newNode.prev = node;
			node.next=newNode;
		}
		node.next.fileKeys.add(targetFile);
		if(node.fileKeys.size()==0)
			removeNode(node);
	}

	@Override
	public String fetch(String targetFile) {

		rwLock.writeLock().lock();
		try{

			if ( !cache.containsKey(targetFile)) {
				if (cache.isEmpty() || cache.size() < cacheCapacity)
					return cacheFile(targetFile);
				else return replace(targetFile);
			}
			rwLock.readLock().lock();
		}
		finally{rwLock.writeLock().unlock();}

		try {
			rearrangeCache(targetFile);
			return cache.get(targetFile);
		}
		 finally {
			rwLock.readLock().unlock();
		}
	}

	private void removeNode(BucketNode node){
		if(node.prev == null)
			head=node.next;
		else node.prev.next=node.next;
		if(node.next != null)
			node.next.prev = node.prev;
	}

	class BucketNode{
		public int freq=0;
		public LinkedHashSet<String> fileKeys = null;
		public BucketNode prev=null,next=null;

		public BucketNode(int val){
			freq=val;
			fileKeys = new LinkedHashSet<String>();
		}
	}
}
