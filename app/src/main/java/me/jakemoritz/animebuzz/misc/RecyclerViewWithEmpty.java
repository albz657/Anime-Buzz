package me.jakemoritz.animebuzz.misc;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import me.jakemoritz.animebuzz.R;
import me.jakemoritz.animebuzz.helpers.App;

public class RecyclerViewWithEmpty extends RecyclerView {

    private View emptyView;

    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };

    void checkIfEmpty(){
        if (emptyView != null && getAdapter() != null){
            final boolean emptyViewVisible = getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null){
            oldAdapter.unregisterAdapterDataObserver(observer);
        }

        super.setAdapter(adapter);

        if (adapter != null){
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    public void setEmptyView(View emptyView){
        this.emptyView = emptyView;

        ImageView emptyViewImage = (ImageView) this.emptyView.findViewById(R.id.empty_image);
        Picasso.with(App.getInstance()).load(R.drawable.empty).fit().centerCrop().into(emptyViewImage);
        emptyViewImage.setAlpha((float) 0.5);

        checkIfEmpty();
    }

    public RecyclerViewWithEmpty(Context context){
        super(context);
    }

    public RecyclerViewWithEmpty(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RecyclerViewWithEmpty(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
}
