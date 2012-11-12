package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import java.util.ArrayList;
import java.util.Date;

import com.gmi.nordborglab.browser.client.ui.HyperlinkCell;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.cell.client.DateCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public interface StudyListDataGridColumns {
	
public static class NameColumn extends Column<StudyProxy,String[]> {
		
		private final PlaceManager placeManager;
		private final PlaceRequest placeRequest;
		

		public NameColumn(final PlaceManager placeManager,final PlaceRequest placeRequest) {
			super(new HyperlinkCell());
			this.placeManager = placeManager;
			this.placeRequest = placeRequest;
		}

		@Override
		public String[] getValue(StudyProxy object) {
			String[] hyperlink = new String[2];
			hyperlink[HyperlinkCell.LINK_INDEX] = "#"+placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
			hyperlink[HyperlinkCell.URL_INDEX] = object.getName();
			return hyperlink;
		}
		
	}

	public static class ProducerColumn extends Column<StudyProxy,String> {

		public ProducerColumn() {
			super(new TextCell());
		}

		@Override
		public String getValue(StudyProxy object) {
			return object.getProducer();
		}
		
	}
	
	public static class StudyDateColumn extends Column<StudyProxy,Date> {

		public StudyDateColumn() {
			super(new DateCell());
		}

		@Override
		public Date getValue(StudyProxy object) {
			return object.getStudyDate();
		}
		
	}
	
	public static class AlleleAssayColumn extends Column<StudyProxy,String> {

		public AlleleAssayColumn() {
			super(new TextCell());
		}

		@Override
		public String getValue(StudyProxy object) {
			String assay = null;
			if (object.getAlleleAssay()!=null)
				assay = object.getAlleleAssay().getName();
			return assay;
		}
		
	}
	
	public static class ProtocolColumn extends Column<StudyProxy,String> {

		public ProtocolColumn() {
			super(new TextCell());
		}

		@Override
		public String getValue(StudyProxy object) {
			if (object.getProtocol() == null)
				return null;
			return object.getProtocol().getAnalysisMethod();
		}
		
	}


}
