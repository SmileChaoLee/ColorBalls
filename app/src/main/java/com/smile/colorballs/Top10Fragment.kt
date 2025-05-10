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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.databinding.FragmentTop10Binding
import com.smile.colorballs.databinding.Top10ScoreListItemsBinding
import com.smile.colorballs.models.Player
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
    private var top10ListView: RecyclerView? = null
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
                getString(Constants.TOP10_TITLE_NAME)?.let { nameIt ->
                    top10TitleName = nameIt
                }
                getStringArrayList(Constants.TOP10_PLAYERS)?.let { listIt ->
                    top10Players = listIt
                }
                getIntegerArrayList(Constants.TOP10_SCORES)?.let { listIt ->
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
            val players = ArrayList<Player>()
            for (i: Int in 0 until top10Players.size) {
                players.add(
                    Player(top10Players[i], top10Scores[i].toString(), medalImageIds[i])
                )
            }

            Log.d(TAG, "onViewCreated.TopViewAdapter.players.size = ${players.size}")
            top10ListView = binding.top10ListView.apply {
                adapter = TopViewAdapter(players)
                layoutManager = LinearLayoutManager(activity)
            }
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach()
    }

    private inner class TopViewAdapter(
        private val players: ArrayList<Player>
    ): RecyclerView.Adapter<TopViewAdapter.ViewHolder>() {
        private lateinit var vBinding: Top10ScoreListItemsBinding
        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)
        init {
            Log.d(TAG, "TopViewAdapter.players.size = ${players.size}")
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            vBinding = Top10ScoreListItemsBinding.inflate(layoutInflater,
                parent, false)
            val view = vBinding.root
            val viewHolder = ViewHolder(view)
            // items for one screen
            val itemNum = if (resources.configuration.orientation
                == Configuration.ORIENTATION_LANDSCAPE) 2 else 4
            Log.d(TAG, "onCreateViewHolder.itemNum = $itemNum")
            view.layoutParams.height = parent.height / itemNum
            Log.d(TAG, "onCreateViewHolder.layoutParams.height = ${view.layoutParams.height}")
            vBinding.playerTextView.let {
                ScreenUtil.resizeTextSize(it, textFontSize,
                    ScreenUtil.FontSize_Pixel_Type)
            }
            vBinding.scoreTextView.let {
                ScreenUtil.resizeTextSize(it, textFontSize,
                    ScreenUtil.FontSize_Pixel_Type)
            }
            vBinding.medalImage.let {
                // set ImageView size
                it.layoutParams.height = (textFontSize * 4).toInt()
                it.layoutParams.width = (textFontSize * 4).toInt()
            }

            return viewHolder
        }

        override fun getItemCount(): Int {
            return players.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Log.d(TAG, "onBindViewHolder.position = $position")
            if (position<0) return
            holder.itemView.setOnClickListener {
                ScreenUtil.showToast(
                    activity, players[position].name,
                    textFontSize, ScreenUtil.FontSize_Pixel_Type,
                    Toast.LENGTH_LONG
                )
            }
            vBinding.apply {
                lifecycleOwner = this@Top10Fragment
                top10Players = players[position]
            }
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
                putString(Constants.TOP10_TITLE_NAME, top10Title)
                putStringArrayList(Constants.TOP10_PLAYERS, playerNames)
                putIntegerArrayList(Constants.TOP10_SCORES, playerScores)
            }
            Log.d(TAG, "newInstance.")
            return Top10Fragment(listener).apply {
                arguments = args
            }
        }
    }
}