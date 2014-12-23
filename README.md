PointSDK-SampleApp-Android
==========================
If you are working with ADT in Eclipse, you can import the project source code as an Android project into Eclipse directly. After import you have to import the Google Play Service library project, Android Support-V7 library into the Eclipse and make references to these two libraries. Finally,  don't forget to put the Bluedot PointSDK jar to the lib folder. 

If you would like to use Android Studio, here is the step you should follow:

1. Make sure you are using the latest Android Studio (Version 1.0+)
2. Import the Point App project using Android Studio import wizard.
3. Click next all the way down until finish. 
4. Wait for all the process comlete, you will see complialtion errors due to missing Android dependencies.
5. Open the Gradle config file in app/build.gradle
6. Make sure the content of the file looks like this:

apply plugin: 'android'

android {

    compileSdkVersion "Google Inc.:Google APIs:19"

    buildToolsVersion "20.0.0"

    defaultConfig {

        applicationId "com.bluedotinnovation.android.pointapp"

        minSdkVersion 9

        targetSdkVersion 19
    }

    buildTypes {

        release {

            runProguard false

            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'
        }

    }

}
dependencies {

    compile fileTree(include: '*.jar', dir: 'libs')

    compile 'com.google.android.gms:play-services:4.2.42'

    compile 'com.android.support:appcompat-v7:19.0.+'
}

Perform a gradle sync and all your problems will be solved. Enjoy!
