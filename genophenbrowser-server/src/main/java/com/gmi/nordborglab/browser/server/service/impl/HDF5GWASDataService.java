package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.ChrGWAData;
import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;
import com.gmi.nordborglab.browser.server.data.csv.CSVGWASReader;
import com.gmi.nordborglab.browser.server.data.hdf5.HDF5GWASReader;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.repository.GWASResultRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.security.AclManager;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.AnnotationDataService;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.gmi.nordborglab.browser.server.tasks.SubmitAnalysisTask;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.CumulativePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.*;
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
    protected RoleHierarchy roleHierarchy;

    @Resource(name = "ES")
    protected AnnotationDataService annotationDataService;

    protected GWASReader gwasReader;

    private static List<String> csvMimeTypes = Lists.newArrayList("text/csv", "application/csv", "application/excel", "application/vnd.ms-excel", "application/vnd.msexcel", "text/comma-separated-values");

    @Override
    public GWASData getGWASDataByStudyId(Long studyId) {
        TraitUom trait = traitUomRepository.findByStudyId(studyId);
        GWASReader gwasReader = new HDF5GWASReader(GWAS_STUDY_FOLDER);
        GWASData gwasData = gwasReader.readAll(studyId + ".hdf5", 2500D);
        gwasData.sortByPosition();
        gwasData = addAnnotation(gwasData);
        return gwasData;
    }

    @Override
    public GWASData getGWASDataByViewerId(Long gwasResultId) {
        GWASResult gwasResult = gwasResultRepository.findOne(gwasResultId);
        GWASReader gwasReader = new HDF5GWASReader(GWAS_VIEWER_FOLDER);
        GWASData gwasData = gwasReader.readAll(gwasResultId + ".hdf5", 2500D);
        gwasData.sortByPosition();
        gwasData = addAnnotation(gwasData);
        return gwasData;
    }

    @Override
    @Transactional(readOnly = false)
    public GWASResult uploadGWASResult(CommonsMultipartFile file) throws IOException {

        AppUser appUser = userRepository.findOne(Long.parseLong(SecurityUtil.getUsername()));
        GWASResult gwasResult = new GWASResult();
        gwasResult.setName(file.getOriginalFilename());
        gwasResult.setAppUser(appUser);
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

        } catch (Exception e) {
            throw new IOException(e.getMessage());
        } finally {

        }
        return gwasResult;
    }

    @Override
    public List<GWASResult> findAllGWASResults() {
        //List<GWASResult> gwasResults = gwasResultRepository.findAllByAppUserUsername(SecurityUtil.getUsername());
        List<GWASResult> gwasResultsToFilter = gwasResultRepository.findAll();
        final ImmutableList<Permission> permissions = ImmutableList.of(CustomPermission.READ, CustomPermission.EDIT, CustomPermission.ADMINISTRATION);
        Sid sid = new PrincipalSid(SecurityUtil.getAuthentication());
        final List<Sid> authorities = ImmutableList.of(sid);
        FluentIterable<GWASResult> gwasResults = aclManager.filterByAcl(gwasResultsToFilter, permissions, authorities);
        return gwasResults.toList();
    }

    @Override
    public GWASResult findOneGWASResult(Long id) {
        return gwasResultRepository.findOne(id);
    }


    @Override
    @Transactional(readOnly = false)
    public List<GWASResult> delete(GWASResult gwasResult) {

        aclManager.deleteAcl(gwasResult);
        File file = new File(GWAS_VIEWER_FOLDER + gwasResult.getId() + ".hdf5");
        if (file.exists()) {
            file.delete();
        }
        gwasResultRepository.delete(gwasResult);
        List<GWASResult> gwasResults = gwasResultRepository.findAllByAppUserUsername(SecurityUtil.getUsername());
        return gwasResults;
    }

    @Override
    @Transactional(readOnly = false)
    public GWASResult save(GWASResult gwasResult) {
        gwasResultRepository.save(gwasResult);
        return gwasResult;
    }

    @Override
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
            chrData.setSnpAnnotations(annotationDataService.getSNPAnnotations(dataEntry.getKey().toLowerCase(), chrData.getPositions()));
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

}
