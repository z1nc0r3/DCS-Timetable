package com.zincore.cstimetable;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TodayFragment extends Fragment {

    private static final String FORMAT = "%02d:%02d";
    private CardView[] cards;
    private TextView[] subjects;
    private TextView[] times;
    private TextView subjectNow;
    private TextView timerNow;
    private float height;
    private int cardNowHeight;
    private String today;
    private String day;
    private JSONObject color;
    private ArrayList<String> table;
    private View view;

    public TodayFragment() {
        // Required empty public constructor
    }

    public TodayFragment(ArrayList<String> table, float height, JSONObject color, String today) {
        this.table = table;
        this.height = height;
        this.color = color;
        this.today = today;
        cardNowHeight = (int) (height - (height / 12) * 10);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_today, container, false);

        cards = new CardView[]{
                view.findViewById(R.id.sub_1),
                view.findViewById(R.id.sub_2),
                view.findViewById(R.id.sub_3),
                view.findViewById(R.id.sub_4),
                view.findViewById(R.id.sub_5),
                view.findViewById(R.id.sub_6),
                view.findViewById(R.id.sub_7),
                view.findViewById(R.id.sub_8),
                view.findViewById(R.id.sub_9),
                view.findViewById(R.id.sub_10)};

        subjects = new TextView[]{
                view.findViewById(R.id.am_7),
                view.findViewById(R.id.am_8),
                view.findViewById(R.id.am_9),
                view.findViewById(R.id.am_10),
                view.findViewById(R.id.am_11),
                view.findViewById(R.id.pm_12),
                view.findViewById(R.id.pm_1),
                view.findViewById(R.id.pm_2),
                view.findViewById(R.id.pm_3),
                view.findViewById(R.id.pm_4)};

        times = new TextView[]{
                view.findViewById(R.id.am_7_time),
                view.findViewById(R.id.am_8_time),
                view.findViewById(R.id.am_9_time),
                view.findViewById(R.id.am_10_time),
                view.findViewById(R.id.am_11_time),
                view.findViewById(R.id.pm_12_time),
                view.findViewById(R.id.pm_1_time),
                view.findViewById(R.id.pm_2_time),
                view.findViewById(R.id.pm_3_time),
                view.findViewById(R.id.pm_4_time)};

        for (int i = 0; i < 10; i++) {
            subjects[i].setText(table.get(i));
            subjects[i].setTextColor(Color.BLACK);
            subjects[i].setAlpha(0.9f);
            times[i].setTextColor(Color.parseColor("#303030"));

            try {
                cards[i].getBackground().setTint(Color.parseColor(color.getString(table.get(i))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Date date = Calendar.getInstance().getTime();
        day = date.toString().substring(0, 3);
        int hour = Integer.parseInt(String.valueOf(date).substring(11, 13));

        if ((hour < 7 || hour > 16) || !today.equals(day)) {
            setOverTime();
        } else {
            setProperTime(hour - 7);
        }

        return view;
    }

    public void setOverTime() {
        view.requestLayout();

        for (CardView card : cards) {
            card.getLayoutParams().height = (int) ((height / 11));
        }
    }

    public void setProperTime(int hour) {
        int now = 0;
        view.requestLayout();

        RelativeLayout.LayoutParams subjectLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        RelativeLayout.LayoutParams timerLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );

        subjectLayoutParams.setMargins(0, 20, 0, 40);
        timerLayoutParams.setMargins(0, (int) (cardNowHeight * 0.4), 0, 0);

        for (int i = 0; i < 10; i++) {
            CardView cardNow = cards[i];
            if (i == hour) {
                now = i;
                cardNow.getLayoutParams().height = cardNowHeight;

                subjectNow = new TextView(getContext());
                subjectNow.setLayoutParams(subjectLayoutParams);
                subjectNow.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.product_sans_bold));
                subjectNow.setText(table.get(i));
                subjectNow.setGravity(Gravity.CENTER_HORIZONTAL);
                subjectNow.setLetterSpacing(-0.01f);
                subjectNow.setTextSize(27);
                subjectNow.setTextColor(Color.BLACK);

                timerNow = new TextView(getContext());
                timerNow.setLayoutParams(timerLayoutParams);
                timerNow.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_bold));
                timerNow.setTextSize(40);
                timerNow.setLetterSpacing(-0.01f);
                timerNow.setGravity(Gravity.CENTER_HORIZONTAL);
                timerNow.setTextColor(Color.parseColor("#262626"));

                subjects[now].setVisibility(View.INVISIBLE);

                cardNow.addView(subjectNow);
                cardNow.addView(timerNow);
                times[now].setVisibility(View.INVISIBLE);
            } else
                cardNow.getLayoutParams().height = (int) ((height / 12));
        }

        if ((hour >= 0 && hour < 10) && today.equals(day)) {
            startTimer(now);
        } else {
            setOverTime();
        }
    }

    public void startTimer(int now) {
        Date date = Calendar.getInstance().getTime();
        int minute = Integer.parseInt(String.valueOf(date).substring(14, 16));
        int second = Integer.parseInt(String.valueOf(date).substring(17, 19));

        int remainSec = (59 - minute) * 60 + (60 - second);

        CountDownTimer cTimer = new CountDownTimer(remainSec * 1000L, 1000) {
            @SuppressLint({"DefaultLocale", "SetTextI18n"})
            public void onTick(long millisUntilFinished) {
                timerNow.setText("" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            @Override
            public void onFinish() {
                Date date = Calendar.getInstance().getTime();
                int hour = Integer.parseInt(String.valueOf(date).substring(11, 13));

                subjects[now].setVisibility(View.VISIBLE);
                times[now].setVisibility(View.VISIBLE);
                subjects[now].setText(table.get(now));
                cards[now].removeView(subjectNow);
                cards[now].removeView(timerNow);
                setProperTime(hour - 7);
            }
        };

        cTimer.start();
    }
}