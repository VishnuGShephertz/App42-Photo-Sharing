package com.shephertz.app42.photo.sharing;

import java.util.ArrayList;
import java.util.Date;
import org.json.JSONObject;
import android.os.Handler;
import com.shephertz.app42.paas.sdk.android.ServiceAPI;
import com.shephertz.app42.paas.sdk.android.review.Review;
import com.shephertz.app42.paas.sdk.android.review.ReviewService;
import com.shephertz.app42.paas.sdk.android.social.Social;
import com.shephertz.app42.paas.sdk.android.social.Social.Friends;
import com.shephertz.app42.paas.sdk.android.social.SocialService;
import com.shephertz.app42.paas.sdk.android.storage.Storage;
import com.shephertz.app42.paas.sdk.android.storage.Storage.JSONDocument;
import com.shephertz.app42.paas.sdk.android.storage.StorageService;
import com.shephertz.app42.paas.sdk.android.upload.Upload;
import com.shephertz.app42.paas.sdk.android.upload.UploadFileType;
import com.shephertz.app42.paas.sdk.android.upload.UploadService;

/*
 * This class allows user to integrate with APP42 Service API
 */
public class App42ServiceApi {

	private static App42ServiceApi _instance = null;
	private StorageService storageService;
	private ReviewService reviewService;
	private SocialService socialService;
	private UploadService uploadService;

	/*
	 * Instance of class
	 */
	public static App42ServiceApi instance() {
		if (_instance == null) {
			_instance = new App42ServiceApi();
		}
		return _instance;
	}

	/*
	 * Constructor of class
	 */
	private App42ServiceApi() {
		ServiceAPI serviceApi = new ServiceAPI(Constants.apiKey,
				Constants.secretKey);
		this.storageService = serviceApi.buildStorageService();
		this.reviewService = serviceApi.buildReviewService();
		this.socialService = serviceApi.buildSocialService();
		this.uploadService = serviceApi.buildUploadService();
	}

	/*
	 * This function allows user to load all comments on a photo
	 * 
	 * @param photoId on which comments are loaded
	 * 
	 * @param callback instance of class where we have to return
	 */
	public void loadComments(final String photoID, final FacebookAlbum callback) {
		final Handler callingThreadHandler = new Handler();
		new Thread() {
			@Override
			public void run() {
				try {

					final ArrayList<Review> allComments = reviewService
							.getCommentsByItem(photoID);
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onGetAllComments(allComments);
							}
						}
					});
				} catch (final Exception ex) {
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onError();
							}
						}
					});
				}
			}
		}.start();
	}

	/*
	 * This function allows user to update/add comment on a photo
	 * 
	 * @param userID the id of person who has commented on photo
	 * 
	 * @param photoID on which user commented
	 * 
	 * @param comments
	 */
	public void sendComments(final String userID, final String photoID,
			final String comments) {
		final Handler callingThreadHandler = new Handler();
		new Thread() {
			@Override
			public void run() {
				try {
					reviewService.addComment(userID, photoID, comments);
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {

						}
					});
				} catch (final Exception ex) {
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {

						}
					});
				}
			}
		}.start();
	}

	/*
	 * This function allows user to load all Face-book friends
	 * 
	 * @param userID FB id of user
	 * 
	 * @param accessToken face-book access token
	 * 
	 * @param callback instance of class on which we have to return
	 */
	public void loadAllFriends(final String userID, final String accessToken,
			final FriendList callback) {
		final Handler callingThreadHandler = new Handler();
		new Thread() {
			@Override
			public void run() {
				try {
					Social linkObj = socialService.linkUserFacebookAccount(
							userID, accessToken);
					Social socialObj = socialService
							.getFacebookFriendsFromLinkUser(userID);
					final ArrayList<Friends> friendList = socialObj
							.getFriendList();
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onFriendListFetched(friendList);
							}
						}
					});
				} catch (final Exception ex) {
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onFbError(ex.toString());
							}
						}
					});
				}
			}
		}.start();
	}

	/*
	 * This function allows user to share photo with face-book friend.
	 * 
	 * @param json data contains information of photo user and friend
	 * 
	 * @param callback instance of class on which we have to return
	 */
	public void sharePicToFriend(final JSONObject jsonData,
			final FriendList callback) {
		final Handler callingThreadHandler = new Handler();
		new Thread() {
			@Override
			public void run() {
				try {
					final boolean uploadStatus = uploadPhoto(jsonData);
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onUpload(uploadStatus);
							}
						}
					});
				} catch (final Exception ex) {
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onUpload(false);
							}
						}
					});
				}
			}
		}.start();

	}

	/*
	 * This function allows user to upload photo
	 * 
	 * @param jsonData contains information of photo,user and friend
	 */
	private boolean uploadPhoto(JSONObject jsonData) throws Exception {
		String photoID = "Id" + new Date().getTime();
		Upload uploadObj = uploadService.uploadFileForUser(photoID,
				jsonData.getString(Constants.keyOwner),
				jsonData.getString(Constants.keyUrl), UploadFileType.IMAGE,
				jsonData.getString(Constants.keyComment));
		if (uploadObj.isResponseSuccess()) {
			jsonData.put(Constants.keyUrl, uploadObj.getFileList().get(0)
					.getUrl());
			jsonData.put(Constants.keyPhotoId, photoID);
			return storePhoto(jsonData);
		} else {
			return false;
		}

	}

	/*
	 * This function allows user to store data when photo is successfully
	 * uploaded
	 * 
	 * @param jsonData contains information of photo, user and friend
	 */
	private boolean storePhoto(JSONObject jsonData) {
		Storage response = storageService.insertJSONDocument(Constants.dbName,
				Constants.colName, jsonData.toString());
		if (response.isResponseSuccess()) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * This function allows user to load user Album
	 * 
	 * @param userId
	 * 
	 * @param callback of class on which we have to return
	 * 
	 * @param album State type of album shared/received
	 */
	public void loadMyAlbum(final String userID,
			final FacebookGallery callback, final int albumType) {
		final Handler callingThreadHandler = new Handler();
		new Thread() {
			@Override
			public void run() {
				try {
					final ArrayList<JSONDocument> albumJson = getAlbumDoc(
							userID, albumType);
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onSuccess(albumJson);
							}
						}
					});
				} catch (Exception ex) {
					callingThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (callback != null) {
								callback.onError();
							}
						}
					});
				}
			}
		}.start();

	}

	/*
	 * This function allows user to load user Album
	 * 
	 * @param userId
	 * 
	 * @param albumState type of album shared/received
	 */
	private ArrayList<JSONDocument> getAlbumDoc(String userID, int albumType) {
		Storage response = null;
		if (albumType == Constants.sharedAlbum) {
			response = storageService.findDocumentByKeyValue(Constants.dbName,
					Constants.colName, Constants.keyOwnerId, userID);
		} else {
			response = storageService.findDocumentByKeyValue(Constants.dbName,
					Constants.colName, Constants.keyReceiverId, userID);
		}
		return response.getJsonDocList();
	}

}
