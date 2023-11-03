package com.smile.colorballs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.smile.smilelibraries.utilities.ScreenUtil

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [Top10ScoreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Top10ScoreFragment : Fragment {
    private var mContext: Context? = null
    private var fragmentView: View? = null
    private var top10Players: ArrayList<String> = ArrayList()
    private var top10Scores: ArrayList<Int> = ArrayList()
    private val medalImageIds = ArrayList<Int>()
    private var top10ListView: ListView? = null
    private var mListAdapter: MyListAdapter? = null
    private var top10OkButtonListener: Top10OkButtonListener? = null
    private var top10TitleName: String = ""
    private var textFontSize = 0f

    interface Top10OkButtonListener {
        fun buttonOkClick(activity: Activity?)
    }

    constructor()

    @SuppressLint("ValidFragment")
    private constructor(listener: Top10OkButtonListener) : super() {
        Log.d(TAG, "constructor")
        top10OkButtonListener = listener
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach")
        super.onAttach(context)
        mContext = context
        textFontSize = ScreenUtil.suitableFontSize(
            activity,
            ScreenUtil.getDefaultTextSizeFromTheme(activity,
                ScreenUtil.FontSize_Pixel_Type, null),
            ScreenUtil.FontSize_Pixel_Type,
            0.0f
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            // if new Fragment instance
            Log.d(TAG, "onCreate.new Fragment instance")
            Log.d(TAG, "onCreate.arguments = $arguments")
            arguments?.let { argIt ->
                argIt.getString(Constants.Top10TitleNameKey)?.let { nameIt ->
                    top10TitleName = nameIt
                }
                argIt.getStringArrayList(Constants.Top10PlayersKey)?.let { listIt ->
                    top10Players = listIt
                }
                argIt.getIntegerArrayList(Constants.Top10ScoresKey)?.let { listIt ->
                    top10Scores = listIt
                }
            }

            medalImageIds.clear()
            medalImageIds.add(R.drawable.gold_medal)
            medalImageIds.add(R.drawable.silver_medal)
            medalImageIds.add(R.drawable.bronze_medal)
            medalImageIds.add(R.drawable.copper_medal)
            medalImageIds.add(R.drawable.olympics_image)
            medalImageIds.add(R.drawable.olympics_image)
            medalImageIds.add(R.drawable.olympics_image)
            medalImageIds.add(R.drawable.olympics_image)
            medalImageIds.add(R.drawable.olympics_image)
            medalImageIds.add(R.drawable.olympics_image)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        // historyView = inflater.inflate(R.layout.fragment_score_history, container, false);
        return inflater.inflate(R.layout.layout_for_top10_score_fragment,
            container,
            false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {   // new Fragment instance
            Log.d(TAG, "onViewCreated.new Fragment instance")
            view.findViewById<TextView>(R.id.top10ScoreTitle).apply {
                text = top10TitleName
                ScreenUtil.resizeTextSize(this, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            }
            view.findViewById<TextView>(R.id.top10ScoreTitle).let {
                ScreenUtil.resizeTextSize(it, textFontSize,
                    ScreenUtil.FontSize_Pixel_Type
                )
            }
            view.findViewById<Button>(R.id.top10OkButton).apply {
                ScreenUtil.resizeTextSize(this, textFontSize, ScreenUtil.FontSize_Pixel_Type)
                setOnClickListener {
                    top10OkButtonListener?.apply {
                        buttonOkClick(activity)
                    }
                }
            }
            activity?.let { activityIt ->
                mListAdapter = MyListAdapter(activityIt.applicationContext,
                    R.layout.top10_score_list_items,
                    top10Players, top10Scores, medalImageIds)
            }
            view.findViewById<ListView>(R.id.top10ListView).apply {
                adapter = mListAdapter
                setOnItemClickListener { _, _, _, _ -> }
            }.also { top10ListView = it}
        }
        fragmentView = view
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach()
    }

    private inner class MyListAdapter (
        mContext: Context,
        val layoutId: Int,
        val players: ArrayList<String>,
        val scores: ArrayList<Int>,
        val medals: ArrayList<Int>
    ) : ArrayAdapter<String>(mContext, layoutId, players) {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = layoutInflater.inflate(layoutId, parent, false)
            if (count == 0) {
                return view
            }
            val listViewHeight = parent.height
            var itemNum = 4
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                itemNum = 2
            }
            val itemHeight = listViewHeight / itemNum // items for one screen
            val layoutParams = view.layoutParams
            layoutParams.height = itemHeight

            // view.setLayoutParams(layoutParams);  // no needed
            view.findViewById<TextView>(R.id.playerTextView).let {
                ScreenUtil.resizeTextSize(it, textFontSize,
                    ScreenUtil.FontSize_Pixel_Type)
                it.text = players[position]
            }
            view.findViewById<TextView>(R.id.scoreTextView).let {
                ScreenUtil.resizeTextSize(it, textFontSize,
                    ScreenUtil.FontSize_Pixel_Type)
                it.text = scores[position].toString()
            }
            view.findViewById<ImageView>(R.id.medalImage).setImageResource(medals[position])

            return view
        }
    }

    companion object {
        // private properties
        private const val TAG = "Top10ScoreFragment"
        fun newInstance(
            top10Title: String,
            playerNames: ArrayList<String>,
            playerScores: ArrayList<Int>,
            listener: Top10OkButtonListener
        ): Top10ScoreFragment {
            val args = Bundle().apply {
                putString(Constants.Top10TitleNameKey, top10Title)
                putStringArrayList(Constants.Top10PlayersKey, playerNames)
                putIntegerArrayList(Constants.Top10ScoresKey, playerScores)
            }
            Log.d(TAG, "newInstance.")
            return Top10ScoreFragment(listener).apply {
                arguments = args
            }
        }
    }
}