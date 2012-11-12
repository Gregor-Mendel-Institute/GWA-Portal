package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.proxy.ObsUnitProxy;
import com.gwtplatform.mvp.client.UiHandlers;

public interface ObsUnitUiHandlers extends UiHandlers{
	
	public void onShowObsUnit(ObsUnitProxy obsUnit);

}
