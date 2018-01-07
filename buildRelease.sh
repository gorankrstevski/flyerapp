#!/bin/bash
export PATH=$PATH:/Users/goran/.gradle/wrapper/dists/gradle-4.1-all/bzyivzo6n839fup2jbap0tjew/gradle-4.1/bin
export ANDROID_HOME=/Users/goran/development/android-sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
/Users/goran/.gradle/wrapper/dists/gradle-4.1-all/bzyivzo6n839fup2jbap0tjew/gradle-4.1/bin/gradle wrapper clean
/Users/goran/.gradle/wrapper/dists/gradle-4.1-all/bzyivzo6n839fup2jbap0tjew/gradle-4.1/bin/gradle wrapper :cuponsapk:assembleLexpressDebug
/Users/goran/.gradle/wrapper/dists/gradle-4.1-all/bzyivzo6n839fup2jbap0tjew/gradle-4.1/bin/gradle wrapper :cuponsapk:assembleLexpressRelease
/Users/goran/.gradle/wrapper/dists/gradle-4.1-all/bzyivzo6n839fup2jbap0tjew/gradle-4.1/bin/gradle wrapper :cuponsapk:assembleBlackfridayDebug
/Users/goran/.gradle/wrapper/dists/gradle-4.1-all/bzyivzo6n839fup2jbap0tjew/gradle-4.1/bin/gradle wrapper :cuponsapk:assembleBlackfridayRelease

mkdir builds
cp ./cuponsapk/build/outputs/apk/lexpress/debug/cuponsapk-lexpress-debug.apk ./builds/couponsapk-lexpress-debug.apk
cp ./cuponsapk/build/outputs/apk/lexpress/release/cuponsapk-lexpress-release.apk ./builds/couponsapk-lexpress-release.apk

cp ./cuponsapk/build/outputs/apk/blackfriday/debug/cuponsapk-blackfriday-debug.apk ./builds/couponsapk-blackfriday-debug.apk
cp ./cuponsapk/build/outputs/apk/blackfriday/release/cuponsapk-blackfriday-release.apk ./builds/couponsapk-blackfriday-release.apk

open ./builds