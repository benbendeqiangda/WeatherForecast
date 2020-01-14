package com.example.constraintlayouttest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    private static final int UPDATE_SUCCESS = 1;
    private static final int UPDATE_FAILURE = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(@NonNull Message msg)
        {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case UPDATE_SUCCESS:
                    SetWeather(get_Weather);
                case UPDATE_FAILURE:
                    Toast.makeText(MainActivity.this, "获取天气信息失败，请稍后重试", Toast.LENGTH_LONG);
                default:
                    break;
            }
        }
    };


    private HashSet<Integer> hashset_cond_code = new HashSet<Integer>();
    private boolean isDay;
    private GetWeather get_Weather;
    private Timer timer;

    private SwipeRefreshLayout weatherRefresh;

    private ImageView iv_weather;
    private TextView tv_weather_now, tv_temprature_now, tv_tmp_today, tv_cond_today,
            tv_date_today, tv_windcond_today, tv_week_today,city_get;

    private String city_name="大兴";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Init_Code();
        UpDate(city_name);

        SharedPreferences preferences=getSharedPreferences("sign",MODE_PRIVATE);
        String datasign=preferences.getString("database","false");
        if(datasign.equals("false"))
        {
            Init_Database();
            Log.d(TAG,datasign+"数据库创建失败");
        }
    }



    private void Init_Database()
    {
        Log.d(TAG,"数据库初始化");
        try
        {
            MyDatabaseHelper dbHelper=new MyDatabaseHelper(this,"CityList.db",null,1);
            SQLiteDatabase db=dbHelper.getWritableDatabase();
            ContentValues values=new ContentValues();

            db.delete("citylist",null,null);

            InputStream city_csv = getResources().openRawResource(R.raw.citylistcsv);
            Scanner in = new Scanner(city_csv);

            in.nextLine();

            int i = 0;
            while (in.hasNext())
            {
                String[] lines = in.nextLine().split(",");
                values.put("id",lines[0]);
                values.put("name",lines[2]);
                db.insert("citylist",null,values);
                values.clear();
            }

            in.close();
            city_csv.close();

            SharedPreferences.Editor editor = getSharedPreferences("sign", MODE_PRIVATE).edit();
            editor.putString("database","exist");
            editor.apply();

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void Init_Code()
    {
        weatherRefresh=findViewById(R.id.weatherRefreshLayout);
        weatherRefresh.setColorSchemeResources(R.color.colorPrimary);
        weatherRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                timer=new Timer();
                UpDate(city_name);
            }
        });

