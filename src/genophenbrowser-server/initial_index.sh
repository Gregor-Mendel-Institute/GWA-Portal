#!/bin/bash
export MAVEN_OPTS="-Xmx8048m -Xms1024m -XX:MaxPermSize=512m"
CONFIG="/home/GMI/uemit.seren/Code/GWAPortal/genophenbrowser/genophenbrowser-server/CONFIG/"

if [ "$2" == "lotus" ]; then
   CONFIG="/home/GMI/uemit.seren/Code/GWAPortal/LOTUS/CONFIG/"
fi

echo $CONFIG

mvn -P indexer  exec:java -Dtype=gwasviewer,candidategenelist,candidategenelistenrichment -Dspring.profiles.active=$1 -Dext.prop.dir=$CONFIG
mvn -P indexer  exec:java -Dtype=experiment -Dspring.profiles.active=$1 -Dext.prop.dir=$CONFIG
mvn -P indexer  exec:java -Dtype=phenotype -Dspring.profiles.active=$1 -Dext.prop.dir=$CONFIG
mvn -P indexer  exec:java -Dtype=study -Dspring.profiles.active=$1 -Dext.prop.dir=$CONFIG
mvn -P indexer  exec:java -Dtype=passport,taxonomy,stock,user,publication -Dspring.profiles.active=$1 -Dext.prop.dir=$CONFIG
