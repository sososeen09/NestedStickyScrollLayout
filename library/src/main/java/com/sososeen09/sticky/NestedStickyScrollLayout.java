package com.sososeen09.sticky;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.OverScroller;


/**
 * @author sososeen09
 */

public class NestedStickyScrollLayout extends LinearLayout implements NestedScrollingParent, ScrollingView {

    private static final String TAG = "NestedStickyScrollLayout";
    private View mStickyView;
    private ViewGroup mScrollView;
    private int mTopViewHeight = -1;
    private OverScroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMaximumVelocity, mMinimumVelocity;
    private float mLastY;
    private boolean mDragging;
    private int mStickyViewID;
    private int mScrollSourceViewID;
    private int mStickyViewIndex;
    private boolean mIsTopHidden = false;
    private int mTempHeight;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private int mContentHeight;

    public NestedStickyScrollLayout(Context context) {
        this(context, null);
    }

    public NestedStickyScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NestedStickyScrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.NestedStickyScrollLayout);
        mStickyViewID = typedArray.getResourceId(R.styleable.NestedStickyScrollLayout_sticky_view, 0);
        mScrollSourceViewID = typedArray.getResourceId(R.styleable.NestedStickyScrollLayout_scroll_source_view, 0);
        typedArray.recycle();
        mScroller = new OverScroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMaximumVelocity = ViewConfiguration.get(context)
                .getScaledMaximumFlingVelocity();
        mMinimumVelocity = ViewConfiguration.get(context)
                .getScaledMinimumFlingVelocity();
        setOrientation(VERTICAL);
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        //返回True才能接收到子控件Scroll状态
        return true;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(@NonNull View child) {
        mNestedScrollingParentHelper.onStopNestedScroll(child);
    }

    @Override
    public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        //dy大于0代表手指向上滑动
        if (Math.abs(dx) > Math.abs(dy)) {
            return;
        }
        boolean hiddenTop = dy > 0 && getScrollY() < mTopViewHeight;
        //如果不加ViewCompat.canScrollVertically(target, -1)判断，ScrollView自身不会滚动
        boolean showTop = dy < 0 && getScrollY() >= 0 && !ViewCompat.canScrollVertically(target, -1);

        if (hiddenTop || showTop) {
            scrollBy(0, dy);
            consumed[1] = dy;
        }
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
        if (Math.abs(velocityX) > Math.abs(velocityY)) {
            return false;
        }
        //当mScrollY的值大于TopView的高度就不再拦截了
        if (getScrollY() >= mTopViewHeight) {
            return false;
        }
        fling((int) velocityY);
        return true;
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }


    /**
     * <p>The scroll range of a scroll view is the overall height of all of its
     * children.</p>
     */
    @Override
    public int computeVerticalScrollRange() {
        final int count = getChildCount();
        final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        if (count == 0) {
            return contentHeight;
        }

        int scrollRange = getChildAt(0).getBottom();
        final int scrollY = getScrollY();
        final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }

        return scrollRange;
    }

    @Override
    public int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    @Override
    public int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    @Override
    public int computeHorizontalScrollRange() {
        return super.computeHorizontalScrollRange();
    }

    @Override
    public int computeHorizontalScrollOffset() {
        return super.computeHorizontalScrollOffset();
    }

    @Override
    public int computeHorizontalScrollExtent() {
        return super.computeHorizontalScrollExtent();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (!isInEditMode()) {
            mTempHeight = 0;
            for (int i = 0; i < mStickyViewIndex; i++) {
                //防止某些情况下顶部View的高度设置为wrap_content的时候测量错误
                View childAt = getChildAt(i);
                if (childAt.getVisibility() == GONE) {
                    continue;
                }
                mTempHeight += childAt.getMeasuredHeight();

                final MarginLayoutParams lp = (MarginLayoutParams) childAt.getLayoutParams();
                final int childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                        getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin
                                + mTempHeight, lp.height);
                int mode = MeasureSpec.getMode(childHeightMeasureSpec);
                if (mode == MeasureSpec.AT_MOST) {
                    childAt.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                }
            }
            if (mScrollView.getVisibility() != GONE) {
                LayoutParams params = (LayoutParams) mScrollView.getLayoutParams();
                params.height = getMeasuredHeight() - mStickyView.getMeasuredHeight();
                mScrollView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.AT_MOST));
            }
            setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight());

            mTopViewHeight = 0;
            for (int i = 0; i < mStickyViewIndex; i++) {
                View childAt = getChildAt(i);
                if (childAt.getVisibility() == GONE) {
                    continue;
                }
                final MarginLayoutParams layoutParams = (MarginLayoutParams) childAt.getLayoutParams();
                mTopViewHeight += childAt.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            }
            mTopViewHeight += ((MarginLayoutParams) mStickyView.getLayoutParams()).topMargin;
