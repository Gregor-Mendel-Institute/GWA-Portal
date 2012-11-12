package com.gmi.nordborglab.browser.client.mvp.view.diversity.study;

import java.util.List;
import java.util.Set;

import at.gmi.nordborglab.widgets.geochart.client.GeoChart;

import com.gmi.nordborglab.browser.client.editors.StudyCreateEditor;
import com.gmi.nordborglab.browser.client.mvp.handlers.StudyWizardUiHandlers;
import com.gmi.nordborglab.browser.client.mvp.presenter.diversity.study.StudyWizardPresenter;
import com.gmi.nordborglab.browser.client.resources.DataGridResources;
import com.gmi.nordborglab.browser.client.resources.FlagMap;
import com.gmi.nordborglab.browser.client.ui.FlagCell;
import com.gmi.nordborglab.browser.client.ui.HighlightCell;
import com.gmi.nordborglab.browser.client.ui.HighlightCell.SearchTerm;
import com.gmi.nordborglab.browser.client.ui.ResizeableColumnChart;
import com.gmi.nordborglab.browser.client.ui.WizardPanel;
import com.gmi.nordborglab.browser.shared.proxy.AlleleAssayProxy;
import com.gmi.nordborglab.browser.shared.proxy.ObsUnitProxy;
import com.gmi.nordborglab.browser.shared.proxy.StatisticTypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProtocolProxy;
import com.gmi.nordborglab.browser.shared.proxy.StudyProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Multiset;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.visualizations.corechart.Options;
import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.gwt.client.RequestFactoryEditorDriver;
import com.google.web.bindery.requestfactory.gwt.ui.client.EntityProxyKeyProvider;
import com.google.web.bindery.requestfactory.gwt.ui.client.ProxyRenderer;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.event.ChosenChangeEvent.ChosenChangeHandler;
import com.watopi.chosen.client.gwt.ChosenListBox;

