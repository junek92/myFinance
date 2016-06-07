package com.kirilov.ivan.myfinance;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Ivan on 01-Jun-16.
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private TypedArray iconTypedArray;

    public ImageAdapter(Context c) {
        this.mContext = c;
        this.iconTypedArray = c.getResources().obtainTypedArray(R.array.walletsIcon);
    }

    public int getCount() {
        // when subtracting 1 will get length of 15 instead of real 16
        return iconTypedArray.length()-1;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        imageView = new ImageView(mContext);

        if (convertView == null) {
            // if it's not recycled, initialize some attributes

            imageView.setLayoutParams(new GridView.LayoutParams(180, 180));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        // add +1 to position, in order to skip the first empty drawable
        // to prevent index out of bounds error -> subtracted -1 from length
        // and now iteration will stop at 15 => 15+1 is exactly array.length()
        imageView.setImageDrawable(iconTypedArray.getDrawable(position+1));

        return imageView;
    }
}
