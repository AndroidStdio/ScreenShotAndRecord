package com.example.zl.screenshotandrecord;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.View;
import android.widget.ScrollView;

/**
 * Created by Administrator on 2018/6/12.
 *
 * 普通页面截图工具类
 *
 */

public class OrdinaryScreenShotUtil {

    /**
     *
     *
     * 优点：
     *  兼容5.0以下的系统，（5.0以上用 MediaProjection截图）
     *
     * 缺点：
     *  5.0以下的系统无法截取到状态栏
     *  surfaceView的播放内容截取为黑屏
     *
     * */
    public static Bitmap shotActivity(Activity ctx) {
        View view = ctx.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);//设置view的缓存为true
        view.buildDrawingCache();//创建view的缓存
        //获取view的缓存，转换成bitmap
        Bitmap bp = Bitmap.createBitmap(view.getDrawingCache(), 0, 0,
                view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(false);//设置view的缓存为false
        view.destroyDrawingCache();//去掉缓存
        return bp;
    }


    /**
     *
     * 截取某个控件的截图
     * 只能截取到屏幕显示的部分，屏幕没有显示的部分截取不到，适用于局部截图
     *
     * 获取当前View的DrawingCache
     *
     * */
    public static Bitmap getViewBp(View v) {
        if (null == v) {
            return null;
        }
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache();

        //将view重新测量，布局
        if (Build.VERSION.SDK_INT >= 11) {
            v.measure(View.MeasureSpec.makeMeasureSpec(v.getWidth(),
                    View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
                    v.getHeight(), View.MeasureSpec.EXACTLY));
            v.layout((int) v.getX(), (int) v.getY(),
                    (int) v.getX() + v.getMeasuredWidth(),
                    (int) v.getY() + v.getMeasuredHeight());
        } else {
            v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        }
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache(), 0, 0,
                v.getMeasuredWidth(), v.getMeasuredHeight());
        v.setDrawingCacheEnabled(false);
        v.destroyDrawingCache();
        return b;
    }




}
