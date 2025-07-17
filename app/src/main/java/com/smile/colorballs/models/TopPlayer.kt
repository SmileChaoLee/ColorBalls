package com.smile.colorballs.models

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.smile.colorballs.BR
import com.smile.smilelibraries.models.Player

class TopPlayer(
    player: Player = Player(),
    medal : Int = 0): BaseObservable() {
    @get:Bindable
    var player: Player = player
        set(value) {
            field = value
            notifyPropertyChanged(BR.player)
        }
    @get:Bindable
    var medal = medal
        set(value) {
            field = value
            notifyPropertyChanged(BR.medal)
        }
}