package com.stetel.printer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Printer allows to easily print log messages on Logcat and files.
 * You must power on the printer to print messages, otherwise they are discarded.
 * The class is thread-safe.
 */
public class Printer {
    private final static int LINE_MAX_CHARS = 2000;
    private final static long MAX_FILE_SIZE = 10485760; // 10 MB
    private static final SimpleDateFormat LOG_DATE_FORMAT =
            new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
    private static final String PRINTER_TAG = "Printer";
    private static UncaughtExceptionHandler defaultCrashHandler;
    private static File logFile;
    private static String tag;
    private static boolean poweredOn;
    private static final Object LOCK = new Object();

    /**
     * Power on the printer.<br>
     * Automatically uses the class name as the tag and does not write messages in a log file.<br>
     * Prints a warning message in case Printer cannot log uncaught exceptions due to a security
     * manager.<br>
     * <br>
     * Notes:
     * <ul>
     *  <li>Should be invoked in the Application class, otherwise the Android system could
     *      power off your printer.</li>
     * </ul>
     */
    public static void powerOn() {
        Printer.powerOn(null, null, null);
    }

    /**
     * Power on the printer.<br>
     * Automatically uses the class name as the tag and writes messages in a log file in the
     * external cache folder of the app.<br>
     * File location: <i>/sdcard/Android/data/APP_PACKAGE_NAME/cache/Printer.log</i><br>
     * Prints a warning message in case Printer cannot log uncaught exceptions due to a security
     * manager or cannot write the log file.<br>
     * <br>
     * Notes:
     * <ul>
     *  <li>Should be invoked in the Application class, otherwise the Android system could
     *      power off your printer.</li>
     *  <li>Writing to a file is a slow operation which can affect optimization tests</li>
     * </ul>
     *
     * @param context Any context
     */
    public static void powerOn(Context context) {
        Printer.powerOn(null, context, null);
    }

    /**
     * Power on the printer.<br>
     * Automatically uses the class name as the tag and writes messages in a log file in the
     * specified location.<br>
     * Prints a warning message in case Printer cannot log uncaught exceptions due to a security
     * manager or cannot write the log file.<br>
     * <br>
     * Notes:
     * <ul>
     *  <li>Should be invoked in the Application class, otherwise the Android system could
     *      power off your printer.</li>
     *  <li>Writing to a file is a slow operation which can affect optimization tests</li>
     *  <li>Printer is not including any permissions</li>
     * </ul>
     *
     * @param context Any context
     * @param filepath Absolute path location where to write the log file
     */
    public static void powerOn(Context context, String filepath) {
        Printer.powerOn(null, context, filepath);
    }

    /**
     * Power on the printer.<br>
     * Uses a custom tag and does not write messages in a log file.<br>
     * Prints a warning message in case Printer cannot log uncaught exceptions due to a security
     * manager.<br>
     * <br>
     * Notes:
     * <ul>
     *  <li>Should be invoked in the Application class, otherwise the Android system could
     *      power off your printer.</li>
     * </ul>
     *
     * @param tag Tag to be used for all messages
     */
    public static void powerOn(String tag) {
        Printer.powerOn(tag, null, null);
    }

    /**
     * Power on the printer.<br>
     * Uses a custom tag and writes messages in a log file in the external cache folder of the app.<br>
     * The file name is the same as the tag.<br>
     * File location: <i>/sdcard/Android/data/APP_PACKAGE_NAME/cache/TAG.log</i><br>
     * Prints a warning message in case Printer cannot log uncaught exceptions due to a security
     * manager or cannot write the log file.<br>
     * <br>
     * Notes:
     * <ul>
     *  <li>Should be invoked in the Application class, otherwise the Android system could
     *      power off your printer.</li>
     *  <li>Writing to a file is a slow operation which can affect optimization tests</li>
     * </ul>
     *
     * @param tag Tag to be used for all messages and for the log file name
     * @param context Any context
     */
    public static void powerOn(String tag, Context context) {
        Printer.powerOn(tag, context, null);
    }

