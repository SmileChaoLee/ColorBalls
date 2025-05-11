package com.smile.colorballs.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smile.colorballs.models.Settings

class SettingViewModel : ViewModel() {
    private val _hasSound = MutableLiveData<Boolean>()
    val hasSound : LiveData<Boolean>
        get() = _hasSound

    private val _easyLevel = MutableLiveData<Boolean>()
    val easyLevel : LiveData<Boolean>
        get() = _easyLevel

    private val _hasNextBall = MutableLiveData<Boolean>()
    val hasNextBall : LiveData<Boolean>
        get() = _hasNextBall

    var settings : Settings
        get() {
            val sound = _hasSound.value ?: true
            val level = _easyLevel.value ?: true
            val next = _hasNextBall.value ?: true
            return Settings(sound, level, next)
        }
        set(value) {
            _hasSound.value = value.hasSound
            _easyLevel.value = value.easyLevel
            _hasNextBall.value = value.hasNextBall
        }

    fun setHasSound(hasSound: Boolean) {
        _hasSound.value = hasSound
    }

    fun setEasyLevel(easyLevel: Boolean) {
        _easyLevel.value = easyLevel
    }

    fun setHasNextBall(hasNextBall: Boolean) {
        _hasNextBall.value = hasNextBall
    }
}