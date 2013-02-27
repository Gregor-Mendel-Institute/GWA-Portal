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
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.GWASDataService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.BasePermission;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class HDF5GWASDataService  implements GWASDataService {

    private static String BASE_DATA_FOLDER = "/net/gmi.oeaw.ac.at/gwasapp/";
    private static String BASE_VIEWER_FOLDER = BASE_DATA_FOLDER + "gwas_results/";
    private static String TEMP_DATA_FOLDER = BASE_DATA_FOLDER+"temp/";

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
				.of(BasePermission.READ);
		ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,trait.getId());
		Acl acl = aclService.readAclById(oid, authorities);
		try {
			if (!acl.isGranted(permissions, authorities, false)) 
				throw new AccessDeniedException("not allowed");
		}
		catch (NotFoundException e) {
			throw new AccessDeniedException("not allowed");
		}
		GWASReader gwasReader = new HDF5GWASReader(BASE_DATA_FOLDER);
		Map<String,GWASData> map = gwasReader.readAll(studyId+".hdf5", 0.05);
		return ImmutableMap.copyOf(map);
	}

    @Override
    public ImmutableMap<String, GWASData> getGWASDataByViewerId(Long gwasResultId) {
        GWASResult gwasResult = gwasResultRepository.findOne(gwasResultId);
        final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
        final ImmutableList<Permission> permissions = ImmutableList
                .of(BasePermission.READ);
        ObjectIdentity oid = new ObjectIdentityImpl(GWASResult.class,gwasResult.getId());
        Acl acl = aclService.readAclById(oid, authorities);
        try {
            if (!acl.isGranted(permissions, authorities, false))
                throw new AccessDeniedException("not allowed");
        }
        catch (NotFoundException e) {
            throw new AccessDeniedException("not allowed");
        }
        GWASReader gwasReader = new HDF5GWASReader(BASE_VIEWER_FOLDER);
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
        gwasResult = gwasResultRepository.save(gwasResult);
        Collection<Permission> permissions = ImmutableList.of(BasePermission.ADMINISTRATION,BasePermission.WRITE,BasePermission.DELETE,BasePermission.READ);
        addPermission(gwasResult, new PrincipalSid(SecurityUtil.getUsername()),permissions);
        addPermission(gwasResult,new GrantedAuthoritySid("ROLE_ADMIN"),permissions);
        File tempFile = new File(TEMP_DATA_FOLDER+UUID.randomUUID().toString());
        file.transferTo(tempFile);
        File targetFile = new File(BASE_VIEWER_FOLDER+gwasResult.getId()+".hdf5");
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
            gwasWriter.saveGWASDataToFile(data,targetFile);
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        finally  {
            tempFile.delete();
        }
        gwasResultRepository.save(gwasResult);
        return gwasResult;
    }

    @Override
    public List<GWASResult> findAllGWASResults() {
        List<GWASResult> gwasResults = gwasResultRepository.findAllByUsername(SecurityUtil.getUsername());
        return gwasResults;
    }


    @Override
    @Transactional(readOnly=false)
    public List<GWASResult> delete(GWASResult gwasResult) {

        ObjectIdentity oid = new ObjectIdentityImpl(GWASResult.class,gwasResult.getId());
        aclService.deleteAcl(oid,true);
        File file = new File(BASE_VIEWER_FOLDER+gwasResult.getId()+".hdf5");
        if (file.exists()) {
            file.delete();
        }
        gwasResultRepository.delete(gwasResult);
        return gwasResultRepository.findAllByUsername(SecurityUtil.getUsername());
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

    private void addPermission(GWASResult gwasResult, Sid recipient,
                              Collection<Permission> permissions) {
        MutableAcl acl;
        ObjectIdentity oid = new ObjectIdentityImpl(GWASResult.class,
                gwasResult.getId());

        try {
            acl = (MutableAcl) aclService.readAclById(oid);
        } catch (NotFoundException nfe) {
            acl = aclService.createAcl(oid);
        }
        for (Permission permission: permissions) {
            acl.insertAce(acl.getEntries().size(), permission, recipient, true);
        }
        aclService.updateAcl(acl);
    }

}
