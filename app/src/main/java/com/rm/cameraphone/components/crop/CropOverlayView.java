package com.rm.cameraphone.components.crop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.rm.cameraphone.constants.ColorConstants;
import com.rm.cameraphone.events.OverlayViewChangeListener;
import com.rm.cameraphone.util.bitmap.RectUtils;

import static com.rm.cameraphone.util.DimenUtils.dp;

/**
 * Created by alex
 */
public class CropOverlayView extends View {

    public static final int DEFAULT_CROP_GRID_ROW_COUNT = 2;
    public static final int DEFAULT_CROP_GRID_COLUMN_COUNT = 2;

    private final RectF mCropViewRect = new RectF();
    private final RectF mTempRect = new RectF();

    private int mCropGridRowCount, mCropGridColumnCount;
    private float mTargetAspectRatio;
    private float[] mGridPoints = null;
    private int mDimmedColor;

    private boolean mShowCropGrid;

    private Path mCircularPath = new Path();
    private Paint mDimmedStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mCropGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mCropFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mCropFrameCornersPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    protected int mThisWidth, mThisHeight;

    protected float[] mCropGridCorners;
    private int mCurrentTouchCornerIndex = -1;
    private int mTouchPointThreshold;
    private int mCropRectMinSize;
    private int mCropRectCornerTouchAreaLineLength;

    private OverlayViewChangeListener mCallback;

    public CropOverlayView(Context context) {
        this(context, null);
    }

    public CropOverlayView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CropOverlayView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setOverlayViewChangeListener(OverlayViewChangeListener callback) {
        mCallback = callback;
    }

    public void setShowCropGrid(boolean showCropGrid) {
        mShowCropGrid = showCropGrid;
        invalidate();
    }

    public void setTargetAspectRatio(float targetAspectRatio) {
        mTargetAspectRatio = targetAspectRatio;
        setupCropBounds();
        postInvalidate();
    }

    public void setupCropBounds() {
        int height = (int) (mThisWidth / mTargetAspectRatio);
        if (height > mThisHeight) {
            int width = (int) (mThisHeight * mTargetAspectRatio);
            int halfDiff = (mThisWidth - width) / 2;
            mCropViewRect.set(getPaddingLeft() + halfDiff, getPaddingTop(),
                    getPaddingLeft() + width + halfDiff, getPaddingTop() + mThisHeight);
        } else {
            int halfDiff = (mThisHeight - height) / 2;
            mCropViewRect.set(getPaddingLeft(), getPaddingTop() + halfDiff,
                    getPaddingLeft() + mThisWidth, getPaddingTop() + height + halfDiff);
        }

        if (mCallback != null) {
            mCallback.onCropRectUpdated(mCropViewRect);
        }

        updateGridPoints();
    }

    private void updateGridPoints() {
        mCropGridCorners = RectUtils.getCornersFromRect(mCropViewRect);

        mGridPoints = null;
        mCircularPath.reset();
        mCircularPath.addOval(mCropViewRect, Path.Direction.CW);

        invalidate();
    }

