#!/bin/sh

set -e

if [ -f tmp/pids/server.pid ]; then
  rm tmp/pids/server.pid
fi

echo "Waiting for Mysql to start..."
while ! nc -z $DEVELOPMENT_MYSQL_DB_HOST 3306; do sleep 0.1; done
echo "Mysql is up"

# echo "Waiting for Redis to start..."
# while ! nc -z redis 6379; do sleep 0.1; done
# echo "Redis is up - execuring command"

echo "bundle exec $@"
exec bundle exec "$@"
