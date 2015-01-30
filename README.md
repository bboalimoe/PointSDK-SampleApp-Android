PointSDK-SampleApp-Android
==========================

Eclipse based development environment
-------------------------------------
If you are working with ADT in Eclipse, the project source code can be imported as an Android project into Eclipse directly. After the project is successfully imported, then import the  Google Play Service library project and Android Support-V7 library into the Eclipse and make references to these two libraries. 

Finally, download the latest version of the Bluedot Point SDK jar to the lib folder. The Bluedot Point SDK can be downloaded by registering with the back-end at

https://www.pointaccess.bluedot.com.au/pointaccess-v1/html/customer/basic-registration-form.html

Android Studio based development environment
--------------------------------------------

If your development environment is Android Studio, please follow the steps below to import the project:

1. Make sure you are using the latest version of Android Studio (Version 1.0+)
2. Import the project using Android Studio import wizard
3. Click Next until the end of the import wizard 
4. Wait for Android Studio to complete the import, there will be compilation errors due to missing Android dependencies, just follow the rest of the instructions to correct the errors
5. Open the Gradle config file located in app/build.gradle
6. Make sure the content of the file is as below:

apply plugin: 'android'

android {

    compileSdkVersion "Google Inc.:Google APIs:21"
    buildToolsVersion "21"
    defaultConfig {
        applicationId "com.bluedotinnovation.android.pointapp"
        minSdkVersion 9
        targetSdkVersion 21
    }
    buildTypes {
        release {
            runProguard false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'
        }
    }
    dependencies {
        compile fileTree(include: '*.jar', dir: 'libs')
        compile 'com.google.android.gms:play-services:4.2.42'
        compile 'com.android.support:appcompat-v7:19.0.+'
    }
}

Perform a Gradle sync and you are all set to go.
