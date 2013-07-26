CREATE OR REPLACE VIEW observation.view_search_div_experiment AS 
 SELECT div_experiment.div_experiment_id, div_experiment.div_experiment_acc, 
    div_experiment.name, div_experiment.design, div_experiment.originator, 
    div_experiment.comments, acl_entry.sid, acl_entry.mask, acl_entry.granting, 
    acl_object_identity.owner_sid as owner_id,acl_sid.sid as owner_sid,CONCAT(users.firstname,' ',users.lastname) as owner_name,
    publications.doi AS "publication.doi", 
    publications.volume AS "publication.volume", 
    publications.url AS "publication.url", 
    publications.issue AS "publication.issue", 
    publications.page AS "publication.page", 
    publications.journal AS "publication.journal", 
    publications.title AS "publication.title", 
    publications.author AS "publication.author", 
    publications.pubdate AS "publication.pubdate", 
    div_experiment.published,
    div_experiment.modified,
    div_experiment.created
   FROM observation.div_experiment
   LEFT JOIN util.publications_experiment ON publications_experiment.div_experiment_id = div_experiment.div_experiment_id
   LEFT JOIN util.publications ON publications.id = publications_experiment.publication_id
   LEFT JOIN acl.acl_object_identity ON acl_object_identity.object_id_identity = div_experiment.div_experiment_id AND acl_object_identity.object_id_class = 1
   LEFT JOIN acl.acl_entry ON acl_entry.acl_object_identity = acl_object_identity.id
   LEFT JOIN acl.acl_sid ON acl_sid.id = acl_object_identity.owner_sid
   LEFT JOIN acl.users ON users.id::text = acl_sid.sid::text
  ORDER BY div_experiment.div_experiment_id;

ALTER TABLE observation.view_search_div_experiment
  OWNER TO "uemit.seren";
GRANT ALL ON TABLE observation.view_search_div_experiment TO "uemit.seren";
GRANT ALL ON TABLE observation.view_search_div_experiment TO gdpdm;

-- View: observation.view_search_div_experiment_acl

-- DROP VIEW observation.view_search_div_experiment_acl;

CREATE OR REPLACE VIEW observation.view_search_div_experiment_acl AS 
        (         SELECT v1.div_experiment_id, v1.div_experiment_acc, v1.name, 
                    v1.design, v1.originator, v1.comments, v1.sid, v1.mask, 
                    v1.granting,
                    v1.owner_id, v1.owner_sid ,v1.owner_name,
                    v1."publication.doi", 
                    v1."publication.volume", v1."publication.url", 
                    v1."publication.issue", v1."publication.page", 
                    v1."publication.journal", v1."publication.title", 
                    v1."publication.author", v1."publication.pubdate", 
                    v1.published, v1.modified, v1.created, 
                    'read'::text AS permission
                   FROM observation.view_search_div_experiment v1
                  WHERE (v1.mask & 1) = 1
        UNION 
                 SELECT v1.div_experiment_id, v1.div_experiment_acc, v1.name, 
                    v1.design, v1.originator, v1.comments, v1.sid, v1.mask, 
                    v1.granting, 
                    v1.owner_id , v1.owner_sid ,v1.owner_name,
                    v1."publication.doi", 
                    v1."publication.volume", v1."publication.url", 
                    v1."publication.issue", v1."publication.page", 
                    v1."publication.journal", v1."publication.title", 
                    v1."publication.author", v1."publication.pubdate", 
                    v1.published, v1.modified, v1.created, 
                    'write'::text AS permission
                   FROM observation.view_search_div_experiment v1
                  WHERE (v1.mask & 2) = 2)
UNION 
         SELECT v1.div_experiment_id, v1.div_experiment_acc, v1.name, v1.design, 
            v1.originator, v1.comments, v1.sid, v1.mask, v1.granting, 
            v1.owner_id , v1.owner_sid ,v1.owner_name,
            v1."publication.doi", v1."publication.volume", 
            v1."publication.url", v1."publication.issue", v1."publication.page", 
            v1."publication.journal", v1."publication.title", 
            v1."publication.author", v1."publication.pubdate", v1.published, 
            v1.modified, v1.created, 'admin'::text AS permission
           FROM observation.view_search_div_experiment v1
          WHERE (v1.mask & 4) = 4;

ALTER TABLE observation.view_search_div_experiment_acl
  OWNER TO "uemit.seren";
GRANT ALL ON TABLE observation.view_search_div_experiment_acl TO "uemit.seren";
GRANT ALL ON TABLE observation.view_search_div_experiment_acl TO gdpdm;




-- View: phenotype."VIEW_SEARCH_div_trait_uom"

-- DROP VIEW phenotype."VIEW_SEARCH_div_trait_uom";

