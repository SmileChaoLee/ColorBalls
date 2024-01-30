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
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.smile.colorballs.databinding.FragmentTop10Binding
import com.smile.colorballs.databinding.Top10ScoreListItemsBinding
import com.smile.colorballs.model.Player
import com.smile.smilelibraries.utilities.ScreenUtil

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the [Top10Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Top10Fragment : Fragment {
    private var mContext: Context? = null
    private var top10Players: ArrayList<String> = ArrayList()
    private var top10Scores: ArrayList<Int> = ArrayList()
    private val medalImageIds = ArrayList<Int>()
    private var top10ListView: ListView? = null
    private var mListAdapter: MyListAdapter? = null
    private var top10OkButtonListener: Top10OkButtonListener? = null
    private var top10TitleName: String = ""
    private var textFontSize = 0f
    private lateinit var binding: FragmentTop10Binding

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
            arguments?.apply {
                getString(Constants.Top10TitleNameKey)?.let { nameIt ->
                    top10TitleName = nameIt
                }
                getStringArrayList(Constants.Top10PlayersKey)?.let { listIt ->
                    top10Players = listIt
                }
                getIntegerArrayList(Constants.Top10ScoresKey)?.let { listIt ->
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
    ): View {
        Log.d(TAG, "onCreateView")
        binding = FragmentTop10Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        // binding = FragmentTop10ScoreBinding.bind(view)
        if (savedInstanceState == null) {   // new Fragment instance
            Log.d(TAG, "onViewCreated.new Fragment instance")
            binding.top10ScoreTitle.apply {
                text = top10TitleName
                ScreenUtil.resizeTextSize(this, textFontSize, ScreenUtil.FontSize_Pixel_Type)
            }
            binding.top10ScoreTitle.let {
                ScreenUtil.resizeTextSize(it, textFontSize,
                    ScreenUtil.FontSize_Pixel_Type
                )
            }
            binding.top10OkButton.apply {
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
            binding.top10ListView.apply {
                adapter = mListAdapter
                setOnItemClickListener { _, _, _, _ -> }
            }.also { top10ListView = it}
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach()
    }

    private inner class MyListAdapter (
        mContext: Context,
        layoutId: Int,
        val players: ArrayList<String>,
        val scores: ArrayList<Int>,
        val medals: ArrayList<Int>
    ) : ArrayAdapter<String>(mContext, layoutId, players) {
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vBinding = Top10ScoreListItemsBinding.inflate(layoutInflater, parent, false)
            // val view = layoutInflater.inflate(layoutId, parent, false)
            Log.d(TAG, "players.size() = ${players.size}")
            Log.d(TAG, "position = $position")
            val mTop10Players = Player(players[position],
                scores[position].toString(),
                medals[position])
            vBinding.apply {
                lifecycleOwner = this@Top10Fragment
                top10Players = mTop10Players
            }
            if (count == 0) {
                return vBinding.root
            }
            val itemNum = if (resources.configuration.orientation
                == Configuration.ORIENTATION_LANDSCAPE) 2 else 4
            // items for one screen
            Log.d(TAG, "itemNum = $itemNum")
            vBinding.root.layoutParams.height = parent.height / itemNum
            Log.d(TAG, "layoutParams.height = ${vBinding.root.layoutParams.height}")

            vBinding.playerTextView.let {
                ScreenUtil.resizeTextSize(it, textFontSize,
                    ScreenUtil.FontSize_Pixel_Type)
                // it.text = players[position]
            }
            vBinding.scoreTextView.let {
                ScreenUtil.resizeTextSize(it, textFontSize,
                    ScreenUtil.FontSize_Pixel_Type)
                // it.text = scores[position].toString()
            }
            // vBinding.medalImage.setImageResource(medals[position])

            return vBinding.root
        }
    }

    companion object {
        // private properties
        private const val TAG = "Top10Fragment"
        fun newInstance(
            top10Title: String,
            playerNames: ArrayList<String>,
            playerScores: ArrayList<Int>,
            listener: Top10OkButtonListener
        ): Top10Fragment {
            val args = Bundle().apply {
                putString(Constants.Top10TitleNameKey, top10Title)
                putStringArrayList(Constants.Top10PlayersKey, playerNames)
                putIntegerArrayList(Constants.Top10ScoresKey, playerScores)
            }
            Log.d(TAG, "newInstance.")
            return Top10Fragment(listener).apply {
                arguments = args
            }
        }
    }
}