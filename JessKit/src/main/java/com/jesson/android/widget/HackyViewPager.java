package com.jesson.android.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Hacky fix for Issue #4 and
 * http://code.google.com/p/android/issues/detail?id=18990
 * <p/>
 * ScaleGestureDetector seems to mess up the touch events, which means that
 * ViewGroups which make use of onInterceptTouchEvent throw a lot of
 * IllegalArgumentException: pointerIndex out of range.
 * <p/>
 * There's not much I can do in my code for now, but we can mask the result by
 * just catching the problem and ignoring it.
 *
 * @author Chris Banes
 */
public class HackyViewPager extends ViewPager {

    public HackyViewPager(Context context) {
        super(context);
        initialize();
    }

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        // Set the page transformer to perform the transition animation
        // for each page in the view.
        setPageTransformer(true, new PageTransformer() {
            @Override
            public void transformPage(View page, float position) {

                // The >= 1 is needed so that the page
                // (page A) that transitions behind the newly visible
                // page (page B) that comes in from the left does not
                // get the touch events because it is still on screen
                // (page A is still technically on screen despite being
                // invisible). This makes sure that when the transition
                // has completely finished, we revert it to its default
                // behavior and move it off of the screen.
                if (position < 0 || position >= 1.f) {
                    page.setTranslationX(0);
                    page.setAlpha(1.f);
                    page.setScaleX(1);
                    page.setScaleY(1);
                } else {
                    page.setTranslationX(-position * page.getWidth());
                    page.setAlpha(Math.max(0, 1.f - position));
                    final float scale = Math.max(0, 1.f - position * 0.3f);
                    page.setScaleX(scale);
                    page.setScaleY(scale);
                }
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

}