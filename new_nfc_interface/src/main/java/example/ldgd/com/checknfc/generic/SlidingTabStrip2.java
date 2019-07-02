package example.ldgd.com.checknfc.generic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by ldgd on 2019/3/15.
 * 功能：
 * 说明：
 */

public class SlidingTabStrip2  extends LinearLayout {
    //选项卡底部下划线的高度常量，高度不要超过下划线高度，不然会覆盖
    private static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 6;
    //选项卡底部长线的默认颜色透明度
    private static final byte DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26;
    //选项卡指示下划线的高度常量
    private static final int SELECTED_INDICATOR_THICKNESS_DIPS = 6;
   //  private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5;
     private static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF1899A2;


    private static final int DEFAULT_DIVIDER_THICKNESS_DIPS = 1;
    private static final byte DEFAULT_DIVIDER_COLOR_ALPHA = 0x20;
    private static final float DEFAULT_DIVIDER_HEIGHT = 0.5f;

    //底部下划线
    private final int mBottomBorderThickness;
    private final Paint mBottomBorderPaint;

    //指示下划线高度
    private final int mSelectedIndicatorThickness;
    private final Paint mSelectedIndicatorPaint;

    private final int mDefaultBottomBorderColor;

    private final Paint mDividerPaint;
 //   private final float mDividerHeight;

    private int mSelectedPosition;
    private float mSelectionOffset;

    private SlidingTabLayout.TabColorizer mCustomTabColorizer;
    private final SimpleTabColorizer mDefaultTabColorizer;

    public SlidingTabStrip2(Context context) {
        this(context, null);
    }

    SlidingTabStrip2(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        // 获取手机屏幕参数,density就是屏幕的密度
        final float density = getResources().getDisplayMetrics().density;

        // 这个类是工具类,作为一个动态容器，它存放一些数据值，这些值主要是resource中的值。
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.colorBackground, outValue, true);
        final int themeForegroundColor =  outValue.data;

        mDefaultBottomBorderColor = setColorAlpha(themeForegroundColor, DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);

        mDefaultTabColorizer = new SimpleTabColorizer();
        mDefaultTabColorizer.setIndicatorColors(DEFAULT_SELECTED_INDICATOR_COLOR);
        mDefaultTabColorizer.setDividerColors(setColorAlpha(themeForegroundColor, DEFAULT_DIVIDER_COLOR_ALPHA));
    //    mDefaultTabColorizer.setDividerColors(getResources().getColor(R.color.indicatorColors), DEFAULT_DIVIDER_COLOR_ALPHA);

        mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density);
        mBottomBorderPaint = new Paint();
       // mBottomBorderPaint.setColor( setColorAlpha(R.color.light_blue, DEFAULT_BOTTOM_BORDER_COLOR_ALPHA));
        mBottomBorderPaint.setColor(Color.argb(230, 175, 200, 201));
      //  mBottomBorderPaint.setColor(mDefaultBottomBorderColor);
        //mBottomBorderPaint.setColor(getResources().getColor(R.color.indicatorColors));
      //  mBottomBorderPaint.setColor(mDefaultBottomBorderColor);

        mSelectedIndicatorThickness = (int) (SELECTED_INDICATOR_THICKNESS_DIPS * density);
        mSelectedIndicatorPaint = new Paint();

       // mDividerHeight = DEFAULT_DIVIDER_HEIGHT;
        mDividerPaint = new Paint();
        mDividerPaint.setStrokeWidth((int) (DEFAULT_DIVIDER_THICKNESS_DIPS * density));
    }

   public void setCustomTabColorizer(SlidingTabLayout.TabColorizer customTabColorizer) {
        mCustomTabColorizer = customTabColorizer;
        invalidate();
    }

    public void setSelectedIndicatorColors(int... colors) {
        // Make sure that the custom colorizer is removed
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setIndicatorColors(colors);
        invalidate();
    }

    public  void setDividerColors(int... colors) {
        // Make sure that the custom colorizer is removed
        mCustomTabColorizer = null;
        mDefaultTabColorizer.setDividerColors(colors);
        invalidate();
    }

    public void onViewPagerPageChanged(int position, float positionOffset) {
        mSelectedPosition = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int height = getHeight();
        final int childCount = getChildCount();
    //    final int dividerHeightPx = (int) (Math.min(Math.max(0f, mDividerHeight), 1f) * height);
        final SlidingTabLayout.TabColorizer tabColorizer = mCustomTabColorizer != null
                ? mCustomTabColorizer
                : mDefaultTabColorizer;

        // Thick colored underline below the current selection
        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition);
            int left = selectedTitle.getLeft();
            int right = selectedTitle.getRight();
            int selectedWidth = selectedTitle.getWidth() / 3 - 30;
            int color = tabColorizer.getIndicatorColor(mSelectedPosition);


            // Thin underline along the entire bottom edge 底部线条
            //   mBottomBorderPaint.setColor( setColorAlpha(Color.RED, DEFAULT_BOTTOM_BORDER_COLOR_ALPHA));
            canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);

            if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {
                int nextColor = tabColorizer.getIndicatorColor(mSelectedPosition + 1);
                if (color != nextColor) {
                    color = blendColors(nextColor, color, mSelectionOffset);
                }

                // Draw the selection partway between the tabs
                View nextTitle = getChildAt(mSelectedPosition + 1);
                left = (int) (mSelectionOffset * nextTitle.getLeft() +
                        (1.0f - mSelectionOffset) * left);
                right = (int) (mSelectionOffset * nextTitle.getRight() +
                        (1.0f - mSelectionOffset) * right);
            }

            mSelectedIndicatorPaint.setColor(color);

           /* canvas.drawRect(left + selectedWidth, height - mSelectedIndicatorThickness, right - selectedWidth,
                    height, mSelectedIndicatorPaint);*/
            canvas.drawRect(left, 0 , right, height,mBottomBorderPaint);
                canvas.drawRect(left , height - mSelectedIndicatorThickness, right ,
                    height, mSelectedIndicatorPaint);
        }



        // Vertical separators between the titles
        //设置分割线

      /*   int separatorTop = (height - dividerHeightPx) / 2;
         for (int i = 0; i < childCount - 1; i++) {
         View child = getChildAt(i);
         mDividerPaint.setColor(tabColorizer.getDividerColor(i));
         canvas.drawLine(child.getRight(), separatorTop, child.getRight(),
         separatorTop + dividerHeightPx, mDividerPaint);
         }*/
    }

    /**
     * Set the alpha value of the {@code color} to be the given {@code alpha} value.
     */
    private static int setColorAlpha(int color, byte alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 1.0 will return {@code color1}, 0.5 will give an even blend,
     *              0.0 will return {@code color2}.
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }

    private static class SimpleTabColorizer implements SlidingTabLayout.TabColorizer {
        private int[] mIndicatorColors;
        private int[] mDividerColors;

        @Override
        public final int getIndicatorColor(int position) {
            return mIndicatorColors[position % mIndicatorColors.length];
        }

        @Override
        public final int getDividerColor(int position) {
            return mDividerColors[position % mDividerColors.length];
        }

        public void setIndicatorColors(int... colors) {
            mIndicatorColors = colors;
        }

        public void setDividerColors(int... colors) {
            mDividerColors = colors;
        }
    }
}
