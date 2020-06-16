package com.gmi.nordborglab.jpaontology.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;


@Entity
@Table(name="term")
@SequenceGenerator(name="idSequence",sequenceName="term_id_seq")
public class Term extends BaseOntologyEntity {
	
	
	private String name;
	
	@Column(name="term_type")
	private String termType;
	private String acc;
	
	@OneToMany(mappedBy="child",cascade={CascadeType.PERSIST,CascadeType.MERGE})

    private Set<Term2Term> parents = new HashSet<Term2Term>();
	
	@OneToMany(mappedBy="parent",cascade={CascadeType.PERSIST,CascadeType.MERGE})
    private Set<Term2Term> childs = new HashSet<Term2Term>();
	
	@OneToOne(mappedBy="term")
	private TermDefinition termDefinition;
	
	@OneToMany(mappedBy="termDBXRefId.term",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<TermDBXref> termDBXrefs;
	
	@OneToMany(mappedBy="termSynonymId.term",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<TermSynonym> synonyms;
	
	@ManyToMany
    @JoinTable(name="term_subset", joinColumns={@JoinColumn(name="term_id")}, inverseJoinColumns={@JoinColumn(name="subset_id")})
	private Set<Term> subsets;
	
	@OneToMany(mappedBy="child",cascade={CascadeType.PERSIST,CascadeType.MERGE})
	private Set<Term2TermMeta> considerations;
	
	
	@Column(name="is_obsolete")
	private Boolean isObsolete ;
	@Column(name="is_root")
	private Boolean isRoot ;
	@Column(name="is_relation")
	private Boolean isRelation ;

    @Transient
    private List<Integer> pathToRoot;


    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTermType() {
		return termType;
	}
	public void setTermType(String termType) {
		this.termType = termType;
	}
	public String getAcc() {
		return acc;
	}
	public void setAcc(String acc) {
		this.acc = acc;
	}
	public Set<Term2Term> getParents() {
		return parents;
	}
	public void setParents(Set<Term2Term> parents) {
		this.parents = parents;
	}
	public Set<Term2Term> getChilds() {
		return childs;
	}
	public void setChilds(Set<Term2Term> childs) {
		this.childs = childs;
	}
	public TermDefinition getTermDefinition() {
		return termDefinition;
	}
	public void setTermDefinition(TermDefinition termDefinition) {
		this.termDefinition = termDefinition;
	}
	public Set<TermDBXref> getTermDBXrefs() {
		return termDBXrefs;
	}
	public void setTermDBXrefs(Set<TermDBXref> termDBXrefs) {
		this.termDBXrefs = termDBXrefs;
	}
	public Set<TermSynonym> getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(Set<TermSynonym> synonyms) {
		this.synonyms = synonyms;
	}
	public Set<Term> getSubsets() {
		return subsets;
	}
	public void setSubsets(Set<Term> subsets) {
		this.subsets = subsets;
	}
	public Set<Term2TermMeta> getConsiderations() {
		return considerations;
	}
	public void setConsiderations(Set<Term2TermMeta> considerations) {
		this.considerations = considerations;
	}
	public Boolean getIsObsolete() {
		return isObsolete;
	}
	public void setIsObsolete(Boolean isObsolete) {
		this.isObsolete = isObsolete;
	}
	public Boolean getIsRoot() {
		return isRoot;
	}
	public void setIsRoot(Boolean isRoot) {
		this.isRoot = isRoot;
	}
	public Boolean getIsRelation() {
		return isRelation;
	}
	public void setIsRelation(Boolean isRelation) {
		this.isRelation = isRelation;
	}

    public int getChildCount() {
        return childs.size();
    }

    public int getParentCount() {
        return parents.size();
    }

    public void setPathToRoot(List<Integer> rootToPath) {
        this.pathToRoot = rootToPath;
    }

    public List<Integer> getPathToRoot() {
        return pathToRoot;
    }
}
