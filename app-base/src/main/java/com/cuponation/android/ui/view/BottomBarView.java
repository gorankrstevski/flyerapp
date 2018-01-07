package com.cuponation.android.ui.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cuponation.android.R;
import com.cuponation.android.util.AnimationUtil;
import com.cuponation.android.util.SharedPreferencesUtil;

/**
 * Created by goran on 7/22/16.
 */

public class BottomBarView extends LinearLayout {

    private static final int[] icons = new int[]{R.drawable.ic_home, R.drawable.ic_bookmark, R.drawable.ic_heart, R.drawable.ic_alarm, R.drawable.ic_settings};
    private static final int[] active_icons = new int[]{R.drawable.ic_home_active, R.drawable.ic_bookmark_fill, R.drawable.ic_heart_active, R.drawable.ic_alarm_active, R.drawable.ic_settings_active};

    public enum State {
        HOME_SCREEN(0), BOOKMARKS_SCREEN(1), FAVOURITE_SCREEN(2), NOTIFICATION_SCREEN(3), SETTINGS_SCREEN(4);

        private int index;

        private static SparseArray<State> map = new SparseArray<>();

        static {
            for (State state : State.values()) {
                map.put(state.index, state);
            }
        }

        State(final int index) {
            this.index = index;
        }

        public static State valueOf(int index) {
            return map.get(index);
        }

        public int getIndex() {
            return index;
        }
    }

    private ImageView menuItemImageViews[] = new ImageView[icons.length];
    private View menuItemMarkViews[] = new View[icons.length];

    private static final int NOTIFICATION_INDEX = State.NOTIFICATION_SCREEN.index;
    private static final int BOOKMARK_INDEX = State.BOOKMARKS_SCREEN.index;

    private OnMenuItemClickListener onMenuItemClickListener;

    public BottomBarView(Context context) {
        super(context);
        init(context);
    }

    public BottomBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BottomBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BottomBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        loadMenuItems();
    }

    public void loadMenuItems() {

        this.setOrientation(HORIZONTAL);
        this.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.search_bar_background));

        LayoutParams param = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.bottom_bar_height));
        this.setLayoutParams(param);

        LayoutParams itemParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, 1);

        for (int i = 0; i < icons.length; i++) {

            View menuItem = LayoutInflater.from(getContext()).inflate(R.layout.bottom_menu_item, null);
            menuItem.setLayoutParams(itemParams);

            ImageView imageView = (ImageView) menuItem.findViewById(R.id.item_image);
            menuItemImageViews[i] = imageView;

            setMenuItemImage(i, i == 0);

            menuItemMarkViews[i] = menuItem.findViewById(R.id.notification_mark);
            if (i == NOTIFICATION_INDEX) {
                menuItemMarkViews[i].setVisibility(
                        SharedPreferencesUtil.getInstance().isNewNotificationAdded() ?
                                VISIBLE : INVISIBLE);
            } else if (i == BOOKMARK_INDEX) {
                menuItemMarkViews[i].setVisibility(
                        SharedPreferencesUtil.getInstance().isNewBookmarkNotificationAdded() ?
                                VISIBLE : INVISIBLE);
            }

            menuItem.setTag(State.valueOf(i));

            menuItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    State state = (State) v.getTag();
                    setActiveMenuItem(state.index);
                    if (onMenuItemClickListener != null) {
                        onMenuItemClickListener.onItemClick(state, null);
                    }

                    if (state == State.NOTIFICATION_SCREEN) {
                        SharedPreferencesUtil.getInstance().setNewNotificationAdded(false);
                        v.findViewById(R.id.notification_mark).setVisibility(View.INVISIBLE);
                    } else if (state == State.BOOKMARKS_SCREEN) {
                        SharedPreferencesUtil.getInstance().setNewBookmarkNotificationAdded(false);
                        v.findViewById(R.id.notification_mark).setVisibility(View.INVISIBLE);
                    }
                }
            });

            this.addView(menuItem);
        }
    }

    public void setOnMenuItemClick(OnMenuItemClickListener onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    public interface OnMenuItemClickListener {
        void onItemClick(State state, Bundle extras);
    }

    public void setActiveMenuItem(int position) {
        for (int i = 0; i < icons.length; i++) {
            setMenuItemImage(i, i == position);
        }
    }

    private void setMenuItemImage(int position, boolean isActive) {
        menuItemImageViews[position].setImageDrawable(
                ContextCompat.getDrawable(getContext(), isActive ? active_icons[position] : icons[position]));
    }

    public void startBookmarkAnimation(long offset) {
        Animation animation = AnimationUtil.getGrowAndBackToOriginal();
        animation.setStartOffset(offset);
        menuItemImageViews[State.BOOKMARKS_SCREEN.ordinal()].startAnimation(animation);
    }

    public int getBookmarksLeftPosition(Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return (width / icons.length) * State.BOOKMARKS_SCREEN.ordinal();
    }

    public void updateMarksForMenuItems() {
        for (int i = 0; i < menuItemMarkViews.length; i++) {
            if (i == NOTIFICATION_INDEX) {
                menuItemMarkViews[i].setVisibility(
                        SharedPreferencesUtil.getInstance().isNewNotificationAdded() ?
                                VISIBLE : INVISIBLE);
            } else if (i == BOOKMARK_INDEX) {
                menuItemMarkViews[i].setVisibility(
                        SharedPreferencesUtil.getInstance().isNewBookmarkNotificationAdded() ?
                                VISIBLE : INVISIBLE);
            }
        }
    }
}
