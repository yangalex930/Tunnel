package com.Tunnel.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.Tunnel.app.R;

/**
 * @author yupeng.yyp
 * @create 14-8-31 20:07
 */
public class RenameDialog {

    public interface RenameListener
    {
        void onRenamed(String newName);
        void onCancel();
    }

    public static AlertDialog create(Context context, String oldName, final RenameListener listener)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.rename, null);
        final EditText editText = (EditText)view.findViewById(R.id.edit);
        editText.setText(oldName);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        return builder.setTitle(R.string.rename)
               .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                        if (listener != null)
                        {
                            listener.onRenamed(editText.getText().toString());
                        }
                   }
               })
               .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                        if (listener != null)
                        {
                            listener.onCancel();
                        }
                   }
               })
               .setView(view)
               .create();
    }
}
