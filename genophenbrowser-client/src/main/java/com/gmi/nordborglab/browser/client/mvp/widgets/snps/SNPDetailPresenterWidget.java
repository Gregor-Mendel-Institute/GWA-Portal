
package com.gmi.nordborglab.browser.client.mvp.widgets.snps;

import com.gmi.nordborglab.browser.client.dto.SNPAllele;
import com.gmi.nordborglab.browser.client.events.LoadingIndicatorEvent;
import com.gmi.nordborglab.browser.client.manager.CdvManager;
import com.gmi.nordborglab.browser.client.place.GoogleAnalyticsManager;
import com.gmi.nordborglab.browser.client.util.Statistics;
import com.gmi.nordborglab.browser.shared.proxy.PassportProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPAlleleInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPGWASInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.SNPInfoProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SNPDetailPresenterWidget extends PresenterWidget<SNPDetailPresenterWidget.MyView> implements SNPDetailUiHandlers {

    public interface MyView extends View, HasUiHandlers<SNPDetailUiHandlers> {

        HasData<SNPAllele> getSNPAlleleDisplay();

        void scheduledLayout();

        void setExplorerData(List<SNPAllele> snpAllelSNPes);

        void displaySNPInfo(SNPGWASInfoProxy response);

        void setPhenotypeRange(Range<Double> valueRange);

        void displayAlleInfo(SNPInfoProxy snpAnnot);

        void setList(List<SNPAllele> snpAlleles);

        void showPhenotypeColumns(boolean show);
    }


    protected Integer chr;
    protected Integer position;
    protected ImmutableMap<Long, String> passportId2Phenotype;
    protected ImmutableMap<Long, PassportProxy> passportId2Passport;
    protected List<Long> passportIds;
    protected Long alleleAssayId;


    protected final PlaceManager placeManager;
    protected boolean fireLoadEvent = false;
    protected final CdvManager cdvManager;
    protected ListDataProvider<SNPAllele> dataProvider = new ListDataProvider<>();
    protected final GoogleAnalyticsManager analyticsManager;


    @Inject
    SNPDetailPresenterWidget(EventBus eventBus, MyView view, PlaceManager placeManager, CdvManager cdvManager,
                             final GoogleAnalyticsManager analyticsManager) {
        super(eventBus, view);
        this.placeManager = placeManager;
        this.cdvManager = cdvManager;
        this.analyticsManager = analyticsManager;
        getView().setUiHandlers(this);
    }

    @Override
    protected void onBind() {
        dataProvider.addDataDisplay(getView().getSNPAlleleDisplay());
    }

    @Override
    protected void onReveal() {
        super.onReveal();
    }

    @Override
    protected void onReset() {
        super.onReset();
    }

    protected void fetchData() {
        if (chr == null || position == null || alleleAssayId == null) {
            clearData();
            return;
        }
        fireEvent(new LoadingIndicatorEvent(true));
        getView().getSNPAlleleDisplay().setRowCount(0, false);
        analyticsManager.startTimingEvent("SNPDetail", "Load");
        cdvManager.requestFactory().annotationDataRequest().getSNPAlleleInfo(alleleAssayId, chr, position, passportIds, passportId2Phenotype == null).with("passports.collection.locality").fire(new Receiver<SNPAlleleInfoProxy>() {
            @Override
            public void onSuccess(SNPAlleleInfoProxy response) {
                analyticsManager.endTimingEvent("SNPDetail", "Load", "SUCCESS");
                fireEvent(new LoadingIndicatorEvent(false));
                displayData(response);
            }
        });
    }

    private void displayData(SNPAlleleInfoProxy alleleInfo) {
        List<SNPAllele> snpAlleles = getListFromAlleleInfo(alleleInfo);
        double maxValue = 0;
        double minValue = 0;
        FluentIterable<Double> phenotypeValues = FluentIterable.from(snpAlleles).transform(new Function<SNPAllele, Double>() {
            @Nullable
            @Override
            public Double apply(@Nullable SNPAllele input) {
                Double value = null;
                try {
                    value = Double.valueOf(input.getPhenotype());
                } catch (Exception e) {
                }
                return value;
            }
        }).filter(Predicates.notNull());
        if (phenotypeValues.size() > 0) {
            minValue = Ordering.natural().min(phenotypeValues);
            maxValue = Ordering.natural().max(phenotypeValues);
        }
        getView().setPhenotypeRange(Range.closed(minValue, maxValue));
        dataProvider.setList(snpAlleles);
        getView().setList(dataProvider.getList());
        getView().displayAlleInfo(alleleInfo.getSnpInfo());
        getView().setExplorerData(snpAlleles);
        getView().scheduledLayout();
    }

    private List<SNPAllele> getListFromAlleleInfo(final SNPAlleleInfoProxy alleleInfo) {
        List<SNPAllele> allelesInfo = Lists.newArrayList();
        SNPInfoProxy annot = alleleInfo.getSnpInfo();
        for (int i = 0; i < alleleInfo.getAlleles().size(); i++) {
            PassportProxy passport;
            if (passportId2Passport != null) {
                passport = passportId2Passport.get(passportIds.get(i));
            } else {
                passport = alleleInfo.getPassports().get(i);
            }
            String allele = alleleInfo.getAlleles().get(i) == 1 ? annot.getAlt() : annot.getRef();
            String value = null;
            if (passportId2Phenotype != null && passport != null) {
                value = passportId2Phenotype.get(passport.getId());
            }
            allelesInfo.add(new SNPAllele(i, passport, allele, value));
        }
        return allelesInfo;
    }

    private void clearData() {
        getView().getSNPAlleleDisplay().setRowCount(0, false);
        getView().displayAlleInfo(null);
        getView().displaySNPInfo(null);
    }

    public void setData(Integer chr, Integer position, Long alleleAssayId, Set<TraitProxy> traits) {
        this.chr = chr;
        this.position = position;
        this.alleleAssayId = alleleAssayId;
        passportId2Phenotype = null;
        passportIds = null;
        passportId2Passport = null;
        if (traits != null) {

            Function<TraitProxy, Long> trait2PassportIdFunc = new Function<TraitProxy, Long>() {
                @Nullable
                @Override
                public Long apply(@Nullable TraitProxy input) {
                    return input.getObsUnit().getStock().getPassport().getId();
                }
            };

            Function<Collection<TraitProxy>, String> traits2StringValueFunc = new Function<Collection<TraitProxy>, String>() {
                @Nullable
                @Override
                public String apply(@Nullable Collection<TraitProxy> input) {
                    if (input == null || input.size() == 0)
                        return "";
                    FluentIterable<Double> values = FluentIterable.from(input)
                            .transform(Statistics.traitToDouble)
                            .filter(Predicates.notNull());
                    if (values.isEmpty())
                        return "";
                    return String.valueOf(calculateMean(values.toList()));
                }
            };
            passportId2Phenotype = ImmutableMap.copyOf(Maps.filterValues(
                            Maps.transformValues(
                                    Multimaps.index(traits, trait2PassportIdFunc).asMap(), traits2StringValueFunc
                            ), Predicates.notNull())
            );
            HashMap<Long, PassportProxy> passportMap = new HashMap<>();
            for (TraitProxy trait : traits) {
                if (!passportMap.containsKey(trait.getObsUnit().getStock().getPassport().getId())) {
                    passportMap.put(trait.getObsUnit().getStock().getPassport().getId(), trait.getObsUnit().getStock().getPassport());
                }
            }
            passportId2Passport = ImmutableMap.copyOf(passportMap);
            passportIds = ImmutableList.copyOf(passportId2Passport.keySet());
        }
        getView().showPhenotypeColumns(passportId2Phenotype != null);
        fetchData();
    }

    private double calculateMean(List<Double> values) {
        double total = 0;
        for (double element : values) {
            total += element;
        }
        return total / values.size();
    }


    public void setSNPGWASInfo(SNPGWASInfoProxy gwasInfo) {
        getView().displaySNPInfo(gwasInfo);
    }

}

