package com.smile.colorballs.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Settings internal constructor(
    var hasSound : Boolean = true,
    // true --> easy for 5 color balls
    // false --> difficult for 6 color balls
    var easyLevel : Boolean = true,
    var hasNext : Boolean = true): Parcelable