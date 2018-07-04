# Introduction
An easy to use - fully featured logging library.
- no more `TAG` vars, automatically use class names
- use custom tags
- log on file
- log app crashes (also on file)
- enable/disable logging easily to support dev/prod environments
- very long messages are split to avoid DDMS bug truncating the text

Power on the printer and forget about everything, just print logs.

# Importing
The library is public and can be imported by adding a dependency in the Gradle build file.
```
implementation 'com.stetel:printer:1.0.0'
```

# Initializing
First you have to power on the printer otherwise nothing will be logged. 

This is better done as soon as possible in th Application class, otherwise Android can destroy the static references.
``` 
Printer.powerOn()
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

# Permissions
Printer does not declare any permission. The log file created automatically by certain `Printer.powerOn()` constructors is created under the external cache folder of the app.
Starting from API 19, there is no need to declare the WRITE_EXTERNAL_STORAGE permission when writing in this folder.

It is responsibility of the main app to declare the permission and request it if the developer wants to write in another protected area.

Printer won't write anything on file if it does not have the permission, but will print on the log console as usual.

# License
Printer is available under the [GNU - LGPL 3.0 license](https://www.gnu.org/licenses/lgpl-3.0.txt)
