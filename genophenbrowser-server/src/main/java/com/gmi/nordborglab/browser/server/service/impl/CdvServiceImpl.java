package com.gmi.nordborglab.browser.server.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.AccessControlEntry;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmi.nordborglab.browser.server.domain.cdv.Study;
import com.gmi.nordborglab.browser.server.domain.pages.StudyPage;
import com.gmi.nordborglab.browser.server.domain.phenotype.TraitUom;
import com.gmi.nordborglab.browser.server.repository.StudyRepository;
import com.gmi.nordborglab.browser.server.repository.TraitUomRepository;
import com.gmi.nordborglab.browser.server.security.CustomAccessControlEntry;
import com.gmi.nordborglab.browser.server.security.SecurityUtil;
import com.gmi.nordborglab.browser.server.service.CdvService;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

@Service
@Transactional(readOnly = true)
public class CdvServiceImpl implements CdvService {

	@Resource
	protected StudyRepository studyRepository;
	
	@Resource
	protected TraitUomRepository traitUomRepository;

	@Resource
	protected RoleHierarchy roleHierarchy;
	
	@Resource
	protected MutableAclService aclService;

	
	@Override
	public StudyPage findStudiesByPhenotypeId(Long id, int start, int size) {
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList
				.of(BasePermission.READ);
		ObjectIdentity oid = new ObjectIdentityImpl(TraitUom.class,id);
		Acl acl = aclService.readAclById(oid, authorities);
		try {
			if (!acl.isGranted(permissions, authorities, false)) 
				throw new AccessDeniedException("not allowed");
		}
		catch (NotFoundException e) {
			throw new AccessDeniedException("not allowed");
		}
		StudyPage page = null;
		if (start > 0)
			start = start/size;
		PageRequest pageRequest = new PageRequest(start, size);
		Page<Study> studyPage = studyRepository.findByPhenotypeId(id,
				pageRequest);
		page = new StudyPage(studyPage.getContent(), pageRequest,
				studyPage.getTotalElements());
		return page;
	}


	@Override
	public Study findStudy(Long id) {
		Study study = studyRepository.findOne(id);
		///TODO add ACL to Study derived from Trait 
		TraitUom trait = Iterables.get(study.getTraits(), 0).getTraitUom();
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
		boolean isOwner = false;
		for (Sid sid : authorities) {
			if (sid.equals(acl.getOwner())) {
				isOwner = true;
				break;
			}
		}
		AccessControlEntry ace = null;
		if (acl.getEntries().size() > 0)
			 ace = acl.getEntries().get(0);
		else if (acl.getParentAcl().getEntries().size() > 0)
			ace = acl.getParentAcl().getEntries().get(0);
			
		study.setIsOwner(isOwner);
		if (ace != null)
			study.setUserPermission(new CustomAccessControlEntry((Long)ace.getId(),ace.getPermission().getMask(),ace.isGranting()));
		return study;
	}


	@Override
	@Transactional(readOnly = false)
	public Study saveStudy(Study study) {
		if (study.getTraits().size() == 0)
			throw new RuntimeException("Study must have phenotypes assigned");
		TraitUom trait = Iterables.get(study.getTraits(), 0).getTraitUom();
		final List<Sid> authorities = SecurityUtil.getSids(roleHierarchy);
		final ImmutableList<Permission> permissions = ImmutableList
				.of(BasePermission.WRITE);
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
		if (acl.getEntries().size() > 0)
			 ace = acl.getEntries().get(0);
		else if (acl.getParentAcl().getEntries().size() > 0)
			ace = acl.getParentAcl().getEntries().get(0);
			
		study.setIsOwner(isOwner);
		if (ace != null)
			study.setUserPermission(new CustomAccessControlEntry((Long)ace.getId(),ace.getPermission().getMask(),ace.isGranting()));
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
		final ImmutableList<Permission> permissions = ImmutableList.of(BasePermission.READ);
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
						if (acl.isGranted(permissions, authorities, false)) 
							flag = true;
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

}
