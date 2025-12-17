package com.smile.smilelibraries.utilities

import android.app.Activity
import android.content.Intent
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import com.smile.smilelibraries.R

object AppLinkUtil {

    data class AndroidApp
        (val appName: String,
         val icon: Int,
         val appUrl: String)

    const val KARAOKE_LINK = "https://play.google.com/store/apps/details?id=com.smile.karaokeplayer"
    const val VIDEO_LINK = "https://play.google.com/store/apps/details?id=com.smile.videoplayer"
    const val KARAOKE_TV_LINK = "https://play.google.com/store/apps/details?id=com.smile.karaoketvplayer"
    const val YOUTUBE_LINK = "https://play.google.com/store/apps/details?id=com.smile.youtubeplayer"
    const val COLOR_BALLS_LINK = "https://play.google.com/store/apps/details?id=com.smile.colorballs"
    const val DROP_COLOR_BALLS_LINK = "https://play.google.com/store/apps/details?id=com.smile.dropcolorballs"
    const val BALLS_REMOVER_LINK = "https://play.google.com/store/apps/details?id=com.smile.ballsremover"
    const val BOUNCY_BALL_LINK = "https://play.google.com/store/apps/details?id=com.smile.bouncyball"
    const val FIVE_COLOR_BALLS_LINK = "https://play.google.com/store/apps/details?id=com.smile.fivecolorballs"

    fun getAppList(activity: Activity): List<AndroidApp> {
        val appList = ArrayList<AndroidApp>()
        appList.add(AndroidApp(activity.getString(R.string.karaoke_app_name),
            R.drawable.karaoke_app_icon, KARAOKE_LINK))
        appList.add(AndroidApp(activity.getString(R.string.video_app_name),
            R.drawable.video_app_icon, VIDEO_LINK))
        appList.add(AndroidApp(activity.getString(R.string.karaoke_tv_app_name),
            R.drawable.karaoke_tv_app_icon, KARAOKE_TV_LINK))
        appList.add(AndroidApp(activity.getString(R.string.youtube_app_name),
            R.drawable.youtube_app_icon, YOUTUBE_LINK))
        appList.add(AndroidApp(activity.getString(R.string.color_balls_name),
            R.drawable.color_balls_app_icon, COLOR_BALLS_LINK))
        appList.add(AndroidApp(activity.getString(R.string.drop_cballs_name),
            R.drawable.dropcolorballs_app_icon, DROP_COLOR_BALLS_LINK))
        appList.add(AndroidApp(activity.getString(R.string.balls_remover_name),
            R.drawable.balls_remover_app_icon, BALLS_REMOVER_LINK))
        appList.add(AndroidApp(activity.getString(R.string.bouncy_ball_name),
            R.drawable.bouncy_ball_app_icon, BOUNCY_BALL_LINK))
        appList.add(AndroidApp(activity.getString(R.string.five_color_balls_name),
            R.drawable.five_color_balls_app, FIVE_COLOR_BALLS_LINK))

        return appList
    }

    fun startAppLinkOnStore(activity: Activity, link: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
    }

    fun startAppLinkOnStore(activity: FragmentActivity, link: String) {
        activity.startActivity(Intent(Intent.ACTION_VIEW, link.toUri()))
    }
}