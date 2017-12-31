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
 * Created by chaolee on 2017-12-30.
 */

public class LoadHistoryDialogFragment extends DialogFragment {

    private TextView textLoad = null;

    public LoadHistoryDialogFragment() {
        setStyle(DialogFragment.STYLE_NORMAL,R.style.MyDialogFragmentStyle);
        // setStyle(DialogFragment.STYLE_NO_INPUT,,R.style.MyDialogFragmentStyle);   // make dialog a modal
        // setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MyDialogFragmentStyle);  // make dialog a modal
        setCancelable(false);   // make dialog a modal

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle("Loading Score History");
        // View view = inflater.inflate(R.layout.loading_dialogfragment,container,false);
        View view = inflater.inflate(R.layout.load_history_dialogfragment,container);

        return view;
        // return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        textLoad = view.findViewById(R.id.textLoad);
    }

    public TextView getTextLoad() {
        return this.textLoad;
    }
}
