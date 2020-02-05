#!/usr/bin/env bash
rm -rf docs epub
./gradlew build dokka
cd epub
sh create_ebook.sh
cp book.epub ..