CREATE OR REPLACE VIEW phenotype.view_search_div_trait_uom AS 
 SELECT DISTINCT div_trait_uom.div_trait_uom_id, 
    div_trait_uom.div_trait_uom_acc, div_trait_uom.div_unit_of_measure_id, 
    div_trait_uom.local_trait_name, div_trait_uom.trait_protocol, 
    div_trait_uom.to_accession, div_trait_uom.eo_accession, 
    div_experiment.div_experiment_id , div_unit_of_measure.unit_type, 
    trait_acl_entry.sid, trait_acl_entry.mask, trait_acl_entry.granting, 
    trait_acl.owner_sid as owner_id, acl_sid.sid as owner_sid,CONCAT(users.firstname,' ',users.lastname) as owner_name,
    experiment_acl_entry.sid AS parent_sid, 
    experiment_acl_entry.mask AS parent_mask, 
    experiment_acl_entry.granting AS parent_granting, 
    trait_acl.entries_inheriting,
    div_trait_uom.published,
    div_trait_uom.modified,
    div_trait_uom.created, 
    div_experiment.name as experiment
   FROM phenotype.div_trait_uom
   LEFT JOIN phenotype.div_unit_of_measure ON div_unit_of_measure.div_unit_of_measure_id = div_trait_uom.div_unit_of_measure_id
   LEFT JOIN phenotype.div_trait ON div_trait.div_trait_uom_id = div_trait_uom.div_trait_uom_id
   LEFT JOIN observation.div_obs_unit ON div_obs_unit.div_obs_unit_id = div_trait.div_obs_unit_id
   LEFT JOIN observation.div_experiment ON div_experiment.div_experiment_id = div_obs_unit.div_experiment_id
   LEFT JOIN acl.acl_object_identity trait_acl ON trait_acl.object_id_identity = div_trait_uom.div_trait_uom_id AND trait_acl.object_id_class = 2
   LEFT JOIN acl.acl_entry trait_acl_entry ON trait_acl_entry.acl_object_identity = trait_acl.id
   LEFT JOIN acl.acl_entry experiment_acl_entry ON experiment_acl_entry.acl_object_identity = trait_acl.parent_object
   LEFT JOIN acl.acl_sid ON acl_sid.id = trait_acl.owner_sid
   LEFT JOIN acl.users ON users.id::text = acl_sid.sid::text;

   ALTER TABLE phenotype.view_search_div_trait_uom
  OWNER TO "uemit.seren";
GRANT ALL ON TABLE phenotype.view_search_div_trait_uom TO "uemit.seren";
GRANT ALL ON TABLE phenotype.view_search_div_trait_uom TO gdpdm;



-- View: phenotype.view_search_div_trait_uom_acl

-- DROP VIEW phenotype.view_search_div_trait_uom_acl;

CREATE OR REPLACE VIEW phenotype.view_search_div_trait_uom_acl AS 
        (        (        (        (         SELECT view_search_div_trait_uom.div_trait_uom_id, 
                                            view_search_div_trait_uom.div_trait_uom_acc, 
                                            view_search_div_trait_uom.div_unit_of_measure_id, 
                                            view_search_div_trait_uom.local_trait_name, 
                                            view_search_div_trait_uom.trait_protocol, 
                                            view_search_div_trait_uom.to_accession, 
                                            view_search_div_trait_uom.eo_accession, 
                                            view_search_div_trait_uom.div_experiment_id,
                                            view_search_div_trait_uom.experiment,
                                            view_search_div_trait_uom.unit_type, 
                                            view_search_div_trait_uom.sid, 
                                            view_search_div_trait_uom.mask, 
                                            view_search_div_trait_uom.granting, 
                                            view_search_div_trait_uom.owner_id, 
                                            view_search_div_trait_uom.owner_sid,
                                            view_search_div_trait_uom.owner_name,
                                            view_search_div_trait_uom.parent_sid, 
                                            view_search_div_trait_uom.parent_mask, 
                                            view_search_div_trait_uom.parent_granting, 
                                            view_search_div_trait_uom.entries_inheriting, 
                                            view_search_div_trait_uom.published, 
                                            view_search_div_trait_uom.modified, 
                                            view_search_div_trait_uom.created, 
                                            view_search_div_trait_uom.sid AS permission_id, 
                                            'read'::text AS permission
                                           FROM phenotype.view_search_div_trait_uom
                                          WHERE (view_search_div_trait_uom.mask & 1) = 1
                                UNION 
                                         SELECT view_search_div_trait_uom.div_trait_uom_id, 
                                            view_search_div_trait_uom.div_trait_uom_acc, 
                                            view_search_div_trait_uom.div_unit_of_measure_id, 
                                            view_search_div_trait_uom.local_trait_name, 
                                            view_search_div_trait_uom.trait_protocol, 
                                            view_search_div_trait_uom.to_accession, 
                                            view_search_div_trait_uom.eo_accession, 
                                            view_search_div_trait_uom.div_experiment_id, 
                                            view_search_div_trait_uom.experiment,
                                            view_search_div_trait_uom.unit_type, 
                                            view_search_div_trait_uom.sid, 
                                            view_search_div_trait_uom.mask, 
                                            view_search_div_trait_uom.granting, 
                                            view_search_div_trait_uom.owner_id,
                                            view_search_div_trait_uom.owner_sid,
                                            view_search_div_trait_uom.owner_name,
                                            view_search_div_trait_uom.parent_sid, 
                                            view_search_div_trait_uom.parent_mask, 
                                            view_search_div_trait_uom.parent_granting, 
                                            view_search_div_trait_uom.entries_inheriting, 
                                            view_search_div_trait_uom.published, 
                                            view_search_div_trait_uom.modified, 
                                            view_search_div_trait_uom.created, 
                                            view_search_div_trait_uom.sid AS permission_id, 
                                            'write'::text AS permission
                                           FROM phenotype.view_search_div_trait_uom
                                          WHERE (view_search_div_trait_uom.mask & 2) = 2)
                        UNION 
                                 SELECT view_search_div_trait_uom.div_trait_uom_id, 
                                    view_search_div_trait_uom.div_trait_uom_acc, 
                                    view_search_div_trait_uom.div_unit_of_measure_id, 
                                    view_search_div_trait_uom.local_trait_name, 
                                    view_search_div_trait_uom.trait_protocol, 
                                    view_search_div_trait_uom.to_accession, 
                                    view_search_div_trait_uom.eo_accession, 
                                    view_search_div_trait_uom.div_experiment_id, 
                                    view_search_div_trait_uom.experiment,
                                    view_search_div_trait_uom.unit_type, 
                                    view_search_div_trait_uom.sid, 
                                    view_search_div_trait_uom.mask, 
                                    view_search_div_trait_uom.granting, 
                                    view_search_div_trait_uom.owner_id,
                                    view_search_div_trait_uom.owner_sid,
                                    view_search_div_trait_uom.owner_name,
                                    view_search_div_trait_uom.parent_sid, 
                                    view_search_div_trait_uom.parent_mask, 
                                    view_search_div_trait_uom.parent_granting, 
                                    view_search_div_trait_uom.entries_inheriting, 
                                    view_search_div_trait_uom.published, 
                                    view_search_div_trait_uom.modified, 
                                    view_search_div_trait_uom.created, 
                                    view_search_div_trait_uom.sid AS permission_id, 
                                    'admin'::text AS permission
                                   FROM phenotype.view_search_div_trait_uom
                                  WHERE (view_search_div_trait_uom.mask & 4) = 4)
                UNION 
                         SELECT view_search_div_trait_uom.div_trait_uom_id, 
                            view_search_div_trait_uom.div_trait_uom_acc, 
                            view_search_div_trait_uom.div_unit_of_measure_id, 
                            view_search_div_trait_uom.local_trait_name, 
                            view_search_div_trait_uom.trait_protocol, 
                            view_search_div_trait_uom.to_accession, 
                            view_search_div_trait_uom.eo_accession, 
                            view_search_div_trait_uom.div_experiment_id, 
                            view_search_div_trait_uom.experiment,
                            view_search_div_trait_uom.unit_type, 
                            view_search_div_trait_uom.sid, 
                            view_search_div_trait_uom.mask, 
                            view_search_div_trait_uom.granting, 
                            view_search_div_trait_uom.owner_id,
                            view_search_div_trait_uom.owner_sid,
                            view_search_div_trait_uom.owner_name,
                            view_search_div_trait_uom.parent_sid, 
                            view_search_div_trait_uom.parent_mask, 
                            view_search_div_trait_uom.parent_granting, 
                            view_search_div_trait_uom.entries_inheriting, 
                            view_search_div_trait_uom.published, 
                            view_search_div_trait_uom.modified, 
                            view_search_div_trait_uom.created, 
                            view_search_div_trait_uom.parent_sid AS permission_id, 
                            'read'::text AS permission
                           FROM phenotype.view_search_div_trait_uom
                          WHERE (view_search_div_trait_uom.parent_mask & 1) = 1 AND view_search_div_trait_uom.entries_inheriting = true)
        UNION 
                 SELECT view_search_div_trait_uom.div_trait_uom_id, 
                    view_search_div_trait_uom.div_trait_uom_acc, 
                    view_search_div_trait_uom.div_unit_of_measure_id, 
                    view_search_div_trait_uom.local_trait_name, 
                    view_search_div_trait_uom.trait_protocol, 
                    view_search_div_trait_uom.to_accession, 
                    view_search_div_trait_uom.eo_accession, 
                    view_search_div_trait_uom.div_experiment_id, 
                    view_search_div_trait_uom.experiment,
                    view_search_div_trait_uom.unit_type, 
                    view_search_div_trait_uom.sid, 
                    view_search_div_trait_uom.mask, 
                    view_search_div_trait_uom.granting, 
                    view_search_div_trait_uom.owner_id,
                    view_search_div_trait_uom.owner_sid,
                    view_search_div_trait_uom.owner_name,
                    view_search_div_trait_uom.parent_sid, 
                    view_search_div_trait_uom.parent_mask, 
                    view_search_div_trait_uom.parent_granting, 
                    view_search_div_trait_uom.entries_inheriting, 
                    view_search_div_trait_uom.published, 
                    view_search_div_trait_uom.modified, 
                    view_search_div_trait_uom.created, 
                    view_search_div_trait_uom.parent_sid AS permission_id, 
                    'write'::text AS permission
                   FROM phenotype.view_search_div_trait_uom
                  WHERE (view_search_div_trait_uom.parent_mask & 2) = 2 AND view_search_div_trait_uom.entries_inheriting = true)
