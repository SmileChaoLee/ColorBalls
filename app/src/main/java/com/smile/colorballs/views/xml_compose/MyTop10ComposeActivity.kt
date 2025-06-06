package com.smile.colorballs.views.xml_compose

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import com.smile.colorballs.R
import com.smile.colorballs.shared_composables.Composables
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.databinding.ActivityTop10ComposeBinding
import com.smile.smilelibraries.player_record_rest.models.Player

class MyTop10ComposeActivity : AppCompatActivity() {
    private lateinit var binding : ActivityTop10ComposeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityTop10ComposeBinding.inflate(layoutInflater)
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

        val top10Fragment: Fragment = MyTop10ComposeFragment.newInstance(top10TitleName, players,
            object : Composables.ButtonClickListener {
                override fun buttonOkClick() {
                    Log.d(TAG, "ComposableFunc.OkButtonListener.buttonOkClick")
                    setResult(RESULT_OK)
                    finish()
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
        private const val TAG = "MyTop10ComposeActivity"
    }
}