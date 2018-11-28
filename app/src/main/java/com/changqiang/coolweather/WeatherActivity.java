package com.changqiang.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.changqiang.coolweather.gson.Forecast;
import com.changqiang.coolweather.gson.Weather;
import com.changqiang.coolweather.utils.HttpUtility;
import com.changqiang.coolweather.utils.Utility;

import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity
{
    private String weatherId;
    private Button navButton;
    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout swipeRefresh;
    private ImageView backgroundImage;

    private TextView titleCity, titleDate;
    private TextView titleLatitude, titleLongitude;

    private TextView nowTemp, nowWeather, nowWindDir, nowWindScale, nowWindSpeed;
    private TextView aqi_quality, aqi_info, aqi_pm25;

    private LinearLayout forecastLayout;

    private TextView suggestion_comfort, suggestion_sport, suggestion_car_wash;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21)
        {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);

        titleCity = findViewById(R.id.title_city_name);
        titleDate = findViewById(R.id.title_update_time);
        navButton = findViewById(R.id.title_home);
        backgroundImage = findViewById(R.id.background_image);
        drawerLayout = findViewById(R.id.drawer_layout);
        swipeRefresh = findViewById(R.id.swipe_layout);
        titleLatitude = findViewById(R.id.title_latitude);
        titleLongitude = findViewById(R.id.title_longitude);
        nowTemp = findViewById(R.id.now_temperature);
        nowWeather = findViewById(R.id.now_weather);
        nowWindDir = findViewById(R.id.now_wind_dir);
        nowWindScale = findViewById(R.id.now_wind_scale);
        nowWindSpeed = findViewById(R.id.now_wind_speed);
        aqi_info = findViewById(R.id.aqi_info);
        aqi_pm25 = findViewById(R.id.aqi_pm25);
        aqi_quality = findViewById(R.id.aqi_quality);
        forecastLayout = findViewById(R.id.forecast_layout);
        suggestion_car_wash = findViewById(R.id.suggestion_car_wash);
        suggestion_comfort = findViewById(R.id.suggestion_comfort);
        suggestion_sport = findViewById(R.id.suggestion_sport);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherContent = prefs.getString("weather", null);
        String bingPic = prefs.getString("bing_pic", null);

        if(bingPic == null)
        {
            loadImage();
        }
        else
        {
            Glide.with(this).load(bingPic).into(backgroundImage);
        }

        if(weatherContent == null)
        {
            weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }
        else
        {
            Weather weather = Utility.handleWeatherResponse(weatherContent);
            if(weather == null)
            {
                SharedPreferences.Editor editor = prefs.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                weatherId = weather.basic.cityCode;
                showWeatherInfo(weather);
            }
        }

        navButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                requestWeather(weatherId);
            }
        });
    }

    private void loadImage()
    {
        String address = "http://guolin.tech/api/bing_pic";
        HttpUtility.sendOkHttpRequest(address, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(WeatherActivity.this, "获取图片信息失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String bingPicAddress = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPicAddress);
                editor.apply();
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Glide.with(WeatherActivity.this).load(bingPicAddress).into(backgroundImage);
                    }
                });
            }
        });
    }

    public void requestWeather(String weatherId)
    {
        String address = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=35be2fb9e7f24c6098f28fa11a8a7674";
        this.weatherId = weatherId;
        HttpUtility.sendOkHttpRequest(address, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                if(weather != null && weather.status.equals("ok"))
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            swipeRefresh.setRefreshing(false);
                            showWeatherInfo(weather);
                        }
                    });
                }
            }
        });
    }

    private void showWeatherInfo(Weather weather)
    {
        if(weather != null)
        {
            titleCity.setText(weather.basic.cityName);
            titleDate.setText(weather.basic.update.updateTime.split(" ")[1]);
            titleLatitude.setText("纬度:" + weather.basic.latitude);
            titleLongitude.setText("经度:" + weather.basic.longitude);

            nowTemp.setText(weather.now.temperature + "℃");
            nowWeather.setText(weather.now.weatherInfo);
            nowWindDir.setText("风向：" + weather.now.windDir);
            nowWindScale.setText("风级：" + weather.now.windScale + "级");
            nowWindSpeed.setText("风速：" + weather.now.windSpeed + "m/s");

            aqi_pm25.setText(weather.aqi.city.pm25);
            aqi_info.setText(weather.aqi.city.aqi);
            aqi_quality.setText("空气质量：" + weather.aqi.city.quality);

            LayoutInflater inflater = getLayoutInflater();
            forecastLayout.removeAllViews();
            for (Forecast forecast : weather.forecastList)
            {
                View view = inflater.inflate(R.layout.forecast_item, forecastLayout, false);
                ((TextView) view.findViewById(R.id.forecast_date)).setText(forecast.date);
                ((TextView) view.findViewById(R.id.forecast_info)).setText(forecast.more.info);
                ((TextView) view.findViewById(R.id.forecast_max)).setText(forecast.temperature.max + "℃");
                ((TextView) view.findViewById(R.id.forecast_min)).setText(forecast.temperature.min + "℃");
                forecastLayout.addView(view);
            }

            suggestion_comfort.setText("舒适度：" + weather.suggestion.comfort.info);
            suggestion_car_wash.setText("洗车指数：" + weather.suggestion.carWash.info);
            suggestion_sport.setText("运动指数：" + weather.suggestion.sport.info);


            Intent intent = new Intent(this, UpdateWeatherService.class);
            startService(intent);

        }
        else
        {
            Toast.makeText(this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
        }
    }
}
