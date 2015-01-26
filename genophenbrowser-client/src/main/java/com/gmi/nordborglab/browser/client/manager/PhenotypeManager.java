package com.gmi.nordborglab.browser.client.manager;

import com.gmi.nordborglab.browser.shared.proxy.PhenotypePageProxy;
import com.gmi.nordborglab.browser.shared.proxy.PhenotypeProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitProxy;
import com.gmi.nordborglab.browser.shared.proxy.TraitStatsProxy;
import com.gmi.nordborglab.browser.shared.service.CustomRequestFactory;
import com.gmi.nordborglab.browser.shared.service.PhenotypeRequest;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.inject.Inject;
import com.google.web.bindery.requestfactory.shared.Receiver;

import java.util.List;

public class PhenotypeManager extends RequestFactoryManager<PhenotypeRequest> {

    public static String[] PATHS = {"statisticTypes", "unitOfMeasure", "userPermission", "traitOntologyTerm", "environOntologyTerm", "ownerUser"};

    @Inject
    public PhenotypeManager(CustomRequestFactory rf) {
        super(rf);
    }

    public void findAll(Receiver<PhenotypePageProxy> receiver, Long id, ConstEnums.TABLE_FILTER filter, String searchString, int start, int size) {
        rf.phenotypeRequest().findAll(id, filter, searchString, start, size).with("contents.traitOntologyTerm", "contents.environOntologyTerm", "contents.ownerUser", "facets").fire(receiver);
    }

    public void findAll(Receiver<PhenotypePageProxy> receiver, ConstEnums.TABLE_FILTER filter, String searchString, int start, int size) {
        rf.phenotypeRequest().findAll(filter, searchString, start, size).with("contents.traitOntologyTerm", "contents.environOntologyTerm", "contents.experiment", "contents.ownerUser", "facets").fire(receiver);
    }

    @Override
    public PhenotypeRequest getContext() {
        return rf.phenotypeRequest();
    }

    public void findAllByAcl(Receiver<List<PhenotypeProxy>> receiver, Long experimentId, int permission) {
        rf.phenotypeRequest().findPhenotypesByExperimentAndAcl(experimentId, permission).with(PATHS).fire(receiver);
    }

    public void findOne(Receiver<PhenotypeProxy> receiver, Long id) {
        rf.phenotypeRequest().findPhenotype(id).with(PATHS).fire(receiver);
    }

    public void findAllTraitValues(Receiver<List<TraitProxy>> receiver, Long phenotypeId, Long alleleAssayId, Long statisticTypeId) {
        rf.traitRequest().findAllTraitValues(phenotypeId, alleleAssayId, statisticTypeId).with("obsUnit.stock.passport.collection.locality", "statisticType").fire(receiver);
    }

    public void findAllTraitValuesByType(Long phenotypeId, Long statisticTypeId,
                                         Receiver<List<TraitProxy>> receiver) {
        rf.traitRequest().findAllTraitValuesByStatisticType(phenotypeId, statisticTypeId).with("obsUnit.stock.passport.collection.locality", "obsUnit.stock.passport.alleleAssays").fire(receiver);
    }

    public void findTraitStatsByStatisticType(Long phenotypeId, Long statisticTypeId, Receiver<List<TraitStatsProxy>> receiver) {
        rf.traitRequest().findTraitStatsByStatisticType(phenotypeId, statisticTypeId).fire(receiver);
    }

    public void findAllByOntology(Receiver<List<PhenotypeProxy>> receiver, String type, String acc, boolean checkChilds) {
        getContext().findAllByOntology(type, acc, checkChilds).with("traitOntologyTerm", "environOntologyTerm", "experiment").fire(receiver);
    }

    public void delete(Receiver<Void> receiver, PhenotypeProxy phenotype) {
        getContext().delete(phenotype).fire(receiver);
    }
}
