package com.drawer.airisith.drawer;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2015/9/24.
 */
public class ViewSp {
    private final String SP_NAME = "VIEW_SELECTED";
    private final String SP_SELECTED = "SELECTED";
    private Context context;
    SharedPreferences sp;
    public ViewSp(Context context){
        this.context = context;
        sp = context.getSharedPreferences(SP_NAME, context.MODE_PRIVATE);

    }

    public void save(int selected){
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SP_SELECTED, selected);
        editor.commit();
    }

    public int get(){
        return sp.getInt(SP_SELECTED, -1);
    }
}
