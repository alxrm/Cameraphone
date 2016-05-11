package com.rm.cameraphone.components.crop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.rm.cameraphone.events.CropBoundsChangeListener;
import com.rm.cameraphone.util.Animators;
import com.rm.cameraphone.util.DispatchUtils;
import com.rm.cameraphone.util.FileUtils;
import com.rm.cameraphone.util.SharedMap;
import com.rm.cameraphone.util.bitmap.Bitmaps;
import com.rm.cameraphone.util.bitmap.RectUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import static com.rm.cameraphone.constants.SharedMapConstants.KEY_CAMERA_CROP_RESULT;

/**
 * Created by alex
 */
public class CroppingImageView extends MatrixImageView {

    public static final int DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION = 500;
    public static final float DEFAULT_MAX_SCALE_MULTIPLIER = 10.0f;
    public static final float SOURCE_IMAGE_ASPECT_RATIO = 0f;

    private final RectF mCropRect = new RectF();

    private final Matrix mTempMatrix = new Matrix();

    private float mTargetAspectRatio;

    private CropBoundsChangeListener mCropBoundsChangeListener;

    private Runnable mWrapCropBoundsRunnable, mZoomImageToPositionRunnable = null;

    private float mMaxScale, mMinScale;

    public CroppingImageView(Context context) {
        this(context, null);
    }

    public CroppingImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CroppingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void cropAndSaveImage(final Runnable mainThreadCallback) {
        cancelAllAnimations();
        setImageToWrapCropBounds(false);

        DispatchUtils.getCropQueue().postRunnable(new Runnable() {
            @Override
            public void run() {
                File output = new File(getImageOutputPath());

                RectF imageCorners = RectUtils.trapToRectF(mCurrentImageCorners);
                float scale = getCurrentScale();
                float angle = getCurrentAngle();

                final Bitmap res = Bitmaps.cropAndRotate(getViewBitmap(), mCropRect, imageCorners, scale, angle);

                FileUtils.writeBitmapToDevice(res, output);
                SharedMap.holder().put(KEY_CAMERA_CROP_RESULT, new WeakReference<Object>(output.toString()));

                DispatchUtils.runOnUiThread(mainThreadCallback);
            }
        });
    }

    public float getMaxScale() {
        return mMaxScale;
    }

    public float getMinScale() {
        return mMinScale;
    }

    /**
     * @return - aspect ratio for crop bounds
     */
    public float getTargetAspectRatio() {
        return mTargetAspectRatio;
    }

    public void setCropRect(RectF cropRect) {
        mCropRect.set(cropRect.left - getPaddingLeft(), cropRect.top - getPaddingTop(),
                cropRect.right - getPaddingRight(), cropRect.bottom - getPaddingBottom());
        calculateImageScaleBounds();
        setImageToWrapCropBounds();
    }

    public void setCropBoundsChangeListener(@Nullable CropBoundsChangeListener cropBoundsChangeListener) {
        mCropBoundsChangeListener = cropBoundsChangeListener;
    }

    public void zoomOutImage(float deltaScale) {
        zoomOutImage(deltaScale, mCropRect.centerX(), mCropRect.centerY());
    }

    public void zoomOutImage(float scale, float centerX, float centerY) {
        if (scale >= getMinScale()) {
            postScale(scale / getCurrentScale(), centerX, centerY);
        }
    }

    public void zoomInImage(float deltaScale) {
        zoomInImage(deltaScale, mCropRect.centerX(), mCropRect.centerY());
    }

    public void zoomInImage(float scale, float centerX, float centerY) {
        if (scale <= getMaxScale()) {
            postScale(scale / getCurrentScale(), centerX, centerY);
        }
    }

    public void postScale(float deltaScale, float px, float py) {
        if (deltaScale > 1 && getCurrentScale() * deltaScale <= getMaxScale()) {
            super.postScale(deltaScale, px, py);
        } else if (deltaScale < 1 && getCurrentScale() * deltaScale >= getMinScale()) {
            super.postScale(deltaScale, px, py);
        }
    }

