package com.gmi.nordborglab.browser.server.es;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.CandidateGeneListEnrichment;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListEnrichmentRepository;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListRepository;
import com.gmi.nordborglab.browser.server.repository.ExperimentRepository;
import com.gmi.nordborglab.browser.server.repository.GWASResultRepository;
import com.gmi.nordborglab.browser.server.repository.PassportRepository;
import com.gmi.nordborglab.browser.server.repository.PublicationRepository;
import com.gmi.nordborglab.browser.server.repository.StockRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TaxonomyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.gmi.nordborglab.browser.server.service.MetaAnalysisService;
import com.gmi.nordborglab.jpaontology.repository.TermRepository;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import ncsa.hdf.hdf5lib.exceptions.HDF5FileNotFoundException;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by uemit.seren on 1/28/15.
 *
 * To Run the Indexer: mvn -P indexer -Dtype=phenotype exec:java -Dspring.profiles.active=dev
 */
@Component
public class EsIndexerApp {
    private static final String CONFIG_PATH = "classpath:META-INF/applicationContext.xml";
    private static int BULK_SIZE = 1000;
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EsIndexerApp.class);

    @Resource
    protected EsIndexer esIndexer;

    @Resource
    protected GWASDataService gwasDataService;

    @Resource
    protected GWASResultRepository gwasResultRepository;

    @Resource
    protected CandidateGeneListRepository candidateGeneListRepository;

    @Resource
    protected CandidateGeneListEnrichmentRepository candidateGeneListEnrichmentRepository;

    @Resource
    protected ExperimentRepository experimentRepository;

    @Resource
    protected TraitUomRepository phenotypeRepository;

    @Resource
    protected StudyRepository studyRepository;

    @Resource
    protected PassportRepository passportRepository;

    @Resource
    protected TaxonomyRepository taxonomyRepository;

    @Resource
    protected StockRepository stockRepository;

    @Resource
    protected PublicationRepository publicationRepository;

    @Resource
    protected MetaAnalysisService metaAnalysisService;

    @Resource
    protected UserRepository userRepository;

    @Resource
    protected Client client;

    @Resource
    protected EsAclManager esAclManager;

    @Resource
    protected TermRepository termRepository;


    private Function ontologyFunc = new Function() {
        @Override
        public Object apply(Object input) {
            Preconditions.checkNotNull(input);
            Preconditions.checkArgument(input instanceof TraitUom);
            TraitUom traitUom = (TraitUom) input;
            if (traitUom.getToAccession() != null) {
                traitUom.setTraitOntologyTerm(termRepository.findByAcc(traitUom.getToAccession()));
            }
            if (traitUom.getEoAccession() != null) {
                traitUom.setEnvironOntologyTerm(termRepository.findByAcc(traitUom.getEoAccession()));
            }
            return input;
        }
    };

    public static void main(String[] args) {
        AbstractApplicationContext context = null;
        try {
            context = new ClassPathXmlApplicationContext(CONFIG_PATH);
            context.registerShutdownHook();
            final EsIndexerApp indexer =
                    context.getBean(EsIndexerApp.class);
            indexer.index(args);
        } catch (Exception e) {
            logger.error("Error", e);
        } finally {
            if (context != null) {
                context.close();
            }
        }
    }

    @Transactional(readOnly = true)
    public <T extends ESDocument> void index(String[] args) {
        Authentication auth =
                new UsernamePasswordAuthenticationToken("EsIndexerApp", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);
        if (args.length != 1)
            throw new RuntimeException("You have to specifiy the type to index");
        logger.info("Starting Bulk-indexing of type: " + args[0].toLowerCase());
        for (String type : Splitter.on(",").split(args[0])) {
            switch (type.toLowerCase()) {
                case "all":
                    indexEntities(gwasResultRepository);
                    indexEntities(candidateGeneListRepository);
                    indexEnrichments();
                    indexEntities(experimentRepository);
                    indexEntities(phenotypeRepository);
                    indexEntities(studyRepository);
                    indexEntities(passportRepository);
                    indexEntities(taxonomyRepository);
                    indexEntities(stockRepository);
                    indexEntities(userRepository);
                    indexEntities(publicationRepository);
                    break;
                case "gwasviewer":
                    indexEntities(gwasResultRepository);
                    break;
                case "candidategenelist":
                    indexEntities(candidateGeneListRepository);
                    break;
                case "candidategenelistenrichment":
                    indexEnrichments();
                    break;
                case "experiment":
                    indexEntities(experimentRepository);
                    break;
                case "phenotype":
                    indexEntities(phenotypeRepository, Optional.<Function<T, T>>of(ontologyFunc));
                    break;
                case "study":
                    indexEntities(studyRepository);
                    break;
                case "passport":
                    indexEntities(passportRepository);
                    break;
                case "taxonomy":
                    indexEntities(taxonomyRepository);
                    break;
                case "stock":
                    indexEntities(stockRepository);
                    break;
                case "user":
                    indexEntities(userRepository);
                    break;
                case "publication":
                    indexEntities(publicationRepository);
                    break;
                case "topsnps":
                    indexMetaAnalysis();
                    break;
                default:
                    throw new RuntimeException(String.format("Method %s not supported", args[0]));
            }
        }
        logger.info("Finished Bulk-indexing of type: " + args[0].toLowerCase());
    }


    private <T extends ESDocument> void indexEntities(JpaRepository<T, Long> repository) {
        indexEntities(repository, Optional.<Function<T, T>>absent());
    }

    private <T extends ESDocument> void indexEntities(JpaRepository<T, Long> repository, Optional<Function<T, T>> applyFunc) {
        int pageNumber = 0;
        try {
            Page<T> resultPage = null;
            long count = repository.count();
            do {
                resultPage = repository.findAll(new PageRequest(pageNumber, BULK_SIZE));
                List<T> documents = resultPage.getContent();
                if (applyFunc.isPresent()) {
                    for (T document : documents) {
                        applyFunc.get().apply(document);
                    }
                }
                BulkResponse response = esIndexer.bulkIndex(documents);
                if (response.hasFailures()) {
                    logger.error(response.buildFailureMessage());
                }
                pageNumber++;
                logger.info("Indexing " + pageNumber * BULK_SIZE + " of " + count + " indexed");

            } while (!resultPage.isLast());
        } catch (IOException e) {
            logger.error("Failed Bulk-indexing", e);
            e.printStackTrace();
        }
    }

    private void indexMetaAnalysis() {
        int pageNumber = 0;
        try {
            Page<Study> resultPage = null;
            long count = studyRepository.count();
            do {
                resultPage = studyRepository.findAll(new PageRequest(pageNumber, BULK_SIZE, Sort.Direction.ASC, "id"));
                for (Study study : resultPage.getContent()) {
                    logger.info("Checking if indexing is for study " + study.getName() + " (" + study.getId() + ") is necessary");
                    CountRequestBuilder request = client.prepareCount(esAclManager.getIndex())
                            .setTypes("meta_analysis_snps").setQuery(QueryBuilders.termQuery("studyid", study.getId()));
                    CountResponse response = request.execute().actionGet();
                    if (response.getCount() == 0) {
                        GWASData gwasData = null;
                        logger.info("Indexing Top SNPS");
                        try {
                            gwasData = gwasDataService.getGWASDataByStudyId(study.getId(), 1000D, true);
                            BulkResponse indexResponse = esIndexer.indexMetaAnalysisSnps(gwasData, study.getId(),study.getPhenotype().getExperiment().getId().toString(),true);
                            if (indexResponse.hasFailures()) {
                                logger.error(indexResponse.buildFailureMessage());
                            }
                        } catch (HDF5FileNotFoundException e) {
                            logger.warn("HDF5 File not found. Skipping");
                        }
                    } else {
                        logger.info("Skipping because SNPs are already indexed");
                    }
                }
                pageNumber++;

            } while (!resultPage.isLast());
        } catch (Exception e) {
            logger.error("Failed Bulk-indexing", e);
            e.printStackTrace();
        }


    }


    private void indexEnrichments() {
        int pageNumber = 0;
        try {
            Page<CandidateGeneListEnrichment> resultPage = null;
            long count = candidateGeneListEnrichmentRepository.count();
            do {
                resultPage = candidateGeneListEnrichmentRepository.findAll(new PageRequest(pageNumber, BULK_SIZE));
                for (CandidateGeneListEnrichment enrichment : resultPage.getContent()) {
                    metaAnalysisService.indexCandidateGeneListEnrichment(enrichment);
                }
                pageNumber++;
                logger.info("Indexing " + pageNumber * BULK_SIZE + " of " + count + " indexed");

            } while (!resultPage.isLast());
        } catch (Exception e) {
            logger.error("Failed Bulk-indexing", e);
            e.printStackTrace();
        }

    }
}
