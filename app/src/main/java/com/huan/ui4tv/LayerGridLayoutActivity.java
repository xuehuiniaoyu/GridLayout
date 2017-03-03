package com.huan.ui4tv;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.huan.ui4tv.views.GridLayout;
import com.huan.ui4tv.views.LayerGridLayout;

/**
 * Created by Administrator on 2017/3/3 0003.
 */
public class LayerGridLayoutActivity extends Activity {
    private LayerGridLayout gridLayout;
    private LayerGridLayout.LayerGridAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(gridLayout=new LayerGridLayout(this));

        // 首先是对GridLayout的一些配置
        gridLayout.setLayoutWidth(600);// 最大宽度
        gridLayout.setLayoutHeight(400); // 最大高度
        gridLayout.setOrientation(GridLayout.HORIZONTAL); // 横向排列
        gridLayout.setLayoutX(50); // 开始位置偏移，有些布局可能有放大某一个块的需求，这个至关重要
        gridLayout.setLayoutY(50);
        gridLayout.setFreePlace(10);
        gridLayout.setGap(5); // 每个块之间的间隙

        gridLayout.setOnItemFocusChangedListener(new GridLayout.OnItemFocusChangedListener() {
            @Override
            public void onItemFocusChangedListener(boolean hasFocus, int position, View view, ViewGroup parent) {
                LayerGridLayout.LayerGrid grid = gridLayout.getLayerGridAt(position);
                GridLayout.Grid.LayoutParams lp = (GridLayout.Grid.LayoutParams) view.getLayoutParams();
                View textView = grid.getViewByTag("txt");
                if(hasFocus) {
                    AnimatorSet animatorSet = new AnimatorSet();
                    ObjectAnimator anim = ObjectAnimator.ofFloat(textView, "y", gridLayout.getLayoutY() + lp.top + 20);
                    ObjectAnimator anim1 = ObjectAnimator.ofFloat(textView, "x", gridLayout.getLayoutX() + lp.left + 20);
                    animatorSet.play(anim).with(anim1);
                    animatorSet.start();
                }
                else {
                    AnimatorSet animatorSet = new AnimatorSet();
                    ObjectAnimator anim = ObjectAnimator.ofFloat(textView, "y", gridLayout.getLayoutY() + lp.top);
                    ObjectAnimator anim1 = ObjectAnimator.ofFloat(textView, "x", gridLayout.getLayoutX() + lp.left);
                    animatorSet.play(anim).with(anim1);
                    animatorSet.start();
                }
            }
        });

        // 添加模板
        gridLayout.addGrid(200, 200);
        gridLayout.addGrid(200, 100);
        gridLayout.addGrid(200, 100);
        gridLayout.addMargin(100, 0);
        gridLayout.addGrid(100, 100);
        gridLayout.addGrid(100, 100);
        gridLayout.addGrid(100, 100);
        gridLayout.addGrid(100, 100);
        gridLayout.addGrid(200, 200);
        gridLayout.addGrid(200, 200);
        gridLayout.addGrid(200, 400);

        adapter = new LayerGridLayout.LayerGridAdapter<String>() {
            @Override
            public void onLayout(int position, GridLayout.Grid.LayoutParams lp, LayerGridLayout.LayerGrid data, LayerGridLayout parent) {
                Button btn = new Button(LayerGridLayoutActivity.this);
                btn.setText(getItem(position));

                data.addView("focusView", btn, lp);
                data.setFocusView(btn);

                TextView textView = new TextView(LayerGridLayoutActivity.this);
                textView.setText(getItem(position));
                data.addView("txt", textView, lp);
            }
        };
        int i = 0;
        for(; i < 10; i++){
            adapter.append("text:"+i);
        }

        // 添加数据
        gridLayout.setAdapter(adapter);
    }
}
