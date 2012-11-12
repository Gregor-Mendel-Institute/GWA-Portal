package com.gmi.nordborglab.browser.client.mvp.view.germplasm.passport;

import javax.annotation.Nullable;

import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.client.ui.FlagCell;
import com.gmi.nordborglab.browser.client.ui.HighlightCell.SearchTerm;
import com.gmi.nordborglab.browser.client.ui.HighlightColumn;
import com.gmi.nordborglab.browser.client.ui.HyperlinkCell;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;

public interface PassportDataGridColumns {

	
	public abstract static class PassportHighlightColumn extends HighlightColumn<PassportProxy> {
		
		
		public PassportHighlightColumn(final SearchTerm searchTerm) {
			super(searchTerm);
		}
	}
	
	
	public static class IdColumn extends Column<PassportProxy,String[]> {
		
		private final PlaceManager placeManager;
		private final PlaceRequest placeRequest;

		public IdColumn(final PlaceManager placeManager,final PlaceRequest placeRequest) {
			super(new HyperlinkCell());
			this.placeManager = placeManager;
			this.placeRequest = placeRequest;
		}

		@Override
		public String[] getValue(PassportProxy object) {
			String[] hyperlink = new String[2];
			hyperlink[HyperlinkCell.LINK_INDEX] = "#"+placeManager.buildHistoryToken(placeRequest.with("id", object.getId().toString()));
			hyperlink[HyperlinkCell.URL_INDEX] = object.getId().toString();
			return hyperlink;
		}
	}
	
	public static class AccNameColumn extends PassportHighlightColumn{

		public AccNameColumn(SearchTerm searchTerm) {
			super(searchTerm);
		}

		@Override
		public String getValue(PassportProxy object) {
			return object.getAccename();
		}
	}
	
	public static class CountryColumn extends Column<PassportProxy,String> {

		public CountryColumn(FlagMap map) {
			super(new FlagCell(map));
		}

		@Override
		public String getValue(PassportProxy object) {
			String icon = null;
			try {
				icon = object.getCollection().getLocality().getOrigcty();
			} catch (Exception e) {

			}
			return icon;
		}
	}
	public static class CollectorColumn extends PassportHighlightColumn{

		public CollectorColumn(SearchTerm searchTerm) {
			super(searchTerm);
		}

		@Override
		public String getValue(PassportProxy object) {
			String collector = "";
			try {
				collector = object.getCollection().getCollector();
			}catch (Exception e) {
				
			}
			return collector;
		}
	}
	
	public static class CollDateColumn extends Column<PassportProxy,String> {

		public CollDateColumn() {
			super(new TextCell());
		}

		@Override
		public String getValue(PassportProxy object) {
			String collDate = null;
			try {
				collDate = object.getCollection().getCollDate();
			}catch (Exception e) {
				
			}
			return collDate;
		}
	}
	
	public static class TypeColumn extends Column<PassportProxy,String> {

		public TypeColumn() {
			super(new TextCell());
		}

		@Override
		public String getValue(PassportProxy object) {
			String sampStat = "";
			try {
				sampStat = object.getSampstat().getGermplasmType(); 
			}
			catch (Exception e) {} 
			return sampStat; 
		}
	}
	
	public static class SourceColumn extends PassportHighlightColumn{

		public SourceColumn(SearchTerm searchTerm) {
			super(searchTerm);
		}

		@Override
		public String getValue(PassportProxy object) {
			String source = "";
			try {
				
				source = object.getSource().getSource();
			}
			catch (Exception e) {} 
			return source; 
		}
	}
	
	public static class AccNumberColumn extends PassportHighlightColumn{
		
		public AccNumberColumn(SearchTerm searchTerm) {
			super(searchTerm);
		}

		@Override
		public String getValue(PassportProxy object) {
			return object.getAccenumb();
		}
	}
	
	public static class AlleleAssayColumn extends Column<PassportProxy,String> {

		public AlleleAssayColumn() {
			super(new TextCell());
			// TODO Auto-generated constructor stub
		}

		@Override
		public String getValue(PassportProxy object) {
			return Joiner.on(',').skipNulls().join(Iterables.transform(object.getAlleleAssays(), new Function<AlleleAssayProxy, String>() {

				@Override
				@Nullable
				public	String apply(@Nullable AlleleAssayProxy input) {
					String name=null;
					if (input != null)
						name = input.getName(); 
					return name;
				}
			
			}));
		}
		
	}
}
