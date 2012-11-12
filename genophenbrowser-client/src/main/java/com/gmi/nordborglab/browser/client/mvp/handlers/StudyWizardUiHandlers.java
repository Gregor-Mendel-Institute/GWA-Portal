package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gwtplatform.mvp.client.UiHandlers;

public interface StudyWizardUiHandlers extends UiHandlers {

	public void onCancel();

	public void onNext();

	public void onPrevious();

	public void onGenotypeChange(AlleleAssayProxy value);
	

	public void selectAllPhenotypeValues(Boolean value);

	public void onStatisticTypeChanged(StatisticTypeProxy type);

	public void onSearchName(String text);
	
	public void onFilterCountry(String country,boolean selected);
}
