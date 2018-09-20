package com.example.zl.screenshotandrecord;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/6/23.
 */

public class RecyclerviewActivity extends Activity {


    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.imageView)
    ImageView imageView;

    private RvAdapter rvAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_recyclerview);
        ButterKnife.bind(this);

        rvAdapter = new RvAdapter(this);
        recyclerView.setAdapter(rvAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }


    @OnClick(R.id.imageView)
    public void onViewClicked() {
        Bitmap screenPic = LongWidgetScreenShotUtil.shotRecyclerView(recyclerView);
        imageView.setImageBitmap(screenPic);
    }


}
