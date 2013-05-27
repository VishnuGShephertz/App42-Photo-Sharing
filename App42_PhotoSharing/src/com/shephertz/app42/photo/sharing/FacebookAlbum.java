package com.shephertz.app42.photo.sharing;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
/*
 * used to show image with comments 
 * We can slide it to get another image of my album
 */

import com.shephertz.app42.paas.sdk.android.review.Review;

public class FacebookAlbum extends Activity {

	private int imageIndex = 0;
	private boolean leftSweap = false;
	private TextView friendName, txtComments;
	private ImageView slidingimage;
	private int albumSize = 0;
	private int imageHgt = 0;
	private ProgressDialog dialog;
	private int albumState;
	private EditText edComment;
	private String photoId;
	private String space = "\n" + "me :";
	private ArrayList<String> imageUrls;
	private ArrayList<String> friendNamesArr;
	private ArrayList<String> commentsArr;
	private ArrayList<String> photoIdsArr;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.album);
		getParameters();
		imageHgt = getImageHeight();
		albumSize = friendNamesArr.size();
		slidingimage = (ImageView) findViewById(R.id.album_pic);
		edComment = (EditText) findViewById(R.id.comment_photo);
		dialog = new ProgressDialog(this);
		friendName = (TextView) findViewById(R.id.page_header);
		txtComments = (TextView) findViewById(R.id.comment_text);

		friendName.setText(friendNamesArr.get(imageIndex));

		ActivitySwipeDetector activitySwipeDetector = new ActivitySwipeDetector(
				this);
		ScrollView scrollview = (ScrollView) this.findViewById(R.id.scrollview);
		LinearLayout lowestLayout = (LinearLayout) this
				.findViewById(R.id.layout_slider);
		scrollview.setOnTouchListener(activitySwipeDetector);
		String Url = imageUrls.get(imageIndex);
		showLoading("Loading image");
		Utils.loadAlbumPic(Url, imageHgt, this);

	}

	/*
	 * This function allows user to send comment
	 */
	public void sendMessage(View v) {
		String comments = edComment.getText().toString();
		if (comments.length() > 0) {
			App42ServiceApi.instance().sendComments(UserContext.MyUserName,
					photoId, comments);
			String prevComments = txtComments.getText().toString();
			txtComments.setText(prevComments + space + comments);
			edComment.setText("");

		}
	}

	/*
	 * This function allows to load parameter from intent
	 */
	private void getParameters() {
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		albumState = bundle.getInt("albumState");
		imageIndex = bundle.getInt("index");
		friendNamesArr = bundle.getStringArrayList("friends");
		commentsArr = bundle.getStringArrayList("comments");
		imageUrls = bundle.getStringArrayList("imageUrls");
		photoIdsArr = bundle.getStringArrayList("photoIds");

	}

	/*
	 * Check internet connectivity (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	public void onStart() {
		super.onStart();
		if (!Utils.haveNetworkConnection(this)) {
			showNoConnectionDialog();
		}
	}

	/*
	 * This function allows to show no internet dialog
	 */
	private void showNoConnectionDialog() {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage("Error in Network Connection!").setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Action for 'Yes' Button
						finish();
					}
				});

		AlertDialog alert = alt_bld.create();
		alert.setTitle("Error!");
		alert.setIcon(R.drawable.ic_launcher);
		alert.show();
	}

	/*
	 * This function call when user slide on image
	 */
	private void AnimateandSlideShow() {
		if (leftSweap) {
			imageIndex++;
		} else {
			if (imageIndex > 0) {
				imageIndex--;
			} else {
				imageIndex = albumSize - 1;
			}
		}
		int index = imageIndex % albumSize;
		friendName.setText(friendNamesArr.get(index));
		String url = imageUrls.get(index);
		showLoading("Laoding image");
		Utils.loadAlbumPic(url, imageHgt, this);
	}

	/*
	 * This function allows user to show loading bar
	 */
	private void showLoading(String message) {
		dialog.setMessage(message + "...");
		dialog.show();
	}

	/*
	 * Callback method called when image bitmap is loaded commented list
	 */
	void onLoadImage(Bitmap bitmapImage) {
		dialog.dismiss();
		slidingimage.setImageBitmap(bitmapImage);
		Animation rotateimage = AnimationUtils.loadAnimation(this,
				R.anim.custom_anim);
		slidingimage.startAnimation(rotateimage);
		showLoading("Loading comments");
		photoId = photoIdsArr.get(imageIndex);
		if (albumState == Constants.sharedAlbum) {
			txtComments.setText(space + commentsArr.get(imageIndex));
		} else {
			txtComments.setText("\n friend :" + commentsArr.get(imageIndex));
		}
		App42ServiceApi.instance().loadComments(photoId, this);

	}

	/*
	 * Callback method error is there when app42 service is called
	 */
	void onError() {
		dialog.dismiss();
	}

	/*
	 * This function allows user to get image height for sampling of image
	 */
	private int getImageHeight() {
		Display display = getWindowManager().getDefaultDisplay();
		int height = display.getHeight();
		return height / 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
		Log.d("method called", "Stop method called");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
		slidingimage = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.app.Activity#onConfigurationChanged(android.content.res.Configuration
	 * )
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		imageHgt = getImageHeight();

	}

	/*
	 * Callback method allows user to parse all comments on image
	 */
	void onGetAllComments(ArrayList<Review> arrAllComments) {
		dialog.dismiss();
		String allComments = "";
		for (int i = 0; i < arrAllComments.size(); i++) {
			Review review = arrAllComments.get(i);
			if (review.getUserId().equals(UserContext.MyUserName)) {
				allComments += space + review.getComment();
			} else {
				allComments += " \n friend :" + review.getComment();
			}
		}
		txtComments.setText(txtComments.getText().toString() + allComments);

	}

	/*
	 * This function allows user to handle sliding event when image is slide
	 */
	private class ActivitySwipeDetector implements View.OnTouchListener {
		private final String logTag = "ActivitySwipeDetector";
		private Activity activity;
		static final int MIN_DISTANCE = 100;
		private float downX, downY, upX, upY;

		public ActivitySwipeDetector(Activity activity) {
			this.activity = activity;
		}

		/*
		 * Right to left sweap
		 */
		public void onRightToLeftSwipe() {
			Log.i(logTag, "RightToLeftSwipe!");
			leftSweap = true;
			AnimateandSlideShow();
		}

		/*
		 * Left to right sweap
		 */
		public void onLeftToRightSwipe() {
			Log.i(logTag, "LeftToRightSwipe!");
			leftSweap = false;
			AnimateandSlideShow();
		}

		/*
		 * Up to bottom sweap
		 */
		public void onTopToBottomSwipe() {
			Log.i(logTag, "onTopToBottomSwipe!");
			// activity.doSomething();
		}

		/*
		 * bottom to up sweap
		 */
		public void onBottomToTopSwipe() {
			Log.i(logTag, "onBottomToTopSwipe!");
			// activity.doSomething();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.view.View.OnTouchListener#onTouch(android.view.View,
		 * android.view.MotionEvent)
		 */
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				downX = event.getX();
				downY = event.getY();
				return true;
			}
			case MotionEvent.ACTION_UP: {
				upX = event.getX();
				upY = event.getY();

				float deltaX = downX - upX;
				float deltaY = downY - upY;

				// swipe horizontal?
				if (Math.abs(deltaX) > MIN_DISTANCE) {
					// left or right
					if (deltaX < 0) {
						this.onLeftToRightSwipe();
						return true;
					}
					if (deltaX > 0) {
						this.onRightToLeftSwipe();
						return true;
					}
				} else {
					Log.i(logTag, "Swipe was only " + Math.abs(deltaX)
							+ " long, need at least " + MIN_DISTANCE);
					return false; // We don't consume the event
				}

				// swipe vertical?
				if (Math.abs(deltaY) > MIN_DISTANCE) {
					// top or down
					if (deltaY < 0) {
						this.onTopToBottomSwipe();
						return true;
					}
					if (deltaY > 0) {
						this.onBottomToTopSwipe();
						return true;
					}
				} else {
					Log.i(logTag, "Swipe was only " + Math.abs(deltaX)
							+ " long, need at least " + MIN_DISTANCE);
					return false; // We don't consume the event
				}

				return true;
			}
			}
			return false;
		}

	}

}
