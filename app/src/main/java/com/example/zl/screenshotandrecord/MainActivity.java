package com.example.zl.screenshotandrecord;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Administrator on 2018/6/24.
 */

public class MainActivity extends Activity {


    @BindView(R.id.text1)
    Button text1;
    @BindView(R.id.text2)
    Button text2;
    @BindView(R.id.text3)
    Button text3;
    @BindView(R.id.text4)
    Button text4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.text1:
                Intent intent1 = new Intent(MainActivity.this, SurfaceViewActivity.class);
                startActivity(intent1);
                break;
            case R.id.text2:
                Intent intent2 = new Intent(MainActivity.this, ScrollViewActivity.class);
                startActivity(intent2);
                break;
            case R.id.text3:
                Intent intent3 = new Intent(MainActivity.this, RecyclerviewActivity.class);
                startActivity(intent3);
                break;
            case R.id.text4:
                Intent intent4 = new Intent(MainActivity.this, WebViewActivity.class);
                startActivity(intent4);
                break;
            case R.id.text5:
                Intent intent5 = new Intent(MainActivity.this, ScreenRecordActivity.class);
                startActivity(intent5);
                break;
        }
    }

    @OnClick(R.id.text5)
    public void onViewClicked() {
    }
}
