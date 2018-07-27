package com.smile.scoresqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lee on 10/8/2014.
 */
public class ScoreSQLite extends SQLiteOpenHelper {

    private Context myContext = null;
    private static final String playerName = new String("playerName");
    private static final String playerScore = new String("playerScore");

    private static final String dbName = new String("colorBallDatabase.db");
    private static final String tableName = new String("score");
    private static final String createTable = "create table if not exists " + tableName + " ("
            + playerName + " text not null ,  " + playerScore + " integer );";
    private static final String upDateTable = new String("update");
    private SQLiteDatabase scoreDatabase = null;
    private static final int dbVersion = 1;

    private boolean readFinished = true;

    public ScoreSQLite(Context context) {
        super(context, dbName,null,dbVersion);
        myContext = context;
        scoreDatabase = null;
        readFinished = true;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database , int oldVersion , int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(database);
    }

    public void openScoreDatabase() {

        try {
            scoreDatabase = getWritableDatabase();
            if (scoreDatabase != null) {
                scoreDatabase.execSQL(createTable);
                String sql = "select count(*) as totalRec from " + tableName + ";";
                Cursor cur = scoreDatabase.rawQuery(sql, new String[]{});
                if (cur.moveToFirst()) {
                    if (cur.getInt(0) == 0) {
                        sql = "insert into " + tableName + " ( playerName , playerScore) values ( 'ChaoLee',100);";
                        scoreDatabase.execSQL(sql);
                    }
                }
                cur.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int readHighestScore() {
        int highestScore = 0;

        while (!readFinished) {
            System.out.println("readFinished = " + readFinished);
        }    // wait for other operations finish
        readFinished = false;

        openScoreDatabase();
        if (scoreDatabase != null) {
            try {
                String sql = "select playerScore from " + tableName + " order by playerScore desc";
                Cursor cur = scoreDatabase.rawQuery(sql, new String[]{});
                if (cur.moveToFirst()) {
                    highestScore = cur.getInt(0);
                }
                cur.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            closeScoreDatabase();
        }

        readFinished = true;

        return highestScore;
    }

    public String[] read10HighestScore() {

        String[] resultStr  = new String[] {"","","","","","","","","",""};
        String space = new String(new char[1]).replace("\0"," ");
        int strLen = 14;

        while (!readFinished) {}    // wait for other operations finish
        readFinished = false;

        openScoreDatabase();
        if (scoreDatabase != null) {
            try {
                String sql = "select playerName,playerScore from " + tableName + " order by playerScore desc";
                Cursor cur = scoreDatabase.rawQuery(sql, new String[]{});
                int i = 0;
                String temp = new String("");
                while (cur.moveToNext() && (i < 10)) {
                    temp = cur.getString(0);
                    temp = temp.substring(0, Math.min(temp.length(), strLen)).trim();
                    temp = temp + (new String(new char[strLen - temp.length()]).replace("\0", " "));
                    resultStr[i] = temp + space + String.valueOf(cur.getInt(1));
                    i++;
                }
                cur.close();
                temp = null;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            closeScoreDatabase();
        }

        readFinished = true;

        return resultStr;
    }

    public ArrayList<Pair<String, Integer>> readTop10ScoreList() {

        ArrayList<Pair<String, Integer>> result = new ArrayList<>();

        while (!readFinished) {}    // wait for other operations finish
        readFinished = false;

        openScoreDatabase();
        if (scoreDatabase != null) {
            String temp = new String("");
            int score = 0;
            int i = 0;
            Pair<String, Integer> pair;

            Cursor cur = scoreDatabase.query(tableName, new String[] {"playerName","playerScore"}, null, null, null, null, "playerScore desc");
            if (cur.moveToFirst()) {
                do {
                    temp = cur.getString(0);
                    score = cur.getInt(1);
                    pair = new Pair<>(temp, score);
                    result.add(pair);
                    temp = null;
                    i++;
                } while (cur.moveToNext() && (i<10));
            }

            closeScoreDatabase();
        }

        readFinished = true;

        return result;
    }

    public ArrayList<String> readAllScores() {

        ArrayList<String> resultStr  = new ArrayList<>();
        String space = new String(new char[1]).replace("\0"," ");
        int strLen = 14;    // maximum chars for player name

        while (!readFinished) { }   // waits for other operations finishes
        readFinished = false;

        openScoreDatabase();
        if (scoreDatabase != null) {
            // succeeded to open
            try {
                String sql = "SELECT playerName, playerScore FROM " + tableName;    // no order
                Cursor cur = scoreDatabase.query(tableName, new String[] {"playerName", "playerScore"},null,null,null,null,null);
                if (cur.moveToFirst()) {
                    // has data
                    String temp = "";
                    do {
                        temp = cur.getString(0);
                        temp = temp.substring(0, Math.min(temp.length(), strLen)).trim();
                        temp = temp + (new String(new char[strLen - temp.length()]).replace("\0", " "));
                        resultStr.add(temp + space + String.valueOf(cur.getInt(1)));
                    } while (cur.moveToNext());

                    temp = null;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        readFinished = true;

        return resultStr;
    }

    public void addScore(final String name , final int score) {
        /*
        String sql = "update " + tableName +" set playerName="+"'"+name+"'"
                + ","+"playerScore="+String.valueOf(score)
                + " where playerName='ChaoLee'";
        */
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (!readFinished) {}    // wait for other operations finish
                readFinished = false;

                openScoreDatabase();
                if (scoreDatabase != null) {
                    try {
                        String sql = "select count(*) as totalRec from " + tableName + ";";
                        Cursor cur = scoreDatabase.rawQuery(sql, new String[]{});
                        if (cur.moveToFirst()) {
                            if (cur.getInt(0) >= 100) {
                                //   Over 100 records,   delete one record
                                sql = "delete from " + tableName + " where playerScore in ( select playerScore from " + tableName + " order by playerScore limit 1);";
                                scoreDatabase.execSQL(sql);
                            }
                        }
                        cur.close();
                        //  insert one record into table    SCORE
                        System.out.println("addScore(final String name , final int score) --> score " + score);
                        sql = "insert into " + tableName + " ( playerName , playerScore) values ("
                                + "'" + name + "'," + String.valueOf(score) + ");";
                        scoreDatabase.execSQL(sql);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    closeScoreDatabase();
                }

                readFinished = true;
            }
        };

        thread.start();
    }

    public void closeScoreDatabase() {
        if (scoreDatabase != null) {
            try {
                scoreDatabase.close();
                scoreDatabase = null;
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}
