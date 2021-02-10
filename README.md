# Introduction
An easy to use - fully featured logging library for Android.
- no more `TAG` vars, automatically use class names
- use custom tags
- log on file
- log app crashes (also on file)
- enable/disable logging easily to support dev/prod environments
- very long messages are split to avoid DDMS bug truncating the text

Power on the printer and forget about everything, just print logs.

# Importing
The library is available at MavenCentral and can be imported by adding a dependency in the Gradle build file.
```
implementation 'com.stetel:printer:1.1.1'
```

Check if you have the correct repositories in the root (not app) gradle file.
``` 
allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
``` 

# Initializing
First you have to power on the printer otherwise nothing will be logged. 

This is better done as soon as possible by extending the Application class, otherwise Android can destroy the static references.
``` 
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Printer.powerOn();
    }
}
```
This base method prepare the library to replicate the standard Android Log class, but you can customize it by using other constructors.
Take a look at Javadocs to see what's best for you.

Note: You can use `Printer.powerOff()` to disable the logging whenever you want. Call `Printer.powerOn()` to enable it again.

# Usage
Printer replicates the logging methods of the Android Log class to print INFO, WARNING, ERROR and DEBUG messages.
You can pass every objects, even exceptions, to the methods and they will be printed as a concatenated string message.
It uses `String.append()` internally so objects are logged by calling `Object.toString()`
```
Printer.i("Hello world!");
// Hello world!

Printer.i("Current date is ", new Date());
// Current date is Wed Jul 04 11:24:31 GMT+02:00 2018

List<String> stringList = new ArrayList<>();
stringList.add("one");
stringList.add("two");
stringList.add("three");
Printer.i("List: ", stringList);
// List: [one, two, three]

try {
    int foo = 2/0;
} catch (Exception e) {
    Printer.e("Error: ", e);
}
// Error: java.lang.ArithmeticException: divide by zero
//        at com.stetel.printerdemo.MainActivity.onCreate(MainActivity.java:37)
//        at android.app.Activity.performCreate(Activity.java:6722)
//        at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1119)
//        at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:2622)
//        at android.app.ActivityThread.handleLaunchActivity(ActivityThread.java:2730)
//        at android.app.ActivityThread.-wrap12(ActivityThread.java)
//        at android.app.ActivityThread$H.handleMessage(ActivityThread.java:1481)
//        at android.os.Handler.dispatchMessage(Handler.java:102)
//        at android.os.Looper.loop(Looper.java:154)
//        at android.app.ActivityThread.main(ActivityThread.java:6144)
//        at java.lang.reflect.Method.invoke(Native Method)
//        at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:886)
//        at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:776)
```
# Bonus
If you enabled Printer by using a `powerOn()` method which writes a log file, you can easily send an email containing the attachment to a specific address.
This should be useful when giving out the app to testers.
Before using the send method, you need to define a `FileProvider` in the AndroidManifest of the app unless you are already using one.

### Do not have a file provider
Create a new file called `file_paths.xml` under the folder app/src/res/xml (create the xml folder if not already present) and add the following:
```
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-path name="external_files" path="."/>
</paths>
```

Then edit the app's `AndroidManifest.xml` and add the following inside the application's section:
```
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/printer_provider_paths" />
</provider>
```

### Have a file provider
You only have to change you paths xml to include the folder with the log file.
If you have a generic implementation like `path="."` you don't have to do anything, otherwise you need to add a specific rule. 
For example if you use powerOn() with context without specifying the path, the log file will be created in the cache folder of the app. So the correct path to include in the xml will be:
```
<?xml version="1.0" encoding="utf-8"?>
<paths>
    ... other external paths ...
    
    <external-path name="cache" path="Android/data/APP_PACKAGE_NAME/cache" />
</paths>
```
(replace APP_PACKAGE_NAME)
 
### Sending the email
Once you have in place the paths xml and the provider in the manifest, you can simply use the following:
```
Printer.sendEmailWithLogFile(
    MainActivity.this,  // context
    BuildConfig.APPLICATION_ID + ".fileprovider",   // provider's authority
    "Select the app for sending the email", // choose app dialog's title
    "recipient@gmail.com",  // recipient's email address
    "Printer log file", // email's subject
    "Attached the log file generated by the Printer demo app."); // email's message
```
and a Share dialog will ask which mail app to use for sending the email.

# Permissions
Printer does not declare any permission. The log file created automatically by certain `Printer.powerOn()` constructors is created under the external cache folder of the app.
Starting from API 19, there is no need to declare the WRITE_EXTERNAL_STORAGE permission when writing in this folder.

It is responsibility of the main app to declare the permission and request it if the developer wants to write in another protected area.

Printer won't write anything on file if it does not have the permission, but will print on the log console as usual.

# Authors
- Lorenzo Lombardo - _Stetel Srl_ - www.stetel.com

# License
Printer is available under the [The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

# History

### 1.1.0
- Added method to get the log file
- Added way to easily send an email with the log file attached

### 1.0.1
- Changes to readme file
- Added lib to Maven Central

### 1.0.0 
- First release