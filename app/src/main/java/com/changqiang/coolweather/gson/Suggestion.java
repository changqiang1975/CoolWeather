package com.changqiang.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion
{
    @SerializedName("comf")
    public Comfort comfort;

    public Sport sport;

    @SerializedName("cw")
    public CarWash carWash;

    public class CarWash
    {
        @SerializedName("txt")
        public String info;

        @SerializedName("brf")
        public String degree_of_wash;
    }

    public class Sport
    {
        @SerializedName("txt")
        public String info;

        @SerializedName("brf")
        public String degree_of_sport;
    }

    public class Comfort
    {
        @SerializedName("txt")
        public String info;

        @SerializedName("brf")
        public String degree_of_comfort;
    }
}
