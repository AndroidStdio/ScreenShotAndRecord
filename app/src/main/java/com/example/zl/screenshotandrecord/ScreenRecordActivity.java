package com.example.zl.screenshotandrecord;

import android.app.Activity;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/7/7.
 *
 * 录制屏幕：只能在5.0以上的系统使用
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenRecordActivity extends Activity {


    @BindView(R.id.button)
    Button button;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.play)
    Button play;
    @BindView(R.id.sfv)
    SurfaceView surfaceView;

    private int width;
    private int height;
    private int dpi;

    private static final int RECORDER_CODE = 0;
    private String videoSavePath;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;;
    private MediaCodec mediaCodec;//编码/解码组件
    private MediaMuxer mediaMuxer;//使用MediaMuxer来封装编码后的视频流和音频流到mp4容器中
    private Surface surface;

    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private AtomicBoolean mQuit = new AtomicBoolean(false);
    private int videoTrackIndex = -1;

    private SurfaceHolder holder;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_screenrecord);
        ButterKnife.bind(this);

        initView();
    }

    /**
     *
     * 使用mediaProjection录屏
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
     * 使用mediaProjection录屏
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
     * 使用mediaProjection录屏
     *
     * 第三步：设置录屏视频保存位置
     *
     * */
    private void initVideoSave(){
        File file = new File(Environment.getExternalStorageDirectory(),
                "screenrecord/record-" + System.currentTimeMillis() + ".mp4");
        videoSavePath = file.getAbsolutePath();
    }

    /**
     * 使用mediaProjection录屏
     *
     * 第四步：调用createScreenCaptureIntent()发起屏幕捕捉请求
     *
     * */
    private void startScreenRecord(){
        initScreenParameter();
        initProjectionManager();
        initVideoSave();
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), RECORDER_CODE);
    }

    /**
     * 使用mediaProjection录屏
     *
     * 第五步：获取保存返回的屏幕截取数据
     *
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RECORDER_CODE){
            if(resultCode == RESULT_OK){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setUpMediaProjection(resultCode, data);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                prepareEncoder();
                                initMediaMuxer();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            createVirtualDisplay();
                            recordVirtualDisplay();
                        }
                    }).start();

                    Toast.makeText(this, "开始录制", Toast.LENGTH_SHORT).show();
                    moveTaskToBack(true);//退出到桌面，录制屏幕
                }
            }
        }
    }

    /**
     * 使用mediaProjection录屏
     *
     * 第六步：实例化MediaProjection
     *
     * */
    private void setUpMediaProjection(int mResultCode, Intent mData){
        mediaProjection = mediaProjectionManager.getMediaProjection(mResultCode, mData);
    }


    /**
     * 使用mediaProjection录屏
     *
     * 第七步：配置视频的录制参数，编码组件
     *
     * */
    private void prepareEncoder() throws IOException {
        //设置视频录制的参数配置
        MediaFormat format = MediaFormat.createVideoFormat("video/avc", width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,//COLOR_FormatSurface这里表明数据将是一个graphicbuffer元数据
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 6000000);//设置码率，码率越大视频越清晰，相对的占用内存也要更大
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);//设置帧数
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 10);

        //创建MediaCodec实例
        mediaCodec = MediaCodec.createEncoderByType("video/avc");
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        // 获取MediaCodec的surface，这个surface其实就是视频编码的入口，
        // 录屏的数据传输入口，然后交给MediaCodec编码
        surface = mediaCodec.createInputSurface();
        mediaCodec.start();
    }


    /**
     * 使用mediaProjection录屏
     *
     * 第八步：创建MediaMuxer视频流封装组件
     *
     * */
    private void initMediaMuxer() throws IOException{
        //创建一个MediaMuxer对象
        mediaMuxer = new MediaMuxer(videoSavePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
    }

    /**
     * 使用mediaProjection截图
     *
     * 第九步：创建VirtualDisplay，把MediaCodec的surface传进去
     *
     * */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void createVirtualDisplay(){
        virtualDisplay = mediaProjection.createVirtualDisplay("LOLLIPOPScreenRecord",
                width, height, dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                surface ,null, null);
    }

    /**
     * 使用mediaProjection截图
     *
     * 第十步：开始录屏，获取屏幕数据
     *
     *
     * */
    private void recordVirtualDisplay() {
        while (!mQuit.get()) {
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat();
            } else if (index >= 0) {
                encodeToVideoTrack(index);
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }
    }


     //获取视频编码格式，添加mediaMuxer媒体通道，并记录下来index,然后开始封装视频流
    private void resetOutputFormat() {
        MediaFormat newFormat = mediaCodec.getOutputFormat();
        videoTrackIndex = mediaMuxer.addTrack(newFormat);
        mediaMuxer.start();
    }


    //将数据传给MediaMuxer，将其封装转换成mp4
    private void encodeToVideoTrack(int index) {
        ByteBuffer encodedData = mediaCodec.getOutputBuffer(index);

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0;
        }
        if (bufferInfo.size == 0) {
            encodedData = null;
        }
        if (encodedData != null) {
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo);
        }
    }

    private void release() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
        if (mediaMuxer != null) {
            mediaMuxer.release();
            mediaMuxer = null;
        }
    }
    

    //停止录制
    public void stopRecorder() {
        mQuit.set(true);
        play.setEnabled(true);
        Toast.makeText(this, "录制结束", Toast.LENGTH_SHORT).show();
    }


    @OnClick({R.id.button, R.id.button2, R.id.play})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button:
                button.setEnabled(false);
                button2.setEnabled(true);
                startScreenRecord();
                break;
            case R.id.button2:
                button.setEnabled(true);
                button2.setEnabled(false);
                stopRecorder();
                release();
                break;
            case R.id.play:
                play();
                break;
        }
    }


    //播放录制好的视频
    public void play() {
        play.setEnabled(false);//在播放时不允许再点击播放按钮

        File file = new File(videoSavePath);
        if (!file.exists()) {//判断需要播放的文件路径是否存在，不存在退出播放流程
            Toast.makeText(this,"文件路径不存在",Toast.LENGTH_LONG).show();
            return;
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(videoSavePath);
            mediaPlayer.setDisplay(holder);//将影像播放控件与媒体播放控件关联起来

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {//视频播放完成后，释放资源
                    play.setEnabled(true);
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

    private void stop(){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            play.setEnabled(true);
        }
    }

    //初始化控件，并且为进度条和图像控件添加监听
    private void initView() {
        button2.setEnabled(false);
        play.setEnabled(false);
        holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

}
