package com.smile.colorballs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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

    // public properties
    public static final String Top10ScoreFragmentTag = "Top10ScoreFragmentTag";

    // private properties
    private static final String TAG = "Top10ScoreFragment";

    private View top10ScoreFragmentView = null;

    private ArrayList<String> top10Players = new ArrayList<String>();
    private ArrayList<Integer> top10Scores = new ArrayList<Integer>();
    private ArrayList<Integer> medalImageIds = new ArrayList<Integer>();
    private ListView top10ListView = null;
    private myListAdapter mListAdapter = null;
    private Top10OkButtonListener top10OkButtonListener = null;


    private OnFragmentInteractionListener mListener;

    public interface Top10OkButtonListener {
        public void buttonOkClick(Activity activity);
    }

    public Top10ScoreFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public Top10ScoreFragment(Top10OkButtonListener listener) {
        super();
        this.top10OkButtonListener = listener;
    }

    public static Top10ScoreFragment newInstance(ArrayList<String> playerNames, ArrayList<Integer> playerScores, Top10OkButtonListener listener) {
        Top10ScoreFragment fragment;
        if (listener == null) {
            fragment = new Top10ScoreFragment();
        } else {
            fragment = new Top10ScoreFragment(listener);
        }

        Bundle args = new Bundle();
        args.putStringArrayList("Top10Players", playerNames);
        args.putIntegerArrayList("Top10Scores", playerScores);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setRetainInstance(false);    // removed on 2018-06-14
        setRetainInstance(true);    // added on 2018-06-14

        // if statement was added on 2018-06-14
        if (savedInstanceState == null) {   // if new Fragment instance
            Bundle args = getArguments();
            if (args != null) {
                top10Players = args.getStringArrayList("Top10Players");
                top10Scores = args.getIntegerArrayList("Top10Scores");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // historyView = inflater.inflate(R.layout.fragment_score_history, container, false);
        View view = inflater.inflate(R.layout.layout_for_top10_score_fragment, container, false);

        System.out.println("Top10ScoreFragment ---> onCreateView() method. ");

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("Top10ScoreFragment onViewCreated() is called.");
        top10ScoreFragmentView = view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {   // new Fragment instance
            Button okButton = (Button) top10ScoreFragmentView.findViewById(R.id.top10OkButton);
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
            mListAdapter = new myListAdapter(getActivity(), R.layout.top10_score_list_items, top10Players, top10Scores, medalImageIds);

            top10ListView = top10ScoreFragmentView.findViewById(R.id.top10ListView);
            // this following statement was removed on 2018-06-14
            // top10ListView.setAdapter(new myListAdapter(getActivity(), R.layout.top10_score_list_items, top10Players, top10Scores, medalImageIds));
            top10ListView.setAdapter(mListAdapter);    // added on 2018-06-14
            top10ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            mListener = new Top10ScoreFragment.OnFragmentInteractionListener() {
                @Override
                public void onFragmentInteraction(Uri uri) {
                    System.out.println("must implement OnFragmentInteractionListener --> Uri = " + uri);
                }
            };
        }
    }

    @Override
    public void onDetach() {
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private class myListAdapter extends ArrayAdapter {  // changed name to MyListAdapter from myListAdapter

        private int layoutId;
        private ArrayList<String> players;
        private ArrayList<Integer> scores;
        private ArrayList<Integer> medals;

        public myListAdapter(Context context, int layoutId, ArrayList<String> players, ArrayList<Integer> scores, ArrayList<Integer> medals) {
            super(context, layoutId, players);

            this.layoutId = layoutId;

            if (players == null) {
                this.players = new ArrayList<String>();
            } else {
                this.players = players;
            }

            if (scores == null) {
                this.scores = new ArrayList<Integer>();
            } else {
                this.scores = scores;
            }

            if (medals == null) {
                this.medals = new ArrayList<Integer>();
            } else {
                this.medals = medals;
            }
        }

        @Nullable
        @Override
        public Object getItem(int position) {
            return super.getItem(position);
        }

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
            int itemHeight = listViewHeight / 4;    // 4 items for one screen
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = itemHeight;
            // view.setLayoutParams(layoutParams);  // no needed

            TextView pTextView = view.findViewById(R.id.playerTextView);
            TextView sTextView = view.findViewById(R.id.scoreTextView);
            ImageView medalImage = view.findViewById(R.id.medalImage);

            pTextView.setText(players.get(position));
            sTextView.setText(String.valueOf(scores.get(position)));
            medalImage.setImageResource(medals.get(position));


            return view;
        }
    }
}
