package com.shephertz.app42.photo.sharing;

import java.util.ArrayList;


import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/*
 * used to load image in grid of user album
 */
public class GalleryAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<String> imageUrls;
	private ArrayList<String> friendName;
	private int gridWidth;
	private final int GridSpace = 10;

	// Constructor
	public GalleryAdapter(Context context,int gridWidth,ArrayList<String> frindList,ArrayList<String> urlList) {
		mContext = context;
		this.gridWidth = gridWidth - GridSpace;
		this.friendName=frindList;
		this.imageUrls=urlList;

	}

	@Override
	public int getCount() {
		return friendName.size();
	}

	@Override
	public Object getItem(int position) {
		return friendName.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LinearLayout linLay = new LinearLayout(mContext);
		linLay.setOrientation(LinearLayout.VERTICAL);
		linLay.setGravity(Gravity.CENTER);
		linLay.setPadding(0, GridSpace, 0, 0);
		ImageView imageView = new ImageView(mContext);//
		imageView.setLayoutParams(new LinearLayout.LayoutParams(gridWidth,
				gridWidth));
		imageView.setBackgroundResource(R.drawable.default_background);
		Utils.loadImageGridFromUrl(imageView,
				 imageUrls.get(position),gridWidth);
		TextView tvSerNumber = new TextView(mContext);
		tvSerNumber.setText(friendName.get(position));
		tvSerNumber.setGravity(Gravity.CENTER);
		tvSerNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
		tvSerNumber.setTextColor(Color.BLACK);
		linLay.addView(imageView);
		linLay.addView(tvSerNumber);
		return linLay;
	}
}