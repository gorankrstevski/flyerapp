package com.cuponation.android.ui.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.R2;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Goran Krstevski (goran.krstevski@cuponation.com) on 2.12.15.
 */
public class NotificationViewHolder extends RecyclerView.ViewHolder {

    @BindView(R2.id.notification_text)
    public TextView text;
    @BindView(R2.id.notification_time)
    public TextView time;
    @BindView(R2.id.notification_image)
    public ImageView imageView;
    @BindView(R2.id.notification_image_in_text_view)
    public TextView imageInTextView;
    @BindView(R2.id.undo_container)
    public ViewGroup undoContainer;
    @BindView(R2.id.notification_delete)
    public TextView deleteLabel;
    @BindView(R2.id.notification_undo)
    public Button undoButton;
    @BindView(R2.id.notification_expired)
    public TextView expiredLabel;

    public NotificationViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}