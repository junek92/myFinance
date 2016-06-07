package com.kirilov.ivan.myfinance.junk_yard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kirilov.ivan.myfinance.R;

/**
 * Created by Ivan on 28-Apr-15.
 */
public class NavigationAdapter extends BaseAdapter{

    Context mContext;
    LayoutInflater mLayoutInflater;
    String[] mMenuText = {
            "This month",
            "History",
            "Analyse",
            "Settings",
            "About"};

    int[] mMenuImg = {
            R.drawable.ic_event_white_48dp,
            R.drawable.ic_history_white_48dp,
            R.drawable.ic_trending_up_white_48dp,
            R.drawable.ic_settings_black_48dp,
            R.drawable.ic_info_outline_black_48dp};

    public NavigationAdapter(Context context, LayoutInflater layoutInflater){
        mContext = context;
        mLayoutInflater = layoutInflater;

    }

    private static class ViewHolder{
        // Used to pack the views in every row into single object
        public ImageView mImageView;
        public TextView mTextView;
    }

    @Override
    public int getCount() {
        return mMenuText.length;
    }

    @Override
    public Object getItem(int position) {
        return mMenuText[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        // check if the view already exists
        // if so, no need to inflate and findViewById again!
        if (convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.row_navigation, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mTextView = (TextView) convertView.findViewById(R.id.nav_text);
            viewHolder.mImageView = (ImageView) convertView.findViewById(R.id.nav_img);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mTextView.setText(mMenuText[position]);
        viewHolder.mImageView.setImageResource(mMenuImg[position]);

        // set custom color filter for checked ListView rows
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {    // >= Lollipop +
            ColorStateList myColorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{android.R.attr.state_activated},   //active state
                            new int[]{}                                  //default state
                    },
                    new int[] {
                            mContext.getResources().getColor(R.color.primaryColorDark),       //active color
                            mContext.getResources().getColor(R.color.myBlack)         //default color
                    }
            );
            viewHolder.mImageView.setImageTintList(myColorStateList);
            viewHolder.mImageView.setImageTintMode(PorterDuff.Mode.MULTIPLY);
        }
        else {                                                          //  < Lollipop
            viewHolder.mImageView.setColorFilter(Color.BLACK);
        }

        return convertView;
    }
}
