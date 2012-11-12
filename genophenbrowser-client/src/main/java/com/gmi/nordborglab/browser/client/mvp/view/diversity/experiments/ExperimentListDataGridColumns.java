package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import com.gmi.nordborglab.browser.client.ui.HyperlinkCell;
import com.gmi.nordborglab.browser.shared.proxy.ExperimentProxy;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public interface ExperimentListDataGridColumns {
	
	
	public static class NameColumn extends Column<ExperimentProxy, String[]> {

		private final PlaceManager placeManager;
		private final PlaceRequest placeRequest;
		
		public NameColumn(final PlaceManager placeManager,final PlaceRequest placeRequest) {
			super(new HyperlinkCell());
			this.placeManager = placeManager;
			this.placeRequest = placeRequest;
		}

		@Override
		public String[] getValue(ExperimentProxy object) {
			String[] hyperlink = new String[2];
			hyperlink[HyperlinkCell.LINK_INDEX] = "#"+placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
			hyperlink[HyperlinkCell.URL_INDEX] = object.getName();
			return hyperlink;
		}
	}
	
	public static class DesignColumn extends Column<ExperimentProxy,String> {
		public DesignColumn() {
			super(new TextCell());
		}

		@Override
		public String getValue(ExperimentProxy object) {
			return object.getDesign();
		}
	}
	
	public static class OriginatorColumn extends Column<ExperimentProxy,String> {

		public OriginatorColumn() {
			super(new TextCell());
		}

		@Override
		public String getValue(ExperimentProxy object) {
			return object.getOriginator();
		}
	}
	
	public static class CommentsColumn extends Column<ExperimentProxy, String> {
		public CommentsColumn() {
			super(new TextCell());
		}

		@Override
		public String getValue(ExperimentProxy object) {
			return object.getComments();
		}
	}

}
