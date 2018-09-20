package com.example.zl.screenshotandrecord;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.bitmap;

public class SurfaceViewActivity extends AppCompatActivity {

    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.playBtn)
    Button playBtn;


    private static final int SCREEN_SHOT = 0;
    private MediaProjection mediaProjection;
    private MediaProjectionManager mediaProjectionManager;
    private ImageReader imageReader;
    private int mResultCode;
    private Intent mData;
    private int width;
    private int height;
    private int dpi;
    private Handler backgroundHandler;
    private Bitmap bitmap;
    private boolean screenShot = false;

    private SurfaceHolder holder;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenshot);
        ButterKnife.bind(this);

        initView();
    }

    //初始化控件，并且为进度条和图像控件添加监听
    private void initView() {
        holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }


    /**
     *
     * 使用mediaProjection截图
     *
     * 第一步：获取屏幕长宽，屏幕密度
     *
     * */
    private void initScreenParameter(){
        if (width == 0 || height == 0){
            DisplayMetrics metric = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metric);
            width = metric.widthPixels;
            height = metric.heightPixels;
            dpi = metric.densityDpi;
        }
    }

    /**
     * 使用mediaProjection截图
     *
     * 第二步：实例化MediaProjectionManager
     *
     * */
    private void initProjectionManager(){
        if (mediaProjectionManager == null){
            mediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        }
    }

    /**
     * 使用mediaProjection截图
     *
     * 第三步：调用createScreenCaptureIntent()发起屏幕捕捉请求
     *
     * */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void StartScreenShot(){
        initScreenParameter();
        initProjectionManager();
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), SCREEN_SHOT);
    }

    /**
     * 使用mediaProjection截图
     *
     * 第四步：获取保存返回的屏幕截取数据
     *
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SCREEN_SHOT){
            if(resultCode == RESULT_OK){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setUpMediaProjection(resultCode, data);
                    setUpImageReader();
                    createScreenShot();
//                    startCapture();
                }
            }
        }
    }

    /**
     * 使用mediaProjection截图
     *
     * 第五步：实例化MediaProjection
     *
     * */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setUpMediaProjection(int mResultCode, Intent mData){
        mediaProjection = mediaProjectionManager.getMediaProjection(mResultCode, mData);
    }

    /**
     * 使用mediaProjection截图
     *
     * 第六步：配置ImageReader
     *
     * */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void setUpImageReader() {
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        //监听ImageReader中的image是否可用，在hander的线程中触发
        imageReader.setOnImageAvailableListener(onImageAvailableListener, getBackgroundHandler());
    }

    /**
     * 使用mediaProjection截图
     *
     * 第七步：创建VirtualDisplay，把录屏数据设置到imageReader中
     *  createVirtualDisplay会不断的把每一帧的图像写入到imageReader里面
     *
     * */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createScreenShot(){
        //getSurface() 用于生成image图像保存屏幕的数据，
        mediaProjection.createVirtualDisplay("LOLLIPOPScreenShot",
                width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface() ,null, null);
    }

    /**
     * 使用mediaProjection截图
     *
     * 第八步：从imageReader中获取image,把image中的图像数据转换成bitmap
     *
     * */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startCapture() {
        SystemClock.sleep(1000);//获取一秒后的图像数据，按下截图后，会弹出截图咨询窗口，一秒后比较合适
        Image image = imageReader.acquireNextImage();
        if (image == null) {
            return;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        image.close();

        if (bitmap != null) {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
        }
    }


    ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override//这个会被不断的触发
        public void onImageAvailable(ImageReader reader) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (screenShot){
                screenShot = false;
                startCapture();
            }
        }
    };

    private Handler getBackgroundHandler() {
        if (backgroundHandler == null) {
            HandlerThread backgroundThread =
                    new HandlerThread("catwindow", android.os.Process
                            .THREAD_PRIORITY_BACKGROUND);
            backgroundThread.start();
            backgroundHandler = new Handler(backgroundThread.getLooper());
        }
        return backgroundHandler;
    }



    //播放本地视频
    public void play() {
        playBtn.setEnabled(false);//在播放时不允许再点击播放按钮

        File videoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/screenrecord/test.mp4");
        if (!videoFile.exists()) {//判断需要播放的文件路径是否存在，不存在退出播放流程
            Toast.makeText(this, "文件路径不存在", Toast.LENGTH_LONG).show();
            playBtn.setEnabled(true);
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(videoFile.getAbsolutePath());
            mediaPlayer.setDisplay(holder);//将影像播放控件与媒体播放控件关联起来
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {//视频播放完成后，释放资源
                    playBtn.setEnabled(true);
                    stop();
                }
            });
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //媒体播放器就绪后，设置进度条总长度，开启计时器不断更新进度条，播放视频
                    mediaPlayer.start();
                }
            });
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            playBtn.setEnabled(true);
        }
    }



    @OnClick({R.id.kitkatbutton, R.id.lollipopbutton, R.id.playBtn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.kitkatbutton:
                Bitmap screenPic = OrdinaryScreenShotUtil.shotActivity(SurfaceViewActivity.this);
                imageView.setImageBitmap(screenPic);
                break;
            case R.id.lollipopbutton:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    screenShot = true;
                    StartScreenShot();
                }else{
                    Toast.makeText(SurfaceViewActivity.this, "系统版本太低", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.playBtn:
                play();
                break;
        }
    }

}
