package com.smile.colorballs.views.xml_base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.core.os.BundleCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.databinding.FragmentTop10Binding
import com.smile.colorballs.databinding.Top10ScoreListItemsBinding
import com.smile.colorballs.models.TopPlayer
import com.smile.smilelibraries.player_record_rest.models.Player
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
    private var top10Players: ArrayList<TopPlayer> = ArrayList()
    // private val medalImageIds = ArrayList<Int>()
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
            val medalImageIds = listOf(
                R.drawable.gold_medal,
                R.drawable.silver_medal,
                R.drawable.bronze_medal,
                R.drawable.copper_medal,
                R.drawable.olympics_image,
                R.drawable.olympics_image,
                R.drawable.olympics_image,
                R.drawable.olympics_image,
                R.drawable.olympics_image,
                R.drawable.olympics_image
            )
            Log.d(TAG, "onCreate.new Fragment instance")
            Log.d(TAG, "onCreate.arguments = $arguments")
            arguments?.apply {
                getString(Constants.TOP10_TITLE_NAME)?.let { nameIt ->
                    top10TitleName = nameIt
                }
                val players = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    BundleCompat.getParcelableArrayList(
                        this@apply, Constants.TOP10_PLAYERS, Player::class.java)!!
                } else getParcelableArrayList(Constants.TOP10_PLAYERS)!!
                Log.d(TAG, "onCreate.players.size = ${players.size}")
                for (i in 0 until players.size) {
                    players[i].playerName?.let { name ->
                        if (name.trim().isEmpty()) players[i].playerName = "No Name"
                    } ?: run {
                        Log.d(TAG, "onCreate.players[i].playerName = null")
                        players[i].playerName = "No Name"
                    }
                    top10Players.add(TopPlayer(players[i], medalImageIds[i]))
                }
            }
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

            Log.d(TAG, "onViewCreated.TopViewAdapter.top10Players.size = ${top10Players.size}")
            top10ListView = binding.top10ListView.apply {
                setHasFixedSize(true)
                adapter = TopViewAdapter(top10Players)
                layoutManager = LinearLayoutManager(activity)
            }
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach")
        super.onDetach()
    }

    private inner class TopViewAdapter(
        private val topPlayers: ArrayList<TopPlayer>
    ): RecyclerView.Adapter<TopViewAdapter.MyViewHolder>() {
        init {
            Log.d(TAG, "TopViewAdapter.players.size = ${topPlayers.size}")
        }
        inner class MyViewHolder(private val binding: Top10ScoreListItemsBinding):
            RecyclerView.ViewHolder(binding.root) {
                init {
                    // items for one screen
                    Log.d(TAG, "MyViewHolder")
                    binding.playerTextView.let {
                        ScreenUtil.resizeTextSize(it, textFontSize,
                            ScreenUtil.FontSize_Pixel_Type)
                    }
                    binding.scoreTextView.let {
                        ScreenUtil.resizeTextSize(it, textFontSize,
                            ScreenUtil.FontSize_Pixel_Type)
                    }
                    binding.medalImage.let {
                        // set ImageView size
                        it.layoutParams.height = (textFontSize * 4).toInt()
                        it.layoutParams.width = (textFontSize * 4).toInt()
                    }
                }

                fun bindData(topPlayer: TopPlayer) {
                    binding.apply {
                        lifecycleOwner = this@Top10Fragment
                        mTop10Player = topPlayer
                    }
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val binding = Top10ScoreListItemsBinding.inflate(layoutInflater,
                parent, false)
            parent.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        Log.d(TAG, "onCreateViewHolder.parent.height = ${parent.height}")
                        val itemNum = if (resources.configuration.orientation
                            == Configuration.ORIENTATION_LANDSCAPE) 2 else 4
                        Log.d(TAG, "onCreateViewHolder.itemNum = $itemNum")
                        binding.root.layoutParams.height = parent.height / itemNum
                        parent.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            return MyViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return topPlayers.size
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            Log.d(TAG, "onBindViewHolder.position = $position")
            if (position<0) return
            holder.itemView.setOnClickListener {
                ScreenUtil.showToast(
                    activity, topPlayers[position].player.playerName,
                    textFontSize, ScreenUtil.FontSize_Pixel_Type,
                    Toast.LENGTH_LONG
                )
            }
            holder.bindData(topPlayers[position])
        }
    }

    companion object {
        // private properties
        private const val TAG = "Top10Fragment"
        @JvmStatic
        fun newInstance(
            top10Title: String,
            top10Players: ArrayList<Player>,
            listener: Top10OkButtonListener
        ) = Top10Fragment(listener).apply {
                arguments = Bundle().apply {
                    Log.d(TAG, "newInstance.putString")
                    putString(Constants.TOP10_TITLE_NAME, top10Title)
                    Log.d(TAG, "newInstance.putParcelableArrayList")
                    putParcelableArrayList(Constants.TOP10_PLAYERS, top10Players)
            }
        }
    }
}