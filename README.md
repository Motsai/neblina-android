neblina-android
=======
 
### This repository is under heavy development.
 
### Need to know when creating new project
 
In order to import the API Neblina class into the project.  Add the source path location of the Neblina.java & NeblinaDelegate.java into the project.  One way to do it in the Android Studio is to add the following to the build.grade (Module : app) inside adroid {
     
```
android {
  ...
  sourceSets {
      main.java.srcDirs += '../../src'
  }
}
```