    public void postRotate(float deltaAngle) {
        postRotate(deltaAngle, mCropRect.centerX(), mCropRect.centerY());
    }

    public void cancelAllAnimations() {
        removeCallbacks(mWrapCropBoundsRunnable);
        removeCallbacks(mZoomImageToPositionRunnable);
    }

    public void setImageToWrapCropBounds() {
        setImageToWrapCropBounds(true);
    }

    public void setImageToWrapCropBounds(boolean animate) {
        if (mBitmapLaidOut && !isImageWrapCropBounds()) {

            float currentX = mCurrentImageCenter[0];
            float currentY = mCurrentImageCenter[1];
            float currentScale = getCurrentScale();

            float deltaX = mCropRect.centerX() - currentX;
            float deltaY = mCropRect.centerY() - currentY;
            float deltaScale = 0;

            mTempMatrix.reset();
            mTempMatrix.setTranslate(deltaX, deltaY);

            final float[] tempCurrentImageCorners = Arrays.copyOf(mCurrentImageCorners, mCurrentImageCorners.length);
            mTempMatrix.mapPoints(tempCurrentImageCorners);

            boolean willImageWrapCropBoundsAfterTranslate = isImageWrapCropBounds(tempCurrentImageCorners);

            if (willImageWrapCropBoundsAfterTranslate) {
                final float[] imageIndents = calculateImageIndents();
                deltaX = -(imageIndents[0] + imageIndents[2]);
                deltaY = -(imageIndents[1] + imageIndents[3]);
            } else {
                RectF tempCropRect = new RectF(mCropRect);
                mTempMatrix.reset();
                mTempMatrix.setRotate(getCurrentAngle());
                mTempMatrix.mapRect(tempCropRect);

                final float[] currentImageSides = RectUtils.getRectSidesFromCorners(mCurrentImageCorners);

                deltaScale = Math.max(tempCropRect.width() / currentImageSides[0],
                        tempCropRect.height() / currentImageSides[1]);
                deltaScale = deltaScale * currentScale - currentScale;
            }

            if (animate) {
                post(mWrapCropBoundsRunnable = new WrapCropBoundsRunnable(
                        CroppingImageView.this, DEFAULT_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, currentX, currentY, deltaX, deltaY,
                        currentScale, deltaScale, willImageWrapCropBoundsAfterTranslate));
            } else {
                postTranslate(deltaX, deltaY);
                if (!willImageWrapCropBoundsAfterTranslate) {
                    zoomInImage(currentScale + deltaScale, mCropRect.centerX(), mCropRect.centerY());
                }
            }
        }
    }

    private float[] calculateImageIndents() {
        mTempMatrix.reset();
        mTempMatrix.setRotate(-getCurrentAngle());

        float[] unrotatedImageCorners = Arrays.copyOf(mCurrentImageCorners, mCurrentImageCorners.length);
        float[] unrotatedCropBoundsCorners = RectUtils.getCornersFromRect(mCropRect);

        mTempMatrix.mapPoints(unrotatedImageCorners);
        mTempMatrix.mapPoints(unrotatedCropBoundsCorners);

        RectF unrotatedImageRect = RectUtils.trapToRectF(unrotatedImageCorners);
        RectF unrotatedCropRect = RectUtils.trapToRectF(unrotatedCropBoundsCorners);

        float deltaLeft = unrotatedImageRect.left - unrotatedCropRect.left;
        float deltaTop = unrotatedImageRect.top - unrotatedCropRect.top;
        float deltaRight = unrotatedImageRect.right - unrotatedCropRect.right;
        float deltaBottom = unrotatedImageRect.bottom - unrotatedCropRect.bottom;

        float indents[] = new float[4];
        indents[0] = (deltaLeft > 0) ? deltaLeft : 0;
        indents[1] = (deltaTop > 0) ? deltaTop : 0;
        indents[2] = (deltaRight < 0) ? deltaRight : 0;
        indents[3] = (deltaBottom < 0) ? deltaBottom : 0;

        mTempMatrix.reset();
        mTempMatrix.setRotate(getCurrentAngle());
        mTempMatrix.mapPoints(indents);

        return indents;
    }

