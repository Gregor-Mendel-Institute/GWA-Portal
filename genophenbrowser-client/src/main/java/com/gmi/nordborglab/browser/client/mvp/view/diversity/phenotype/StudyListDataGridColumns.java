package com.gmi.nordborglab.browser.client.mvp.view.diversity.phenotype;

import java.util.Date;
import java.util.List;

import com.github.gwtbootstrap.client.ui.base.ProgressBarBase;
import com.github.gwtbootstrap.client.ui.constants.LabelType;
import com.gmi.nordborglab.browser.client.ui.cells.HyperlinkCell;
import com.gmi.nordborglab.browser.client.ui.cells.ProgressBarCell;
import com.gmi.nordborglab.browser.shared.proxy.StudyJobProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
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
	
	public static class PhenotypeColumn extends TextColumn<StudyProxy> {
		@Override
		public String getValue(StudyProxy object) {
			String phenotype = null;
			if (object.getPhenotype() != null)
				phenotype = object.getPhenotype().getLocalTraitName();
			return phenotype;
		}
	}
	
	public static class ExperimentColumn extends TextColumn<StudyProxy> {
		@Override
		public String getValue(StudyProxy object) {
			String experiment = null;
			if (object.getPhenotype().getExperiment() != null)
				experiment  = object.getPhenotype().getExperiment().getName();
			return experiment;
		}
	}

    public static class ProgressCell implements HasCell<StudyJobProxy,Number> {

        @Override
        public Cell<Number> getCell() {
            return new ProgressBarCell(true,true, null);
        }

        @Override
        public FieldUpdater<StudyJobProxy, Number> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Number getValue(StudyJobProxy object) {
            if (object != null) {
                return object.getProgress();
            }
            else
            {
                return null;
            }
        }
    }

    public static class StatusCell implements HasCell<StudyJobProxy,String> {

        @Override
        public Cell<String> getCell() {
            return new StatusTextCell();
        }

        @Override
        public FieldUpdater<StudyJobProxy, String> getFieldUpdater() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public String getValue(StudyJobProxy object) {
            if (object != null ){
               return object.getStatus();
            }
            return null;
        }
    }

    public static class StatusTextCell extends AbstractCell<String> {

        public interface Template extends SafeHtmlTemplates {
            @Template("<div class=\"label {0}\">{1}</div>")
            SafeHtml statustext(String cssClass,String label);
        }
        private static Template template = GWT.create(Template.class);

        @Override
        public void render(Context context, String value, SafeHtmlBuilder sb) {
            String className = "";
            if (value == null || value.equals("")) {
                value = "N/A";
            }
            if (value.equalsIgnoreCase("Finished")) {
                className = LabelType.SUCCESS.get();
            }
            else  if (value.equalsIgnoreCase("Running")) {
                className = LabelType.WARNING.get();
            }
            else if (value.equalsIgnoreCase("Queued")) {
                className = LabelType.IMPORTANT.get();
            }
            else if (value.equalsIgnoreCase("Error")) {
                className = LabelType.IMPORTANT.get();
            }
            sb.append(template.statustext(className,value));
        }
    }

    public static class StatusCompositeCell extends CompositeCell<StudyJobProxy> {


        public StatusCompositeCell(List<HasCell<StudyJobProxy, ?>> hasCells) {
            super(hasCells);
        }

        @Override
        protected <X> void render(Context context, StudyJobProxy value, SafeHtmlBuilder sb, HasCell<StudyJobProxy, X> hasCell) {
            final Cell<X> cell = hasCell.getCell();
            sb.appendHtmlConstant("<div style=\"display:inline-block;margin-right:20px;\">");
            if (!(cell instanceof ProgressBarCell && value != null && (value.getStatus().equalsIgnoreCase("Finished") || value.getStatus().equalsIgnoreCase("Error")))) {
                cell.render(context,hasCell.getValue(value),sb);
            }
            sb.appendHtmlConstant("</div>");
        }
    }


    public static class StatusColumn extends Column<StudyProxy,StudyJobProxy> {

        public StatusColumn(List<HasCell<StudyJobProxy,?>> cells) {
            super(new StatusCompositeCell(cells));
        }

        @Override
        public StudyJobProxy getValue(StudyProxy object) {
            return object.getJob();
        }
    }


}
