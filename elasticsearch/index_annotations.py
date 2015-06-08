#!/usr/bin/env python

import csv
import os
import itertools
import copy
from optparse import OptionParser
import pdb
import cPickle
import requests
import json
import StringIO
import codecs
import re
import sys

SNPEFF_REGEX = re.compile(r"(\w+)\((.+)\)")


index = 'geno_chr%s'

bulk_index_count = 1000

def _indexDocument(elasticsearch_host,document,index,type,id,parent_id=None,routing=None):
    url = '%s/%s/%s/%s' % (elasticsearch_host,index.lower(),type,id)
    if parent_id is not None:
        url=url+"?parent=%s" % parent_id
    elif routing is not None:
        url = url+"?routing=%s" % routing
    req = requests.put(url,data=json.dumps(document,encoding='cp1252'))
    if req.status_code in [200,201,202]:
        return req.json()
    else: 
        raise Exception(str(req.text))

def _updateDocument(elasticsearch_host,document,index,type,id):
    url = '%s/%s/%s/%s/_update' % (elasticsearch_host,index.lower(),type,id)
    document_to_update = {'doc':document}
    req = requests.post(url,data=json.dumps(document_to_update,encoding='cp1252'))
    if req.status_code in [200,201,202]:
        return req.json()
    else: 
        raise Exception(str(req.text)) 

def _bulkIndexDocuments(elasticsearch_host,documents,type,idKey='position'):
    url = '%s/_bulk' % elasticsearch_host
    payload = ''
    for document in documents:
        bulk_index = index % document["chr"].lower()
        #parent_routing_suffix = '"_parent":"%s"' % document['gene'][0]['name'] if 'gene' in document else '"_routing":"%s"' % document['position']
        action = '{"index":{"_index":"%s","_type":"%s","_id":"%s"}}\n' % (bulk_index,type,document[idKey])
        data = json.dumps(document,encoding='cp1252')+'\n'
        payload = payload + action + data
    req = requests.put(url,data=payload)
    if req.status_code in [200,201,202]:
        return req.json()
    else:  
        raise Exception(str(req.text))

def _indexGenes(elasticsearch_host,gene_annotation_file):
    if gene_annotation_file is None:
        return
    genes = cPickle.load(open(gene_annotation_file,'r'))
    failed_genes = []
    documents = []
    bulk_ix = 1
    try:
        for id,gene in genes.iteritems():
            document = _getDocumentFromGene(gene)
            documents.append(document)
            if len(documents) == bulk_index_count:
                status = _bulkIndexDocuments(elasticsearch_host,documents,'gene',idKey='name') 
                documents = []
                for document_status in status['items']: 
                    if "index" not in document_status or document_status['index']['status'] not in [200,201,202]:
                         failed_genes.append({'name':"%s" % document_status})
                print "%s Genes indexed\n" % (bulk_ix * bulk_index_count)
                bulk_ix += 1
    except Exception,err:
        pdb.set_trace()
        raise err
    return failed_genes


def _getDocumentFromGene(gene):
    document = {'chr':gene['chromosome'],'start_pos':int(gene['start_pos']),'end_pos':int(gene['end_pos']),'name':gene['name'],'annotation':gene['gene_type'],'strand':1 if gene['strand'] == '+' else 0}
    if gene['gene_type'] == 'transposable_element':
        document['fragments'] = gene['fragments']
    else:
        document['isoforms'] = _getIsoForms(gene)
    return document

def _getDocumentsFromOntologies(row):
    documents = {}
    document = _getDocumentFromSNP(row)
    if document['inGene'] == 1:
        ontologies = row[12].split(";")
        i = 0
        for gene in document['gene']: 
            GO_ontologies = _getGOntologies(ontologies[i])
            if GO_ontologies is not None:
                documents[gene['name']] = {'GO':GO_ontologies}
            i = i+1
    else: 
        GO_ontologies_left = _getGOntologies(row[12].split(";")[0])
        GO_ontologies_right = _getGOntologies(row[12].split(";")[1])
        if GO_ontologies_left is not None:
            documents[document['gene_left']] = {'GO':GO_ontologies_left}
        if GO_ontologies_right is not None:
            documents[document['gene_right']] = {'GO':GO_ontologies_right}
    return documents

def _getGOntologies(text):
    if text == 'NA':
        return
    text_split = text.split(",")
    ontologies = []
    for ontology in text_split:
        ontology_split = ontology.split(":")
        ontologies.append({'relation':ontology_split[0],'exact':ontology_split[1],'narrow':ontology_split[2]})
    return ontologies

