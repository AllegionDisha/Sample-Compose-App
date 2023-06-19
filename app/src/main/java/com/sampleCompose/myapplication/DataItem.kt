package com.sampleCompose.myapplication

data class DataItem(
    val weather: List<WeatherItem>,
    val main: MainItem,
    val name: String
)
data class WeatherItem(
    val main: String,
    val description: String,
    val icon: String
)

data class MainItem(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)