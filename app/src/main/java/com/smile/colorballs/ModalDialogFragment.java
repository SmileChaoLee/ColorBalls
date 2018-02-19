package com.smile.colorballs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by chaolee on 2017-12-30.
 */

public class ModalDialogFragment extends DialogFragment {

    private TextView text_shown = null;
    private Button button1 = null;
    private Button button2 = null;

    private String textContext = "";
    private int textColor = 0;
    private int dialogWidth = 0;
    private int dialogHeight = 0;
    private int numButtons = 0; // default is no buttons
    private DialogButtonListener ndl;

    public interface DialogButtonListener {
        public void button1OnClick(ModalDialogFragment dialogFragment);
        public void button2OnClick(ModalDialogFragment dialogFragment);
    }

    public ModalDialogFragment() {
    }

    @SuppressLint("ValidFragment")
    public ModalDialogFragment(DialogButtonListener ndl) {
        super();
        this.ndl = ndl;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textContext = getArguments().getString("textContent");
        textColor = getArguments().getInt("color");
        dialogWidth = getArguments().getInt("width");
        dialogHeight = getArguments().getInt("height");

        float factor =  getActivity().getResources().getDisplayMetrics().density;
        if (dialogWidth == 0) {
            dialogWidth = FrameLayout.LayoutParams.WRAP_CONTENT;
        } else {
            dialogWidth = (int) ((float) dialogWidth * factor);
        }
        if (dialogHeight == 0) {
            dialogHeight = FrameLayout.LayoutParams.WRAP_CONTENT;
        } else {
            dialogHeight = (int) ((float) dialogHeight * factor);
        }
        numButtons = getArguments().getInt("numButtons");
    }

    public static ModalDialogFragment newInstance(String textContent, int color, int width, int height, int numButtons) {
        ModalDialogFragment modalDialog = new ModalDialogFragment();
        Bundle args = new Bundle();
        args.putString("textContent", textContent);
        args.putInt("color", color);
        args.putInt("width", width);
        args.putInt("height", height);
        args.putInt("numButtons", numButtons);
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

        setStyle(DialogFragment.STYLE_NORMAL,R.style.MyDialogFragmentStyle);
        // setStyle(DialogFragment.STYLE_NO_INPUT,,R.style.MyDialogFragmentStyle);   // make dialog a modal
        // setStyle(DialogFragment.STYLE_NO_FRAME, R.style.MyDialogFragmentStyle);  // make dialog a modal
        setCancelable(false);   // make dialog a modal

        // for background window
        // WindowManager.LayoutParams lp = getDialog().getWindow().getAttributes();
        // lp.dimAmount = 0.0f; // no dim
        // getDialog().getWindow().setAttributes(lp);
        // the three statements above are useless

        Window window = getDialog().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.0f);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout dialogView = (FrameLayout)view.findViewById(R.id.dialog_layout);
        // FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams)dialogView.getLayoutParams();
        FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(dialogWidth, dialogHeight, Gravity.CENTER);
        // ll.width = dialogWidth;
        // ll.height = dialogHeight;
        // ll.gravity = Gravity.CENTER;
        dialogView.setLayoutParams(ll);
        // layoutParams.width = dialogWidth;
        // layoutParams.height = dialogHeight;
        // dialogView.setLayoutParams(layoutParams);



        text_shown = view.findViewById(R.id.text_shown);
        text_shown.setText(textContext);
        text_shown.setTextColor(textColor);
        button1 = view.findViewById(R.id.dialogfragment_button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ndl.button1OnClick(ModalDialogFragment.this);
            }
        });
        button2 = view.findViewById(R.id.dialogfragment_button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ndl.button2OnClick(ModalDialogFragment.this);
            }
        });
        LinearLayout.LayoutParams lp = null;
        switch (numButtons) {
            case 2:
                // buttons. nothing has to be changed
                break;
            case 1:
                // only 1 button, then disable button2 and make it invisible
                button2.setVisibility(View.GONE);
                button2.setEnabled(false);
                lp = (LinearLayout.LayoutParams)button2.getLayoutParams();
                lp.weight = 0.0f;

                lp = (LinearLayout.LayoutParams)button1.getLayoutParams();
                lp.weight = 2.0f;

                break;
            case 0:
                // no buttons
            default:
                // no buttons
                // TextView
                lp = (LinearLayout.LayoutParams)text_shown.getLayoutParams();
                lp.weight = 3.0f;

                // buttons
                button1.setVisibility(View.GONE);
                button1.setEnabled(false);
                button2.setVisibility(View.GONE);
                button2.setEnabled(false);
                LinearLayout linearLayout = view.findViewById(R.id.linearlayout_for_buttons_in_modalfragment);
                lp = (LinearLayout.LayoutParams)linearLayout.getLayoutParams();
                lp.weight = 0.0f;

                break;
        }
    }

    public void showDialogFragment(Activity activity) {
        FragmentManager fmManager = activity.getFragmentManager();
        Fragment prev = fmManager.findFragmentByTag("dialog");
        FragmentTransaction ft = fmManager.beginTransaction();
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        this.show(ft,"dialog");
    }

    public TextView getText_shown() {
        return this.text_shown;
    }
    public void setText_shown(TextView text_shown) {
        this.text_shown = text_shown;
    }
    public Button getButton1() {
        return this.button1;
    }
    public Button getButton2() {
        return this.button2;
    }
}
