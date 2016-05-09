package com.rm.cameraphone.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.rm.cameraphone.R;
import com.rm.cameraphone.worker.CropWorker;

public class PhotoCropActivity extends BaseActivity<CropWorker> {

    public static void start(Context context, Bundle args) {
        Intent starter = new Intent(context, PhotoCropActivity.class);
        if (args != null) starter.putExtras(args);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_crop);
    }

    @Override
    protected CropWorker setupWorker() {
        return new CropWorker(this);
    }
}
