package com.smile.colorballs.views

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toDrawable
import com.facebook.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.smile.colorballs.BuildConfig
import com.smile.colorballs.ColorBallsApp
import com.smile.colorballs.R
import com.smile.colorballs.models.Settings
import com.smile.colorballs.models.TopPlayer
import com.smile.colorballs.views.ui.theme.ColorPrimary
import com.smile.smilelibraries.AdMobBanner
import com.smile.smilelibraries.FacebookBanner

object CbComposable {

    private const val TAG = "CbComposable"

    interface ButtonClickListener {
        fun buttonOkClick()
        fun buttonCancelClick() = run { }
    }

    interface ButtonClickListenerString {
        fun buttonOkClick(value: String?)
        fun buttonCancelClick(value: String?) = run { }
    }

    interface SettingClickListener {
        fun hasSoundClick(hasSound: Boolean)
        fun easyLevelClick(easyLevel: Boolean)
        fun hasNextClick(hasNext: Boolean)
    }

    var mFontSize = 24.sp
        set(value) {
            if (field != value) {
                field = value
                menuItemFontSize = value
            }
        }

    var toastFontSize = (mFontSize.value * 0.7f).sp
        set(value) {
            if (field != value) {
                field = value
            }
        }

    private var menuItemFontSize = mFontSize

    @Composable
    fun textUnitToDp(sp: TextUnit): Dp {
        val dp = with(LocalDensity.current) {
            sp.toDp()
        }
        return dp
    }

    @Composable
    fun HorDivider(
        modifier: Modifier = Modifier,
        color: Color = Color.Black, thickness: Dp = 2.dp
    ) {
        HorizontalDivider(
            modifier = modifier.fillMaxWidth(),
            color = color, thickness = thickness
        )
    }

    @Composable
    fun MenuItemText(
        modifier: Modifier = Modifier,
        text: String, color: Color
    ) {
        Text(
            modifier = modifier,
            lineHeight = (menuItemFontSize.value + 2f).sp,
            text = text, color = color,
            fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal,
            fontSize = menuItemFontSize
        )
    }

    @Composable
    fun DropdownMenuItem(
        text: String, color: Color,
        onClick: () -> Unit,
        isDivider: Boolean = true
    ) {
        val itemHeight = textUnitToDp((menuItemFontSize.value + 12).sp)
        androidx.compose.material3.DropdownMenuItem(
            modifier = Modifier
                .height(height = itemHeight)
                .padding(all = 0.dp),
            text = { MenuItemText(text = text, color = color) },
            onClick = { onClick() })
        if (isDivider) HorDivider()
    }

