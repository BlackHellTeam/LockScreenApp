package com.askhmer.lockscreen.activity;

import android.os.Bundle;

import com.askhmer.lockscreen.R;
import com.liuguangqiang.swipeback.SwipeBackActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;

public class Withdrawal extends SwipeBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal);

        //swipe back
        setDragEdge(SwipeBackLayout.DragEdge.LEFT);
    }
}
