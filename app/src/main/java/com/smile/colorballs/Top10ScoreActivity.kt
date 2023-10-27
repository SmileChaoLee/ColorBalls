package com.smile.colorballs

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.smile.colorballs.databinding.ActivityTop10ScoreBinding

class Top10ScoreActivity : AppCompatActivity() {
    private lateinit var binding : ActivityTop10ScoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityTop10ScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var top10TitleName: String? = ""
        var top10Players: ArrayList<String>? = ArrayList()
        var top10Scores: ArrayList<Int>? = ArrayList()
        intent.extras?.let {
            top10TitleName = it.getString(Constants.Top10TitleNameKey)
            top10Players = it.getStringArrayList(Constants.Top10PlayersKey)
            top10Scores = it.getIntegerArrayList(Constants.Top10ScoresKey)
        }

        val top10ScoreFragment: Fragment = Top10ScoreFragment
            .newInstance(
                top10TitleName, top10Players, top10Scores
            ) { activity: Activity ->
                setResult(RESULT_OK)
                activity.finish()
            }

        val top10ScoreFragmentTag = "Top10ScoreFragment"
        val top10LayoutId = R.id.top10_score_linear_layout
        supportFragmentManager.let {
            it.beginTransaction().apply {
                if (it.findFragmentByTag(top10ScoreFragmentTag) == null) {
                    add(top10LayoutId, top10ScoreFragment, top10ScoreFragmentTag)
                } else {
                    replace(top10LayoutId, top10ScoreFragment, top10ScoreFragmentTag)
                }
                commit()
            }
        }
        Log.d(TAG, "onCreate.top10ScoreFragment is created.")
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
        private const val TAG = "Top10ScoreActivity"
    }
}