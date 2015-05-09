package com.Tunnel.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.Tunnel.app.R;

/**
 * @author yupeng.yyp
 * @create 14-8-31 14:03
 */
public class ConfirmDeleteDialog {
    public static AlertDialog create(Context context, DialogInterface.OnClickListener confirmListener, DialogInterface.OnClickListener cancelListener)
    {
        return new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.confirm_delete))
                .setNegativeButton(context.getString(R.string.ok), confirmListener == null ?
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                } : confirmListener)
                .setPositiveButton(context.getString(R.string.cancel), cancelListener == null ?
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        } : cancelListener)
                .create();
    }
}
