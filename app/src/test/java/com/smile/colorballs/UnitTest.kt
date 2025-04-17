package com.smile.colorballs

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.smile.colorballs.interfaces.PresentView
import com.smile.colorballs.models.GameProp
import com.smile.colorballs.models.GridData
import com.smile.colorballs.presenters.MyPresenter
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitTest {

    @Mock
    private lateinit var mockGridData : GridData
    @Mock
    private lateinit var presentView: PresentView

    private lateinit var appContext : Context
    private lateinit var resources : Resources
    private lateinit var presenter: MyPresenter

    @Before
    fun setUp() {
        println("setUp")
        MockitoAnnotations.openMocks(this)
        Assert.assertNotNull("setUp.mockGridData is null", mockGridData)
        // val presentView = mock(PresentView::class.java)
        Assert.assertNotNull("setUp.presentView is null", presentView)

        appContext = ApplicationProvider.getApplicationContext()
        Assert.assertNotNull("setUp.appContext is null", appContext)
        resources = appContext.resources
        Assert.assertNotNull("setUp.resources is null", resources)
        presenter = MyPresenter(presentView)
        Assert.assertNotNull("setUp.presenter is null", presenter)
        val isNewGame = presenter.initializeColorBallsGame(1000, 1000, null)
        Assert.assertTrue(isNewGame)
    }

    @After
    fun tearDown() {
        println("tearDown")
    }

    @Test
    fun test_PresenterHasSound() {
        println("test_PresenterHasSound")
        val gameProp = mock(GameProp::class.java)
        Assert.assertNotNull("test_PresenterHasSound.gameProp is null",
            gameProp)

        `when`(gameProp.hasSound).thenReturn(true)
        Assert.assertTrue(presenter.hasSound())
    }
}