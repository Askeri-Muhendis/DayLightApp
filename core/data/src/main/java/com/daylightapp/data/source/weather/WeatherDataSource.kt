package com.daylightapp.data.source.weather

import com.daylightapp.common.NetworkResult
import com.daylightapp.data.dto.weather.current.CurrentWeather
import com.daylightapp.data.dto.weather.fiveday.WeatherResponse

interface WeatherDataSource {
    suspend fun getFiveDayWeatherForecast(lat : String, lon : String) : NetworkResult<WeatherResponse>
    suspend fun getCurrentDayWeather(lat : String,lon : String) : NetworkResult<CurrentWeather>
}