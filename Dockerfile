ARG GIT_BRANCH
ARG GIT_COMMIT
ARG SOURCE_BRANCH
ARG SOURCE_COMMIT
ARG BUILD_NUMBER
ARG BUILD_URL

FROM maven:3.3.3-jdk-8 as maven_builder
RUN mkdir -p /code/src
WORKDIR /code/src
COPY src/pom.xml .
COPY src/genophenbrowser-server/pom.xml genophenbrowser-server/
COPY src/genophenbrowser-shared/pom.xml genophenbrowser-shared/
COPY src/genophenbrowser-client/pom.xml genophenbrowser-client/
RUN ["mvn","de.qaware.maven:go-offline-maven-plugin:resolve-dependencies","verify", "clean","-B", "--fail-never"]
COPY src /code/src
RUN ["mvn", "clean", "package"]
RUN mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[' | tail -n 1 > version
RUN export VERSION=$(cat version); cp genophenbrowser-server/target/genophenbrowser-server-${VERSION}.war /root/ROOT.war

FROM jetty:9.4.26-jre8 as hdf5_builder
USER root
WORKDIR /root
RUN apt update && apt install -y gcc libhdf5-dev git
RUN git clone https://github.com/h5py/h5py.git
RUN cd h5py/lzf && gcc -O2 -fPIC -I/usr/include/hdf5/serial -L/usr/lib/x86_64-linux-gnu/hdf5/serial/ -shared lzf/*.c lzf_filter.c -o liblzf_filter.so -lhdf5 

FROM jetty:9.4.26-jre8
USER root
RUN apt update && apt install -y libhdf5-dev hdf5-tools
RUN echo "/usr/lib/x86_64-linux-gnu/hdf5/serial/" > /etc/ld.so.conf.d/hdf5.conf
RUN mkdir -p /usr/local/hdf5/lib/plugin
ENV HDF5_PLUGIN_PATH /usr/local/hdf5/lib/plugin
COPY --from=hdf5_builder /root/h5py/lzf/liblzf_filter.so /usr/local/hdf5/lib/plugin
RUN useradd --uid=10372 -ms /bin/bash gwaportal
RUN mkdir ${JETTY_BASE}/webapps/genophenbrowser.d
RUN mkdir ${JETTY_BASE}/CONFIG
RUN chown -R gwaportal:gwaportal "$JETTY_HOME" "$JETTY_BASE" "$TMPDIR"
COPY --chown=gwaportal:gwaportal --from=maven_builder /root/ROOT.war ${JETTY_BASE}/webapps/genophenbrowser.war
COPY --chown=gwaportal:gwaportal genophenbrowser.xml  ${JETTY_BASE}/webapps/
COPY --chown=gwaportal:gwaportal override-web.xml ${JETTY_BASE}/webapps/genophenbrowser.d/
USER gwaportal
RUN cp $JETTY_HOME/lib/jetty-proxy*jar $JETTY_BASE/lib/ext
RUN java -jar "$JETTY_HOME/start.jar" --add-to-startd=servlets
COPY --chown=gwaportal:gwaportal bioportal-ontology-detail.cache ProgramData/cache/bioportal-ontology-detail.cache
ARG GIT_BRANCH
ARG GIT_COMMIT
ARG SOURCE_BRANCH
ARG SOURCE_COMMIT
ARG BUILD_NUMBER
ARG BUILD_URL
ENV VERSION ${GIT_BRANCH:-$SOURCE_BRANCH}
ENV COMMIT_HASH ${GIT_COMMIT:-$SOURCE_COMMIT}
ENV BUILD_NUMBER $BUILD_NUMBER
ENV BUILD_URL $BUILD_URL
RUN echo ${VERSION}