    @Composable
    fun Top10Composable(
        title: String, topPlayers: List<TopPlayer>,
        buttonListener: ButtonClickListener,
        oKStr: String
    ) {
        Log.d(TAG, "Top10Composable.topPlayers.size = ${topPlayers.size}")
        val imageWidth = (mFontSize.value * 3.0).dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xff90e5c4))
        ) {
            Text(text = title, fontSize = mFontSize, color = Color.Blue)
            HorizontalDivider(
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(10.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(topPlayers) { topPlayer ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(3f)) {
                            Text(
                                text = topPlayer.player.playerName!!,
                                color = Color.Red, fontSize = mFontSize
                            )
                            Text(
                                text = topPlayer.player.score!!.toString(),
                                color = Color.Red, fontSize = mFontSize
                            )
                        }
                        Image(
                            modifier = Modifier
                                .weight(2f)
                                .size(imageWidth),
                            painter = painterResource(id = topPlayer.medal),
                            contentDescription = "", // Accessibility text
                            contentScale = ContentScale.Fit
                        )
                    }
                    HorizontalDivider(
                        color = Color.Blue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(5.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Blue)/*.weight(10f, true)*/,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { buttonListener.buttonOkClick() },
                    /* modifier = Modifier
                    .size(buttonWidth, height = buttonHeight)
                    .weight(weight = 1f, fill = true)
                    .align(Alignment.CenterHorizontally)
                    .background(color = Color.Green), */
                    colors = ButtonColors(
                        containerColor = Color.Cyan,
                        disabledContainerColor = Color.DarkGray,
                        contentColor = Color.Red,
                        disabledContentColor = Color.LightGray
                    )
                )
                { Text(text = oKStr, fontSize = mFontSize) }
            }
        }
    }

    @Composable
    fun SettingCompose(
        buttonListener: ButtonClickListener,
        textListener: SettingClickListener,
        backgroundColor: Color,
        setting: Settings, title: String,
        soundStr: String, playerLevelStr: String, hasNextStr: String,
        onStr: String, offStr: String,
        yesStr: String, noStr: String,
        no1Str: String, no2Str: String, easyStr: String, diffStr: String,
        okStr: String, cancelStr: String
    ) {
        val textColor = Color(0xffffa500)
        val spaceWeight = 1.0f
        val dividerWeight = 1.0f
        var setRowWeight = 3.0f    // for setting row
        var setColumnWeight = 10.0f     // for setting column
        var rowWeight = 5.0f
        var textWeight = 3.0f
        var buttonWeight = spaceWeight * 3.0f
        if (LocalConfiguration.current.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        ) {
            setRowWeight = 8.0f
            setColumnWeight = 6.0f
            rowWeight = 8.0f
            textWeight = 6.0f
            buttonWeight = spaceWeight * 2.0f
        }

        // val onStr = activity.getString(R.string.onStr)
        // val offStr = activity.getString(R.string.offStr)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent)
        ) { // main column
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(spaceWeight)
            )
            // val yesStr = activity.getString(R.string.yesStr)
            // val noStr = activity.getString(R.string.noStr)
            Row(modifier = Modifier.weight(setRowWeight)) {    // setting row
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(spaceWeight)
                )
                Column(
                    modifier = Modifier
                        .background(color = backgroundColor)
                        .weight(setColumnWeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { // setting column
                    Column(
                        modifier = Modifier.weight(textWeight),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = title, color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = mFontSize
                        )
                    }
                    HorDivider(
                        color = Color.White,
                        modifier = Modifier.weight(dividerWeight)
                    )
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        var hasSound by remember { mutableStateOf(setting.hasSound) }
                        MenuItemText(
                            // text = activity.getString(R.string.soundStr),
                            text = soundStr,
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                                .clickable {
                                    hasSound = !hasSound
                                    textListener.hasSoundClick(hasSound)
                                },
                            text = if (hasSound) onStr else offStr,
                            Color.White
                        )
                        MenuItemText(
                            text = if (hasSound) yesStr else noStr,
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                    }
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // val no1Str = activity.getString(R.string.no1)
                        // val no2Str = activity.getString(R.string.no2)
                        // val easyStr = activity.getString(R.string.easyStr)
                        // val diffStr = activity.getString(R.string.difficultStr)
                        var easyLevel by remember { mutableStateOf(setting.easyLevel) }
                        MenuItemText(
                            // text = activity.getString(R.string.playerLevelStr),
                            text = playerLevelStr,
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                                .clickable {
                                    easyLevel = !easyLevel
                                    textListener.easyLevelClick(easyLevel)
                                },
                            text = if (easyLevel) no1Str else no2Str,
                            Color.White
                        )
                        MenuItemText(
                            text = if (easyLevel) easyStr else diffStr,
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                    }
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        var hasNext by remember { mutableStateOf(setting.hasNext) }
                        MenuItemText(
                            // text = activity.getString(R.string.fillColumnStr),
                            text = hasNextStr,
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp).clickable {
                                    hasNext = !hasNext
                                    textListener.hasNextClick(hasNext)
                                },
                            text = if (hasNext) onStr else offStr,
                            Color.White
                        )
                        MenuItemText(
                            text = if (hasNext) yesStr else noStr,
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                    }
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { buttonListener.buttonCancelClick() },
                            modifier = Modifier.weight(buttonWeight),
                            colors = ButtonColors(
                                containerColor = Color.Yellow,
                                disabledContentColor = Color.Yellow,
                                contentColor = Color.Red,
                                disabledContainerColor = Color.Red,
                            )
                        ) {
                            Text(
                                // text = activity.getString(R.string.cancelStr),
                                text = cancelStr,
                                fontSize = mFontSize
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(spaceWeight)
                        )
                        Button(
                            onClick = { buttonListener.buttonOkClick() },
                            modifier = Modifier.weight(buttonWeight),
                            colors = ButtonColors(
                                containerColor = Color.Yellow,
                                disabledContentColor = Color.Yellow,
                                contentColor = Color.Blue,
                                disabledContainerColor = Color.Blue
                            )
                        ) {
                            Text(
                                // text = activity.getString(R.string.okStr),
                                text = okStr,
                                fontSize = mFontSize
                            )
                        }
                    }
                }   // end of setting column
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(spaceWeight)
                )
            }   // end of setting row
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(spaceWeight)
            )
        }   // end of main column
    }

    @Composable
    fun DialogWithText(activity: Activity,
                       buttonListener: ButtonClickListener,
                       dialogTitle: String, dialogText: String) {
        Log.d(TAG, "DialogWithText")
        val okStr = activity.getString(R.string.okStr)
        val noStr = activity.getString(R.string.noStr)
        val lightRed = Color(0xffff4444)
        AlertDialog(
            icon = null, title = {
                if (dialogTitle.isNotEmpty())
                    Text(
                        text = dialogTitle,
                        fontWeight = FontWeight.Medium, fontSize = mFontSize
                    )
            },
            text = {
                if (dialogText.isNotEmpty())
                    Text(
                        text = dialogText,
                        fontWeight = FontWeight.Medium, fontSize = mFontSize
                    )
            },
            containerColor = Color(0xffffa500),
            titleContentColor = Color.White,
            textContentColor = Color.Blue,
            onDismissRequest = { /* isOpen = false */ },
            confirmButton = {
                Button(
                    onClick = {
                        // isOpen = false
                        buttonListener.buttonOkClick()
                    }, colors = ButtonColors(
                        containerColor = ColorPrimary,
                        disabledContainerColor = ColorPrimary,
                        contentColor = Color.Yellow,
                        disabledContentColor = Color.Yellow
                    )
                ) { Text(text = okStr, fontSize = mFontSize) }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // isOpen = false
                        buttonListener.buttonCancelClick()
                    }, colors = ButtonColors(
                        containerColor = ColorPrimary,
                        disabledContainerColor = ColorPrimary,
                        contentColor = lightRed,
                        disabledContentColor = lightRed
                    )
                ) { Text(text = noStr, fontSize = mFontSize) }
            }
        )
    }

    @Composable
    fun textFieldValue(hintText: String): String {
        var textValue by rememberSaveable { mutableStateOf("") }
        TextField(
            value = textValue,
            onValueChange = {
                textValue = it
                Log.d(TAG, "textFieldValue.textValue = $textValue")
            },
            textStyle = LocalTextStyle.current.copy(fontSize = mFontSize),
            placeholder = {
                Text(
                    text = hintText, color = Color.LightGray,
                    fontWeight = FontWeight.Light, fontSize = mFontSize
                )
            }
        )
        return textValue
    }

    @Composable
    fun DialogWithTextField(activity: Activity,
                            buttonListener: ButtonClickListenerString,
                            dialogTitle: String, hintText: String) {
        Log.d(TAG, "DialogWithTextField")

        val lightRed = Color(0xffff4444)
        val okStr = activity.getString(R.string.okStr)
        val noStr = activity.getString(R.string.noStr)
        var returnValue = ""
        Log.d(TAG, "DialogWithTextField.AlertDialog")
        AlertDialog(
            onDismissRequest = { /* isOpen = false */ },
            icon = null, title = {
                if (dialogTitle.isNotEmpty())
                    Text(
                        text = dialogTitle,
                        fontWeight = FontWeight.Medium, fontSize = mFontSize
                    )
            },
            text = {
                if (hintText.isNotEmpty())
                    returnValue = textFieldValue(hintText)
            },
            containerColor = Color(0xffffa500),
            titleContentColor = Color.White,
            textContentColor = Color.Blue,
            confirmButton = {
                Button(
                    onClick = {
                        // isOpen = false
                        buttonListener.buttonOkClick(returnValue)
                    }, colors = ButtonColors(
                        containerColor = ColorPrimary,
                        disabledContainerColor = ColorPrimary,
                        contentColor = Color.Yellow,
                        disabledContentColor = Color.Yellow
                    )
                ) { Text(text = okStr, fontSize = mFontSize) }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // isOpen = false
                        buttonListener.buttonCancelClick(returnValue)
                    }, colors = ButtonColors(
                        containerColor = ColorPrimary,
                        disabledContainerColor = ColorPrimary,
                        contentColor = lightRed,
                        disabledContentColor = lightRed
                    )
                ) { Text(text = noStr, fontSize = mFontSize) }
            }
        )
    }

    @Composable
    fun ShowAdmobBanner(modifier: Modifier = Modifier,
                        adId: String, width: Int = 0) {
        Log.d(TAG, "ShowAdmobBanner.adId = $adId")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                AdView(context)
            },
            update = { adView ->
                AdMobBanner(adView, adId, width)
            }
        )
    }

    @Composable
    fun ShowAdmobNormalBanner(modifier: Modifier = Modifier) {
        val adId = ColorBallsApp.googleAdMobBannerID
        Log.d(TAG, "ShowAdmobNormalBanner.adId = $adId")
        ShowAdmobBanner(modifier = modifier, adId = adId)
    }

    @Composable
    fun ShowAdmobAdaptiveBanner(modifier: Modifier = Modifier, width: Int) {
        val adId = ColorBallsApp.googleAdMobBannerID2
        Log.d(TAG, "ShowAdmobAdaptiveBanner.adId = $adId")
        ShowAdmobBanner(modifier = modifier, adId = adId, width = width)
    }

    @Composable
    fun MyNativeAdView(
        modifier: Modifier = Modifier,
        ad: NativeAd,
        adContent: @Composable (ad: NativeAd, view: View) -> Unit,
    ) {
        Log.d(TAG, "MyNativeAdView")
        val contentViewId by rememberSaveable { mutableIntStateOf(View.generateViewId()) }
        val adViewId by rememberSaveable { mutableIntStateOf(View.generateViewId()) }
        Log.d(TAG, "MyNativeAdView.AndroidView")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                Log.d(TAG, "MyNativeAdView.AndroidView.factory")
                val contentView = ComposeView(context).apply {
                    id = contentViewId
                }
                NativeAdView(context).apply {
                    id = adViewId
                    addView(contentView)
                }
            },
            update = { nativeAdView ->
                Log.d(TAG, "MyNativeAdView.AndroidView.update")
                val adView = nativeAdView.findViewById<NativeAdView>(adViewId)
                val contentView = nativeAdView.findViewById<ComposeView>(contentViewId)
                Log.d(TAG, "MyNativeAdView.AndroidView.update.setNativeAd()")
                adView.setNativeAd(ad)
                adView.background = (-0x1).toDrawable()
                adView.callToActionView = contentView
                contentView.setContent { adContent(ad, contentView) }
            }
        )
    }

    @Composable
    fun ShowFacebookBanner(modifier: Modifier = Modifier, adId: String) {
        // val adId = ColorBallsApp.facebookBannerID
        val pId = if (BuildConfig.DEBUG) "IMG_16_9_APP_INSTALL#$adId" else adId
        Log.d(TAG, "ShowFacebookBanner.adId = $adId")
        AndroidView(
            modifier = modifier,
            factory = { context ->
                com.facebook.ads.AdView(context, pId,
                    AdSize.BANNER_HEIGHT_50)
            },
            update = { faceAdView ->
                FacebookBanner(faceAdView)
            }
        )
    }
}