FROM armdockerhub.rnd.ericsson.se/registry:2

ARG REGISTRY_USER=testuser
ARG REGISTRY_PASS=testpass

RUN    apk --update upgrade   \
    && apk add apache2-utils  \
               bash

RUN htpasswd -cB -b /htpasswd $REGISTRY_USER $REGISTRY_PASS

ADD config.yml /etc/docker/registry/config.yml
ADD entrypoint.sh /usr/local/bin/

ENTRYPOINT ["entrypoint.sh"]
