package com.smile.colorballs

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.widget.MenuPopupWindow.MenuDropDownListView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InstrumentedKotlin {
    private var appContext: Context? = null
    private var scenario: ActivityScenario<MyActivity>? = null
    private val oneSecond = 1000 // 1 second
    private val threeSeconds = 3000 // 3 seconds
    private val fiveSeconds = 5000 // 5 seconds

    // do use the following because AndroidJUnit4.class is deprecated
    // start using androidx.test.ext.junit.runners.AndroidJUnit4;
    @get:Rule
    var myActivityScenarioRule
            : ActivityScenarioRule<MyActivity> = ActivityScenarioRule(MyActivity::class.java)

    @Before
    fun test_PreRun() {
        Log.d(TAG, "Setting up before each test case.")
        appContext = ApplicationProvider.getApplicationContext()
        scenario = myActivityScenarioRule.scenario
        Espresso.closeSoftKeyboard()
    }

    @Test
    fun test_PackageNameForColorBalls() {
        Assert.assertEquals("com.smile.colorballs",
            appContext!!.packageName)
    }

    @Test
    fun test_GoogleAdMobBannerID() {
        Assert.assertEquals("ca-app-pub-8354869049759576/3904969730",
            ColorBallsApp.googleAdMobBannerID)
    }

    @Test
    fun test_undoSubmenu() {
        onView(withId(R.id.undoGame)).perform(click())
    }

    private fun test_settingSubmenu() {
        onView(withId(R.id.setting)).perform(click())
        onView(withText(R.string.settingStr)).check(matches(isDisplayed()))
        onView(withText(R.string.soundStr)).check(matches(isDisplayed()))
        onView(withText(R.string.playerLevelStr)).check(matches(isDisplayed()))
        onView(withText(R.string.nextBallSettingStr)).check(matches(isDisplayed()))
        onView(withText(R.string.cancelStr)).check(matches(isDisplayed()))
        onView(withText(R.string.okString)).check(matches(isDisplayed()))

        SystemClock.sleep(threeSeconds.toLong())
    }

    @Test
    fun test_settingSubmenuPressBack() {
        test_settingSubmenu()
        Espresso.pressBack()
    }

    @Test
    fun test_settingSubmenuOK() {
        test_settingSubmenu()
        onView(withId(R.id.confirmSettingButton)).perform(click())
    }

    @Test
    fun test_settingSubmenuCancel() {
        test_settingSubmenu()
        onView(withId(R.id.cancelSettingButton)).perform(click())
    }

    @Test
    fun test_moreActionSubmenu() {
        onView(withId(R.id.gameAction)).perform(click())
        onView(withText(R.string.globalTop10Str)).check(matches(isDisplayed()))
        onView(withText(R.string.localTop10Str)).check(matches(isDisplayed()))
        onView(withText(R.string.saveGameStr)).check(matches(isDisplayed()))
        onView(withText(R.string.loadGameStr)).check(matches(isDisplayed()))
        onView(withText(R.string.newGame)).check(matches(isDisplayed()))
        onView(withText(R.string.quitGame)).check(matches(isDisplayed()))
        onView(withText(R.string.privacyPolicyString)).check(matches(isDisplayed()))

        SystemClock.sleep(threeSeconds.toLong())

        Espresso.pressBack()
    }

    private fun actionSubMenu(position: Int) {
        onView(withId(R.id.gameAction)).perform(click())
        onData(CoreMatchers.anything())
            .inRoot(RootMatchers.isPlatformPopup()) // isPlatformPopup() == is in PopupWindow
            .inAdapterView(CoreMatchers.instanceOf(MenuDropDownListView::class.java))
            .atPosition(position) // for the seventh submenu item, here: R.id.privacyPolicy
            .perform(click())
        SystemClock.sleep(oneSecond.toLong())
    }

    @Test
    fun test_globalTop10Submenu() {
        actionSubMenu(0)

        SystemClock.sleep(fiveSeconds.toLong())

        onView(withId(R.id.top10ScoreTitle)).check(matches(isDisplayed()))
        onView(withText(R.string.globalTop10Score)).check(matches(isDisplayed()))
        onView(withId(R.id.top10ListView)).check(matches(isDisplayed()))
        onView(withId(R.id.top10OkButton)).check(matches(isDisplayed()))
        onView(withText(R.string.okString)).check(matches(isDisplayed()))

        SystemClock.sleep(threeSeconds.toLong())

        onView(withId(R.id.top10OkButton)).perform(click()) // Espresso.pressBack();
    }

    @Test
    fun test_localTop10Submenu() {
        actionSubMenu(1)

        SystemClock.sleep(fiveSeconds.toLong())

        onView(withId(R.id.top10ScoreTitle)).check(matches(isDisplayed()))
        onView(withText(R.string.localTop10Score)).check(matches(isDisplayed()))
        onView(withId(R.id.top10ListView)).check(matches(isDisplayed()))
        onView(withId(R.id.top10OkButton)).check(matches(isDisplayed()))
        onView(withText(R.string.okString)).check(matches(isDisplayed()))

        SystemClock.sleep(threeSeconds.toLong())

        onView(withId(R.id.top10OkButton)).perform(click()) // Espresso.pressBack();
    }

    private fun alertDialogFragment(stringId: Int) {
        onView(withId(R.id.text_shown)).check(matches(isDisplayed()))
        onView(withText(stringId)).check(matches(isDisplayed()))
        onView(withId(R.id.dialogfragment_noButton)).check(matches(isDisplayed()))
        onView(withText(R.string.noString)).check(matches(isDisplayed())) // button
        onView(withId(R.id.dialogfragment_okButton)).check(matches(isDisplayed()))
        onView(withText(R.string.okString)).check(matches(isDisplayed())) // button
        SystemClock.sleep(oneSecond.toLong())
    }

    private fun saveGameSubmenu() {
        actionSubMenu(2)
        alertDialogFragment(R.string.sureToSaveGameStr)
        SystemClock.sleep(threeSeconds.toLong())
    }

    @Test
    fun test_saveGameSubmenuOk() {
        saveGameSubmenu()
        onView(withId(R.id.dialogfragment_okButton)).perform(click())
    }

    @Test
    fun test_saveGameSubmenuNo() {
        saveGameSubmenu()
        onView(withId(R.id.dialogfragment_noButton)).perform(click())
    }

    private fun loadGameSubmenu() {
        actionSubMenu(3)
        alertDialogFragment(R.string.sureToLoadGameStr)
        SystemClock.sleep(threeSeconds.toLong())
    }

    @Test
    fun test_loadGameSubmenuOk() {
        loadGameSubmenu()
        onView(withId(R.id.dialogfragment_okButton)).perform(click())
    }

    @Test
    fun test_loadGameSubmenuNo() {
        loadGameSubmenu()
        onView(withId(R.id.dialogfragment_noButton)).perform(click())
    }

    private fun alertDialog() {
        onView(withHint(R.string.nameStr)).check(matches(isDisplayed()))
        onView(withText(R.string.cancelStr)).check(matches(isDisplayed()))
        onView(withText(R.string.submitStr)).check(matches(isDisplayed()))
    }

    private fun newGameSubmenu() {
        actionSubMenu(4)
        alertDialog()
        SystemClock.sleep(threeSeconds.toLong())
    }

    @Test
    fun test_newGameSubmenuSubmit() {
        newGameSubmenu()
        onView(withText(R.string.submitStr)).perform(click())
    }

    @Test
    fun test_newGameSubmenuCancel() {
        newGameSubmenu()
        onView(withText(R.string.cancelStr)).perform(click())
    }

    private fun quitGameSubmenu() {
        actionSubMenu(5)
        alertDialog()
        SystemClock.sleep(threeSeconds.toLong())
        Espresso.closeSoftKeyboard()
    }

    @Test
    fun test_quitGameSubmenuSubmit() {
        quitGameSubmenu()
        onView(withText(R.string.submitStr)).perform(click())
    }

    @Test
    fun test_quitGameSubmenuCancel() {
        quitGameSubmenu()
        onView(withText(R.string.cancelStr)).perform(click())
    }

    @Test
    fun test_privatePolicySubmenu() {
        actionSubMenu(6)
        SystemClock.sleep(fiveSeconds.toLong())
        onView(withId(R.id.privacyPolicyWebView)).check(matches(isDisplayed()))
        Espresso.pressBack()
    }

    @After
    fun test_PostRun() {
        Log.d(TAG, "Cleaning up after each test case.")
    }

    companion object {
        private const val TAG = "InstrumentedKotlin"

        @JvmStatic
        @BeforeClass
        fun test_Setup() {
            Log.d(TAG, "Initializing before all test cases. One time running.")
        }

        @JvmStatic
        @AfterClass
        fun test_CleanUp() {
            Log.d(TAG, "Cleaning up after all test cases. One time running.")
        }
    }
}