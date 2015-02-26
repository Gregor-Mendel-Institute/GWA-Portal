#!/bin/sh

echo "DELETE TEMPLATE:"
curl -XDELETE $1/_template/template_annotation
echo "\n\nSAVE TEMPLATE"
curl -XPUT $1/_template/template_annotation -d @$2
echo "\n\nGET TEMPLATE"
curl -XGET $1/_template/template_annotation 
echo "\n\n"
