package com.easefun.polyv.commonui.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public abstract class PolyvBaseRecyclerViewAdapter extends
        RecyclerView.Adapter<PolyvBaseRecyclerViewAdapter.ClickableViewHolder> {

    protected RecyclerView mRecyclerView;
    private Context context;
    private List<RecyclerView.OnScrollListener> mListeners = new ArrayList<>();
    private OnItemClickListener itemClickListener;
    private OnItemLongClickListener itemLongClickListener;


    public PolyvBaseRecyclerViewAdapter(RecyclerView recyclerView) {

        this.mRecyclerView = recyclerView;
        this.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView rv, int newState) {

                for (RecyclerView.OnScrollListener listener : mListeners) {
                    listener.onScrollStateChanged(rv, newState);
                }
            }


            @Override
            public void onScrolled(RecyclerView rv, int dx, int dy) {

                for (RecyclerView.OnScrollListener listener : mListeners) {
                    listener.onScrolled(rv, dx, dy);
                }
            }
        });
    }

    public void addOnScrollListener(RecyclerView.OnScrollListener listener) {

        mListeners.add(listener);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {

        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {

        this.itemLongClickListener = listener;
    }

    public void bindContext(Context context) {
        this.context = context;
    }

    public Context getContext() {

        return this.context;
    }

    @Override
    public void onBindViewHolder(final ClickableViewHolder holder, final int position) {

        holder.getParentView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(position, holder);
                }
            }
        });
        holder.getParentView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return itemLongClickListener != null
                        && itemLongClickListener.onItemLongClick(position, holder);
            }
        });
    }


    public interface OnItemClickListener {

        void onItemClick(int position, ClickableViewHolder holder);
    }


    interface OnItemLongClickListener {

        boolean onItemLongClick(int position, ClickableViewHolder holder);
    }

    public static class ClickableViewHolder extends RecyclerView.ViewHolder {

        private View parentView;


        public ClickableViewHolder(View itemView) {

            super(itemView);
            this.parentView = itemView;
        }


        View getParentView() {

            return parentView;
        }


        @SuppressWarnings("unchecked")
        public <T extends View> T $(@IdRes int id) {

            return (T) parentView.findViewById(id);
        }
    }
}