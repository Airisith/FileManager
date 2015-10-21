package com.drawer.airisith.drawer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/22.
 */
public class FileAdapter  extends BaseAdapter {

    private List<File> list;
    private Context context;
    private int layout;
    private List<ImageView> imageViews;
    public FileAdapter(Context context, List<File> list, int layout){
        this.context = context;
        this.list = list;
        this.layout = layout;
        imageViews = new ArrayList<ImageView>();
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        //l调用LayoutInflater对象的inflate方法，可以生成一个View对象
        View view = layoutInflater.inflate(layout, null);
        //得到该View当中的两个控件
        ImageView imageView = (ImageView)view.findViewById(R.id.item_img);
        TextView txtView = (TextView)view.findViewById(R.id.item_txt);
        File file = list.get(position);
        txtView.setText(file.getName());
        if (file.isDirectory()){
            imageView.setImageResource(R.drawable.folder);
        }else if (FileManager.checkEndsWithInStringArray(file.getName(), context.getResources().getStringArray(R.array.fileEndingImage))) {
            imageView.setImageResource(R.drawable.image);
            // 加载图片
            new Thread(new LoadImgThread(file, position)).start();
        }else if (FileManager.checkEndsWithInStringArray(file.getName(), context.getResources().getStringArray(R.array.fileEndingAudio))) {
            imageView.setImageResource(R.drawable.audio);
        }else if (FileManager.checkEndsWithInStringArray(file.getName(), context.getResources().getStringArray(R.array.fileEndingVideo))) {
            imageView.setImageResource(R.drawable.video);
        }else if (FileManager.checkEndsWithInStringArray(file.getName(), context.getResources().getStringArray(R.array.fileEndingText))) {
            imageView.setImageResource(R.drawable.text);
        }else if (FileManager.checkEndsWithInStringArray(file.getName(), context.getResources().getStringArray(R.array.fileEndingWebText))) {
            imageView.setImageResource(R.drawable.webtext);
        }else if (FileManager.checkEndsWithInStringArray(file.getName(), context.getResources().getStringArray(R.array.fileEndingPackage))) {
            imageView.setImageResource(R.drawable.packed);
        }else {
            imageView.setImageResource(R.drawable.text);
        }
        if (!imageViews.contains(imageView)) {
            imageViews.add(position, imageView);
        }
        return view;
    }

    public class LoadImgThread implements Runnable{
        private File file;
        int position;
        public LoadImgThread(File file, int position){
            this.file = file;
            this.position = position;
        }
        @Override
        public void run() {
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            Bitmap mBitmap = Bitmap.createScaledBitmap(bmp, 35, 35, true);
            Message msg = mHandler.obtainMessage();
            msg.obj = mBitmap;
            msg.what = position;
            mHandler.sendMessage(msg);
        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                Bitmap bitmap = (Bitmap)msg.obj;
                imageViews.get(msg.what).setImageBitmap(bitmap);
            }catch (Exception e){
            }
        }
    };
}
