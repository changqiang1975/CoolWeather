package com.changqiang.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic
{
    @SerializedName("cid")
    public String cityCode;

    @SerializedName("lat")
    public String latitude;

    @SerializedName("lon")
    public String longitude;

    @SerializedName("city")
    public String cityName;

    public Update update;

    public class Update
    {
        @SerializedName("loc")
        public String updateTime;
    }
}
