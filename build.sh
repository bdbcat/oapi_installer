#!/bin/bash

##################################################################
#  CONFIGURE IT FOR YOUR SYSTEM !
##################################################################

export JAVABIN=~/jdk-10.0.1/bin/
export ANDROIDSDK=~/android-sdk
export PATH=$PATH:~/android-sdk/tools:
export KEYSTORE=keystore/my-release-key.keystore

##################################################################
#  PREPARE ENVIRONMENT
##################################################################

CD=`pwd`

rm -r bin
rm -r gen
mkdir bin
mkdir gen


##################################################################
#  SET VARS
##################################################################

# Set your application name
APP_NAME=MySpecialPlugin

# Define minimal Android revision
ANDROID_REV=android-22

#define the SDK build tools revision
ANDROID_TOOLS_REV=24.0.0

# Define aapt add command
ANDROID_AAPT_ADD="$ANDROIDSDK/build-tools/$ANDROID_TOOLS_REV/aapt add -k"

# Define aapt pack and generate resources command
ANDROID_AAPT_PACK="$ANDROIDSDK/build-tools/$ANDROID_TOOLS_REV/aapt package -v -f -I $ANDROIDSDK/platforms/$ANDROID_REV/android.jar"

# Define class file generator command
ANDROID_DX="$ANDROIDSDK/build-tools/$ANDROID_TOOLS_REV/dx --dex"

# Define Java compiler command
JAVAC="$JAVABIN/javac --release 7 -classpath $ANDROIDSDK/platforms/$ANDROID_REV/android.jar:./lib/android-support-v4-extras.jar"

JAVAC_BUILD="$JAVAC -sourcepath src -sourcepath gen -d bin"


##################################################################
#  PROCESS
##################################################################


# Generate R class and pack resources and assets into resources.ap_ file
$ANDROID_AAPT_PACK --auto-add-overlay -M "AndroidManifest.xml" -A "assets" -S "res" -m -J "gen" -F "bin/resources.ap_"


# Compile sources. All *.class files will be put into the bin folder
$JAVAC_BUILD ./src/org/opencpn/plugin/*.java

# Extract the "FileProvider.class" implementations from the support library
unzip -n lib/android-support-v4-extras.jar android/support/v4/content/FileProvider*.class -d ./bin


# Generate dex files with compiled and extracted Java classes
$ANDROID_DX --output="$CD/bin/classes.dex" $CD/bin

# Recources file need to be copied. This is needed for signing.
mv "$CD/bin/resources.ap_" "$CD/bin/$APP_NAME.ap_"

# Add generated classes.dex file into application package
$ANDROID_AAPT_ADD "$CD/bin/$APP_NAME.ap_" "$CD/bin/classes.dex"

# Create signed Android application from *.ap_ file. Output and Input files must be different.
"$JAVABIN/jarsigner" -keystore "$CD/$KEYSTORE" -storepass "password" -keypass "password" -signedjar "$CD/bin/$APP_NAME.apk" "$CD/bin/$APP_NAME.ap_" "alias_name" -sigalg MD5withRSA -digestalg SHA1

# Delete temp file
rm "bin/$APP_NAME.ap_"

