package com.example.android.sunshine.app;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by N Willis on 20/05/2015.
 */
public class MyView extends View {

    public MyView(Context context){
        super(context);
    }

    public MyView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defaultStyle){
        super(context, attrs, defaultStyle);
    }

    @Override
    protected void onMeasure(int wMeasureSpec, int hMeasureSpec){
        int hSpecMode = MeasureSpec.getMode(hMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(hMeasureSpec);
        int myHeight = hSpecSize;

        int wSpecMode = MeasureSpec.getMode(wMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(wMeasureSpec);
        int myWidth = wSpecSize;

        if(hSpecMode == MeasureSpec.EXACTLY){
            myHeight = hSpecSize;
        } else if (hSpecMode == MeasureSpec.AT_MOST){

        }

        if(wSpecMode == MeasureSpec.EXACTLY){
            myWidth = wSpecSize;
        } else if (wSpecMode == MeasureSpec.AT_MOST){

        }

        setMeasuredDimension(myWidth, myHeight);
    }

}
