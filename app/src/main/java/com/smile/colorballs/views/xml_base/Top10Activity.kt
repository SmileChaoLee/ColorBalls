package com.smile.colorballs.views.xml_base

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import com.smile.colorballs.R
import com.smile.colorballs.views.xml_base.Top10Fragment.Top10OkButtonListener
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.databinding.ActivityTop10Binding
import com.smile.smilelibraries.player_record_rest.models.Player

class Top10Activity : AppCompatActivity() {
    private lateinit var binding : ActivityTop10Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityTop10Binding.inflate(layoutInflater)
        setContentView(binding.root)

        var top10TitleName = ""
        var players: ArrayList<Player> = ArrayList()
        intent.extras?.let {
            it.getString(Constants.TOP10_TITLE_NAME)?.let { nameIt ->
                top10TitleName = nameIt
            }
            players = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                BundleCompat.getParcelableArrayList(
                    it, Constants.TOP10_PLAYERS, Player::class.java)!!
            } else it.getParcelableArrayList(Constants.TOP10_PLAYERS)!!
        }

        val top10Fragment: Fragment = Top10Fragment.newInstance(
            top10TitleName, players,
            object : Top10OkButtonListener {
                override fun buttonOkClick(activity: Activity?) {
                    Log.d(TAG, "Top10OkButtonListener.buttonOkClick")
                    activity?.let {
                        setResult(RESULT_OK)
                        it.finish()
                    }
                }
            }
        )

        val top10FragmentTag = "Top10Fragment"
        val top10LayoutId = R.id.top10_players_layout
        supportFragmentManager.let {
            it.beginTransaction().apply {
                if (it.findFragmentByTag(top10FragmentTag) == null) {
                    add(top10LayoutId, top10Fragment, top10FragmentTag)
                } else {
                    replace(top10LayoutId, top10Fragment, top10FragmentTag)
                }
                commit()
            }
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "onBackPressedDispatcher.handleOnBackPressed")
                setResult(RESULT_OK)
                finish()
            }
        })
        Log.d(TAG, "onCreate.top10Fragment is created.")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState()")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }

    companion object {
        private const val TAG = "Top10Activity"
    }
}