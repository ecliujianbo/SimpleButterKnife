package com.liu.simplebutterknife;

import android.app.Activity;
import android.util.Log;

import com.liu.simple_butterknife_annotation.SimpleBindView;
import com.liu.simple_butterknife_annotation.SimpleViewBinder;

/**
 * <p>Created by jianbo on 2018/1/26.</p>
 */
public class SimpleButterKnife {
    public static void bind(Activity activity) {

        //获取编写的内部类
        String innerClassName = activity.getClass().getName() + "$SimpleViewBinder";
        try {
            Class<?> aClass = Class.forName(innerClassName);
            SimpleViewBinder viewBinder = (SimpleViewBinder) aClass.newInstance();
            //绑定activity
            viewBinder.bind(activity);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
