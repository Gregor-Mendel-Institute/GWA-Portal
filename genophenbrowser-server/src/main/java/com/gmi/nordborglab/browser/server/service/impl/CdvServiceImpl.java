package com.gmi.nordborglab.browser.server.service.impl;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.genotype.AlleleAssay;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.Trait;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.domain.util.StudyJob;
import com.gmi.nordborglab.browser.server.repository.AlleleAssayRepository;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;
import com.gmi.nordborglab.browser.server.security.CustomPermission;
import com.gmi.nordborglab.browser.server.security.CustomUser;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CdvServiceImpl implements CdvService {

	@Resource
	protected StudyRepository studyRepository;
	
	@Resource
	protected TraitUomRepository traitUomRepository;
	
	@Resource
	protected TraitRepository traitRepository;

	@Resource
	protected RoleHierarchy roleHierarchy;

    @Resource
    protected AlleleAssayRepository alleleAssayRepository;
	
	@Resource
	protected MutableAclService aclService;

	
	@Override
	public StudyPage findStudiesByPhenotypeId(Long id, int start, int size) {
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList
				.of(CustomPermission.READ);
		ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,id);
		Acl acl = aclService.readAclById(oid, authorities);
		try {
			if (!acl.isGranted(permissions, authorities, false)) 
				throw new AccessDeniedException("not allowed");
		}
		catch (NotFoundException e) {
			throw new AccessDeniedException("not allowed");
		}
		if (start > 0)
			start = start/size;
		PageRequest pageRequest = new PageRequest(start, size);
		Page<Study> studyPage = studyRepository.findByPhenotypeId(id,
				pageRequest);
        StudyPage page = new StudyPage(studyPage.getContent(), pageRequest,
				studyPage.getTotalElements());
		return page;
	}


	@Override
	public Study findStudy(Long id) {
		Study study = studyRepository.findOne(id);
		study = checkStudyPermissions(study, CustomPermission.READ);
		return study;
	}


	@Override
	@Transactional(readOnly = false)
	public Study saveStudy(Study study) {
        study = checkStudyPermissions(study, CustomPermission.EDIT);
        CustomUser user = SecurityUtil.getUserFromContext();
        if (user != null)
            study.setProducer(user.getFullName());
		study = studyRepository.save(study);
		return study;
	}


	@Override
	public List<Study> findStudiesByPassportId(Long passportId) {
		Sort sort = new Sort("id");
		ImmutableList<Study> studies = filterStudiesByAcl(studyRepository.findAllByPassportId(passportId,sort)).toImmutableList();
		return studies;
	}
	
	private FluentIterable<Study> filterStudiesByAcl(List<Study> studiesToFilter) {
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList.of(CustomPermission.READ);
		FluentIterable<Study> studies = FluentIterable.from(studiesToFilter);
		if (studies.size() > 0) {
			List<Object[]> studyTraits = traitUomRepository.findAllByStudiesGrouped(studiesToFilter);
			ImmutableListMultimap<TraitUom, Object[]> studyTraitMap = Multimaps.index(studyTraits, new Function<Object[],TraitUom>() {

				@Override
				@Nullable
				public TraitUom apply(@Nullable Object[] input) {
					return (TraitUom)input[1];
				}
			});
			//ImmutableList<TraitUom> traits =  ImmutableList.copyOf(traitUomRepository.findAllByStudies(studiesToFilter));
			ImmutableSet<TraitUom> traits = studyTraitMap.keySet();
			final ImmutableBiMap<TraitUom,ObjectIdentity> identities = SecurityUtil.retrieveObjectIdentites(traits.asList()).inverse();
			final ImmutableMap<ObjectIdentity,Acl> acls = ImmutableMap.copyOf(aclService.readAclsById(identities.values().asList(), authorities));
			
			Predicate<Map.Entry<TraitUom, Object[]>> predicate = new Predicate<Map.Entry<TraitUom, Object[]>>() {

				@Override
				public boolean apply(Map.Entry<TraitUom, Object[]> entry) {
					boolean flag = false;
					ObjectIdentity identity = identities.get(entry.getKey());
					if (acls.containsKey(identity)) {
						Acl acl = acls.get(identity);
						try {
                            if (acl.isGranted(permissions, authorities, false))
							    flag = true;
                        }
                        catch (NotFoundException ex) {

                        }
					}
					return flag;
				}
			};
			Multimap<TraitUom,Object[]> filteredStudyTraitMap = Multimaps.filterEntries(studyTraitMap,predicate);
			studies = FluentIterable.from(Collections2.transform(filteredStudyTraitMap.values(),new Function<Object[], Study>() {

				@Override
				@Nullable
				public Study apply(@Nullable Object[] input) {
					Study study = null;
					if (input != null) {
						study = (Study)input[0];
					}
					return study;
				}
			
			}));
		}
		
		return studies;
	}


	@Override
	public StudyPage findAll(String name, String phenotype, String experiment,
			Long alleleAssayId, Long studyProtocolId, int start, int size) {
		StudyPage page;
		int pageStart = 0;
		if (start > 0)
			pageStart = start/size;
		PageRequest pageRequest = new PageRequest(start, size);
		Sort sort = new Sort("id");
		ImmutableList<Study> studies = filterStudiesByAcl(studyRepository.findAll(sort)).toList();
		List<Study> partitionedStudies = Iterables.get(Iterables.partition(studies, size),pageStart);
		int	totalElements = partitionedStudies.size();
		if (totalElements > 0) {
			page = new StudyPage(partitionedStudies, pageRequest,
				totalElements);
		}
		else {
			page = new StudyPage(partitionedStudies, pageRequest, 0);
		}
		return page;
	}


	@Override
	public List<Trait> findTraitValues(Long studyId) {
		Study study = studyRepository.findOne(studyId);
		study = checkStudyPermissions(study, CustomPermission.READ);
		List<Trait> traits = traitRepository.findAllByStudiesId(studyId);
		return traits;
	}

    @Override
    public List<AlleleAssay> findAlleleAssaysWithStats(Long phenotypeId, Long statisticTypeId) {
        Long traitValuesCount = traitRepository.countNumberOfTraitValues(phenotypeId, statisticTypeId);
        List<AlleleAssay> alleleAssays = alleleAssayRepository.findAll();
        for (AlleleAssay alleleAssay :alleleAssays) {
            Long availableAllelesCount = alleleAssayRepository.countAvailableAlleles(phenotypeId, statisticTypeId, alleleAssay.getId());
            alleleAssay.setTraitValuesCount(traitValuesCount);
            alleleAssay.setAvailableAllelesCount(availableAllelesCount);
        }
        return alleleAssays;
    }

    @Transactional(readOnly = false)
    @Override
    public Study createStudyJob(Long studyId) {
        Study study = studyRepository.findOne(studyId);
        if (study.getJob() != null) {
            throw new RuntimeException("Study has already a job assigned");
        }
        study = checkStudyPermissions(study, CustomPermission.EDIT);
        StudyJob job = new StudyJob();
        job.setStatus("Queued");
        job.setProgress(1);
        job.setCreateDate(new Date());
        job.setModificationDate(new Date());
        job.setTask("Waiting for workflow to start");
        study.setJob(job);
        studyRepository.save(study);
        return study;
    }

    private Study checkStudyPermissions(Study study,Permission permission) {
		if (study.getTraits().size() == 0)
			throw new RuntimeException("Study must have phenotypes assigned");
		TraitUom trait = Iterables.get(study.getTraits(), 0).getTraitUom();
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList
				.of(permission);
		ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,trait.getId());
		Acl acl = aclService.readAclById(oid, authorities);
		try {
			if (!acl.isGranted(permissions, authorities, false)) 
				throw new AccessDeniedException("not allowed");
		}
		catch (NotFoundException e) {
			throw new AccessDeniedException("not allowed");
		}
		boolean isOwner = false;
		for (Sid sid : authorities) {
			if (sid.equals(acl.getOwner())) {
				isOwner = true;
				break;
			}
		}
		AccessControlEntry ace = null;
		if (acl.getEntries().size() > 0) {
			 ace = acl.getEntries().get(0);
        }
		else if (acl.getParentAcl().getEntries().size() > 0) {
			for (AccessControlEntry aceToCheck:acl.getParentAcl().getEntries()) {
                if (authorities.contains(aceToCheck.getSid())) {
                    ace = aceToCheck;
                    break;
                }
            }
        }
		study.setIsOwner(isOwner);
		if (ace != null)
			study.setUserPermission(new CustomAccessControlEntry((Long)ace.getId(),ace.getPermission().getMask(),ace.isGranting()));
		return study;
	}

}
