package com.gmi.nordborglab.browser.server.data;

import java.util.Map;

public interface GWASReader {
	public GWASData readForChr(String file,String chr,Double limit);
	public Map<String, GWASData> readAll(String file,Double limit);
}
