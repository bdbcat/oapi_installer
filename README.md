# oapi_installer
Simple Plugin installer app for Opencpn on Android


This Project presents a skeleton for creating a simple Plugin installer app for OpenCPN for Android.
By adapting this skeleton as described below, you may build an APK that may be instlled and run on the Android device.
After the user runs the installer, the configured OpenCPN Plugin will be available for user access for OpenCPN.

The instructions assume that you have created and built an ARMHF compatible Plugin, and have a "libxxx_pi.so" file ready to be installed.


1.  Reference web page:
    https://www.apriorit.com/dev-blog/233-how-to-build-apk-file-from-command-line

2.  Download prerequisites:

    a.  Java JDK
            http://www.oracle.com/technetwork/java/javase/downloads/index.html

            Using Version Java SE 10.0.1 (May, 2018)

            Choose the location in which to install JDK.  Probably easiest to install in user home directory, so no special permissions are required.
            Follow the clear Oracle installation instructions.

            $cd ~
            $tar zxvf jdk-10.0.1_linux-x64_bin.tar.gz

    b.  Android SDK
            http://developer.android.com/sdk/index.html

            We want the "command line tools", without Android Studio.  Download the zip file.
            This will be something like:  sdk-tools-linux-xxxxxxx.zip

            $cd ~
            $mkdir android-sdk
            $cd android-sdk
            $unzip sdk-tools-linux-3859397.zip

            Now we need to download the correct SDK contents for the target build version of our plugin, plus related toolsets.
            We can use the CLI version of the sdkmanager.
            Note that the CLI sdkmanager does not give much feedback while download is proceeding.
            You may like to monitor your network interface to see progress.

            $cd ~/android-sdk
            $tools/bin/sdkmanager "platforms;android-22"
            $tools/bin/sdkmanager "build-tools;24.0.0"
            $tools/bin/sdkmanager "platform-tools"



3.  Create a Private Key for signing the application.

            Move to the root of this project on you local file system.

            Use the java keytool to build a keystore file.

            $~/jdk-10.0.1/bin/keytool  -genkey -v -keystore  my-release-key.keystore -alias alias_name  -keyalg RSA -keysize 2048  -validity 10000

            Answer the questions.
            Choose a good password.  In the attached script (build.sh), I have used "password".  This is a bad choice.  Do better.

            This results in a file called "my-release-key.keystore" in the project root directory.
            Move that file to a folder called "keystore".


4.  Prepare the source tree
            This Project is a skeleton of a plugin installer build setup.
            You will probably want multiple copies of this Project if you are working with multiple plugins.
            The folders and files mentioned below are found in this Project as cloned, and may be initially empty.

            A.  Edit the "manifest.xml" file:
                i.   Change the key {package="org.opencpn.blahblah"} to a unique name for your plugin installer.  Something like "xxx_pi" is canonical.
                ii.  Change the key {android:authorities="org.opencpn.blahblah"} to the same unique name as (i).
                iii. Change the key {android:name="org.opencpn.blahblah.MainActivity" using the same unique name as (i).


            B.  Place your plugin "libxxx_pi.so" file in the root of the "assets" directory.

            C.  Place your localization files (*.mo), if any are required,  in the "assets/locale" directory, following the structure provided by the sample files.

            D.  Edit the first line of "/src/org/opencpn/plugin/MainActivity.java" to be:
                "package org.opencpn.blahblah;"
                substituting the name used in step 4.A.i above.

            E.  Edit the file "res/strings/values.xml"
                Change the key {    <string name="app_name">OpenCPN Plugin Installer</string>   }  to something specific and unique for your plugin.
                This string will be shown as the desktop icon title of the installer app on Android.


5.  Ready to build the APK

            A.  Edit the "build.sh" script, updating the following:

                i. Environment variables JAVABIN,ANDROIDSDK, and PATH.   Make them point to the items installed in Steps 1 and 2.
                ii. "APP_NAME"  Choose a name for your plugin installer APK, human readable.  e.g. "MySpecialPlugin".
                This will be the root name of the .APK, so spaces and special characters are not recommended.


            B.  Run the script
                $./build.sh

            C.  Find the created .APK in the "bin" directory.

