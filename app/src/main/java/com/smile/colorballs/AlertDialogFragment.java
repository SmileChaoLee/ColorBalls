package com.smile.colorballs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.CalendarContract;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlertDialogFragment extends DialogFragment {

    private TextView text_shown = null;
    private EditText text_edit = null;
    private Button button1 = null;
    private Button button2 = null;

    private String textContext = "";
    private float textSize = 24;
    private int textColor = 0;
    private int dialogWidth = 0;
    private int dialogHeight = 0;
    private int numButtons = 0; // default is no buttons

    private String modalDialogTag = "";
    private boolean isDialogShown = false;
    private boolean isDismissed = false;
    private View modalDialogView = null;
    private AlertDialogFragment alertDialog = null;

    private DialogButtonListener ndl;
    private Context context = null;
    private Activity activity = null;

    public interface DialogButtonListener {
        void button1OnClick(AlertDialogFragment dialogFragment);
        void button2OnClick(AlertDialogFragment dialogFragment);
    }

    public AlertDialogFragment() {
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

            Bundle args = getArguments();
            if (args != null) {
                textContext = args.getString("textContent");
                textSize = args.getFloat("textSize");
                textColor = args.getInt("color");
                dialogWidth = args.getInt("width");
                dialogHeight = args.getInt("height");
                numButtons = getArguments().getInt("numButtons");
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

    @SuppressLint("ValidFragment")
    public AlertDialogFragment(AlertDialogFragment.DialogButtonListener ndl) {
        super();
        this.ndl = ndl;
    }

    public static AlertDialogFragment newInstance(String textContent, float textSize, int color, int width, int height, int numButtons) {
        AlertDialogFragment modalDialog = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("textContent", textContent);
        args.putFloat("textSize", textSize);
        args.putInt("color", color);
        args.putInt("width", width);
        args.putInt("height", height);
        args.putInt("numButtons", numButtons);
        modalDialog.setArguments(args);

        return modalDialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        System.out.println("AlertDialogFragment.onCreateView() is called.");

        Window window = getDialog().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.0f);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);   // make dialog a modal
        setShowsDialog(true);   // added on 2018-06-24

        View view = inflater.inflate(R.layout.modal_dialogfragment, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        System.out.println("AlertDialogFragment.onViewCreated() is called.");

        modalDialogView = view;
        alertDialog = this;

        FrameLayout fLayout = (FrameLayout) view.findViewById(R.id.dialog_fragment_body_layout);
        FrameLayout.LayoutParams ll = new FrameLayout.LayoutParams(dialogWidth, dialogHeight, Gravity.CENTER);
        fLayout.setLayoutParams(ll);

        text_shown = view.findViewById(R.id.text_shown);
        text_shown.setText(textContext);
        text_shown.setTextSize(textSize);
        text_shown.setTextColor(textColor);
        button1 = view.findViewById(R.id.dialogfragment_button1);
        button1.setTextSize(textSize);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ndl.button1OnClick(alertDialog);
            }
        });
        button2 = view.findViewById(R.id.dialogfragment_button2);
        button2.setTextSize(textSize);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ndl.button2OnClick(alertDialog);
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
                text_shown.setPadding(20,20,20,20);
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

    // due to the bus of Android SDK, this override to onDestroyView() has to be done. Added on 2018-06-19 11:50pm
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
    public Button getButton1() {
        return this.button1;
    }
    public Button getButton2() {
        return this.button2;
    }

}
