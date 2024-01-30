package com.smile.colorballs.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.google.gson.annotations.SerializedName
import com.smile.colorballs.BR

class Player(name : String,
             score : String,
             medal : Int) : BaseObservable() {
    @SerializedName("name")
    @get:Bindable
    var name = name
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    @SerializedName("score")
    @get:Bindable
    var score = score
        set(value) {
            field = value
            notifyPropertyChanged(BR.score)
        }

    @SerializedName("medal")
    @get:Bindable
    var medal = medal
        set(value) {
            field = value
            notifyPropertyChanged(BR.medal)
        }
}