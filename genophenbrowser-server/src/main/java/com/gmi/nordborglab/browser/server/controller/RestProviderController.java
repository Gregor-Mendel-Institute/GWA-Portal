package com.gmi.nordborglab.browser.server.controller;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeValue;
import com.gmi.nordborglab.browser.server.data.annotation.FetchGeneInfoResult;
import com.gmi.nordborglab.browser.server.data.annotation.FetchGeneResult;
import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.GenomeStat;
import com.gmi.nordborglab.browser.server.data.annotation.GenomeStatsDataResultStatus;
import com.gmi.nordborglab.browser.server.data.annotation.GenomeStatsResultStatus;
import com.gmi.nordborglab.browser.server.data.annotation.Isoform;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListEnrichmentRepository;
import com.gmi.nordborglab.browser.server.rest.PhenotypeUploadData;
import com.gmi.nordborglab.browser.server.rest.StudyGWASData;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.browser.server.service.MetaAnalysisService;
import com.gmi.nordborglab.browser.server.service.TraitService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.render.JsonRenderer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@Controller
@RequestMapping("/provider")
public class RestProviderController {

    @Resource
    private CdvService cdvService;

    @Resource
    private TraitService traitService;

    @Resource
    private HelperService helperService;

    @Resource
    private MetaAnalysisService metaAnalysisService;

    @Resource
    private GWASDataService gwasDataService;

    @Resource
    private CandidateGeneListEnrichmentRepository candidateGeneListEnrichmentRepository;


    @Resource
    private GWASDataService gwasService;

    @Resource(name = "jbrowse")
    private AnnotationDataService annotationDataService;

    private static final Logger logger = LoggerFactory.getLogger(RestProviderController.class);


