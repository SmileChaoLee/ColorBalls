package com.smile.colorballs;

import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * Created by chaolee on 2017-12-30.
 */

public class ModalDialogFragment extends DialogFragment {

    private TextView text_shown = null;
    private String textContext = "";
    private int textColor = 0;
    private int dialogWidth = 0;
    private int dialogHeight = 0;

    public ModalDialogFragment() {
        setStyle(DialogFragment.STYLE_NORMAL,R.style.MyDialogFragmentStyle);
        // setStyle(DialogFragment.STYLE_NO_INPUT,,R.style.MyDialogFragmentStyle);   // make dialog a modal
        // setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MyDialogFragmentStyle);  // make dialog a modal
        setCancelable(false);   // make dialog a modal
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textContext = getArguments().getString("text_shown");
        textColor = getArguments().getInt("color");
        dialogWidth = getArguments().getInt("width");
        dialogHeight = getArguments().getInt("height");

        float factor =  getActivity().getResources().getDisplayMetrics().density;
        dialogWidth = (int)((float)dialogWidth * factor);
        dialogHeight = (int)((float)dialogHeight * factor);
    }

    public static ModalDialogFragment newInstance(String text_shown, int color, int width, int height) {
        ModalDialogFragment modalDialog = new ModalDialogFragment();
        Bundle args = new Bundle();
        args.putString("text_shown", text_shown);
        args.putInt("color", color);
        args.putInt("width", width);
        args.putInt("height", height);
        modalDialog.setArguments(args);

        return modalDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // View view = inflater.inflate(R.layout.loading_dialogfragment,container,false);
        View view = inflater.inflate(R.layout.modal_dialogfragment,container);

        return view;
        // return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ViewGroup dialogView = view.findViewById(R.id.dialog_layout);
        ViewGroup.LayoutParams layoutParams = dialogView.getLayoutParams();
        layoutParams.width = dialogWidth;
        layoutParams.height = dialogHeight;
        dialogView.setLayoutParams(layoutParams);

        text_shown = view.findViewById(R.id.text_shown);
        text_shown.setText(textContext);
        text_shown.setTextColor(textColor);
    }

    public TextView getText_shown() {
        return this.text_shown;
    }
}
