package com.Tunnel.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.Tunnel.app.R;
import com.Tunnel.app.dialog.ConfirmDeleteDialog;
import com.Tunnel.app.dialog.ProjectCreateDialog;
import com.Tunnel.app.dialog.RenameDialog;
import com.Tunnel.app.util.BitmapUtil;
import com.Tunnel.app.util.FileUtil;
import com.Tunnel.app.util.ProjectUtil;
import com.Tunnel.app.util.TunnelUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yang Yupeng on 2014/7/28.
 */
public class ImageGridActivity extends Activity implements ActionMode.Callback {

    private GridView gridView;
    private ImageAdapter imgAdapter;
    private int level = 0;
    private String currentPath;
    private boolean needRefresh = true;
    private File[] files;
    private ActionMode actionMode;
    String destProject;
    static int dftWidth = 1;
    static int dftHight = 1;
    static DisplayMetrics dm = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_grid);

        if (dm == null)
        {
            dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            dftWidth = dm.widthPixels / 3;
            dftHight = (int)(dftWidth * 1.2);
        }

        level = getIntent().getIntExtra("Level", 0);
        currentPath = getIntent().getStringExtra("Path");
        switch (level) {
            case 1:
                setTitle(currentPath.substring(currentPath.lastIndexOf("/") + 1));
                break;
            case 2:
                setTitle(currentPath.substring(currentPath.lastIndexOf("/") + 1));
                break;
            default:
                setTitle("全部工程");
                currentPath = TunnelUtil.getTunnelFolderPath();
                break;
        }


        gridView = (GridView) findViewById(R.id.picture_grid);
        imgAdapter = new ImageAdapter(this);
        gridView.setAdapter(imgAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (actionMode == null) {
                    Intent intent;
                    if (files[position].isDirectory()) {
                        intent = new Intent(ImageGridActivity.this, ImageGridActivity.class);
                        intent.putExtra("Level", level + 1);
                        intent.putExtra("Path", files[position].getPath());
                    } else if (files[position].getName().endsWith(".jpg")) {
                        intent = new Intent(ImageGridActivity.this, ImageViewActivity.class);
                        intent.putExtra("Path", currentPath);
                        intent.putExtra("ImageIndex", position);
                    }
                    else {
                        return;
                    }
                    startActivityForResult(intent, 1);
                } else {
                    imgAdapter.toggleSelect(position);
                }
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (actionMode == null) {
                    actionMode = startActionMode(ImageGridActivity.this);
                    imgAdapter.toggleSelect(i);
                }
                return true;
            }
        });


        getWindowManager().getDefaultDisplay().getMetrics(dm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (needRefresh) {
            refresh();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imgAdapter.clearAll();
        if (folderBitmap != null && !folderBitmap.isRecycled()) {
            folderBitmap.recycle();
            folderBitmap = null;
        }
    }

    private void refresh() {
        needRefresh = false;
        imgAdapter.clearAll();
        new AsyncLoadedImage().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuItem item;
        if (level == 0) {
            item = menu.add(Menu.FIRST, 0, 0, R.string.create_new_project);
            item.setTitle(R.string.create_new_project);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    ProjectCreateDialog.create(ImageGridActivity.this, null, new ProjectCreateDialog.ProjectCreateListener() {
                        @Override
                        public void onProjectCreated(String projectName) {
                            refresh();
                        }

                        @Override
                        public void onCancel() {
                        }
                    }).show();
                    return false;
                }
            });
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                needRefresh = data.getBooleanExtra("Need_Refresh", false);
                if (needRefresh) {
                    refresh();
                }
            }
        }
    }

    private void addImage(Bitmap... loadImages) {
        for (Bitmap loadImage : loadImages) {
            imgAdapter.addPhoto(loadImage);
        }
    }

    @Override
    public boolean onCreateActionMode(final ActionMode actionMode, Menu menu) {

        MenuItem item = menu.add(Menu.FIRST, R.string.delete, 0, R.string.delete);
        item.setTitle(R.string.delete);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setIcon(R.drawable.delete);

        if (level == 0)
        {
            item = menu.add(Menu.FIRST, R.string.rename, 0, R.string.rename);
            item.setTitle(R.string.rename);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(Menu.FIRST, R.string.project_upload, 0, R.string.project_upload);
            item.setTitle(R.string.project_upload);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        if (level == 1) {
            item = menu.add(Menu.FIRST, R.string.rename, 0, R.string.rename);
            item.setTitle(R.string.rename);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(Menu.FIRST, R.string.move_to, 0, R.string.move_to);
            item.setTitle(R.string.move_to);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

            item = menu.add(Menu.FIRST, R.string.pic_upload, 0, R.string.pic_upload);
            item.setTitle(R.string.pic_upload);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        if (level == 0 || level == 1)
        {
            MenuItem item = menu.findItem(R.string.rename);
            item.setVisible(imgAdapter.getSelectedCount() == 1);
        }
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        if (menuItem.getItemId() == R.string.delete) {
            ConfirmDeleteDialog.create(ImageGridActivity.this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (imgAdapter.getSelectedCount() > 0) {
                        List<Boolean> selectedList = imgAdapter.getSelectedList();
                        for (int index = 0; index < selectedList.size(); index++) {
                            if (selectedList.get(index)) {
                                if (level == 0)
                                {
                                    ProjectUtil.deleteProject(files[index].getName());
                                    FileUtil.deleteFile(files[index]);
                                }
                                else if (level == 1)
                                {
                                    FileUtil.deleteFile(files[index]);
                                }
                                else if (level == 2)
                                {
                                    FileUtil.deleteFile(files[index]);
                                }

                            }
                        }
                        needRefresh = true;
                    }
                    actionMode.finish();
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    actionMode.finish();
                }
            }).show();
        }
        else if (menuItem.getItemId() == R.string.move_to)
        {
            String [] projects = new String[ProjectUtil.getProjects().size() - 1];
            for (int i = 0; i < projects.length; i++)
            {
                projects[i] = ProjectUtil.getProjects().get(i);
            }
            destProject = ProjectUtil.getDefaultProject();
            AlertDialog.Builder builder = new AlertDialog.Builder(ImageGridActivity.this);
            builder.setTitle(R.string.choose_project)
                    .setSingleChoiceItems(projects, ProjectUtil.getDefaultIndex(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            destProject = ProjectUtil.getProjects().get(i);
                        }
                    })
                    .setNegativeButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (imgAdapter.getSelectedCount() > 0)
                            {
                                List<Boolean> selectedList = imgAdapter.getSelectedList();
                                for (int index = 0; index < selectedList.size(); index++) {
                                    if (selectedList.get(index)) {
                                        StringBuilder sb = new StringBuilder(TunnelUtil.getTunnelFolderPath());
                                        sb.append(destProject).append("/").append(files[index].getName());
                                        FileUtil.moveDirectory(files[index].getPath(), sb.toString());
                                    }
                                }
                                needRefresh = true;
                            }
                            actionMode.finish();
                        }
                    })
                    .setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            actionMode.finish();
                        }
                    }).create().show();
        }
        else if (menuItem.getItemId() == R.string.pic_upload || menuItem.getItemId() == R.string.pic_upload)
        {
            new AlertDialog.Builder(ImageGridActivity.this)
                    .setMessage(getResources().getString(R.string.feature_not_avalible))
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    actionMode.finish();
                                }
                            })
                    .create().show();
        }
        else if (menuItem.getItemId() == R.string.rename)
        {
            final int selectIndex = imgAdapter.getFirstSelectIndex();
            RenameDialog.create(ImageGridActivity.this, files[selectIndex].getName(), new RenameDialog.RenameListener() {
                @Override
                public void onRenamed(String newName) {
                    String srcFolder = files[selectIndex].getAbsolutePath();
                    String newFolder = srcFolder.substring(0, srcFolder.lastIndexOf("/")) + "/" + newName;
                    FileUtil.moveDirectory(srcFolder, newFolder);
                    if (level == 0)
                    {
                        ProjectUtil.renameProject(files[selectIndex].getName(), newName);
                    }
                    needRefresh = true;
                    actionMode.finish();
                }
                @Override
                public void onCancel() {
                    actionMode.finish();
                }
            }).show();
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        imgAdapter.clearSelection();
        if (folderBitmap != null && !folderBitmap.isRecycled()) {
            folderBitmap.recycle();
            folderBitmap = null;
        }
        if (needRefresh) {
            refresh();
        }
    }

    Bitmap folderBitmap = null;
    class AsyncLoadedImage extends AsyncTask<Object, Bitmap, Boolean> {
        @Override
        protected Boolean doInBackground(Object... params) {
            boolean result = false;
            files = FileUtil.getFileList(currentPath);
            if (files != null) {
                for (File file : files) {
                    long t = System.currentTimeMillis();
                    Bitmap bitmap = null;
                    try {
                        if (file.isDirectory()) {
                            if (folderBitmap == null || folderBitmap.isRecycled()) {
                                folderBitmap = BitmapUtil.getImageThumbnail(R.drawable.file_folder, dftWidth, dftHight);
                            }
                            bitmap = folderBitmap;
                        } else if (file.getName().endsWith(".jpg")){
                            bitmap = BitmapUtil.getImageThumbnail(file.getPath(), dftWidth, dftHight);
                        }
                        else
                        {
                            bitmap = BitmapUtil.getImageThumbnail(R.drawable.no_recognize, dftWidth, dftHight);
                        }
                        if (bitmap != null) {
                            publishProgress(bitmap);
                            result = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("TimeProfile", "Load cost: " + (System.currentTimeMillis() - t));
                }
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        public void onProgressUpdate(Bitmap... value) {
            addImage(value);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            //invalidateOptionsMenu();
        }
    }

    class ImageAdapter extends BaseAdapter {
        private List<Bitmap> picList = new ArrayList<Bitmap>();
        private List<Boolean> selectedList = new ArrayList<Boolean>();

        private Context mContext;

        public ImageAdapter(Context mContext) {
            this.mContext = mContext;
        }

        public void toggleSelect(int index) {
            if (index >= 0 && index < selectedList.size()) {
                selectedList.set(index, !selectedList.get(index));
                this.notifyDataSetChanged();
            }
        }

        public void clearSelection() {
            for (int i = 0; i < selectedList.size(); i++) {
                selectedList.set(i, false);
            }
            notifyDataSetChanged();
        }

        public void clearAll() {
            for (Bitmap bitmap : picList) {
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            }
            picList.clear();
            selectedList.clear();
            notifyDataSetChanged();
        }

        public int getSelectedCount() {
            int count = 0;
            for (int i = 0; i < selectedList.size(); i++) {
                if (selectedList.get(i)) {
                    count++;
                }
            }
            return count;
        }

        public int getFirstSelectIndex()
        {
            for (int i = 0; i < selectedList.size(); i++) {
                if (selectedList.get(i)) {
                    return i;
                }
            }
            return -1;
        }

        public List<Boolean> getSelectedList()
        {
            return selectedList;
        }

        public void deleteSelectedItem() {
            int size = selectedList.size();
            for (int i = size - 1; i >= 0; i--) {
                if (selectedList.get(i)) {
                    FileUtil.deleteFile(files[i]);
                    Bitmap bitmap = picList.get(i);
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                    picList.remove(i);
                    selectedList.remove(i);
                }
            }
        }

        @Override
        public int getCount() {
            return picList.size();
        }

        @Override
        public Object getItem(int position) {
            return picList.get(position);
        }

        public void addPhoto(Bitmap loadImage) {
            picList.add(loadImage);
            selectedList.add(false);
            notifyDataSetChanged();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(ImageGridActivity.this);
                view = inflater.inflate(R.layout.imageview_with_name, null);
                view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            } else {
                view = convertView;
            }
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
            imageView.setImageBitmap(picList.get(position));
            if (selectedList.get(position)) {
                imageView.setBackgroundColor(Color.parseColor("#ff00bfff"));
            } else {
                imageView.setBackgroundColor(Color.TRANSPARENT);
            }
            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(files[position].getName());
            return view;
        }
    }
}
