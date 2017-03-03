package com.huan.ui4tv;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import com.huan.ui4tv.views.GridLayout;

/**
 * Created by Administrator on 2017/3/3 0003.
 */
public class GridLayoutActivity extends Activity {
    private GridLayout gridLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(gridLayout=new GridLayout(this));

        // 这里是准备好的数据
        Button[] buttons = new Button[10];
        int i = 0;
        Button btn;
        for(; i < buttons.length; i++){
            btn = new Button(this);
            btn.setText("text："+i);
            buttons[i] = btn;
        }

        // 首先是对GridLayout的一些配置
        gridLayout.setLayoutWidth(600);// 最大宽度
        gridLayout.setLayoutHeight(400); // 最大高度
        gridLayout.setOrientation(GridLayout.HORIZONTAL); // 横向排列
        gridLayout.setLayoutX(50); // 开始位置偏移，有些布局可能有放大某一个块的需求，这个至关重要
        gridLayout.setLayoutY(50);
        gridLayout.setFreePlace(10);
        gridLayout.setGap(5); // 每个块之间的间隙


        // 添加模板和数据
        gridLayout.addGrid(buttons[0], 200, 200);
        gridLayout.addGrid(buttons[1], 200, 100);
        gridLayout.addGrid(buttons[2], 200, 100);
        gridLayout.addMargin(100, 0);
        gridLayout.addGrid(buttons[3], 100, 100);
        gridLayout.addGrid(buttons[4], 100, 100);
        gridLayout.addGrid(buttons[5], 100, 100);
        gridLayout.addGrid(buttons[6], 100, 100);
        gridLayout.addGrid(buttons[7], 200, 200);
        gridLayout.addGrid(buttons[8], 200, 200);
        gridLayout.addGrid(buttons[9], 200, 400);
    }
}