//            Log.d(TAG, "onMeasure: mTopViewHeight: " + mTopViewHeight + "  mTempHeight: " + mTempHeight);
            mContentHeight = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                if (childAt.getVisibility() == GONE) {
                    continue;
                }

                final MarginLayoutParams layoutParams = (MarginLayoutParams) childAt.getLayoutParams();
                mContentHeight += childAt.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //计算所有子View的高度
        mContentHeight = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() == GONE) {
                continue;
            }

            final MarginLayoutParams layoutParams = (MarginLayoutParams) childAt.getLayoutParams();
            mContentHeight += childAt.getMeasuredHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mStickyView = findViewById(mStickyViewID);
        mScrollView = (ViewGroup) findViewById(mScrollSourceViewID);
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) == mStickyView) {
                mStickyViewIndex = i;
                break;
            }
        }
    }


    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        initVelocityTrackerIfNotExists();
        mVelocityTracker.addMovement(event);
        int action = event.getAction();
        float y = event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mLastY;
                if (!mDragging && Math.abs(dy) > mTouchSlop) {
                    mDragging = true;
                }
                if (mDragging) {
                    if (dy < 0 && getScrollY() >= mContentHeight - getHeight()) {
                        return super.onTouchEvent(event);
                    } else {
                        scrollBy(0, (int) -dy);
                    }
                }

                mLastY = y;
                break;
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                recycleVelocityTracker();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                break;
            case MotionEvent.ACTION_UP:
                mDragging = false;
                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityY = (int) mVelocityTracker.getYVelocity();
                if (Math.abs(velocityY) > mMinimumVelocity) {
                    fling(-velocityY);
                }
                recycleVelocityTracker();
                break;

            default:
                break;

        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 获取相对于屏幕左上角的 x 坐标值
        float rawX = ev.getRawX();
        // 获取相对于屏幕左上角的 y 坐标值
        float rawY = ev.getRawY();
        RectF rect = calcViewScreenLocation(mScrollView);
        boolean isInViewRect = rect.contains(rawX, rawY);
        if (isInViewRect) {
            return super.onInterceptTouchEvent(ev);
        }
        int action = ev.getAction();
        float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float dy = y - mLastY;
                if (Math.abs(dy) > mTouchSlop) {
                    mDragging = true;
                    if (!mIsTopHidden || nestedScrollEnable(dy)) {
                        return true;
                    }

                }
                break;
            default:
                break;

        }
        return super.onInterceptTouchEvent(ev);
    }


    private RectF calcViewScreenLocation(View view) {
        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + view.getWidth(),
                location[1] + view.getHeight());
    }


    protected boolean nestedScrollEnable(float dy) {
        return false;
    }

    private void fling(int velocityY) {
        mScroller.fling(0, getScrollY(), 0, velocityY, 0, 0, 0, mContentHeight - getHeight());
        invalidate();
    }

    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > mTopViewHeight) {
            y = mTopViewHeight;
        }
        if (y != getScrollY()) {
            super.scrollTo(x, y);
        }
        mIsTopHidden = getScrollY() == mTopViewHeight;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            invalidate();
        }
    }

    public void scrollToBottom() {
        scrollTo(0, mContentHeight - getHeight());
    }

    protected ViewGroup getScrollSourceView() {
        return mScrollView;
    }
}
