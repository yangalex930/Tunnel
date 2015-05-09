package com.Tunnel.app.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.Tunnel.app.R;
import com.Tunnel.app.TunnelApplication;
import com.Tunnel.app.dialog.ProjectCreateDialog;

import java.io.BufferedReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author yupeng.yyp
 * @create 14-10-12 21:20
 */
public class QRCodeParser {

    Context context;

    public QRCodeParser(Context context)
    {
        this.context = context;
    }

    public boolean tryParseTunnelInfo(final String s)
    {
        boolean ret = false;
        if (s.startsWith(context.getString(R.string.app_name)))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.find_project)
                   .setMessage(s)
                   .setNegativeButton(R.string.create_new_project, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {
                           try {
                               BufferedReader reader = new BufferedReader(new StringReader(s));
                               String line;
                               while ((line = reader.readLine()) != null) {
                                   String[] split = line.split(":");
                                   if (split.length > 1 && split[0].equals("工程名")) {
                                       AlertDialog alertDialog = ProjectCreateDialog.create(context, split[1], null);
                                       alertDialog.show();
                                   }
                               }
                           }
                           catch (Exception e)
                           {
                               Log.e("tryParseTunnelInfo", e.getMessage());
                           }
                       }
                   })
                   .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                       @Override
                       public void onClick(DialogInterface dialogInterface, int i) {

                       }
                   }).create().show();
            ret = true;
        }
        return ret;
    }

    public boolean tryParseURL(final String s)
    {
        boolean isValidURL = false;
        try {
            URL url = new URL(s);
            isValidURL = true;
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (isValidURL) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("链接")
                    .setMessage(String.format(TunnelApplication.getInstance().getString(R.string.find_url), s))
                    .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(s));
                            context.startActivity(intent);
                        }
                    })
                    .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    })
                    .create().show();
            return true;
        }
        return false;
    }
}
