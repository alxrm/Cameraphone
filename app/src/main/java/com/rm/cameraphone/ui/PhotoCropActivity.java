package com.rm.cameraphone.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rm.cameraphone.R;
import com.rm.cameraphone.components.crop.GestureCropImageView;
import com.rm.cameraphone.components.crop.HorizontalWheelView;
import com.rm.cameraphone.components.crop.OverlayView;
import com.rm.cameraphone.components.crop.TransformImageView;
import com.rm.cameraphone.events.CropBoundsChangeListener;
import com.rm.cameraphone.events.OnWheelScrollingListener;
import com.rm.cameraphone.events.OverlayViewChangeListener;
import com.rm.cameraphone.util.DispatchUtils;
import com.rm.cameraphone.util.SharedMap;
import com.rm.cameraphone.worker.CropWorker;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_SHOT_PATH;

public class PhotoCropActivity extends BaseActivity<CropWorker> {

    @InjectView(R.id.crop_overlay) OverlayView mOverlayView;
    @InjectView(R.id.crop_rotate_icon) ImageView mRotateIcon;
    @InjectView(R.id.crop_rotate_wheel) HorizontalWheelView mRotateWheel;
    @InjectView(R.id.crop_rotate_angle) TextView mTextAngle;
    @InjectView(R.id.crop_target) GestureCropImageView mTarget;
    @InjectView(R.id.crop_cancel) Button mBtnCancel;
    @InjectView(R.id.crop_done) Button mBtnDone;
    @InjectView(R.id.crop_reset) Button mBtnReset;

    public static void start(Context context, Bundle args) {
        Intent starter = new Intent(context, PhotoCropActivity.class);
        if (args != null) starter.putExtras(args);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_crop);
        ButterKnife.inject(this);
        setupViews();

        String path = (String) SharedMap.holder().get(KEY_CAMERA_SHOT_PATH);

        try {
            mTarget.setImagePath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DispatchUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resetImage();
            }
        }, 200);
    }

    private void setupViews() {
        mRotateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTarget.postRotate(-90);
                mTarget.setImageToWrapCropBounds(false);
                mBtnReset.setVisibility(View.VISIBLE);
            }
        });

        mRotateWheel.setOnWheelScrollingListener(new OnWheelScrollingListener() {
            @Override
            public void onWheelScrollStart() {

            }

            @Override
            public void onWheelScroll(float delta, float totalDistance) {
                mTarget.postRotate(delta / 10);
                mTarget.setImageToWrapCropBounds(true);
                mBtnReset.setVisibility(View.VISIBLE);
            }

            @Override
            public void onWheelScrollEnd() {

            }
        });

        mTarget.setTransformImageListener(new TransformImageView.TransformImageListener() {
            @Override
            public void onLoadComplete() {
            }

            @Override
            public void onLoadFailure(@NonNull Exception e) {

            }

            @Override
            public void onRotate(float currentAngle) {
                mOverlayView.setShowCropGrid(true);
                mBtnReset.setVisibility(View.VISIBLE);
                mTextAngle.setText(currentAngle == -0F ?
                        "0°" : String.format("%.1f°", currentAngle));
            }

            @Override
            public void onScale(float currentScale) {
                mOverlayView.setShowCropGrid(true);
                mBtnReset.setVisibility(View.VISIBLE);
            }
        });

        mTarget.setCropBoundsChangeListener(new CropBoundsChangeListener() {
            @Override
            public void onCropAspectRatioChanged(float cropRatio) {
                mOverlayView.setTargetAspectRatio(cropRatio);
            }
        });

        mOverlayView.setOverlayViewChangeListener(new OverlayViewChangeListener() {
            @Override
            public void onCropRectUpdated(RectF cropRect) {
                mBtnReset.setVisibility(View.VISIBLE);
                mOverlayView.setShowCropGrid(true);
                mTarget.setCropRect(cropRect);
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetImage();
            }
        });

        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTarget.cropAndSaveImage();
            }
        });
    }

    @Override
    protected CropWorker setupWorker() {
        return new CropWorker(this);
    }

    private void resetImage() {
        mOverlayView.setTargetAspectRatio(mTarget.getTargetAspectRatio());
        mTarget.postRotate(-mTarget.getCurrentAngle());
        mTarget.zoomOutImage(mTarget.getMinScale());

        mTarget.cancelAllAnimations();
        mTarget.setImageToWrapCropBounds(false);
        mBtnReset.setVisibility(View.GONE);
        mOverlayView.setShowCropGrid(false);
    }
}
