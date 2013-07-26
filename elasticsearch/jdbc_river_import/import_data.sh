#!/bin/sh
db_user="sync_elasticsearch"
db_password="searchisawesome"
db_host="gdpdm.gmi.oeaw.ac.at"
index=$2
es_host=$1

curl -XPUT $es_host/_river/${index}_experiment_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"oneshot\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT DISTINCT div_experiment_id as _id,  name, design, originator,comments, owner_id as \\\"owner.id\\\",owner_sid as \\\"owner.sid\\\", owner_name as \\\"owner.name\\\",\\\"publication.doi\\\",\\\"publication.volume\\\", \\\"publication.url\\\", \\\"publication.issue\\\",\\\"publication.page\\\", \\\"publication.journal\\\", \\\"publication.title\\\",\\\"publication.author\\\", \\\"publication.pubdate\\\", published, modified,created,permission as \\\"acl.permissions\\\",sid as \\\"acl.id\\\" FROM observation.view_search_div_experiment_acl order by div_experiment_id ASC\"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"experiment\"
    }
}"

echo "\n"

curl -XPUT $es_host/_river/${index}_phenotype_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"oneshot\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT DISTINCT div_trait_uom_id as _id,div_experiment_id as _parent, local_trait_name,trait_protocol,unit_type as \\\"div_unit_of_measure.unit_type\\\", published, modified,created,permission as \\\"acl.permissions\\\", permission_id as \\\"acl.id\\\",owner_id as \\\"owner.id\\\",owner_sid as \\\"owner.sid\\\", owner_name as \\\"owner.name\\\",experiment,t_ont.acc as \\\"to_accession.term_id\\\",t_ont.name as \\\"to_accession.term_name\\\",t_ont.definition as \\\"to_accession.term_definition\\\",t_ont.comment as \\\"to_accession.term_comment\\\" FROM phenotype.view_search_div_trait_uom_acl LEFT JOIN dblink('dbname=Ontologies user=$db_user password=$db_password','SELECT acc,name,term_definition as definition,term_comment as comment FROM term LEFT JOIN term_definition ON term_definition.term_id = term.id') AS t_ont(acc varchar(255),name varchar(255),definition text,comment text) ON t_ont.acc = to_accession\"
   },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"phenotype\"
    }
}"

echo "\n"

curl -XPUT $es_host/_river/${index}_study_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"oneshot\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT DISTINCT cdv_g2p_study_id as _id,div_trait_uom_id as _parent,div_experiment_id as _routing,name,producer,study_date,\\\"protocol.analysis_method\\\",\\\"allele_assay.name\\\",\\\"allele_assay.producer\\\",\\\"allele_assay.comments\\\",\\\"allele_assay.assay_date\\\",\\\"allele_assay.scoring_tech_type.scoring_tech_group\\\" ,\\\"allele_assay.scoring_tech_type.scoring_tech_type\\\",published,modified,created,permission as \\\"acl.permissions\\\", permission_id as \\\"acl.id\\\",owner_id as \\\"owner.id\\\",owner_sid as \\\"owner.sid\\\", owner_name as \\\"owner.name\\\",experiment,phenotype from cdv.view_search_cdv_g2p_study_acl order by cdv_g2p_study_id\"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"study\"
    }
}"

echo "\n"

curl -XPUT $es_host/_river/${index}_publication_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"simple\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT id as _id,doi ,volume ,url,issue ,page ,journal ,title,author,pubdate  FROM util.publications\"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"publication\"
    }
}"



echo "\n"

curl -XPUT $es_host/_river/${index}_user_river/_meta -d "{
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


curl -XPUT $es_host/_river/${index}_taxonomy_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
        \"strategy\":\"oneshot\",
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

curl -XPUT $es_host/_river/${index}_passport_river/_meta -d "{
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

curl -XPUT $es_host/_river/${index}_stock_river/_meta -d "{
    \"type\" : \"jdbc\",
    \"jdbc\" : {
	\"strategy\":\"simple\",
        \"driver\" : \"org.postgresql.Driver\",
        \"url\" : \"jdbc:postgresql://$db_host:5432/GDPDM\",
        \"user\" : \"$db_user\",
        \"password\" : \"$db_password\",
        \"sql\" : \"SELECT div_stock_id as _id,div_stock.div_passport_id as _parent,p.div_taxonomy_id as _routing,seed_lot,stock_source, div_stock.comments,p.div_passport_id,p.div_taxonomy_id,g.icis_id as \\\"generation.icis_id\\\",g.selfing_number as \\\"generation.selfing_number\\\",g.sibbing_number as \\\"generation.sibbing_number\\\",g.comments as \\\"generation.comments\\\" FROM germplasm.div_stock LEFT JOIN germplasm.div_passport as p ON div_stock.div_passport_id = p.div_passport_id LEFT JOIN germplasm.div_generation as g ON g.div_generation_id = div_stock.div_generation_id  \"
    },
    \"index\":{
       \"index\":\"$index\",
       \"type\":\"stock\"
    }
}"

echo '\n'
