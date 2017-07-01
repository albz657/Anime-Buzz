package me.jakemoritz.animebuzz.misc;

import me.jakemoritz.animebuzz.R;

public enum SetupObject {

    INTRO(R.layout.welcome_page_1),
    LOGIN(R.layout.welcome_page_2),
    OPTIONS(R.layout.welcome_page_3);

    private int mLayoutId;

    SetupObject(int layoutId){
        mLayoutId = layoutId;
    }

    public int getLayoutId() {
        return mLayoutId;
    }
}