UNION 
         SELECT view_search_div_trait_uom.div_trait_uom_id, 
            view_search_div_trait_uom.div_trait_uom_acc, 
            view_search_div_trait_uom.div_unit_of_measure_id, 
            view_search_div_trait_uom.local_trait_name, 
            view_search_div_trait_uom.trait_protocol, 
            view_search_div_trait_uom.to_accession, 
            view_search_div_trait_uom.eo_accession, 
            view_search_div_trait_uom.div_experiment_id, 
            view_search_div_trait_uom.experiment,
            view_search_div_trait_uom.unit_type, view_search_div_trait_uom.sid, 
            view_search_div_trait_uom.mask, view_search_div_trait_uom.granting, 
            view_search_div_trait_uom.owner_id,
            view_search_div_trait_uom.owner_sid,
            view_search_div_trait_uom.owner_name,
            view_search_div_trait_uom.parent_sid, 
            view_search_div_trait_uom.parent_mask, 
            view_search_div_trait_uom.parent_granting, 
            view_search_div_trait_uom.entries_inheriting, 
            view_search_div_trait_uom.published, 
            view_search_div_trait_uom.modified, 
            view_search_div_trait_uom.created, 
            view_search_div_trait_uom.parent_sid AS permission_id, 
            'admin'::text AS permission
           FROM phenotype.view_search_div_trait_uom
          WHERE (view_search_div_trait_uom.parent_mask & 4) = 4 AND view_search_div_trait_uom.entries_inheriting = true;

ALTER TABLE phenotype.view_search_div_trait_uom_acl
  OWNER TO "uemit.seren";
GRANT ALL ON TABLE phenotype.view_search_div_trait_uom_acl TO "uemit.seren";
GRANT ALL ON TABLE phenotype.view_search_div_trait_uom_acl TO gdpdm;



-- View: cdv."VIEW_SEARCH_cdv_g2p_study"

-- DROP VIEW cdv."VIEW_SEARCH_cdv_g2p_study";

