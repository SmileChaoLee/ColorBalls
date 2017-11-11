package com.smile.colorballs;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.smile.dao.ScoreSQLite;
import com.smile.utility.ScreenUtl;


public class HistoryActivity extends ListActivity {

    private static final String TAG = "HistoryActivity";
    private String[] queryResult = new String[] {"","","","","","","","","",""};
    private float listViewHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // View historyView = getLayoutInflater().inflate(R.layout.activity_history,null);
        // final ListView listView = findViewById(android.R.id.list);

        Button okButton = (Button)findViewById(R.id.historyOkButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String[] itemNo = new String[] {"1 ","2 ","3 ","4 ","5 ","6 ","7 ","8 ","9 ","10"};

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            queryResult = extras.getStringArray("resultStr");
        }

        for (int i=0 ; i<queryResult.length ; i++) {
            queryResult[i] = itemNo[i] + " " + queryResult[i];
        }

        setListAdapter(new mListAdapter(queryResult));

    }


    private class mListAdapter extends BaseAdapter {

        private String text1[] ;  // or private String[] text1,text2;

        public mListAdapter() {
            this.text1 = new String[] {"No initialization"};
        }

        public mListAdapter(String[] text1) {
            this.text1 = text1;
        }

        @Override
        public int getCount() {
            return this.text1.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.history_list_items, container, false);
            }

            // int listViewHeight = getListView().getHeight();
            int listViewHeight = container.getHeight();
            int itemHeight = listViewHeight / (getCount()+1);

            TextView vText1;
            vText1 = (TextView) convertView.findViewById(R.id.text1);
            vText1.setText(this.text1[position]);
            vText1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            vText1.setHeight(itemHeight);
            // or
            // ViewGroup.LayoutParams params = convertView.getLayoutParams();
            // params.height = itemHeight; // set height for height

            // Because the list item contains multiple touch targets, you should not override
            // onListItemClick. Instead, set a click listener for each target individually.

            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
