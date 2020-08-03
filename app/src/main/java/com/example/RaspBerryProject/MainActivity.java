package com.example.RaspBerryProject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;

public class MainActivity extends AppCompatActivity {
    private LineChart mpLineChart;
    private DataAcquisition data;
    public static boolean status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mpLineChart = findViewById(R.id.line_chart);
        data = new DataAcquisition(this, mpLineChart);
        data.setDesc("Pressure");
        AsyncTask runningTask = new Networking();
        runningTask.execute("");
        data.handler();
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_1:
                data.setMode(1);
                data.setDesc("Temperature");
                item.setChecked(true);
                return true;
            case R.id.menu_item_2:
                data.setMode(0);
                data.setDesc("Pressure");
                item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
