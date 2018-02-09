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
import java.util.EnumSet;
import java.util.Locale;


/**
 *
 */
public class Printer {
    private final static int LINE_MAX_CHARS = 2000;
    private final static long MAX_FILE_SIZE = 10485760; // 10 MB
    private static final SimpleDateFormat LOG_DATE_FORMAT =
            new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
    private static final String LOGCAT_SPLIT_REGEX = String.format(Locale.US,
            "(?<=\\G.{%1$d})", LINE_MAX_CHARS);
    private static final String DEFAULT_TAG = "Printer";
    private static UncaughtExceptionHandler defaultCrashHandler;
    private static File logFile;
    private static String tag;
    private static boolean poweredOn;
    private static final Object LOCK = new Object();

    public static void powerOn(String tag) {
        Printer.powerOn(tag, null, null);
    }

    public static void powerOn(String tag, Context context) {
        Printer.powerOn(tag, context, null);
    }

    public static void powerOn(String tag, Context context, String filepath) {
        synchronized (LOCK) {
            Printer.defaultCrashHandler = Thread.getDefaultUncaughtExceptionHandler();
            Printer.tag = TextUtils.isEmpty(tag) ? DEFAULT_TAG : tag;
            // if context is present, but not the filepath, we use the default cache location
            if (TextUtils.isEmpty(filepath)) {
                if (context != null) {
                    File externalCacheDir = context.getExternalCacheDir();
                    if (externalCacheDir == null) {
                        writeLogcat(Log.WARN, "Printer - Default location for log file not available");
                    } else {
                        Printer.logFile = new File(externalCacheDir.getAbsolutePath() + "/" + Printer.tag + ".log");
                    }
                }
            } else {
                Printer.logFile = new File(filepath);
            }
            if (Printer.logFile != null) {
                if (!Printer.logFile.exists()) { // creates a new file and updates the media scanner
                    createLogFile("");
                    if (context != null) {
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                Uri.fromFile(Printer.logFile)));
                    }
                } else if (Printer.logFile.length() > MAX_FILE_SIZE) { // creates a new file if exceeding max size
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
                writeLogcat(Log.WARN, "Printer - Cannot log fatal exceptions");
            }
            Printer.poweredOn = true;
        }
    }

    private static void createLogFile(String message) {
        BufferedWriter bufferWriter = null;
        try {
            bufferWriter = new BufferedWriter(new FileWriter(Printer.logFile, false));
            bufferWriter.write(message);
        } catch (Exception e) {
            writeLogcat(Log.WARN, "Printer - Cannot create log file");
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

    public static void powerOff() {
        synchronized (LOCK) {
            Printer.poweredOn = false;
            Thread.setDefaultUncaughtExceptionHandler(Printer.defaultCrashHandler);
            Printer.defaultCrashHandler = null;
            Printer.tag = null;
            Printer.logFile = null;
        }
    }

    public static boolean isPoweredOn() {
        synchronized (LOCK) {
            return poweredOn;
        }
    }

    public static void i(Object... objs) {
        msg(Log.INFO, objs);
    }

    public static void w(Object... objs) {
        msg(Log.WARN, objs);
    }

    public static void e(Object... objs) {
        msg(Log.ERROR, objs);
    }

    public static void d(Object... objs) {
        msg(Log.DEBUG, objs);
    }

    private static void msg(int type, Object... objs) {
        synchronized (LOCK) {
            if (Printer.poweredOn) {
                StringBuilder message = new StringBuilder(objs.length);
                for (Object obj : objs) {
                    if (obj instanceof Throwable) {
                        message.append(Log.getStackTraceString((Throwable) obj));
                    } else {
                        message.append(obj);
                    }
                }
                writeLogcat(type, message.toString());
                writeFile(type, message.toString());
            }
        }
    }

    private static void writeLogcat(int type, String message) {
        for (String line : message.split(LOGCAT_SPLIT_REGEX)) {
            Log.println(type, tag, line);
        }
    }

    private static void writeFile(int type, String message) {
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
}