package com.gmi.nordborglab.browser.server.domain.ontology;

import com.google.common.collect.Sets;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.*;

import javax.persistence.Transient;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: uemit.seren
 * Date: 5/8/13
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */

@NodeEntity
public class Term extends BaseGraphOntologyEntity{

    @Indexed(unique=true)
    private String id;
    private String name;
    private String definition;
    private String synonyms;


    @RelatedToVia(type="is_a",direction=Direction.INCOMING)
    private Set<IsATerm> is_a_children;

    @RelatedToVia(type="is_a",direction=Direction.OUTGOING)
    Set<IsATerm> is_a_parents;

    @RelatedToVia(type="part_of",direction = Direction.INCOMING)
    Set<PartOfTerm> part_of_children;

    @RelatedToVia(type="part_of",direction = Direction.OUTGOING)
    Set<PartOfTerm> part_of_parents;

    @Transient
    private List<Long> pathToRoot;

    public Term() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public Set<IsATerm> getIsAChildren() {
        return is_a_children;
    }

    public Set<PartOfTerm> getPartOfChildren() {
        return part_of_children;
    }

    public Set<IsATerm> getIsAParents() {
        return is_a_parents;
    }

    public Set<PartOfTerm> getPartOfParents() {
        return part_of_parents;
    }

    public Set<BasicTerm2Term> getParents() {
        return Sets.union(is_a_parents,part_of_parents);
    }

    public Set<BasicTerm2Term> getChildren() {
        return Sets.union(is_a_children,part_of_children);
    }

    public int getChildCount() {
        return getChildren().size();
    }

    public void setPathToRoot(List<Long> pathToRoot) {
        this.pathToRoot = pathToRoot;
    }

    public List<Long> getPathToRoot() {
        return pathToRoot;
    }

    public String getComment() {
        return "";
    }

    public String getType() {
        return "";
    }


}
