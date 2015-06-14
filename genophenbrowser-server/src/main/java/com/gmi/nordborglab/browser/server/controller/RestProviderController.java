package com.gmi.nordborglab.browser.server.controller;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.annotation.FetchGeneInfoResult;
import com.gmi.nordborglab.browser.server.data.annotation.FetchGeneResult;
import com.gmi.nordborglab.browser.server.data.annotation.Gene;
import com.gmi.nordborglab.browser.server.data.annotation.GenomeStat;
import com.gmi.nordborglab.browser.server.data.annotation.GenomeStatsDataResultStatus;
import com.gmi.nordborglab.browser.server.data.annotation.GenomeStatsResultStatus;
import com.gmi.nordborglab.browser.server.data.annotation.Isoform;
import com.gmi.nordborglab.browser.server.data.isatab.IsaTabExporter;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.observation.Experiment;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.domain.util.GWASRuntimeInfo;
import com.gmi.nordborglab.browser.server.repository.CandidateGeneListEnrichmentRepository;
import com.gmi.nordborglab.browser.server.rest.ExperimentUploadData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeData;
import com.gmi.nordborglab.browser.server.rest.PhenotypeValue;
import com.gmi.nordborglab.browser.server.rest.StudyGWASData;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.gmi.nordborglab.browser.server.service.ExperimentService;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.gmi.nordborglab.browser.server.service.HelperService;
import com.gmi.nordborglab.browser.server.service.MetaAnalysisService;
import com.gmi.nordborglab.browser.server.service.TraitService;
import com.gmi.nordborglab.browser.server.service.TraitUomService;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.render.JsonRenderer;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Controller
@RequestMapping("/provider")
public class RestProviderController {

    @Resource
    private IsaTabExporter isaTabExporter;

    @Resource
    private CdvService cdvService;

    @Resource
    private ExperimentService experimentService;

    @Resource
    private TraitService traitService;

    @Resource
    private TraitUomService traitUomService;

    @Resource
    private HelperService helperService;

    @Resource
    private MetaAnalysisService metaAnalysisService;

    @Resource
    private GWASDataService gwasDataService;

    @Resource
    private CandidateGeneListEnrichmentRepository candidateGeneListEnrichmentRepository;


    @Resource(name = "jbrowse")
    private AnnotationDataService annotationDataService;

    private static final Logger logger = LoggerFactory.getLogger(RestProviderController.class);


