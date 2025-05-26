package com.smile.colorballs

import android.content.Context
import android.content.Intent
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.ToggleButton
import androidx.lifecycle.Lifecycle.State
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.views.xml_base.MyActivity
import com.smile.colorballs.views.xml_base.SettingActivity
import com.smile.colorballs.views.xml_base.Top10Activity
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class TestActivities {

    private lateinit var appContext : Context

    @Before
    fun setUp() {
        println("setUp")
        appContext = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        println("tearDown")
    }

    @Test
    fun test_MyActivityScenario() {
        println("test_MyActivityScenario")
        ActivityScenario.launch(MyActivity::class.java).use {
            it.moveToState(State.RESUMED)
            Assert.assertEquals(State.RESUMED, it.state)
            it.onActivity { activity ->
                val textView: TextView = activity.findViewById(R.id.currentScoreTextView)
                onView(withId(R.id.currentScoreTextView)).check(matches(isDisplayed()))
                Assert.assertEquals("0", textView.text?.trim())
            }
            it.moveToState(State.DESTROYED)
            it.close()
        }
    }

    @Test
    fun test_MyActivityController() {
        println("test_MyActivityController")
        Robolectric.buildActivity(MyActivity::class.java).use {
            it.create()
            it.start()
            it.resume().visible()
            val activity = it.get()
            val textView: TextView = activity.findViewById(R.id.currentScoreTextView)
            onView(withId(R.id.currentScoreTextView)).check(matches(isDisplayed()))
            Assert.assertEquals("0", textView.text?.trim())
            it.pause()
            it.stop()
            it.destroy()
            it.close()
        }
    }

    private fun settingActivity() : ActivityController<SettingActivity>? {
        val controller : ActivityController<SettingActivity>? =
            Robolectric.buildActivity(SettingActivity::class.java)
        controller?.let {
            it.create()
            it.start()
            it.resume().visible()
            val activity = it.get()
            val resources = activity.resources

            val settingTitle = activity.findViewById<TextView>(R.id.settingTitle)
            Assert.assertNotNull("settingTitle is null", settingTitle)
            Assert.assertEquals(resources.getString(R.string.settingStr),
                settingTitle.text.toString())

            val soundSettingTitle = activity.findViewById<TextView>(R.id.soundSettingTitle)
            Assert.assertNotNull("soundSettingTitle is null", soundSettingTitle)
            Assert.assertEquals(resources.getString(R.string.soundStr),
                soundSettingTitle.text.toString())
            val soundButton = activity.findViewById<ToggleButton>(R.id.soundSwitch)
            Assert.assertNotNull("toggleButton is null", soundButton)

            val levelSettingTitle = activity.findViewById<TextView>(R.id.levelSettingTitle)
            Assert.assertNotNull("levelSettingTitle is null", levelSettingTitle)
            Assert.assertEquals(resources.getString(R.string.playerLevelStr),
                levelSettingTitle.text.toString())
            val levelButton = activity.findViewById<ToggleButton>(R.id.levelSwitch)
            Assert.assertNotNull("levelButton is null", levelButton)

            val nextBallSettingTitle = activity.findViewById<TextView>(R.id.nextBallSettingTitle)
            Assert.assertNotNull("nextBallSettingTitle is null", nextBallSettingTitle)
            Assert.assertEquals(resources.getString(R.string.nextBallSettingStr),
                nextBallSettingTitle.text.toString())
            val nextBallButton = activity.findViewById<ToggleButton>(R.id.nextBallSettingSwitch)
            Assert.assertNotNull("nextBallButton is null", nextBallButton)

            val cancelButton = activity.findViewById<Button>(R.id.cancelSettingButton)
            Assert.assertNotNull("cancelButton is null", cancelButton)
            Assert.assertEquals(resources.getString(R.string.cancelStr),
                cancelButton.text.toString())

            val confirmSettingButton = activity.findViewById<Button>(R.id.confirmSettingButton)
            Assert.assertNotNull("confirmSettingButton is null", confirmSettingButton)
            Assert.assertEquals(resources.getString(R.string.okString),
                confirmSettingButton.text.toString())

            onView(withText(R.string.settingStr)).check(matches(isDisplayed()))
            onView(withText(R.string.soundStr)).check(matches(isDisplayed()))
            onView(withText(R.string.playerLevelStr)).check(matches(isDisplayed()))
            onView(withText(R.string.nextBallSettingStr)).check(matches(isDisplayed()))
            onView(withText(R.string.cancelStr)).check(matches(isDisplayed()))
            onView(withText(R.string.okString)).check(matches(isDisplayed()))
        }

        return controller
    }

    @Test
    fun test_SettingActivity() {
        println("test_SettingActivity")
        settingActivity()?.let {
            it.pause()
            it.stop()
            it.destroy()
            it.close()
        }
    }

    @Test
    fun test_SettingActivityOk() {
        println("test_SettingActivityOk")
        settingActivity()?.let {
            it.get().findViewById<Button>(R.id.confirmSettingButton).performClick()
            it.pause()
            it.stop()
            it.destroy()
            it.close()
        }
    }

    @Test
    fun test_SettingActivityCancel() {
        println("test_SettingActivityCancel")
        settingActivity()?.let {
            it.get().findViewById<Button>(R.id.cancelSettingButton).performClick()
            it.pause()
            it.stop()
            it.destroy()
            it.close()
        }
    }

    private fun top10Activity(stringId : Int) : ActivityController<Top10Activity>? {
        val tmpIntent = Intent().apply {
            putExtra(Constants.TOP10_TITLE_NAME, appContext.resources.getString(stringId))
        }
        val controller : ActivityController<Top10Activity>? =
            Robolectric.buildActivity(Top10Activity::class.java, tmpIntent)
        controller?.let {
            it.create()
            it.start()
            it.resume().visible()
            val activity = it.get()
            val resources = activity.resources

            val top10ScoreTitle = activity.findViewById<TextView>(R.id.top10ScoreTitle)
            Assert.assertNotNull("top10ScoreTitle is null", top10ScoreTitle)
            Assert.assertEquals(resources.getString(stringId),
                top10ScoreTitle.text.toString())

            val top10ListView = activity.findViewById<ListView>(R.id.top10ListView)
            Assert.assertNotNull("top10ListView is null", top10ListView)

            val top10OkButton = activity.findViewById<Button>(R.id.top10OkButton)
            Assert.assertNotNull("top10OkButton is null", top10OkButton)
            Assert.assertEquals(resources.getString(R.string.okStr),
                top10OkButton.text.toString())

            onView(withId(R.id.top10ScoreTitle)).check(matches(isDisplayed()))
            onView(withText(stringId)).check(matches(isDisplayed()))
            onView(withId(R.id.top10ListView)).check(matches(isDisplayed()))
            onView(withId(R.id.top10OkButton)).check(matches(isDisplayed()))
            onView(withText(R.string.okString)).check(matches(isDisplayed()))
        }

        return controller
    }

    private fun globalTop10Activity(str : String) : ActivityController<Top10Activity>? {
        Assert.assertEquals(appContext.resources.getString(R.string.globalTop10Score),
            str)
        return top10Activity(R.string.globalTop10Score)
    }

    private fun localTop10Activity(str : String) : ActivityController<Top10Activity>? {
        Assert.assertEquals(appContext.resources.getString(R.string.localTop10Score),
            str)
        return top10Activity(R.string.localTop10Score)
    }

    @Test
    fun test_globalTop10ActivityOk() {
        println("test_globalTop10ActivityOk")
        val str = appContext.resources.getString(R.string.globalTop10Score)
        globalTop10Activity(str)?.let {
            it.get().findViewById<Button>(R.id.top10OkButton).performClick()
            it.pause()
            it.stop()
            it.destroy()
            it.close()
        }
    }

    @Test
    fun test_globalTop10ActivityPressBack() {
        println("test_globalTop10ActivityPressBack")
        val str = appContext.resources.getString(R.string.globalTop10Score)
        globalTop10Activity(str)?.let {
            Espresso.pressBack()
            it.pause()
            it.stop()
            it.destroy()
            it.close()
        }
    }

    @Test
    fun test_localTop10ActivityOk() {
        println("test_localTop10ActivityOk")
        val str = appContext.resources.getString(R.string.localTop10Score)
        localTop10Activity(str)?.let {
            it.get().findViewById<Button>(R.id.top10OkButton).performClick()
            it.pause()
            it.stop()
            it.destroy()
            it.close()
        }
    }
    @Test
    fun test_localTop10ActivityPressBack() {
        println("test_localTop10ActivityPressBack")
        val str = appContext.resources.getString(R.string.localTop10Score)
        localTop10Activity(str)?.let {
            Espresso.pressBack()
            it.pause()
            it.stop()
            it.destroy()
            it.close()
        }
    }

}