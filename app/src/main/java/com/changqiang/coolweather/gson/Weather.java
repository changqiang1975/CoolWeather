package com.changqiang.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather
{
    public Basic basic;
    public Now now;
    public AQI aqi;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
