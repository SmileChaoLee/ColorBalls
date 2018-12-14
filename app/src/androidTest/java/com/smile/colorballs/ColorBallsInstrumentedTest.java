package com.smile.colorballs;
import android.content.Context;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ColorBallsInstrumentedTest {

    private String packageNameForColorBalls = "com.smile.colorballs";
    private String googleAdMobBannerIDForColorBalls = "ca-app-pub-8354869049759576/3904969730";
    private String packageNameForFiveColorBalls = "com.smile.fivecolorballs";
    private String googleAdMobBannerIDForFiveColorBalls = "ca-app-pub-8354869049759576/7162646323";

    public static void test_Setup() {
        System.out.println("Initializing before all test cases. One time running.");
    }

    @Rule
    public ActivityTestRule<MyActivity> myActivityTestRule = new ActivityTestRule<>(MyActivity.class);

    @Before
    public void test_PreRun() {
        System.out.println("Setting up before each test case.");
    }

    @Test
    public void testPackageNameForColorBalls() {
        Context appContext = ApplicationProvider.getApplicationContext();
        assertEquals(packageNameForColorBalls, appContext.getPackageName());
    }

    @Test
    public void testPackageNameForFiveColorBalls() {
        Context appContext = ApplicationProvider.getApplicationContext();
        assertEquals(packageNameForFiveColorBalls, appContext.getPackageName());
    }

    @Test
    public void TestGoogleAdMobBannerID() {
        Context appContext = ApplicationProvider.getApplicationContext();
        String packageName = appContext.getPackageName();
        if (packageName.equals(packageNameForColorBalls)) {
            assertEquals(googleAdMobBannerIDForColorBalls, ColorBallsApp.googleAdMobBannerID);
        } else if (packageName.equals(packageNameForFiveColorBalls)) {
            assertEquals(googleAdMobBannerIDForFiveColorBalls, ColorBallsApp.googleAdMobBannerID);
        } else {
            fail("No Google AdMob Banner Id was not defined.");
        }
    }

    @Test
    public void test_FileSubmenu() {
        // This test should succeed when the device's orientation is portrait
        // and it will fail when the device's orientation is landscape
        onView(withId(R.id.file)).perform(click());
        onView(withText(R.string.settingStr)).check(matches(isDisplayed()));
        onView(withText(R.string.newGame)).check(matches(isDisplayed()));
        onView(withText(R.string.quitGame)).check(matches(isDisplayed()));
    }

    @After
    public void test_PostRun() {
        System.out.println("Cleaning up after each test case.");
    }

    @AfterClass
    public static void test_CleanUp() {
        System.out.println("Cleaning up after all test cases. One time running.");
    }
}
