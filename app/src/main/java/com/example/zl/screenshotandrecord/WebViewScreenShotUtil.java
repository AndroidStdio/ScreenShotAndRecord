package com.example.zl.screenshotandrecord;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.webkit.WebView;

/**
 * Created by Administrator on 2018/6/18.
 */

public class WebViewScreenShotUtil {


    /**
     * app没有集成浏览器x5内核
     *
     * 可以放弃这个方法
     *
     * 可以在5.0以下的系统使用
     * 在5.0系统一下可以截图整个网页，5.0以上只能截取屏幕显示部分
     * (测试结果是联想5.1系统也可以截图，不知道其他牌子的手机行不行)
     * @param webView
     * @return
     */
    public static Bitmap captureWebViewKitKat(WebView webView) {
        Picture picture = webView.capturePicture();
        int width = picture.getWidth();
        int height = picture.getHeight();
        if (width > 0 && height > 0) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            picture.draw(canvas);
            return bitmap;
        }
        return null;
    }

    /**
     *  app没有集成浏览器x5内核
     *
     * 根据Google文档中描述，capturePicture()方法已不鼓励使用，
     * 推荐我们通过webView的onDraw(Canvas)去获取图像
     * 兼容5.0以上的系统，5.0以下的系统也可以截图
     *
     * Android对WebView进行了优化，为了减少内存使用和提高性能，使用WebView加载网页时只绘制显示部分
     * 通过调用WebView.enableSlowWholeDocumentDraw()方法可以关闭这种优化，
     * 但要注意的是，该方法需要在WebView实例被创建前就要调用
     *
     * 在WebView实例被创建前加入代码
     *  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
     *      android.webkit.WebView.enableSlowWholeDocumentDraw();
     *  }
     *
     * @param webView
     * @return
     */
    public static Bitmap captureWebViewLollipop(WebView webView) {
        float scale = webView.getScale();
        int width = webView.getWidth();
        int height = (int) (webView.getContentHeight() * scale + 0.5);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);
        return bitmap;
    }


    /**
     *
     * 集成成了x5内核浏览器
     *
     * 不用考虑版本问题
     * 使用X5内核方法snapshotWholePage(Canvas, boolean, boolean)
     *
     * 这个方法有个缺点，就是不以屏幕上WebView的宽高截图，
     * 只是以WebView的contentWidth和contentHeight为宽高截图，
     * 截出来的图片会不怎么清晰
     *
     * 截取清洗长图的方法
     * 如果想要在X5内核下截到清晰的长图，不能使用snapshotWholePage()，依然可以采用capturePicture()。
     * X5内核下使用capturePicture()进行截图，可以直接拿到WebView的清晰长图，
     * 但这是个Deprecated的方法，使用的时候要做好异常处理
     *
     *
     * 这个方法还没测试过
     * */
//    private static Bitmap captureX5WebViewUnsharp(Context context, WebView webView) {
//        if (webView == null) {
//            return null;
//        }
//        if (context == null) {
//            context = webView.getContext();
//        }
//        int width = webView.getContentWidth();
//        int height = webView.getContentHeight();
//        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        Canvas canvas = new Canvas(bitmap);
//        webView.getX5WebViewExtension().snapshotWholePage(canvas, false, false);
//        return bitmap;
//    }




}
