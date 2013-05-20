package com.shephertz.app42.photo.sharing;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;


import com.shephertz.app42.paas.sdk.android.storage.Storage.JSONDocument;
/*
 * This class is used for User Gallery
 * Show all the images in grid that he/she shared/received
 */
public class FacebookGallery extends Activity {

	private ProgressDialog dialog;
	private GridView gridAlbum;
	private int girdWidth;
	private ArrayList<String> imageUrlsArr;
	private ArrayList<String> friendNamesArr;
	private ArrayList<String> commentsArr;
	private ArrayList<String> photoIdsArr;
	private String keyOwner;
	private int albumType = 0;
/*
 * (non-Javadoc)
 * @see android.app.Activity#onCreate(android.os.Bundle)
 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery);
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		albumType = bundle.getInt(Constants.keyAlbumType);
		setImageOwner(albumType);
		dialog = new ProgressDialog(this);
		gridAlbum = buildGridView();
		loadMyAlbum();
	}

	/*
	 * Check type of album
	 */
	private void setImageOwner(int state) {
		if (state == Constants.sharedAlbum) {
			keyOwner = Constants.keyReceiver;
		} else {
			keyOwner = Constants.keyOwner;
		}
	}
/*
 * Use to load album
 */
	private void loadMyAlbum() {
		if (Utils.haveNetworkConnection(this)) {
			try {
				dialog.setMessage("Loading album.......");
				dialog.show();
				App42ServiceApi.instance().loadMyAlbum(
						UserContext.MyUserName, this, albumType);
			} catch (Exception e) {
				dialog.dismiss();
			}
		}
	}
/*
 * (non-Javadoc)
 * @see android.app.Activity#onStart()
 */
	public void onStart() {
		super.onStart();
		if (!Utils.haveNetworkConnection(this)) {
			showNoConnectionDialog();
		}
	}

	/*
	 * Used to show dialog that show no internet
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
	 * Call back method 
	 * used to parse all information of album
	 * @param albumJson contains all information of album
	 * 
	 */
	 void onSuccess(ArrayList<JSONDocument> albumJson) {
		imageUrlsArr = new ArrayList<String>();
		friendNamesArr = new ArrayList<String>();
		commentsArr = new ArrayList<String>();
		photoIdsArr = new ArrayList<String>();
		System.out.println(albumType);
		for (int i = 0; i < albumJson.size(); i++) {
			try {
				JSONObject jsonData = new JSONObject(albumJson.get(i)
						.getJsonDoc());
				imageUrlsArr.add(jsonData.getString(Constants.keyUrl));
				friendNamesArr.add(jsonData.getString(keyOwner));
				commentsArr.add(jsonData.getString(Constants.keyComment));
				photoIdsArr.add(jsonData.getString(Constants.keyPhotoId));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		dialog.dismiss();
		updateAlbumView();
	}
/*
 * Used to update album
 */
	private void updateAlbumView() {
		gridAlbum.setAdapter(new GalleryAdapter(this, girdWidth, friendNamesArr,
				imageUrlsArr));
	}

	/*
	 * callback method
	 */
	 void onError() {
		dialog.dismiss();
	}

	 /*
	  * Used to set properties of android gridView for album
	  */
	private GridView buildGridView() {
		GridView grid = (GridView) findViewById(R.id.album_grid_view);
		Display display = getWindowManager().getDefaultDisplay();
		girdWidth = (display.getWidth() / 3);
		if (girdWidth > Constants.maxGridWidth) {
			girdWidth = Constants.maxGridWidth;
		}
		grid.setColumnWidth(girdWidth);
		grid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Intent intent = new Intent(FacebookGallery.this,
						FacebookAlbum.class);
				intent.putExtra("index", position);
				intent.putExtra("albumState", albumType);
				intent.putStringArrayListExtra("friends", friendNamesArr);
				intent.putStringArrayListExtra("comments", commentsArr);
				intent.putStringArrayListExtra("imageUrls", imageUrlsArr);
				intent.putStringArrayListExtra("photoIds", photoIdsArr);
				startActivity(intent);

			}
		});
		return grid;
	}

}
