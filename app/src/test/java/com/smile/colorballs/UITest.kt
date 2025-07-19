package com.smile.colorballs

import android.content.Context
import android.content.res.Resources
import android.os.SystemClock
import androidx.lifecycle.Lifecycle.State
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.views.ColorBallActivity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricTestRunner::class)
@Config(application = ColorBallsApp::class, manifest = "AndroidManifest.xml")
class UITest {
    private var appContext : Context? = null
    private lateinit var activity : ColorBallActivity
    private lateinit var resource : Resources
    private val oneSecond = 1000 // 1 second
    private val threeSeconds = 3000 // 3 seconds
    private val fiveSeconds = 5000 // 5 seconds

    @Before
    fun test_PreRun() {
        println("test_PreRun.before each test case.")
        appContext = ApplicationProvider.getApplicationContext()
        controller = Robolectric.buildActivity(ColorBallActivity::class.java)
        controller?.let {
            it.setup().visible()
            activity = it.get()
            resource = activity.resources
        }
        Espresso.closeSoftKeyboard()
    }

    @After
    fun test_PostRun() {
        println("test_PostRun")
        scenario?.apply {
            moveToState(State.DESTROYED)
            close()
        }
        controller?.apply {
            close()
        }
    }

    @Test
    fun test_PackageNameForColorBalls() {
        Assert.assertEquals(PACKAGE_NAME, appContext!!.packageName)
    }

    @Test
    fun test_GoogleAdMobBannerID() {
        Assert.assertEquals("ca-app-pub-8354869049759576/3904969730",
            ColorBallsApp.googleAdMobBannerID)
    }

    /*
    @Test
    fun test_undoSubmenu() {
        onView(withId(R.id.undoGame)).perform(click())
    }

    @Test
    fun settingSubmenu() {
        println("settingSubmenu")
        onView(withId(R.id.setting)).perform(click())
        controller?.let {
            val shadowActivity : ShadowActivity = shadowOf(activity)
            val expectedActivityName = "com.smile.colorballs.views.xml_base.SettingActivity"
            val actualIntent = shadowActivity.nextStartedActivity
            val actualActivityName = actualIntent.component?.className
            println("settingSubmenu.actualActivityName = $actualActivityName")
            Assert.assertSame(expectedActivityName, actualActivityName)
            SystemClock.sleep(threeSeconds.toLong())
        }
    }
    */

    /*
    private fun actionSubMenu() : MenuItem? {
        println("actionSubMenu")
        controller?.let {
            // val activity = it.get()
            val toolbar: Toolbar = activity.findViewById(R.id.colorBallToolbar)
            val menu: Menu = toolbar.menu
            val menuItem: MenuItem = menu.findItem(R.id.gameAction)
            Assert.assertTrue(menuItem.hasSubMenu())
            menuItem.subMenu?.let { subIt ->
                Assert.assertTrue(subIt.size() == 7)    // 7 submenu items
                Assert.assertEquals(resource.getString(R.string.globalTop10Str),
                    subIt.getItem(0).title.toString())
                Assert.assertEquals(resource.getString(R.string.localTop10Str),
                    subIt.getItem(1).title.toString())
                Assert.assertEquals(resource.getString(R.string.saveGameStr),
                    subIt.getItem(2).title.toString())
                Assert.assertEquals(resource.getString(R.string.loadGameStr),
                    subIt.getItem(3).title.toString())
                Assert.assertEquals(resource.getString(R.string.newGame),
                    subIt.getItem(4).title.toString())
                Assert.assertEquals(resource.getString(R.string.quitGame),
                    subIt.getItem(5).title.toString())
                Assert.assertEquals(resource.getString(R.string.privacyPolicyString),
                    subIt.getItem(6).title.toString())
            }
            return menuItem
        } ?: run {
            println("actionSubMenu.controller is null.")
            return null
        }
    }
    */

    /*
    @Test
    fun test_moreActionSubmenu() {
        actionSubMenu()
        SystemClock.sleep(threeSeconds.toLong())
        Espresso.pressBack()
    }
    */

    @Test
    fun test_globalTop10SubmenuService() {
        controller?.let {
            val shadowActivity = shadowOf(activity)
            val expectedServiceName = "com.smile.colorballs.services.GlobalTop10Service"
            val actualIntent = shadowActivity.nextStartedService
            val actualServiceName = actualIntent.component?.className
            println("test_globalTop10Submenu.actualServiceName = $actualServiceName")
            Assert.assertSame(expectedServiceName, actualServiceName)
            val actualString = actualIntent.getStringExtra(Constants.GAME_ID_STRING)
            println("test_globalTop10Submenu.actualString = $actualString")
            Assert.assertEquals("1", actualString)
        }
    }

    /*
    @Test
    fun test_globalTop10SubmenuCoroutine() = runTest {
        Top10Coroutine.getGlobalAndSendBack(appContext!!)
        println("test_globalTop10SubmenuCoroutine.Top10Coroutine.isBroadcastSent = " +
                "${Top10Coroutine.isBroadcastSent}")
        Assert.assertTrue(Top10Coroutine.isBroadcastSent)
    }

    @Test
    fun test_localTop10SubmenuCoroutine() = runTest {
        Top10Coroutine.getLocalAndSendBack(appContext!!)
        println("test_localTop10SubmenuCoroutine.Top10Coroutine.isBroadcastSent = " +
                "${Top10Coroutine.isBroadcastSent}")
        Assert.assertTrue(Top10Coroutine.isBroadcastSent)
    }
    */

