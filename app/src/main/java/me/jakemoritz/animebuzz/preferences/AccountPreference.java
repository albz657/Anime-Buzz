package me.jakemoritz.animebuzz.preferences;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.utils.SharedPrefsUtils;

public class AccountPreference extends Preference {
    private Context context;

    public AccountPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.context = context;
    }

    public AccountPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    public AccountPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public AccountPreference(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        CircleImageView circleImageView = (CircleImageView) holder.findViewById(R.id.account_avatar);
        circleImageView.setImageResource(R.drawable.placeholder);

        if (SharedPrefsUtils.getInstance().isLoggedIn()) {
            Drawable avatarDrawable = Drawable.createFromPath(context.getFilesDir().getPath() + File.separator + context.getString(R.string.file_avatar));

            if (avatarDrawable != null) {
                circleImageView.setImageDrawable(avatarDrawable);
            }
        }
    }
}