    @RequestMapping(method = RequestMethod.GET, value = "/study/{id}/pvalues")
    public
    @ResponseBody
    GWASData getPvalues(@PathVariable("id") Long id) {
        GWASData data = gwasDataService.getGWASDataByStudyId(id, null);
        data.setFilename(id + ".pvals");
        return data;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/study/{id}/pvalues", produces = {"application/hdf5"})
    public
    @ResponseBody
    FileSystemResource getPvaluesHDF5(@PathVariable("id") Long id) {
        return new FileSystemResource(gwasDataService.getHDF5StudyFile(id));
    }


    @RequestMapping(method = RequestMethod.GET, value = "/study/{id}/phenotypedata")
    public
    @ResponseBody
    PhenotypeData getStudyPhenotypeData(@PathVariable("id") Long id) {
        Study study = cdvService.findStudy(id);
        study = helperService.applyTransformation(study);
        List<PhenotypeValue> values = Lists.newArrayList(Iterables.transform(study.getTraits(), new Function<Trait, PhenotypeValue>() {
            @Nullable
            @Override
            public PhenotypeValue apply(@Nullable Trait input) {
                return new PhenotypeValue(input.getObsUnit().getStock().getPassport().getId(), input.getValue());
            }
        }));
        return new PhenotypeData(study.getTransformation().getName(), id.toString(), values);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/phenotype/{id}/phenotypedata/{statisticsTypeId}")
    public
    @ResponseBody
    String getPhenotypeData(@PathVariable("id") Long id, @PathVariable("statisticsTypeId") Long statisticsTypeId) {
        List<Trait> traits = traitService.findAllTraitValuesByStatisticType(id, statisticsTypeId);
        String csvData = null;
        StringBuilder builder = new StringBuilder();
        Joiner joiner = Joiner.on(",").useForNull("NA");
        builder.append(joiner.join("ecotypeId", id));
        for (Trait trait : traits) {
            builder.append("\n").append(joiner.join(trait.getObsUnit().getStock().getPassport().getId(), trait.getValue()));
        }
        csvData = builder.toString();
        return csvData;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/study/{id}/studygwasdata")
    public
    @ResponseBody
    StudyGWASData getStudyGWASData(@PathVariable("id") Long id) {
        Study study = cdvService.findStudy(id);
        String csvData = null;
        StringBuilder builder = new StringBuilder();
        Joiner joiner = Joiner.on(",").useForNull("NA");
        builder.append(joiner.join("ecotypeId", id));
        for (Trait trait : study.getTraits()) {
            builder.append("\n").append(joiner.join(trait.getObsUnit().getStock().getPassport().getId(), trait.getValue()));
        }
        csvData = builder.toString();
        //todo mapping between alleleassay and genotype
        StudyGWASData data = new StudyGWASData(csvData, study.getProtocol().getAnalysisMethod(), study.getAlleleAssay().getId().intValue());
        return data;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/study/{id}/upload")
    public
    @ResponseBody
    Long uploadStudyGWASResult(@PathVariable("id") Long id, @RequestParam("file") CommonsMultipartFile file) throws IOException {
        Long studyId = null;
        Study study = gwasService.uploadStudyGWASResult(id, file);
        studyId = study.getId();
        return studyId;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/candidategenelist/{id}/upload")
    public
    @ResponseBody
    String uploadCandidateGeneList(@PathVariable("id") Long candidateGeneListId, @RequestParam("file") CommonsMultipartFile file) {
        String result = null;
        try {
            byte[] csvData = IOUtils.toByteArray(file.getInputStream());
            List<String> geneIds = helperService.getGenesFromCanddiateGeneListUpload(csvData);
            List<Gene> genes = metaAnalysisService.addGenesToCandidateGeneList(candidateGeneListId, geneIds);
            result = String.format("%s/%s", geneIds.size(), genes.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return result;
    }


    @RequestMapping(method = RequestMethod.POST, value = "/phenotype/upload")
    public
    @ResponseBody
    PhenotypeUploadData uploadPhenotype(@RequestParam("file") CommonsMultipartFile file) {
        PhenotypeUploadData data = null;
        try {
            byte[] csvData = IOUtils.toByteArray(file.getInputStream());
            data = helperService.getPhenotypeUploadData(csvData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return data;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/gwas/upload")
    public
    @ResponseBody
    Long uploadGWASResult(@RequestParam("file") CommonsMultipartFile file) throws IOException {
        Long gwasResultId = null;
        GWASResult gwasResult = gwasService.uploadGWASResult(file);
        gwasResultId = gwasResult.getId();
        return gwasResultId;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/genes/getGenes")
    public
    @ResponseBody
    FetchGeneResult getGenes(@RequestParam("chromosome") String chromosome, @RequestParam("start") Long start, @RequestParam("end") Long end, @RequestParam("isFeatures") Boolean isFeatures) {
        List<Gene> genes = annotationDataService.getGenes(chromosome, start, end, isFeatures);
        FetchGeneResult result = new FetchGeneResult(genes);
        return result;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/genes/getGeneDescription")
    public
    @ResponseBody
    FetchGeneInfoResult getGenes(@RequestParam("gene") String gene) {
        Isoform isoform = annotationDataService.getGeneIsoform(gene);
        FetchGeneInfoResult result = new FetchGeneInfoResult(isoform.getDescription());
        return result;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/genes/getGenomeStatsList")
    public
    @ResponseBody
    GenomeStatsResultStatus getGenomeStatsList() {
        //TODO implment properly
        List<GenomeStat> genomeStats = Lists.newArrayList(new GenomeStat("genecount", "# Genes"));
        return new GenomeStatsResultStatus("OK", "", genomeStats);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/genes/getCustomGenomeStatsList")
    public
    @ResponseBody
    GenomeStatsResultStatus getCustomGenomeStatsList() {
        //TODO implment properly
        List<GenomeStat> genomeStats = Lists.newArrayList();
        return new GenomeStatsResultStatus("OK", "", genomeStats);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/genes/getGenomeStatsData")
    public
    @ResponseBody
    GenomeStatsDataResultStatus getGenomeStatsData(@RequestParam("stats") String stats, @RequestParam("chr") String chr) {
        //TODO implment properly
        DataTable dataTable = annotationDataService.getGenomeStatData(stats, chr);
        CharSequence json = JsonRenderer.renderDataTable(dataTable, true, false, true);
        return new GenomeStatsDataResultStatus("OK", "", json.toString());
    }


    @RequestMapping(method = RequestMethod.GET, value = "/candidategenelist/{id}/genes")
    public
    @ResponseBody
    List<Gene> getCandidateGeneListGenes(@PathVariable("id") Long id) {
        List<Gene> genes = metaAnalysisService.getGenesInCandidateGeneListEnrichment(id);
        return genes;
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "No Access")
    public void handleAccessDeniedException(AccessDeniedException ex, HttpServletResponse response) throws IOException {
        logger.error("AccessDeniedException", ex);
    }

}
