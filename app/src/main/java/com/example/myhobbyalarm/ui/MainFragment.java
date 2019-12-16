package com.example.myhobbyalarm.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
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
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.example.myhobbyalarm.R;
import com.example.myhobbyalarm.adapter.AlarmsAdapter;
import com.example.myhobbyalarm.model.Alarm;
import com.example.myhobbyalarm.service.LoadAlarmsReceiver;
import com.example.myhobbyalarm.service.LoadAlarmsService;
import com.example.myhobbyalarm.util.AlarmUtils;
import com.example.myhobbyalarm.view.DividerItemDecoration;
import com.example.myhobbyalarm.view.EmptyRecyclerView;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikepenz.iconics.IconicsDrawable;

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
import static com.example.myhobbyalarm.calendar.CalendarActivity.MODE_EXTRA;
import static com.example.myhobbyalarm.calendar.CalendarActivity.buildCalendarActivityIntent;
import static com.example.myhobbyalarm.ui.AddEditAlarmActivity.ADD_ALARM;
import static com.example.myhobbyalarm.ui.AddEditAlarmActivity.buildAddEditAlarmActivityIntent;


public class MainFragment extends Fragment
        implements LoadAlarmsReceiver.OnAlarmsLoadedListener, View.OnClickListener {

    private static final String TAG = "MainFragment";
    private LoadAlarmsReceiver mReceiver;
    private AlarmsAdapter mAdapter;

    //날씨 관련 변수
    TextView tvUpdated, tvStatus, tvTemp, tvTempMin, tvTempMax;
    String API = "b298b9fd9ad4b11c145505d66138d5a9"; //openweatherMap에서 받은 api이다.
    TextView tvDay; //달, 일, 요일 변수
    static double LON, LAT;
    ImageView weatherImage;
    ImageButton imgBtnRefresh;



    //Gps
    private GpsTracker gpsTracker;
    private static final int GPS_ENABLE_REQUEST_CODE = 2000;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    static TextView tvGPS; //gps로 받은 주소값 변수

    //네트워크
    private boolean isConnected=false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new LoadAlarmsReceiver(this);
        Log.d(TAG,"onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_main, container, false);
        Log.d(TAG,"onCreateView");
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
            Log.d(TAG,"onCreateView, FloatingActionButton");
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

        //달,월,일 가져오는 함수
        currentMonth();

        /**Gps onCreate************************
         */
        if (!checkLocationServicesStatus()) {

            showDialogForLocationServiceSetting();
        } else {

            checkRunTimePermission();
        }

        tvGPS.setOnClickListener(this);
        imgBtnRefresh.setOnClickListener(this);


        long now = System.currentTimeMillis();
        Date mDate=new Date(now);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm:ss aa");
        String getTime =simpleDateFormat.format(mDate);
        Log.d("시간 체크", "onClick "+getTime+" "+now);

//        if(now > 1576464149938L) { //18시 이후
//
//        }

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

            Toast.makeText(getActivity(), "현재위치 \n위도 " + latitude + "\n경도 " + longitude, Toast.LENGTH_LONG).show();

            LAT = latitude;
            LON = longitude;

            new weatherTask().execute();

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
            if(result == null) {
                isConnected=true;
                if(isConnected) {
                    Toast.makeText(getActivity(), "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                isConnected=false;
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
//                String pressure = main.getString("pressure");
//                String humidity = main.getString("humidity");

//                Long sunrise = sys.getLong("sunrise");
//                Long sunset = sys.getLong("sunset");
//                String windSpeed = wind.getString("speed");
//                String weatherDescription = weather.getString("description");
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



                switch (id) {
                    case 200:
                        tvStatus.setText("	가벼운 비를 동반한 뇌우	");
                        break;
                    case 201:
                        tvStatus.setText("	비를 동반한 뇌우	");
                        break;
                    case 202:
                        tvStatus.setText("	강한 비를 동반한 뇌우	");
                        break;
                    case 210:
                        tvStatus.setText("	가벼운 뇌우	");
                        break;
                    case 211:
                        tvStatus.setText("	뇌우	");
                        break;
                    case 212:
                        tvStatus.setText("	강한 뇌우	");
                        break;
                    case 221:
                        tvStatus.setText("	들쑥날쑥한 뇌우	");
                        break;
                    case 230:
                        tvStatus.setText("	약한 이슬비를 동반한 뇌우	");
                        break;
                    case 231:
                        tvStatus.setText("	이슬비를 동반한 뇌우	");
                        break;
                    case 232:
                        tvStatus.setText("	강한 이슬비를 동반한 뇌우	");
                        break;
                    case 300:
                        tvStatus.setText("	가벼운 안개비	");
                        break;
                    case 301:
                        tvStatus.setText("	안개비	");
                        break;
                    case 302:
                        tvStatus.setText("	강한 안개비	");
                        break;
                    case 310:
                        tvStatus.setText("	가벼운 적은비	");
                        break;
                    case 311:
                        tvStatus.setText("	적은비	");
                        break;
                    case 312:
                        tvStatus.setText("	강한 적은비	");
                        break;
                    case 313:
                        tvStatus.setText("	소나기와 안개비	");
                        break;
                    case 314:
                        tvStatus.setText("	강한 소나기와 안개비	");
                        break;
                    case 321:
                        tvStatus.setText("	소나기	");
                        break;
                    case 500:
                        tvStatus.setText("	약한 비	");
                        break;
                    case 501:
                        tvStatus.setText("	중간 비	");
                        break;
                    case 502:
                        tvStatus.setText("	강한 비	");
                        break;
                    case 503:
                        tvStatus.setText("	매우 강한 비	");
                        break;
                    case 504:
                        tvStatus.setText("	극심한 비	");
                        break;
                    case 511:
                        tvStatus.setText("	우박	");
                        break;
                    case 520:
                        tvStatus.setText("	약한 소나기 비	");
                        break;
                    case 521:
                        tvStatus.setText("	소나기 비	");
                        break;
                    case 522:
                        tvStatus.setText("	강한 소나기 비	");
                        break;
                    case 531:
                        tvStatus.setText("	불규칙적 소나기 비	");
                        break;
                    case 600:
                        tvStatus.setText("	가벼운 눈	");
                        break;
                    case 601:
                        tvStatus.setText("	눈	");
                        break;
                    case 602:
                        tvStatus.setText("	강한 눈	");
                        break;
                    case 611:
                        tvStatus.setText("	진눈깨비	");
                        break;
                    case 612:
                        tvStatus.setText("	소나기 진눈깨비	");
                        break;
                    case 615:
                        tvStatus.setText("	약한 비와 눈	");
                        break;
                    case 616:
                        tvStatus.setText("	비와 눈	");
                        break;
                    case 620:
                        tvStatus.setText("	약한 소나기 눈	");
                        break;
                    case 621:
                        tvStatus.setText("	소나기 눈	");
                        break;
                    case 622:
                        tvStatus.setText("	강한 소나기 눈	");
                        break;
                    case 701:
                        tvStatus.setText("	안개	");

                        new MaterialStyledDialog.Builder(getActivity()).setTitle("오늘은 안개 낀 날이에요").setDescription("운전 조심하세요~").setPositiveText("닫기").setHeaderColor(R.color.secondary_text).setIcon(R.drawable.foggyday).onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        }).show();
                        break;
                    case 711:
                        tvStatus.setText("	연기	");
                        break;
                    case 721:
                        tvStatus.setText("	실안개	");
                        break;
                    case 731:
                        tvStatus.setText("	모래 먼지	");
                        break;
                    case 741:
                        tvStatus.setText("	안개	");
                        break;
                    case 751:
                        tvStatus.setText("	모래	");
                        break;
                    case 761:
                        tvStatus.setText("	먼지	");
                        break;
                    case 762:
                        tvStatus.setText("	화산재	");
                        break;
                    case 771:
                        tvStatus.setText("	돌풍	");
                        break;
                    case 781:
                        tvStatus.setText("	토네이도	");
                        break;
                    case 800:
                        tvStatus.setText("	구름 한 점 없는 맑은 하늘	");
//                        new MaterialStyledDialog.Builder(getActivity()).setTitle("선크림 필쑤우우우우ㅜ").setDescription("산책......").setPositiveText("일정 등록하러 가기").setHeaderColor(R.color.slightlyCyan).setIcon(R.mipmap.sunny).onPositive(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//
//                            }
//                        }).show();
                        new MaterialStyledDialog.Builder(getActivity()).setTitle("선크림 필쑤우우우우ㅜ").setDescription("산책......").setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.sunny).onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        }).show();

                        break;
                    case 801:
                        tvStatus.setText("	약간의 구름이 낀 하늘	");

                        break;
                    case 802:
                        tvStatus.setText("	드문드문 구름이 낀 하늘	");
                        break;
                    case 803:
                        tvStatus.setText("	구름이 거의 없는 하늘	");
                        new MaterialStyledDialog.Builder(getActivity()).setTitle("선크림 필쑤우우우우ㅜ").setDescription("산책......").setPositiveText("닫기").setHeaderColor(R.color.slightlyCyan).setIcon(R.drawable.hail).onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                            }
                        }).show();
                        break;

                    case 804:
                        tvStatus.setText("	구름으로 뒤덮인 흐린 하늘	");
                        break;
                    case 900:
                        tvStatus.setText("	토네이도	");
                        break;
                    case 901:
                        tvStatus.setText("	태풍	");
                        break;
                    case 902:
                        tvStatus.setText("	허리케인	");
                        break;
                    case 903:
                        tvStatus.setText("	한랭	");
                        break;
                    case 904:
                        tvStatus.setText("	고온	");
                        break;
                    case 905:
                        tvStatus.setText("	바람부는	");
                        break;
                    case 906:
                        tvStatus.setText("	우박	");
                        break;
                    case 951:
                        tvStatus.setText("	바람이 거의 없는	");
                        break;
                    case 952:
                        tvStatus.setText("	약한 바람	");
                        break;
                    case 953:
                        tvStatus.setText("	부드러운 바람	");
                        break;
                    case 954:
                        tvStatus.setText("	중간 세기 바람	");
                        break;
                    case 955:
                        tvStatus.setText("	신선한 바람	");
                        break;
                    case 956:
                        tvStatus.setText("	센 바람	");
                        break;
                    case 957:
                        tvStatus.setText("	돌풍에 가까운 센 바람	");
                        break;
                    case 958:
                        tvStatus.setText("	돌풍	");
                        break;
                    case 959:
                        tvStatus.setText("	심각한 돌풍	");
                        break;
                    case 960:
                        tvStatus.setText("	폭풍	");
                        break;
                    case 961:
                        tvStatus.setText("	강한 폭풍	");
                        break;
                    case 962:
                        tvStatus.setText("	허리케인	");
                        break;
                    default:
                        tvStatus.setText("날씨를 가져올 수 없습니다.");

                }


                /* Views populated, Hiding the loader, Showing the main design */
