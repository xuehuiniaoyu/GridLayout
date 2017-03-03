package com.example.gridTest.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

/**
 * Created by tjy on 2017/2/8 0008.
 * 网格布局
 *
 * <pre>
 *
 *      我举个不恰当的例子，有可能有人玩过麻将。
 *      麻将在麻将盒里的排列就像是网格布局在屏幕上的排列一样，麻将盒就是一个容积的约束。
 *      如果继续抽象：相邻的2只麻将可以组合在一起成为一个比较大的麻将，那么4只甚至6只也同样可以组合起来成为一个更大的（块）
 *      那么这个时候麻将盒中分布的就是大小不一的放个并有序的排列在麻将盒内。
 *      继续抽象：如果我们把麻将和的一个边拆掉，那么然后无限的延伸这个边，此时麻将盒的容积就变的更大，并从一个方向延续排列。
 *
 *      我们的网格布局更像是这样一个场景，如果你要使用GridLayout进行布局的话，脑海中就出现上面故事的一个大概流程。
 *      1.设置麻将盒宽高  setLayoutWidth 和 setLayoutHeight
 *      2.设置延续方向 setOrientation
 *      3.添加麻将 addGrid ...
 *
 *      addMargin可以规定下一个麻将（Grid）的开始位置，默认是当前麻将（Grid）的 right/bottom
 *
 *      GridLayout只是一个简单View的布局，如果你想要更实现更加复杂比如一个块上有多个元素甚至有组合动画，那么你就需要使用LayerGridLayout
 *      @see LayerGridLayout
 *      LayerGridLayout 实现了一个view的分组。
 *
 *
 * <pre/>
 */
public class GridLayout extends TouchGroup implements View.OnFocusChangeListener, View.OnClickListener, View.OnLongClickListener {

    // 水平开始位置（第一个item的left）
    private int layoutX;

    // 垂直开始位置（第一个item的top）
    private int layoutY;

    // 可见区域的宽度（不包括layoutX）
    private int layoutWidth;

    // 可见高度的高度（不包括layoutY）
    private int layoutHeight;

    private int layoutMeasureWidth;
    private int layoutMeasureHeight;

    // 安全边距（保证显示在屏幕内）
    private int freePlace;

    // 每一个grid 上下左右的间隙
    private int gap;

    // 当前焦点位置
    private int selection = 1;

    // 动画时长，当移动到下一个焦点，滚动动画一次执行的时间。
    private int duration = 600;

    // 添加一个间隙，对下一个grid添加的位置进行偏移。
    private Margin margin;

    // 焦点滚动插入器
    private FocusScrollInterpolator mFocusInterpolator;

    /**
     * 内容注册表
     */
    private HashMap<Integer, Grid> gridHashRegistry = new HashMap<Integer, Grid>();

    /**
     * 焦点滚动插入器
     * 由于水平和垂直的焦点滚动是不一样的，所以不同的方向选择不同的插入器。
     */
    interface FocusScrollInterpolator {
        void onFocus(Grid.LayoutParams layoutParams);
    }


    public GridLayout(Context context) {
        super(context);
    }

    public GridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public static class Margin {
        public int leftMargin;
        public int topMargin;
        public Margin(int leftMargin, int topMargin) {
            this.leftMargin = leftMargin;
            this.topMargin = topMargin;
        }
    }

    /**
     *
     * 一个方块
     * 每一个内容被抽象为一个Grid。
     */
    public static class Grid {

        public static class LayoutParams extends ViewGroup.LayoutParams {
            public LayoutParams(int w, int h) {
                super(w, h);
            }

            public LayoutParams(LayoutParams source) {
                super(source);
                this.left = source.left;
                this.top = source.top;
            }

            public int left;
            public int top;
            public int right;
            public int bottom;

            void reset() {
                right = left + width;
                bottom = top + height;
            }
        }

        int id;
        LayoutParams params;
        View mView;
    }