    /**
     * When image is laid out it must be centered properly to fit current crop bounds.
     */
    @Override
    protected void onImageLaidOut() {
        super.onImageLaidOut();
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        float drawableWidth = drawable.getIntrinsicWidth();
        float drawableHeight = drawable.getIntrinsicHeight();

        if (mTargetAspectRatio == SOURCE_IMAGE_ASPECT_RATIO) {
            mTargetAspectRatio = drawableWidth / drawableHeight;
        }

        int height = (int) (mThisWidth / mTargetAspectRatio);
        if (height > mThisHeight) {
            int width = (int) (mThisHeight * mTargetAspectRatio);
            int halfDiff = (mThisWidth - width) / 2;
            mCropRect.set(halfDiff, 0, width + halfDiff, mThisHeight);
        } else {
            int halfDiff = (mThisHeight - height) / 2;
            mCropRect.set(0, halfDiff, mThisWidth, height + halfDiff);
        }

        calculateImageScaleBounds(drawableWidth, drawableHeight);
        setupInitialImagePosition(drawableWidth, drawableHeight);

        if (mCropBoundsChangeListener != null) {
            mCropBoundsChangeListener.onCropAspectRatioChanged(mTargetAspectRatio);
        }
        if (mTransformImageListener != null) {
            mTransformImageListener.onScale(getCurrentScale());
            mTransformImageListener.onRotate(getCurrentAngle());
        }
    }

    /**
     * This method checks whether current image fills the crop bounds.
     */
    protected boolean isImageWrapCropBounds() {
        return isImageWrapCropBounds(mCurrentImageCorners);
    }

    protected boolean isImageWrapCropBounds(float[] imageCorners) {
        mTempMatrix.reset();
        mTempMatrix.setRotate(-getCurrentAngle());

        float[] unrotatedImageCorners = Arrays.copyOf(imageCorners, imageCorners.length);
        mTempMatrix.mapPoints(unrotatedImageCorners);

        float[] unrotatedCropBoundsCorners = RectUtils.getCornersFromRect(mCropRect);
        mTempMatrix.mapPoints(unrotatedCropBoundsCorners);

        return RectUtils.trapToRectF(unrotatedImageCorners).contains(RectUtils.trapToRectF(unrotatedCropBoundsCorners));
    }

    protected void zoomImageToPosition(float scale, float centerX, float centerY, long durationMs) {
        if (scale > getMaxScale()) {
            scale = getMaxScale();
        }

        final float oldScale = getCurrentScale();
        final float deltaScale = scale - oldScale;

        post(mZoomImageToPositionRunnable = new ZoomImageToPosition(CroppingImageView.this,
                durationMs, oldScale, deltaScale, centerX, centerY));
    }

    private void calculateImageScaleBounds() {
        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }
        calculateImageScaleBounds(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    }

    private void calculateImageScaleBounds(float drawableWidth, float drawableHeight) {
        float widthScale = mCropRect.width() / drawableWidth;
        float heightScale = mCropRect.height() / drawableHeight;

        mMinScale = Math.max(widthScale, heightScale);
        mMaxScale = mMinScale * DEFAULT_MAX_SCALE_MULTIPLIER;
    }

    private void setupInitialImagePosition(float drawableWidth, float drawableHeight) {
        float cropRectWidth = mCropRect.width();
        float cropRectHeight = mCropRect.height();

        float tw = (cropRectWidth - drawableWidth * mMinScale) / 2.0f + mCropRect.left;
        float th = (cropRectHeight - drawableHeight * mMinScale) / 2.0f + mCropRect.top;

        mCurrentImageMatrix.reset();
        mCurrentImageMatrix.postScale(mMinScale, mMinScale);
        mCurrentImageMatrix.postTranslate(tw, th);
        setImageMatrix(mCurrentImageMatrix);
    }

