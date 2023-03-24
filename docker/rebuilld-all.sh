#!/bin/sh
echo "Rebuilding Started..."

#docker image prune -a
cd /Users/frank.tsai/dev/proservcloud/ds-proserv-cloud
mvn clean install -DskipTests
docker-compose --profile base -f ../docker/docker-compose.yml build
../docker/push-remote.sh

echo "Rebuild Completed"
