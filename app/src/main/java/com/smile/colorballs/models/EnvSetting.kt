package com.smile.colorballs.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.smile.colorballs.BR

class EnvSetting(hasSound : Boolean = true,
                 easyLevel : Boolean = true,
                 hasNextBall : Boolean = true) : BaseObservable() {
     @get:Bindable
     var hasSound = hasSound
         set(value) {
             if (field != value) {
                 field = value
                 notifyPropertyChanged(BR.hasSound)
             }
         }

    @get:Bindable
    var easyLevel = easyLevel
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.easyLevel)
            }
        }

    @get:Bindable
    var hasNextBall = hasNextBall
        set(value) {
            if (field != value) {
                field = value
                notifyPropertyChanged(BR.hasNextBall)
            }
        }

    fun copy(envSetting: EnvSetting): EnvSetting {
        val tempEnvSetting = EnvSetting()
        tempEnvSetting.hasSound = envSetting.hasSound
        tempEnvSetting.easyLevel = envSetting.easyLevel
        tempEnvSetting.hasNextBall = envSetting.hasNextBall
        return tempEnvSetting
    }
}