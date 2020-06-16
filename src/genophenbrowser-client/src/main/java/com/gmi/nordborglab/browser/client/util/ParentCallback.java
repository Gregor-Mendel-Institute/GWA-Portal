package com.gmi.nordborglab.browser.client.util;

public abstract class ParentCallback {

	 /** The number of service calls that have successfully completed. */
    private int doneCount = 0;
    
    private ParallelRunnable childCallbacks[];
    
    
    /**
     * Default constructor, passing in all child callbacks for the parent to check if they are done.
     */
    protected ParentCallback(ParallelRunnable... callbacks) {
        if (callbacks == null || callbacks.length == 0) {
            throw new RuntimeException("No callbacks passed to parent");
        }

        this.childCallbacks = callbacks;

        for (ParallelRunnable callback : callbacks) {
            callback.setParent(this);
        }
    }
    
    /**
     * Called by the child ParallelCallbacks on completion of the service call. Only when all
     * children have completed does this parent kick off it's on handleSuccess().
     */
    protected synchronized void done() {
        doneCount++;

        if (doneCount == childCallbacks.length) {
            handleSuccess();
        }
    }
    
    /**
     * Called only when all children callbacks have completed.
     */
    protected abstract void handleSuccess();
    
}
