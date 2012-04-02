package edu.cornell.jnutella;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import com.google.inject.Key;

public class SessionCallable implements Callable<Object> {
	
	private boolean running = false;
	
	public void submitTask(Runnable runnable) {
	}
	
	
	@Override
	public Object call() throws Exception {
		
		return null;
	}

}
