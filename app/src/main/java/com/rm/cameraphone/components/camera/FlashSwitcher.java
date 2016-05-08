package com.rm.cameraphone.components.camera;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.rm.cameraphone.R;
import com.rm.cameraphone.events.OnFlashModeListener;

import static com.rm.cameraphone.constants.FlashSwitcherConstants.FLASH_MODE_AUTO;
import static com.rm.cameraphone.constants.FlashSwitcherConstants.FLASH_MODE_OFF;
import static com.rm.cameraphone.constants.FlashSwitcherConstants.FLASH_MODE_ON;
import static com.rm.cameraphone.util.Interpolators.DECELERATE;

/**
 * Created by alex
 */
public class FlashSwitcher extends FrameLayout {

    private OnFlashModeListener mFlashModeListener;
    private FrameLayout.LayoutParams mIconParams;
    private ImageView mFlashModeIconTop;
    private ImageView mFlashModeIconBot;
    private ImageView[] mIcons;

    private Animation mAnimationFromTop;
    private Animation mAnimationToBottom;

    private int mFlashMode;
    private int mNextIconRes;

    public FlashSwitcher(Context context) {
        super(context);
        initialize();
    }

    public FlashSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public FlashSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FlashSwitcher(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize();
    }

    private void initialize() {
        mFlashMode = FLASH_MODE_AUTO;
        mNextIconRes = R.drawable.flash_on;

        mIconParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        mIconParams.gravity = Gravity.CENTER;

        mFlashModeIconBot = new ImageView(getContext());
        mFlashModeIconBot.setLayoutParams(mIconParams);
        mFlashModeIconBot.setImageResource(R.drawable.flash_auto);

        mFlashModeIconTop = new ImageView(getContext());
        mFlashModeIconTop.setLayoutParams(mIconParams);
        mFlashModeIconTop.setImageResource(mNextIconRes);
        mFlashModeIconTop.setVisibility(GONE);

        mAnimationFromTop = AnimationUtils.loadAnimation(getContext(), R.anim.flash_from_top);
        mAnimationToBottom = AnimationUtils.loadAnimation(getContext(), R.anim.flash_to_bottom);
        mAnimationToBottom.setAnimationListener(getSlideAnimationListener());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        addView(mFlashModeIconTop);
        addView(mFlashModeIconBot);
    }

    private Animation.AnimationListener getSlideAnimationListener() {
        return new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                setClickable(false);
                mIcons[1].setImageResource(mNextIconRes);
                mIcons[1].setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mIcons[0].setVisibility(GONE);
                setClickable(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        };
    }

    public void switchFlashMode() {
        switch (mFlashMode) {
            case FLASH_MODE_AUTO:
                mFlashMode = FLASH_MODE_ON;
                mNextIconRes = R.drawable.flash_on;
                break;
            case FLASH_MODE_ON:
                mFlashMode = FLASH_MODE_OFF;
                mNextIconRes = R.drawable.flash_off;
                break;
            case FLASH_MODE_OFF:
                mFlashMode = FLASH_MODE_AUTO;
                mNextIconRes = R.drawable.flash_auto;
                break;
        }

        animateState();
    }

    public void switchFlashModeTo(int flashMode) {
        mFlashMode = flashMode;

        switch (flashMode) {
            case FLASH_MODE_AUTO:
                mNextIconRes = R.drawable.flash_auto;
                break;
            case FLASH_MODE_ON:
                mNextIconRes = R.drawable.flash_on;
                break;
            case FLASH_MODE_OFF:
                mNextIconRes = R.drawable.flash_off;
                break;
        }

        animateState();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setAlpha(enabled ? 1F : 0.5F);
    }

    public void hide() {
        animateGone(true);
    }

    public void show() {
        animateGone(false);
    }

    /**
     * @return ImageView[], where 0 is visible, 1 is not visible
     */
    private ImageView[] getIconsByVisibility() {
        if (mFlashModeIconBot.isShown() && mFlashModeIconTop.isShown())
            throw new IllegalStateException("Cannot show two icons at one time");

        if (mFlashModeIconTop.isShown()) return new ImageView[] { mFlashModeIconTop, mFlashModeIconBot };
        else if (mFlashModeIconBot.isShown()) return new ImageView[] { mFlashModeIconBot, mFlashModeIconTop };

        return null;
    }

    private void animateState() {
        if (!isClickable()) return;

        mIcons = getIconsByVisibility();
        if (mIcons == null) return;

        setClickable(false);
        mIcons[0].startAnimation(mAnimationToBottom);
        mIcons[1].startAnimation(mAnimationFromTop);

        if (mFlashModeListener != null)
            mFlashModeListener.onFlashModeChanged(mFlashMode);
    }

    private void animateGone(final boolean toHide) {
        animate()
                .alpha(toHide ? 0 : isEnabled() ? 1 : 0.5F)
                .setDuration(200)
                .setInterpolator(DECELERATE)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        if (!toHide) setVisibility(VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (toHide) setVisibility(GONE);
                    }
                })
                .start();
    }

    public void setFlashModeListener(OnFlashModeListener flashModeListener) {
        mFlashModeListener = flashModeListener;
    }
}