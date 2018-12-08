package com.smile.colorballs;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ColorBallsInstrumentedText {

    private String packageNameForColorBalls = "com.smile.colorballs";
    private String googleAdMobBannerIDForColorBalls = "ca-app-pub-8354869049759576/3904969730";
    private String packageNameForFiveColorBalls = "com.smile.fivecolorballs";
    private String googleAdMobBannerIDForFiveColorBalls = "ca-app-pub-8354869049759576/7162646323";

    public static void test_Setup() {
        System.out.println("Initializing before all test cases. One time running.");
    }
    @Before
    public void test_PreRun() {
        System.out.println("Setting up before each test case.");
    }

    @Test
    public void testPackageNameForColorBalls() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals(packageNameForColorBalls, appContext.getPackageName());
    }

    @Test
    public void testPackageNameForFiveColorBalls() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals(packageNameForFiveColorBalls, appContext.getPackageName());
    }

    @Test
    public void TestGoogleAdMobBannerID() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        String packageName = appContext.getPackageName();
        if (packageName.equals(packageNameForColorBalls)) {
            assertEquals(googleAdMobBannerIDForColorBalls, ColorBallsApp.googleAdMobBannerID);
        } else if (packageName.equals(packageNameForFiveColorBalls)) {
            assertEquals(googleAdMobBannerIDForFiveColorBalls, ColorBallsApp.googleAdMobBannerID);
        } else {
            fail("No Google AdMob Banner Id was not defined.");
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
}
