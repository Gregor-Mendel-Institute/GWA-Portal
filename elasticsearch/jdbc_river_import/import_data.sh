#!/bin/sh
db_user="sync_elasticsearch"
db_password="searchisawesome"
db_host="gdpdm.gmi.oeaw.ac.at"
index="gdpdm"

curl -XPUT $1/_river/gdpdm_experiment_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"simple\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT div_experiment.div_experiment_id as _id,name,design,originator,comments,publications.doi as \\\"publication.doi\\\",publications.volume as \\\"publication.volume\\\",publications.url as \\\"publication.url\\\",publications.issue as \\\"publication.issue\\\",publications.page as \\\"publication.page\\\",publications.journal as \\\"publication.journal\\\",publications.title as \\\"publication.title\\\",publications.author as \\\"publication.author\\\",publications.pubdate as \\\"publication.pubdate\\\" FROM observation.div_experiment LEFT JOIN util.publications_experiment ON publications_experiment.div_experiment_id = div_experiment.div_experiment_id LEFT JOIN util.publications ON publications.id = publications_experiment.publication_id order by div_experiment.div_experiment_id ASC\"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"experiment\"
    }
}"

echo "\n"

curl -XPUT $1/_river/gdpdm_phenotype_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"simple\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT DISTINCT div_trait_uom.div_trait_uom_id as _id,div_experiment.div_experiment_id as _parent, local_trait_name,trait_protocol,to_accession,eo_accession,div_unit_of_measure.unit_type as \\\"div_unit_of_measure.unit_type\\\" FROM phenotype.div_trait_uom LEFT JOIN phenotype.div_unit_of_measure ON div_unit_of_measure.div_unit_of_measure_id = div_trait_uom.div_unit_of_measure_id LEFT JOIN phenotype.div_trait ON div_trait.div_trait_uom_id = div_trait_uom.div_trait_uom_id LEFT JOIN observation.div_obs_unit ON div_obs_unit.div_obs_unit_id = div_trait.div_obs_unit_id LEFT JOIN observation.div_experiment ON div_experiment.div_experiment_id = div_obs_unit.div_experiment_id\"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"phenotype\"
    }
}"

echo "\n"

curl -XPUT $1/_river/gdpdm_study_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"simple\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT DISTINCT cdv_g2p_study.cdv_g2p_study_id as _id,div_trait.div_trait_uom_id as _parent,cdv_g2p_study.name,cdv_g2p_study.producer,study_date,analysis_method as \\\"protocol.analysis_method\\\",div_allele_assay.name as \\\"allele_assay.name\\\",div_allele_assay.producer as \\\"allele_assay.producer\\\",div_allele_assay.comments as \\\"allele_assay.comments\\\",div_allele_assay.assay_date as \\\"allele_assay.assay_date\\\",scoring_tech_group as \\\"allele_assay.scoring_tech_type.scoring_tech_group\\\" ,scoring_tech_type as \\\"allele_assay.scoring_tech_type.scoring_tech_type\\\" FROM cdv.cdv_g2p_study LEFT JOIN cdv.cdv_g2p_protocol ON cdv_g2p_protocol.cdv_g2p_protocol_id = cdv_g2p_study.cdv_g2p_protocol_id LEFT  JOIN genotype.div_allele_assay ON div_allele_assay.div_allele_assay_id =cdv_g2p_study.div_allele_assay_id LEFT JOIN genotype.div_scoring_tech_type ON div_allele_assay.div_scoring_tech_type_id = div_scoring_tech_type.div_scoring_tech_type_id LEFT  JOIN cdv.cdv_pheno_set ON cdv_pheno_set.cdv_g2p_study_id = cdv_g2p_study.cdv_g2p_study_id LEFT  JOIN phenotype.div_trait ON div_trait.div_trait_id = cdv_pheno_set.div_trait_id\"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"study\"
    }
}"

echo "\n"

