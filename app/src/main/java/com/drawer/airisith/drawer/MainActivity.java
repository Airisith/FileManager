package com.drawer.airisith.drawer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MainActivity extends Activity {

    private SlidingDrawer mDrawer;
    private ImageView handle;
    private GridView mGrid;
    private ListView mList;
    private final static String IMG_NAME = "itemImg";
    private final static String TXT_NAME = "itemTxt";
    private final static int VIEW_GRID = -1;
    private final static int VIEW_LIST = 1;
    private List<File> currentList = null;
    private File currentDirectory = null;
    private boolean exit = false;
    private int viewSelected = VIEW_GRID; // -1为grid，1为list
    private ViewSp viewSp;
    private EditText createDirTxt;
    private final String ROOT = "/";
    private final String DEATLPATH = "/sdcard/";
    private File copiedFile = null;
    private File movedFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBackground(new File("/sdcard/dat/bg.dat"));


        mGrid = (GridView) findViewById(R.id.gridView);
        mList = (ListView) findViewById(R.id.listView);
        // 视图
        viewSp = new ViewSp(getApplicationContext());
        viewSelected = viewSp.get();
        if (VIEW_GRID != viewSelected && VIEW_LIST != viewSelected) {
            viewSelected = VIEW_GRID;
        }

        if (VIEW_GRID == viewSelected) {
            if (View.VISIBLE == mList.getVisibility()) {
                mList.setVisibility(View.GONE);
            }
            if (View.VISIBLE != mGrid.getVisibility()) {
                mGrid.setVisibility(View.VISIBLE);
            }
            List<File> fileList = openFile(new File(DEATLPATH), viewSelected);
        } else if (VIEW_LIST == viewSelected) {
            if (View.VISIBLE == mGrid.getVisibility()) {
                mGrid.setVisibility(View.GONE);
            }
            if (View.VISIBLE != mList.getVisibility()) {
                mList.setVisibility(View.VISIBLE);
            }
            List<File> fileList = openFile(new File(DEATLPATH), viewSelected);
        }
        mGrid.setOnItemClickListener(onItemClickListener);
        mGrid.setOnItemLongClickListener(onItemLongClickListener);
        mList.setOnItemClickListener(onItemClickListener);
        mList.setOnItemLongClickListener(onItemLongClickListener);

        //剪切板
        if (android.os.Build.VERSION.SDK_INT > 11) {
            android.content.ClipboardManager c = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        } else {
            android.text.ClipboardManager c = (android.text.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        exit = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_setting1: // 切换视图
                viewSelected = -viewSelected;
                viewSp.save(viewSelected);
                if (VIEW_GRID == viewSelected) {
                    if (View.VISIBLE == mList.getVisibility()) {
                        mList.setVisibility(View.GONE);
                    }
                    if (View.VISIBLE != mGrid.getVisibility()) {
                        mGrid.setVisibility(View.VISIBLE);
                    }
                } else if (VIEW_LIST == viewSelected) {
                    if (View.VISIBLE == mGrid.getVisibility()) {
                        mGrid.setVisibility(View.GONE);
                    }
                    if (View.VISIBLE != mList.getVisibility()) {
                        mList.setVisibility(View.VISIBLE);
                    }
                }
                openFile(currentDirectory, viewSelected);
                break;
            case R.id.action_setting2: // 新建文件夹
                createDirTxt = new EditText(this);
                createDirTxt.setText("新建文件夹");
                createDirTxt.selectAll();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(createDirTxt);
                builder.setNegativeButton("确定", creatDirListener);
                builder.setPositiveButton("取消", null);
                builder.show();
                break;
            case R.id.action_setting3: //删除目录
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setTitle("确定删除当前文件夹及文件夹下所有文件？");
                builder1.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (FileManager.deleteFolder(currentDirectory)) {
                            openFile(currentDirectory.getParentFile(), viewSelected);
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("删除成功")
                                    .setNegativeButton("确认", null)
                                    .show();
                        } else {
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("删除失败")
                                    .setNegativeButton("确认", null)
                                    .show();
                        }
                    }
                });
                builder1.setPositiveButton("取消", null);
                builder1.show();
                break;
            case R.id.action_setting4: // 粘贴文件
                if ((null != copiedFile) && (null == movedFile)){ // 复制
                    final File target = new File(currentDirectory.getAbsolutePath() +"/"+ copiedFile.getName());
                    if (target.exists()){ // 重复
                        new AlertDialog.Builder(MainActivity.this).setTitle("文件名重复，是否覆盖")
                                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (FileManager.copyFile(copiedFile, target)) {
                                            openFile(currentDirectory, viewSelected);
                                        } else {
                                            new AlertDialog.Builder(MainActivity.this).setTitle("粘贴失败")
                                                    .setNegativeButton("确定", null)
                                                    .show();
                                        }
                                    }
                                })
                                .setPositiveButton("否", null)
                                .show();
                    } else { // 不重复
                        if (FileManager.copyFile(copiedFile, target)) {
                            openFile(currentDirectory, viewSelected);
                        } else {
                            new AlertDialog.Builder(MainActivity.this).setTitle("粘贴失败")
                                    .setNegativeButton("确定", null)
                                    .show();
                        }
                    }
                }else if ((null == copiedFile) && (null != movedFile)) { // 移动
                    final String targetPath = currentDirectory.getAbsolutePath();
                    if (new File(targetPath+"/"+movedFile.getName()).exists()){ // 重复
                        new AlertDialog.Builder(MainActivity.this).setTitle("文件名重复，是否覆盖")
                                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (FileManager.moveFile(movedFile, targetPath)) {
                                            openFile(currentDirectory, viewSelected);
                                        } else {
                                            new AlertDialog.Builder(MainActivity.this).setTitle("粘贴失败")
                                                    .setNegativeButton("确定", null)
                                                    .show();
                                        }
                                    }
                                })
                                .setPositiveButton("否", null)
                                .show();
                    } else { // 不重复
                        if (FileManager.moveFile(movedFile, targetPath)) {
                            openFile(currentDirectory, viewSelected);
                        } else {
                            new AlertDialog.Builder(MainActivity.this).setTitle("粘贴失败")
                                    .setNegativeButton("确定", null)
                                    .show();
                        }
                    }
                } else {
                    if ((null==copiedFile) && (null==movedFile))
                    System.out.println("无文件");
                }
                break;
            case R.id.action_setting5:
                openFile(new File(ROOT), viewSelected);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<File> openFile(File f, int viewSwitch) {
        exit = false;
        File[] files = FileManager.getFiles(this, f);
        currentDirectory = f;
        if (currentDirectory.isFile()){
            currentDirectory = currentDirectory.getParentFile();
        }
        setTitle(currentDirectory.getAbsolutePath());
        if (null != files) {
            List<File> listfolder = new ArrayList<File>();
            List<File> listfiles = new ArrayList<File>();
            for (File current : files) {
                if (current.isDirectory()) {
                    listfolder.add(current);
                } else {
                    listfiles.add(current);
                }
            }

            // 排序
            Collections.sort(listfolder, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    String lhsString = lhs.getName();
                    String rhsString = rhs.getName();
                    return -rhsString.compareTo(lhsString);
                }
            });
            Collections.sort(listfiles, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    String lhsString = lhs.getName();
                    String rhsString = rhs.getName();
                    return -rhsString.compareTo(lhsString);
                }
            });
            listfolder.addAll(listfiles);

            if (-1 == viewSwitch) {
                BaseAdapter adapter = new FileAdapter(this, listfolder, R.layout.griditem);
                mGrid.setAdapter(adapter);
            } else if (1 == viewSwitch) {
                BaseAdapter adapter = new FileAdapter(this, listfolder, R.layout.listitem);
                mList.setAdapter(adapter);
            }
            currentList = listfolder;
            return listfolder;
        } else if (f.isDirectory()) {
            if (-1 == viewSwitch) {
                mGrid.setAdapter(null);
            } else if (-1 == viewSwitch) {
                mList.setAdapter(null);
            }
        }
        return null;
    }

    // 点击事件
    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (currentList != null) {
                openFile(currentList.get(position), viewSelected);
            }
        }
    };
    // 长点击事件
    private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            File file = currentList.get(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("请选择你要进行的操作");
            if (FileManager.checkEndsWithInStringArray(file.getName(), getResources().getStringArray(R.array.fileEndingImage))){
                builder.setItems(new String[]{"打开", "重命名", "删除", "复制", "剪切", "设为背景"}, new MListenr(position));
            } else {
                builder.setItems(new String[]{"打开", "重命名", "删除", "复制", "剪切"}, new MListenr(position));
            }
            builder.show();
            return true;
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((KeyEvent.KEYCODE_BACK == keyCode) && (currentDirectory.getParent() != null) && (!(DEATLPATH).equals(currentDirectory.getAbsolutePath() + "/"))) { // 如果不是根目录则返回上一级
            upOneLevel();
            return true;
        } else {
            if (exit) {
                exit = false;
                return super.onKeyDown(keyCode, event);
            } else {
                exit = true;
                Toast toast = Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT);
                toast.getView().setBackgroundColor(Color.argb(100, 0, 0, 0));
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();
                return true;
            }
        }
    }

    private DialogInterface.OnClickListener creatDirListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String name = createDirTxt.getText().toString();
            File file = new File(currentDirectory.getAbsolutePath() + "/" + name + "/");
            if (!name.equals("")) {
                if (file.exists()) {
                    for (int index = 1; ; index++) {
                        file = new File(currentDirectory.getAbsolutePath() + "/" + name + index + "/");
                        if (!file.exists()) {
                            file.mkdirs();
                            break;
                        }
                    }
                } else {
                    file.mkdirs();
                }
                openFile(currentDirectory, viewSelected);
            }
        }
    };

    private class MListenr implements DialogInterface.OnClickListener {
        private File mFlile = null;

        public MListenr(int position) {
            mFlile = currentList.get(position);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0: // 打开
                    openFile(mFlile, viewSelected);
                    break;
                case 1: // 重命名
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                    final View mView = inflater.inflate(R.layout.edit_dialog, null);
                    EditText editText = (EditText) mView.findViewById(R.id.item_edittext);
                    editText.setText(mFlile.getName());
                    editText.selectAll();
                    builder.setTitle("重命名");
                    builder.setView(mView);
                    builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String value = currentDirectory.getPath() + "/" + ((EditText) mView.findViewById(R.id.item_edittext)).getText().toString();
                            if (new File(value).exists()) {
                                new AlertDialog.Builder(MainActivity.this).setTitle("文件名重复，是否需要覆盖？")
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (mFlile.renameTo(new File(value))) {
                                                    openFile(currentDirectory, viewSelected);
                                                } else {
                                                    new AlertDialog.Builder(MainActivity.this).setTitle("重命名失败")
                                                            .setNegativeButton("确认", null)
                                                            .show();
                                                }
                                            }
                                        })
                                        .setNegativeButton("取消", null).show();
                            } else {
                                if (mFlile.renameTo(new File(value))) {
                                    openFile(currentDirectory, viewSelected);
                                }
                            }
                        }
                    });
                    builder.setPositiveButton("取消", null);
                    builder.show();

                    break;
                case 2: // 删除
                    new AlertDialog.Builder(MainActivity.this).setTitle("确认删除？")
                            .setNegativeButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mFlile.isFile()) {
                                        if (mFlile.delete()) {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("删除成功")
                                                    .setNegativeButton("确认", null)
                                                    .show();
                                            openFile(currentDirectory, viewSelected);
                                        } else {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("删除失败")
                                                    .setNegativeButton("确认", null)
                                                    .show();
                                        }
                                    } else if (mFlile.isDirectory()){
                                        if (FileManager.deleteFolder(mFlile)) {
                                            openFile(currentDirectory, viewSelected);
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("删除成功")
                                                    .setNegativeButton("确认", null)
                                                    .show();
                                        } else {
                                            new AlertDialog.Builder(MainActivity.this)
                                                    .setTitle("删除失败")
                                                    .setNegativeButton("确认", null)
                                                    .show();
                                        }
                                    }
                                }
                            }).setPositiveButton("取消", null).show();
                    break;
                case 3: // 复制
                    copiedFile = mFlile;
                    if (null != movedFile){
                        movedFile = null;
                    }
                    break;
                case 4: // 剪切
                    movedFile = mFlile;
                    if (null != copiedFile){
                        copiedFile = null;
                    }
                    break;
                case 5: // 设为背景
                    setBackground(mFlile);
                    File bgDict =new File("/sdcard/dat/");
                    File bgFile = new File("/sdcard/dat/bg.dat");
                    if(!bgDict.exists()){
                        bgDict.mkdirs();
                    }
                    if(bgFile.exists()){
                        bgFile.delete();
                    }
                    FileManager.copyFile(mFlile, bgFile);
                    break;
            }
        }
    }

    private void setBackground(File imgFile){
        if (imgFile.exists()) {
            try {
                Bitmap bitmap;
                BitmapDrawable drawable;
                bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                drawable = new BitmapDrawable(bitmap);
                findViewById(R.id.layout_main).setBackgroundDrawable(drawable);
            }catch (Exception e){}
        }
    }

    private void upOneLevel() {
        openFile(currentDirectory.getParentFile(), viewSelected);
    }
}