CREATE OR REPLACE VIEW cdv.view_search_cdv_g2p_study AS 
 SELECT DISTINCT cdv_g2p_study.cdv_g2p_study_id, 
    cdv_g2p_study.cdv_g2p_study_acc, cdv_g2p_study.cdv_g2p_protocol_id, 
    cdv_g2p_study.name, cdv_g2p_study.producer, 
    cdv_g2p_study.div_allele_assay_id, cdv_g2p_study.study_date, 
    cdv_g2p_study.cdv_phen_transformation_id, div_trait.div_trait_uom_id, 
    div_obs_unit.div_experiment_id, 
    cdv_g2p_protocol.analysis_method AS "protocol.analysis_method", 
    div_allele_assay.name AS "allele_assay.name", 
    div_allele_assay.producer AS "allele_assay.producer", 
    div_allele_assay.comments AS "allele_assay.comments", 
    div_allele_assay.assay_date AS "allele_assay.assay_date", 
    div_scoring_tech_type.scoring_tech_group AS "allele_assay.scoring_tech_type.scoring_tech_group", 
    div_scoring_tech_type.scoring_tech_type AS "allele_assay.scoring_tech_type.scoring_tech_type", 
    study_acl_entry.sid, study_acl_entry.mask, study_acl_entry.granting, 
    study_acl.owner_sid as owner_id,acl_sid.sid as owner_sid,CONCAT(users.firstname,' ',users.lastname) as owner_name,
    study_acl.entries_inheriting, 
    trait_acl_entry.sid AS parent_sid, trait_acl_entry.mask AS parent_mask, 
    trait_acl_entry.granting AS parent_granting, 
    trait_acl.entries_inheriting AS parent_entries_inheriting, 
    experiment_acl_entry.sid AS grandparent_sid, 
    experiment_acl_entry.mask AS grandparent_mask, 
    experiment_acl_entry.granting AS grandparent_granting,
    cdv_g2p_study.published,
    cdv_g2p_study.modified,
    cdv_g2p_study.created,
    div_experiment.name as experiment,
    div_trait_uom.local_trait_name as phenotype
   FROM cdv.cdv_g2p_study
   LEFT JOIN cdv.cdv_g2p_protocol ON cdv_g2p_protocol.cdv_g2p_protocol_id = cdv_g2p_study.cdv_g2p_protocol_id
   LEFT JOIN genotype.div_allele_assay ON div_allele_assay.div_allele_assay_id = cdv_g2p_study.div_allele_assay_id
   LEFT JOIN genotype.div_scoring_tech_type ON div_allele_assay.div_scoring_tech_type_id = div_scoring_tech_type.div_scoring_tech_type_id
   LEFT JOIN cdv.cdv_pheno_set ON cdv_pheno_set.cdv_g2p_study_id = cdv_g2p_study.cdv_g2p_study_id
   LEFT JOIN phenotype.div_trait ON div_trait.div_trait_id = cdv_pheno_set.div_trait_id
   LEFT JOIN observation.div_obs_unit ON div_obs_unit.div_obs_unit_id = div_trait.div_obs_unit_id
   LEFT JOIN phenotype.div_trait_uom ON div_trait.div_trait_uom_id = div_trait_uom.div_trait_uom_id
   LEFT JOIN observation.div_experiment ON div_obs_unit.div_experiment_id = div_experiment.div_experiment_id
   LEFT JOIN acl.acl_object_identity study_acl ON study_acl.object_id_identity = cdv_g2p_study.cdv_g2p_study_id AND study_acl.object_id_class = 4
   LEFT JOIN acl.acl_entry study_acl_entry ON study_acl_entry.acl_object_identity = study_acl.id
   LEFT JOIN acl.acl_object_identity trait_acl ON trait_acl.id = study_acl.parent_object
   LEFT JOIN acl.acl_entry trait_acl_entry ON trait_acl_entry.acl_object_identity = study_acl.parent_object
   LEFT JOIN acl.acl_entry experiment_acl_entry ON experiment_acl_entry.acl_object_identity = trait_acl.parent_object
   LEFT JOIN acl.acl_sid ON acl_sid.id = study_acl.owner_sid
   LEFT JOIN acl.users ON users.id::text = acl_sid.sid::text;

ALTER TABLE cdv.view_search_cdv_g2p_study
  OWNER TO "uemit.seren";
GRANT ALL ON TABLE cdv.view_search_cdv_g2p_study TO "uemit.seren";
GRANT ALL ON TABLE cdv.view_search_cdv_g2p_study TO gdpdm;



-- View: cdv.view_search_cdv_g2p_study_acl

-- DROP VIEW cdv.view_search_cdv_g2p_study_acl;