def _getIsoForms(gene):
    if gene['gene_type'] != 'gene':
        return None
    isoforms = []
    for gene_item_name,gene_item_body in gene.iteritems():
        if gene_item_name[0:2] == 'AT':
            isoform =  {}
            isoform['RNA_type'] = gene_item_body['RNA_type']
            isoform['name'] = gene_item_body['name']
            isoform['start_pos'] = gene_item_body['start_pos']
            isoform['end_pos'] = gene_item_body['end_pos']
            isoform['strand'] = 1 if gene_item_body['strand'] == '+' else 0
            isoform['exons'] = gene_item_body['exons']
            isoform['cds'] = gene_item_body['cds']
            if 'five_prime_UTR' in gene_item_body:
                isoform['five_prime_UTR'] = gene_item_body['five_prime_UTR']
            if 'three_prime_UTR' in gene_item_body:
                isoform['three_prime_UTR'] = gene_item_body['three_prime_UTR']
            isoform['description'] = gene_item_body['functional_description']['computational_description']
            isoform['curator_summary'] = gene_item_body['functional_description']['curator_summary']
            isoform['short_description'] = gene_item_body['functional_description']['short_description']
            isoform['annotation'] = gene_item_body['functional_description']['type']
            isoforms.append(isoform)
    return isoforms

def _indexSNPS(elasticsearch_host,snps_annotation_file):
    if snps_annotation_file is None:
        return
    failed_snps = []
    documents = []
     
    try:
        with open(snps_annotation_file,'rb') as content:
            reader = csv.reader((x.replace('\0','') for x in content),delimiter='\t')
            for row in reader:
                document = _getDocumentFromSNP(row)
                documents.append(document)
                if len(documents) == bulk_index_count:
                    status = _bulkIndexDocuments(elasticsearch_host,documents,'snps') 
                    documents = []
                    for document_status in status['items']: 
                        if document_status['index']['ok'] != True:
                            failed_snps.append({'name':"%s" % document_status['index']['_id'],'error':str(err)})
    except Exception,err:
        raise err
    return failed_snps

def _indexOntologies(elasticsearch_host,ontology_file):
    if ontology_file is None:
        return
    failed_ontologies = []
    documents = []
    updated_ontologies= {} 
    try:
        with open(ontology_file,'rb') as content:
            reader = csv.reader((x.replace('\0','') for x in content),delimiter='\t')
            for row in reader:
                documents = _getDocumentsFromOntologies(row)
                for key,document in documents.iteritems():
                    try:
                        if key in updated_ontologies:
                            continue
                        chr = key[2].lower()
                        retval = _updateDocument(elasticsearch_host,document,index % chr,'gene',key)
                        updated_ontologies[key] = True
                        if retval is None:
                            raise Exception(str(retval))    
                    except Exception, err:
                        failed_ontologies.append({'name':key,'error':str(err)})
                        raise err
    except Exception,err:
        raise err
    return failed_ontologies    

def _getDocumentFromSNP(snp):
    document = {'chr':snp[0],'position':int(snp[1]),'ref':snp[2],'lyr':snp[3],'alt':snp[4],'annotation':snp[6]}
    if snp[7].startswith('Intergenic'):
        genes_split = snp[7].split(":")[1].split(";")
        left_gene = genes_split[0].split("_")[0]
        right_gene = genes_split[1].split("_")[0]
        document['gene_left'] = left_gene
        document['gene_right'] = right_gene
        document['inGene'] = 0
    else:
        document['inGene'] = 1
        genes_split = snp[7].split(";")
        annotations_split = snp[8].split(";")
        gene_regions_split = snp[9].split(";")
        translation_types_split = snp[10].split(";")
        genes = []
        i = 0
        for gene in genes_split:
            gene_doc = {'name':gene[0:-3],'annotation':annotations_split[i],'strand':1 if gene[-3:] == '(+)' else 0}
            gene_region_split = gene_regions_split[i].split(",")
            translation_type_split = translation_types_split[i].split(",")
            j = 0
            gene_regions = []
            translation_types = []
            for gene_region in gene_region_split:
                gene_regions.append(gene_region.split(":")[1].split("_")[0])
                translation_types.append(translation_type_split[j].split(":")[1].split("_")[0])
                j = j+1
            gene_doc['gene_region'] = gene_regions
            gene_doc['translation_type'] = translation_types
            genes.append(gene_doc)
            i = i+1
        document['gene'] = genes    
    return document
 

def _getDocumentFromSNPEff(snp):
    lyr= snp[4]
    if lyr == '0':
        lyr = snp[2]
    elif lyr == '1':
        lyr = snp[3]
    document = {'chr':snp[0][3],'position':int(snp[1]),'ref':snp[2],'alt':snp[3],'lyr':lyr}
    inGene,annotations = _parseSNPEffInfo(snp[5],snp[6])
    document['inGene'] = inGene
    document['annotations'] = annotations
    return document

