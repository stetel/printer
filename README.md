# Introduction
Printer is an easy to use - fully featured logging library.
- no more `TAG` vars, automatically use class names
- use custom tags
- log on file
- log app crashes (also on file)
- enable/disable logging easily to support dev/prod environments
- very long messages are split to avoid DDMS bug truncating the text

Power on the printer and forget about everything, just print logs.

# Importing
The library is available in the JCenter repository and can be imported by adding a dependency in the Gradle build file.
```
implementation 'com.stetel:printer:1.0.0'
```

# Initializing
First you have to power on the printer otherwise nothing will be logged. This is better done as soon as possible in th Application class, otherwise Android can destroy the static references so you printer will be powered off.
``` 
Printer.powerOn()
```
This base method prepare the library to replicate the standard Android Log class, but you can customize it by using other constructors.
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

# Proguard
Printer does not need any rule.

# Authors
Lorenzo Lombardo - Stetel Srl - [www.stetel.com](www.stetel.com)

#License
This project is licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3.