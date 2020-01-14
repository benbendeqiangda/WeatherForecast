package com.example.constraintlayouttest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GetCity extends AppCompatActivity {

    public LocationClient mLocationClient;
    private static final String TAG = "GetCity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new GetCity.MyLocationListener());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_get_city);

        InitCode();

    }
    private void InitCode()
    {
        ImageView imageView=findViewById(R.id.locate);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetAuthority();
                requestLocation();
            }
        });

        ListView listView=findViewById(R.id.city_list);

        final SharedPreferences preferences=getSharedPreferences("cityHistory",MODE_PRIVATE);
        final Set<String> citylist=new HashSet<>(preferences.getStringSet("citylist",new HashSet<String>()));

        final ArrayList<String> city_list;
        if(citylist.size()==0)
        {
            Log.d(TAG,"历史城市列表没有");
            city_list=new ArrayList<>();
        }
        else
        {
            Log.d(TAG,"历史城市列表大小"+citylist.size());
            city_list=new ArrayList<>(citylist);
        }

        final ArrayAdapter<String> adapter=new ArrayAdapter<>(GetCity.this,android.R.layout.simple_list_item_1,city_list);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent();
                String item=city_list.get(position);

                citylist.add(item);
                Log.d(TAG,"查看是否修改成功"+citylist.size());

                SharedPreferences.Editor editor=getSharedPreferences("cityHistory",MODE_PRIVATE).edit();
                editor.putStringSet("citylist",citylist);
                editor.apply();

                intent.putExtra("city_return",item);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        MyDatabaseHelper dbHelper=new MyDatabaseHelper(this,"CityList.db",null,1);
        final SQLiteDatabase db=dbHelper.getReadableDatabase();
        EditText textView=findViewById(R.id.editText);
        textView.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(s.length()==0)
                {
                    ArrayList<String> temp_city_list=new ArrayList<String>(citylist);
                    city_list.clear();
                    city_list.addAll(temp_city_list);
                }
                else
                {
                    String cityName=s.toString();
                    String sql="select name from citylist where name like '%"+cityName+"%'";
                    Cursor cursor=db.rawQuery(sql,null);

                    city_list.clear();
                    if(cursor.moveToFirst())
                    {
                        do
                        {
                            String name=cursor.getString(cursor.getColumnIndex("name"));
                            city_list.add(name);
                        }
                        while (cursor.moveToNext());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });

    }
    private void GetAuthority()
    {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(GetCity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
//        if (ContextCompat.checkSelfPermission(LocatedTest.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            permissionList.add(Manifest.permission.READ_PHONE_STATE);
//        }
        if (ContextCompat.checkSelfPermission(GetCity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(GetCity.this, permissions, 1);
        }
    }
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
//            currentPosition.append("纬度：").append(location.getLatitude()).append("\n");
//            currentPosition.append("经线：").append(location.getLongitude()).append("\n");

            currentPosition.append(location.getCity().substring(0,location.getCity().length()-1));
            Log.d(TAG,"接收成功"+currentPosition);
            if (currentPosition==null)
            {
                Toast.makeText(GetCity.this,"定位失败",Toast.LENGTH_LONG).show();
            }
            else
            {
                Intent intent=new Intent();
                intent.putExtra("city_return",currentPosition.toString());
                setResult(RESULT_OK,intent);
                mLocationClient.stop();
                finish();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra("city_return","NO_DATA");
        setResult(RESULT_OK,intent);
        finish();
    }
}
