package com.cuponation.android.ui.adapter;

/**
 * Created by goran on 7/22/16.
 */

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cuponation.android.R;
import com.cuponation.android.model.Retailer;
import com.cuponation.android.model.Voucher;
import com.cuponation.android.model.VoucherFull;
import com.cuponation.android.model.notifications.Notification;
import com.cuponation.android.service.local.CategoriesService;
import com.cuponation.android.service.local.NotificationService;
import com.cuponation.android.service.local.RetailerService;
import com.cuponation.android.service.remote.VoucherService;
import com.cuponation.android.ui.listener.OnGridGestureListener;
import com.cuponation.android.ui.viewholder.NotificationViewHolder;
import com.cuponation.android.util.Constants;
import com.cuponation.android.util.DateUtils;
import com.cuponation.android.util.DeepLinkHandler;
import com.cuponation.android.util.TimeUtil;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;


public class NotificationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int PENDING_REMOVAL_TIMEOUT = 3000; // 3sec

    private Context context;
    private List<Notification> notificationList;
    private OnGridGestureListener onGridGestureListener;

    List<Notification> itemsPendingRemoval;
    boolean undoOn;

    // hanlder for running delayed runnables
    private Handler handler = new Handler();

    // map of items to pending runnables, so we can cancel a removal if need be
    HashMap<Notification, Runnable> pendingRunnables = new HashMap<>();


    public NotificationListAdapter(Context context, List<Notification> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
        itemsPendingRemoval = new ArrayList<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification, viewGroup, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Notification currentNotification = notificationList.get(position);
        NotificationViewHolder viewHolder = (NotificationViewHolder) holder;
        final Context context = holder.itemView.getContext();

        ((NotificationViewHolder) holder).expiredLabel.setVisibility(View.GONE);
        viewHolder.text.setTextColor(ContextCompat.getColor(context, R.color.text_color_dark));
        ((CardView)holder.itemView).setCardBackgroundColor(Color.WHITE);

        ImageView imageView = viewHolder.imageView;
        imageView.setVisibility(View.VISIBLE);
        TextView imageTextView = viewHolder.imageInTextView;
        imageTextView.setVisibility(View.GONE);
        int size = context.getResources().getDimensionPixelSize(R.dimen.notification_image_size);


        if (currentNotification.getType() == Notification.NotificationType.APP_NOTIF) {
            if (currentNotification.getVoucher() != null) {
                viewHolder.text.setText(currentNotification.getVoucher().getTitle());
                holder.itemView.setTag(currentNotification.getVoucher());
                Picasso.with(context).load(currentNotification.getVoucher().getRetailerLogoUrl()).resize(size, size).centerInside().into(imageView);
                setNotificationExpired(currentNotification.getVoucher(), (NotificationViewHolder) holder, true);
            } else {
                viewHolder.text.setText(currentNotification.getRetailerLabel());
                holder.itemView.setTag(currentNotification.getRetailer());
                Picasso.with(context).load(currentNotification.getRetailer().getRetailerLogo()).resize(size, size).centerInside().into(imageView);
            }

        } else if (currentNotification.getType() == Notification.NotificationType.EXPIRED_NOTIF) {
            viewHolder.text.setText(getNotificationTextForExpiringBookmark(currentNotification.getVoucher()));
            holder.itemView.setTag(currentNotification.getVoucher());
            holder.itemView.setTag(Constants.TAG_IS_BOOKMARK, Boolean.TRUE);
            Picasso.with(context).load(currentNotification.getVoucher().getRetailerLogoUrl()).resize(size, size).centerInside().into(imageView);
        } else {
            String text = currentNotification.getTitle() != null ? currentNotification.getTitle() + " - " : "";
            if (currentNotification.getMessage() != null) {
                text += currentNotification.getMessage();
            }
            viewHolder.text.setText(text);
            holder.itemView.setTag(currentNotification.getDeeplink());
            if (currentNotification.getIconUrl() != null) {
                Picasso.with(context).load(currentNotification.getIconUrl()).resize(size, size).centerInside().into(imageView);
            } else {
                if (currentNotification.getDeeplink() != null) {
                    DeepLinkHandler.DeepLinkType type = DeepLinkHandler.getDeepLingType(currentNotification.getDeeplink());
                    if (type != null) {
                        switch (type) {
                            case Category:
                                String categoryId = DeepLinkHandler.getCategoryId(currentNotification.getDeeplink());

                                imageTextView.setText(CategoriesService.getInstance().getCategoryLogo(categoryId));
                                imageTextView.setVisibility(View.VISIBLE);
                                imageView.setVisibility(View.GONE);
                                break;
                            case Retailer:
                            case Voucher:
                                String retailerId = DeepLinkHandler.getRetailerId(currentNotification.getDeeplink());
                                Retailer retailer = RetailerService.getInstance().getRetailerById(retailerId);
                                if (retailer != null) {
                                    String retailerLogoUrl = retailer.getRetailerLogo();
                                    Picasso.with(context).load(retailerLogoUrl).resize(size, size).centerInside().into(imageView);
                                }

                                if(type == DeepLinkHandler.DeepLinkType.Retailer){
                                    break;
                                }

                                String voucherId = DeepLinkHandler.getVoucherId(currentNotification.getDeeplink());
                                VoucherService.getInstance().getVoucherFull(voucherId)
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Consumer<VoucherFull>() {
                                            @Override
                                            public void accept(VoucherFull voucher) throws Exception {
                                                currentNotification.setVoucher(voucher);
                                                NotificationService.getInstance().updateNotification(currentNotification);
                                                setNotificationExpired(voucher, (NotificationViewHolder) holder, voucher.isPublished());
                                            }
                                        });
                                break;
                            case Http:
                                Picasso.with(context).load(R.mipmap.ic_launcher).resize(size, size).centerInside().into(imageView);
                                break;
                            default:
                        }

                    } else {
                        Picasso.with(context).load(R.mipmap.ic_launcher).resize(size, size).centerInside().into(imageView);
                    }
                }
            }
        }


        TextView time = ((NotificationViewHolder) holder).time;
        time.setText(DateUtils.getInstance().dateString(new Date(currentNotification.getDate()),
                DateUtils.DateUtilFormat.DayMonthYear));


        if (itemsPendingRemoval.contains(currentNotification)) {
            viewHolder.undoContainer.setVisibility(View.VISIBLE);
            viewHolder.undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Runnable pendingRemovalRunnable = pendingRunnables.get(currentNotification);
                    pendingRunnables.remove(currentNotification);
                    if (pendingRemovalRunnable != null)
                        handler.removeCallbacks(pendingRemovalRunnable);
                    itemsPendingRemoval.remove(currentNotification);
                    // this will rebind the row in "normal" state
                    notifyItemChanged(notificationList.indexOf(currentNotification));
                }
            });

        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onGridGestureListener != null) {
                        onGridGestureListener.onItemClicked(position, v);
                    }
                }
            });
            viewHolder.undoContainer.setVisibility(View.GONE);
            viewHolder.undoButton.setOnClickListener(null);
        }

        viewHolder.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (!itemsPendingRemoval.contains(currentNotification)) {
                    removeAllPending();
                }
                return false;
            }
        });
        viewHolder.itemView.setTag(R.integer.tag_notification_type, currentNotification.getType());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public void setOnClickListener(OnGridGestureListener onGridGestureListener) {
        this.onGridGestureListener = onGridGestureListener;
    }

    public void setUndoOn(boolean undoOn) {
        this.undoOn = undoOn;
    }

    public boolean isUndoOn() {
        return undoOn;
    }

    public void pendingRemoval(int position) {
        final Notification item = notificationList.get(position);
        if (!itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.add(item);
            // this will redraw row in "undo" state
            notifyItemChanged(position);
            // let's create, store and post a runnable to remove the item
            Runnable pendingRemovalRunnable = new Runnable() {
                @Override
                public void run() {
                    //remove(notificationList.indexOf(item));
                    //NotificationService.getInstance().removeNotification(item);
                }
            };
            handler.postDelayed(pendingRemovalRunnable, PENDING_REMOVAL_TIMEOUT);
            pendingRunnables.put(item, pendingRemovalRunnable);
        }
    }

    public void remove(int position) {
        Notification item = notificationList.get(position);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);
        }
        if (notificationList.contains(item)) {
            notificationList.remove(position);
            notifyItemRemoved(position);
        }
        NotificationService.getInstance().removeNotification(item);
    }

    public void removeAllPending() {
        for (Notification item : itemsPendingRemoval) {
            remove(notificationList.indexOf(item));
        }
    }

    public void removeFromPending(int position) {
        Notification item = notificationList.get(position);
        if (itemsPendingRemoval.contains(item)) {
            itemsPendingRemoval.remove(item);
        }
    }

    public boolean isPendingRemoval(int position) {
        Notification item = notificationList.get(position);
        return itemsPendingRemoval.contains(item);
    }

    private String getNotificationTextForExpiringBookmark(Voucher voucher) {
        String message = context.getString(voucher.isCode() ? R.string.cn_app_expire_code_notiffication : R.string.cn_app_expire_deal_notiffication);
        message = String.format(message, voucher.getRetailerName());

        message = message + TimeUtil.getExpireTimeInHours(voucher.getEndDate(), context);
        return message;
    }

    private void setNotificationExpired(Voucher voucher, NotificationViewHolder holder, boolean isPublished) {
        if ((voucher != null && voucher.getEndDate() != null && TimeUtil.getDiff(voucher.getEndDate()) < 0) || !isPublished) {
            holder.expiredLabel.setVisibility(View.VISIBLE);
            ((CardView) holder.itemView).setCardBackgroundColor(ContextCompat.getColor(context, R.color.background_color_disabled));
            holder.text.setTextColor(ContextCompat.getColor(context, R.color.text_color_light_gray));
            holder.expiredLabel.setTypeface(Typeface.DEFAULT_BOLD);
        }
    }
}