//                findViewById(R.id.loader).setVisibility(View.GONE);
//                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);


            } catch (JSONException e) {
//                findViewById(R.id.loader).setVisibility(View.GONE);
//                findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }

        }
    }


    /**
     * GPS***************************
     */

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if (permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if (check_result) {
                //위치 값을 가져올 수 있음
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

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

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getActivity(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
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
            //네트워크 문제
            Toast.makeText(getActivity(), "위치 서비스 사용불가", Toast.LENGTH_SHORT).show();
            return "네트워크 문제로 위치를 가져올 수 없습니다.";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getActivity(), "잘못된 GPS 좌표", Toast.LENGTH_SHORT).show();
            return "잘못된 GPS 좌표";

        }



        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getActivity(), "주소를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return "주소를 찾을 수 없습니다.";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString() + "\n";

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

                //사용자가 GPS 활성 시켰는지 검사
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
        Log.d(TAG,"onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mReceiver);
        Log.d(TAG,"onStop");
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
                Log.d(TAG,"onCreateView, FloatingActionButton");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAlarmsLoaded(ArrayList<Alarm> alarms) {
        for(Alarm list : alarms){
            Log.d(getClass().getSimpleName(),list.toString());
        }
        mAdapter.setAlarms(alarms);
        Log.d(TAG,"onAlarmsLoaded");
    }
}
