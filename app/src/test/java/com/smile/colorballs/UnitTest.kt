package com.smile.colorballs

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.models.CBallGridData
import com.smile.colorballs.models.GameProp
import org.junit.After
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Spy
import org.robolectric.RobolectricTestRunner


@RunWith(RobolectricTestRunner::class)
open class UnitTest {
    private val rowCounts = Constants.ROW_COUNTS
    private val colCounts = Constants.COLUMN_COUNTS
    private var twoDimArray = Array(rowCounts) { IntArray(colCounts){0} }
    private val hashMap : HashMap<Point, Int> = HashMap()
    private val hashSet : HashSet<Point> = HashSet()
    private val arrayList : ArrayList<Point> = ArrayList()

    // @Spy
    // private val gridData = GridData(Constants.NUM_EASY, twoDimArray,
    //     twoDimArray, hashMap, hashMap, hashSet, arrayList)
    @Spy
    private val gridData = CBallGridData()
    /*
    @Spy
    private val gameProp = GameProp(
        isShowingLoadingMessage = false,
        isShowingScoreMessage = false,
        isShowNextBallsAfterBlinking = false,
        isProcessingJob = false,
        isShowingNewGameDialog = false,
        isShowingQuitGameDialog = false,
        isShowingSureSaveDialog = false,
        isShowingSureLoadDialog = false,
        isShowingGameOverDialog = false,
        threadCompleted = booleanArrayOf(true,true,true,true,true,true,true,true,true,true),
        bouncyBallIndexI = -1,
        bouncyBallIndexJ = -1,
        isBallBouncing = false,
        isBallMoving = false,
        undoEnable = false,
        currentScore = 0,
        undoScore = 0,
        lastGotScore = 0,
        isEasyLevel = true,
        hasSound = true,
        hasNextBall = true)
    */
    @Spy
    private val gameProp = GameProp()
    @Mock
    // private lateinit var presentView: PresentView
    @InjectMocks
    // private lateinit var presenter: Presenter

    private lateinit var appContext : Context
    private lateinit var resources : Resources

    /*
    @Before
    fun setUp() {
        println("setUp")
        MockitoAnnotations.openMocks(this)

        Assert.assertNotNull("setup.gridData is null", gridData)
        Assert.assertNotNull("setup.gameProp is null", gameProp)
        // val presentView = mock(PresentView::class.java)
        Assert.assertNotNull("setUp.presentView is null", presentView)
        // Assert.assertNotNull("setUp.presenter is null", presenter)

        appContext = ApplicationProvider.getApplicationContext()
        Assert.assertNotNull("setUp.appContext is null", appContext)
        resources = appContext.resources
        Assert.assertNotNull("setUp.resources is null", resources)

        `when`(presentView.contextResources()).thenReturn(resources)
        `when`(presentView.getImageViewById(anyInt())).thenReturn(ImageView(appContext))
        // doNothing().`when`(presentView).showGameOverDialog()
        // presenter.initGame(1000, 1000, isNewGame = true)
    }
    */

    @After
    fun tearDown() {
        println("tearDown")
    }

    /*
    @Test
    fun test_PresenterHasSound() {
        println("test_PresenterHasSound.gameProp = $gameProp")
        // `when`(gameProp.hasSound).thenReturn(true)
        presenter.setHasSound(true)
        // gameProp.hasSound = true
        Assert.assertTrue(presenter.hasSound())

        // `when`(gameProp.hasSound).thenReturn(false)
        presenter.setHasSound(false)
        // gameProp.hasSound = false
        Assert.assertFalse(presenter.hasSound())
    }
    */
}