#!/bin/sh
export PYTHONPATH=/home/GMI/uemit.seren/Code/disease-ontology/scripts/
python index_ontologies.py -o  ~/Projects/Ontologies/trait_ontologies.obo -t trait -e http://${1}:9200
python index_ontologies.py -o  ~/Projects/Ontologies/environment_ontology.obo -t environment -e http://${1}:9200