    /**
     * Power on the printer.<br>
     * Uses a custom tag and writes messages in a log file in the specified location.<br>
     * Prints a warning message in case Printer cannot log uncaught exceptions due to a security
     * manager or cannot write the log file.<br>
     * <br>
     * Notes:
     * <ul>
     *  <li>Should be invoked in the Application class, otherwise the Android system could
     *      power off your printer.</li>
     *  <li>Writing to a file is a slow operation which can affect optimization tests</li>
     *  <li>Printer is not including any permissions</li>
     * </ul>
     *
     * @param tag Tag to be used for all messages and for the log file name
     * @param context Any context
     * @param filepath Absolute path location where to write the log file
     */
    public static void powerOn(String tag, Context context, String filepath) {
        synchronized (LOCK) {
            Printer.defaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
            Printer.tag = tag;
            // if context is present, but not the filepath, we use the default cache location
            if (TextUtils.isEmpty(filepath)) {
                if (context != null) {
                    File externalCacheDir = context.getExternalCacheDir();
                    if (externalCacheDir == null) {
                        Log.w(PRINTER_TAG, "Printer - Default location for log file not available");
                    } else {
                        String defaultPath = externalCacheDir.getAbsolutePath() + "/" +
                                (TextUtils.isEmpty(tag) ? PRINTER_TAG : tag) + ".log";
                        Printer.logFile = new File(defaultPath);
                    }
                }
            } else {
                Printer.logFile = new File(filepath);
            }
            if (Printer.logFile != null) {
                if (!Printer.logFile.exists()) {
                    // creates a new file and updates the media scanner
                    createLogFile("");
                    if (context != null) {
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(Printer.logFile)));
                    }
                } else if (Printer.logFile.length() > MAX_FILE_SIZE) {
                    // creates a new file if exceeding max size
                    createLogFile("...\n");
                }
            }
            // set crash handler to log app crashes
            try {
                Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread thread, Throwable throwable) {
                        e("FATAL EXCEPTION\n", throwable);
                        if (defaultCrashHandler != null) {
                            defaultCrashHandler.uncaughtException(thread, throwable);
                        }
                    }
                });
            } catch (SecurityException e) {
                Log.w(PRINTER_TAG,"Printer - Cannot log fatal exceptions");
            }
            Printer.poweredOn = true;
        }
    }

    /**
     * Power off the printer.
     */
    public static void powerOff() {
        synchronized (LOCK) {
            Printer.poweredOn = false;
            if (Printer.defaultCrashHandler != null) {
                Thread.setDefaultUncaughtExceptionHandler(Printer.defaultCrashHandler);
            }
            Printer.defaultCrashHandler = null;
            Printer.tag = null;
            Printer.logFile = null;
        }
    }

    /**
     * Checks if the printer is on.<br>
     * Helpful if you need to write the results of heavy or unusual operations.
     *
     * @return Power status
     */
    public static boolean isPoweredOn() {
        synchronized (LOCK) {
            return poweredOn;
        }
    }

    /**
     * Prints an INFO message.<br>
     * Objects are concatenated as a string.
     *
     * @param objs Objects to print
     */
    public static void i(Object... objs) {
        msg(Log.INFO, objs);
    }

    /**
     * Prints a WARNING message.<br>
     * Objects are concatenated as a string.
     *
     * @param objs Objects to print
     */
    public static void w(Object... objs) {
        msg(Log.WARN, objs);
    }

    /**
     * Prints an ERROR message.<br>
     * Objects are concatenated as a string.
     *
     * @param objs Objects to print
     */
    public static void e(Object... objs) {
        msg(Log.ERROR, objs);
    }

    /**
     * Prints a DEBUG message.<br>
     * Objects are concatenated as a string.
     *
     * @param objs Objects to print
     */
    public static void d(Object... objs) {
        msg(Log.DEBUG, objs);
    }

    /**
     * Get the log file.
     *
     * Note this can be unavailable if the Printer is not powered on yet or
     * if the used powerOn() method does not save a log file.
     *
     * @return The current log file instance
     */
    public static File getLogFile() {
        return logFile;
    }

    /**
     * Create the log file and prints a message directly.<br>
     *
     * @param message Message to print
     */
    private static void createLogFile(String message) {
        BufferedWriter bufferWriter = null;
        try {
            bufferWriter = new BufferedWriter(new FileWriter(Printer.logFile, false));
            bufferWriter.write(message);
        } catch (Exception e) {
            Log.w(PRINTER_TAG,"Printer - Cannot create log file");
        } finally {
            if (bufferWriter!= null) {
                try {
                    bufferWriter.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    /**
     * Main method to print messages.<br>
     * Takes care of concatenating the objects and print the message on both the Logcat and the log
     * file.
     *
     * @param type Log type
     * @param objs Objects to print
     */
    private static void msg(int type, Object... objs) {
        synchronized (LOCK) {
            if (Printer.poweredOn) {
                StringBuilder message = new StringBuilder();
                if (objs != null) {
                    for (Object obj : objs) {
                        if (obj instanceof Throwable) {
                            message.append(Log.getStackTraceString((Throwable) obj));
                        } else {
                            message.append(obj);
                        }
                    }
                }
                String tag = TextUtils.isEmpty(Printer.tag) ?
                        Printer.getCurrentClassName() : Printer.tag;
                writeLogcat(type, tag, message.toString());
                writeFile(type, tag, message.toString());
            }
        }
    }

    /**
     * Splits the message if too long and writes the Logcat
     *
     * @param type Log type
     * @param tag Tag to use
     * @param message Message to print
     */
    private static void writeLogcat(int type, String tag, String message) {
        int len = message.length();
        for (int i = 0; i < len; i += LINE_MAX_CHARS) {
            Log.println(type, tag, message.substring(i, Math.min(len, i + LINE_MAX_CHARS)));
        }
    }

    /**
     * Writes the file if log file is enabled.
     *
     * @param type Log type
     * @param tag Tag to use
     * @param message Message to print
     */
    private static void writeFile(int type, String tag, String message) {
        if (Printer.logFile != null) {
            String messageTypePrefix = "";
            switch (type) {
                case Log.INFO:
                    messageTypePrefix = "I";
                    break;
                case Log.WARN:
                    messageTypePrefix = "W";
                    break;
                case Log.ERROR:
                    messageTypePrefix = "E";
                    break;
                case Log.DEBUG:
                    messageTypePrefix = "D";
                    break;
            }
            BufferedWriter bufferWriter = null;
            try {
                bufferWriter = new BufferedWriter(new FileWriter(Printer.logFile, true));
                bufferWriter.write(String.format("%s %s/%s: %s\n", LOG_DATE_FORMAT.format(new Date()),
                        messageTypePrefix, tag, message));
            } catch (Exception e) {
                // do nothing
            } finally {
                if (bufferWriter!= null) {
                    try {
                        bufferWriter.close();
                    } catch (IOException e) {
                        // do nothing
                    }
                }
            }
        }
    }

    /**
     * Retrieve the name of the class invoking a print method.
     *
     * @return Class name
     */
    private static String getCurrentClassName() {
        boolean printerClassFoundPrev = false;
        try {
            final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                boolean printerClassFound = stackTraceElement.getClassName().equals(Printer.class.getName());
                if (printerClassFoundPrev && !printerClassFound) {
                    try {
                        return stackTraceElement.getClassName()
                                .substring(stackTraceElement.getClassName().lastIndexOf(".") + 1);
                    } catch (Exception e) {
                        return stackTraceElement.getClassName();
                    }
                }
                printerClassFoundPrev = printerClassFound;
            }
        } catch (Exception e) {
            // do nothing
        }
        return PRINTER_TAG;
    }
}