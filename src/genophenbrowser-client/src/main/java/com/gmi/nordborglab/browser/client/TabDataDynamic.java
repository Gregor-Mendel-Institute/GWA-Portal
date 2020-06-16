package com.gmi.nordborglab.browser.client;

import com.gwtplatform.mvp.client.TabDataBasic;
import com.gwtplatform.mvp.client.proxy.Gatekeeper;

public class TabDataDynamic extends TabDataBasic {

	private String historyToken;
	private final Gatekeeper gatekeeper;
	private boolean hasAccess =true;
	
	public TabDataDynamic(String label, float priority,String historyToken) {
		this(label,priority,historyToken,null);
	}
	
	public TabDataDynamic(String label, float priority,String historyToken,final Gatekeeper gatekeeper) {
		super(label, priority);
		this.historyToken = historyToken;
		this.gatekeeper = gatekeeper;
	}

	public String getHistoryToken() {
		return historyToken;
	}
	
	public void setHistoryToken(String historyToken) {
		this.historyToken = historyToken;
	}
	
	public Gatekeeper getGatekeeper() {
		return gatekeeper;
	}
	
	public boolean hasAccess() {
		return hasAccess;
	}
	
	public void setHasAccess(boolean hasAccess) {
		this.hasAccess = hasAccess;
	}
}
