package com.quarkworks.apartmentgroceries.grocery;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class PagerContainer extends FrameLayout implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    boolean needsRedraw = false;

    public PagerContainer(Context context) {
        super(context);
        init();
    }

    public PagerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PagerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setClipChildren(false);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onFinishInflate() {
        try {
            viewPager = (ViewPager) getChildAt(0);
            viewPager.setOnPageChangeListener(this);
        } catch (Exception e) {
            throw new IllegalStateException("The root child of PagerContainer must be a ViewPager");
        }
        super.onFinishInflate();
    }

    private Point center = new Point();
    private Point initialTouch = new Point();

    @Override
    protected void onSizeChanged(int w, int h, int oldWidth, int oldHeight) {
        center.x = w / 2;
        center.y = h / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                int delta = isInNonTappableRegion(initialTouch.x, motionEvent.getX());
                if (delta != 0) {
                    motionEvent.setAction(MotionEvent.ACTION_CANCEL);
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + delta );
                }
                motionEvent.offsetLocation(center.x - initialTouch.x, center.y - initialTouch.y);
                break;
            case MotionEvent.ACTION_DOWN:
                initialTouch.x = (int)motionEvent.getX();
                initialTouch.y = (int)motionEvent.getY();
                break;
            default:
                motionEvent.offsetLocation(center.x - initialTouch.x, center.y - initialTouch.y);
                break;
        }

        return viewPager.dispatchTouchEvent(motionEvent);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (needsRedraw) invalidate();
    }

    @Override
    public void onPageSelected(int position) {
        FrameLayout.LayoutParams viewPagerLayoutParams
                = (FrameLayout.LayoutParams) viewPager.getLayoutParams();
        if (position == 0) {
            viewPagerLayoutParams.gravity = Gravity.START;
        } else if (viewPager.getAdapter().getCount() - 1 == position) {
            viewPagerLayoutParams.gravity = Gravity.END;
        } else {
            viewPagerLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        }
        viewPager.requestLayout();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        needsRedraw = (state != ViewPager.SCROLL_STATE_IDLE);
    }

    private int isInNonTappableRegion(float oldX, float newX) {
        int tappableWidth = viewPager.getWidth();
        int totalWidth = getWidth();
        int nonTappableWidth = (totalWidth - tappableWidth) / 2;
        if (oldX < nonTappableWidth && newX < nonTappableWidth) {
            return -(int) Math.ceil((nonTappableWidth - newX) / (float) tappableWidth);
        }
        nonTappableWidth = (totalWidth + tappableWidth) / 2;
        if (oldX > nonTappableWidth && newX > nonTappableWidth) {
            return (int) Math.ceil((newX - nonTappableWidth) / (float) tappableWidth);
        }
        return 0;
    }
}
