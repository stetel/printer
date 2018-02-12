package com.stetel.printerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.stetel.printer.Printer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Printer.i("Hello world!");

        Printer.i("Current date is ", new Date());

        List<String> stringList = new ArrayList<>();
        stringList.add("one");
        stringList.add("two");
        stringList.add("three");
        Printer.i("List: ", stringList);

        Map<String, Integer> stringIntegerMap = new HashMap<>();
        stringIntegerMap.put("one", 1);
        stringIntegerMap.put("two", 2);
        stringIntegerMap.put("three", 3);
        Printer.i("Map: ", stringIntegerMap);

        Printer.e("Exception: ", new Exception());
    }
}
