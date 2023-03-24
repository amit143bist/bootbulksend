#!/bin/sh

docker images | awk '{print $1 ":" $2}' | grep ":1.0-GUITAR" | xargs docker rmi 
