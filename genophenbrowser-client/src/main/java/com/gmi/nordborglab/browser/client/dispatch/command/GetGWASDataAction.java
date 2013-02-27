package com.gmi.nordborglab.browser.client.dispatch.command;

import com.gmi.nordborglab.browser.client.dispatch.RequestBuilderActionImpl;
import com.gmi.nordborglab.browser.client.dto.GWASData;
import com.gmi.nordborglab.browser.client.dto.GWASDataDTO;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Response;

public class GetGWASDataAction extends RequestBuilderActionImpl<GetGWASDataActionResult> {


    public static enum TYPE {STUDY,GWASVIEWER}

    private final TYPE type;
	private final Long id;
	
	
	public GetGWASDataAction(final Long id,final TYPE type) {
		this.type = type;
        this.id = id;
	}
	

	@Override
	public String getUrl() {
		return getUrl(id,type);
	}

	@Override
	public GetGWASDataActionResult extractResult(Response response) {
		GWASData gwasData = JsonUtils.unsafeEval(response.getText());
		GWASDataDTO gwasDataDTO = new GWASDataDTO(gwasData);
		return new GetGWASDataActionResult(gwasDataDTO);
	}
	
	public static String getUrl(Long id,TYPE type)  {
		return BaseURL + "?id="+id+"&type="+type.toString().toLowerCase();
	}
}
