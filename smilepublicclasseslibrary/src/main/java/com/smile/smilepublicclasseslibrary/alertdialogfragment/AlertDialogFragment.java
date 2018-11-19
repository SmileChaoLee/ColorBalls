package com.smile.smilepublicclasseslibrary.alertdialogfragment;

import com.smile.smilepublicclasseslibrary.*;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.Serializable;

public class AlertDialogFragment extends DialogFragment {

    private TextView text_shown = null;
    private EditText text_edit = null;
    private Button noButton = null;
    private Button okButton = null;

    private String textContext = "";
    private float textSize = 24;
    private int textColor = 0;
    private int dialogWidth = 0;
    private int dialogHeight = 0;
    private int numButtons = 0; // default is no buttons
    private boolean isAnimation = false;
    private Animation animationText;

    private AlertDialogFragment alertDialog = null;

    private DialogButtonListener ndl;

    public interface DialogButtonListener {
        void noButtonOnClick(AlertDialogFragment dialogFragment);
        void okButtonOnClick(AlertDialogFragment dialogFragment);
    }

    public AlertDialogFragment() {
    }
    @SuppressLint("ValidFragment")
    public AlertDialogFragment(DialogButtonListener ndl) {
        super();
        this.ndl = ndl;
    }
    public static AlertDialogFragment newInstance(String textContent, float textSize, int color, int width, int height, boolean isAnimation) {
        AlertDialogFragment modalDialog = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("textContent", textContent);
        args.putFloat("textSize", textSize);
        args.putInt("color", color);
        args.putInt("width", width);
        args.putInt("height", height);
        args.putInt("numButtons", 0);
        args.putBoolean("IsAnimation", isAnimation);
        modalDialog.setArguments(args);

        return modalDialog;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        // super.show(manager, tag);
        FragmentTransaction ft = manager.beginTransaction();
        Fragment prev = manager.findFragmentByTag(tag);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        System.out.println("AlertDialogFragment.onCreate() is called");

        // if statement was added on 2018-06-14 for avoiding to reset the parameters and values of
        // properties because of configuration changing
        if (savedInstanceState == null) {
            // initialize variables
            textContext = "No content";
            textSize = 24;  // default font size
            textColor = Color.BLUE;
            dialogWidth = 0;    // wrap_content
            dialogHeight = 0;   // wrap_content
            numButtons = 0;     // default is no buttons
            isAnimation = false;

            Bundle args = getArguments();
            if (args != null) {
                textContext = args.getString("textContent");
                textSize = args.getFloat("textSize");
                textColor = args.getInt("color");
                dialogWidth = args.getInt("width");
                dialogHeight = args.getInt("height");
                numButtons = args.getInt("numButtons");
                isAnimation = args.getBoolean("IsAnimation", false);
            }

            if (isAnimation) {
                animationText = new AlphaAnimation(0.0f,1.0f);
                animationText.setDuration(300);
                animationText.setStartOffset(0);
                animationText.setRepeatMode(Animation.REVERSE);
                animationText.setRepeatCount(Animation.INFINITE);
            }

            float factor = getActivity().getResources().getDisplayMetrics().density;
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
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("AlertDialogFragment.onCreateView() is called.");

        // View view = inflater.inflate(R.layout.modal_dialogfragment, container, false);
        // Changed to
        View view = inflater.inflate(R.layout.modal_dialogfragment, container);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("AlertDialogFragment.onViewCreated() is called.");

        Window window = getDialog().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.0f);
        // remove the background of DialogFragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        // setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme);  // removed on 2018-11-09 at 7:72am
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setCancelable(false);   // make dialog a modal
        setShowsDialog(true);   // added on 2018-06-24

        alertDialog = this;

        FrameLayout fLayout = view.findViewById(R.id.dialog_fragment_body_layout);
        FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(dialogWidth, dialogHeight, Gravity.CENTER);
        fLayout.setLayoutParams(ll);

        text_shown = view.findViewById(R.id.text_shown);
        text_shown.setText(textContext);
        text_shown.setTextSize(textSize);
        text_shown.setTextColor(textColor);
        if ( (isAnimation) && (animationText != null) ) {
            text_shown.startAnimation(animationText);
        }

        LinearLayout noButton_Layout = view.findViewById(R.id.noButton_Layout);
        noButton = view.findViewById(R.id.dialogfragment_noButton);
        noButton.setTextSize(textSize);
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ndl != null) {
                    ndl.noButtonOnClick(alertDialog);
                }
            }
        });
        LinearLayout okButton_Layout = view.findViewById(R.id.okButton_Layout);
        okButton = view.findViewById(R.id.dialogfragment_okButton);
        okButton.setTextSize(textSize);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ndl != null) {
                    ndl.okButtonOnClick(alertDialog);
                }
            }
        });
        LinearLayout.LayoutParams lp = null;
        switch (numButtons) {
            case 2:
                // buttons. nothing has to be changed
                break;
            case 1:
                // only 1 button, then disable noButton and make it invisible
                noButton_Layout.setVisibility(View.GONE);
                noButton.setEnabled(false);
                lp = (LinearLayout.LayoutParams)noButton_Layout.getLayoutParams();
                lp.weight = 0.0f;

                lp = (LinearLayout.LayoutParams)okButton_Layout.getLayoutParams();
                lp.weight = 2.0f;

                break;
            case 0:
                // no buttons
            default:
                // no buttons

                // setting buttons
                // noButton_Layout.setVisibility(View.GONE);
                noButton.setEnabled(false);
                // okButton_Layout.setVisibility(View.GONE);
                okButton.setEnabled(false);

                LinearLayout buttons_LinearLayout = view.findViewById(R.id.linearlayout_for_buttons_in_modalfragment);
                buttons_LinearLayout.setVisibility(View.GONE);

                break;
        }
    }

    // due to the bugs of Android SDK, this override to onDestroyView() has to be done. Added on 2018-06-19 11:50pm
    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if ( (dialog != null) && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    public TextView getText_shown() {
        return this.text_shown;
    }
    public void setText_shown(TextView text_shown) {
        this.text_shown = text_shown;
    }
    public Button getNoButton() {
        return this.noButton;
    }
    public Button getOkButton() {
        return this.okButton;
    }

}
