package com.example.RaspBerryProject;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

import java.util.List;


public class DataAcquisition {
    private ArrayList<Model> theList;
    private RequestQueue myQueue;
    private LineChart mChart;
    private int mode;
    private String desc;
    private List<Entry> finalValues;

    DataAcquisition(Context context, LineChart lineChart) {
        myQueue = Volley.newRequestQueue(context);
        mChart = lineChart;
    }

    //Setting the mode indicates the data set
    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    //Volley request that adds data to object Model
    private void parsedData() {
        String url = "http://users.du.se/~h17najse/Android/assignment/fileReader.php";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                theList = new ArrayList<>();
                try {
                    for (int i = 0; i < response.length(); i++) {
                        Model model = new Model();
                        JSONObject jsonObject = response.getJSONObject(i);
                        model.setDate(jsonObject.getString("Date"));
                        model.setTime(jsonObject.getString("Time"));
                        model.setTemp(jsonObject.getString("Temp"));
                        model.setHumidity(jsonObject.getString("Humidity"));
                        model.setPressure(jsonObject.getString("Pressure"));
                        if (!model.getDate().equalsIgnoreCase("null")) {
                            Log.d("Volley", "Data added");
                            theList.add(model);
                        }
                    }
                    addEntry(theList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        myQueue.add(request);
    }

    private void addEntry(ArrayList<Model> list) {
        finalValues = null;
        final List<Entry> tempValues = new ArrayList<>();
        final List<Entry> pressureValues = new ArrayList<>();
        final String[] xLabels = new String[list.size()];

        //listFixing manipulates the list so we only get last 5 values from the volley data
        //and adds said data to appropriate lists
        listFixing(list, tempValues, 1);
        listFixing(list, pressureValues, 2);
        //labelFixing modifies the xAxis values
        labelFixing(list, xLabels);
        //Styling the xAxis
        xAxisFixing(xLabels);

        //Dataset calls on dataSetFixing which styles and checks which mode the user has set
        //the app on, changes in real time
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSetFixing(tempValues, pressureValues));
        LineData tempData = new LineData(dataSets);

        //More styling
        chartStyling(tempData);
        //We set our data to the chart and notify changes that happen in real time
        //so the chart changes in real time
        mChart.setData(tempData);
        mChart.notifyDataSetChanged();
        mChart.invalidate();
    }

    //We fix our lists by adding the correct values to the appropriate list
    private void listFixing(ArrayList<Model> list, List<Entry> valueList, int id) {
        for (int i = list.size() - 5; i < list.size(); i++) {
            Model mod = list.get(i);
            float tempY = Float.parseFloat(mod.getTemp());
            float pressureY = Float.parseFloat(mod.getPressure());
            if (id == 1) {
                valueList.add(new Entry(i, tempY));
            } else if (id == 2) {
                valueList.add(new Entry(i, pressureY));
            }
        }
    }

    //We style our chart a bit more here
    private void chartStyling(LineData myData) {
        myData.setValueTextSize(13);
        mChart.setKeepPositionOnRotation(false);
        mChart.setDrawGridBackground(false);
        mChart.getXAxis().setDrawGridLines(false);
        Description description = new Description();
        description.setText(desc);
        description.setTextColor(Color.BLACK);
        description.setTextSize(14);
        mChart.setDescription(description);
        mChart.setBorderColor(Color.RED);
    }

    //We fix the labels for the chart, we wanted time to be shown
    private void labelFixing(ArrayList<Model> list, String[] xLabels) {
        for (int i = list.size() - 5; i < xLabels.length; i++) {
            xLabels[i] = list.get(i).getTime();
        }
    }

    //Here we fix the xAxis labels for the chart
    private void xAxisFixing(final String[] xLabels) {
        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(true);
        //ValueFormatters formats the xAxis value from a regular int to different labels
        xAxis.setValueFormatter(new IndexAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return xLabels[(int) value];
            }
        });
    }

    //Depending on the checked radio button, the data alternates between temperature and pressure
    private LineDataSet dataSetFixing(List<Entry> tempValues, List<Entry> pressureValues) {
        if (mode == 1) {
            finalValues = tempValues;
        } else if (mode == 0) {
            finalValues = pressureValues;
        }

        LineDataSet tempSetLine = new LineDataSet(finalValues, "Set 1");
        tempSetLine.setLineWidth(4f);
        tempSetLine.enableDashedLine(10f, 5f, 0f);
        tempSetLine.setCircleColor(Color.BLACK);

        if (mode == 1) {
            tempSetLine.setColor(Color.RED);
        } else if (mode == 0) {
            tempSetLine.setColor(Color.BLUE);
        }
        return tempSetLine;
    }

    //Handler runs this application for a 1000 loop every 5 seconds
    //which is the time it takes for the sensor to post new data
    void handler() {
        Handler handler = new Handler();
        for (int i = 0; i < 1000; i++) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    parsedData();
                }
            }, 5000);
        }
    }
}
