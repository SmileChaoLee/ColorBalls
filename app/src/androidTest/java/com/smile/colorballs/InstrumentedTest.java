package com.smile.colorballs;
import android.content.Context;
import android.os.SystemClock;
import androidx.appcompat.widget.MenuPopupWindow;

import android.util.Log;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {
    private static final String TAG = "InstrumentedTest";
    private static Context appContext;
    private ActivityScenario scenario;
    private int oneWaitSeconds = 1000; // 1 second
    private int threeWaitSeconds = 3000; // 3 seconds
    private int fiveWaitSeconds = 5000; // 5 seconds

    @BeforeClass
    public static void test_Setup() {
        appContext = ApplicationProvider.getApplicationContext();
        Log.d(TAG, "Initializing before all test cases. One time running.");
    }

    // do use the following because AndroidJUnit4.class is deprecated
    // start using androidx.test.ext.junit.runners.AndroidJUnit4;
    @Rule
    public ActivityScenarioRule<MyActivity> myActivityScenarioRule
            = new ActivityScenarioRule<>(MyActivity.class);

    @Before
    public void test_PreRun() {
        scenario = myActivityScenarioRule.getScenario();
        Espresso.closeSoftKeyboard();
        Log.d(TAG, "Setting up before each test case.");
    }

    private void actionSubMenu(int position) {
        onView(withId(R.id.gameAction)).perform(click());
        onData(CoreMatchers.anything())
                .inRoot(RootMatchers.isPlatformPopup()) // isPlatformPopup() == is in PopupWindow
                .inAdapterView(CoreMatchers.instanceOf(MenuPopupWindow.MenuDropDownListView.class))
                .atPosition(position) // for the seventh submenu item, here: R.id.privacyPolicy
                .perform(click());
        SystemClock.sleep(oneWaitSeconds);
    }

    private void alertDialogFragment(int stringId) {
        onView(withId(R.id.text_shown)).check(matches(isDisplayed()));
        onView(withText(stringId)).check(matches(isDisplayed()));
        onView(withId(R.id.dialogfragment_noButton)).check(matches(isDisplayed()));
        onView(withText(R.string.noString)).check(matches(isDisplayed()));  // button
        onView(withId(R.id.dialogfragment_okButton)).check(matches(isDisplayed()));
        onView(withText(R.string.okString)).check(matches(isDisplayed()));  // button
        SystemClock.sleep(oneWaitSeconds);
    }

    @Test
    public void test_PackageNameForColorBalls() {
        assertEquals("com.smile.colorballs",
                appContext.getPackageName());
    }

    @Test
    public void test_GoogleAdMobBannerID() {
        assertEquals("ca-app-pub-8354869049759576/3904969730",
                ColorBallsApp.googleAdMobBannerID);
    }

    @Test
    public void test_undoSubmenu() {
        onView(withId(R.id.undoGame)).perform(click());
    }

    @Test
    public void test_settingSubmenuPressBack() {
        test_settingSubmenu();
        Espresso.pressBack();
    }

    @Test
    public void test_settingSubmenuOK() {
        test_settingSubmenu();
        onView(withId(R.id.confirmSettingButton)).perform(click());
    }

    @Test
    public void test_settingSubmenuCancel() {
        test_settingSubmenu();
        onView(withId(R.id.cancelSettingButton)).perform(click());
    }

    private void test_settingSubmenu() {
        onView(withId(R.id.setting)).perform(click());
        onView(withText(R.string.settingStr)).check(matches(isDisplayed()));
        onView(withText(R.string.soundStr)).check(matches(isDisplayed()));
        onView(withText(R.string.playerLevelStr)).check(matches(isDisplayed()));
        onView(withText(R.string.nextBallSettingStr)).check(matches(isDisplayed()));
        onView(withText(R.string.cancelStr)).check(matches(isDisplayed()));
        onView(withText(R.string.okString)).check(matches(isDisplayed()));

        SystemClock.sleep(threeWaitSeconds);
    }

    @Test
    public void test_moreActionSubmenu() {
        onView(withId(R.id.gameAction)).perform(click());
        onView(withText(R.string.globalTop10Str)).check(matches(isDisplayed()));
        onView(withText(R.string.localTop10Str)).check(matches(isDisplayed()));
        onView(withText(R.string.saveGameStr)).check(matches(isDisplayed()));
        onView(withText(R.string.loadGameStr)).check(matches(isDisplayed()));
        onView(withText(R.string.newGame)).check(matches(isDisplayed()));
        onView(withText(R.string.quitGame)).check(matches(isDisplayed()));
        onView(withText(R.string.privacyPolicyString)).check(matches(isDisplayed()));

        SystemClock.sleep(threeWaitSeconds);

        Espresso.pressBack();
    }

    @Test
    public void test_globalTop10Submenu() {
        actionSubMenu(0);

        SystemClock.sleep(fiveWaitSeconds);

        onView(withId(R.id.top10ScoreTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.globalTop10Score)).check(matches(isDisplayed()));
        onView(withId(R.id.top10ListView)).check(matches(isDisplayed()));
        onView(withId(R.id.top10OkButton)).check(matches(isDisplayed()));
        onView(withText(R.string.okString)).check(matches(isDisplayed()));

        SystemClock.sleep(threeWaitSeconds);

        onView(withId(R.id.top10OkButton)).perform(click());    // Espresso.pressBack();
    }

    @Test
    public void test_localTop10Submenu() {
        actionSubMenu(1);

        SystemClock.sleep(fiveWaitSeconds);

        onView(withId(R.id.top10ScoreTitle)).check(matches(isDisplayed()));
        onView(withText(R.string.localTop10Score)).check(matches(isDisplayed()));
        onView(withId(R.id.top10ListView)).check(matches(isDisplayed()));
        onView(withId(R.id.top10OkButton)).check(matches(isDisplayed()));
        onView(withText(R.string.okString)).check(matches(isDisplayed()));

        SystemClock.sleep(threeWaitSeconds);

        onView(withId(R.id.top10OkButton)).perform(click());    // Espresso.pressBack();
    }

    private void saveGameSubmenu() {
        actionSubMenu(2);
        alertDialogFragment(R.string.sureToSaveGameStr);
        SystemClock.sleep(threeWaitSeconds);
    }

    @Test
    public void test_saveGameSubmenuOk() {
        saveGameSubmenu();
        onView(withId(R.id.dialogfragment_okButton)).perform(click());
    }

    @Test
    public void test_saveGameSubmenuNo() {
        saveGameSubmenu();
        onView(withId(R.id.dialogfragment_noButton)).perform(click());
    }

    private void loadGameSubmenu() {
        actionSubMenu(3);
        alertDialogFragment(R.string.sureToLoadGameStr);
        SystemClock.sleep(threeWaitSeconds);
    }

    @Test
    public void test_loadGameSubmenuOk() {
        loadGameSubmenu();
        onView(withId(R.id.dialogfragment_okButton)).perform(click());
    }

    @Test
    public void test_loadGameSubmenuNo() {
        loadGameSubmenu();
        onView(withId(R.id.dialogfragment_noButton)).perform(click());
    }

    private void alertDialog() {
        onView(withHint(R.string.nameStr)).check(matches(isDisplayed()));
        onView(withText(R.string.cancelStr)).check(matches(isDisplayed()));
        onView(withText(R.string.submitStr)).check(matches(isDisplayed()));
    }

    private void newGameSubmenu() {
        actionSubMenu(4);
        alertDialog();
        SystemClock.sleep(threeWaitSeconds);
    }

    @Test
    public void test_newGameSubmenuSubmit() {
        newGameSubmenu();
        onView(withText(R.string.submitStr)).perform(click());
    }

    @Test
    public void test_newGameSubmenuCancel() {
        newGameSubmenu();
        onView(withText(R.string.cancelStr)).perform(click());
    }

    private void quitGameSubmenu() {
        actionSubMenu(5);
        alertDialog();
        SystemClock.sleep(threeWaitSeconds);
        Espresso.closeSoftKeyboard();

        // onView(withText(R.string.cancelStr)).perform(click());

        // SystemClock.sleep(2000);

        // After that, press back button to go back
        // UiDevice mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // mDevice.pressBack();
    }

    @Test
    public void test_quitGameSubmenuSubmit() {
        quitGameSubmenu();
        onView(withText(R.string.submitStr)).perform(click());
    }

    @Test
    public void test_quitGameSubmenuCancel() {
        quitGameSubmenu();
        onView(withText(R.string.cancelStr)).perform(click());
    }

    @Test
    public void test_privatePolicySubmenu() {
        actionSubMenu(6);
        SystemClock.sleep(fiveWaitSeconds);
        onView(withId(R.id.privacyPolicyWebView)).check(matches(isDisplayed()));
        Espresso.pressBack();
    }

    @After
    public void test_PostRun() {
        Log.d(TAG, "Cleaning up after each test case.");
    }

    @AfterClass
    public static void test_CleanUp() {
        Log.d(TAG, "Cleaning up after all test cases. One time running.");
    }
}
