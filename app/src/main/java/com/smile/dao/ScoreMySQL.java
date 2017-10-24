package com.smile.dao;

import android.content.Context;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Created by lee on 01/05/2015.
 */
public class ScoreMySQL {

    private Context myContext = null;
    private boolean registerYN = false;

    private Connection con = null; //Database objects
    private String url = new String("jdbc:mysql://smsong.c56yuu3xozbt.us-east-1.rds.amazonaws.com:3306/");

    final private String dbName = new String("smsong");
    final private String userName = new String("chaolee");
    final private String passWord = new String("86637971");

    final private String tableName = new String("score");
    final private String playerName = new String("playerName");
    final private String playerScore = new String("playerScore");

    final private String createTable = "create table if not exists " + tableName + " ("
            + playerName + " text not null ,  " + playerScore + " integer );";

    public ScoreMySQL(Context context) {
        this.myContext = context;
        registerJDBCDriver();
        // createColorBallTable();
    }

    private void registerJDBCDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            registerYN = true;
            System.out.println("Succeeded to register com.mysql.Driver !!");
        }
        catch (Exception e) {
            registerYN = false;
            System.out.println("Failed to register com.mysql.Driver !!");
        }
    }

    private boolean createColorBallTable() {

        boolean result = false;

        if (registerYN) {
            con = connectToJDBC();
            if (con != null) {
                String createColorBall = "CREATE TABLE IF NOT EXISTS "+tableName + " (" +
                        "    playerName     VARCHAR(30)  "     +
                        "  , playerScore    INT(7)       )" ;
                Statement stat = null;
                try {
                    DatabaseMetaData dbm = (DatabaseMetaData) con.getMetaData();
                    // check if "score" table is there
                    ResultSet tables = dbm.getTables(null, null, "score", null);
                    if (!tables.next()) {
                        System.out.println("table not exists");
                        result = true;
                        stat = con.createStatement();
                        stat.executeUpdate(createColorBall);
                        stat.close();
                    }
                } catch (SQLException e) {
                    result = false;
                    System.out.println("CreateColorBallTable Exception :" + e.toString());
                }
            }
            closeJDBCConnection();
        }
        return(result);
    }

    public boolean getStatusRegister() {
        return this.registerYN;
    }

    public String[] read10HighestScore() {

        String[] resultStr  = new String[] {"","","","","","","","","",""};

        connectToJDBC();
        if (con == null) {
            resultStr[0] = " Failed to connect to database!!";
            return resultStr;
        }

        String temp = new String("");
        String space = new String(new char[1]).replace("\0"," ");
        int strLen = 18;

        String sql = "select playerName,playerScore from " + tableName + " order by playerScore desc";

        try {
            Statement stat = con.createStatement();
            ResultSet rs = stat.executeQuery(sql);

            int i=0;
            while (rs.next() && (i<10) ) {
                temp = rs.getString(playerName);
                temp = temp.substring(0,Math.min(temp.length(),strLen)).trim();
                temp = temp + (new String(new char[strLen-temp.length()]).replace("\0"," "));
                resultStr[i] = temp+space+String.valueOf(rs.getInt(playerScore));
                i++;
            }

            rs.close();
            stat.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQLException -> Failed to query for high score from MySQL server !!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception - > Failed to query for high score from MySQL server !!");
        }

        closeJDBCConnection();

        return resultStr;
    }


    public void addHighestScore(String name , int score) {

        connectToJDBC();
        if (con == null) {
            return;
        }

        Statement stat = null;
        ResultSet rs = null;
        PreparedStatement pst = null;

        String sql = "select count(*) as totalRec from " +tableName +";" ;

        try {
            stat = con.createStatement();
            rs = stat.executeQuery(sql);
            if (rs.next()) {
                if (rs.getInt("totalREc")>=1000) {
                    //   Over 1000 records,   delete one record
                    rs.close();
                    rs = null;
                    sql = "delete from "+tableName+" where playerScore in ( select playerScore from "+tableName+" order by playerScore limit 1);" ;
                    pst = con.prepareStatement(sql);
                    pst.executeUpdate();
                    pst.close();
                    pst = null;
                }
            }
            stat.close();
            stat = null;
        } catch(SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to deleted one record !!");
        }

        //  insert one record into table    SCORE
        sql = "insert into " + tableName + " ( playerName , playerScore) values ("
                +"'"+name+"',"+String.valueOf(score)+");" ;
        try {
            stat = con.createStatement();
            stat.executeUpdate(sql);
            System.out.println("Succeeded to insert on record to database !!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to insert one record to database !!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception ---> insert one record !!");
        }

        closeJDBCConnection();
    }

    public Connection connectToJDBC() {

        if ( (con == null) && registerYN) {

            try {
                con = DriverManager.getConnection(url + dbName+"?useUnicode=true&characterEncoding=utf-8",
                        "chaolee", "86637971");
            } catch (SQLException e ) {
                con = null;
                e.printStackTrace();
                System.out.println("SQLException -> Failed to connect to database !!");
            } catch (Exception e) {
                con = null;
                e.printStackTrace();
                System.out.println("Exception -> Failed to connect to database !!");
            }
        }

        return this.con;
    }

    public void closeJDBCConnection() {
        if (con != null) {
            try {
                con.close();
                con = null;
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    public void databaseCreate() {

    }

}
