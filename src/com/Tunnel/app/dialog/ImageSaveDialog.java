package com.Tunnel.app.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import com.Tunnel.app.R;
import com.Tunnel.app.activity.ImageCropActivity;
import com.Tunnel.app.util.BitmapUtil;
import com.Tunnel.app.util.TunnelUtil;
import com.Tunnel.app.util.ProjectUtil;

import java.util.List;

/**
 * @author yupeng.yyp
 * @create 14-8-24 16:25
 */
public class ImageSaveDialog {

    private static boolean restartPreview;

    public static AlertDialog create(final Context context, final Bitmap bitmap, final Camera camera)
    {
        restartPreview = true;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.image_save, null);
        final EditText editText = (EditText)view.findViewById(R.id.edit);
        editText.setText(TunnelUtil.getDefaultImageName());
        editText.selectAll();
        editText.requestFocus();
        final Spinner spinner = (Spinner)view.findViewById(R.id.spinner);
        final List<String> projects = ProjectUtil.getProjects();
        spinner.setAdapter(new ArrayAdapter<String>(context, R.layout.spinner_dropdown_item, projects));
        spinner.setSelection(ProjectUtil.getDefaultIndex());
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (projects.get(i).equals(context.getString(R.string.create_new_project)))
                {
                    ProjectCreateDialog.create(context, null, new ProjectCreateDialog.ProjectCreateListener() {
                        @Override
                        public void onProjectCreated(String projectName) {
                            spinner.setSelection(ProjectUtil.getDefaultIndex());
                            ((ArrayAdapter) spinner.getAdapter()).notifyDataSetChanged();
                        }

                        @Override
                        public void onCancel() {
                            spinner.setSelection(ProjectUtil.getDefaultIndex());
                        }
                    }).show();
                }
                else
                {
                    ProjectUtil.selectProject(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.setTitle(R.string.save_image)
                .setView(view)
                .setNegativeButton(R.string.begin_check, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        restartPreview = false;
                        String imagePath = BitmapUtil.saveBitmapByName(bitmap, editText.getText().toString());
                        bitmap.recycle();
                        Intent intent = new Intent(context, ImageCropActivity.class);
                        intent.putExtra("ImagePath", imagePath);
                        context.startActivity(intent);
                    }
                })
                .setNeutralButton(R.string.continue_capture, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BitmapUtil.saveBitmapByName(bitmap, editText.getText().toString());
                        bitmap.recycle();
                    }
                })
                .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bitmap.recycle();
                    }
                })
                .create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (restartPreview) {
                    camera.startPreview();
                }
            }
        });
        return dialog;
    }
}
