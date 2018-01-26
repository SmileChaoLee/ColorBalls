package com.smile.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lee on 10/8/2014.
 */
public class ScoreSQLite extends SQLiteOpenHelper {

    private Context myContext = null;
    private static final String playerName = new String("playerName");
    private static final String playerScore = new String("playerScore");
    private static final String[] columns = {"playerName", "playerScore"};

    private static final String dbName = new String("colorBallDatabase.db");
    private static final String tableName = new String("score");
    private static final String createTable = "create table if not exists " + tableName + " ("
            + playerName + " text not null ,  " + playerScore + " integer );";
    private static final String upDateTable = new String("update");
    // private SQLiteDatabase scoreDatabase = null;
    private static final int dbVersion = 1;

    public ScoreSQLite(Context context) {
        super(context,dbName,null,dbVersion);
        myContext = context;
        // scoreDatabase = null;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // MyActivity act = new MyActivity();
        // act.getApplicationContext().getDatabasePath(dbName);
        database.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database , int oldVersion , int newVersion) {
    }

    public void openScoreDatabase() {

        try {
            SQLiteDatabase database = getWritableDatabase();
            if (database != null) {
                database.execSQL(createTable);
                String sql = "select count(*) as totalRec from " + tableName + ";";
                Cursor cur = database.rawQuery(sql, new String[]{});
                if (cur.moveToFirst()) {
                    if (cur.getInt(0) == 0) {

                        // insert one new record for starting
                        // sql = "insert into " + tableName + " ( playerName , playerScore) values ( 'ChaoLee',100);";
                        // scoreDatabase.execSQL(sql);

                        // insert one new record for starting
                        ContentValues values = new ContentValues();
                        values.put(playerName,"ChaoLee");
                        values.put(playerScore,100);
                        database.insert(tableName, null, values);
                    }
                }
                cur.close();
                database.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int readHighestScore() {
        int highestScore = 0;

        try {
            SQLiteDatabase database = getReadableDatabase();
            if (database != null) {
                /*
                // use rawQuery
                String sql = "select playerScore from " + tableName + " order by playerScore desc";
                Cursor cur = database.rawQuery(sql, new String[]{});
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        highestScore = cur.getInt(0);
                    }
                    cur.close();
                }
                */

                // use query
                Cursor cur = database.query(tableName, columns, null, new String[]{}, null, null, playerScore + " desc");
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        highestScore = cur.getInt(1);   // for playerScore
                    }
                    cur.close();
                }

                database.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return highestScore;
    }

    public String[] read10HighestScore() {

        String[] resultStr  = new String[] {"","","","","","","","","",""};
        String temp = new String("");
        String space = new String(new char[1]).replace("\0"," ");
        int strLen = 14;

        SQLiteDatabase database = getReadableDatabase();
        if (database != null) {
            try {
                String sql = "select playerName,playerScore from " + tableName + " order by playerScore desc";
                Cursor cur = database.rawQuery(sql, new String[]{});
                int i = 0;
                while (cur.moveToNext() && (i < 10)) {
                    temp = cur.getString(0);
                    temp = temp.substring(0, Math.min(temp.length(), strLen)).trim();
                    temp = temp + (new String(new char[strLen - temp.length()]).replace("\0", " "));
                    resultStr[i] = temp + space + String.valueOf(cur.getInt(1));
                    i++;
                }
                cur.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            database.close();
        }

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

                SQLiteDatabase database = getWritableDatabase();
                if (database != null) {
                    try {
                        String sql = "select count(*) as totalRec from " + tableName + ";";
                        Cursor cur = database.rawQuery(sql, new String[]{});
                        if (cur.moveToFirst()) {
                            if (cur.getInt(0) >= 100) {
                                //   Over 100 records,   delete one record
                                sql = "delete from " + tableName + " where playerScore in ( select playerScore from " + tableName + " order by playerScore limit 1);";
                                database.execSQL(sql);
                            }
                        }
                        cur.close();
                        //  insert one record into table    SCORE
                        // sql = "insert into " + tableName + " ( playerName , playerScore) values ("
                        //         + "'" + name + "'," + String.valueOf(score) + ");";
                        // database.execSQL(sql);

                        //  insert one record into table    SCORE
                        ContentValues values = new ContentValues();
                        values.put(playerName, name);
                        values.put(playerScore,score);
                        database.insert(tableName, null, values);

                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }

                    database.close();
                }
            }
        };

        thread.start();
    }
}
