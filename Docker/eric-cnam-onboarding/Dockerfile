ARG CBO_IMAGE_URL=armdocker.rnd.ericsson.se/proj-ldc/common_base_os_release/sles
ARG CBO_VERSION

FROM ${CBO_IMAGE_URL}:${CBO_VERSION} AS packages_base

ARG HELM_VERSION
ARG ORAS_CLI_VERSION
ENV ARTIFACTORY_URL=https://arm.sero.gic.ericsson.se/artifactory/proj-eric-lcm-helm-executor-artifacts-generic-local

ADD ${ARTIFACTORY_URL}/packages/oras/${ORAS_CLI_VERSION}/oras_${ORAS_CLI_VERSION}_linux_amd64.tar.gz /
RUN tar -zxf oras_${ORAS_CLI_VERSION}_linux_amd64.tar.gz

ADD ${ARTIFACTORY_URL}/packages/helm/${HELM_VERSION}/helm-${HELM_VERSION}-linux-amd64.tar.gz /
RUN tar -xf helm-${HELM_VERSION}-linux-amd64.tar.gz

COPY ./Docker/eric-cnam-onboarding/script/ /script/
RUN chmod -R +x /script

FROM ${CBO_IMAGE_URL}:${CBO_VERSION}

ARG CBO_REPO=arm.rnd.ki.sw.ericsson.se/artifactory/proj-ldc-repo-rpm-local/common_base_os/sles/
ARG CBO_VERSION=${CBO_VERSION}
ARG HELM_VERSION
ARG PRODUCT_VERSION
ARG COMMIT
ARG CONTAINER_ID
ARG PRODUCT_NUMBER
ARG PRODUCT_NAME
ARG REPOSITORY
ARG BUILD_DATE
ARG ORAS_CLI_VERSION

LABEL \
    adp.commit=$COMMIT \
    adp.repository=$REPOSITORY \
    adp.app.version=$PRODUCT_VERSION \
    com.ericsson.base-image.product-name="Common Base OS SLES IMAGE" \
    com.ericsson.base-image.product-number="CXC2012032" \
    com.ericsson.base-image.product-version="${CBO_VERSION}" \
    com.ericsson.product-number="$PRODUCT_NUMBER" \
    com.ericsson.product-name="$PRODUCT_NAME" \
    com.ericsson.product-revision="$PRODUCT_VERSION" \
    com.ericsson.product-3pp-app2-name="Helm" \
    com.ericsson.product-3pp-app2-version="${HELM_VERSION}" \
    com.ericsson.product-3pp-app1-name="Oras" \
    com.ericsson.product-3pp-app1-version="${ORAS_CLI_VERSION}" \
    org.opencontainers.image.title="$PRODUCT_NAME" \
    org.opencontainers.image.created="$BUILD_DATE" \
    org.opencontainers.image.revision="$COMMIT" \
    org.opencontainers.image.vendor="Ericsson" \
    org.opencontainers.image.version="$PRODUCT_VERSION"

RUN echo "${CONTAINER_ID}:x:${CONTAINER_ID}:" >> /etc/group \
    && echo "${CONTAINER_ID}:x:${CONTAINER_ID}:${CONTAINER_ID}:An identity for RA CNAM components:/home/${CONTAINER_ID}:/bin/false" >> /etc/passwd \
    && echo "${CONTAINER_ID}:!::0:::::" >> /etc/shadow \
    && mkdir /logs /home/${CONTAINER_ID} && chown $CONTAINER_ID:$CONTAINER_ID /home/${CONTAINER_ID} /logs \
    && zypper addrepo --gpgcheck-strict -f https://${CBO_REPO}${CBO_VERSION} CBO_ENV \
    && zypper --gpg-auto-import-keys refresh \
    && zypper refresh \
    && zypper install -l -y java-11-openjdk \
                            unzip \
                            git-core \
    && zypper rr CBO_ENV \
    && zypper clean --all

COPY --chown=${CONTAINER_ID}:${CONTAINER_ID} --from=packages_base /oras /usr/local/bin/
COPY --chown=${CONTAINER_ID}:${CONTAINER_ID} --from=packages_base /linux-amd64/helm /usr/local/bin/helm
COPY --chown=${CONTAINER_ID}:${CONTAINER_ID} ./eric-cnam-onboarding-server/target/eric-cnam-onboarding-server.jar /
COPY --from=0 /script/ /

ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 8080
USER ${CONTAINER_ID}

CMD [ "java" \
   , "-Xlog:age*=debug" \
   , "-Djava.security.egd=file:/dev/./urandom" \
   , "-jar" \
   , "-Dserver.port=8080" \
   , "/eric-cnam-onboarding-server.jar" \
]

