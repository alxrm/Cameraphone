package com.rm.cameraphone.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rm.cameraphone.R;
import com.rm.cameraphone.components.crop.CropOverlayView;
import com.rm.cameraphone.components.crop.GestureImageView;
import com.rm.cameraphone.components.crop.MatrixImageView;
import com.rm.cameraphone.components.crop.WheelView;
import com.rm.cameraphone.events.CropBoundsChangeListener;
import com.rm.cameraphone.events.OnWheelScrollingListener;
import com.rm.cameraphone.events.OverlayViewChangeListener;
import com.rm.cameraphone.util.DispatchUtils;
import com.rm.cameraphone.util.SharedMap;
import com.rm.cameraphone.worker.CropWorker;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_CROP_RESULT;
import static com.rm.cameraphone.util.DimenUtils.dp;
import static com.rm.cameraphone.util.DimenUtils.width;
import static com.rm.cameraphone.util.DispatchUtils.cleanUp;

public class PhotoCropActivity extends BaseActivity<CropWorker> {

    private static final String PHOTO_SOURCE_PATH = "arg_photo_src_path";

    @InjectView(R.id.crop_overlay) CropOverlayView mCropOverlayView;
    @InjectView(R.id.crop_rotate_icon) ImageView mRotateIcon;
    @InjectView(R.id.crop_rotate_wheel) WheelView mRotateWheel;
    @InjectView(R.id.crop_rotate_actions) RelativeLayout mRotationActions;
    @InjectView(R.id.crop_rotate_angle) TextView mTextAngle;
    @InjectView(R.id.crop_target) GestureImageView mTarget;
    @InjectView(R.id.crop_result) ImageView mResult;

    // actions
    @InjectView(R.id.crop_cancel) Button mBtnCancel;
    @InjectView(R.id.crop_done) Button mBtnDone;
    @InjectView(R.id.crop_reset) Button mBtnReset;

    private Runnable mTaskShowResult;
    private String mCurrentImagePath;

    public static void start(Context context, String srcPath) {
        Intent starter = new Intent(context, PhotoCropActivity.class);
        starter.putExtra(PHOTO_SOURCE_PATH, srcPath);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_crop);
        ButterKnife.inject(this);

        setupViews();
        registerTasks();

        mCurrentImagePath = getIntent().getExtras().getString(PHOTO_SOURCE_PATH);
        if (mCurrentImagePath == null) return;

        updateImage();

        resetCropSettings(200);
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

        mTarget.setTransformImageListener(new MatrixImageView.TransformImageListener() {
            @Override
            public void onLoadComplete() {
            }

            @Override
            public void onLoadFailure(@NonNull Exception e) {

            }

            @Override
            public void onRotate(float currentAngle) {
                mCropOverlayView.setShowCropGrid(true);
                mBtnReset.setVisibility(View.VISIBLE);
                mTextAngle.setText(String.format("%.1f°", currentAngle));
            }

            @Override
            public void onScale(float currentScale) {
                mCropOverlayView.setShowCropGrid(true);
                mBtnReset.setVisibility(View.VISIBLE);
            }
        });

        mTarget.setCropBoundsChangeListener(new CropBoundsChangeListener() {
            @Override
            public void onCropAspectRatioChanged(float cropRatio) {
                mCropOverlayView.setTargetAspectRatio(cropRatio);
            }
        });

        mCropOverlayView.setOverlayViewChangeListener(new OverlayViewChangeListener() {
            @Override
            public void onCropRectUpdated(RectF cropRect) {
                mBtnReset.setVisibility(View.VISIBLE);
                mCropOverlayView.setShowCropGrid(true);
                mTarget.setCropRect(cropRect);
            }
        });

        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetCropSettings(0);
            }
        });

        mBtnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTarget.cropAndSaveImage(mTaskShowResult);
            }
        });
    }


    private void registerTasks() {
        mTaskShowResult = registerTaskShowResult();
    }

    private Runnable registerTaskShowResult() {
        return new Runnable() {
            @Override
            public void run() {
                mCurrentImagePath = (String) SharedMap.holder().get(KEY_CAMERA_CROP_RESULT);

                showResultState();

                Glide.with(PhotoCropActivity.this)
                        .load(mCurrentImagePath)
                        .override((int) width(), dp(200))
                        .fitCenter()
                        .into(mResult);

                mBtnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mWorker.eraseResult(mCurrentImagePath);
                        close();
                    }
                });

                mBtnDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mWorker.saveResult(mCurrentImagePath);
                        close();
                    }
                });
            }
        };
    }

    @Override
    protected CropWorker setupWorker() {
        return new CropWorker(this);
    }

    private void updateImage() {
        try {
            mTarget.setImagePath(mCurrentImagePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetCropSettings(long delay) {
        DispatchUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCropOverlayView.setTargetAspectRatio(mTarget.getTargetAspectRatio());
                mTarget.postRotate(-mTarget.getCurrentAngle());
                mTarget.zoomOutImage(mTarget.getMinScale());

                mTarget.cancelAllAnimations();
                mTarget.setImageToWrapCropBounds(false);
                mBtnReset.setVisibility(View.GONE);
                mCropOverlayView.setShowCropGrid(false);
            }
        }, delay);
    }

    private void showResultState() {
        mCropOverlayView.setVisibility(View.GONE);
        mTarget.setVisibility(View.GONE);
        mBtnReset.setVisibility(View.GONE);
        mRotationActions.setVisibility(View.GONE);
        mTarget.setImageBitmap(null);

        mResult.setVisibility(View.VISIBLE);
        mBtnDone.setText("Save");
    }

    private void close() {
        DispatchUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cleanUp();
                finish();
            }
        }, 200);
    }
}
