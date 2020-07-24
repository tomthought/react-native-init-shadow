#!/bin/bash

LEIN_SNAPSHOTS_IN_RELEASE=true lein uberjar
cp target/react-native-init-shadow.jar package/react-native-init-shadow.jar
mkdir -p package/resources/
cp -r resources/template/ package/resources/template
