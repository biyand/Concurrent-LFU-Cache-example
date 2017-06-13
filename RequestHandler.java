//LFU FileCache and AccessCounter

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;


public class RequestHandler implements Runnable {
	private ReentrantLock lock;
	private ReentrantReadWriteLock rwLock;
	private AccessCounter ac;
	private FileCache fc;
	private Path path;
	
	private volatile boolean done = false;
	private final int total=20;
	private Random rand;
	int filePick;
	String targetFile;
	File currDir = new File(".");
	String fileName = "/files/file";
	String prefix;
	String suffix=".txt";
	public void setDone(){
		done = true;
	}
	public RequestHandler(AccessCounter ac, FileCache fc){
		this.ac = ac;
		this.fc = fc;
		rand = new Random();
		filePick = rand.nextInt(total)+1;
		rwLock =new ReentrantReadWriteLock();
		lock = new ReentrantLock();
	}

	public void run() {
		rwLock.readLock().lock();
		try{
		System.out.println("\nExecuting the handler for file file"+String.valueOf(filePick)+suffix);
		prefix = currDir.getCanonicalPath();
		targetFile = prefix+ fileName+String.valueOf(filePick)+suffix;
		path=Paths.get(targetFile);
		System.out.println("\nFetching file: "+targetFile+", content: "+fc.fetch(targetFile));
		//TimeUnit.SECONDS.sleep(1);
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			rwLock.readLock().unlock();
		}		

		try{
		rwLock.writeLock().lock();

		ac.increment(path);
		System.out.println("\n"+targetFile+", current access count: "+ac.getCount(path));
		} finally {rwLock.writeLock().unlock();}
		try{
			TimeUnit.MILLISECONDS.sleep(100);
			rwLock.readLock().lock();
			if(Thread.interrupted()){
				if(done == true){
				System.out.println("\nDone is true.Thread for file: "+targetFile+" is interrupted!\n");
				}
			}else {
				System.out.println("\n"+path.toString()+": total access: "+ac.getCount(path)+"\n");
			}
		}catch (InterruptedException e) {
			
			e.printStackTrace();
			}
		 finally {
			 rwLock.readLock().unlock();
			}
	}

}
