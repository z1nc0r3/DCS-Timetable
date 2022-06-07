package com.zincore.cstimetable;

import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements TabLayoutMediator.TabConfigurationStrategy {

    private final ArrayList<String> monday = new ArrayList<>();
    private final ArrayList<String> tuesday = new ArrayList<>();
    private final ArrayList<String> wednesday = new ArrayList<>();
    private final ArrayList<String> thursday = new ArrayList<>();
    private final ArrayList<String> friday = new ArrayList<>();
    private ViewPager2 viewPager2;
    private ArrayList<String> titles;
    private float height;
    private float density;
    private String today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager);
        titles = new ArrayList<>();
        titles.add("Monday");
        titles.add("Tuesday");
        titles.add("Wednesday");
        titles.add("Thursday");
        titles.add("Friday");

        LoadData("data.json");

        Date date = Calendar.getInstance().getTime();
        today = date.toString().substring(0, 3);

        height = Resources.getSystem().getDisplayMetrics().heightPixels - getExtraHeight();
        density = getResources().getDisplayMetrics().density;

        setTabLayoutAdapter();
        new TabLayoutMediator(tabLayout, viewPager2, this).attach();

        TabLayout.Tab tab = tabLayout.getTabAt(seletectToday());
        tab.select();

        tabLayout.getLayoutParams().height = (int) (40 * density);

        viewPager2.setOffscreenPageLimit(5);
    }

    public void setTabLayoutAdapter() {
        TabLayoutAdapter tabLayoutAdapter = new TabLayoutAdapter(this);
        ArrayList<Fragment> fragmentArrayList = new ArrayList<>();

        JSONObject color = LoadColors("colors.json");

        fragmentArrayList.add(new TodayFragment(monday, height, color, "Mon"));
        fragmentArrayList.add(new TodayFragment(tuesday, height, color, "Tue"));
        fragmentArrayList.add(new TodayFragment(wednesday, height, color, "Wed"));
        fragmentArrayList.add(new TodayFragment(thursday, height, color, "Thu"));
        fragmentArrayList.add(new TodayFragment(friday, height, color, "Fri"));

        tabLayoutAdapter.setData(fragmentArrayList);

        viewPager2.setAdapter(tabLayoutAdapter);
    }

    @Override
    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
        tab.setText(titles.get(position));
    }

    public int seletectToday() {
        int dateIndex = 0;

        switch (today) {
            case ("Tue"):
                dateIndex = 1;
                break;
            case ("Wed"):
                dateIndex = 2;
                break;
            case ("Thu"):
                dateIndex = 3;
                break;
            case ("Fri"):
                dateIndex = 4;
                break;
        }

        return dateIndex;
    }

    public void LoadData(String fileName) {
        String data = "";

        try {
            InputStream stream = getAssets().open(fileName);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();

            data = new String(buffer);

        } catch (IOException e) {
            Toast.makeText(this, "File reading error.", Toast.LENGTH_SHORT).show();
            finish();
        }

        try {
            JSONArray timetable = new JSONObject(data).getJSONArray("timetable");
            JSONArray mondayData = timetable.getJSONObject(0).getJSONArray("monday");
            JSONArray tuesdayData = timetable.getJSONObject(0).getJSONArray("tuesday");
            JSONArray wednesdayData = timetable.getJSONObject(0).getJSONArray("wednesday");
            JSONArray thursdayData = timetable.getJSONObject(0).getJSONArray("thursday");
            JSONArray fridayData = timetable.getJSONObject(0).getJSONArray("friday");

            for (int i = 0; i < 10; i++) {
                monday.add(mondayData.getJSONObject(i).getString("subject"));
                tuesday.add(tuesdayData.getJSONObject(i).getString("subject"));
                wednesday.add(wednesdayData.getJSONObject(i).getString("subject"));
                thursday.add(thursdayData.getJSONObject(i).getString("subject"));
                friday.add(fridayData.getJSONObject(i).getString("subject"));
            }

        } catch (Exception e) {
            Toast.makeText(this, "JSON parse Error! Please contact the developer.", Toast.LENGTH_SHORT).show();
        }
    }

    public JSONObject LoadColors(String fileName) {
        String data = "";
        JSONObject color = null;

        try {
            InputStream stream = getAssets().open(fileName);

            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();

            data = new String(buffer);

        } catch (IOException e) {
            Toast.makeText(this, "File reading error.", Toast.LENGTH_SHORT).show();
            finish();
        }

        try {
            color = new JSONObject(data);
        } catch (Exception e) {
            Toast.makeText(this, "JSON ", Toast.LENGTH_SHORT).show();
        }

        return color;
    }

    private float getExtraHeight() {
        int statusBarHeight;
        float navigationBarHeight = 0;

        Resources myResources = getResources();
        int idStatusBarHeight = myResources.getIdentifier("status_bar_height", "dimen", "android");
        statusBarHeight = getResources().getDimensionPixelSize(idStatusBarHeight);

        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(resourceId) / getResources().getDisplayMetrics().density;
        }

        return statusBarHeight + navigationBarHeight + (50 * density);
    }
}