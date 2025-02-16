package com.baikas.sporthub6.customclasses;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;

import com.baikas.sporthub6.R;

public class ExpandableLayout extends CardView {
    private ViewGroup contentLayout;
    private View arrow;

    private boolean expanded = false;
    private boolean animating = false;
    private long animDuration = 600L;

    private ValueAnimator expandAnimator;

    public ExpandableLayout(Context context) {
        this(context, null);
    }

    public ExpandableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        expandAnimator = ValueAnimator.ofFloat(0f, 1f);
        expandAnimator.setDuration(animDuration);
        expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float progress = (float) animation.getAnimatedValue();
                int wrapContentHeight = measureWrapContentHeight();
                contentLayout.getLayoutParams().height = (int) (wrapContentHeight * progress);
                contentLayout.requestLayout();

                arrow.setRotation(progress * 180);
            }
        });
        expandAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                animating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animating = false;
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (animating) {
                    expandAnimator.reverse();
                    expanded = !expanded;
                } else if (expanded) {
                    expandAnimator.reverse();
                    expanded = false;
                } else {
                    expandAnimator.start();
                    expanded = true;
                }
            }
        });
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ViewGroup parentLayout = (ViewGroup) getChildAt(0);

        contentLayout = (ViewGroup) parentLayout.getChildAt(1);
        arrow = parentLayout.findViewById(R.id.arrow);

        if (expanded) {
            arrow.setRotation(180f);
        } else {
            contentLayout.getLayoutParams().height = 0;
            arrow.setRotation(0f);
        }
    }

    private int measureWrapContentHeight() {
        contentLayout.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        );
        return contentLayout.getMeasuredHeight();
    }
}
