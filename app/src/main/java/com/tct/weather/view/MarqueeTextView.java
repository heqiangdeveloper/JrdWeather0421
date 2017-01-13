package com.tct.weather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * User : user
 * Date : 2015-11-09
 * Time : 13:55
 */
public class MarqueeTextView extends TextView {

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean isFocused() {
        return true;
    }

}
