package com.lessons.android.se.omeram.patients;

/**
 * Created by Zaken on 21/07/2015.
 */

import android.os.AsyncTask;
import android.content.Context;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;




    public class UsersDAL extends AsyncTask<String,Object,Integer> {
    private  UsersBL ubl;
    private  Context context;
    private String query;
    private String idForDB,DB_URL,USER,PASS ;
    private String userName;
    private int userPassword;

    public UsersDAL(UsersBL activity,Context context,int userPassword,String userName){
        this.ubl=activity;
        this.context=context;
        this.userName=userName;
        this.userPassword=userPassword;
        query = "SELECT "+DatabaseConstants.MAIN_ID+" FROM "+DatabaseConstants.USERS+
                " WHERE "+DatabaseConstants.USER_NAME+" = '" + userName + "' and " +DatabaseConstants.USER_PASSWORD+" = '" + userPassword + "'";
        idForDB=DatabaseConstants.MAIN_ID;
        DB_URL = DatabaseConstants.DB_URL;
        PASS= DatabaseConstants.PASS;
        USER=DatabaseConstants.USER;
    }
    // Check The User Name And Password To Get Key.
    protected Integer doInBackground(String... sqlQ) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Log.d("Connection","HERERERERE");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(query);
            Log.d("Exexute","HERERERERE");


            if (!rs.next())
                publishProgress(0);
            else {
                Log.d("Else","HERERERERE");

                int doctorKey = rs.getInt(idForDB);
                publishProgress(doctorKey);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        ubl.startMain(context,values);
    }




}


