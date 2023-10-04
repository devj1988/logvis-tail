#!/bin/bash
# setup dirs for volumes
mkdir -p ./vol/kafka
mkdir -p ./vol/syslog
mkdir -p ./vol/elastic
# build lola image
cd ../logvis-tail/server/Lola
./gradlew assemble
sudo docker build . -t devj2019/lola 
# setup containers
cd -
docker-compose up -d