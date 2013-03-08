package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.data.GWASData;
import com.gmi.nordborglab.browser.server.data.GWASReader;
import com.gmi.nordborglab.browser.server.data.csv.CSVGWASReader;
import com.gmi.nordborglab.browser.server.data.hdf5.HDF5GWASReader;
import com.gmi.nordborglab.browser.server.domain.acl.AppUser;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.GWASResult;
import com.gmi.nordborglab.browser.server.repository.GWASResultRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.repository.UserRepository;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HDF5GWASDataService  implements GWASDataService {

    private @Value("${GWAS.study.folder}") String GWAS_STUDY_FOLDER;
    private @Value("${GWAS.viewer.dest_folder}") String GWAS_VIEWER_FOLDER;

    @Value("${java.io.tmpdir}")
    private String GWAS_VIEWER_TEMP_FOLDER;

	@Resource
	protected TraitUomRepository traitUomRepository;

    @Resource
    protected GWASResultRepository gwasResultRepository;

    @Resource
    protected UserRepository userRepository;
	
	@Resource
	protected MutableAclService aclService;

	
	@Resource
	protected RoleHierarchy roleHierarchy;
	
	protected GWASReader gwasReader;
	
	@Override
	public ImmutableMap<String,GWASData> getGWASDataByStudyId(Long studyId) {
		TraitUom trait = traitUomRepository.findByStudyId(studyId);
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList
				.of(CustomPermission.READ);
		ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,trait.getId());
		Acl acl = aclService.readAclById(oid, authorities);
		try {
			if (!acl.isGranted(permissions, authorities, false)) 
				throw new AccessDeniedException("not allowed");
		}
		catch (NotFoundException e) {
			throw new AccessDeniedException("not allowed");
		}
		GWASReader gwasReader = new HDF5GWASReader(GWAS_STUDY_FOLDER);
		Map<String,GWASData> map = gwasReader.readAll(studyId+".hdf5", 0.05);
		return ImmutableMap.copyOf(map);
	}

    @Override
    public ImmutableMap<String, GWASData> getGWASDataByViewerId(Long gwasResultId) {
        GWASResult gwasResult = gwasResultRepository.findOne(gwasResultId);
        final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
        final ImmutableList<Permission> permissions = ImmutableList
                .of(CustomPermission.READ);
        ObjectIdentity oid = new ObjectIdentityImpl(GWASResult.class,gwasResult.getId());
        Acl acl = aclService.readAclById(oid, authorities);
        try {
            if (!acl.isGranted(permissions, authorities, false))
                throw new AccessDeniedException("not allowed");
        }
        catch (NotFoundException e) {
            throw new AccessDeniedException("not allowed");
        }
        GWASReader gwasReader = new HDF5GWASReader(GWAS_VIEWER_FOLDER);
        Map<String,GWASData> map = gwasReader.readAll(gwasResultId+".hdf5", 0.05);
        return ImmutableMap.copyOf(map);
    }

    @Override
    @Transactional(readOnly = false)
    public GWASResult uploadGWASResult(CommonsMultipartFile file) throws IOException {
        if (file.isEmpty())
            throw new RuntimeException("File is empty");
        if (!file.getContentType().trim().equalsIgnoreCase("application/x-hdf") && !(file.getContentType().trim().equalsIgnoreCase("text/csv"))) {
            throw new IOException("Content Type "+ file.getContentType()+ " not supported");
        }

        AppUser appUser = userRepository.findOne(SecurityUtil.getUsername());
        GWASResult gwasResult = new GWASResult();
        gwasResult.setName(file.getOriginalFilename());
        gwasResult.setAppUser(appUser);
        File tempFile = new File(GWAS_VIEWER_TEMP_FOLDER+"/"+UUID.randomUUID().toString());
        file.transferTo(tempFile);

        GWASReader gwasWriter = new HDF5GWASReader("");
        GWASReader  gwasReader = null;
        if (file.getContentType().trim().equalsIgnoreCase("application/x-hdf")) {
            gwasReader = gwasWriter;
        }
        else if (file.getContentType().trim().equalsIgnoreCase("text/csv")) {
            gwasReader = new CSVGWASReader();
        }
        try {
            gwasReader.isValidGWASFile(tempFile);
            Map<String,GWASData> data = gwasReader.parseGWASDataFromFile(tempFile);
            gwasResult = updateStats(gwasResult,data);
            gwasResult = gwasResultRepository.save(gwasResult);
            File targetFile = new File(GWAS_VIEWER_FOLDER +gwasResult.getId()+".hdf5");
            gwasWriter.saveGWASDataToFile(data,targetFile);
            addPermission(gwasResult, new GrantedAuthoritySid("ROLE_ANONYMOUS"),new CustomPermission(0));
            addPermission(gwasResult, new PrincipalSid(SecurityUtil.getUsername()),CustomPermission.ALL);
            addPermission(gwasResult,new GrantedAuthoritySid("ROLE_ADMIN"),CustomPermission.ALL);
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        finally  {
            tempFile.delete();
        }

        return gwasResult;
    }

    @Override
    public List<GWASResult> findAllGWASResults() {
        //List<GWASResult> gwasResults = gwasResultRepository.findAllByUsername(SecurityUtil.getUsername());
        List<GWASResult> gwasResultsToFilter = gwasResultRepository.findAll();
        final ImmutableList<Permission> permissions = ImmutableList.of(CustomPermission.READ, CustomPermission.EDIT, CustomPermission.ADMINISTRATION);
        Sid sid = new PrincipalSid(SecurityUtil.getAuthentication());
        final List<Sid> authorities = ImmutableList.of(sid);
        FluentIterable<GWASResult> gwasResults = FluentIterable.from(gwasResultsToFilter);
        if (gwasResults .size() > 0) {
            final ImmutableBiMap<GWASResult,ObjectIdentity> identities = SecurityUtil.retrieveObjectIdentites(gwasResults.toList()).inverse();
            final ImmutableMap<ObjectIdentity,Acl> acls = ImmutableMap.copyOf(aclService.readAclsById(identities.values().asList(), authorities));

            Predicate<GWASResult> predicate = new Predicate<GWASResult>() {

                @Override
                public boolean apply(GWASResult gwasResult) {
                    boolean flag = false;
                    ObjectIdentity identity = identities.get(gwasResult);
                    if (acls.containsKey(identity)) {
                        Acl acl = acls.get(identity);
                        try {
                            if (acl.isGranted(permissions, authorities, false))
                                flag = true;
                        }catch (NotFoundException e) {

                        }
                    }
                    return flag;
                }
            };
            gwasResults = gwasResults.filter(predicate);
        }
        return gwasResults.toList();
    }

    @Override
    public GWASResult findOneGWASResult(Long id) {
        return gwasResultRepository.findOne(id);
    }




    @Override
    @Transactional(readOnly=false)
    public List<GWASResult> delete(GWASResult gwasResult) {

        ObjectIdentity oid = new ObjectIdentityImpl(GWASResult.class,gwasResult.getId());
        aclService.deleteAcl(oid,true);
        File file = new File(GWAS_VIEWER_FOLDER +gwasResult.getId()+".hdf5");
        if (file.exists()) {
            file.delete();
        }
        gwasResultRepository.delete(gwasResult);
        return gwasResultRepository.findAllByUsername(SecurityUtil.getUsername());
    }

    @Override
    @Transactional(readOnly = false)
    public GWASResult save(GWASResult gwasResult) {
        return gwasResultRepository.save(gwasResult);
    }

    private GWASResult updateStats(GWASResult gwasResult,Map<String,GWASData> data) {
        float maxScore = 0;
        int numberOfSNPs = 0;
        for (Map.Entry<String,GWASData> entry:data.entrySet()) {
            GWASData gwasData = entry.getValue();
            if (maxScore < gwasData.getPvalues()[0]) {
                maxScore = gwasData.getPvalues()[0];
            }
            numberOfSNPs = numberOfSNPs + gwasData.getPositions().length;
        }
        gwasResult.setMaxScore(maxScore);
        gwasResult.setNumberOfSNPs(numberOfSNPs);
        return gwasResult;
    }

    @Transactional(readOnly = false)
    private void addPermission(GWASResult gwasResult, Sid recipient,
                              Permission permission) {
        MutableAcl acl;
        ObjectIdentity oid = new ObjectIdentityImpl(GWASResult.class,
                gwasResult.getId());

        try {
            acl = (MutableAcl) aclService.readAclById(oid);
        } catch (NotFoundException nfe) {
            acl = aclService.createAcl(oid);
        }
        acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        aclService.updateAcl(acl);
    }

}
