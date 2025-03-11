package com.smile.colorballs.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.smile.colorballs.models.Settings

class SettingViewModel() : ViewModel() {
    private var _hasSound = MutableLiveData<Boolean>()
    var hasSound : MutableLiveData<Boolean>
        get() = _hasSound
        set(value) {_hasSound = value}
    private var _easyLevel = MutableLiveData<Boolean>()
    var easyLevel : MutableLiveData<Boolean>
        get() = _easyLevel
        set(value) {_easyLevel = value}
    private var _hasNextBall = MutableLiveData<Boolean>()
    var hasNextBall : MutableLiveData<Boolean>
        get() = _hasNextBall
        set(value) {_hasNextBall = value}
    var settings : Settings
        get() {
            val sound = _hasSound?.value ?: true
            val level = _easyLevel?.value ?: true
            val next = _hasNextBall.value ?: true
            return Settings(sound, level, next)
        }
        set(value) {
            _hasSound.value = value.hasSound
            _easyLevel.value = value.easyLevel
            _hasNextBall.value = value.hasNextBall
        }
}