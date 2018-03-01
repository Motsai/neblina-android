# Neblina&trade; SDK for Android
=========  

You can find more advance information on Neblina&trade; module, Neblina&trade; Development Kit, Neblina&trade; SDK and API, [here](http://docs.motsai.com).

### Prerequisite

* One or more Neblina&trade; module or Development Kit
* An Android Phone or Tablet with Bluetooth LE capability.
* Android 6.0 (Marshmallow) or above.
* Clone or download this repo.
* Android Studio 2.3 or later
* Android programming skills (Java and/or Kotlin)

### Functional check  

Download or Clone this repo using the command

```
$ git clone https://github.com/Motsai/neblina-android.git
```

Open the Tutorial1 project, compile and execute the App.  The initial screen will list all available Neblina&trade; devices found.  Select one of the Neblina&trade; that shows up.  It will automatically connect to the Neblina&trade; and begin streaming Quaternion.

Tutorial1 Screen Shot

![Tutorial1 Screen Shot](docs/images/Screenshot_Tutorial1.png)



### Need to know when creating new project

In order to use the Neblina&trade; API in a new project the source files Neblina.java, NeblinaDelegate.java & NebCmdItem.java are needed to import into the project.  One way to do this in the Android Studio is to add the source path into the 'build.grade (Module : app)' inside 'android {' as shown bellow.  

```
android {
  ...
  sourceSets {
      main.java.srcDirs += '../../src'
  }
}
```

To enable Bluetooth support in the App.  Permission settings are required.  The following are needed to be added into the AndroidManifest.xml of the project.

```
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```