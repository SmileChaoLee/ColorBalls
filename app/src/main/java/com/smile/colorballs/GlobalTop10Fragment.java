package com.smile.colorballs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
 * {@link GlobalTop10Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GlobalTop10Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GlobalTop10Fragment extends Fragment {

    // public properties
    public static final String GlobalTop10FragmentTag = "GlobalTop10FragmentTag";

    // private properties
    private static final String TAG = new String("com.smile.colorballs.GlobalTop10Fragment");

    private View globalTop10FragmentView = null;

    private ArrayList<String> top10Players = new ArrayList<>();
    private ArrayList<Integer> top10Scores = new ArrayList<>();
    private ArrayList<Integer> medalImageIds = new ArrayList<>();
    private ListView top10ListView = null;
    private myListAdapter mListAdapter = null;
    private Button okButton = null;
    private GlobalTop10OkButtonListener globalTop10OkButtonListener = null;
    private TextView titleForGlobalTop10ListView = null;

    private int fontSizeForText = 24;

    private OnFragmentInteractionListener mListener;

    public interface GlobalTop10OkButtonListener {
        void buttonOkClick(Activity activity);
    }

    public GlobalTop10Fragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    private GlobalTop10Fragment(GlobalTop10OkButtonListener listener) {
        super();
        this.globalTop10OkButtonListener = listener;
    }

    public static GlobalTop10Fragment newInstance(ArrayList<String> playerNames, ArrayList<Integer> playerScores, int fontSize, GlobalTop10Fragment.GlobalTop10OkButtonListener listener) {
        GlobalTop10Fragment fragment;
        if (listener == null) {
            fragment = new GlobalTop10Fragment();
        } else {
            fragment = new GlobalTop10Fragment(listener);
        }

        Bundle args = new Bundle();
        args.putStringArrayList("Top10Players", playerNames);
        args.putIntegerArrayList("Top10Scores", playerScores);
        args.putInt("FontSizeForText", fontSize);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);    // added on 2018-06-14

        // if statement was added on 2018-06-14
        if (savedInstanceState == null) {   // if new Fragment instance
            Bundle args = getArguments();
            if (args != null) {
                top10Players = args.getStringArrayList("Top10Players");
                top10Scores = args.getIntegerArrayList("Top10Scores");
                fontSizeForText = args.getInt("FontSizeForText");
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // historyView = inflater.inflate(R.layout.fragment_score_history, container, false);
        View view = inflater.inflate(R.layout.layout_for_global_top10_fragment, container, false);

        System.out.println("GlobalTop10Fragment ---> onCreateView() method. ");

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("GlobalTop10Fragment onViewCreated() is called.");
        globalTop10FragmentView = view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {   // new Fragment instance
            titleForGlobalTop10ListView = globalTop10FragmentView.findViewById(R.id.globalTop10Title);
            okButton = globalTop10FragmentView.findViewById(R.id.globalTop10OkButton);
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    globalTop10OkButtonListener.buttonOkClick(getActivity());
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
            int layId = R.layout.global_top10_list_items;
            mListAdapter = new myListAdapter(getActivity(), layId, top10Players, top10Scores, medalImageIds);

            top10ListView = globalTop10FragmentView.findViewById(R.id.globalTop10ListView);
            top10ListView.setAdapter(mListAdapter);    // added on 2018-06-14
            top10ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                }
            });
        }
        if (okButton != null) {
            // set text size for okButton
            okButton.setTextSize(fontSizeForText);
        }
        if (titleForGlobalTop10ListView != null) {
            titleForGlobalTop10ListView.setTextSize(fontSizeForText);
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
            mListener = new GlobalTop10Fragment.OnFragmentInteractionListener() {
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
                itemNum = 3;
            }
            int itemHeight = listViewHeight / itemNum;    // items for one screen
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = itemHeight;
            // view.setLayoutParams(layoutParams);  // no needed

            TextView pTextView = view.findViewById(R.id.playerTextView);
            pTextView.setTextSize(fontSizeForText);
            TextView sTextView = view.findViewById(R.id.scoreTextView);
            sTextView.setTextSize(fontSizeForText);
            ImageView medalImage = view.findViewById(R.id.medalImage);

            pTextView.setText(players.get(position));
            sTextView.setText(String.valueOf(scores.get(position)));
            medalImage.setImageResource(medals.get(position));


            return view;
        }
    }
}
