#!/bin/bash

# Decrypt config & key files
openssl aes-256-cbc -K $encrypted_b31e40152ed3_key -iv $encrypted_b31e40152ed3_iv -in secrets.tar.xz.enc -out secrets.tar.xz -d
tar xvf ./secrets.tar.xz

# Clean up browser path
rm -rf firefox/

# Authorise GCloud SDK
gcloud auth activate-service-account teammates-haky@appspot.gserviceaccount.com --key-file=gae.json --project=teammates-haky

# Run Gradle deployment task
"./gradlew appengineDeployAll"