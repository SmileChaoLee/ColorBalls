package com.smile.colorballs;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.v7.widget.MenuPopupWindow;
import android.view.View;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ColorBallsInstrumentedTest {

    private static Context appContext;
    private static Resources appResources;

    private String packageNameForColorBalls = "com.smile.colorballs";
    private String googleAdMobBannerIDForColorBalls = "ca-app-pub-8354869049759576/3904969730";
    private String packageNameForFiveColorBalls = "com.smile.fivecolorballs";
    private String googleAdMobBannerIDForFiveColorBalls = "ca-app-pub-8354869049759576/7162646323";

    @BeforeClass
    public static void test_Setup() {
        appContext = ApplicationProvider.getApplicationContext();
        appResources = appContext.getResources();
        System.out.println("Initializing before all test cases. One time running.");
    }

    // do use the following because AndroidJUnit4.class is deprecated
    // start using androidx.test.ext.junit.runners.AndroidJUnit4;
    @Rule
    public ActivityTestRule<MyActivity> myActivityTestRule = new ActivityTestRule<>(MyActivity.class);

    @Before
    public void test_PreRun() {
        // appContext = myActivityTestRule.getActivity();
        // appResources = appContext.getResources();
        System.out.println("Setting up before each test case.");
    }

    @Test
    public void testPackageNameForColorBalls() {
        assertEquals(packageNameForColorBalls, appContext.getPackageName());
    }

    @Test
    public void testPackageNameForFiveColorBalls() {
        assertEquals(packageNameForFiveColorBalls, appContext.getPackageName());
    }

    @Test
    public void TestGoogleAdMobBannerID() {
        String packageName = appContext.getPackageName();
        if (packageName.equals(packageNameForColorBalls)) {
            assertEquals(googleAdMobBannerIDForColorBalls, ColorBallsApp.googleAdMobBannerID);
        } else if (packageName.equals(packageNameForFiveColorBalls)) {
            assertEquals(googleAdMobBannerIDForFiveColorBalls, ColorBallsApp.googleAdMobBannerID);
        } else {
            fail("Wrong package name and wrong AdMob Banner ID");
        }
    }

    @Test
    public void test_UndoSubmenu() {
        onView(withId(R.id.undoGame)).perform(click());
    }

    @Test
    public void test_ActionSubmenu() {
        onView(withId(R.id.gameAction)).perform(click());
        onView(withText(R.string.top10Str)).check(matches(isDisplayed()));
        onView(withText(R.string.globalTop10Str)).check(matches(isDisplayed()));
        onView(withText(R.string.saveGameString)).check(matches(isDisplayed()));
        onView(withText(R.string.loadGameString)).check(matches(isDisplayed()));
    }

    @Test
    public void test_top10SubmenuUnderActionGame() {
        onView(withId(R.id.gameAction)).perform(click());
        // test Top 10 submenu
        onData(CoreMatchers.anything())
                .inRoot(RootMatchers.isPlatformPopup()) // isPlatformPopup() == is in PopupWindow
                .inAdapterView(CoreMatchers.<View>instanceOf(MenuPopupWindow.MenuDropDownListView.class))
                .atPosition(0) // for the first submenu item, here: R.id.top10
                .perform(click());
        // R.id.top10ScoreTitle is in layout_for_top10_score_fragment.xml which is used by Top10ScoreFragment
        // onView(withId(R.id.top10ScoreTitle)).check(matches(isDisplayed()));  // succeeded
        // or
        // the string R.string.top10Score will be displayed in Top10ScoreFragment (called by Top10ScoreActivity)
        onView(withText(R.string.top10Score)).check(matches(isDisplayed()));
        onView(withText(R.string.okStr));
        onView(withId(R.id.top10OkButton)).perform(click());

    }

    @Test
    public void test_globalTop10SubmenuUnderActionGame() {
        onView(withId(R.id.gameAction)).perform(click());
        // test Top 10 submenu
        onData(CoreMatchers.anything())
                .inRoot(RootMatchers.isPlatformPopup()) // isPlatformPopup() == is in PopupWindow
                .inAdapterView(CoreMatchers.<View>instanceOf(MenuPopupWindow.MenuDropDownListView.class))
                .atPosition(1) // for the second submenu item, here: R.id.globalTop10
                .perform(click());
        // R.id.top10ScoreTitle is in layout_for_top10_score_fragment.xml which is used by Top10ScoreFragment
        // onView(withId(R.id.top10ScoreTitle)).check(matches(isDisplayed()));  // succeeded
        // or
        // the string R.string.globalTop10Score will be displayed in Top10ScoreFragment (called by Top10ScoreActivity)
        // onView(withText(R.string.globalTop10Score)).check(matches(isDisplayed()));
        onView(withText(R.string.globalTop10Score)).check(matches(isDisplayed()));
        onView(withText(R.string.okStr));
        onView(withId(R.id.top10OkButton)).perform(click());

    }

    @Test
    public void test_settingSubmenu() {
        if(appResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            test_settingMenuItem();
        }
    }

    @Test
    public void test_newGameSubmenu() {
        if(appResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            test_newGameMenuItem();
        }
    }

    @Test
    public void test_quitGameSubmenu() {
        if(appResources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            test_quitGameMenuItem();
        }
    }

    @Test
    public void test_FileSubmenu() {
        if(appResources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            test_FileSubmenuForPortrait();
        }
    }

    @After
    public void test_PostRun() {
        System.out.println("Cleaning up after each test case.");
    }

    @AfterClass
    public static void test_CleanUp() {
        System.out.println("Cleaning up after all test cases. One time running.");
    }

    private void test_FileSubmenuForPortrait() {
        onView(withId(R.id.file)).perform(click());
        onView(withText(R.string.settingStr)).check(matches(isDisplayed()));
        onView(withText(R.string.newGame)).check(matches(isDisplayed()));
        onView(withText(R.string.quitGame)).check(matches(isDisplayed()));

        test_settingMenuItem();
        test_newGameMenuItem();
        test_quitGameMenuItem();
    }

    private void test_settingMenuItem() {
        onView(withId(R.id.setting)).perform(click());
        onView(withText(R.string.settingStr)).check(matches(isDisplayed()));
        onView(withText(R.string.soundStr)).check(matches(isDisplayed()));
        onView(withText(R.string.playerLevelStr)).check(matches(isDisplayed()));
        onView(withText(R.string.cancelStr)).check(matches(isDisplayed()));
        onView(withText(R.string.okString)).check(matches(isDisplayed()));
    }

    private void test_newGameMenuItem() {
        onView(withId(R.id.newGame)).perform(click());
        onView(withText(R.string.cancelStr)).check(matches(isDisplayed()));
        onView(withText(R.string.submitStr)).check(matches(isDisplayed()));
    }

    private void test_quitGameMenuItem() {
        onView(withId(R.id.quitGame)).perform(click());
        onView(withText(R.string.cancelStr)).check(matches(isDisplayed()));
        onView(withText(R.string.submitStr)).check(matches(isDisplayed()));
    }

}
