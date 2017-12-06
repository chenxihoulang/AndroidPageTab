package com.example.chaihongwei.androidpagetab;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面内tab标签View
 *
 * @author chaihongwei 2017/12/5 15:20
 */
public class PageTabView extends View {
    /**
     * 最少的item个数
     */
    private static final int MIN_ITEM_COUNT = 2;
    /**
     * 圆角半径,单位dp
     */
    private float mCornerRadius = 4;
    /**
     * 边框宽度,单位dp
     */
    private float mBorderWidth = 0.5F;
    /**
     * tab的默认高度,单位dp
     */
    private float mTabHeight = 40;
    /**
     * 分割线宽度,单位dp
     */
    private float mDividerWidth = 0.3F;
    /**
     * 分割线上下间距,单位dp
     */
    private float mDividerMargin = 10;
    /**
     * 字体大小,单位:sp
     */
    private float mTextSize = 16;
    /**
     * 背景颜色
     */
    private int mBgColor = Color.WHITE;
    /**
     * 边框颜色
     */
    private int mBorderColor = Color.GRAY;
    /**
     * 分割线颜色
     */
    private int mDividerColor = Color.GRAY;
    /**
     * 选择文本颜色
     */
    private int mSelectedTextColor = Color.BLUE;
    /**
     * 默认文本颜色
     */
    private int mDefaultTextColor = Color.BLACK;

    private Paint mPaint;

    private List<TabItem> mTabItems = new ArrayList<>();
    /**
     * 当前选中的索引
     */
    private int mSelectItemIndex = 0;
    private OnTabChangedListener mOnTabChangedListener;

    public PageTabView(Context context) {
        super(context);
        initData();
    }

