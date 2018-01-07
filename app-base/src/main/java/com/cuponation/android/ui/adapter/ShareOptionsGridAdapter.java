package com.cuponation.android.ui.adapter;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuponation.android.R;
import com.cuponation.android.ui.viewholder.ShareOptionViewHolder;

import java.util.List;

/**
 * Created by goran on 2/3/17.
 */

public class ShareOptionsGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    private List<ResolveInfo> launchables;
    private View.OnClickListener onClickListener;

    public ShareOptionsGridAdapter(List<ResolveInfo> launchables){
        this.launchables = launchables;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_share_option, viewGroup, false);
        return new ShareOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ResolveInfo resolveInfo = launchables.get(position);

        PackageManager packageManager = holder.itemView.getContext().getPackageManager();

        ((ShareOptionViewHolder)holder).title.setText(resolveInfo.loadLabel(packageManager));
        ((ShareOptionViewHolder)holder).logo.setImageDrawable(resolveInfo.loadIcon(packageManager));

        holder.itemView.setTag(resolveInfo.activityInfo.applicationInfo.packageName);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onClickListener!=null){
                    onClickListener.onClick(v);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return launchables.size();
    }

    public void setClickListener(View.OnClickListener clickListener){
        this.onClickListener = clickListener;
    }

}
