package com.example.zl.screenshotandrecord;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.ScrollView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/6/23.
 */

public class ScrollViewActivity extends Activity {


    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.scrollView)
    ScrollView scrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrollview);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.imageView)
    public void onViewClicked() {
        Bitmap screenPic = OrdinaryScreenShotUtil.getViewBp(scrollView);
        imageView.setImageBitmap(screenPic);
    }
}
