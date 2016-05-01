package com.rm.cameraphone.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.rm.cameraphone.R;
import com.rm.cameraphone.constants.ColorConstants;

import static com.rm.cameraphone.util.Interpolators.ACCELERATE;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;

/**
 * Created by alex
 */
public class StartView extends RelativeLayout {

    private ImageView mCameraIcon;
    private RelativeLayout.LayoutParams mCameraIconParams;

    public StartView(Context context) {
        super(context);
        initialize();
    }

    public StartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public StartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StartView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        setWillNotDraw(false);
        setBackgroundColor(ColorConstants.COLOR_PRIMARY_DARK);
        setAlpha(0);

        mCameraIconParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        mCameraIconParams.addRule(CENTER_IN_PARENT);

        mCameraIcon = new ImageView(getContext());
        mCameraIcon.setLayoutParams(mCameraIconParams);
        mCameraIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mCameraIcon.setImageResource(R.drawable.ic_start_icon);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(mCameraIcon);
    }

    public void show() {
        mCameraIcon.setScaleX(1);
        mCameraIcon.setScaleY(1);
        mCameraIcon.setRotation(0);
        animateBackground(true);
    }

    public void hide() {
        animateIcon(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animateBackground(false);
            }
        });
    }

    private void animateBackground(boolean toShow) {
        this.animate()
                .alpha(toShow ? 1 : 0)
                .setDuration(200)
                .setInterpolator(DECELERATE)
                .start();
    }

    private void animateIcon(Animator.AnimatorListener listener) {
        mCameraIcon.animate()
                .scaleY(0)
                .scaleX(0)
                .rotation(90)
                .setDuration(300)
                .setInterpolator(ACCELERATE)
                .setListener(listener)
                .start();
    }
}
