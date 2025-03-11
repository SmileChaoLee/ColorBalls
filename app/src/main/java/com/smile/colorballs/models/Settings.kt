package com.smile.colorballs.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Settings internal constructor(var hasSound : Boolean, var easyLevel : Boolean,
                                    var hasNextBall : Boolean) : Parcelable