    protected void init() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }

        mTouchPointThreshold = dp(30);
        mCropRectMinSize = dp(100);
        mCropRectCornerTouchAreaLineLength = dp(10);

        mDimmedColor = ColorConstants.COLOR_OVERLAY;
        mDimmedStrokePaint.setColor(mDimmedColor);
        mDimmedStrokePaint.setStyle(Paint.Style.STROKE);
        mDimmedStrokePaint.setStrokeWidth(1);

        initCropFrameStyle();
        initCropGridStyle();
    }

    private void initCropFrameStyle() {
        int cropFrameStrokeSize = dp(1);
        int cropFrameColor = Color.WHITE;
        mCropFramePaint.setStrokeWidth(cropFrameStrokeSize);
        mCropFramePaint.setColor(cropFrameColor);
        mCropFramePaint.setStyle(Paint.Style.STROKE);

        mCropFrameCornersPaint.setStrokeWidth(cropFrameStrokeSize * 3);
        mCropFrameCornersPaint.setColor(cropFrameColor);
        mCropFrameCornersPaint.setStyle(Paint.Style.STROKE);
    }


    private void initCropGridStyle() {
        int cropGridStrokeSize = dp(1);
        int cropGridColor = ColorConstants.COLOR_CROP_GRID;
        mCropGridPaint.setStrokeWidth(cropGridStrokeSize);
        mCropGridPaint.setColor(cropGridColor);

        mCropGridRowCount = DEFAULT_CROP_GRID_ROW_COUNT;
        mCropGridColumnCount = DEFAULT_CROP_GRID_COLUMN_COUNT;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            left = getPaddingLeft();
            top = getPaddingTop();
            right = getWidth() - getPaddingRight();
            bottom = getHeight() - getPaddingBottom();
            mThisWidth = right - left;
            mThisHeight = bottom - top;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDimmedLayer(canvas);
        drawCropGrid(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mCropViewRect.isEmpty()) return false;

        float x = event.getX();
        float y = event.getY();

        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
            mCurrentTouchCornerIndex = getCurrentTouchCorner(x, y);
            return mCurrentTouchCornerIndex != -1;
        }

        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
            if (event.getPointerCount() == 1 && mCurrentTouchCornerIndex != -1) {

                x = Math.min(Math.max(x, getPaddingLeft()), getWidth() - getPaddingRight());
                y = Math.min(Math.max(y, getPaddingTop()), getHeight() - getPaddingBottom());

                resizeCropViewRect(x, y);

                return true;
            }
        }

        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            mCurrentTouchCornerIndex = -1;
            if (mCallback != null) {
                mCallback.onCropRectUpdated(mCropViewRect);
            }
        }

        return false;
    }

    /**
     * Order of the corners
     * 0------->1
     * ^        |
     * |        |
     * |        v
     * 3<-------2
     */
    private void resizeCropViewRect(float touchX, float touchY) {
        mTempRect.set(mCropViewRect);

        switch (mCurrentTouchCornerIndex) {
            case 0:
                mTempRect.set(touchX, touchY, mCropViewRect.right, mCropViewRect.bottom);
                break;
            case 1:
                mTempRect.set(mCropViewRect.left, touchY, touchX, mCropViewRect.bottom);
                break;
            case 2:
                mTempRect.set(mCropViewRect.left, mCropViewRect.top, touchX, touchY);
                break;
            case 3:
                mTempRect.set(touchX, mCropViewRect.top, mCropViewRect.right, touchY);
                break;
        }

        boolean changeHeight = mTempRect.height() >= mCropRectMinSize;
        boolean changeWidth = mTempRect.width() >= mCropRectMinSize;
        mCropViewRect.set(
                changeWidth ? mTempRect.left : mCropViewRect.left,
                changeHeight ? mTempRect.top : mCropViewRect.top,
                changeWidth ? mTempRect.right : mCropViewRect.right,
                changeHeight ? mTempRect.bottom : mCropViewRect.bottom);

        if (changeHeight || changeWidth) {
            updateGridPoints();
            postInvalidate();
        }
    }

    private int getCurrentTouchCorner(float touchX, float touchY) {
        int closestPointIndex = -1;
        double closestPointDistance = mTouchPointThreshold;
        for (int i = 0; i < 8; i += 2) {
            double distanceToCorner = Math.sqrt(Math.pow(touchX - mCropGridCorners[i], 2)
                    + Math.pow(touchY - mCropGridCorners[i + 1], 2));
            if (distanceToCorner < closestPointDistance) {
                closestPointDistance = distanceToCorner;
                closestPointIndex = i / 2;
            }
        }
        return closestPointIndex;
    }

    protected void drawDimmedLayer(@NonNull Canvas canvas) {
        canvas.save();
        canvas.clipRect(mCropViewRect, Region.Op.DIFFERENCE);
        canvas.drawColor(mDimmedColor);
        canvas.restore();
    }

    protected void drawCropGrid(@NonNull Canvas canvas) {
        if (mShowCropGrid) {
            if (mGridPoints == null && !mCropViewRect.isEmpty()) {

                mGridPoints = new float[(mCropGridRowCount) * 4 + (mCropGridColumnCount) * 4];

                int index = 0;
                for (int i = 0; i < mCropGridRowCount; i++) {
                    mGridPoints[index++] = mCropViewRect.left;
                    mGridPoints[index++] = (mCropViewRect.height() * (((float) i + 1.0f) / (float) (mCropGridRowCount + 1))) + mCropViewRect.top;
                    mGridPoints[index++] = mCropViewRect.right;
                    mGridPoints[index++] = (mCropViewRect.height() * (((float) i + 1.0f) / (float) (mCropGridRowCount + 1))) + mCropViewRect.top;
                }

                for (int i = 0; i < mCropGridColumnCount; i++) {
                    mGridPoints[index++] = (mCropViewRect.width() * (((float) i + 1.0f) / (float) (mCropGridColumnCount + 1))) + mCropViewRect.left;
                    mGridPoints[index++] = mCropViewRect.top;
                    mGridPoints[index++] = (mCropViewRect.width() * (((float) i + 1.0f) / (float) (mCropGridColumnCount + 1))) + mCropViewRect.left;
                    mGridPoints[index++] = mCropViewRect.bottom;
                }
            }
        }

        if (mGridPoints != null) {
            canvas.drawLines(mGridPoints, mCropGridPaint);
        }

        canvas.drawRect(mCropViewRect, mCropFramePaint);

        canvas.save();

        mTempRect.set(mCropViewRect);
        mTempRect.inset(mCropRectCornerTouchAreaLineLength, -mCropRectCornerTouchAreaLineLength);
        canvas.clipRect(mTempRect, Region.Op.DIFFERENCE);

        mTempRect.set(mCropViewRect);
        mTempRect.inset(-mCropRectCornerTouchAreaLineLength, mCropRectCornerTouchAreaLineLength);
        canvas.clipRect(mTempRect, Region.Op.DIFFERENCE);

        canvas.drawRect(mCropViewRect, mCropFrameCornersPaint);

        canvas.restore();
    }
}