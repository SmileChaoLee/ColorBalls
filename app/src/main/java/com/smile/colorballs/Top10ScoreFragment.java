package com.smile.colorballs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Top10ScoreFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Top10ScoreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Top10ScoreFragment extends Fragment {

    // private properties
    private static final String TAG = new String("com.smile.colorballs.Top10ScoreFragment");

    private Context context;
    private View top10ScoreFragmentView = null;

    private ArrayList<String> top10Players = new ArrayList<>();
    private ArrayList<Integer> top10Scores = new ArrayList<>();
    private ArrayList<Integer> medalImageIds = new ArrayList<>();
    private ListView top10ListView = null;
    private myListAdapter mListAdapter = null;
    private Button okButton = null;
    private Top10OkButtonListener top10OkButtonListener = null;
    private TextView titleForTop10ListView = null;
    private String top10TitleName = "";

    private float textFontSize;

    private OnFragmentInteractionListener mListener;

    public interface Top10OkButtonListener {
        void buttonOkClick(Activity activity);
    }

    public Top10ScoreFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    private Top10ScoreFragment(Top10OkButtonListener listener) {
        super();
        this.top10OkButtonListener = listener;
    }

    public static Top10ScoreFragment newInstance(String top10Title, ArrayList<String> playerNames, ArrayList<Integer> playerScores, Top10OkButtonListener listener) {
        Top10ScoreFragment fragment;
        if (listener == null) {
            fragment = new Top10ScoreFragment();
        } else {
            fragment = new Top10ScoreFragment(listener);
        }

        Bundle args = new Bundle();
        args.putString(Constants.Top10TitleNameKey, top10Title);
        args.putStringArrayList(Constants.Top10PlayersKey, playerNames);
        args.putIntegerArrayList(Constants.Top10ScoresKey, playerScores);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "Top10ScoreFragment.onAttach() is called. ");
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            mListener = new Top10ScoreFragment.OnFragmentInteractionListener() {
                @Override
                public void onFragmentInteraction(Uri uri) {
                    Log.d(TAG, "must implement OnFragmentInteractionListener --> Uri = " + uri);
                }
            };
        }

        this.context = context;
        float defaultTextFontSize = ScreenUtil.getDefaultTextSizeFromTheme(getActivity(), ScreenUtil.FontSize_Pixel_Type, null);
        textFontSize = ScreenUtil.suitableFontSize(getActivity(), defaultTextFontSize, ScreenUtil.FontSize_Pixel_Type,0.0f);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "Top10ScoreFragment.onCreate() is called. ");
        super.onCreate(savedInstanceState);
        // setRetainInstance(true);    // deprecated

        // if statement was added on 2018-06-14
        if (savedInstanceState == null) {   // if new Fragment instance
            Bundle args = getArguments();
            if (args != null) {
                top10TitleName = args.getString(Constants.Top10TitleNameKey);
                top10Players = args.getStringArrayList(Constants.Top10PlayersKey);
                top10Scores = args.getIntegerArrayList(Constants.Top10ScoresKey);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "Top10ScoreFragment.onCreateView() is called. ");
        // Inflate the layout for this fragment
        // historyView = inflater.inflate(R.layout.fragment_score_history, container, false);
        View view = inflater.inflate(R.layout.layout_for_top10_score_fragment, container, false);

        TextView top10TitleTextView = view.findViewById(R.id.top10ScoreTitle);
        top10TitleTextView.setText(top10TitleName);
        ScreenUtil.resizeTextSize(top10TitleTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Top10ScoreFragment.onViewCreated() is called.");
        super.onViewCreated(view, savedInstanceState);
        top10ScoreFragmentView = view;

        // moved here from onActivityCreated() because onActivityCreated() is deprecated
        if (savedInstanceState == null) {   // new Fragment instance
            titleForTop10ListView = top10ScoreFragmentView.findViewById(R.id.top10ScoreTitle);
            ScreenUtil.resizeTextSize(titleForTop10ListView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
            okButton = top10ScoreFragmentView.findViewById(R.id.top10OkButton);
            ScreenUtil.resizeTextSize(okButton, textFontSize, ScreenUtil.FontSize_Pixel_Type);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    top10OkButtonListener.buttonOkClick(getActivity());
                }
            });

            medalImageIds.add(R.drawable.gold_medal);
            medalImageIds.add(R.drawable.silver_medal);
            medalImageIds.add(R.drawable.bronze_medal);
            medalImageIds.add(R.drawable.copper_medal);
            medalImageIds.add(R.drawable.olympics_image);
            medalImageIds.add(R.drawable.olympics_image);
            medalImageIds.add(R.drawable.olympics_image);
            medalImageIds.add(R.drawable.olympics_image);
            medalImageIds.add(R.drawable.olympics_image);
            medalImageIds.add(R.drawable.olympics_image);

            // the following statement was added on 2018-06-14
            int layId = R.layout.top10_score_list_items;
            mListAdapter = new myListAdapter(getActivity(), layId, top10Players, top10Scores, medalImageIds);

            top10ListView = top10ScoreFragmentView.findViewById(R.id.top10ListView);
            // this following statement was removed on 2018-06-14
            // top10ListView.setAdapter(new myListAdapter(getActivity(), R.layout.top10_score_list_items, top10Players, top10Scores, medalImageIds));
            top10ListView.setAdapter(mListAdapter);    // added on 2018-06-14
            top10ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
            //
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "Top10ScoreFragment.onDetach() is called.");
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class myListAdapter extends ArrayAdapter {  // changed name to MyListAdapter from myListAdapter

        private int layoutId;
        private ArrayList<String> players;
        private ArrayList<Integer> scores;
        private ArrayList<Integer> medals;

        @SuppressWarnings("unchecked")
        myListAdapter(Context context, int layoutId, ArrayList<String> players, ArrayList<Integer> scores, ArrayList<Integer> medals) {
            super(context, layoutId, players);

            this.layoutId = layoutId;

            if (players == null) {
                this.players = new ArrayList<>();
            } else {
                this.players = players;
            }

            if (scores == null) {
                this.scores = new ArrayList<>();
            } else {
                this.scores = scores;
            }

            if (medals == null) {
                this.medals = new ArrayList<>();
            } else {
                this.medals = medals;
            }
        }

        @Nullable
        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

        @SuppressWarnings("unchecked")
        @Override
        public int getPosition(@Nullable Object item) {
            return super.getPosition(item);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            View view = getLayoutInflater().inflate(layoutId, parent,false);

            if (getCount() == 0) {
                return view;
            }

            int listViewHeight = parent.getHeight();
            int itemNum = 4;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                itemNum = 2;
            }
            int itemHeight = listViewHeight / itemNum;    // items for one screen
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = itemHeight;
            // view.setLayoutParams(layoutParams);  // no needed

            TextView pTextView = view.findViewById(R.id.playerTextView);
            ScreenUtil.resizeTextSize(pTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
            TextView sTextView = view.findViewById(R.id.scoreTextView);
            ScreenUtil.resizeTextSize(sTextView, textFontSize, ScreenUtil.FontSize_Pixel_Type);
            ImageView medalImage = view.findViewById(R.id.medalImage);

            pTextView.setText(players.get(position));
            sTextView.setText(String.valueOf(scores.get(position)));
            medalImage.setImageResource(medals.get(position));

            return view;
        }
    }
}