    public PageTabView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData();
        initAttrs(attrs);
    }

    public PageTabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
        initAttrs(attrs);
    }

    private void initAttrs(AttributeSet attrs) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PageTabView);

        mCornerRadius = a.getDimension(R.styleable.PageTabView_ptvCornerRadius, mCornerRadius);
        mBorderWidth = a.getDimension(R.styleable.PageTabView_ptvBorderWidth, mBorderWidth);
        mTabHeight = a.getDimension(R.styleable.PageTabView_ptvTabHeight, mTabHeight);
        mDividerWidth = a.getDimension(R.styleable.PageTabView_ptvDividerWidth, mDividerWidth);
        mDividerMargin = a.getDimension(R.styleable.PageTabView_ptvDividerMargin, mDividerMargin);
        mTextSize = a.getDimension(R.styleable.PageTabView_ptvTextSize, mTextSize);
        mBgColor = a.getColor(R.styleable.PageTabView_ptvBgColor, mBgColor);
        mBorderColor = a.getColor(R.styleable.PageTabView_ptvBorderColor, mBorderColor);
        mDividerColor = a.getColor(R.styleable.PageTabView_ptvDividerColor, mDividerColor);
        mSelectedTextColor = a.getColor(R.styleable.PageTabView_ptvSelectedTextColor, mSelectedTextColor);
        mDefaultTextColor = a.getColor(R.styleable.PageTabView_ptvDefaultTextColor, mDefaultTextColor);

        //解析item列表
        CharSequence[] tabItems = a.getTextArray(R.styleable.PageTabView_ptvTabItems);
        if (tabItems != null && tabItems.length >= MIN_ITEM_COUNT) {
            for (CharSequence tabItemText : tabItems) {
                mTabItems.add(new TabItem(tabItemText.toString()));
            }
        }
        mSelectItemIndex = a.getInt(R.styleable.PageTabView_ptvSelectItemIndex, mSelectItemIndex);

        a.recycle();

        //重新初始化画笔
        initPaint();
    }

    private void initData() {
        mCornerRadius = dip2px(mCornerRadius);
        mBorderWidth = dip2px(mBorderWidth);
        mTabHeight = dip2px(mTabHeight);
        mDividerWidth = dip2px(mDividerWidth);
        mDividerMargin = dip2px(mDividerMargin);
        mTextSize = sp2px(mTextSize);

        initPaint();

        //将背景色设置为透明,忽略背景,否则圆角处会出现直角背景色
        setBackgroundColor(Color.TRANSPARENT);
    }

    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(mTextSize);
    }

    public void setTabItems(List<TabItem> tabItems) {
        if (!hasContainAtLeastItems(tabItems)) {
            throw new IllegalArgumentException("must contain at least two items!");
        }

        this.mTabItems = tabItems;
        invalidate();
    }

    public void setSelectItemIndex(int selectItemIndex) {
        if (mSelectItemIndex == selectItemIndex) {
            return;
        }

        //一般会配合ViewPager进行使用,所以此处按ViewPager的处理逻辑来进行索引处理
        int count = mTabItems.size();
        if (selectItemIndex < 0) {
            selectItemIndex = 0;
        } else if (selectItemIndex >= count) {
            selectItemIndex = count - 1;
        }

        this.mSelectItemIndex = selectItemIndex;

        invalidate();
    }

    public int getSelectItemIndex() {
        return mSelectItemIndex;
    }

    public void setOnTabChangedListener(OnTabChangedListener onTabChangedListener) {
        this.mOnTabChangedListener = onTabChangedListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (!hasContainAtLeastItems(mTabItems)) {
            //没有满足最小item个数就隐藏
            setVisibility(GONE);
        } else {
            setMeasuredDimension(widthMeasureSpec, (int) mTabHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //必须满足最少item个数,否则不进行绘制
        if (!hasContainAtLeastItems(mTabItems)) {
            return;
        }

        int width = getMeasuredWidth();

        //绘制边框
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(0);
        RectF roundRectF = new RectF(0, 0, width, mTabHeight);
        canvas.drawRoundRect(roundRectF, mCornerRadius, mCornerRadius, mPaint);

        mPaint.setColor(mBgColor);
        //由于只使用的是同一画笔,所以边框绘制完了之后要将画笔样式重置
        mPaint.setStyle(Paint.Style.FILL);
        //绘制边框的时候不设置边框宽度,在这里设置边框宽度,搭配上面的样式将先绘制的边框显示出来
        mPaint.setStrokeWidth(mBorderWidth);
        //绘制最底层的圆角矩形
        canvas.drawRoundRect(roundRectF, mCornerRadius, mCornerRadius, mPaint);

        int count = mTabItems.size();
        //每一个item的宽度=总宽度/item个数-分割线的宽度
        float itemWidth = width / count - (count - 1) * mDividerWidth;

        for (int i = 0; i < count; i++) {
            //设置选中和默认显示的文本颜色
            if (i == mSelectItemIndex) {
                mPaint.setColor(mSelectedTextColor);
            } else {
                mPaint.setColor(mDefaultTextColor);
            }

            String tabText = mTabItems.get(i).text;
            //计算文本宽高,用于绘制位置判断
            Rect rect = new Rect();
            mPaint.getTextBounds(tabText, 0, tabText.length(), rect);
            int textWidth = rect.width();

            Paint.FontMetricsInt fontMetrics = mPaint.getFontMetricsInt();
            float textStartX = i * (itemWidth + mDividerWidth) + (itemWidth - textWidth) / 2;
            //文本绘制是基于baseline进行绘制的
            float baselineY = (mTabHeight - fontMetrics.bottom + fontMetrics.top) / 2 - fontMetrics.top;

            //绘制文本
            canvas.drawText(tabText, textStartX, baselineY, mPaint);

            //设置分割线的颜色
            mPaint.setColor(mDividerColor);
            mPaint.setStrokeWidth(mDividerWidth);
            //绘制分割线
            if (i != count - 1) {
                float lineStartX = (i + 1) * itemWidth;
                float lineEndY = mTabHeight - mDividerMargin;

                canvas.drawLine(lineStartX, mDividerMargin, lineStartX, lineEndY, mPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            int touchX = (int) event.getX();

            int touchItemIndex = getTouchItemIndex(touchX);
            //如果点击的是不同的item,则进行重绘
            if (touchItemIndex != mSelectItemIndex) {
                mSelectItemIndex = touchItemIndex;

                invalidate();

                if (mOnTabChangedListener != null) {
                    mOnTabChangedListener.onChanged(mSelectItemIndex);
                }
            }
        }
        return true;
    }

    /**
     * 是否包含最小需要的item个数
     */
    private boolean hasContainAtLeastItems(List<TabItem> tabItems) {
        if (tabItems != null && tabItems.size() >= MIN_ITEM_COUNT) {
            return true;
        }

        return false;
    }

    private int getTouchItemIndex(int touchX) {
        int width = getMeasuredWidth();
        int count = mTabItems.size();
        //每一个item的宽度=总宽度/item个数-分割线的宽度
        int itemWidth = (int) (width / count - (count - 1) * mDividerWidth);
        for (int i = 0; i < count; i++) {
            //当前位置的item的最大x坐标
            int tabItemMaxX = (i + 1) * itemWidth;

            if (touchX <= tabItemMaxX) {
                return i;
            }
        }

        return mSelectItemIndex;
    }

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static class TabItem {
        public String text;

        public TabItem(String text) {
            this.text = text;
        }
    }

    public interface OnTabChangedListener {
        void onChanged(int selectedIndex);
    }
}
