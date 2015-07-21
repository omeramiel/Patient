package com.lessons.android.se.omeram.patients;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Zaken on 21/07/2015.
 */
public class UsersBL {
    UsersDAL task;
    private SweetAlertDialog pDialog;
    private SweetAlertDialog wrongPassword;


    public void getBusinessID(Activity activity,Context context,int userPassword,String userName)
    {
        pDialog=new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setTitleText(context.getString(R.string.wait));
        pDialog.setCancelable(true);
        pDialog.show();

        task = new UsersDAL(this,context,userPassword,userName);
        task.execute();
    }

    // Gets The Key From The AsyncTask And Starting The Main Activity
    public void startMain(Context context,Object... values)
    {
        pDialog.cancel();
        // Wrong Password Or User Name, Start Alert
        if (values[0]==0) {

            wrongPasswordAlertInit(context);
            task.cancel(true);
        }
        else
        {
            // If The Password And UserName Is Correct, Start Main Activity
            Intent i=new Intent(context ,MainActivity.class);
            context.startActivity(i);
            task.cancel(true);
        }

    }

    private void wrongPasswordAlertInit(Context context)
    {
        wrongPassword=new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);
        wrongPassword.setTitleText(context.getString(R.string.connectionProblem));
        wrongPassword.setContentText(context.getString(R.string.incorrectInput));
        wrongPassword.setCancelable(true);
        wrongPassword.show();
    }
}
