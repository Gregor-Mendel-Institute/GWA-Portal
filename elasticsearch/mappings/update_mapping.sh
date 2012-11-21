#!/bin/sh

curl -XDELETE $1/_template/template_gdpdm
print "\n"
curl -XPUT $1/_template/template_gdpdm -d @$2
print "\n"
curl -XGET $1/_template/template_gdpdm 
print "\n"
