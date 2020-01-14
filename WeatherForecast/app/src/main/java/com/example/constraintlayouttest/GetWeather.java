package com.example.constraintlayouttest;



import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class GetWeather {
    public static final int UPDATE_TEXT=1;
    private Handler handler=new Handler()
    {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what)
            {
                case UPDATE_TEXT:

                    resolve_Date();
                default:break;
            }
        }
    };

    private static final String TAG = "GetWeather";

    public boolean getIs_Update() {
        return is_Update;
    }

    private boolean is_Update;

    private ArrayList<String> address_List=new ArrayList<String>();
    private ArrayList<String> weather_Data =new ArrayList<String>();

    public Weather_Info getWeather_info() {
        if(is_Update==true)
            return weather_info;
        else
            return null;
    }

    private Weather_Info weather_info=new Weather_Info();

    GetWeather()
    {
        is_Update=false;

        String address_Forecast="https://free-api.heweather.net/s6/weather/forecast?location=daxing&key=5bb56ca6ec0c4fd99f72a84a68b0f49f";
        String address_Now="https://free-api.heweather.net/s6/weather/now?location=daxing&key=5bb56ca6ec0c4fd99f72a84a68b0f49f";

        address_List.add(address_Now);
        address_List.add(address_Forecast);
        getWeatherInfo();

    }
    GetWeather(ArrayList<String> address_List)
    {
        is_Update=false;
        for(int i=0;i<address_List.size();i++)
        {
            this.address_List.add(address_List.get(i));
        }
        getWeatherInfo();
    }

    private void getWeatherInfo()
    {
        start_Thread();
    }

    private void start_Thread()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection=null;
                BufferedReader reader=null;
                try {

                    Message message=new Message();
                    message.what=UPDATE_TEXT;

                    for(int i=0;i<address_List.size();i++)
                    {
                        String address=address_List.get(i);
                        URL url=new URL(address);
                        connection=(HttpURLConnection)url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(8000);
                        connection.setReadTimeout(8000);

                        InputStream in = connection.getInputStream();
                        // 下面对获取到的输入流进行读取
                        reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        //Log.d(TAG,response.toString());

                        weather_Data.add(response.toString());
                        //Bundle bundle=new Bundle();
                        //bundle.putString("weather_now",response.toString());
                        //message.setData(bundle);
                        Log.d(TAG,response.toString());
                    }
                    handler.sendMessage(message);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void resolve_Date()
    {
        boolean sign1=resovle_Date_Now(weather_Data.get(0));
        boolean sign2=resovle_Date_Forecast(weather_Data.get(1));
        Log.d(TAG,sign1+" "+sign2);
        is_Update=sign1&sign2;
        Log.d(TAG,String.valueOf(is_Update));
    }
    private boolean resovle_Date_Now(String data)
    {
        try
        {
            JSONArray jsonArray;
            JSONObject jsonObject;

            jsonObject=new JSONObject(data);
            jsonArray=new JSONArray(jsonObject.get("HeWeather6").toString());
            jsonObject=jsonArray.getJSONObject(0);
            String status=jsonObject.getString("status");
            if(!status.equals("ok"))
            {
                return false;
            }
            else
            {
                jsonObject=jsonObject.getJSONObject("now");

                String cond_code=jsonObject.getString("cond_code");
                String tmp=jsonObject.getString("tmp");
                String cond_txt=jsonObject.getString("cond_txt");
                //Log.d(TAG,cond_code+" "+tmp+" "+cond_txt+" ");
                weather_info.n_Info=new Weather_Info.Now(tmp,cond_txt,cond_code);
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
    private boolean resovle_Date_Forecast(String data)
    {
        try
        {
            JSONArray jsonArray;
            JSONObject jsonObject;

            jsonObject=new JSONObject(data);
            jsonArray=new JSONArray(jsonObject.get("HeWeather6").toString());
            jsonObject=jsonArray.getJSONObject(0);
            String status=jsonObject.getString("status");
            if(!status.equals("ok"))
            {
                return false;
            }
            else
            {
                jsonArray=jsonObject.getJSONArray("daily_forecast");
                jsonObject=jsonArray.getJSONObject(0);

                String tmp_Max=jsonObject.getString("tmp_max");
                String tmp_Min=jsonObject.getString("tmp_min");
                String cond_txt_d=jsonObject.getString("cond_txt_d");
                String cond_txt_n=jsonObject.getString("cond_txt_n");
                String wind_dir=jsonObject.getString("wind_dir");
                String wind_sc=jsonObject.getString("wind_sc");
                String sr=jsonObject.getString("sr");
                String ss=jsonObject.getString("ss");
                //Log.d(TAG,tmp_Max+" "+tmp_Min+" "+cond_txt_d+" "+cond_txt_n+" "+wind_dir+" "+wind_sc+" ");
                weather_info.f_Info=new Weather_Info.Forecast(tmp_Max,tmp_Min,cond_txt_d,cond_txt_n,wind_dir,wind_sc,ss,sr);
                return true;

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
}
class Weather_Info
{
    static class Forecast
    {
        private String tmp_Max;
        private String tmp_Min;
        private String cond_txt_d;
        private String cond_txt_n;
        private String wind_dir;
        private String wind_sc;
        private String ss;
        private String sr;

        public Forecast(String tmp_Max, String tmp_Min, String cond_txt_d, String cond_txt_n, String wind_dir, String wind_sc, String ss, String sr) {
            this.tmp_Max = tmp_Max;
            this.tmp_Min = tmp_Min;
            this.cond_txt_d = cond_txt_d;
            this.cond_txt_n = cond_txt_n;
            this.wind_dir = wind_dir;
            this.wind_sc = wind_sc;
            this.ss = ss;
            this.sr = sr;
        }

        public String getSs() {
            return ss;
        }

        public String getSr() {
            return sr;
        }

        public String getTmp_Max() {
            return tmp_Max;
        }

        public String getTmp_Min() {
            return tmp_Min;
        }

        public String getCond_txt_d() {
            return cond_txt_d;
        }

        public String getCond_txt_n() {
            return cond_txt_n;
        }

        public String getWind_dir() {
            return wind_dir;
        }

        public String getWind_sc() {
            return wind_sc;
        }
    }
    static class Now
    {
        private String tmp;
        private String cond_txt;
        private String cond_Code;

        public Now(String tmp, String cond_txt, String cond_Code) {
            this.tmp = tmp;
            this.cond_txt = cond_txt;
            this.cond_Code = cond_Code;
        }

        public String getTmp() {
            return tmp;
        }

        public String getCond_txt() {
            return cond_txt;
        }

        public String getCond_Code() {
            return cond_Code;
        }
    }
    public Forecast f_Info;
    public Now n_Info;
}
