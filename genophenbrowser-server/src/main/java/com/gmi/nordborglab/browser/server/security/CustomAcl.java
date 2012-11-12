package com.gmi.nordborglab.browser.server.security;

import java.util.List;

public class CustomAcl {

	private List<CustomAccessControlEntry> aces;
	private boolean entriesInheriting = true;
	
	public CustomAcl() {}

	public CustomAcl(List<CustomAccessControlEntry> aces,
			boolean entriesInheriting) {
		this.aces = aces;
		this.entriesInheriting = entriesInheriting;
	}

	public boolean getIsEntriesInheriting() {
		return entriesInheriting;
	}
	
	public void setIsEntriesInheriting(boolean entriesInheriting)  {
		this.entriesInheriting = entriesInheriting;
	}
	
	public List<CustomAccessControlEntry> getEntries() {
		return aces;
	}
	
	public void setEntries(List<CustomAccessControlEntry> aces) {
		this.aces = aces;
	}

}
