package com.sukrithi.weatherpoc

import com.google.gson.annotations.SerializedName


data class Rain(
    @SerializedName("1h") var onehour: Double? = null
)
