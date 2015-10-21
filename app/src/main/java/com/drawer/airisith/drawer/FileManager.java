package com.drawer.airisith.drawer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2015/9/22.
 */
public class FileManager {

    private final static String TAG = "FileManager";

    public static File[] getFiles(Context context, File f) {
        if (f.exists()) {
            File[] files;
            if (f.isDirectory()) {
                files = f.listFiles();
                return files;
            } else {
                openFile(context, f);
                return null;
            }
        }
        return null;
    }

    //打开指定文件
    protected static void openFile(Context context, File aFile) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(aFile.getAbsolutePath());
        // 取得文件名
        String fileName = file.getName();
        // 根据不同的文件类型来打开文件
        if (checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingImage))) {
            intent.setDataAndType(Uri.fromFile(file), "image/*");
        } else if (checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingAudio))) {
            intent.setDataAndType(Uri.fromFile(file), "audio/*");
        } else if (checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingVideo))) {
            intent.setDataAndType(Uri.fromFile(file), "video/*");
        }
        context.startActivity(intent);
    }

    //通过文件名判断是什么类型的文件
    public static boolean checkEndsWithInStringArray(String checkItsEnd,
                                                     String[] fileEndings) {
        for (String aEnd : fileEndings) {
            if (checkItsEnd.endsWith(aEnd))
                return true;
        }
        return false;
    }

    public static boolean deleteFolder(File folder) {
        boolean result = false;
        try {
            String childs[] = folder.list();
            if (childs == null || childs.length <= 0) {
                if (folder.delete()) {
                    result = true;
                }
            } else {
                for (int i = 0; i < childs.length; i++) {
                    String childName = childs[i];
                    String childPath = folder.getPath() + File.separator + childName;
                    File filePath = new File(childPath);
                    if (filePath.exists() && filePath.isFile()) {
                        if (filePath.delete()) {
                            result = true;
                        } else {
                            result = false;
                            break;
                        }
                    } else if (filePath.exists() && filePath.isDirectory()) {
                        if (deleteFolder(filePath)) {
                            result = true;
                        } else {
                            result = false;
                            break;
                        }
                    }
                }
                folder.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    /**
     *
     * @param source 源文件
     * @param destinationPath 文件新路径（文件夹）
     * @return
     */
    public static boolean moveFile(File source, String destinationPath) {
        String filePath = destinationPath;
        if (source.getParent().equals(destinationPath)) { // 如果移动的目标路径与原路径相同，则不移动
            return true;
        }
        filePath = filePath + "/" + source.getName();
        if (new File(filePath).exists()) { // 同文件夹下该文件名存在
            // filePath = getNewName(new File(filePath)).getAbsolutePath(); // 自动重命名
            // 覆盖
            new File(filePath).delete();
        }
        return source.renameTo(new File(filePath));
    }

    /**
     *
     * @param src 源文件
     * @param target 目标文件
     * @return
     */
    public static boolean copyFile(File src, File target) {
        InputStream in = null;
        OutputStream out = null;
        boolean exist = false;
        File newFile;

        String filePath = target.getParent();
        if (target.exists()) { // 同文件夹下该文件名存在
            // filePath = getNewName(target).getAbsolutePath(); // 自动重命名
            filePath = filePath + "/" + target.getName()+".bak";
            exist = true;
        } else { // 目标文件夹下不存在同名文件
            filePath = filePath + "/" + target.getName();
        }
        Log.i(TAG, "filepath:" + filePath);
        newFile = new File(filePath);

        // 流操作
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;
        boolean success = true;
        try {
            in = new FileInputStream(src);
            out = new FileOutputStream(newFile);
            bin = new BufferedInputStream(in);
            bout = new BufferedOutputStream(out);

            byte[] b = new byte[8192];
            int len = bin.read(b);
            while (len != -1) {
                bout.write(b, 0, len);
                len = bin.read(b);
            }

        } catch (FileNotFoundException e) {
            success = false;
            e.printStackTrace();
        } catch (IOException e) {
            success = false;
            e.printStackTrace();
        } catch (Exception e) {
            success = false;
        } finally {
            try {
                if (bin != null) {
                    bin.close();
                }
                if (bout != null) {
                    bout.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (exist){
                target.delete();
                newFile.renameTo(target);
            }
            return success;
        }
    }

    /**
     * 文件名重复时，以（n）的方式重命名
     * @param file
     * @return
     */
    public static File getNewName(File file) {
        File newFile = file;
        String path = file.getParent(); // 路径名
        String name = file.getName(); // 文件名
        String startName = name;
        String tail = "";
        int num = 1;
        if (name.contains(".")) { // 后缀名
            tail = name.substring(name.lastIndexOf("."));
            startName = name.substring(0, name.lastIndexOf("."));
        }

        Log.i(TAG, "path:" + path + ",name:" + startName + ",tail" + tail);
        if (startName.endsWith(")") && (startName.contains("("))) { // 包含“（”和“）”
            int startIndex = startName.lastIndexOf("(");
            String sub = "";
            try {
                sub = startName.substring(startIndex);
            } catch (Exception e) {
            }
            //Log.i(TAG, "sub:"+sub);
            try {
                num = Integer.parseInt(sub.substring(1, sub.length() - 1)) + 1;
            } catch (Exception e) {
            }
            Log.i(TAG, "num:" + num);
            startName = startName.substring(0,startName.lastIndexOf("("))+"(" + num + ")";
        }else {
            startName = startName + "(" + num + ")";
        }

        newFile = new File(path + "/" + startName + tail);
        if (newFile.exists()) {
            newFile = getNewName(newFile);
        }
        return newFile;
    }
}
