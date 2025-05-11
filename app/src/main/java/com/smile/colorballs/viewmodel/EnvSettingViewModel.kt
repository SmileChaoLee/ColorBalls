package com.smile.colorballs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smile.colorballs.models.EnvSetting

class EnvSettingViewModel : ViewModel() {
    private val _settings = MutableLiveData<EnvSetting>()
    val settings : LiveData<EnvSetting>
        get() = _settings

    fun setSettings(set: EnvSetting) {
        _settings.value = set
    }

    fun setHasSound(hasSound: Boolean) {
        _settings.value?.let {
            it.hasSound = hasSound
            // EveSetting is a Observable class, so no need to post value
            // Each property changes will trigger notifyPropertyChanged()
            // _settings.postValue(it) or
            // _settings.value = it
        }
    }

    fun setEasyLevel(easyLevel: Boolean) {
        _settings.value?.let {
            it.easyLevel = easyLevel
            // EveSetting is a Observable class, so no need to post value
            // Each property changes will trigger notifyPropertyChanged()
            // _settings.postValue(it) or
            // _settings.value = it
        }
    }

    fun setHasNextBall(hasNextBall: Boolean) {
        _settings.value?.let {
            it.hasNextBall = hasNextBall
            // EveSetting is a Observable class, so no need to post value
            // Each property changes will trigger notifyPropertyChanged()
            // _settings.postValue(it) or
            // _settings.value = it
        }
    }
}