curl -XPUT $1/_river/gdpdm_user_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"simple\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT username as _id,username,firstname,lastname,email, CASE WHEN enabled is true THEN 1 ELSE 0 END as enabled FROM acl.users\"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"user\"
    }
}"

echo "\n"


curl -XPUT $1/_river/gdpdm_taxonomy_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"simple\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT div_taxonomy_id as _id, genus,species,subspecies,subtaxa,race,population,common_name,term_accession,comments FROM germplasm.div_taxonomy \"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"taxonomy\"
    }
}"

echo "\n"

#parent child not supported by jdbc-river (workaround store the parent id)

curl -XPUT $1/_river/gdpdm_passport_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
	\"strategy\":\"simple\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT div_passport.div_passport_id as _id,div_taxonomy_id as _parent,accename,accenumb,div_passport.comments,div_sampstat.germplasm_type as \\\"sampstat.germplasm_type\\\",c.collector as \\\"collecting.collector\\\",c.collnumb as \\\"collecting.collnum\\\",c.collsrc as \\\"collecting.collsrc\\\",c.collcode as \\\"collecting.collcode\\\",c.col_date as \\\"collecting.col_date\\\",l.locality_name as \\\"collecting.locality.locality_name\\\",l.elevation as \\\"collecting.locality.elevation\\\",l.city as \\\"collecting.locality.city\\\",l.country as \\\"collecting.locality.country\\\",l.origcty as \\\"collecting.locality.origcty\\\",l.state_province as \\\"collecting.locality.state_province\\\",l.lo_accession as \\\"collecting.locality.lo_accession\\\",CASE WHEN l.latitude IS NOT NULL AND l.longitude is NOT NULL THEN CONCAT(l.latitude,',',l.longitude) ELSE NULL END as \\\"collecting.locality.location\\\",div_taxonomy_id as \\\"div_taxonomy_id\\\",div_allele_assay.name as \\\"allele_assay.name\\\",div_allele_assay.producer as \\\"allele_assay.producer\\\",div_allele_assay.comments as \\\"allele_assay.comments\\\", div_allele_assay.assay_date as \\\"allele_assay.assay_date\\\",scoring_tech_group as \\\"allele_assay.scoring_tech_type.scoring_tech_group\\\" ,scoring_tech_type as \\\"allele_assay.scoring_tech_type.scoring_tech_type\\\"  FROM germplasm.div_passport LEFT JOIN genotype.div_allele ON div_allele.div_passport_id = div_passport.div_passport_id LEFT JOIN genotype.div_allele_assay On div_allele_assay.div_allele_assay_id = div_allele.div_allele_assay_id LEFT JOIN germplasm.div_accession_collecting as c ON c.div_accession_collecting_id = div_passport.div_accession_collecting_id LEFT JOIN observation.div_locality as l ON l.div_locality_id = c.div_locality_id LEFT JOIN germplasm.div_sampstat ON div_sampstat.div_sampstat_id = div_passport.div_sampstat_id LEFT JOIN genotype.div_scoring_tech_type ON div_allele_assay.div_scoring_tech_type_id = div_scoring_tech_type.div_scoring_tech_type_id\"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"passport\"
    }
}"

echo "\n"

curl -XPUT $1/_river/gdpdm_stock_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
	\"strategy\":\"simple\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT div_stock_id as _id,div_stock.div_passport_id as _parent,seed_lot,stock_source, div_stock.comments,p.div_passport_id,p.div_taxonomy_id,g.icis_id as \\\"generation.icis_id\\\",g.selfing_number as \\\"generation.selfing_number\\\",g.sibbing_number as \\\"generation.sibbing_number\\\",g.comments as \\\"generation.comments\\\" FROM germplasm.div_stock LEFT JOIN germplasm.div_passport as p ON div_stock.div_passport_id = p.div_passport_id LEFT JOIN germplasm.div_generation as g ON g.div_generation_id = div_stock.div_generation_id  \"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"stock\"
    }
}"

echo '\n'