    private static class WrapCropBoundsRunnable implements Runnable {

        private final WeakReference<CroppingImageView> mCropImageView;

        private final long mDurationMs, mStartTime;
        private final float mOldX, mOldY;
        private final float mCenterDiffX, mCenterDiffY;
        private final float mOldScale;
        private final float mDeltaScale;
        private final boolean mWillBeImageInBoundsAfterTranslate;

        public WrapCropBoundsRunnable(CroppingImageView cropImageView,
                                      long durationMs,
                                      float oldX, float oldY,
                                      float centerDiffX, float centerDiffY,
                                      float oldScale, float deltaScale,
                                      boolean willBeImageInBoundsAfterTranslate) {

            mCropImageView = new WeakReference<>(cropImageView);

            mDurationMs = durationMs;
            mStartTime = System.currentTimeMillis();
            mOldX = oldX;
            mOldY = oldY;
            mCenterDiffX = centerDiffX;
            mCenterDiffY = centerDiffY;
            mOldScale = oldScale;
            mDeltaScale = deltaScale;
            mWillBeImageInBoundsAfterTranslate = willBeImageInBoundsAfterTranslate;
        }

        @Override
        public void run() {
            CroppingImageView cropImageView = mCropImageView.get();
            if (cropImageView == null) {
                return;
            }

            long now = System.currentTimeMillis();
            float currentMs = Math.min(mDurationMs, now - mStartTime);

            float newX = Animators.easeOut(currentMs, 0, mCenterDiffX, mDurationMs);
            float newY = Animators.easeOut(currentMs, 0, mCenterDiffY, mDurationMs);
            float newScale = Animators.easeInOut(currentMs, 0, mDeltaScale, mDurationMs);

            if (currentMs < mDurationMs) {
                cropImageView.postTranslate(newX - (cropImageView.mCurrentImageCenter[0] - mOldX), newY - (cropImageView.mCurrentImageCenter[1] - mOldY));
                if (!mWillBeImageInBoundsAfterTranslate) {
                    cropImageView.zoomInImage(mOldScale + newScale, cropImageView.mCropRect.centerX(), cropImageView.mCropRect.centerY());
                }
                if (!cropImageView.isImageWrapCropBounds()) {
                    cropImageView.post(this);
                }
            }
        }
    }

    private static class ZoomImageToPosition implements Runnable {

        private final WeakReference<CroppingImageView> mCropImageView;

        private final long mDurationMs, mStartTime;
        private final float mOldScale;
        private final float mDeltaScale;
        private final float mDestX;
        private final float mDestY;

        public ZoomImageToPosition(CroppingImageView cropImageView,
                                   long durationMs,
                                   float oldScale, float deltaScale,
                                   float destX, float destY) {

            mCropImageView = new WeakReference<>(cropImageView);

            mStartTime = System.currentTimeMillis();
            mDurationMs = durationMs;
            mOldScale = oldScale;
            mDeltaScale = deltaScale;
            mDestX = destX;
            mDestY = destY;
        }

        @Override
        public void run() {
            CroppingImageView cropImageView = mCropImageView.get();
            if (cropImageView == null) {
                return;
            }

            long now = System.currentTimeMillis();
            float currentMs = Math.min(mDurationMs, now - mStartTime);
            float newScale = Animators.easeInOut(currentMs, 0, mDeltaScale, mDurationMs);

            if (currentMs < mDurationMs) {
                cropImageView.zoomInImage(mOldScale + newScale, mDestX, mDestY);
                cropImageView.post(this);
            } else {
                cropImageView.setImageToWrapCropBounds();
            }
        }

    }

}