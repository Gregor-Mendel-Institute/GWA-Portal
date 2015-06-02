#!/bin/sh

filename=$(basename "$2")
filename="${filename%.*}"
echo "DELETE TEMPLATE:"
curl -XDELETE $1/_template/$filename
echo "\n\nSAVE TEMPLATE"
curl -XPUT $1/_template/$filename -d @$2
echo "\n\nGET TEMPLATE"
curl -XGET $1/_template/$filename 
echo "\n\n"
