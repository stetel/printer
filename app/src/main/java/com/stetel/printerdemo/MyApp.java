package com.stetel.printerdemo;

import android.app.Application;
import android.content.Context;

import com.stetel.printer.Printer;

/**
 * Created by lorenzo on 09/02/18.
 */

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Printer.powerOn();
    }
}