CREATE OR REPLACE VIEW cdv.view_search_cdv_g2p_study_acl AS 
        (        (        (        (        (        (        (         SELECT view_search_cdv_g2p_study.cdv_g2p_study_id, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_study_acc, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_protocol_id, 
                                                                    view_search_cdv_g2p_study.name, 
                                                                    view_search_cdv_g2p_study.producer, 
                                                                    view_search_cdv_g2p_study.div_allele_assay_id, 
                                                                    view_search_cdv_g2p_study.study_date, 
                                                                    view_search_cdv_g2p_study.cdv_phen_transformation_id, 
                                                                    view_search_cdv_g2p_study.div_trait_uom_id, 
                                                                    view_search_cdv_g2p_study.phenotype, 
                                                                    view_search_cdv_g2p_study.div_experiment_id, 
                                                                    view_search_cdv_g2p_study.experiment,           
                                                                    view_search_cdv_g2p_study."protocol.analysis_method", 
                                                                    view_search_cdv_g2p_study."allele_assay.name", 
                                                                    view_search_cdv_g2p_study."allele_assay.producer", 
                                                                    view_search_cdv_g2p_study."allele_assay.comments", 
                                                                    view_search_cdv_g2p_study."allele_assay.assay_date", 
					view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_group", 
                                                                    view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_type", 
                                                                    view_search_cdv_g2p_study.sid, 
                                                                    view_search_cdv_g2p_study.mask, 
                                                                    view_search_cdv_g2p_study.granting, 
                                                                    view_search_cdv_g2p_study.owner_id, 
                                                                    view_search_cdv_g2p_study.owner_sid, 
                                                                    view_search_cdv_g2p_study.owner_name, 
                                                                    view_search_cdv_g2p_study.entries_inheriting, 
                                                                    view_search_cdv_g2p_study.parent_sid, 
                                                                    view_search_cdv_g2p_study.parent_mask, 
                                                                    view_search_cdv_g2p_study.parent_granting, 
                                                                    view_search_cdv_g2p_study.parent_entries_inheriting, 
                                                                    view_search_cdv_g2p_study.grandparent_sid, 
                                                                    view_search_cdv_g2p_study.grandparent_mask, 
                                                                    view_search_cdv_g2p_study.grandparent_granting, 
                                                                    view_search_cdv_g2p_study.published, 
                                                                    view_search_cdv_g2p_study.modified, 
                                                                    view_search_cdv_g2p_study.created, 
                                                                    view_search_cdv_g2p_study.sid AS permission_id, 
                                                                    'read'::text AS permission
                                                                   FROM cdv.view_search_cdv_g2p_study
                                                                  WHERE (view_search_cdv_g2p_study.mask & 1) = 1
                                                        UNION 
                                                                 SELECT view_search_cdv_g2p_study.cdv_g2p_study_id, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_study_acc, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_protocol_id, 
                                                                    view_search_cdv_g2p_study.name, 
                                                                    view_search_cdv_g2p_study.producer, 
                                                                    view_search_cdv_g2p_study.div_allele_assay_id, 
                                                                    view_search_cdv_g2p_study.study_date, 
                                                                    view_search_cdv_g2p_study.cdv_phen_transformation_id, 
                                                                    view_search_cdv_g2p_study.div_trait_uom_id, 
                                                                    view_search_cdv_g2p_study.phenotype, 
                                                                    view_search_cdv_g2p_study.div_experiment_id, 
                                                                    view_search_cdv_g2p_study.experiment,           
                                                                    view_search_cdv_g2p_study."protocol.analysis_method", 
                                                                    view_search_cdv_g2p_study."allele_assay.name", 
                                                                    view_search_cdv_g2p_study."allele_assay.producer", 
                                                                    view_search_cdv_g2p_study."allele_assay.comments", 
                                                                    view_search_cdv_g2p_study."allele_assay.assay_date", 
					view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_group", 
                                                                    view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_type", 
                                                                    view_search_cdv_g2p_study.sid, 
                                                                    view_search_cdv_g2p_study.mask, 
                                                                    view_search_cdv_g2p_study.granting, 
                                                                    view_search_cdv_g2p_study.owner_id, 
                                                                    view_search_cdv_g2p_study.owner_sid, 
                                                                    view_search_cdv_g2p_study.owner_name, 
                                                                    view_search_cdv_g2p_study.entries_inheriting, 
                                                                    view_search_cdv_g2p_study.parent_sid, 
                                                                    view_search_cdv_g2p_study.parent_mask, 
                                                                    view_search_cdv_g2p_study.parent_granting, 
                                                                    view_search_cdv_g2p_study.parent_entries_inheriting, 
                                                                    view_search_cdv_g2p_study.grandparent_sid, 
                                                                    view_search_cdv_g2p_study.grandparent_mask, 
                                                                    view_search_cdv_g2p_study.grandparent_granting, 
                                                                    view_search_cdv_g2p_study.published, 
                                                                    view_search_cdv_g2p_study.modified, 
                                                                    view_search_cdv_g2p_study.created, 
                                                                    view_search_cdv_g2p_study.sid AS permission_id,                                                                     'write'::text AS permission
                                                                   FROM cdv.view_search_cdv_g2p_study
                                                                  WHERE (view_search_cdv_g2p_study.mask & 2) = 2)
                                                UNION 
                                                         SELECT view_search_cdv_g2p_study.cdv_g2p_study_id, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_study_acc, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_protocol_id, 
                                                                    view_search_cdv_g2p_study.name, 
                                                                    view_search_cdv_g2p_study.producer, 
                                                                    view_search_cdv_g2p_study.div_allele_assay_id, 
                                                                    view_search_cdv_g2p_study.study_date, 
                                                                    view_search_cdv_g2p_study.cdv_phen_transformation_id, 
                                                                    view_search_cdv_g2p_study.div_trait_uom_id, 
                                                                    view_search_cdv_g2p_study.phenotype, 
                                                                    view_search_cdv_g2p_study.div_experiment_id, 
                                                                    view_search_cdv_g2p_study.experiment,           
                                                                    view_search_cdv_g2p_study."protocol.analysis_method", 
                                                                    view_search_cdv_g2p_study."allele_assay.name", 
                                                                    view_search_cdv_g2p_study."allele_assay.producer", 
                                                                    view_search_cdv_g2p_study."allele_assay.comments", 
                                                                    view_search_cdv_g2p_study."allele_assay.assay_date", 
					view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_group", 
                                                                    view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_type", 
                                                                    view_search_cdv_g2p_study.sid, 
                                                                    view_search_cdv_g2p_study.mask, 
                                                                    view_search_cdv_g2p_study.granting, 
                                                                    view_search_cdv_g2p_study.owner_id, 
                                                                    view_search_cdv_g2p_study.owner_sid, 
                                                                    view_search_cdv_g2p_study.owner_name, 
                                                                    view_search_cdv_g2p_study.entries_inheriting, 
                                                                    view_search_cdv_g2p_study.parent_sid, 
                                                                    view_search_cdv_g2p_study.parent_mask, 
                                                                    view_search_cdv_g2p_study.parent_granting, 
                                                                    view_search_cdv_g2p_study.parent_entries_inheriting, 
                                                                    view_search_cdv_g2p_study.grandparent_sid, 
                                                                    view_search_cdv_g2p_study.grandparent_mask, 
                                                                    view_search_cdv_g2p_study.grandparent_granting, 
                                                                    view_search_cdv_g2p_study.published, 
                                                                    view_search_cdv_g2p_study.modified, 
                                                                    view_search_cdv_g2p_study.created, 
                                                                    view_search_cdv_g2p_study.sid AS permission_id,                                                             'admin'::text AS permission
                                                           FROM cdv.view_search_cdv_g2p_study
                                                          WHERE (view_search_cdv_g2p_study.mask & 4) = 4)
                                        UNION 
                                                 SELECT view_search_cdv_g2p_study.cdv_g2p_study_id, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_study_acc, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_protocol_id, 
                                                                    view_search_cdv_g2p_study.name, 
                                                                    view_search_cdv_g2p_study.producer, 
                                                                    view_search_cdv_g2p_study.div_allele_assay_id, 
                                                                    view_search_cdv_g2p_study.study_date, 
                                                                    view_search_cdv_g2p_study.cdv_phen_transformation_id, 
                                                                    view_search_cdv_g2p_study.div_trait_uom_id, 
                                                                    view_search_cdv_g2p_study.phenotype, 
                                                                    view_search_cdv_g2p_study.div_experiment_id, 
                                                                    view_search_cdv_g2p_study.experiment,           
                                                                    view_search_cdv_g2p_study."protocol.analysis_method", 
                                                                    view_search_cdv_g2p_study."allele_assay.name", 
                                                                    view_search_cdv_g2p_study."allele_assay.producer", 
                                                                    view_search_cdv_g2p_study."allele_assay.comments", 
                                                                    view_search_cdv_g2p_study."allele_assay.assay_date", 
					view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_group", 
                                                                    view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_type", 
                                                                    view_search_cdv_g2p_study.sid, 
                                                                    view_search_cdv_g2p_study.mask, 
                                                                    view_search_cdv_g2p_study.granting, 
                                                                    view_search_cdv_g2p_study.owner_id, 
                                                                    view_search_cdv_g2p_study.owner_sid, 
                                                                    view_search_cdv_g2p_study.owner_name, 
                                                                    view_search_cdv_g2p_study.entries_inheriting, 
                                                                    view_search_cdv_g2p_study.parent_sid, 
                                                                    view_search_cdv_g2p_study.parent_mask, 
                                                                    view_search_cdv_g2p_study.parent_granting, 
                                                                    view_search_cdv_g2p_study.parent_entries_inheriting, 
                                                                    view_search_cdv_g2p_study.grandparent_sid, 
                                                                    view_search_cdv_g2p_study.grandparent_mask, 
                                                                    view_search_cdv_g2p_study.grandparent_granting, 
                                                                    view_search_cdv_g2p_study.published, 
                                                                    view_search_cdv_g2p_study.modified, 
                                                                    view_search_cdv_g2p_study.created, 
                                                                    view_search_cdv_g2p_study.parent_sid AS permission_id,                                                     'read'::text AS permission
                                                   FROM cdv.view_search_cdv_g2p_study
                                                  WHERE (view_search_cdv_g2p_study.parent_mask & 1) = 1 AND view_search_cdv_g2p_study.entries_inheriting = true)
                                UNION 
                                         SELECT view_search_cdv_g2p_study.cdv_g2p_study_id, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_study_acc, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_protocol_id, 
                                                                    view_search_cdv_g2p_study.name, 
                                                                    view_search_cdv_g2p_study.producer, 
                                                                    view_search_cdv_g2p_study.div_allele_assay_id, 
                                                                    view_search_cdv_g2p_study.study_date, 
                                                                    view_search_cdv_g2p_study.cdv_phen_transformation_id, 
                                                                    view_search_cdv_g2p_study.div_trait_uom_id, 
                                                                    view_search_cdv_g2p_study.phenotype, 
                                                                    view_search_cdv_g2p_study.div_experiment_id, 
                                                                    view_search_cdv_g2p_study.experiment,           
                                                                    view_search_cdv_g2p_study."protocol.analysis_method", 
                                                                    view_search_cdv_g2p_study."allele_assay.name", 
                                                                    view_search_cdv_g2p_study."allele_assay.producer", 
                                                                    view_search_cdv_g2p_study."allele_assay.comments", 
                                                                    view_search_cdv_g2p_study."allele_assay.assay_date", 
					view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_group", 
                                                                    view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_type", 
                                                                    view_search_cdv_g2p_study.sid, 
                                                                    view_search_cdv_g2p_study.mask, 
                                                                    view_search_cdv_g2p_study.granting, 
                                                                    view_search_cdv_g2p_study.owner_id, 
                                                                    view_search_cdv_g2p_study.owner_sid, 
                                                                    view_search_cdv_g2p_study.owner_name, 
                                                                    view_search_cdv_g2p_study.entries_inheriting, 
                                                                    view_search_cdv_g2p_study.parent_sid, 
                                                                    view_search_cdv_g2p_study.parent_mask, 
                                                                    view_search_cdv_g2p_study.parent_granting, 
                                                                    view_search_cdv_g2p_study.parent_entries_inheriting, 
                                                                    view_search_cdv_g2p_study.grandparent_sid, 
                                                                    view_search_cdv_g2p_study.grandparent_mask, 
                                                                    view_search_cdv_g2p_study.grandparent_granting, 
                                                                    view_search_cdv_g2p_study.published, 
                                                                    view_search_cdv_g2p_study.modified, 
                                                                    view_search_cdv_g2p_study.created, 
                                                                    view_search_cdv_g2p_study.parent_sid AS permission_id,                                             'write'::text AS permission
                                           FROM cdv.view_search_cdv_g2p_study
                                          WHERE (view_search_cdv_g2p_study.parent_mask & 2) = 2 AND view_search_cdv_g2p_study.entries_inheriting = true)
                        UNION 
                                 SELECT view_search_cdv_g2p_study.cdv_g2p_study_id, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_study_acc, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_protocol_id, 
                                                                    view_search_cdv_g2p_study.name, 
                                                                    view_search_cdv_g2p_study.producer, 
                                                                    view_search_cdv_g2p_study.div_allele_assay_id, 
                                                                    view_search_cdv_g2p_study.study_date, 
                                                                    view_search_cdv_g2p_study.cdv_phen_transformation_id, 
                                                                    view_search_cdv_g2p_study.div_trait_uom_id, 
                                                                    view_search_cdv_g2p_study.phenotype, 
                                                                    view_search_cdv_g2p_study.div_experiment_id, 
                                                                    view_search_cdv_g2p_study.experiment,           
                                                                    view_search_cdv_g2p_study."protocol.analysis_method", 
                                                                    view_search_cdv_g2p_study."allele_assay.name", 
                                                                    view_search_cdv_g2p_study."allele_assay.producer", 
                                                                    view_search_cdv_g2p_study."allele_assay.comments", 
                                                                    view_search_cdv_g2p_study."allele_assay.assay_date", 
					view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_group", 
                                                                    view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_type", 
                                                                    view_search_cdv_g2p_study.sid, 
                                                                    view_search_cdv_g2p_study.mask, 
                                                                    view_search_cdv_g2p_study.granting, 
                                                                    view_search_cdv_g2p_study.owner_id, 
                                                                    view_search_cdv_g2p_study.owner_sid, 
                                                                    view_search_cdv_g2p_study.owner_name, 
                                                                    view_search_cdv_g2p_study.entries_inheriting, 
                                                                    view_search_cdv_g2p_study.parent_sid, 
                                                                    view_search_cdv_g2p_study.parent_mask, 
                                                                    view_search_cdv_g2p_study.parent_granting, 
                                                                    view_search_cdv_g2p_study.parent_entries_inheriting, 
                                                                    view_search_cdv_g2p_study.grandparent_sid, 
                                                                    view_search_cdv_g2p_study.grandparent_mask, 
                                                                    view_search_cdv_g2p_study.grandparent_granting, 
                                                                    view_search_cdv_g2p_study.published, 
                                                                    view_search_cdv_g2p_study.modified, 
                                                                    view_search_cdv_g2p_study.created, 
                                                                    view_search_cdv_g2p_study.parent_sid AS permission_id,                                     'admin'::text AS permission
                                   FROM cdv.view_search_cdv_g2p_study
                                  WHERE (view_search_cdv_g2p_study.parent_mask & 4) = 4 AND view_search_cdv_g2p_study.entries_inheriting = true)
                UNION 
                         SELECT view_search_cdv_g2p_study.cdv_g2p_study_id, 
                                                                     view_search_cdv_g2p_study.cdv_g2p_study_acc, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_protocol_id, 
                                                                    view_search_cdv_g2p_study.name, 
                                                                    view_search_cdv_g2p_study.producer, 
                                                                    view_search_cdv_g2p_study.div_allele_assay_id, 
                                                                    view_search_cdv_g2p_study.study_date, 
                                                                    view_search_cdv_g2p_study.cdv_phen_transformation_id, 
                                                                    view_search_cdv_g2p_study.div_trait_uom_id, 
                                                                    view_search_cdv_g2p_study.phenotype, 
                                                                    view_search_cdv_g2p_study.div_experiment_id, 
                                                                    view_search_cdv_g2p_study.experiment,           
                                                                    view_search_cdv_g2p_study."protocol.analysis_method", 
                                                                    view_search_cdv_g2p_study."allele_assay.name", 
                                                                    view_search_cdv_g2p_study."allele_assay.producer", 
                                                                    view_search_cdv_g2p_study."allele_assay.comments", 
                                                                    view_search_cdv_g2p_study."allele_assay.assay_date", 
					view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_group", 
                                                                    view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_type", 
                                                                    view_search_cdv_g2p_study.sid, 
                                                                    view_search_cdv_g2p_study.mask, 
                                                                    view_search_cdv_g2p_study.granting, 
                                                                    view_search_cdv_g2p_study.owner_id, 
                                                                    view_search_cdv_g2p_study.owner_sid, 
                                                                    view_search_cdv_g2p_study.owner_name, 
                                                                    view_search_cdv_g2p_study.entries_inheriting, 
                                                                    view_search_cdv_g2p_study.parent_sid, 
                                                                    view_search_cdv_g2p_study.parent_mask, 
                                                                    view_search_cdv_g2p_study.parent_granting, 
                                                                    view_search_cdv_g2p_study.parent_entries_inheriting, 
                                                                    view_search_cdv_g2p_study.grandparent_sid, 
                                                                    view_search_cdv_g2p_study.grandparent_mask, 
                                                                    view_search_cdv_g2p_study.grandparent_granting, 
                                                                    view_search_cdv_g2p_study.published, 
                                                                    view_search_cdv_g2p_study.modified, 
                                                                    view_search_cdv_g2p_study.created, 
                                                                    view_search_cdv_g2p_study.grandparent_sid AS permission_id,                             'read'::text AS permission
                           FROM cdv.view_search_cdv_g2p_study
                          WHERE (view_search_cdv_g2p_study.grandparent_mask & 1) = 1 AND view_search_cdv_g2p_study.entries_inheriting = true AND view_search_cdv_g2p_study.parent_entries_inheriting = true)
        UNION 
                 SELECT view_search_cdv_g2p_study.cdv_g2p_study_id, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_study_acc, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_protocol_id, 
                                                                    view_search_cdv_g2p_study.name, 
                                                                    view_search_cdv_g2p_study.producer, 
                                                                    view_search_cdv_g2p_study.div_allele_assay_id, 
                                                                    view_search_cdv_g2p_study.study_date, 
                                                                    view_search_cdv_g2p_study.cdv_phen_transformation_id, 
                                                                    view_search_cdv_g2p_study.div_trait_uom_id, 
                                                                    view_search_cdv_g2p_study.phenotype, 
                                                                    view_search_cdv_g2p_study.div_experiment_id, 
                                                                    view_search_cdv_g2p_study.experiment,           
                                                                    view_search_cdv_g2p_study."protocol.analysis_method", 
                                                                    view_search_cdv_g2p_study."allele_assay.name", 
                                                                    view_search_cdv_g2p_study."allele_assay.producer", 
                                                                    view_search_cdv_g2p_study."allele_assay.comments", 
                                                                    view_search_cdv_g2p_study."allele_assay.assay_date", 
					view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_group", 
                                                                    view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_type", 
                                                                    view_search_cdv_g2p_study.sid, 
                                                                    view_search_cdv_g2p_study.mask, 
                                                                    view_search_cdv_g2p_study.granting, 
                                                                    view_search_cdv_g2p_study.owner_id, 
                                                                    view_search_cdv_g2p_study.owner_sid, 
                                                                    view_search_cdv_g2p_study.owner_name, 
                                                                    view_search_cdv_g2p_study.entries_inheriting, 
                                                                    view_search_cdv_g2p_study.parent_sid, 
                                                                    view_search_cdv_g2p_study.parent_mask, 
                                                                    view_search_cdv_g2p_study.parent_granting, 
                                                                    view_search_cdv_g2p_study.parent_entries_inheriting, 
                                                                    view_search_cdv_g2p_study.grandparent_sid, 
                                                                    view_search_cdv_g2p_study.grandparent_mask, 
                                                                    view_search_cdv_g2p_study.grandparent_granting, 
                                                                    view_search_cdv_g2p_study.published, 
                                                                    view_search_cdv_g2p_study.modified, 
                                                                    view_search_cdv_g2p_study.created, 
                                                                    view_search_cdv_g2p_study.grandparent_sid AS permission_id,                     'write'::text AS permission
                   FROM cdv.view_search_cdv_g2p_study
                  WHERE (view_search_cdv_g2p_study.grandparent_mask & 2) = 2 AND view_search_cdv_g2p_study.entries_inheriting = true AND view_search_cdv_g2p_study.parent_entries_inheriting = true)
