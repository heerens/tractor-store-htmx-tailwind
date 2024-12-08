#!/bin/sh

cd discover
./gradlew clean build
cd ..

cd checkout
./gradlew clean build
cd ..

