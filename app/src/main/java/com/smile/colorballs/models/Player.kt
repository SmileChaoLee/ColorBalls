package com.smile.colorballs.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.smile.colorballs.BR

class Player(name : String = "",
             score : String = "",
             medal : Int = 0) : BaseObservable() {
    @get:Bindable
    var name = name
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    @get:Bindable
    var score = score
        set(value) {
            field = value
            notifyPropertyChanged(BR.score)
        }

    @get:Bindable
    var medal = medal
        set(value) {
            field = value
            notifyPropertyChanged(BR.medal)
        }
}