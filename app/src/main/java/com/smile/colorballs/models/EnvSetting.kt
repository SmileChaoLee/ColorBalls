package com.smile.colorballs.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.smile.colorballs.BR

class EnvSetting(hasSound : Boolean,
                 easyLevel : Boolean,
                 hasNextBall : Boolean) : BaseObservable() {
     @get:Bindable
     var hasSound = hasSound
         set(value) {
            field = value
            notifyPropertyChanged(BR.hasSound)
         }

    @get:Bindable
    var easyLevel = easyLevel
        set(value) {
            field = value
            notifyPropertyChanged(BR.easyLevel)
        }

    @get:Bindable
    var hasNextBall = hasNextBall
        set(value) {
            field = value
            notifyPropertyChanged(BR.hasNextBall)
        }
}