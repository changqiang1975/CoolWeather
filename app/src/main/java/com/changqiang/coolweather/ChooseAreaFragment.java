package com.changqiang.coolweather;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.changqiang.coolweather.db.City;
import com.changqiang.coolweather.db.County;
import com.changqiang.coolweather.db.Province;
import com.changqiang.coolweather.gson.Weather;
import com.changqiang.coolweather.utils.HttpUtility;
import com.changqiang.coolweather.utils.Utility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment
{
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private int currentLevel;

    private Button backButton;
    private TextView titleText;
    private ListView listView;
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private Province currentProvince;
    private City currentCity;
    private County currentCounty;

    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;

    private static final String BASE_ADDRESS = "http://guolin.tech/api/china";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_choose_area, container, false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                switch(currentLevel)
                {
                    case LEVEL_PROVINCE:
                        currentProvince = provinceList.get(i);
                        queryCity();
                        break;

                    case LEVEL_CITY:
                        currentCity = cityList.get(i);
                        queryCounty();
                        break;

                    case LEVEL_COUNTY:
                        currentCounty = countyList.get(i);
                        if(getActivity() instanceof MainActivity)
                        {
                            Intent intent = new Intent(getContext(), WeatherActivity.class);
                            intent.putExtra("weather_id", currentCounty.getWeatherId());
                            startActivity(intent);
                            getActivity().finish();
                        }
                        else if(getActivity() instanceof WeatherActivity)
                        {
                            WeatherActivity activity = (WeatherActivity)getActivity();
                            activity.drawerLayout.closeDrawers();
                            activity.swipeRefresh.setRefreshing(true);
                            activity.requestWeather(currentCounty.getWeatherId());
                        }
                        break;

                    default:
                        break;
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                switch(currentLevel)
                {
                    case LEVEL_COUNTY:
                        queryCity();
                        break;

                    case LEVEL_CITY:
                        queryProvince();
                        break;

                    case LEVEL_PROVINCE:
                    default:
                        break;
                }
            }
        });

        queryProvince();
    }

    private void updateList()
    {
        adapter.notifyDataSetChanged();
        listView.setSelection(0);
    }

    private void queryProvince()
    {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if(provinceList.size() > 0)
        {
            dataList.clear();
            for(Province province : provinceList)
            {
                dataList.add(province.getProvinceName());
            }
            updateList();
            currentLevel = LEVEL_PROVINCE;
        }
        else
        {
            String address = BASE_ADDRESS;
            queryFromServer(address, "province");
        }
    }

    private void queryCity()
    {
        titleText.setText(currentProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid = ?", currentProvince.getId() + "").find(City.class);
        if(cityList.size() > 0)
        {
            dataList.clear();
            for(City city : cityList)
            {
                dataList.add(city.getCityName());
            }
            updateList();
            currentLevel = LEVEL_CITY;
        }
        else
        {
            String address = BASE_ADDRESS + "/" + currentProvince.getProvinceCode();
            queryFromServer(address, "city");
        }
    }

    private void queryCounty()
    {
        titleText.setText(currentCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityid = ?", currentCity.getId() + "").find(County.class);
        if(countyList.size() > 0)
        {
            dataList.clear();
            for(County county : countyList)
            {
                dataList.add(county.getCountyName());
            }
            updateList();
            currentLevel = LEVEL_COUNTY;
        }
        else
        {
            String address = BASE_ADDRESS + "/" + currentProvince.getProvinceCode() + "/" + currentCity.getCityCode();
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address, final String type)
    {
        HttpUtility.sendOkHttpRequest(address, new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                getActivity().runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
                String responseText = response.body().string();
                boolean result = false;
                if (type.equals("province"))
                {
                    result = Utility.handleProvinceResponse(responseText);
                }
                else if (type.equals("city"))
                {
                    result = Utility.handleCityResponse(responseText, currentProvince.getId());
                }
                else if (type.equals("county"))
                {
                    result = Utility.handleCountyResponse(responseText, currentCity.getId());
                }

                if(result)
                {
                    getActivity().runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if(type.equals("province"))
                                queryProvince();
                            else if(type.equals("city"))
                                queryCity();
                            else if(type.equals("county"))
                                queryCounty();
                        }
                    });
                }
            }
        });
    }

}