    /**
     * 设置方向
     *
     * @see GridLayout#HORIZONTAL
     * @see GridLayout#VERTICAL
     *
     * @param orientation
     */

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        if(this.getOrientation() == HORIZONTAL) {
            mFocusInterpolator = new FocusScrollInterpolator() {
                @Override
                public void onFocus(Grid.LayoutParams lp) {
                    if (lp.right - mScroller.getFinalX()  > layoutWidth - freePlace) {
                        int x = lp.right - layoutWidth + freePlace;
                        scrollTo(mScroller.getCurrX(), mScroller.getCurrY(), x, mScroller.getCurrY(), duration);
                    } else if (lp.left + layoutX - mScroller.getFinalX()  < freePlace) {
                        int x = lp.left;
                        if (lp.left != 0) {
                            x = lp.left + freePlace;
                        }
                        scrollTo(mScroller.getCurrX(), mScroller.getCurrY(), x, mScroller.getCurrY(), duration);
                    }
                }
            };
        }
        else if(this.getOrientation() == VERTICAL) {
            mFocusInterpolator = new FocusScrollInterpolator() {
                @Override
                public void onFocus(Grid.LayoutParams lp) {
                    if (lp.bottom - mScroller.getFinalY()  > layoutHeight - freePlace) {
                        int y = lp.bottom - layoutHeight + freePlace;
                        scrollTo(mScroller.getCurrX(), mScroller.getCurrY(), mScroller.getCurrX(), y, duration);
                    } else if (lp.top + layoutY - mScroller.getFinalY() < freePlace) {
                        int y = lp.top;
                        if (lp.top != 0) {
                            y = lp.top + freePlace;
                        }
                        scrollTo(mScroller.getCurrX(), mScroller.getCurrY(), mScroller.getCurrX(), y, duration);
                    }
                }
            };
        }
    }

    /**
     * 添加Grid
     * @param grid
     */
    private void addGrid(Grid grid) {
        grid.mView.setId(grid.id = gridHashRegistry.size()+1);
        if(grid.mView.isFocusable()) {
            grid.mView.setOnFocusChangeListener(this);
        }
        if(grid.mView.isClickable()) {
            grid.mView.setOnClickListener(this);
            grid.mView.setOnLongClickListener(this);
        }
        layoutInfo(grid);
        gridHashRegistry.put(grid.id, grid);
    }

    /**
     * 添加Grid
     * @param view
     * @param width
     * @param height
     */
    public void addGrid(View view, int width, int height){
        Grid grid = new Grid();
        grid.mView = view;
        grid.params = new Grid.LayoutParams(width, height);
        super.addView(grid.mView, -1, grid.params);
        this.addGrid(grid);
    }

    /**
     * 添加一个空的块
     * @param width
     * @param height
     */
    public void addGrid(int width, int height) {
        Grid.LayoutParams lp = new Grid.LayoutParams(width, height);
        Grid grid = new Grid();
        grid.id = gridHashRegistry.size()+1;
        grid.params = lp;
        layoutInfo(grid);
        gridHashRegistry.put(grid.id, grid);
    }

    /**
     * 添加块的View
     * @param position
     * @param view
     */
    public void addGridView(int position, View view) {
        Grid grid = gridHashRegistry.get(position);
        super.addView(grid.mView = view);
    }

    /**
     * 得到块的数量
     * @return
     */
    public int getGridCount() {
        return gridHashRegistry.size();
    }

    /**
     * 获取块
     * @param id
     * @return
     */
    public Grid getGridAt(int id) {
        return gridHashRegistry.get(id);
    }

    /**
     * 获取块的View
     * @param id
     * @return
     */
    public View getGridViewAt(int id) {
        Grid grid = getGridAt(id);
        if(grid != null)
            return grid.mView;
        return null;
    }

    /**
     * 添加一个位置偏移的 Margin
     * @param margin
     */
    public void addMargin(Margin margin) {
        this.margin = margin;
    }

    public void addMargin(int leftMargin, int topMargin) {
        this.margin = new Margin(leftMargin, topMargin);
    }

    public boolean removeGridAt(View view) {
        return this.removeGridAt(view.getId());
    }

    public boolean removeGridAt(int position) {
        /*if(position == gridHashRegistry.size()) {
            Grid grid = gridHashRegistry.remove(position);
            if (grid != null) {
                selection = Math.min(selection, position-1);
                Grid prevGrid = gridHashRegistry.get(selection);
                if(prevGrid != null) {
                    prevGrid.mView.requestFocus();
                }
                super.removeView(grid.mView);
                return true;
            }
            return true;
        }*/
        if(gridHashRegistry.containsKey(position)) {
            Grid grid = gridHashRegistry.remove(position);
            super.removeView(grid.mView);
            return requestFocus();
        }
        return false;
    }

    /**
     * 删除所有Grid
     */
    public void removeAllGrids() {
        selection = 0;
        gridHashRegistry.clear();
        super.removeAllViews();
    }

    /**
     * 子类可以添加View，但不记录
     * @param view
     */
    protected void addView0(View view) {
        super.addView(view, -1, view.getLayoutParams());
    }

    /**
     * 删除View
     * @param view
     */
    protected void removeView0(View view) {
        super.removeView(view);
    }

    protected void removeAllViews0() {
        super.removeAllViews();
    }

    @Deprecated
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void addView(View child) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void addView(View child, ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void addView(View child, int width, int height) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void addView(View child, int index) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void removeView(View view) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void removeViewAt(int index) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void removeViews(int start, int count) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void removeViewInLayout(View view) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void removeViewsInLayout(int start, int count) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    protected void removeDetachedView(View child, boolean animate) {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void removeAllViews() {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    @Deprecated
    public void removeAllViewsInLayout() {
        throw new UnsupportedOperationException("错误的方法调用！");
    }

    /* 计算grid的位置 */ private void layoutGridByLp(Grid lastGrid, Grid grid){
        Grid.LayoutParams lastGridLp = lastGrid.params;
        Grid.LayoutParams gridLp = grid.params;
        if(getOrientation() == HORIZONTAL) {
            if (gridLp.height + (lastGridLp.top + lastGridLp.height) > layoutHeight) {
                // 往右排
//                gridLp.addRule(RelativeLayout.RIGHT_OF, lastGrid.mView.getId());
                gridLp.left = lastGridLp.right;
                gridLp.top = 0;
            } else {
                // 往下排
//                gridLp.addRule(RelativeLayout.ALIGN_LEFT, lastGrid.mView.getId());
//                gridLp.addRule(RelativeLayout.BELOW, lastGrid.mView.getId());
                gridLp.left = lastGridLp.left;
                gridLp.top = lastGridLp.bottom;
            }
        }
        else if(getOrientation() == VERTICAL) {
            if (gridLp.width + (lastGridLp.left + lastGridLp.width) > layoutWidth) {
                // 往下排
//                gridLp.addRule(RelativeLayout.BELOW, lastGrid.mView.getId());
                gridLp.top = lastGridLp.bottom;
                gridLp.left = 0;
            } else {
                // 往右排
//                gridLp.addRule(RelativeLayout.ALIGN_TOP, lastGrid.mView.getId());
//                gridLp.addRule(RelativeLayout.RIGHT_OF, lastGrid.mView.getId());
                gridLp.left = lastGridLp.right;
                gridLp.top = lastGridLp.top;
            }
        }
        if(margin != null) {
            gridLp.left += margin.leftMargin;
            gridLp.top += margin.topMargin;
            margin = null;
        }
        gridLp.reset();
        Log.i(GridLayout.class.getSimpleName(), "gridLp.left:"+gridLp.left);
        Log.i(GridLayout.class.getSimpleName(), "gridLp.top:"+gridLp.top);
    }

    /* 计算位置 */ private void layoutInfo(Grid grid){
        Grid lastGrid = gridHashRegistry.get(grid.id-1);
        if(lastGrid != null) {
            layoutGridByLp(lastGrid, grid);
        }
        else {
            grid.params.reset();
        }
    }

    protected final void setMeasuredDimension() {
        setMeasuredDimension(View.MeasureSpec.makeMeasureSpec(layoutMeasureWidth, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(layoutMeasureHeight, View.MeasureSpec.EXACTLY));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Grid grid;
        int gaps = getGap() * 2;
        for ( Integer key : gridHashRegistry.keySet() ) {
            grid = gridHashRegistry.get(key);
            if(grid.mView != null) {
                Grid.LayoutParams lp = grid.params;
                int width = View.MeasureSpec.makeMeasureSpec(lp.width, View.MeasureSpec.EXACTLY);
                int height = View.MeasureSpec.makeMeasureSpec(lp.height, View.MeasureSpec.EXACTLY);
                grid.mView.measure(width - gaps, height - gaps);
            }
        }
        setMeasuredDimension();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Grid grid;
        for ( Integer key : gridHashRegistry.keySet() ) {
            grid = gridHashRegistry.get(key);
            draw( grid.mView );
        }
    }

    // 绘制
    protected void draw (View view) {
        Log.i(TAG, "draw:"+view);
        Grid.LayoutParams lp = (Grid.LayoutParams) view.getLayoutParams();
        lp.reset();
        view.layout(layoutX + lp.left + gap, layoutY + lp.top + gap, layoutX + lp.right - gap, layoutY + lp.bottom - gap);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if(gridHashRegistry.containsKey(selection)) {
            Grid grid = gridHashRegistry.get(selection);
            if(grid.mView != null) {
                return grid.mView.requestFocus();
            }
        }
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Grid grid = null;
        if (hasFocus) {
            selection = v.getId();
            grid = gridHashRegistry.get(selection);
            if(grid == null) {
                return;
            }
            Grid.LayoutParams lp = (Grid.LayoutParams) grid.mView.getLayoutParams();
            mFocusInterpolator.onFocus(lp);
        }
        if(onItemFocusChangedListener != null){
            onItemFocusChangedListener.onItemFocusChangedListener(hasFocus, selection, v, this);
        }
    }

    @Override
    public void onClick(View v) {
        if(onItemClickListener != null){
            onItemClickListener.onItemClicked(v.getId(), v, this);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if(onItemLongClickListener != null){
            onItemLongClickListener.onItemLongClicked(v.getId(), v, this);
            return true;
        }
        return false;
    }

    /**
     * 位置移动
     * @param x 开始横坐标
     * @param y 开始纵坐标
     * @param xto 目标横坐标
     * @param yto 目标纵坐标
     */
    protected void scrollTo(int x, int y, int xto, int yto) {
        scrollTo(x, y, xto, yto, duration);
    }

    /**
     *
     * 位置移动
     * @param x 开始横坐标
     * @param y 开始纵坐标
     * @param xto 目标横坐标
     * @param yto 目标纵坐标
     * @param duration 移动一次所需要的时间
     */
    protected void scrollTo(int x, int y, int xto, int yto, int duration) {
        x = mScroller.getCurrX();
        mScroller.startScroll(x, y, 0, 0, duration);
        mScroller.setFinalX(xto);
        mScroller.setFinalY(yto);
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    /// 属性

    public int getLayoutX() {
        return layoutX;
    }

    public void setLayoutX(int layoutX) {
        this.layoutX = layoutX;
        layoutMeasureWidth = this.layoutWidth + this.layoutX;
    }

    public int getLayoutY() {
        return layoutY;
    }

    public void setLayoutY(int layoutY) {
        this.layoutY = layoutY;
        layoutMeasureHeight =this.layoutHeight + this.layoutY;
    }

    public int getLayoutWidth() {
        return layoutWidth;
    }

    public void setLayoutWidth(int layoutWidth) {
        this.layoutWidth = layoutWidth;
        layoutMeasureWidth = this.layoutWidth + this.layoutX;
    }

    public int getLayoutHeight() {
        return layoutHeight;
    }

    public void setLayoutHeight(int layoutHeight) {
        this.layoutHeight = layoutHeight;
        layoutMeasureHeight =this.layoutHeight + this.layoutY;
    }

    public int getFreePlace() {
        return freePlace;
    }

    public void setFreePlace(int freePlace) {
        this.freePlace = freePlace;
    }

    public int getGap() {
        return gap;
    }

    public void setGap(int gap) {
        this.gap = gap;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getSelection() {
        return selection;
    }

    public void setSelection(int selection) {
        this.selection = selection;
    }

    ///

    @Override
    protected int getScrollWidth() {
        Grid lastGrid = gridHashRegistry.get(gridHashRegistry.size()-1);
        if(lastGrid != null) {
            Grid.LayoutParams lp = (Grid.LayoutParams) lastGrid.mView.getLayoutParams();
            return lp.right - layoutWidth + layoutX - freePlace;
        }
        return 0;
    }

    @Override
    protected int getScrollHeight() {
        Grid lastGrid = gridHashRegistry.get(gridHashRegistry.size()-1);
        if(lastGrid != null) {
            Grid.LayoutParams lp = (Grid.LayoutParams) lastGrid.mView.getLayoutParams();
            return lp.bottom - layoutHeight + layoutY - freePlace;
        }
        return 0;
    }

    public static final int BOUNDARY_LEFT = 1; // 左边界
    public static final int BOUNDARY_RIGHT = 2; // 右边界
    public static final int BOUNDARY_TOP = 3; // 右边界
    public static final int BOUNDARY_BOTTOM = 4; // 右边界
    public interface OnFocusChangedFromBoundaryListener{
        boolean onFocusChangedFromBoundary(int boundary);
    }

    public interface OnItemClickListener{
        void onItemClicked(int position, View view, ViewGroup parent);
    }

    public interface OnItemLongClickListener{
        boolean onItemLongClicked(int position, View view, ViewGroup parent);
    }

    public interface OnItemFocusChangedListener{
        void onItemFocusChangedListener(boolean hasFocus, int position, View view, ViewGroup parent);
    }

    protected OnFocusChangedFromBoundaryListener onFocusChangedFromBoundaryListener;
    protected OnItemClickListener onItemClickListener;
    protected OnItemLongClickListener onItemLongClickListener;
    protected OnItemFocusChangedListener onItemFocusChangedListener;

    public void setOnFocusChangedFromBoundaryListener(OnFocusChangedFromBoundaryListener onFocusChangedFromBoundaryListener) {
        this.onFocusChangedFromBoundaryListener = onFocusChangedFromBoundaryListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnItemFocusChangedListener(OnItemFocusChangedListener onItemFocusChangedListener) {
        this.onItemFocusChangedListener = onItemFocusChangedListener;
    }

    // 加入焦点View
    public interface JointCallback{
        public static final int JOINT_TYPE_ADD = 0;
        public static final int JOINT_TYPE_REMOVE = 1;

        void onJoint(int jointType, ViewGroup parent, View child);
    }

    protected final HashMap<String, View> itemFocusViewCache = new HashMap<String, View>();
    public View getItemFocusView(String id) {
        return itemFocusViewCache.get(id);
    }

    /**
     * 添加一个焦点View并作为父类的子元素
     * @param id
     * @param itemFocusView
     */
    public void addItemFocusView(String id, View itemFocusView){
        itemFocusView.setEnabled(false);
        itemFocusViewCache.put(id, itemFocusView);
        addView(itemFocusView);
    }

    /**
     * 添加一个焦点View，父类只知道其引用，至于如何添加到容器父类不做处理。
     * @param id
     * @param itemFocusView
     * @param callback 添加动作
     */
    public void addItemFocusView(String id, View itemFocusView, JointCallback callback) {
        itemFocusView.setEnabled(false);
        itemFocusViewCache.put(id, itemFocusView);
        callback.onJoint(JointCallback.JOINT_TYPE_ADD, GridLayout.this, itemFocusView);
    }

    /**
     * 移除焦点View
     * @param itemFocusView
     */
    public void removeItemFocusView(View itemFocusView){
        if(itemFocusView != null) {
            removeView(itemFocusView);
        }
    }

    /**
     * 移除焦点View
     * @param itemFocusView
     * @param callback 移除动作
     */
    public void removeItemFocusView(View itemFocusView, JointCallback callback){
        callback.onJoint(JointCallback.JOINT_TYPE_REMOVE, GridLayout.this, itemFocusView);
    }

    /**
     * 移除焦点View，通过Id
     * @param id
     */
    public void removeItemFocusView(String id){
        removeItemFocusView(itemFocusViewCache.remove(id));
    }

    /**
     * 移除焦点View，通过Id
     * @param id
     * @param callback 移除动作
     */
    public void removeItemFocusView(String id, JointCallback callback){
        removeItemFocusView(itemFocusViewCache.remove(id), callback);
    }

    Grid.LayoutParams getLayoutParams(int i) {
        if(gridHashRegistry.containsKey(i)) {
            Grid grid = gridHashRegistry.get(i);
            return grid.params;
        }
        return null;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN) {
            if(onFocusChangedFromBoundaryListener != null) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_LEFT: {
                        if (getOrientation() == HORIZONTAL) {
                            Grid.LayoutParams lp = getLayoutParams(selection);
                            if (lp != null && lp.left == 0) {
                                return onFocusChangedFromBoundaryListener.onFocusChangedFromBoundary(BOUNDARY_LEFT);
                            }
                        }
                        break;
                    }
                    case KeyEvent.KEYCODE_DPAD_RIGHT: {
                        if (getOrientation() == HORIZONTAL) {
                            Grid.LayoutParams lp = getLayoutParams(selection);
                            Grid.LayoutParams lastLp = getLayoutParams(gridHashRegistry.size());
                            if (lp != null && lastLp != null && lp.right >= lastLp.right) {
                                return onFocusChangedFromBoundaryListener.onFocusChangedFromBoundary(BOUNDARY_RIGHT);
                            }
                        }
                        break;
                    }
                    case KeyEvent.KEYCODE_DPAD_UP: {
                        if (getOrientation() == VERTICAL) {
                            Grid.LayoutParams lp = getLayoutParams(selection);
                            if (lp != null && lp.top == 0) {
                                return onFocusChangedFromBoundaryListener.onFocusChangedFromBoundary(BOUNDARY_TOP);
                            }
                        }
                        break;
                    }
                    case KeyEvent.KEYCODE_DPAD_DOWN: {
                        if (getOrientation() == VERTICAL) {
                            Grid.LayoutParams lp = getLayoutParams(selection);
                            Grid.LayoutParams lastLp = getLayoutParams(gridHashRegistry.size());
                            if (lp != null && lastLp != null && lp.bottom >= lastLp.bottom) {
                                return onFocusChangedFromBoundaryListener.onFocusChangedFromBoundary(BOUNDARY_BOTTOM);
                            }
                        }
                        break;
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }
}