UNION 
         SELECT view_search_cdv_g2p_study.cdv_g2p_study_id, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_study_acc, 
                                                                    view_search_cdv_g2p_study.cdv_g2p_protocol_id, 
                                                                    view_search_cdv_g2p_study.name, 
                                                                    view_search_cdv_g2p_study.producer, 
                                                                    view_search_cdv_g2p_study.div_allele_assay_id, 
                                                                    view_search_cdv_g2p_study.study_date, 
                                                                    view_search_cdv_g2p_study.cdv_phen_transformation_id, 
                                                                    view_search_cdv_g2p_study.div_trait_uom_id, 
                                                                    view_search_cdv_g2p_study.phenotype, 
                                                                    view_search_cdv_g2p_study.div_experiment_id, 
                                                                    view_search_cdv_g2p_study.experiment,           
                                                                    view_search_cdv_g2p_study."protocol.analysis_method", 
                                                                    view_search_cdv_g2p_study."allele_assay.name", 
                                                                    view_search_cdv_g2p_study."allele_assay.producer", 
                                                                    view_search_cdv_g2p_study."allele_assay.comments", 
                                                                    view_search_cdv_g2p_study."allele_assay.assay_date", 
					view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_group", 
                                                                    view_search_cdv_g2p_study."allele_assay.scoring_tech_type.scoring_tech_type", 
                                                                    view_search_cdv_g2p_study.sid, 
                                                                    view_search_cdv_g2p_study.mask, 
                                                                    view_search_cdv_g2p_study.granting, 
                                                                    view_search_cdv_g2p_study.owner_id, 
                                                                    view_search_cdv_g2p_study.owner_sid, 
                                                                    view_search_cdv_g2p_study.owner_name, 
                                                                    view_search_cdv_g2p_study.entries_inheriting, 
                                                                    view_search_cdv_g2p_study.parent_sid, 
                                                                    view_search_cdv_g2p_study.parent_mask, 
                                                                    view_search_cdv_g2p_study.parent_granting, 
                                                                    view_search_cdv_g2p_study.parent_entries_inheriting, 
                                                                    view_search_cdv_g2p_study.grandparent_sid, 
                                                                    view_search_cdv_g2p_study.grandparent_mask, 
                                                                    view_search_cdv_g2p_study.grandparent_granting, 
                                                                    view_search_cdv_g2p_study.published, 
                                                                    view_search_cdv_g2p_study.modified, 
                                                                    view_search_cdv_g2p_study.created, 
                                                                    view_search_cdv_g2p_study.grandparent_sid AS permission_id,             'admin'::text AS permission
           FROM cdv.view_search_cdv_g2p_study
          WHERE (view_search_cdv_g2p_study.grandparent_mask & 4) = 4 AND view_search_cdv_g2p_study.entries_inheriting = true AND view_search_cdv_g2p_study.parent_entries_inheriting = true;

ALTER TABLE cdv.view_search_cdv_g2p_study_acl
  OWNER TO "uemit.seren";
GRANT ALL ON TABLE cdv.view_search_cdv_g2p_study_acl TO "uemit.seren";
GRANT ALL ON TABLE cdv.view_search_cdv_g2p_study_acl TO gdpdm;