def _parseSNPEffInfo(genotype,info):
    infos = info.split(";")[-1].split("=")[-1].split(",")
    annotations = []
    inGene = 0
    for info in infos:
        matches = SNPEFF_REGEX.match(info)
        if not matches or len(matches.groups()) != 2:
            return (inGene,annotations)
        annotation = {'effect':matches.group(1)}
        fields = matches.group(2).split("|")
        if len(fields) > 12:
            raise Exception('Length of parsed fields must be at least 11 but was %s' % len(fields))
        if fields[-1] != genotype:
            continue
        annotation['impact'] = fields[0]
        if fields[1] != '':
            annotation['function'] = fields[1]
        if fields[2]!= '':
            annotation['codon_change'] = fields[2]
        if fields[3] != '':
            annotation['amino_acid_change'] = fields[3]
        if fields[5] != '':
            annotation['gene_name'] = fields[5]
            inGene = 1
        if fields[8] != '':
            annotation['transcript_id'] = fields[8]
        if fields[9] != '':
            annotation['rank'] = int(fields[9])
        annotations.append(annotation)
    return inGene,annotations
             
def _indexSNPEff(elasticsearch_host,snpeff_filename):
    if snpeff_filename is None:
        return
    failed_snps = []
    documents = []
    bulk_ix = 1
    try:
        with open(snpeff_filename,'rb') as content:
            for row in content:
                fields = row.split("\t")
                document = _getDocumentFromSNPEff(fields)
                documents.append(document)
                if len(documents) == bulk_index_count:
                    status = _bulkIndexDocuments(elasticsearch_host,documents,'snps') 
                    if len(documents) != len(status['items']):
                        raise Exception('Requested %s documents to be indexed but only %s in result' % (len(documents),len(status['items'])))
                    for document_status in status['items']: 
                        if "index" not in document_status or document_status['index']['status'] not in [200,201,202]:
                            failed_snps.append({'name':"%s" % document_status})
                    documents = []
                    print "%s SNPs indexed\n" % (bulk_ix * bulk_index_count)
              	    bulk_ix += 1
    except Exception,err:
        raise err
    return failed_snps    


def indexData(elasticsearch_host,gene_annotation_filename=None,snp_annotation_filename =None,ontology_filename=None,snpeff_filename=None):
    failed_genes = _indexGenes(elasticsearch_host,gene_annotation_filename)
    failed_snps = _indexSNPS(elasticsearch_host,snp_annotation_filename)
    failed_ontologies= _indexOntologies(elasticsearch_host,ontology_filename)
    failed_snpeff = _indexSNPEff(elasticsearch_host,snpeff_filename)

    if (failed_genes is not None):
        print "======== FAILED GENES ==========="
        if len(failed_genes) == 0:
            print " ALL SUCCESSFUL"
        for failed_gene in failed_genes:
            print "%s" % failed_gene
    if (failed_snps is not None):
        print "======== FAILED SNPS ==========="
        if len(failed_snps) == 0:
            print " ALL SUCCESSFUL"
        for failed_snp in failed_snps:
            print "%s" % failed_snp
    if failed_ontologies is not None:
        print "======== FAILED Ontologies ==========="
        if len(failed_ontologies) == 0:
            print " ALL SUCCESSFUL"
        for failed_ontology in failed_ontologies:
            print "%s" % failed_ontology
    if failed_snpeff is not None:
        print "======== FAILED SNPEff ==========="
        if len(failed_snpeff) == 0:
            print " ALL SUCCESSFUL"
        for failed in failed_snpeff:
            print "%s" % failed





if __name__ == '__main__':
    usage = """usage: %prog [options] 
examples: 
  python index_annotations.py -g /net/gmi.oeaw.ac.at/gwasapp/gwas-web/genome_annotation.pickled -e http://elasticsearch.gmi.oeaw.ac.at:9200 
  python index_annotations.py -s ~/Projects/annotations/SNPs/chr1.annotation -e http://elasticsearch.gmi.oeaw.ac.at:9200
  python index_annotations.py -o ~/Projects/annotations/SNPs/chr1.annotation -e http://elasticsearch.gmi.oeaw.ac.at:9200
  python index_annotations.py -f ~/Projects/annotations/SNPs/snpeff.annotation -e http://elasticsearch.gmi.oeaw.ac.at:9200
"""
    parser = OptionParser(usage=usage)
    parser.add_option("-g", "--gene_annotation_file", dest="gene_annotation_filename",help="the gene annotation filename ", metavar="FILE")
    parser.add_option("-s", "--snp_annotation_file", dest="snp_annotation_filename",help="the snp annotation filename", metavar="FILE")
    parser.add_option("-f","--snpeff_file",dest="snpeff_filename",help="the snpeff file",metavar="FILE")
    parser.add_option("-o", "--ontology_file",dest="ontology_filename",help="the ontology filename",metavar="FILE")
    parser.add_option("-e", "--elasticsearch_host",dest="elasticsearch_host",help="the host where elasticsearch is running")
    (options, args) = parser.parse_args()
    if not options.gene_annotation_filename and not options.snp_annotation_filename and not options.ontology_filename and not options.snpeff_filename:
         parser.error("you have to specify an snp_annotation_file or a gene_annotation_file or snpeff_filename")
    elif not options.elasticsearch_host:
         parser.error("you must specify the elasticsearch host")
    else:
        indexData(options.elasticsearch_host,options.gene_annotation_filename,options.snp_annotation_filename,options.ontology_filename,options.snpeff_filename)
    parser.destroy()
