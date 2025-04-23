package com.smile.colorballs

import android.content.Context
import android.content.res.Resources
import androidx.test.core.app.ApplicationProvider
import com.smile.colorballs.interfaces.PresentView
import com.smile.colorballs.models.GameProp
import com.smile.colorballs.models.GridData
import com.smile.colorballs.presenters.Presenter
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UnitTest {

    private lateinit var gameProp: GameProp
    private lateinit var gridData: GridData
    @Mock
    private lateinit var presentView: PresentView
    @InjectMocks
    private lateinit var presenter: Presenter

    private lateinit var appContext : Context
    private lateinit var resources : Resources
    private var isNewGame: Boolean = false

    @Before
    fun setUp() {
        println("setUp")
        MockitoAnnotations.openMocks(this)

        // val presentView = mock(PresentView::class.java)
        Assert.assertNotNull("setUp.presentView is null", presentView)
        Assert.assertNotNull("setUp.presenter is null", presenter)

        appContext = ApplicationProvider.getApplicationContext()
        Assert.assertNotNull("setUp.appContext is null", appContext)
        resources = appContext.resources
        Assert.assertNotNull("setUp.resources is null", resources)
        // presenter = Presenter(presentView)
        // Assert.assertNotNull("setUp.presenter is null", presenter)

        `when`(presentView.contextResources()).thenReturn(resources)
        // doNothing().`when`(presentView).showGameOverDialog()
        isNewGame = presenter.initGame(1000, 1000, null)
        Assert.assertTrue("setup.not a name game", isNewGame)

        gameProp = presenter.mGameProp
        Assert.assertNotNull("setup.gameProp is null", gameProp)
        gridData = gameProp.gridData
        Assert.assertNotNull("setup.gridData is null", gridData)
    }

    @After
    fun tearDown() {
        println("tearDown")
    }

    @Test
    fun test_PresenterHasSound() {
        println("test_PresenterHasSound.gameProp = $gameProp")
        // `when`(gameProp.hasSound).thenReturn(true)
        presenter.setHasSound(true)
        // gameProp.hasSound = true
        Assert.assertTrue(presenter.hasSound())

        // `when`(gameProp.hasSound).thenReturn(false)
        // presenter.setHasSound(false)
        gameProp.hasSound = false
        Assert.assertFalse(presenter.hasSound())
    }
}