package com.gmi.nordborglab.browser.server.service;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.google.common.collect.ImmutableMap;

public interface GWASDataService {

	public ImmutableMap<String,GWASData> getGWASData(Long studyId);
}
