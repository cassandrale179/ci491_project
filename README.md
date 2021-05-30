
# CI491 Project
<img width="900" alt="Screen Shot 2021-05-30 at 5 10 52 PM" src="https://user-images.githubusercontent.com/22923895/120120252-020c8080-c16a-11eb-8061-a0920ddf01a7.png">


## 1. Installation
- Please make sure you have Android Studio (latest version) 
- Simply clone this repository: 
```bash
git clone https://github.com/cassandrale179/ci491_project/ 
``` 
- Go to Android Studio, then go to `File > Open > ci491_project` and choose the Folder where you just clone the project. 
- Press the Green triangle button on Android Studio to run the project. 
- Source code is located in `app/src/main/java/com/example/caregiver/` 
- You should see the app look like this (https://user-images.githubusercontent.com/22923895/104358187-1530f380-54dc-11eb-866a-240e6062b162.png) 

## 2. Contributing
- Do NOT write code directly onto the main branch. Please set up a new branch in which the first two letters are your initial, and
the name after that is the feature you are working on.
```bash
git checkout -b ml-firebase
```
- Open a Pull Request, so we can code-review together and merge it to the main branch.


## 3. Tips and Resources
- If you want to use the blue color in the design, it is the @color/teal_700:
```java
android:backgroundTint="@color/teal_700"
```

- If you ever run into this error when writing to Firebase (com.google.firebase.database.DatabaseException: No properties to serialize found on class), make sure your class have public variables or getter/setter. (https://stackoverflow.com/questions/37743661/firebase-no-properties-to-serialize-found-on-class)
