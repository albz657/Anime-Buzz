package me.jakemoritz.animebuzz.misc;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jakemoritz.animebuzz.R;

public class AccountPreference extends Preference {
    public AccountPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public AccountPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AccountPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        CircleImageView imageView = (CircleImageView) holder.findViewById(R.id.account_avatar);
        imageView.setImageResource(R.drawable.placeholder);
    }
}
