package com.sampleCompose.myapplication

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class AppViewModel: ViewModel() {
    private var measurementOption = mutableStateOf("standard")
    val MeasurementOption: State<String> get() = measurementOption

    fun setMeasurementOption(option: String) {
        measurementOption.value = option
    }

}