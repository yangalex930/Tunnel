package com.Tunnel.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.Tunnel.app.dialog.ProjectCreateDialog;
import com.Tunnel.app.util.*;
import com.Tunnel.app.R;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private final static int SCANNIN_GREQUEST_CODE = 1;
    private List<Module> moduleList = new ArrayList<Module>();
    private QRCodeParser qrCodeParser = new QRCodeParser(this);

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setTitle(R.string.app_name);

        TunnelUtil.prepareTunnelFolder();
        ProjectUtil.getProjects();

        final SharedPreferences sharedPreferences = getSharedPreferences("Pref", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("NeedCorrection", true))
        {
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("NeedCorrection", false);
            edit.commit();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle(R.string.notification)
                           .setMessage(R.string.need_correct)
                           .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   GlobalSwitch.bCorrectionMode = true;
                                   Intent intent = new Intent(MainActivity.this, ImageCaptureActivity.class);
                                   startActivity(intent);
                               }
                           })
                           .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                               }
                           })
                           .create().show();
                }
            }, 500);
        }
        else {
            Orientation.restoreCorrection();
        }

        initModules();

        GridView gridView = (GridView)findViewById(R.id.gridView);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Module module = moduleList.get(position);
                if (module.intendClass != null)
                {
                    Intent intent = new Intent(MainActivity.this, module.intendClass);
                    if (module.resultCode >= 0) {
                        startActivityForResult(intent, module.resultCode);
                    } else {
                        startActivity(intent);
                    }

                    return;
                }

                if (module.moduleClickListener != null)
                {
                    module.moduleClickListener.onClickModule();

                    return;
                }

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(getString(R.string.feature_not_avalible))
                        .setPositiveButton(getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                        .create().show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item = menu.add(Menu.FIRST, 0, 0, R.string.help);
        item.setTitle(R.string.help);
        item.setIcon(R.drawable.help2);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TunnelUserManual.pdf");
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    if (!file.exists()) {
                        inputStream = getAssets().open("user_manual.pdf");
                        outputStream = new FileOutputStream(file);
                        byte[] buffer = new byte[1024];
                        while (inputStream.read(buffer) != -1) {
                            outputStream.write(buffer);
                        }
                        outputStream.flush();
                    }
                }
                catch (Exception e)
                {
                    Log.e("Tunnel.GuideActivity", e.getMessage());
                }
                finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_HISTORY);
                Uri uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "application/pdf");
                startActivity(intent);
                return false;
            }
        });
        return true;
    }

    int correctionModeSelectIndex = -1;
    private void initModules()
    {
        moduleList.add(Module.makeModule(R.string.capture, R.drawable.camera, ImageCaptureActivity.class));
        Bundle bundle = new Bundle();
        bundle.putInt("Level", 0);
        moduleList.add(Module.makeModule(R.string.view_project, R.drawable.album, ImageGridActivity.class));
        moduleList.add(Module.makeModule(R.string.download, R.drawable.download, (Class)null));
        moduleList.add(Module.makeModule(R.string.scan_qr_code, R.drawable.qr_code_scan, MipcaActivityCapture.class, SCANNIN_GREQUEST_CODE));
        moduleList.add(Module.makeModule(R.string.correction, R.drawable.correction, new Module.ModuleClickListener() {
            @Override
            public void onClickModule() {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.correction)
                        .setSingleChoiceItems(new String[] {getString(R.string.begin_correction),
                                        getString(R.string.clear_correction_result)}, -1,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        correctionModeSelectIndex = i;
                                    }
                                })
                        .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (correctionModeSelectIndex == 0)
                                {
                                    GlobalSwitch.bCorrectionMode = true;
                                    Intent intent = new Intent(MainActivity.this, ImageCaptureActivity.class);
                                    startActivity(intent);
                                }
                                else if (correctionModeSelectIndex == 1)
                                {
                                    Orientation.clearCorrection();
                                    Toast.makeText(MainActivity.this, R.string.clear_correction_finish, Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .create().show();
            }
        }));

    }

    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return moduleList.size();
        }

        @Override
        public Object getItem(int position) {
            return moduleList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                view = inflater.inflate(R.layout.module, null);
            }
            Module module = (Module)getItem(position);
            TextView textView = (TextView)view.findViewById(R.id.textView);
            textView.setText(module.titleRes);
            ImageView imageView = (ImageView)view.findViewById(R.id.imageView);
            imageView.setImageResource(module.iconRes);
            return view;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if(resultCode == RESULT_OK){
                    Bundle bundle = data.getExtras();
                    String result = bundle.getString("result");
                    if (qrCodeParser.tryParseTunnelInfo(result))
                    {
                        return;
                    }
                    if (qrCodeParser.tryParseURL(result))
                    {
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.scan_result)
                            .setMessage(result)
                            .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create().show();
                }
                break;
        }
    }
}
