FROM armdockerhub.rnd.ericsson.se/alpine:3.16.1

ARG GIT_SECURED=false
ARG GIT_USER=gitrepouser
ARG GIT_PASS=gitrepopass

RUN    apk --update upgrade                               \
    && apk add git                                        \
               git-daemon                                 \
               apache2-utils                              \
               bash                                       \
               nginx                                      \
               nginx-mod-stream                           \
               fcgiwrap                                   \
               spawn-fcgi                                 \
    && adduser git -h /var/lib/git -D                     \
    && adduser nginx git                                  \
    && git config --system http.receivepack true          \
    && git config --system http.uploadpack true           \
    && git config --system user.email "gitserver@localhost.com" \
    && git config --system user.name "Git Server"         \
    && ln -sf /dev/stdout /var/log/nginx/access.log       \
    && ln -sf /dev/stderr /var/log/nginx/error.log

ADD etc/ /etc


ADD entrypoint.sh /usr/local/bin/

ENTRYPOINT ["entrypoint.sh"]
