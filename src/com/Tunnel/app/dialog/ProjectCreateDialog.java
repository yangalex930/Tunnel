package com.Tunnel.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import com.Tunnel.app.R;
import com.Tunnel.app.util.ProjectUtil;

import java.util.List;

/**
 * @author yupeng.yyp
 * @create 14-8-24 20:16
 */
public class ProjectCreateDialog {

    public interface ProjectCreateListener
    {
        void onProjectCreated(String projectName);
        void onCancel();
    }
    public static AlertDialog create(Context context, String projectName, final ProjectCreateListener listener)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view1 = layoutInflater.inflate(R.layout.project_create, null);
        final EditText editText1 = (EditText)view1.findViewById(R.id.edit);
        if (projectName == null || projectName.isEmpty()) {
            editText1.setText(ProjectUtil.genDefaultName());
        }
        else {
            editText1.setText(projectName);
        }
        editText1.selectAll();
        editText1.requestFocus();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        return  builder.setTitle(R.string.create_new_project)
                .setView(view1)
                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String projectName = editText1.getText().toString();
                        ProjectUtil.addProject(projectName);
                        if (listener != null) {
                            listener.onProjectCreated(projectName);
                        }
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (listener != null) {
                            listener.onCancel();
                        }
                    }
                }).create();
    }
}
