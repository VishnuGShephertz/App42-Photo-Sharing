package com.shephertz.app42.photo.sharing;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.shephertz.app42.paas.sdk.android.social.Social.Friends;

/*
 * This class is used to show facebook friendlist
 */
public class FriendList extends Activity implements OnItemClickListener {
	private ListView friendList;
	private List<Friends> fbFriendList;
	private List<Friends> searchFriendList;
	private final int LoadImageResult = 1;
	private ProgressDialog dialog;
	private int index = 0;
	private boolean searchTag = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_list);
		dialog = new ProgressDialog(this);
		Utils.clearCache(this);
		loadMyFriendList();
	}

	/*
	 * Used to load friend list if user is authenticated else autherize user
	 * first
	 */
	private void loadMyFriendList() {
		if (Utils.haveNetworkConnection(this)) {
			dialog.setMessage("Loading data...");
			dialog.show();
			FacebookService.instance().setContext(getApplicationContext());
			if (!Utils.isAuthenticated()) {
				FacebookService.instance().fetchFacebookProfile(this);
			} else {
				UserContext.authorized = true;
				App42ServiceApi.instance().loadAllFriends(
						UserContext.myUserName, UserContext.accessToken, this);
			}
		}

	}

	/*
	 * Check internet connectivity (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStart()
	 */
	public void onStart() {
		super.onStart();
		if (!Utils.haveNetworkConnection(this)) {
			showErrorDialog("Error in Network Connection!");
		}
	}

	/*
	 * Show dialog with no connection
	 */
	private void showErrorDialog(String message) {
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
		alt_bld.setMessage(message).setCancelable(false)
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
	 * Used to show image preview that user want to share with selected friend
	 * 
	 * @param imgPath path of image
	 */
	private void previewImagePopup(final String imgPath) {

		final Dialog dialog = new Dialog(this,
				android.R.style.Theme_Translucent);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(true);
		dialog.setContentView(R.layout.preview);
		ImageView imageView = (ImageView) dialog.findViewById(R.id.upload_img);
		final EditText edComments = (EditText) dialog
				.findViewById(R.id.share_content);
		imageView.setImageBitmap(BitmapFactory.decodeFile(imgPath));
		dialog.show();

		Button cancel = (Button) dialog.findViewById(R.id.cancel);
		Button upload = (Button) dialog.findViewById(R.id.upload);
		cancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		upload.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				String comments = edComments.getText().toString().trim();
				buildJsonDoc(comments, imgPath);
			}
		});
	}

	/*
	 * Used to make json object that contains information of image that is uploaded
	 * @param comment on image
	 * @param imagepath
	 */
	private void buildJsonDoc(String comment, String imgPath) {
		JSONObject jsonData = new JSONObject();
		if (comment.equals("") || comment == null) {
			comment = "Hi....";
		}
		String msg;
		try {
			jsonData.put(Constants.KeyOwner, UserContext.myDisplayName);
			jsonData.put(Constants.KeyOwnerId, UserContext.myUserName);
			if (searchTag) {
				jsonData.put(Constants.KeyReceiver, searchFriendList.get(index)
						.getName());
				jsonData.put(Constants.KeyReceiverId,
						searchFriendList.get(index).getId());
			} else {
				jsonData.put(Constants.KeyReceiver, fbFriendList.get(index)
						.getName());
				jsonData.put(Constants.KeyReceiverId, fbFriendList.get(index)
						.getId());
			}
			jsonData.put(Constants.KeyUrl, imgPath);
			jsonData.put(Constants.KeyComment, comment);
			App42ServiceApi.instance().sharePicToFriend(jsonData, this);
		} catch (JSONException e) {
			msg = "Exception " + e;
			Toast.makeText(this, "Upload Falied " + e, Toast.LENGTH_SHORT)
					.show();
		} catch (Exception e) {
			msg = "Exception " + e;
			Toast.makeText(this, "Upload Falied " + e, Toast.LENGTH_SHORT)
					.show();

		}

	}

	/*
	 * Used to show Toast message when image is uploaded/failed
	 */
	 void onUpload(boolean uploadStatus) {
		if (uploadStatus) {
			Toast.makeText(this, "Uplodes successfully", Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(this, "Upload Falied", Toast.LENGTH_SHORT).show();
		}

	}

	 /*
	  * Callback when user select image from gallery for upload
	  * and call previewImagePopup for preview
	  * User autherize with facebook on first time and he has to send autherize callback
	  * 
	  * (non-Javadoc)
	  * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	  */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == LoadImageResult && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			System.out.println(picturePath);
			cursor.close();
			previewImagePopup(picturePath);
		} else if (!UserContext.authorized) {
			FacebookService.instance().authorizeCallback(requestCode,
					resultCode, data);
			UserContext.authorized = true;
		}

	}

	/*
	 * Callback when we autherize with facebook
	 * If succes load my friend list
	 */
	 void onFacebookProfileRetreived(boolean isSuccess) {
		// override this method
		if (isSuccess) {
			App42ServiceApi.instance().loadAllFriends(
					UserContext.myUserName, UserContext.accessToken, this);
		}
		else{
			dialog.dismiss();
		}
	}

	/*
	 * * This method is called when a Activty is stop disable all the events if
	 * occuring (non-Javadoc)
	 * 
	 * @see android.app.Activity#onStop()
	 */
	public void onStop() {
		super.onStop();

	}

	/*
	 * This method is called when a Activty is finished or user press the back
	 * button (non-Javadoc)
	 * 
	 * @override method of superclass
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	public void onDestroy() {
		super.onDestroy();
	}

	/*
	 * called when this activity is restart again
	 * 
	 * @override method of superclass
	 */
	public void onReStart() {
		super.onRestart();
	}

	/*
	 * called when activity is paused
	 * 
	 * @override method of superclass (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	public void onPause() {
		super.onPause();
	}

	/*
	 * called when activity is resume
	 * 
	 * @override method of superclass (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	public void onResume() {
		super.onResume();
	}

	/*
	 * used to create alert dialog when logout option is selected
	 * @param name of friend whom you want to sahre image
	 */
	public void browsePhotoDialog(String name) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setTitle("Share Photo to");
		alertbox.setMessage(name);
		alertbox.setIcon(R.drawable.ic_launcher);
		alertbox.setPositiveButton("Browse",
				new DialogInterface.OnClickListener() {
					// do something when the button is clicked
					public void onClick(DialogInterface arg0, int arg1) {
						browsePhoto();
					}
				});
		alertbox.setNegativeButton("Skip",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});
		alertbox.show();
	}

	/*
	 * Call when user clicks on browse photo
	 */
	private void browsePhoto() {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, LoadImageResult);
	}

	/*
	 * Used to show facebook friend list when user gets friends information
	 * @param friendsInfo contains information of friends like id/picUrl/name/application installed status
	 */
	private void showFriendList(ArrayList<Friends> friendsInfo) {
		setContentView(R.layout.friend_list);
		friendList = (ListView) findViewById(R.id.friend_list);
		fbFriendList = new ArrayList<Friends>();
		searchFriendList = new ArrayList<Friends>();
		int size = friendsInfo.size();
		for (int i = 0; i < size; i++) {
			fbFriendList.add(friendsInfo.get(i));
		}
		friendList.setAdapter(new ActionListAdapter(this, R.id.friend_list,
				fbFriendList));
		friendList.setOnItemClickListener(this);
//		Button shared = (Button) findViewById(R.id.shared);
//		shared.setOnClickListener(new Button.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				goToGallery(Constants.sharedAlbum);
//			}
//		});
//
//		Button received = (Button) findViewById(R.id.received);
//		received.setOnClickListener(new Button.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				goToGallery(Constants.sharedAlbum);
//			}
//		});

		final EditText search = (EditText) findViewById(R.id.search);
		search.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				showSearchList(s, start, before, count, search);

			}
		});

	}

	/*
	 * Used to handle click event on received button
	 */
	public void onReceiveAlbumClicked(View receivedBtn){ 
		goToGallery(Constants.ReceivedAlbum);
	}
	/*
	 * Used to handle click event on shared button
	 */
	public void onSharedAlbumClicked(View sharedBtn){ 
		goToGallery(Constants.SharedAlbum);
	}
			
	/*
	 * Used to navigate on MyGallery to view album
	 */
	private void goToGallery(int albumState) {
		Intent intent = new Intent(this, FacebookGallery.class);
		intent.putExtra(Constants.KeyAlbumType, albumState);
		startActivity(intent);

	}

	/*
	 * Used to show search list when user search his friend
	 */
	private void showSearchList(CharSequence s, int start, int before,
			int count, EditText search) {
		int textlength = search.getText().length();
		searchTag = true;
		searchFriendList.clear();
		for (int i = 0; i < fbFriendList.size(); i++) {
			if (textlength <= fbFriendList.get(i).getName().length()) {
				if (search
						.getText()
						.toString()
						.equalsIgnoreCase(
								(String) fbFriendList.get(i).getName()
										.subSequence(0, textlength))) {
					searchFriendList.add(fbFriendList.get(i));
				}
			}
		}
		friendList.setAdapter(new ActionListAdapter(this, R.id.friend_list,
				searchFriendList));

	}

	/*
	 * Call back method when friend list is fethed
	 */
	 void onFriendListFetched(ArrayList<Friends> fbFriends) {
		dialog.dismiss();
		showFriendList(fbFriends);
	}

	 /*
	  * Callback on error
	  */
	 void onFbError(String errorMsg) {
		dialog.dismiss();
		showErrorDialog(errorMsg);
	}

	 /*
	  * Used to show friend list
	  */
	private class ActionListAdapter extends ArrayAdapter<Friends> {
		private List<Friends> listElementAdapter;
		Context context;

		public ActionListAdapter(Context context, int resourceId,
				List<Friends> listElements) {
			super(context, resourceId, listElements);
			this.context = context;
			this.listElementAdapter = listElements;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.list_item, null);
			}

			Friends friend = listElementAdapter.get(position);
			if (friend != null) {

				ImageView picIcon = (ImageView) view
						.findViewById(R.id.profile_pic);
				TextView friendName = (TextView) view
						.findViewById(R.id.friend_name);
			
				if (picIcon != null) {
					Utils.loadImageFromUrl(picIcon, friend.getPicture());
				}

				if (friendName != null) {
					friendName.setText(friend.getName());
				}
		
			}
			return view;
		}

	}
/*
 * Used to browse dialog when friend is clicked
 * (non-Javadoc)
 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {

		index = pos;
		try {
			if (searchTag) {
				browsePhotoDialog(searchFriendList.get(pos).getName());

			} else {
				browsePhotoDialog(fbFriendList.get(pos).getName());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * used to create menu
	 */
	private void CreateMenu(Menu menu) {
		menu.setQwertyMode(true);
		menu.add(0, 0, 0, "Refresh").setIcon(R.drawable.refresh);

	}

	/*
	 * used to handle selection of option menu
	 */
	private boolean MenuChoice(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			loadMyFriendList();
			return true;

		}
		return false;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	// ---only created once---
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		CreateMenu(menu);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return MenuChoice(item);
	}

}