    /*
    private fun alertDialogFragment(stringId: Int, tag : String) : AlertDialogFragment? {
        println("alertDialogFragment")
        controller?.let {
            val fragment = activity.supportFragmentManager
                .findFragmentByTag(tag)
            println("alertDialogFragment.fragment = $fragment")
            val alertDialogFragment = fragment as AlertDialogFragment
            alertDialogFragment.apply {
                val actualAlertDialogName = javaClass.name
                println("alertDialogFragment.actualAlertDialogName = $actualAlertDialogName")
                Assert.assertSame(
                    "com.smile.smilelibraries.alertdialogfragment.AlertDialogFragment",
                    actualAlertDialogName
                )
                Assert.assertEquals(
                    resource.getString(stringId),
                    text_shown.text.toString()
                )
                Assert.assertEquals(
                    resource.getString(R.string.okString),
                    okButton.text.toString()
                )
                Assert.assertEquals(
                    resource.getString(R.string.noString),
                    noButton.text.toString()
                )
            }
            return alertDialogFragment
        } ?: run {
            Assert.fail("alertDialogFragment is null")
            return null
        }
    }
    */
        /*
    private fun saveGameSubmenu() : AlertDialogFragment? {
        whichActionSubMenu(2)
        return alertDialogFragment(R.string.sureToSaveGameStr, Constants.SURE_SAVE_DIALOG_TAG)
    }
    */

    /*
    @Test
    fun test_saveGameSubmenuOk() {
        println("test_saveGameSubmenuOk")
        saveGameSubmenu()?.let {
            println("test_saveGameSubmenuOk.alertDialogFragment = $it")
            it.okButton.performClick()
        }
    }
    */

    /*
    @Test
    fun test_saveGameSubmenuNo() {
        println("test_saveGameSubmenuNo")
        saveGameSubmenu()?.let {
            println("test_saveGameSubmenuNo.alertDialogFragment = $it")
            it.noButton.performClick()
        }
    }
    */

    /*
    private fun loadGameSubmenu() : AlertDialogFragment? {
        whichActionSubMenu(3)
        return alertDialogFragment(R.string.sureToLoadGameStr, Constants.SURE_LOAD_DIALOG_TAG)
    }
    */

    /*
    @Test
    fun test_loadGameSubmenuOk() {
        println("test_loadGameSubmenuOk")
        loadGameSubmenu()?.let {
            println("test_loadGameSubmenuOk.alertDialogFragment = $it")
            it.okButton.performClick()
        }
    }
    */

    /*
    @Test
    fun test_loadGameSubmenuNo() {
        println("test_loadGameSubmenuNo")
        loadGameSubmenu()?.let {
            println("test_loadGameSubmenuNo.alertDialogFragment = $it")
            it.noButton.performClick()
        }
    }
    */

    /*
    private fun alertDialog() : AlertDialog? {
        controller?.let {
            val alertDialog = (ShadowAlertDialog.getLatestDialog() as AlertDialog).apply {
                val actualAlertDialogName = javaClass.name
                println("alertDialog.actualAlertDialogName = $actualAlertDialogName")
                Assert.assertSame("androidx.appcompat.app.AlertDialog",
                    actualAlertDialogName)
                val summitButton  = getButton(AlertDialog.BUTTON_POSITIVE)
                Assert.assertEquals(resource.getString(R.string.submitStr),
                    summitButton.text.toString())
                val cancelButton  = getButton(AlertDialog.BUTTON_NEGATIVE)
                Assert.assertEquals(resource.getString(R.string.cancelStr),
                    cancelButton.text.toString())
            }
            return alertDialog
        } ?: run {
            Assert.fail("alertDialog is null")
            return null
        }
    }
    */

    /*
    private fun newGameSubmenu() : AlertDialog? {
        whichActionSubMenu(4)
        return alertDialog()
    }
    */

    /*
    @Test
    fun test_newGameSubmenuSubmit() {
        println("test_newGameSubmenuSubmit")
        newGameSubmenu()?.let {
            println("test_newGameSubmenuSubmit.alertDialog = $it")
            it.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        }
    }
    */

    /*
    @Test
    fun test_newGameSubmenuCancel() {
        println("test_newGameSubmenuCancel")
        newGameSubmenu()?.let {
            println("test_newGameSubmenuCancel.alertDialog = $it")
            it.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
        }
    }
    */

    /*
    private fun quitGameSubmenu() : AlertDialog? {
        whichActionSubMenu(5)
        return alertDialog()
    }
    */

    /*
    @Test
    fun test_quitGameSubmenuSubmit() {
        println("test_quitGameSubmenuSubmit")
        quitGameSubmenu()?.let {
            println("test_quitGameSubmenuSubmit.alertDialog = $it")
            it.getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        }
    }
    */

    /*
    @Test
    fun test_quitGameSubmenuCancel() {
        println("test_quitGameSubmenuCancel")
        quitGameSubmenu()?.let {
            println("test_quitGameSubmenuCancel.alertDialog = $it")
            it.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
        }
    }
    */

    /*
    @Test
    fun test_privatePolicySubmenu() {
        println("test_privatePolicySubmenu")
        whichActionSubMenu(6)
        SystemClock.sleep(fiveSeconds.toLong())
        // onView(withId(R.id.privacyPolicyWebView)).check(matches(isDisplayed()))
        Espresso.pressBack()
    }
    */

    companion object {
        private const val PACKAGE_NAME = "com.smile.colorballs"
        private var scenario : ActivityScenario<ColorBallActivity>? = null
        private var controller : ActivityController<ColorBallActivity>? = null

        @JvmStatic
        @BeforeClass
        fun test_Setup() {
            println("test_Setup.Initializing before all test cases. One time running.")
        }

        @JvmStatic
        @AfterClass
        fun test_CleanUp() {
            println("Cleaning up after all test cases. One time running.")
        }
    }
}