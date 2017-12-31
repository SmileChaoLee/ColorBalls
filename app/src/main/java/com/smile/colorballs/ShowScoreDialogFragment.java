package com.smile.colorballs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by chaolee on 2017-12-31.
 */

public class ShowScoreDialogFragment extends DialogFragment {

    private TextView textScore = null;

    public ShowScoreDialogFragment() {
        setStyle(DialogFragment.STYLE_NORMAL,R.style.MyDialogFragmentStyle);
        // setStyle(DialogFragment.STYLE_NO_INPUT,,R.style.MyDialogFragmentStyle);   // make dialog a modal
        // setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MyDialogFragmentStyle);  // make dialog a modal
        setCancelable(false);   // make dialog a modal

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // View view = inflater.inflate(R.layout.show_score_dialogfragment,container,false);
        View view = inflater.inflate(R.layout.show_score_dialogfragment,container);

        return view;
        // return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        textScore = view.findViewById(R.id.textScore);
    }

    public TextView getTextScore() {
        return this.textScore;
    }
}

