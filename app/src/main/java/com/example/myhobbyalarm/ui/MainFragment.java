package com.example.myhobbyalarm.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androdocs.httprequest.HttpRequest;
import com.bumptech.glide.Glide;
import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.adapter.AlarmsAdapter;
import com.example.myhobbyalarm.data.DatabaseHelper;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.service.LoadAlarmsReceiver;
import com.example.myhobbyalarm.service.LoadAlarmsService;
import com.example.myhobbyalarm.util.AlarmUtils;
import com.example.myhobbyalarm.view.DividerItemDecoration;
import com.example.myhobbyalarm.view.EmptyRecyclerView;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


import static android.content.Context.LOCATION_SERVICE;

import static com.example.myhobbyalarm.calendar.CalendarActivity.buildCalendarActivityIntent;
import static com.example.myhobbyalarm.ui.AddEditAlarmActivity.ADD_ALARM;
import static com.example.myhobbyalarm.ui.AddEditAlarmActivity.buildAddEditAlarmActivityIntent;


public class MainFragment extends Fragment
        implements LoadAlarmsReceiver.OnAlarmsLoadedListener, View.OnClickListener, SlidingUpPanelLayout.PanelSlideListener {

    String selectcolor;
    ArrayList<Alarm> allAlarms = new ArrayList<>();
    ArrayList<Alarm> daysAlarms = new ArrayList<>();
    ArrayList<Alarm> colorAlarms = new ArrayList<>();

    private static final String TAG = "MainFragment";
    private LoadAlarmsReceiver mReceiver;
    private AlarmsAdapter mAdapter;

    //슬라이딩업패널레이아웃
    private SlidingUpPanelLayout slidingLayout;

    //날씨 관련 변수
    TextView tvUpdated, tvStatus, tvTemp, tvTempMin, tvTempMax; //업데이트 시간, 날씨상태, 기온, 최저온도, 최고온도 변수
    String API = "b298b9fd9ad4b11c145505d66138d5a9"; //openweatherMap에서 받은 api이다.
    TextView tvDay; //달, 일, 요일 변수
    static double LON, LAT; //위도 경도를 담을 변수
    ImageView weatherImage; //OpenWeatherMap에서 가져오는 날씨 이미지 담을 변수
    ImageButton imgBtnRefresh; //업데이트 새로고침 버튼 변수
    int getTimes; //18시 이후일 때 배경색/이미지 변경을 위한 변수
    ImageView imgDragUp; //드래그 표시 이미지 변수


    //Gps
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2000;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    static TextView tvGPS; //gps로 받은 주소값 변수

    //네트워크
    private boolean isConnected = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new LoadAlarmsReceiver(this);
        Log.d(TAG, "onCreate");

        //현재 시간 체크
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
        String getTime = simpleDateFormat.format(mDate);
        getTimes = Integer.parseInt(getTime);
        Log.d("시간체크", "onCreate" + getTimes);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d(TAG, "onCreateView");
        final EmptyRecyclerView rv = v.findViewById(R.id.recycler);
        mAdapter = new AlarmsAdapter();
        rv.setEmptyView(v.findViewById(R.id.empty_view));
        rv.setAdapter(mAdapter);
        rv.addItemDecoration(new DividerItemDecoration(getContext()));
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setItemAnimator(new DefaultItemAnimator());

        setHasOptionsMenu(true);
        final FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setBackgroundResource(R.drawable.custom_gradients_color_1);
        fab.setOnClickListener(view -> {
            AlarmUtils.checkAlarmPermissions(getActivity());
            final Intent i = buildAddEditAlarmActivityIntent(getContext(), ADD_ALARM);
            startActivity(i);
            Log.d(TAG, "onCreateView, FloatingActionButton");
        });


        //gps
        tvGPS = v.findViewById(R.id.tvGPS);

        //날씨
        tvUpdated = v.findViewById(R.id.tvUpdated);
        tvStatus = v.findViewById(R.id.tvStatus);
        tvTemp = v.findViewById(R.id.tvTemp);
        tvTempMin = v.findViewById(R.id.tvTempMin);
        tvTempMax = v.findViewById(R.id.tvTempMax);
        weatherImage = v.findViewById(R.id.weatherImage);
        tvDay = v.findViewById(R.id.tvDay);
        imgBtnRefresh = v.findViewById(R.id.imgBtnRefresh);
        imgDragUp = v.findViewById(R.id.imgDragUp);

        //슬라이딩업패널레이아웃
        slidingLayout = v.findViewById(R.id.slidingLayout);

        //달,월,일 가져오는 함수
        currentMonth();


        //Gps
        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        } else {

            checkRunTimePermission();
        }

        slidingLayout.addPanelSlideListener(this);
        imgBtnRefresh.setOnClickListener(this);

        return v;

    }

    //새로고침 버튼 클릭 시 현재 위치값을 받아 현재 위치에 대한 날씨를 알려준다.
    @Override
    public void onClick(View view) {
        refreshGPSWeather();

    }

    private void refreshGPSWeather() {

        gpsTracker = new GpsTracker(getActivity());

        double longitude = gpsTracker.getLongitude();
        double latitude = gpsTracker.getLatitude();

        if (longitude == 0.0 && latitude == 0.0) {
            Toast.makeText(getActivity(), "날씨를 불러올 수 없습니다\n" + "위치 설정과 네트워크 연결을 확인해주세요" +
                    "", Toast.LENGTH_LONG).show();
        } else {

            String address = getCurrentAddress(latitude, longitude);
            tvGPS.setText(address);

            LAT = latitude;
            LON = longitude;

            new weatherTask().execute();

        }

    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        Log.d("슬라이드", "onPanelSlide" + slideOffset);
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        String newStateValue = "EXPANDED";

        if (String.valueOf(newState) == newStateValue) {

            refreshGPSWeather();

            imgDragUp.animate().translationY(panel.getTop()).alpha(0.0f).setDuration(100).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    imgDragUp.setVisibility(imgDragUp.GONE);
                }
            });


            Log.d("슬라이드", "onPanelStateChanged" + newState);
        }
    }


    /**
     * 날씨
     */
    public class weatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        protected String doInBackground(String... args) {


            Log.d("테스트", "doInBack" + LAT);
            Log.d("테스트", "doInBack" + LON);

            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat=" + LAT + "&lon=" + LON + "&units=metric&appid=" + API);
            Log.d("테스트", "doInBack" + response);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {


            //네트워크 연결 오류 시 앱 꺼짐 현상 막음
            if (result == null) {
                isConnected = true;
                if (isConnected) {
                    Toast.makeText(getActivity(), "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                isConnected = false;
            }


            try {
                JSONObject jsonObj = new JSONObject(result);
                // main에 온도와, 기압, 습도, 최저기온, 최고기온
                JSONObject main = jsonObj.getJSONObject("main");
                // sys에 국가와 일출시간, 일몰시간
                JSONObject sys = jsonObj.getJSONObject("sys");
                // wind에 풍속
                JSONObject wind = jsonObj.getJSONObject("wind");
                // weather에 날씨정보(ex.구름 많음 등등)
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);
                // coord에 위도와 경도 값이 들어있다.
                JSONObject coord = jsonObj.getJSONObject("coord");


                Long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "업데이트 : " + new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.KOREA).format(new Date(updatedAt * 1000));
                String temp = main.getString("temp") + "°C";
                String tempMin = "최저 기온: " + main.getString("temp_min") + "°C";
                String tempMax = "최고 기온: " + main.getString("temp_max") + "°C";
                //날씨 코드 값 가져오기
                int id = weather.getInt("id");
                //날씨 아이콘 아이디 가져오기
                String iconID = weather.getString("icon");
                Log.d("테스트", "onPost" + iconID);
                String iconUrl = "http://openweathermap.org/img/wn/" + iconID + "@2x.png";

                tvUpdated.setText(updatedAtText);
                tvTemp.setText(temp);
                tvTempMin.setText(tempMin);
                tvTempMax.setText(tempMax);

                Glide.with(getActivity()).load(iconUrl).into(weatherImage);


                //날씨 아이디 값에 맞는 MaterialStyledDialog 출력 및 배경 색, 아이콘 설정
                switch (id) {
                    case 200:
                        tvStatus.setText("가벼운 비를 동반한 뇌우");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가벼운 비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가벼운 비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 201:
                        tvStatus.setText("	비를 동반한 뇌우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 202:
                        tvStatus.setText("	강한 비를 동반한 뇌우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 210:
                        tvStatus.setText("	약한 뇌우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 211:
                        tvStatus.setText("	뇌우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 212:
                        tvStatus.setText("	강한 뇌우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 221:
                        tvStatus.setText("	들쑥날쑥한 뇌우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("들쑥날쑥한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("들쑥날쑥한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 230:
                        tvStatus.setText("	약한 이슬비를 동반한 뇌우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 이슬비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 이슬비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 231:
                        tvStatus.setText("	이슬비를 동반한 뇌우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("이슬비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("이슬비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 232:
                        tvStatus.setText("	강한 이슬비를 동반한 뇌우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 이슬비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 이슬비를 동반한 뇌우").setDescription("뇌우는 번개와 천둥을 발생시키는 폭풍우에요. 돌풍과 폭우, 우박을 발생시킬 수 있으니 주의하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain_thunder).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 300:
                        tvStatus.setText("집중적으로 내리는 가랑비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 집중적으로 내리고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 집중적으로 내리고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 301:
                        tvStatus.setText("	가랑비	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 내리고 있어요. 가랑비는 이슬비보다 좀 더 굵은 비랍니다.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 내리고 있어요. 가랑비는 이슬비보다 좀 더 굵은 비랍니다.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 302:
                        tvStatus.setText("집중적으로 세게 내리는 가랑비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 세게 내리고 있어요. 가랑비는 이슬비보다 좀 더 굵은 비랍니다.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 세게 내리고 있어요. 가랑비는 이슬비보다 좀 더 굵은 비랍니다.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 310:
                        tvStatus.setText("집중적으로 약하게 내리는 가랑비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 약하게 내리고 있어요. 가랑비는 이슬비보다 좀 더 굵은 비랍니다.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 약하게 내리고 있어요. 가랑비는 이슬비보다 좀 더 굵은 비랍니다.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 311:
                        tvStatus.setText("이슬비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("이슬비가 내려요").setDescription("비가 이슬처럼 내리고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("이슬비가 내려요").setDescription("비가 이슬처럼 내리고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 312:
                        tvStatus.setText("집중적으로 강하게 내리는 보슬비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 세게 내리고 있어요. 가랑비는 이슬비보다 좀 더 굵은 비랍니다.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("가랑비가 내려요").setDescription("가랑비가 세게 내리고 있어요. 가랑비는 이슬비보다 좀 더 굵은 비랍니다.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 313:
                        tvStatus.setText("소나기와 이슬비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기와 이슬비").setDescription("소나기와 이슬비가 사이좋게 내리고 있어요.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기와 이슬비").setDescription("소나기와 이슬비가 사이좋게 내리고 있어요.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 314:
                        tvStatus.setText("강한 소나기와 이슬비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 소나기와 이슬비").setDescription("강한 소나기와 이슬비가 만나 내리고 있어요.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 소나기와 이슬비").setDescription("강한 소나기와 이슬비가 만나 내리고 있어요.").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 321:
                        tvStatus.setText("소나기성 가랑비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기성 가랑비").setDescription("가랑비가 잠시 내릴거에요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기성 가랑비").setDescription("가랑비가 잠시 내릴거에요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 500:
                        tvStatus.setText("	약한 비	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 비").setDescription("오늘은 약한 비가 내려요. 외출 하실 때 우산 꼭 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 비").setDescription("오늘은 약한 비가 내려요. 외출 하실 때 우산 꼭 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 501:
                        tvStatus.setText("비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비").setDescription("비가 내리고 있어요. 외출 하실 때 우산 꼭 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비").setDescription("배가 내리고 있어요. 외출 하실 때 우산 꼭 챙기세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 502:
                        tvStatus.setText("집중호우");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("집중호우").setDescription("강하고 많은 양의 비가 집중적으로 내리고 있어요! 오늘은 그냥 집에서 뒹굴뒹굴해요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("집중호우").setDescription("강하고 많은 양의 비가 집중적으로 내리고 있어요! 오늘은 그냥 집에서 뒹굴뒹굴해요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 503:
                        tvStatus.setText("	폭우	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("폭우").setDescription("비가 세차게 내리고 있어요. 오늘 외출하면 옷을 다 버리게 될텐데... 오늘은 그냥 집에서 쉬는게 어때요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("폭우").setDescription("비가 세차게 내리고 있어요. 오늘 외출하면 옷을 다 버리게 될텐데... 오늘은 그냥 집에서 쉬는게 어때요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 504:
                        tvStatus.setText("극심한 비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("극심한 비").setDescription("비가 세기가 강해요!! 우산에 구멍 날 수도 있으니 조심하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("극심한 비").setDescription("비가 세기가 강해요!! 우산에 구멍 날 수도 있으니 조심하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 511:
                        tvStatus.setText("어는 비"); //어는 비
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("얼어붙는 비가 내리고 있어요").setDescription("지면과 닿으면 얼어붙는 비에요! 밤에는 얼어붙은 바닥인지 아닌지 분간하기 더 어려우니 보행/운전시에 주의하세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("얼어붙는 비가 내리고 있어요").setDescription("지면과 닿으면 얼어붙는 비에요! 얼어붙은 바닥 때문에 미끄러져 넘어질 수 있으니 주의하세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 520:
                        tvStatus.setText("약한 소나기");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 소나기").setDescription("잠깐 스쳐지나가는 소나기에요. 밖이라면 비가 그칠 때까지 잠시 기다렸다가 이동해요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 소나기").setDescription("잠깐 스쳐지나가는 소나기에요. 밖이라면 비가 그칠 때까지 잠시 기다렸다가 이동해요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 521:
                        tvStatus.setText("소나기");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기").setDescription("언제 내리는지 언제 그치는지 모르니 우산 챙기는거 잊지 마세욧").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기").setDescription("언제 내리는지 언제 그치는지 모르니 우산 챙기는거 잊지 마세욧").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 522:
                        tvStatus.setText("집중적으로 강하게 내리는 소나기");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("집중적으로 강하게 내리는 소나기").setDescription("소나기가 집중적으로 오고 있어요. 비가 그칠 때까지 따뜻한 차 한 잔 하는 건 어떠세요?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("집중적으로 강하게 내리는 소나기").setDescription("소나기가 집중적으로 오고 있어요. 비가 그칠 때까지 커피 한 잔 하는 건 어떠세요?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 531:
                        tvStatus.setText("들쑥날쑥한 소나기");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("둘쑥날쑥한 소나기").setDescription("정말 언제 내릴지 언제 그칠지 몰라요!! 우산 까먹으시면 안 됩니당").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("둘쑥날쑥한 소나기").setDescription("정말 언제 내릴지 언제 그칠지 몰라요!! 우산 까먹으시면 안 됩니당").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_rain).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 600:
                        tvStatus.setText("적게 내리는 눈");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("눈이에요 눈").setDescription("가볍게 내리는 눈이에요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("눈이에요 눈").setDescription("가볍게 내리는 눈이에요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 601:
                        tvStatus.setText("눈");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("눈이에요 눈").setDescription("Do you wanna build a snowman~?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("눈이에요 눈").setDescription("Do you wanna build a snowman~?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 602:
                        tvStatus.setText("폭설");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("폭설입니다!!").setDescription("오늘 같은 날에 밖으로 나가면 집으로 못돌아와요ㅠㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("폭설입니다!!").setDescription("오늘 같은 날에 밖으로 나가면 집으로 못돌아와요ㅠㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 611:
                        tvStatus.setText("	진눈깨비	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비와 눈이 섞여서 내리고 있어요").setDescription("비랑 친한 눈은 싫어요ㅠ 외출 하실 때 우산 잊지 마세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비와 눈이 섞여서 내리고 있어요").setDescription("비랑 친한 눈은 싫어요ㅠ 외출 하실 때 우산 잊지 마세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 612:
                        tvStatus.setText("소나기성 진눈깨비");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기성 진눈깨비").setDescription("비랑 친한 눈이 잠깐 지나가겠데요...").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기성 진눈깨비").setDescription("비랑 친한 눈이 잠깐 지나가겠데요...").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 615:
                        tvStatus.setText("약한 비와 눈");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 비와 눈").setDescription("비랑 눈이 같이 내리고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 비와 눈").setDescription("비랑 눈이 같이 내리고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 616:
                        tvStatus.setText("비와 눈");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비와 눈").setDescription("비랑 눈이 사이좋게 같이 내리고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("비와 눈").setDescription("비랑 눈이 사이좋게 같이 내리고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_sleet).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 620:
                        tvStatus.setText("약한 소나기성 눈");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 소나기성 눈").setDescription("소나기처럼 눈이 잠깐만 있다가 간데요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 소나기성 눈").setDescription("소나기처럼 눈이 잠깐만 있다가 간데요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 621:
                        tvStatus.setText("소나기성 눈");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기성 눈").setDescription("소나기처럼 눈이 잠깐만 있다가 간데요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("소나기성 눈").setDescription("소나기처럼 눈이 잠깐만 있다가 간데요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 622:
                        tvStatus.setText("강한 소나기성 눈");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 소나기성 눈").setDescription("소나기처럼 눈이 잠깐만 있다가 간데요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 소나기성 눈").setDescription("소나기처럼 눈이 잠깐만 있다가 간데요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_snow).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 701:
                        tvStatus.setText("옅은 안개");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("옅은 안개가 끼었어요").setDescription("옅은 안개라고 얕보다가 큰 코 다쳐요ㅠ 안전운전 하셔야 합니다!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.mist).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("옅은 안개가 끼었어요").setDescription("옅은 안개라고 얕보다가 큰 코 다쳐요ㅠ 안전운전 하셔야 합니다!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.mist).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 711:
                        tvStatus.setText("연기");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("연기").setDescription("연기가 낀 날시네요ㅠ 마스크 필수 착용하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.fog).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("연기").setDescription("연기가 낀 날시네요ㅠ 마스크 필수 착용하세요!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.fog).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }

                        break;
                    case 721:
                        tvStatus.setText("실안개	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("실안개 낀 날씨에요").setDescription("안개 낀 날에는 서행운전...아시죠? 안전운전 하세요~~!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.mist).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("실안개 낀 날씨에요").setDescription("안개 낀 날에는 서행운전...아시죠? 안전운전 하세요~~!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.mist).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 731:
                        tvStatus.setText("모래 먼지");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("으악 모래먼지 낀 날씨에요").setDescription("우리의 호흡기는 소중하니깐! 마스크 꼭 착용하고 외출하셔야 해요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.mask).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("으악 모래먼지 낀 날씨에요").setDescription("우리의 호흡기는 소중하니깐! 마스크 꼭 착용하고 외출하셔야 해요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.mask).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 741:
                        tvStatus.setText("안개");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("안개 낀 날이에요").setDescription("안개 낀 날에는 서행운전...아시죠? 안전운전 하세요~~!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.fog).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("안개 낀 날이에요").setDescription("안개 낀 날에는 서행운전...아시죠? 안전운전 하세요~~!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.fog).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 751:
                        tvStatus.setText("모래");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("으악...모래라니..모래라닝").setDescription("우리의 호흡기는 소중하니깐! 마스크 필수 착용입니다!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.mask).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("으악...모래라니..모래라닝").setDescription("우리의 호흡기는 소중하니깐! 마스크 필수 착용입니다!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.mask).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 761:
                        tvStatus.setText("먼지");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("먼지...싫어").setDescription("우리의 호흡기는 소중하니깐! 마스크 필수 착용입니다!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.mask).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("먼지...싫어").setDescription("우리의 호흡기는 소중하니깐! 마스크 필수 착용입니다!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.mask).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 762:
                        tvStatus.setText("화산재");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("화산재").setDescription("화산재...? 빨리 대피하세요!!!!!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.volcano).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("화산재").setDescription("화산재...? 빨리 대피하세요!!!!!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.volcano).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 771:
                        tvStatus.setText("돌풍");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("돌풍").setDescription("갑자기 바람이 세게 불고 있어요! 바람 따라 날라가지 않게 조심조심하세요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("돌풍").setDescription("갑자기 바람이 세게 불고 있어요! 바람 따라 날라가지 않게 조심조심하세요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }

                        break;
                    case 781:
                        tvStatus.setText("	토네이도	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("토네이도").setDescription("...어떡해ㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("토네이도").setDescription("...어떡해ㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 800:
                        tvStatus.setText("맑은 하늘");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("구름 한 점 없는 맑은 날이에요").setDescription("이렇게 맑은 날에 운동장 한 바퀴 시원하게 돌고 오면 몸도 마음도 맑은 하늘처럼 개운해질거에요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_clear).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("구름 한 점 없는 맑은 날이에요").setDescription("오늘 같은 맑은 날에는 가까운 공원에 가서 산책하는 것이 어떠세요?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_clear).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 801:
                        tvStatus.setText("약간의 구름 낀 하늘");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("구름이 약간 낀 날씨에요").setDescription("오늘은 구름이 달을 가리지 않겠네요 ><").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_partial_cloud).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("구름이 약간 낀 날씨에요").setDescription("구름이 햇빛을 모두 가리지 않아 적당히 따스해요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_partial_cloud).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 802:
                        tvStatus.setText("	드문드문 구름이 낀 하늘	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("오늘 하늘은 구름이 드문드문 떠 있어요").setDescription("달이 구름이랑 까꿍놀이 하고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_partial_cloud).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("오늘 하늘은 구름이 드문드문 떠 있어요").setDescription("볕이 드문드문 떠 있는 구름에 가려져서 그늘을 만들고 있어요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_partial_cloud).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 803:
                        tvStatus.setText("양떼구름");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("양떼구름").setDescription("양떼가 줄지어 가는 모습이 계속 보인다면 비가 올 확률이 높아진다고 해요! 휴대용 우산을 챙기는게 어떠세요?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.cloudy).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("양떼구름").setDescription("양떼가 줄지어 가는 모습이 계속 보인다면 비가 올 확률이 높아진다고 해요! 휴대용 우산을 챙기는게 어떠세요?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.cloudy).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;

                    case 804:
                        tvStatus.setText("	구름으로 뒤덮인 흐린 하늘	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("오늘 하늘은 흐려요").setDescription("달이 구름으로 뒤덮혀서 달빛이 매우 흐려요 ㅠㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.overcast).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("오늘 하늘은 흐려요").setDescription("햇빛이 구름에 뒤덮혀도 우리는 자외선에 항상 신경써야 해요~! 선크림 바르고 외출하세요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.overcast).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 900:
                        tvStatus.setText("토네이도");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("토네이도").setDescription("...어떡해ㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("토네이도").setDescription("...어떡해ㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 902:
                        tvStatus.setText("	허리케인	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("허리케인").setDescription("...어떡해ㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("허리케인").setDescription("...어떡해ㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 903:
                        tvStatus.setText("한랭");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("으아 추워").setDescription("외투 두껍게 입고 외출하세요 안그럼 감기 걸린다요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.cold).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("으아 추워").setDescription("외투 두껍게 입으세요! 그래야 감기 걸리지 않아요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.cold).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 904:
                        tvStatus.setText("고온");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("아우 더워더워").setDescription("오늘 기온이 높아요! 저녁이라고 선크림 안 바르시면 피부 노화가 2배~! 듬뿍듬뿍 바르고 손풍기도 챙겨요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.hot).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("아우 더워더워").setDescription("오늘 기온이 높아요! 선크림 듬뿍듬뿍 바르고 손풍기도 챙겨요 ㅎㅎ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.hot).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 905:
                        tvStatus.setText("바람이 많이 불어요");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("바람이 많이 부는 날씨에요").setDescription("날아가지 않게 조심하세요~!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("바람이 많이 부는 날씨에요").setDescription("날아가지 않게 조심하세요~!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 906:
                        tvStatus.setText("우박");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("우박 조심!").setDescription("우산을 뚫을 수도 있으니 머리조심하세요..!!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_hail).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("우박 조심!").setDescription("우산을 뚫을 수도 있으니 머리조심하세요..!!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_hail).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 951:
                        tvStatus.setText("바람 없는 하늘");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("바람 없는 하늘").setDescription("바람이 불지 않는 날씨에요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.night_clear).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("바람 없는 하늘").setDescription("바람이 불지 않는 날씨에요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.day_clear).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 952:
                        tvStatus.setText("약한 바람");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 바람").setDescription("바람이 가볍게 스치는 날씨에요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("약한 바람").setDescription("바람이 가볍게 스치는 날씨에요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    case 953:
                        tvStatus.setText("부드러운 바람");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("부드러운 바람이 불고 있어요").setDescription("이런 날에는 한강가서 치맥><").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("부드러운 바람이 불고 있어요").setDescription("이런 날에는 한강가서 치맥><").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;
                    case 954:
                        tvStatus.setText("적당한 바람");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("적당한 바람").setDescription("적당한 바람 맞으면서 운동하는건 어떠세요?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("적당한 바람").setDescription("적당한 바람 맞으면서 운동하는건 어떠세요?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;
                    case 955:
                        tvStatus.setText("신선한 바람");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("신선한 바람").setDescription("신선한 바람 맞으면서 운동하는건 어떠세요?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("신선한 바람").setDescription("신선한 바람 맞으면서 운동하는건 어떠세요?").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;
                    case 956:
                        tvStatus.setText("강한 바람");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("오늘은 바람 세기가 강해요").setDescription("공들인 머리가 망가질수도 있으니 조심하세요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.cloud_wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("오늘은 바람 세기가 강해요").setDescription("공들인 머리가 망가질수도 있으니 조심하세요").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.cloud_wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;
                    case 957:
                        tvStatus.setText("돌풍에 가까운 센 바람");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("돌풍에 가까운 센 바람").setDescription("으으 오늘은 바람이 돌풍 급이에요!!! 날라가지 않게 조심하세용~!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.cloud_wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("돌풍에 가까운 센 바람").setDescription("으으 오늘은 바람이 돌풍 급이에요!!! 날라가지 않게 조심하세용~!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.cloud_wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;
                    case 958:
                        tvStatus.setText("돌풍");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("돌풍").setDescription("진짜 돌풍이에요!! 집에서 자신을 지키세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.cloud_wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("돌풍").setDescription("진짜 돌풍이에요!! 집에서 자신을 지키세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.cloud_wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;
                    case 959:
                        tvStatus.setText("강한 돌풍");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 돌풍").setDescription("강한 돌풍입니다!!! 집에 꼼짝말고 있으세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.cloud_wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 돌풍").setDescription("강한 돌풍입니다!!! 집에 꼼짝말고 있으세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.cloud_wind).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;
                    case 960:
                        tvStatus.setText("폭풍");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("폭풍").setDescription("집에 꼼짝말고 있으세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("폭풍").setDescription("집에 꼼짝말고 있으세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;
                    case 961:
                        tvStatus.setText("강한 폭풍");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 폭풍").setDescription("집에 꼼짝말고 있으세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("강한 폭풍").setDescription("집에 꼼짝말고 있으세요!!").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                }
                            }).show();
                        }
                        break;
                    case 962:
                        tvStatus.setText("	허리케인	");
                        if (getTimes >= 18) {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("허리케인").setDescription("...어떡해ㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.black).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        } else {
                            new MaterialStyledDialog.Builder(getActivity()).setTitle("허리케인").setDescription("...어떡해ㅠ").withDialogAnimation(true).setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.tornado).onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                }
                            }).show();
                        }
                        break;
                    default:
                        tvStatus.setText("날씨를 가져올 수 없습니다.");
                }

            } catch (JSONException e) {

            }

        }
    }

    /**GPS************************
     */

    //ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 받는다.
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if (check_result) {
                //위치 값을 가져올 수 있다면
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료.
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
//                    finish();
                } else {
                    Toast.makeText(getActivity(), "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }


    void checkRunTimePermission() {

        // 위치 퍼미션을 가지고 있는지 체크.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 이미 퍼미션을 가지고 있다면 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식한다.
            // 위치 값을 가져올 수 있음

        } else {  //퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다.

            // 사용자가 퍼미션 거부를 한 적이 있는 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {

                // 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getActivity(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }


    public String getCurrentAddress(double latitude, double longitude) {

        //GPS를 주소로 변환한다.
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

        List<Address> addresses;
        try {
            //현재 위치 주소를 가져온다.
            addresses = geocoder.getFromLocation(latitude, longitude, 7);
        } catch (IOException ioException) {
            //네트워크 문제 발생 시
            Toast.makeText(getActivity(), "위치 서비스 사용불가", Toast.LENGTH_SHORT).show();
            return "네트워크 문제로 주소를 불러올 수 없습니다.";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getActivity(), "잘못된 GPS 좌표", Toast.LENGTH_SHORT).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getActivity(), "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return "주소를 찾을 수 없습니다.";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0) + "\n";
    }


    /**
     * gps가 비활성화 되어있을 경우 gps활성화 할 수 있는 설정창으로 이동한다.
     */
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("위치 서비스가 비활성화되어있습니다.");
        builder.setIcon(R.drawable.location);
        builder.setMessage("TODO 어플을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 활성화하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("활성화하러 가기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사한다.
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
    }

    /**
     * GPS >> checkLocationServicesStatus()
     * <p>
     * 현재 위치 값을 가지고 오기 위한 객체 선언한다.
     * LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
     * <p>
     * GPS로부터 현재 위치 값을 가져온다.
     * LocationManager.GPS_PROVIDER
     * <p>
     * 기지국으로부터 현재 위치 값을 가져온다.
     * LocationManager.NETWORK_PROVIDER
     */
    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);


        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //달,월,일 가져오는 함수
    //SlideUpPanel Layout에 XXXX년 XX월 XX일이 표시된다.
    private void currentMonth() {
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sDay = new SimpleDateFormat("EE", Locale.getDefault());
        SimpleDateFormat sDate = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat sMonth = new SimpleDateFormat("MM", Locale.getDefault());

        String month = sMonth.format(currentTime);
        String date = sDate.format(currentTime);
        String day = sDay.format(currentTime);

        tvDay.setText(month + "월 " + date + "일 " + day + "요일");
    }

    @Override
    public void onStart() {
        super.onStart();
        final IntentFilter filter = new IntentFilter(LoadAlarmsService.ACTION_COMPLETE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mReceiver, filter);
        LoadAlarmsService.launchLoadAlarmsService(getContext());
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        Log.d(TAG, "onStop");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_alarm_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_calendar:
                Date today = new Date();
                final Intent i = buildCalendarActivityIntent(getContext(), today);
                startActivity(i);
                Log.d(TAG, "onCreateView, FloatingActionButton");
                break;
            case R.id.action_refresh:
                mAdapter.setAlarms(allAlarms);
                break;

            case R.id.action_color:
                /**
                 * 다이얼 로그창에서 선택한 컬러 타이틀에 따라 알람을 정렬함
                 */
                View colorView = View.inflate(getContext(), R.layout.color_dialog, null);
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setView(colorView);
                RadioGroup dialog_edit_alarm_rdo_g = colorView.findViewById(R.id.dialog_edit_alarm_rdo_g);
                dialog_edit_alarm_rdo_g.check(-1);
                dialog_edit_alarm_rdo_g.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                        switch (checkedId) {
                            case R.id.dialog_edit_alarm_color_softRed:
                                selectcolor = "softRed";
                                break;
                            case R.id.dialog_edit_alarm_color_lightOrange:
                                selectcolor = "lightOrange";
                                break;
                            case R.id.dialog_edit_alarm_color_softOrange:
                                selectcolor = "softOrange";
                                break;
                            case R.id.dialog_edit_alarm_color_slightlyCyan:
                                selectcolor = "slightlyCyan";
                                break;
                            case R.id.dialog_edit_alarm_color_slightlyGreen:
                                selectcolor = "slightlyGreen";
                                break;
                            case R.id.dialog_edit_alarm_color_green:
                                selectcolor = "green";
                                break;
                            case R.id.dialog_edit_alarm_color_strongCyan:
                                selectcolor = "strongCyan";
                                break;
                            case R.id.dialog_edit_alarm_color_blue:
                                selectcolor = "blue";
                                break;
                            case R.id.dialog_edit_alarm_color_moderateBlue:
                                selectcolor = "moderateBlue";
                                break;
                            case R.id.dialog_edit_alarm_color_moderateViolet:
                                selectcolor = "moderateViolet";
                                break;
                            case R.id.dialog_edit_alarm_color_black:
                                selectcolor = "black";
                                break;
                        }
                    }
                });

                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (selectcolor == null) {
                            selectcolor = "softRed";
                        }
                        if (selectcolor != null) {
                            setColorData(selectcolor);
                        }
                    }
                });
                dialog.setNegativeButton("취소", null);
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setColorData(String colorTitle) {
        colorAlarms.removeAll(colorAlarms);
        for (Alarm alarm : allAlarms) {
            if (alarm.getColorTitle() != null) {
                if (alarm.getColorTitle().equals(colorTitle)) {
                    colorAlarms.add(alarm);
                }
            }
        }
        mAdapter.setAlarms(colorAlarms);
    }

    @Override
    public void onAlarmsLoaded(ArrayList<Alarm> alarms) {
        allAlarms.removeAll(allAlarms);
        for (Alarm list : alarms) {
            allAlarms.add(list);
            Log.d(getClass().getSimpleName(), list.toString());
            mAdapter.setAlarms(alarms);
            Log.d(TAG, "onAlarmsLoaded");
        }
    }
}
