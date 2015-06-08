#!/usr/bin/env python

import os
import itertools
from optparse import OptionParser
import pdb
import requests
import json
import oboparser


index = 'ontologies'

bulk_index_count = 1000

def _indexDocument(elasticsearch_host,document,index,type,id):
    url = '%s/%s/%s/%s' % (elasticsearch_host,index.lower(),type,id)
    req = requests.put(url,data=json.dumps(document,encoding='utf8'))
    if req.status_code in [200,201,202]:
        return req.json()
    else: 
        raise Exception(str(req.text))



def _bulkIndexDocuments(elasticsearch_host,documents,type):
    url = '%s/_bulk' % elasticsearch_host
    payload = ''
    for document in documents:
        bulk_index = index % document["chr"]
        #parent_routing_suffix = '"_parent":"%s"' % document['gene'][0]['name'] if 'gene' in document else '"_routing":"%s"' % document['position']
        action = '{"index":{"_index":"%s","_type":"%s","_id":"%s"}}\n' % (bulk_index,type,document['position'])
        data = json.dumps(document,encoding='cp1252')+'\n'
        payload = payload + action + data
    req = requests.put(url,data=payload)
    if req.status_code in [200,201,202]:
        return req.json()
    else:  
        raise Exception(str(req.text))


def _getDocumentFromOntology(term):
    document = {}
    document['term_id'] = term.id
    document['name'] = term.name
    document['definition'] = term.definition
    document['synonyms'] = [{'name':synonym[0],'type':synonym[1]} for synonym in term.synonyms if synonym]
    document['xrefs'] = term.xrefs
    document['alt_id'] = term.alternateIds
    document['relationships'] = [{'term_id':rel[1],'type':rel[0]} for rel in term.relationships]
    document['subsets'] = term.subsets
    return document


def _indexOntologies(elasticsearch_host,ontology_file,ontology_type):
    failed_ontologies = []
    documents = []
    updated_ontologies= {}
    try:
        for term in oboparser.parse(ontology_file, ['is_a','part_of']):
            try: 
                if term.obsolete:
                    continue
                document = _getDocumentFromOntology(term)
                document['type'] = ontology_type
                retval = _indexDocument(elasticsearch_host,document,index,'term',document['term_id'])
                if retval is None:
                    raise Exception(str(retval))    
            except Exception, err:
                failed_ontologies.append({'name':term.id,'error':str(err)})
                raise err
    except Exception,err:
        raise err
    return failed_ontologies    


    


def indexData(elasticsearch_host,ontology_filename,ontology_type):
    failed_ontologies= _indexOntologies(elasticsearch_host,ontology_filename,ontology_type)
    if failed_ontologies is not None:
        print "======== FAILED Ontologies ==========="
        if len(failed_ontologies) == 0:
            print " ALL SUCCESSFUL"
        for failed_ontology in failed_ontologies:
            print "%s" % failed_ontology





if __name__ == '__main__':
    usage = """usage: %prog [options] 
examples: 
  python index_ontologies.py -o ontology.obo -e http://elasticsearch.gmi.oeaw.ac.at:9200
  make sure to export PYTHONPATH=/home/GMI/uemit.seren/eclipse_workspaces/intellij/disease-ontology/scripts/
"""
    parser = OptionParser(usage=usage)
    parser.add_option("-o", "--ontology_file",dest="ontology_filename",help="the ontology filename",metavar="FILE")
    parser.add_option("-e", "--elasticsearch_host",dest="elasticsearch_host",help="the host where elasticsearch is running")
    parser.add_option("-t", "--ontology_type",dest="ontology_type",help="type of the ontology")
    (options, args) = parser.parse_args()
    if not options.ontology_filename:
         parser.error("you have to specify an ontology file")
    elif not options.elasticsearch_host:
         parser.error("you must specify the elasticsearch host")
    elif not options.ontology_type:
         parser.error("you must specify the ontology type")
    else:
        indexData(options.elasticsearch_host,options.ontology_filename,options.ontology_type)
    parser.destroy()
