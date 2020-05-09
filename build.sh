#!/usr/bin/env bash
# brew install ktlint & pandoc for this to work
rm -rf docs epub
ktlint -F src/main/kotlin src/main/test
./gradlew build dokka
cd epub
sh create_ebook.sh
cp book.epub ..
