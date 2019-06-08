#!/bin/sh

chown -R jetty:jetty /var/lib/jetty/logs/ /var/lib/jetty/output/
su jetty -c /docker-entrypoint.sh
