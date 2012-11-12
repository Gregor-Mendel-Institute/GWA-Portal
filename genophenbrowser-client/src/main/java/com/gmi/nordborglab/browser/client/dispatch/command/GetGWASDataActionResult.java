package com.gmi.nordborglab.browser.client.dispatch.command;

import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.gwtplatform.dispatch.shared.Result;

public class GetGWASDataActionResult implements Result {
	private final GWASDataDTO resultData;
	
	public GetGWASDataActionResult(final GWASDataDTO resultData) {
		this.resultData = resultData;
	}
	
	public GWASDataDTO getResultData() {
		return this.resultData;
	}


}
