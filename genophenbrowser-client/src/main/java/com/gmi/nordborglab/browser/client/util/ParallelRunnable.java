package com.gmi.nordborglab.browser.client.util;

public class ParallelRunnable implements Runnable {
	
	/** A reference to the parent callback, which runs when all are complete. */
    private ParentCallback parentCallback;

	@Override
	public void run() {
		parentCallback.done();
	}
	
	protected void setParent(ParentCallback parentCallback) {
        this.parentCallback = parentCallback;
    }

}
