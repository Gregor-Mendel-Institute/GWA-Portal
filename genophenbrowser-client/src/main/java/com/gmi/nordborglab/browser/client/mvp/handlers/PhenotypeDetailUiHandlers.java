package com.gmi.nordborglab.browser.client.mvp.handlers;

import com.gwtplatform.mvp.client.UiHandlers;

public interface PhenotypeDetailUiHandlers extends UiHandlers{

	public void onEdit();

	public void onSave();

	public void onCancel();

	public void onDelete();

	public void onSelectPhenotypeType(Integer type);
}
