package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;
import com.gmi.nordborglab.browser.server.data.SNPGWASInfo;
import com.gmi.nordborglab.browser.server.data.csv.CSVGWASReader;
import com.gmi.nordborglab.browser.server.data.es.ESFacet;
import com.gmi.nordborglab.browser.server.data.hdf5.HDF5GWASReader;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.pages.GWASResultPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.es.EsIndexer;
import com.gmi.nordborglab.browser.server.es.EsSearcher;
import com.gmi.nordborglab.browser.server.repository.GWASResultRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.EsAclManager;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.gmi.nordborglab.browser.server.tasks.SubmitAnalysisTask;
import com.gmi.nordborglab.browser.shared.util.ConstEnums;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import ncsa.hdf.hdf5lib.exceptions.HDF5FileNotFoundException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.elasticsearch.action.search.SearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HDF5GWASDataService implements GWASDataService {

    private
    @Value("${GWAS.study.folder}")
    String GWAS_STUDY_FOLDER;
    private
    @Value("${GWAS.viewer.dest_folder}")
    String GWAS_VIEWER_FOLDER;

    @Value("${java.io.tmpdir}")
    private String TEMP_FOLDER;

    @Resource
    protected SubmitAnalysisTask submitAnalysisTask;

    @Resource
    protected StudyRepository studyRepository;

    @Resource
    protected TraitUomRepository traitUomRepository;

    @Resource
    protected GWASResultRepository gwasResultRepository;

    @Resource
    protected UserRepository userRepository;

    @Resource
    protected AclManager aclManager;

    @Resource
    protected EsAclManager esAclManager;

    @Resource
    protected RoleHierarchy roleHierarchy;

    @Resource(name = "ES")
    protected AnnotationDataService annotationDataService;

    @Resource
    protected EsSearcher esSearcher;

    @Resource
    protected EsIndexer esIndexer;

    private static final ImmutableSet<String> SUPPORTED_PLOT_FORMATS = ImmutableSet.of("png", "pdf");
    private static final ImmutableSet<String> SUPPORTED_CHROMOSOMES = ImmutableSet.of("chr1", "chr2", "chr3", "chr4", "chr5");

    private static List<String> csvMimeTypes = Lists.newArrayList("text/csv", "application/csv", "application/excel", "application/vnd.ms-excel", "application/vnd.msexcel", "text/comma-separated-values");


    @Override
    public GWASData getGWASDataByStudyId(Long studyId, Double limit,boolean addAnnotation) {
        GWASReader gwasReader = new HDF5GWASReader(GWAS_STUDY_FOLDER);
        GWASData gwasData = gwasReader.readAll(studyId + ".hdf5", limit);
        gwasData.sortByPosition();
        if (addAnnotation) {
            gwasData = addAnnotation(gwasData);
        }
        return gwasData;
    }

    @Override
    @Transactional(noRollbackFor = {IOException.class, HDF5FileNotFoundException.class})
    public GWASData getGWASDataByStudyIdForIndexer(long studyId) {
        return getGWASDataByStudyId(studyId,1000D,true);
    }

    @Override
    public SNPGWASInfo getSNPGWASInfoByStudyId(Long studyId, Integer chromosome, Integer position) {
        GWASReader gwasReader = new HDF5GWASReader(GWAS_STUDY_FOLDER);
        return gwasReader.readSingle(studyId + ".hdf5", chromosome, position);
    }

    @Override
    public GWASData getGWASDataByStudyId(Long studyId) {
        return getGWASDataByStudyId(studyId, 2500D,true);
    }

    @Override
    public GWASData getGWASDataByViewerId(Long gwasResultId) {
        GWASReader gwasReader = new HDF5GWASReader(GWAS_VIEWER_FOLDER);
        GWASData gwasData = gwasReader.readAll(gwasResultId + ".hdf5", 2500D);
        gwasData.sortByPosition();
        gwasData = addAnnotation(gwasData);
        return gwasData;
    }

    @Override
    @Transactional(readOnly = false)
    public GWASResult uploadGWASResult(CommonsMultipartFile file) throws IOException {
        GWASResult gwasResult = new GWASResult();
        gwasResult.setName(file.getOriginalFilename());
        HDF5GWASReader gwasWriter = new HDF5GWASReader("");
        try {
            Map<String, ChrGWAData> data = getGWASDataFromUploadFile(file);
            gwasResult = updateStats(gwasResult, data);
            gwasResult = gwasResultRepository.save(gwasResult);
            CumulativePermission fullPermission = new CumulativePermission();
            fullPermission.set(CustomPermission.ADMINISTRATION).set(CustomPermission.EDIT).set(CustomPermission.READ);
            aclManager.addPermission(gwasResult, new GrantedAuthoritySid("ROLE_ANONYMOUS"), new CustomPermission(0), null);
            aclManager.addPermission(gwasResult, new PrincipalSid(SecurityUtil.getUsername()), fullPermission, null);
            aclManager.addPermission(gwasResult, new GrantedAuthoritySid("ROLE_ADMIN"), fullPermission, null);
            File targetFile = new File(GWAS_VIEWER_FOLDER + gwasResult.getId() + ".hdf5");
            gwasWriter.saveGWASDataToFile(data, targetFile);
            indexGWASResult(gwasResult);

        } catch (Exception e) {
            throw new IOException(e);
        } finally {

        }
        return gwasResult;
    }

    @Override
    public GWASResultPage findAllGWASResults(ConstEnums.TABLE_FILTER filter, String searchString, int start, int size) {
        SearchResponse response = esSearcher.search(filter, true, new String[]{"name", "name.partial", "comments", "type", "owner.name"}, searchString, "gwasviewer", start, size);
        Iterable<Long> idsToFetch = EsSearcher.getIdsFromResponse(response);
        List<GWASResult> results = gwasResultRepository.findAll(idsToFetch);
        Iterable<GWASResult> gwasResultsFiltered = aclManager.filterByAcl(results, Lists.newArrayList(CustomPermission.READ));
        //extract facets
        List<ESFacet> facets = EsSearcher.getAggregations(response);
        List<GWASResult> gwasResults = ImmutableList.copyOf(gwasResultsFiltered);
        aclManager.setPermissionAndOwners(gwasResults);
        return new GWASResultPage(gwasResults, new PageRequest(start, size), response.getHits().getTotalHits(), facets);
    }

    private void deleteFromIndex(GWASResult gwasResult) {
        esIndexer.delete(gwasResult);
    }

    private void indexGWASResult(GWASResult gwasResult) {
        try {
            esIndexer.index(gwasResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GWASResult findOneGWASResult(Long id) {
        return gwasResultRepository.findOne(id);
    }


    @Override
    @Transactional(readOnly = false)
    public List<GWASResult> delete(GWASResult gwasResult) {
        File file = new File(GWAS_VIEWER_FOLDER + gwasResult.getId() + ".hdf5");
        if (file.exists()) {
            file.delete();
        }
        gwasResultRepository.delete(gwasResult);
        aclManager.deletePermissions(gwasResult, true);
        deleteFromIndex(gwasResult);
        List<GWASResult> gwasResults = Lists.newArrayList();
        return gwasResults;
    }

    @Override
    @Transactional(readOnly = false)
    public GWASResult save(GWASResult gwasResult) {
        gwasResultRepository.save(gwasResult);
        indexGWASResult(gwasResult);
        return gwasResult;
    }

    @Override
    @Transactional(readOnly = false)
    public Study uploadStudyGWASResult(Long studyId, CommonsMultipartFile file) throws IOException {
        Study study = studyRepository.findOne(studyId);
        if (study.getTraits().size() == 0)
            throw new RuntimeException("Study must have phenotypes assigned");
        TraitUom trait = Iterables.get(study.getTraits(), 0).getTraitUom();
        HDF5GWASReader gwasWriter = new HDF5GWASReader("");
        try {
            Map<String, ChrGWAData> data = getGWASDataFromUploadFile(file);
            File targetFile = new File(GWAS_STUDY_FOLDER + studyId + ".hdf5");
            gwasWriter.saveGWASDataToFile(data, targetFile);
            StudyJob studyJob = study.getJob();
            if (studyJob == null) {
                studyJob = new StudyJob();
                study.setJob(studyJob);
                studyJob.setCreateDate(new Date());
            }
            studyJob.setProgress(100);
            studyJob.setStatus("Finished");
            studyJob.setTask("Uploaded");
            studyJob.setModificationDate(new Date());
            studyRepository.save(study);
            submitAnalysisTask.submitAnalysisForTopSNPs(study);

        } catch (Exception e) {
            throw new IOException(e.getMessage());
        } finally {

        }
        return study;
    }

    @Override
    public void deleteStudyFile(Long id) {
        File file = new File(GWAS_STUDY_FOLDER + id + ".hdf5");
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public String getHDF5StudyFile(Long id) {
        return GWAS_STUDY_FOLDER + id + ".hdf5";
    }


    @Override
    @Transactional(readOnly = false)
    public Study storeGWASResult(Long studyId, CommonsMultipartFile file) throws IOException{
        Study study = studyRepository.findOne(studyId);
        File targetFile = new File(GWAS_STUDY_FOLDER + studyId + ".hdf5");
        try {
            file.transferTo(targetFile);
            StudyJob studyJob = study.getJob();
            if (studyJob == null) {
                studyJob = new StudyJob();
                studyJob.setStatus("Running");
                study.setJob(studyJob);
                studyJob.setCreateDate(new Date());
            }
            studyJob.setProgress(90);
            studyJob.setTask("Finished...Cleaning up");
            studyJob.setModificationDate(new Date());
            studyRepository.save(study);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        } finally {

        }
        return study;
    }


    private GWASResult updateStats(GWASResult gwasResult, Map<String, ChrGWAData> data) {
        float maxScore = 0;
        int numberOfSNPs = 0;
        for (Map.Entry<String, ChrGWAData> entry : data.entrySet()) {
            ChrGWAData chrGWAData = entry.getValue();
            if (maxScore < chrGWAData.getPvalues()[0]) {
                maxScore = chrGWAData.getPvalues()[0];
            }
            numberOfSNPs = numberOfSNPs + chrGWAData.getPositions().length;
        }
        gwasResult.setMaxScore(maxScore);
        gwasResult.setNumberOfSNPs(numberOfSNPs);
        return gwasResult;
    }


    private GWASData addAnnotation(GWASData data) {
        for (Map.Entry<String, ChrGWAData> dataEntry : data.getChrGWASData().entrySet()) {
            ChrGWAData chrData = dataEntry.getValue();
            chrData.setSNPInfos(annotationDataService.getSNPAnnotations(dataEntry.getKey().toLowerCase(), chrData.getPositions()));
        }
        return data;
    }

    private Map<String, ChrGWAData> getGWASDataFromUploadFile(CommonsMultipartFile file) throws Exception {
        Map<String, ChrGWAData> data = null;
        if (file.isEmpty())
            throw new RuntimeException("File is empty");
        if (!file.getContentType().trim().equalsIgnoreCase("application/x-hdf") && !(csvMimeTypes.contains(file.getContentType().trim()))) {
            throw new IOException("Content Type " + file.getContentType() + " not supported");
        }
        File tempFile = new File(TEMP_FOLDER + "/" + UUID.randomUUID().toString());
        try {
            file.transferTo(tempFile);
            GWASReader gwasReader = null;
            if (file.getContentType().trim().equalsIgnoreCase("application/x-hdf")) {
                gwasReader = new HDF5GWASReader("");
            } else {
                gwasReader = new CSVGWASReader();
            }
            gwasReader.isValidGWASFile(tempFile);
            data = gwasReader.parseGWASDataFromFile(tempFile);
        } catch (Exception e) {
            throw e;
        } finally {
            if (tempFile != null)
                tempFile.delete();
        }
        return data;
    }

    private String generateGWASPlots(String gwasFile, String gwasFolder, String name, String chr, Integer minMac, String format) {
        if (!SUPPORTED_PLOT_FORMATS.contains(format)) {
            throw new RuntimeException(String.format("Format %s not supported", format));
        }
        String chrFlag = "";
        String chrNamePart = "";
        //FIXME workaround because mounting gwasFolder directly sometimes causes problems but usually works when mounting the parent folder
        File folder = new File(gwasFolder).getAbsoluteFile();
        String parentFolder = folder.getParent();
        String targetFolder = folder.getName();

        String plotterPrgm = String.format("docker run --rm -v %s:/GWAS_DATA:ro -v %s/PLOT_OUTPUT:/PLOT_OUTPUT pygwas_plotter", parentFolder, TEMP_FOLDER);
        if (chr != null && !chr.isEmpty()) {
            if (!SUPPORTED_CHROMOSOMES.contains(chr)) {
                throw new RuntimeException(String.format("Chromosome %s invalid", chr));
            }
            chrFlag = String.format("-c %s", chr);
            chrNamePart = String.format("_%s", chr);
        }
        String outputFile = String.format("%s%s_mac%s.%s", name, chrNamePart, minMac, format);
        String plotterCmd = String.format("%s /GWAS_DATA/%s/%s -m %d -o /PLOT_OUTPUT/%s %s", plotterPrgm, targetFolder, gwasFile, minMac, outputFile, chrFlag);
        CommandLine cmdLine = CommandLine.parse(plotterCmd);
        DefaultExecutor executor = new DefaultExecutor();
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);
        try {
            int exitValue = executor.execute(cmdLine);
            if (exitValue != 0)
                throw new RuntimeException("Error creating plots");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        return String.format("%s/PLOT_OUTPUT/%s", TEMP_FOLDER, outputFile);
    }

    @Override
    public String getStudyPlotFile(Long id, String chr, Integer minMac, String format) {
        String hdf5file = String.format("%s.hdf5", id);
        return generateGWASPlots(hdf5file, GWAS_STUDY_FOLDER, id.toString(), chr, minMac, format);
    }

    @Override
    public String getGWASViewerPlotFile(Long id, String chr, Integer minMac, String format) {
        String hdf5file = String.format("%s.hdf5", id);
        return generateGWASPlots(hdf5file, GWAS_VIEWER_FOLDER, id.toString(), chr, minMac, format);
    }

}