//        ImageView backgroud=findViewById(R.id.background);
//        backgroud.setImageResource(R.drawable.backgroud);
//        backgroud.setAlpha(200);

        RecyclerView recyclerView=findViewById(R.id.forecast);
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);

        List<String[]> weather_three=new ArrayList<>();
        weather_three.add(new String[]{"-9~3℃","晴","周三"});
        weather_three.add(new String[]{"-5~6℃","晴转多云","周四"});
        weather_three.add(new String[]{"-4~8℃","晴","周五"});
        Weather_Item_Adapter weather_item_adapter=new Weather_Item_Adapter(weather_three);
        recyclerView.setAdapter(weather_item_adapter);

        hashset_cond_code.add(100);
        hashset_cond_code.add(103);
        hashset_cond_code.add(104);
        hashset_cond_code.add(300);
        hashset_cond_code.add(301);
        hashset_cond_code.add(406);
        hashset_cond_code.add(407);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null)
            actionbar.hide();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1);
            }
        }


        iv_weather = (ImageView) findViewById(R.id.imageView);
        tv_temprature_now = (TextView) findViewById(R.id.textView);
        tv_weather_now = (TextView) findViewById(R.id.textView3);
        tv_tmp_today = (TextView) findViewById(R.id.textView4);
        tv_cond_today = (TextView) findViewById(R.id.textView5);
        tv_windcond_today = (TextView) findViewById(R.id.textView6);
        tv_date_today = (TextView) findViewById(R.id.textView7);
        tv_week_today = (TextView) findViewById(R.id.textView8);
        city_get=findViewById(R.id.city_get);

        FloatingActionButton button = findViewById(R.id.get_City);

        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, GetCity.class);
                startActivityForResult(intent, 1);
            }
        });

    }

    private void UpDate(String city)
    {
        String address_Forecast = "https://free-api.heweather" +
                ".net/s6/weather/forecast?location="+city+"&key=5bb56ca6ec0c4fd99f72a84a68b0f49f";
        String address_Now = "https://free-api.heweather" +
                ".net/s6/weather/now?location="+city+"&key=5bb56ca6ec0c4fd99f72a84a68b0f49f";

        city_get.setText(city_name);

        timer = new Timer();
        ArrayList<String> address = new ArrayList<>();
        address.add(address_Now);
        address.add(address_Forecast);
        getWeather(address);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1)
        {
            for (int i = 0; i < permissions.length; i++)
            {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //getWeather();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        timer.cancel();
    }

    private void getWeather(ArrayList<String> address)
    {
        get_Weather = new GetWeather(address);
        final TimerTask task = new TimerTask()
        {
            @Override
            public void run()
            {
                if (get_Weather.getIs_Update() == true)
                {
                    timer.cancel();
                    Message message = new Message();
                    message.what = UPDATE_SUCCESS;

                    //Bundle bundle=new Bundle();
                    //bundle.putParcelable("Get_Weather",(Parcelable)get_Weather);

                    //message.setData(bundle);
                    handler.sendMessage(message);
                } else
                {
                    Log.d(TAG, "仍在运行");
                }
            }
        };
        final TimerTask task1 = new TimerTask()
        {
            @Override
            public void run()
            {
                timer.cancel();
                Log.d(TAG, "终止进程");
                Message message = new Message();
                message.what = UPDATE_FAILURE;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 0, 1000);
        timer.schedule(task1, 10000);
    }

    private void SetWeather(GetWeather weather_Info)
    {
        Log.d(TAG,"怎么了");
        SetWeather_Forecast(weather_Info.getWeather_info().f_Info);
        SetWeather_Now(weather_Info.getWeather_info().n_Info);
    }

    private void SetWeather_Forecast(Weather_Info.Forecast forecast)
    {
        String ss = forecast.getSs();
        String sr = forecast.getSr();
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        isDay = JudgeDay(sr, ss, date);
        tv_tmp_today.setText(forecast.getTmp_Min() + "～" + forecast.getTmp_Max() + "℃");
        tv_windcond_today.setText(forecast.getWind_dir() + forecast.getWind_sc() + "级");

        if (isDay == true)
        {
            tv_cond_today.setText(forecast.getCond_txt_d());
        } else
        {
            tv_cond_today.setText(forecast.getCond_txt_n());
        }

        SimpleDateFormat format;
        format = new SimpleDateFormat("MM月dd日");
        tv_date_today.setText(format.format(date));
        format = new SimpleDateFormat("EEEE");
        tv_week_today.setText(format.format(date));

    }

    private void SetWeather_Now(Weather_Info.Now now)
    {
        ApplicationInfo appInfo = getApplicationInfo();

        if (hashset_cond_code.contains(Integer.valueOf(now.getCond_Code())) != false)
        {
            if (isDay == true)
            {
                int png_d =
                        getResources().getIdentifier("w_" + String.valueOf(now.getCond_Code()),
                                "drawable", appInfo.packageName);
                iv_weather.setImageResource(png_d);
            } else
            {
                int png_n =
                        getResources().getIdentifier("w_" + String.valueOf(now.getCond_Code() +
                                "n"), "drawable", appInfo.packageName);
                iv_weather.setImageResource(png_n);
            }
        } else
        {
            int png_d = getResources().getIdentifier("w_" + String.valueOf(now.getCond_Code()),
                    "drawable", appInfo.packageName);
            iv_weather.setImageResource(png_d);
        }
        tv_temprature_now.setText(now.getTmp());
        tv_weather_now.setText("℃\n" + now.getCond_txt());

        weatherRefresh.setRefreshing(false);
    }

    private boolean JudgeDay(String sr, String ss, Date date)
    {
        //Log.d(TAG,String.valueOf(date.getHours()));
        String time_Sr = sr;
        String time_Ss = ss;
        String time_Sr_hour = time_Sr.split(":")[0];
        //Log.d(TAG,String.valueOf(time_Sr_hour));
        //String time_Sr_minute=time_Sr.split(":")[1];
        String time_Ss_hour = time_Ss.split(":")[0];
        //String time_Ss_minute=time_Ss.split(":")[1];
        if (date.getHours() >= Integer.parseInt(time_Sr_hour) && date.getHours() <= Integer.parseInt(time_Ss_hour))
        {
            return true;
        } else
        {
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case 1:
                if (resultCode == RESULT_OK)
                {
                    //Log.d("Main", data.getStringExtra("city_return"));
                    String city = data.getStringExtra("city_return");
                    Log.d(TAG,"获取到的城市为"+city);
                    if(city.equals("NO_DATA"))
                    {

                    }
                    else
                    {
                        city_name=city;
                        UpDate(city_name);
                    }
                }
            default:
                ;
        }
    }

    class TestDataBean
    {

        private String City_ID;
        private String City_EN;
        private String City_CN;

        public String getCity_ID()
        {
            return City_ID;
        }

        public String getCity_EN()
        {
            return City_EN;
        }

        public String getCity_CN()
        {
            return City_CN;
        }

        public TestDataBean(String city_ID, String city_EN, String city_CN)
        {
            City_ID = city_ID;
            City_EN = city_EN;
            City_CN = city_CN;
        }
    }
    /*private void getWeatherForecast()
    {
        HeWeather.getWeatherForecast(MainActivity.this,"daxing,beijing", new HeWeather
        .OnResultWeatherForecastBeanListener()
                {
                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG, "Weather Now onError: ",e);
                    }

                    @Override
                    public void onSuccess(Forecast forecast) {

                        isDay=true;

                        Log.i(TAG, " Weather forecast onSuccess: " + new Gson().toJson(forecast));
                        List<ForecastBase> storage_forcast=new ArrayList<ForecastBase>();
                        storage_forcast=forecast.getDaily_forecast();
                        //Log.d(TAG,String.valueOf(storage_forcast.size()));
                        ForecastBase record_base=storage_forcast.get(0);
                        //String date_today=record_base.getDate();
                        String tmp_today_max=record_base.getTmp_max();
                        String tmp_today_min=record_base.getTmp_min();
                        String cond_today_d=record_base.getCond_txt_d();
                        String cond_today_n=record_base.getCond_txt_n();
                        String wind_today_dir=record_base.getWind_dir();
                        String wind_today_sc=record_base.getWind_sc();
                        tv_tmp_today.setText(tmp_today_min+"～"+tmp_today_max+"℃");
                        tv_windcond_today.setText(wind_today_dir+wind_today_sc+"级");


                        long time=System.currentTimeMillis();
                        Date date=new Date(time);

                        judgeDay(record_base,date);
                        getWeatherNow();
                        if(isDay==true)
                        {
                            tv_cond_today.setText(cond_today_d);
                        }
                        else
                        {
                            tv_cond_today.setText(cond_today_n);
                        }

                        SimpleDateFormat format;
                        format=new SimpleDateFormat("MM月dd日");
                        tv_date_today.setText(format.format(date));
                        format=new SimpleDateFormat("EEEE");
                        tv_week_today.setText(format.format(date));
                    }
                }
        );
    }
    private void getWeatherNow()
    {
        HeWeather.getWeatherNow(MainActivity.this, "daxing,beijing", Lang.CHINESE_SIMPLIFIED ,
        Unit.METRIC , new HeWeather.OnResultWeatherNowBeanListener() {
            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "Weather Now onError: ", e);
            }

            @Override
            public void onSuccess(Now dataObject) {
                Log.i(TAG, " Weather Now onSuccess: " + new Gson().toJson(dataObject));
                //先判断返回的status是否正确，当status正确时获取数据，若status不正确，可查看status对应的Code值找到原因
                //String Code = new Gson().toJson(dataObject);
                if ( Code.OK.getCode().equalsIgnoreCase(dataObject.getStatus()) ){
                    //此时返回数据
                    NowBase now = dataObject.getNow();
                    String temprature_now,weather_now,cond_code_now;
                    temprature_now=now.getTmp();
                    weather_now=now.getCond_txt();
                    cond_code_now=now.getCond_code();
                    if(hashset_cond_code.contains(Integer.valueOf(cond_code_now))!=false)
                    {
                        if(isDay==true)
                        {
                            iv_weather.setImageURI(Uri.parse
                            ("file:///mnt/sdcard/Pictures/drawable/"+"w_"+String.valueOf
                            (cond_code_now)+".png"));
                        }
                        else
                        {
                            iv_weather.setImageURI(Uri.parse
                            ("file:///mnt/sdcard/Pictures/drawable/"+"w_"+String.valueOf
                            (cond_code_now)+"n.png"));
                        }
                    }
                    else
                    {
                        iv_weather.setImageURI(Uri.parse
                        ("file:///mnt/sdcard/Pictures/drawable/"+"w_"+String.valueOf
                        (cond_code_now)+".png"));
                    }
                    tv_temprature_now.setText(temprature_now);
                    tv_weather_now.setText("℃\n"+weather_now);

                } else {
                    //在此查看返回数据失败的原因
                    String status = dataObject.getStatus();
                    Code code = Code.toEnum(status);
                    Log.i(TAG, "failed code: " + code);
                }
            }

        });
    }
    */
}
