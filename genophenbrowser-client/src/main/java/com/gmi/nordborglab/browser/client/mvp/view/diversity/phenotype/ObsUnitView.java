package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import java.util.Map;

import com.gmi.nordborglab.browser.client.editors.ObsUnitDisplayEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.ObsUnitUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.phenotype.ObsUnitPresenter;
import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.shared.proxy.ObsUnitProxy;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ObsUnitView extends ViewWithUiHandlers<ObsUnitUiHandlers>
		implements ObsUnitPresenter.MyView {

	public interface ObsUnitDisplayDriver extends
			RequestFactoryEditorDriver<ObsUnitProxy, ObsUnitDisplayEditor> {
	}

	public interface Binder extends UiBinder<Widget, ObsUnitView> {
	}

	public static final ProvidesKey<ObsUnitProxy> KEY_PROVIDER = new ProvidesKey<ObsUnitProxy>() {
		public Object getKey(ObsUnitProxy item) {
			return item == null ? null : item.stableId();
		}
	};

	static class ObsUnitCell extends AbstractCell<ObsUnitProxy> {

		interface Template extends SafeHtmlTemplates {
			@Template("<table><tr><td rowspan='2'>{0}</td><td style='font-size:95%;'>{1}</td></tr><tr><td>{2}</td></tr></table>")
			SafeHtml showInfo(SafeHtml flag, String Name, String block);
		}

		private static Template template = GWT.create(Template.class);;
		private final Map<String,ImageResource> flagMap;

		public ObsUnitCell( final Map<String,ImageResource> flagMap) {
			super();
			this.flagMap = flagMap;
		}

		@Override
		public void render(com.google.gwt.cell.client.Cell.Context context,
				ObsUnitProxy value, SafeHtmlBuilder sb) {
			ImageResource img = flagMap.get("UNK");
			try {
				String origCty = value.getStock().getPassport().getCollection().getLocality().getOrigcty();
				if (flagMap.containsKey(origCty))
					img = flagMap.get(origCty);
			}
			catch (NullPointerException e) {
				
			}
			AbstractImagePrototype image  = AbstractImagePrototype.create(img);
			SafeHtml imageHTML = image.getSafeHtml();
			String name = "";
			if (value.getName() != null)
				name = value.getName();
			String block = "";
			if (value.getBlock() != null)
				block = value.getBlock();
			sb.append(template.showInfo(imageHTML, name, block));
		}
	}

	private final Widget widget;

	@UiField(provided = true)
	CellList<ObsUnitProxy> obsUnitList;
	@UiField
	ObsUnitDisplayEditor obsUnitDisplayEditor;
	@UiField(provided = true)
	SimplePager pager;

	protected final ObsUnitDisplayDriver displayDriver;
	protected final SingleSelectionModel<ObsUnitProxy> selectionModel = new SingleSelectionModel<ObsUnitProxy>(
			KEY_PROVIDER);

	@Inject
	public ObsUnitView(final Binder binder,
			final ObsUnitDisplayDriver displayDriver, 
			final FlagMap flagMap) {
		this.displayDriver = displayDriver;
		obsUnitList = new CellList<ObsUnitProxy>(new ObsUnitCell(flagMap.getMap()),
				KEY_PROVIDER);
		obsUnitList.setPageSize(30);
		obsUnitList.setKeyboardPagingPolicy(KeyboardPagingPolicy.CHANGE_PAGE);
		obsUnitList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
		pager = new SimplePager();
		widget = binder.createAndBindUi(this);

		this.displayDriver.initialize(obsUnitDisplayEditor);
		// scrollablePager.setDisplay(obsUnitList);
		obsUnitList.setSelectionModel(selectionModel);
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
					public void onSelectionChange(SelectionChangeEvent event) {
						getUiHandlers().onShowObsUnit(
								selectionModel.getSelectedObject());
					}
				});
		pager.setDisplay(obsUnitList);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public HasData<ObsUnitProxy> getDisplay() {
		return obsUnitList;
	}

	@Override
	public void setSelected(ObsUnitProxy obsUnit) {
		selectionModel.setSelected(obsUnit, true);
	}

	@Override
	public ObsUnitDisplayDriver getDisplayDriver() {
		return displayDriver;
	}
}
