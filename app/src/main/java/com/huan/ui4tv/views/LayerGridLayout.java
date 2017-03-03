package com.example.gridTest.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tjy on 2015/3/28.
 * <tt>
 *      网格UI布局类
 *      多层网格布局
 * </tt>
 */
public class LayerGridLayout extends GridLayout {
    public LayerGridLayout(Context context) {
        super(context);
    }

    public LayerGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LayerGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /** 内容集合 **/
    private HashMap<Integer, LayerGrid> mLayerGrids = new HashMap<Integer, LayerGrid>();

    /** 内容适配器 **/
    private LayerGridAdapter<?> adapter;


    /**
     * 多层块
     */
    public static class LayerGrid {
        private int id;
        private LayerGridLayout rootView;
        private View focusView;
        LayerGrid(LayerGridLayout rootView) {
            this.rootView = rootView;
        }

        private HashMap<String, View> views = new HashMap<String, View>();
        public void addView(String tag, View view, Grid.LayoutParams lp) {
            try {
                view.setLayoutParams(lp);
                rootView.addView0(view);
            } catch (Exception e) {

            }
            views.put(tag, view);
        }

        public void setFocusView(View focusView) {
            int gridId = this.id;
            this.focusView = focusView;
            this.focusView.setId(gridId);
            Grid grid = rootView.getGridAt(gridId);
            grid.mView = this.focusView;
            focusView.setFocusable(true);
            focusView.setClickable(true);
            focusView.setOnFocusChangeListener(rootView);
            focusView.setOnClickListener(rootView);
            focusView.setOnLongClickListener(rootView);
        }

        public View getFocusView() {
            return focusView;
        }

        public Collection<View> getViews() {
            return views.values();
        }

        public View getViewByTag(String tag) {
            return views.get(tag);
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

    }

    /**
     * 添加数据
     * 给所有网格添加数据
     * @param adapter
     */
    public void setAdapter(LayerGridAdapter<?> adapter) {
        this.adapter = adapter;
        int count = Math.min(adapter.getCount(), getGridCount());
        if(count > 0) {
            mLayerGrids.clear();
            for (int i = 0; i < count; i++) {
                LayerGrid item = new LayerGrid(this);
                item.setId(i+1);
                mLayerGrids.put(item.getId(), item);
                adapter.onLayout(i, getLayoutParams(item.id), item, this);
            }
        }
    }

    public <T> LayerGridAdapter<T> getAdapter() {
        return (LayerGridAdapter<T>) adapter;
    }

    /**
     * 获取内容
     * 通过下标获取某一个网格
     * @param position
     * @return
     */
    public LayerGrid getLayerGridAt(int position) {
        return mLayerGrids.get(position);
    }

    /**
     * 删除
     * @param position
     */
    public void removeLayerGridAt(int position) {
        if(mLayerGrids.containsKey(position)) {
            LayerGrid item = mLayerGrids.remove(position);
            for(String tag : item.views.keySet()) {
                removeView0(item.views.get(tag));
            }
            item.views.clear();
            removeGridAt(item.id);
        }
    }

    /**
     * 数据适配器
     * @param <T>
     */
    public static abstract class LayerGridAdapter<T> {
        private List<T> list = new ArrayList<T>();

        public int getCount() {
            return list.size();
        }

        public T getItem(int position) {
            return list.get(position);
        }

        /**
         *
         * @param position
         * @param lp
         * @param data
         * @param parent
         */
        public abstract void onLayout(int position, Grid.LayoutParams lp, LayerGrid data, LayerGridLayout parent);

        public void setData(List<T> list) {
            this.list = list;
        }

        public void append(T t) {
            this.list.add(t);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Grid grid;
        LayerGrid data;
        int gaps = getGap() * 2;
        for(Integer id : mLayerGrids.keySet()) {
            data = mLayerGrids.get(id);
            grid = getGridAt(id);
            for(String tag : data.views.keySet()) {
                int width = View.MeasureSpec.makeMeasureSpec(grid.params.width, View.MeasureSpec.EXACTLY);
                int height = View.MeasureSpec.makeMeasureSpec(grid.params.height, View.MeasureSpec.EXACTLY);
                data.views.get(tag).measure(width - gaps, height - gaps);
            }
        }
        setMeasuredDimension();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG, "onLayout:"+changed);
        for (Integer key : mLayerGrids.keySet()) {
            LayerGrid item = mLayerGrids.get(key);
            for (String tag : item.views.keySet()) {
                draw(item.views.get(tag));
            }
        }
    }

    /**
     * 释放资源
     */
    public void gc() {
        mLayerGrids.clear();
        super.removeAllGrids();
        super.removeAllViews0();
    }
}
