package com.zincore.cstimetable;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
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
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements TabLayoutMediator.TabConfigurationStrategy {

    private final ArrayList<String> monday = new ArrayList<>();
    private final ArrayList<String> tuesday = new ArrayList<>();
    private final ArrayList<String> wednesday = new ArrayList<>();
    private final ArrayList<String> thursday = new ArrayList<>();
    private final ArrayList<String> friday = new ArrayList<>();
    private ViewPager2 viewPager2;
    private ArrayList<String> titles;
    private TabLayout tabLayout;
    private float height;
    private float density;
    private String today;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.viewPager);
        titles = new ArrayList<>();
        titles.add("Monday");
        titles.add("Tuesday");
        titles.add("Wednesday");
        titles.add("Thursday");
        titles.add("Friday");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sharedPreferences = getSharedPreferences("timetable", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        try {
            copyAssetFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LoadData();
        setup();
        setData();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            UpdateData updateData = new UpdateData(swipeRefreshLayout, sharedPreferences);
            updateData.execute();
        });
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

    public int selectToday() {
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

    // read the data.json file from application's internal storage. This will be used to load the timetable data.
    public void LoadData() {
        String data = sharedPreferences.getString("data", "0");
        monday.clear();
        tuesday.clear();
        wednesday.clear();
        thursday.clear();
        friday.clear();

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
            showToast("Error loading data");
        }
    }

    // load the colors.json file from application's internal storage. This will be used to set the color of the timetable.
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
            finish();
            Toast.makeText(this, "Adding subject colors failed. Please restart the application.", Toast.LENGTH_SHORT).show();
        }

        try {
            color = new JSONObject(data);
        } catch (Exception e) {
            Toast.makeText(this, "Adding subject colors failed. Please restart the application.", Toast.LENGTH_SHORT).show();

        }

        return color;
    }

    // calculate the extra height of the status bar and action bar.
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

    // copy the data.json file from assets folder to application's internal storage.
    public void copyAssetFile() throws IOException {
        String data;

        if (sharedPreferences.getBoolean("firstTime", true)) {
            try {
                InputStream stream = getAssets().open("data.json");

                int size = stream.available();
                byte[] buffer = new byte[size];
                stream.read(buffer);
                stream.close();

                data = new String(buffer);
                editor.putString("data", data);
                editor.putBoolean("firstTime", false);

            } catch (IOException e) {
                Toast.makeText(this, "File reading error.", Toast.LENGTH_SHORT).show();
                editor.putBoolean("firstTime", true);
                finish();
            }

            editor.apply();
        }
    }

    public void setup() {
        Date date = Calendar.getInstance().getTime();
        today = date.toString().substring(0, 3);
        height = Resources.getSystem().getDisplayMetrics().heightPixels - getExtraHeight();
        density = getResources().getDisplayMetrics().density;

        tabLayout.getLayoutParams().height = (int) (40 * density);
        viewPager2.setOffscreenPageLimit(5);
    }

    public void setData() {
        setTabLayoutAdapter();
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, this);
        tabLayoutMediator.attach();
        TabLayout.Tab tab = tabLayout.getTabAt(selectToday());
        assert tab != null;
        tab.select();
    }

    class UpdateData extends AsyncTask<String, Void, String> {

        private final SwipeRefreshLayout swipeRefreshLayout;
        private final SharedPreferences.Editor editor;

        public UpdateData(SwipeRefreshLayout swipeRefreshLayout, SharedPreferences sharedPreferences) {
            this.swipeRefreshLayout = swipeRefreshLayout;
            editor = sharedPreferences.edit();
        }

        @Override
        protected String doInBackground(String... query) {
            String resBody;

            OkHttpClient client = new OkHttpClient();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = RequestBody.create("", mediaType);
            Request request = new Request.Builder()
                    .url("http://dcstimetable.rf.gd/data.json")
                    .method("POST", body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                resBody = Objects.requireNonNull(response.body()).string();
                response.close();

                return resBody;
            } catch (IOException | NullPointerException e) {
                return "error";
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (response.equals("error")) {
                showToast("Network error. Please check your internet connection.");
            } else {
                editor.putString("data", response);
                editor.apply();
                showToast("Timetable updated successfully.");
                LoadData();
                setData();
            }

            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void showToast(final String toast) {
        runOnUiThread(() -> Toast.makeText(this, toast, Toast.LENGTH_SHORT).show());
    }
}