    @RequestMapping(method = RequestMethod.GET, value = "/study/{id}/pvalues")
    public
    @ResponseBody
    GWASData getPvalues(@PathVariable("id") Long id) {
        GWASData data = gwasDataService.getGWASDataByStudyId(id, null, false);
        data.setFilename(id + ".pvals");
        return data;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/study/{id}/pvalues", produces = {"application/hdf5"})
    public
    @ResponseBody
    FileSystemResource getPvaluesHDF5(@PathVariable("id") Long id) {
        return new FileSystemResource(gwasDataService.getHDF5StudyFile(id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/study/{id}/{filename}", produces = {"application/zip"})
    public
    @ResponseBody
    FileSystemResource getISATab(@PathVariable("id") Long id) throws IOException {
        Experiment experiment = experimentService.findExperiment(id);
        FileSystemResource file = new FileSystemResource(isaTabExporter.save(experiment, true));
        return file;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/study/{id}/phenotypedata")
    public
    @ResponseBody
    PhenotypeData getStudyPhenotypeData(@PathVariable("id") Long id) {
        Study study = cdvService.findStudy(id);
        study = helperService.applyTransformation(study);
        List<PhenotypeValue> values = getPhenotypeDataFromTraits(study.getTraits());
        return new PhenotypeData(study.getTransformation().getName(), id.toString(), values);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/phenotype/{id}/phenotypedata")
    public
    @ResponseBody
    PhenotypeData getPhenotypeData(@PathVariable("id") Long id) {
        TraitUom traitUom = traitUomService.findPhenotype(id);
        List<PhenotypeValue> values = getPhenotypeDataFromTraits(traitUom.getTraits());
        return new PhenotypeData("", id.toString(), values);
    }


    private List<PhenotypeValue> getPhenotypeDataFromTraits(Set<Trait> traits) {

        // Group by PassportId
        Ordering<Multiset.Entry<?>> descendingOrder = new Ordering<Multiset.Entry<?>>() {
            @Override
            public int compare(Multiset.Entry<?> left, Multiset.Entry<?> right) {
                return Ints.compare(left.getCount(), right.getCount());
            }
        }.reverse();


        // sort by number of trait values
        ImmutableListMultimap<Long, Trait> grouped = Multimaps.index(traits, new Function<Trait, Long>() {
            @Nullable
            @Override
            public Long apply(Trait input) {
                Preconditions.checkNotNull(input);
                return input.getObsUnit().getStock().getPassport().getId();
            }
        });
        ImmutableMultimap.Builder<Long, Trait> builder = ImmutableMultimap.builder();
        for (Multiset.Entry<Long> entry : descendingOrder.sortedCopy(grouped.keys().entrySet())) {
            builder.putAll(entry.getElement(), grouped.get(entry.getElement()));
        }
        ImmutableMultimap<Long, Trait> groupedAndSorted = builder.build();
        List<PhenotypeValue> values = Lists.newArrayList();
        for (Map.Entry<Long, Collection<Trait>> entry : groupedAndSorted.asMap().entrySet()) {
            Long passportId = entry.getKey();
            //sort by statisticType
            Map<String, String> phenValues = Maps.transformValues(
                    Maps.uniqueIndex(
                            Ordering.natural().onResultOf(
                                    new Function<Trait, Long>() {
                                        @Nullable
                                        @Override
                                        public Long apply(Trait input) {
                                            Preconditions.checkNotNull(input);
                                            return input.getStatisticType().getId();
                                        }
                                    }
                            ).immutableSortedCopy(entry.getValue()),
                            new Function<Trait, String>() {
                                @Nullable
                                @Override
                                public String apply(Trait input) {
                                    Preconditions.checkNotNull(input);
                                    return input.getStatisticType().getStatType();
                                }
                            }
                    ),
                    new Function<Trait, String>() {
                        @Nullable
                        @Override
                        public String apply(Trait input) {
                            Preconditions.checkNotNull(input);
                            return input.getValue();
                        }
                    }
            );
            values.add(new PhenotypeValue(passportId, phenValues));
        }
        return values;
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
        final Study study = cdvService.findStudy(id);
        String transformation = study.getTransformation().getName();
        String csvData = null;
        StringBuilder builder = new StringBuilder();
        Joiner joiner = Joiner.on(",").useForNull("NA");
        builder.append(joiner.join("ecotypeId", id));
        for (Trait trait : study.getTraits()) {
            builder.append("\n").append(joiner.join(trait.getObsUnit().getStock().getPassport().getId(), trait.getValue()));
        }
        csvData = builder.toString();
        //todo mapping between alleleassay and genotype
        //calculate runtime

        GWASRuntimeInfo info = Iterables.find(study.getProtocol().getGwasRuntimeInfos(), new Predicate<GWASRuntimeInfo>() {
            @Override
            public boolean apply(GWASRuntimeInfo input) {
                Preconditions.checkNotNull(input);
                return input.getAlleleAssay().equals(study.getAlleleAssay());
            }
        });
        int sampleSize = study.getTraits().size();
        long runtime = Math.round(info.getCoefficient1() * Math.pow(sampleSize, 2) + info.getCoefficient2() * sampleSize + info.getCoefficient3());
        StudyGWASData data = new StudyGWASData(csvData, study.getProtocol().getAnalysisMethod(), study.getAlleleAssay().getId().intValue(), transformation, runtime);
        return data;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/study/{id}/upload")
    public
    @ResponseBody
    Long uploadStudyGWASResult(@PathVariable("id") Long id, @RequestParam("file") CommonsMultipartFile file) throws IOException {
        Long studyId = null;
        Study study = gwasDataService.uploadStudyGWASResult(id, file);
        studyId = study.getId();
        return studyId;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/study/{id}/store")
    public
    @ResponseBody
    Long storeStudyGWASResult(@PathVariable("id") Long id, @RequestParam("file") CommonsMultipartFile file) throws IOException {
        Long studyId = null;
        Study study = gwasDataService.storeGWASResult(id, file);
        studyId = study.getId();
        return studyId;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/isatab/upload")
    public
    @ResponseBody
    ExperimentUploadData uploadISATabArchive(@RequestParam("file") CommonsMultipartFile file) throws IOException {
        ExperimentUploadData data = null;
        try {
            byte[] isaTabData = IOUtils.toByteArray(file.getInputStream());
            data = helperService.getExperimentUploadDataFromIsaTab(isaTabData);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
        return data;
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
    ExperimentUploadData uploadPhenotype(@RequestParam("file") CommonsMultipartFile file) throws IOException {
        ExperimentUploadData data = null;
        byte[] csvData = IOUtils.toByteArray(file.getInputStream());
        data = helperService.getExperimentUploadDataFromCsv(csvData);
        return data;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/gwas/upload")
    public
    @ResponseBody
    Long uploadGWASResult(@RequestParam("file") CommonsMultipartFile file) throws IOException {
        Long gwasResultId = null;
        GWASResult gwasResult = gwasDataService.uploadGWASResult(file);
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

    @RequestMapping(method = RequestMethod.GET, value = "/gwasviewer/{id}/plots")
    public void getGWASViewerPlot(HttpServletResponse response, HttpServletRequest request,
                                  @PathVariable("id") Long id,
                                  @RequestParam(value = "chr", required = false) String chr,
                                  @RequestParam(value = "mac", required = false, defaultValue = "15") Integer minMac,
                                  @RequestParam(value = "format", required = false, defaultValue = "png") String format) {

        String filename = gwasDataService.getGWASViewerPlotFile(id, chr, minMac, format);
        processPlotFileRequest(response, request, filename);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/gwas/{id}/plots")
    public void getStudyPlot(HttpServletResponse response, HttpServletRequest request,
                             @PathVariable("id") Long id,
                             @RequestParam(value = "chr", required = false) String chr,
                             @RequestParam(value = "mac", required = false, defaultValue = "15") Integer minMac,
                             @RequestParam(value = "format", required = false, defaultValue = "png") String format) {
        String filename = gwasDataService.getStudyPlotFile(id, chr, minMac, format);
        processPlotFileRequest(response, request, filename);
    }

    private void processPlotFileRequest(HttpServletResponse response, HttpServletRequest request, String filename) {
        File file = new File(filename);
        try {
            String mimeType = request.getServletContext().getMimeType(filename);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }
            response.setContentType(mimeType);
            response.setContentLength((int) file.length());
            response.setHeader("Content-disposition", "attachment; filename=" + file.getName());
            OutputStream out = response.getOutputStream();
            FileInputStream in = new FileInputStream(file);
            FileCopyUtils.copy(in, out);
            in.close();
        } catch (IOException ex) {
            logger.error("Error downloading plots", ex);
            throw new RuntimeException(ex.getMessage());
        } finally {
            if (file.isFile()) {
                file.delete();
            }
        }
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "No Access")
    public void handleAccessDeniedException(AccessDeniedException ex, HttpServletResponse response) throws IOException {
        logger.error("AccessDeniedException", ex);
    }

}
