package com.gmi.nordborglab.browser.client.dispatch.command;

import com.gmi.nordborglab.browser.client.dispatch.RequestBuilderActionImpl;
import com.gmi.nordborglab.browser.client.dto.GWASData;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Response;

public class GetGWASDataAction extends RequestBuilderActionImpl<GetGWASDataActionResult> {

	private final Long studyId;
	
	
	public GetGWASDataAction(final Long studyId) {
		this.studyId = studyId;
	}
	

	@Override
	public String getUrl() {
		return getUrl(studyId);
	}

	@Override
	public GetGWASDataActionResult extractResult(Response response) {
		GWASData gwasData = JsonUtils.unsafeEval(response.getText());
		GWASDataDTO gwasDataDTO = new GWASDataDTO(gwasData);
		return new GetGWASDataActionResult(gwasDataDTO);
	}
	
	public static String getUrl(Long studyId)  {
		return BaseURL + "?studyId="+studyId;
	}
}
