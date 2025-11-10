package com.smile.colorballs_main.models

import android.os.Parcelable
import com.smile.colorballs_main.constants.Constants
import kotlinx.parcelize.Parcelize

@Parcelize
class Settings internal constructor(
    var hasSound : Boolean = true,
    // 1 --> easy for 5 color balls
    // 2 --> difficult for 6 color balls
    var gameLevel : Int = Constants.GAME_LEVEL_1,
    var hasNext : Boolean = true): Parcelable