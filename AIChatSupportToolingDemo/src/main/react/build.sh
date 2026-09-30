#!/bin/sh
set -e
cd reactfrontend
npm ci
npm run build
rm -rf ../../resources/static/*
cp -r ./dist/* ../../resources/static/
if [ ! -e "../../resources/static/index.html" ]; then
    echo "ERROR: index.html is missing in resources!"
    exit 1
fi
cd ..
