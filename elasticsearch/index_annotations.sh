#!/bin/sh

python index_annotations.py -g /net/gmi.oeaw.ac.at/gwasapp/gwas-web/genome_annotation.pickled -e http://${1}:9200
python index_annotations.py -o ~/Projects/annotations/SNPs/chr1.annotation -e http://${1}:9200
python index_annotations.py -o ~/Projects/annotations/SNPs/chr2.annotation -e http://${1}:9200
python index_annotations.py -o ~/Projects/annotations/SNPs/chr3.annotation -e http://${1}:9200
python index_annotations.py -o ~/Projects/annotations/SNPs/chr4.annotation -e http://${1}:9200
python index_annotations.py -o ~/Projects/annotations/SNPs/chr5.annotation -e http://${1}:9200
python index_annotations.py -f ~/Projects/annotations/SNPs/snpeff.csv -e http://${1}:9200
