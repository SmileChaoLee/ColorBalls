package com.smile.colorballs.compose_view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.sp
import androidx.core.os.BundleCompat
import com.smile.colorballs.R
import com.smile.colorballs.constants.Constants
import com.smile.colorballs.models.TopPlayer
import com.smile.smilelibraries.player_record_rest.models.Player
import com.smile.smilelibraries.utilities.ScreenUtil

/**
 * A simple [Fragment] subclass.
 * Use the [Top10ComposeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class Top10ComposeFragment: Fragment {
    private lateinit var buttonListener: Composables.OkButtonListener
    private var mContext: Context? = null
    private var top10Players: ArrayList<TopPlayer> = ArrayList()
    private var top10TitleName: String = ""
    private var textFontSize = 0f

    constructor()

    @SuppressLint("ValidFragment")
    private constructor(listener: Composables.OkButtonListener) : super() {
        Log.d(TAG, "private constructor")
        buttonListener = listener
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach")
        super.onAttach(context)
        mContext = context
        textFontSize = Composables.textFontSize.value
        Log.d(TAG, "onAttach.textFontSize = $textFontSize")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
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
                    R.drawable.olympics_image)
                Log.d(TAG, "onCreate.new Fragment instance")
                arguments?.apply {
                    Log.d(TAG, "onCreate.retrieve arguments")
                    getString(Constants.TOP10_TITLE_NAME)?.let { nameIt ->
                        top10TitleName = nameIt
                    }
                    val players = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        BundleCompat.getParcelableArrayList(
                            this@apply, Constants.TOP10_PLAYERS, Player::class.java)!!
                    } else getParcelableArrayList(Constants.TOP10_PLAYERS)!!
                    for (i in 0 until players.size) {
                        players[i].playerName?.let { name ->
                            if (name.trim().isEmpty()) players[i].playerName = "No Name"
                        } ?: run {
                            Log.d(TAG, "onCreate.players[i].playerName = null")
                            players[i].playerName = "No Name"
                        }
                        top10Players.add(TopPlayer(players[i], medalImageIds[i]))
                    }
                    Log.d(TAG, "onCreate.retrieve arguments")
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView")
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_top10_compose, container, false)
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy
                .DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Log.d(TAG, "onCreateView.setContent.ComposableFunc.Top10Compose")
                Composables.Top10Composable(title = top10TitleName,
                    topPlayers = top10Players, buttonListener = buttonListener,
                    resources.getString(R.string.okStr))
            }
        }
    }

    companion object {
        private const val TAG = "Top10ComposeFragment"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment Top10ComposeFragment.
         */
        @JvmStatic
        fun newInstance(top10Title: String,
                        top10Players: ArrayList<Player>,
                        listener: Composables.OkButtonListener) =
            Top10ComposeFragment(listener).apply {
                arguments = Bundle().apply {
                    Log.d(TAG, "newInstance.setting arguments")
                    putString(Constants.TOP10_TITLE_NAME, top10Title)
                    putParcelableArrayList(Constants.TOP10_PLAYERS, top10Players)
                }
            }
    }
}