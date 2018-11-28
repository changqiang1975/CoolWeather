package com.changqiang.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Now
{
    @SerializedName("cond_txt")
    public String weatherInfo;

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("wind_dir")
    public String windDir;

    @SerializedName("wind_sc")
    public String windScale;

    @SerializedName("wind_spd")
    public String windSpeed;
}
