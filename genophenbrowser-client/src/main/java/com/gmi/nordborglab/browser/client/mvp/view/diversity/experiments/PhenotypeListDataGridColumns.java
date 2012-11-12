package com.gmi.nordborglab.browser.client.mvp.view.diversity.experiments;

import java.util.List;

import com.gmi.nordborglab.browser.client.ui.HyperlinkCell;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public interface PhenotypeListDataGridColumns {
	
	public static class NameColumn extends Column<PhenotypeProxy,String[]> {
		
		private final PlaceManager placeManager;
		private final PlaceRequest placeRequest;
		

		public NameColumn(final PlaceManager placeManager,final PlaceRequest placeRequest) {
			super(new HyperlinkCell());
			this.placeManager = placeManager;
			this.placeRequest = placeRequest;
		}

		@Override
		public String[] getValue(PhenotypeProxy object) {
			String[] hyperlink = new String[2];
			hyperlink[HyperlinkCell.LINK_INDEX] = "#"+placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
			hyperlink[HyperlinkCell.URL_INDEX] = object.getLocalTraitName();
			return hyperlink;
		}
		
	}
	
	public static class ProtocolColumn extends Column<PhenotypeProxy,String> {

		public ProtocolColumn() {
			super(new TextCell());
		}

		@Override
		public String getValue(PhenotypeProxy object) {
			return object.getTraitProtocol();
		}
		
	}
	
	public static class TraitOntologyColumn extends Column<PhenotypeProxy,String> {

		public TraitOntologyColumn(List<String> options) {
			super(new SelectionCell(options));
		}

		@Override
		public String getValue(PhenotypeProxy object) {
			//return object.getTraitOntology();
			return "";
		}
		
	}

}
