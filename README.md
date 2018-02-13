#Introduction
Printer is an easy to use - fully featured logging library.
* use class names automatically as tags
* use custom tags
* log on file

Power on the printer and forget about everything, just print logs.

# Importing
The library can be imported by adding a dependency in the Gradle build file.
```
implementation 'com.stetel:printer:1.0.0'
```
Check [Stetel Maven repository website](https://maven.stetel.com/help.jsp) for more information.

# Initializing
First you have to power on the printer. This is better done as soon as possible in th Application class, otherwise Android can destroy the static references so you printer will be powered off.
``` 
Printer.powerOn()
```
This is the base method which replicates the standard Android Log class, but there are others which accepts parameters.
Take a look at Javadocs to see what's best for you.

# Usage
Printer replicates the logging methods of the Android Log class to print INFO, WARNING, ERROR and DEBUG messages.
You can pass every objects, even exceptions, to the methods and they will be printed as a concatenated string message.
```
Printer.i("Hello world!");

Printer.i("Current date is ", new Date());

List<String> stringList = new ArrayList<>();
stringList.add("one");
stringList.add("two");
stringList.add("three");
Printer.i("List: ", stringList);

try {
    int foo = 2/0;
} catch (Exception e) {
    Printer.e("Exception: ", e);
}
```