#!/bin/bash
cd ../MainApp/
./gradlew lintColgateDebug lintKolibreeDebug -Dlint.baselines.continue=true
cd ..SDK/
./gradlew lintColgateDebug lintDebug -Dlint.baselines.continue=true

