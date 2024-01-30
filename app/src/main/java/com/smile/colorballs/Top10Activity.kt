package com.smile.colorballs

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.smile.colorballs.Top10Fragment.Top10OkButtonListener
import com.smile.colorballs.databinding.ActivityTop10Binding

class Top10Activity : AppCompatActivity() {
    private lateinit var binding : ActivityTop10Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityTop10Binding.inflate(layoutInflater)
        setContentView(binding.root)

        var top10TitleName = ""
        var top10Players: ArrayList<String> = ArrayList()
        var top10Scores: ArrayList<Int> = ArrayList()
        intent.extras?.let {
            it.getString(Constants.Top10TitleNameKey)?.let { nameIt ->
                top10TitleName = nameIt
            }
            it.getStringArrayList(Constants.Top10PlayersKey)?.let { listIt ->
                top10Players = listIt
            }
            it.getIntegerArrayList(Constants.Top10ScoresKey)?.let { listIt ->
                top10Scores = listIt
            }
        }

        val top10Fragment: Fragment = Top10Fragment
            .newInstance(
                top10TitleName, top10Players, top10Scores,
                object : Top10OkButtonListener {
                    override fun buttonOkClick(activity: Activity?) {
                        Log.d(TAG, "Top10OkButtonListener")
                        setResult(RESULT_OK)
                        activity?.finish()
                    }
                }
            )

        val top10FragmentTag = "Top10Fragment"
        val top10LayoutId = R.id.top10_score_linear_layout
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