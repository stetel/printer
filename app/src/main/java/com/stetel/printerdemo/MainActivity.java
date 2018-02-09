package com.stetel.printerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.stetel.printer.Printer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Printer.i("Hello world!");
    }
}
