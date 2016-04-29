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
import com.rm.cameraphone.constants.StyleConstants;

import static com.rm.cameraphone.constants.Interpolators.ACCELERATE;
import static com.rm.cameraphone.constants.Interpolators.DECELERATE;

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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addView(mCameraIcon);
    }

    public void show() {
        animateBackground(true, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animateIcon(true, null);
            }
        });
    }

    public void hide() {
        animateIcon(false, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animateBackground(false, null);
            }
        });
    }

    private void initialize() {
        setWillNotDraw(false);
        setBackgroundColor(StyleConstants.COLOR_PRIMARY_DARK);
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

        mCameraIcon.setScaleX(0);
        mCameraIcon.setScaleY(0);
        mCameraIcon.setRotation(90);
    }

    private void animateBackground(boolean toShow, Animator.AnimatorListener listener) {
        this.animate()
                .alpha(toShow ? 1 : 0)
                .setDuration(200)
                .setInterpolator(DECELERATE.get())
                .setListener(listener)
                .start();
    }

    private void animateIcon(boolean toShow, Animator.AnimatorListener listener) {
        mCameraIcon.animate()
                .scaleY(toShow ? 1 : 0)
                .scaleX(toShow ? 1 : 0)
                .rotation(toShow ? 0 : 90)
                .setDuration(300)
                .setInterpolator(toShow ? DECELERATE.get() : ACCELERATE.get())
                .setListener(listener)
                .start();
    }
}
