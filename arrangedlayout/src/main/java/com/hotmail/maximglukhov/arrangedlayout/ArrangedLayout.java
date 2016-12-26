package com.hotmail.maximglukhov.arrangedlayout;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by maxim on 26-Dec-16.
 */

public class ArrangedLayout extends ViewGroup {

    public ArrangedLayout(Context context) {
        this(context, null, 0);
    }

    public ArrangedLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArrangedLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public ArrangedLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int paddingStart = ViewCompat.getPaddingStart(this);
        int paddingEnd = ViewCompat.getPaddingEnd(this);
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int childStart = paddingStart;
        int childTop = paddingTop;

        int childWidth;
        int childHeight;

        int highestView = 0;
        int availableWidth = right - left;

        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);

            // Skip views with GONE visibility.
            if (childView.getVisibility() == View.GONE) {
                continue;
            }

            childWidth = childView.getMeasuredWidth();
            childHeight = childView.getMeasuredHeight();

            highestView = Math.max(childHeight, highestView);
            if (childWidth + childStart + paddingStart > availableWidth) {
                childStart = paddingStart;
                childTop += paddingTop + paddingBottom + highestView;
                highestView = childHeight;
            }

            if (ViewCompat.getLayoutDirection(this) == LAYOUT_DIRECTION_LTR) {
                // Layout for Left-To-Right
                childView.layout(childStart, childTop,
                        childStart + childWidth, childTop + childHeight);
            } else {
                // Layout for Right-To-Left
                childView.layout(childStart + childWidth,
                        childTop, childStart, childTop + childHeight);
            }

            childStart += childWidth + paddingStart + paddingEnd;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int paddingStart = ViewCompat.getPaddingStart(this);
        int paddingEnd = ViewCompat.getPaddingEnd(this);
        int horizontalPadding = paddingStart + paddingEnd;

        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();
        int verticalPadding = paddingTop + paddingBottom;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int maxWidth = widthSize;
        int maxHeight = heightSize;

        int childrenWidth = horizontalPadding;
        int childHeight;

        int highestChild = 0;

        int totalWidth = childrenWidth;
        int totalHeight = verticalPadding;

        // Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            // Must be this size
            maxWidth = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            // Lower of both
            maxWidth = Math.min(0, widthSize);
        } else {
            // No limitations (whatever our contents are).
            maxWidth = 0;
        }

        // Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            // Must be this size
            maxHeight = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            // Lower of both
            maxHeight = Math.min(0, heightSize);
        } else {
            // No limitations (whatever our contents are).
            maxHeight = 0;
        }

        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);

            // Skip views with GONE visibility.
            if (childView.getVisibility() == GONE)
                continue;

            // Let the child measure itself
            childView.measure(
                    getChildMeasureSpec(widthMeasureSpec, paddingStart + paddingEnd,
                            childView.getLayoutParams().width),
                    getChildMeasureSpec(heightMeasureSpec, paddingTop + paddingBottom,
                            childView.getLayoutParams().height));

            // Add to total width calculation
            // We don't add to total height calculation because we only do that when going down one row.
            totalWidth += childView.getMeasuredWidth();

            if (widthMode == MeasureSpec.AT_MOST) {
                // Update the maxWidth if it's smaller than the maximum given width.
                maxWidth = Math.min(totalWidth, widthSize);
            } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                // No width restriction.
                maxWidth = totalWidth;
            }

            childHeight = childView.getMeasuredHeight();

            if (childHeight > highestChild) {
                // Remove previous calculated highest child
                totalHeight -= highestChild;
                // Update highest child
                highestChild = childHeight;
                // Add updated calculated highest child.
                totalHeight += highestChild;
            }

            if (childrenWidth + childView.getMeasuredWidth() > maxWidth) {
                // Reset the width measure
                childrenWidth = horizontalPadding;
                // Add to total height calculation the highest child again because we always remove it from calculation
                // when we encounter new higher child.
                totalHeight += highestChild;
                // Reset highest child for next row
                highestChild = childHeight;
            }

            childrenWidth += childView.getMeasuredWidth();

            if (heightMode == MeasureSpec.AT_MOST) {
                // Update the maxHeight if it's smaller than the maximum given height.
                maxHeight = Math.min(totalHeight, heightSize);
            } else if (heightMode == MeasureSpec.UNSPECIFIED) {
                // No height restriction.
                maxHeight = totalHeight;
            }
        }

        setMeasuredDimension(maxWidth, maxHeight);
    }
}