public class StudyWizardView extends ViewWithUiHandlers<StudyWizardUiHandlers>
		implements StudyWizardPresenter.MyView {

	static class CheckBoxHeader extends Header<Boolean> {

		protected boolean checked = false;

		public CheckBoxHeader() {
			super(new CheckboxCell(true, false));
		}

		@Override
		public Boolean getValue() {
			return checked;
		}

		public void reverseChecked() {
			checked = !checked;
		}
	}

	private final Widget widget;

	public interface StudyCreateDriver extends
			RequestFactoryEditorDriver<StudyProxy, StudyCreateEditor> {
	}

	public interface Binder extends UiBinder<Widget, StudyWizardView> {
	}

	public interface MyStyle extends CssResource {
		String emptyDataGridWidget();

		String loadingIndicator();
	}

	private final ScheduledCommand layoutCmd = new ScheduledCommand() {
		public void execute() {
			layoutScheduled = false;
			forceLayout();
		}
	};

	private boolean layoutScheduled = false;

	@UiField
	WizardPanel wizard;
	@UiField
	StudyCreateEditor studyCreateEditor;
	@UiField
	HTMLPanel piechartContainer;
	@UiField(provided = true)
	DataGrid<ObsUnitProxy> missingGenotypesGrid;
	@UiField(provided = true)
	DataGrid<TraitProxy> phenotypeDataGrid;
	@UiField
	SimpleLayoutPanel phenotypeContainer;
	@UiField
	SimpleLayoutPanel mapContainer;
	@UiField(provided = true)
	ValueListBox<StatisticTypeProxy> statisticTypeListBox;
	@UiField
	TextBox searchNameTb;
	//@UiField(provided=true)
	ChosenListBox countryFilterLb;
	
	@UiField HTMLPanel countryFilterContainer;

	@UiField
	MyStyle style;

	@UiField(provided = true)
	SimplePager pager;
	@UiField(provided = true)
	SimplePager phenotypeDataGridPager;

	private SearchTerm searchNameTerm = new SearchTerm(); 
	private PieChart dataAccessionPiechart;
	private final StudyCreateDriver studyCreateDriver;
	private HTMLPanel emptyDataGridWidget = new HTMLPanel(
			"There are no plants with missing genotypes");
	private HTMLPanel loadingIndicatorWidget = new HTMLPanel(
			"Select a genotype dataset");
	private HTMLPanel emptyPhenotypeDataGridWidget = new HTMLPanel(
			"There are no phenotype values to select");
	private ResizeableColumnChart phenotypeHistogramChart;
	private GeoChart geoChart = new GeoChart();

	@Inject
	public StudyWizardView(final Binder binder,
			final StudyCreateDriver studyCreateDriver, final FlagMap flagMap,
			final DataGridResources dataGridResources) {
		this.studyCreateDriver = studyCreateDriver;

		SimplePager.Resources pagerResources = GWT
				.create(SimplePager.Resources.class);
		pager = new SimplePager(TextLocation.CENTER, pagerResources, true, 100,
				true);
		phenotypeDataGridPager = new SimplePager(TextLocation.CENTER,
				pagerResources, true, 100, true);

		missingGenotypesGrid = new DataGrid<ObsUnitProxy>(
				new EntityProxyKeyProvider<ObsUnitProxy>());
		phenotypeDataGrid = new DataGrid<TraitProxy>(100, dataGridResources,
				new EntityProxyKeyProvider<TraitProxy>());
		phenotypeDataGrid.setMinimumTableWidth(500, Unit.PX);
		phenotypeDataGrid.setTableWidth(590, Unit.PX);
		phenotypeDataGrid.setSelectionModel(null, DefaultSelectionEventManager
				.<TraitProxy> createCheckboxManager());

		statisticTypeListBox = new ValueListBox<StatisticTypeProxy>(
				new ProxyRenderer<StatisticTypeProxy>(null) {

					@Override
					public String render(StatisticTypeProxy object) {
						if (object != null)
							return object.getStatType();
						return null;
					}
				});

		widget = binder.createAndBindUi(this);

		this.studyCreateDriver.initialize(studyCreateEditor);
		emptyDataGridWidget.setStylePrimaryName(style.emptyDataGridWidget());
		loadingIndicatorWidget.setStylePrimaryName(style.loadingIndicator());
		emptyPhenotypeDataGridWidget.setStylePrimaryName(style
				.emptyDataGridWidget());

		pager.setDisplay(missingGenotypesGrid);
		phenotypeDataGridPager.setDisplay(phenotypeDataGrid);

		missingGenotypesGrid.setEmptyTableWidget(emptyDataGridWidget);
		missingGenotypesGrid.setLoadingIndicator(loadingIndicatorWidget);
		phenotypeDataGrid.setEmptyTableWidget(emptyPhenotypeDataGridWidget);

		wizard.addCancelButtonClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				getUiHandlers().onCancel();
			}
		});

		wizard.addNextButtonClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				getUiHandlers().onNext();
			}
		});
		wizard.addPreviousButtonClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				getUiHandlers().onPrevious();

			}
		});
		studyCreateEditor
				.addGenotypeChangeHandler(new ValueChangeHandler<AlleleAssayProxy>() {

					@Override
					public void onValueChange(
							ValueChangeEvent<AlleleAssayProxy> event) {
						getUiHandlers().onGenotypeChange(event.getValue());
					}
				});

		statisticTypeListBox
				.addValueChangeHandler(new ValueChangeHandler<StatisticTypeProxy>() {

					@Override
					public void onValueChange(
							ValueChangeEvent<StatisticTypeProxy> event) {
						getUiHandlers()
								.onStatisticTypeChanged(event.getValue());
					}
				});
		searchNameTb.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				getUiHandlers().onSearchName(searchNameTb.getText());
			}
		});
		countryFilterContainer.clear();
		countryFilterLb = new ChosenListBox(true);
		countryFilterLb.setMaxSelectedOptions(5);
		countryFilterLb.setWidth("200px");
		countryFilterContainer.add(countryFilterLb);
		countryFilterLb.setPlaceholderText("Select countries to filter...");
		countryFilterLb.addChosenChangeHandler(new ChosenChangeHandler() {
			
			@Override
			public void onChange(ChosenChangeEvent event) {
				getUiHandlers().onFilterCountry(event.getValue(), event.isSelection());
			}
		});
		initMissingGenotypesDataGrid(flagMap);
		initPhenotypeDataGrid(flagMap);
		mapContainer.add(geoChart);
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public StudyCreateDriver getStudyCreateDriver() {
		return studyCreateDriver;
	}

	@Override
	public void setAcceptableValues(
			List<StudyProtocolProxy> studyProtocolValues,
			List<AlleleAssayProxy> alleleAssayValues,
			List<StatisticTypeProxy> statisticTypeValues) {
		studyCreateEditor.setAcceptableValues(studyProtocolValues,
				alleleAssayValues);
		statisticTypeListBox.setValue(statisticTypeValues.get(0), false); // Workaround
																			// for
																			// Bug
																			// for
																			// ValueListBox
																			// (fixed
																			// in
																			// 2.5)
		statisticTypeListBox.setAcceptableValues(statisticTypeValues);
	}

	@Override
	public void setPreviousStep() {
		wizard.previousStep();

	}

	@Override
	public void setNextStep() {
		wizard.nextStep();
	}

	@Override
	public void showAccessionGenotypeOverlapChart(
			int numberOfObsUnitsWithGenotype,
			int numberOfObsUnitsWithoutGenotype) {
		DataTable dataTable = DataTable.create();
		dataTable.addColumn(ColumnType.STRING, "Type");
		dataTable.addColumn(ColumnType.NUMBER, "Plants");
		dataTable.addRows(2);
		dataTable.setValue(0, 0, "Genotype");
		dataTable.setValue(0, 1, numberOfObsUnitsWithGenotype);
		dataTable.setValue(1, 0, "No genotype");
		dataTable.setValue(1, 1, numberOfObsUnitsWithoutGenotype);
		piechartContainer.clear();
		dataAccessionPiechart = new PieChart(dataTable,
				createPieChartOptions(false));
		piechartContainer.add(dataAccessionPiechart);
	}

	private PieChart.PieOptions createPieChartOptions(boolean isblank) {
		PieChart.PieOptions options = PieChart.PieOptions.create();
		options.setHeight(400);
		options.setWidth(600);
		options.setTitle("Genotype overlap");
		if (isblank) {
			options.setColors("#888");
		}
		Options animationOptions = Options.create();
		animationOptions.set("duration", 1000.0);
		animationOptions.set("easing", "out");
		options.set("animation", animationOptions);
		return options;
	}

	@Override
	public void showBlankPieChart() {
		DataTable dataTable = DataTable.create();
		dataTable.addColumn(ColumnType.STRING, "Type");
		dataTable.addColumn(ColumnType.NUMBER, "Plants");
		dataTable.addRows(1);
		dataTable.setValue(0, 0, "Select a genotype dataset");
		dataTable.setValue(0, 1, 1);
		piechartContainer.clear();
		dataAccessionPiechart = new PieChart(dataTable,
				createPieChartOptions(true));
		piechartContainer.add(dataAccessionPiechart);
	}

	@Override
	public void showBlankColumnChart() {
		DataTable data = DataTable.create();
		data.addColumn(ColumnType.STRING, "Range");
		data.addColumn(ColumnType.NUMBER, "Frequency");
		data.addRows(3);
		data.setValue(0, 0, "A");
		data.setValue(0, 1, 5);
		data.setValue(1, 0, "B");
		data.setValue(1, 1, 10);
		data.setValue(2, 0, "C");
		data.setValue(2, 1, 7);
		drawHistogram(data, createColumnChartOptions(true));
	}

	@Override
	public void showBlankGeoChart() {
		DataTable data = DataTable.create();
		data.addColumn(ColumnType.STRING, "Country");
		data.addColumn(ColumnType.NUMBER, "Frequency");
		drawGeoChart(data, createGeoChart(true));
	}

	private GeoChart.Options createGeoChart(boolean isblank) {
		GeoChart.Options options = GeoChart.Options.create();
		if (isblank) {
		}
		options.setTitle("Geographic distribution");
		options.setWidth(mapContainer.getOffsetWidth());
		return options;
	}

	private Options createColumnChartOptions(boolean isblank) {
		Options options = Options.create();
		if (isblank) {
			options.setColors("#CCC");
			Options toolTip = Options.create();
			toolTip.set("trigger", "none");
			options.set("tooltip", toolTip);
			Options legendOption = Options.create();
			legendOption.set("position", "none");
			options.set("legend", legendOption);
		}
		options.setTitle("Phenotype Histogram");
		Options animationOptions = Options.create();
		animationOptions.set("duration", 1000.0);
		animationOptions.set("easing", "out");
		options.set("animation", animationOptions);
		return options;
	}

	private void drawHistogram(DataTable data, Options options) {
		if (phenotypeHistogramChart == null) {
			phenotypeHistogramChart = new ResizeableColumnChart(data, options);
			phenotypeContainer.add(phenotypeHistogramChart);
		} else
			phenotypeHistogramChart.draw2(data, options);
	}

	private void drawGeoChart(DataTable data, GeoChart.Options options) {
		geoChart.draw(data, options);
	}

	@Override
	public void showPhenotypeHistogramChart(
			ImmutableSortedMap<Double, Integer> data) {
		DataTable dataTable = DataTable.create();
		dataTable.addColumn(ColumnType.STRING, "Bin");
		dataTable.addColumn(ColumnType.NUMBER, "Frequency");
		dataTable.addRows(data.size() - 1);
		ImmutableList<Double> keys = data.keySet().asList();
		ImmutableList<Integer> values = data.values().asList();
		for (int i = 0; i < data.size() - 1; i++) {
			dataTable.setValue(i, 0, keys.get(i) + " - " + keys.get(i + 1));
			dataTable.setValue(i, 1, values.get(i));
		}
		drawHistogram(dataTable, createColumnChartOptions(false));
	}

	private void forceLayout() {
		if (!widget.isAttached() || !widget.isVisible())
			return;
		showBlankPieChart();
	}

	@Override
	public void scheduledLayout() {
		if (widget.isAttached() && !layoutScheduled) {
			layoutScheduled = true;
			Scheduler.get().scheduleDeferred(layoutCmd);
		}
	}

	@Override
	public void showMissingGenotypes(List<ObsUnitProxy> missingGenotypes) {
		// TODO Auto-generated method stub

	}

	private void initMissingGenotypesDataGrid(FlagMap flagMap) {

		missingGenotypesGrid.addColumn(new Column<ObsUnitProxy, String>(
				new FlagCell(flagMap)) {
			@Override
			public String getValue(ObsUnitProxy object) {
				String icon = null;
				try {
					icon = object.getStock().getPassport().getCollection()
							.getLocality().getOrigcty();
				} catch (NullPointerException e) {

				}
				return icon;
			}
		}, "");

		missingGenotypesGrid.addColumn(new Column<ObsUnitProxy, String>(
				new TextCell()) {
			@Override
			public String getValue(ObsUnitProxy object) {
				return object.getName();
			}
		}, "Name");

		missingGenotypesGrid.addColumn(new Column<ObsUnitProxy, String>(
				new TextCell()) {

			@Override
			public String getValue(ObsUnitProxy object) {
				return object.getSeason();
			}

		}, "Season");

		missingGenotypesGrid.addColumn(new Column<ObsUnitProxy, String>(
				new TextCell()) {

			@Override
			public String getValue(ObsUnitProxy object) {
				// TODO Auto-generated method stub
				return object.getBlock();
			}

		}, "Block");

		missingGenotypesGrid.addColumn(new Column<ObsUnitProxy, String>(
				new TextCell()) {

			@Override
			public String getValue(ObsUnitProxy object) {
				// TODO Auto-generated method stub
				return object.getRep();
			}

		}, "Rep");
	}

	private void initPhenotypeDataGrid(FlagMap flagMap) {

		Column<TraitProxy, Boolean> checkBoxColumn = new Column<TraitProxy, Boolean>(
				new CheckboxCell(true, false)) {

			@Override
			public Boolean getValue(TraitProxy object) {
				return phenotypeDataGrid.getSelectionModel().isSelected(object);
			}
		};
		final CheckBoxHeader checkBoxHeader = new CheckBoxHeader();

		Header<String> selectedFooter = new Header<String>(new TextCell()) {
			@Override
			public String getValue() {
				MultiSelectionModel<TraitProxy> selectionModel = (MultiSelectionModel<TraitProxy>) phenotypeDataGrid
						.getSelectionModel();
				return selectionModel.getSelectedSet().size() + " ";
			}
		};

		checkBoxHeader.setUpdater(new ValueUpdater<Boolean>() {

			@Override
			public void update(Boolean value) {
				checkBoxHeader.reverseChecked();
				getUiHandlers().selectAllPhenotypeValues(value);

			}
		});
		phenotypeDataGrid.addColumn(checkBoxColumn, checkBoxHeader,
				selectedFooter);
		phenotypeDataGrid.setColumnWidth(checkBoxColumn, "50px");

		Column<TraitProxy, String> countryColumn = new Column<TraitProxy, String>(
				new FlagCell(flagMap)) {
			@Override
			public String getValue(TraitProxy object) {
				String icon = null;
				try {
					icon = object.getObsUnit().getStock().getPassport()
							.getCollection().getLocality().getOrigcty();
				} catch (NullPointerException e) {

				}
				return icon;
			}
		};
		phenotypeDataGrid.addColumn(countryColumn, "");
		phenotypeDataGrid.setColumnWidth(countryColumn, "150px");

		Column<TraitProxy, String> nameColumn = new Column<TraitProxy, String>(
				new HighlightCell(searchNameTerm)) {
			@Override
			public String getValue(TraitProxy object) {
				return object.getObsUnit().getName();
			}
		};
		phenotypeDataGrid.addColumn(nameColumn, "Name");
		phenotypeDataGrid.setColumnWidth(nameColumn, "100%");

		/*
		 * Column<TraitProxy, String> typeColumn = new Column<TraitProxy,
		 * String>( new TextCell()) {
		 * 
		 * @Override public String getValue(TraitProxy object) { return
		 * object.getStatisticType().getStatType(); } };
		 * phenotypeDataGrid.addColumn(typeColumn, "Type");
		 * phenotypeDataGrid.setColumnWidth(typeColumn, "50%");
		 */

		Column<TraitProxy, String> valueColumn = new Column<TraitProxy, String>(
				new TextCell()) {

			@Override
			public String getValue(TraitProxy object) {
				return object.getValue();
			}

		};
		phenotypeDataGrid.addColumn(valueColumn, "Value");
		phenotypeDataGrid.setColumnWidth(valueColumn, "50px");

	}

	@Override
	public HasData<ObsUnitProxy> getMissingGenotypesDisplay() {
		return missingGenotypesGrid;
	}

	@Override
	public void resetMissingGenotypesDataGrid() {
		missingGenotypesGrid.setVisibleRangeAndClearData(
				missingGenotypesGrid.getVisibleRange(), false);
		missingGenotypesGrid.setRowCount(1);
	}

	@Override
	public HasData<TraitProxy> getPhenotypesDisplay() {
		return phenotypeDataGrid;
	}

	@Override
	public void resetPhenotypeDataGrid() {
		phenotypeDataGrid.setVisibleRangeAndClearData(
				phenotypeDataGrid.getVisibleRange(), false);
		phenotypeDataGrid.setRowCount(1);
	}

	@Override
	public void showGeoChart(Multiset<String> geochartData) {
		DataTable data = DataTable.create();
		data.addColumn(ColumnType.STRING, "Country");
		data.addColumn(ColumnType.NUMBER, "Frequency");
		for (String cty : geochartData.elementSet()) {
			int i = data.addRow();
			data.setValue(i, 0, cty);
			data.setValue(i, 1, geochartData.count(cty));
		}
		drawGeoChart(data, createGeoChart(false));
	}

	@Override
	public TakesValue<StatisticTypeProxy> getStatisticTypeListBox() {
		return statisticTypeListBox;
	}
	
	@Override
	public SearchTerm getNameSearchTerm() {
		return searchNameTerm;
	}
	
	
	@Override
	public void setCountriesToFilter(Set<String> countries) {
		initCountryFilter();
		countryFilterLb.clear();
		countryFilterLb.addItem("");
		for (String country:countries) {
			countryFilterLb.addItem(country);
		}
		countryFilterLb.update();
		
		// because of bug http://code.google.com/p/gwtquery/issues/detail?id=145
		countryFilterLb.forceRedraw();
	}
	
	private void initCountryFilter() {
		if (countryFilterLb != null)
			return;
		
	}
}
