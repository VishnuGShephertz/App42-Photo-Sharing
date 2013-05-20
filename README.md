App42-Photo-Sharing
===================

# Runnnig Sample

This is a sample Android social app is made using App42 backened platform. It uses social, upload, storage and review service App42 platform. Here are the few easy steps to run this sample app.

1. [Register] (https://apphq.shephertz.com/register) with App42 platform
2. Create an app once you are on Quickstart page after registeration.
3. Goto dashboard and create a new game PhotoSharingApp (Click on Business service manager->game service->add game->)
4. If you are already registered, login to [AppHQ] (http://apphq.shephertz.com) console and create an app from App Manager tab and do step #3 to create a game.
5. Download the eclipse project from this repo and import it in the same.
6. Open Constants.java in sample app and give the value of app42APIkey app42SecretKey that you have received in step 2 or 4
7. [Download Facebook SDk] (https://github.com/facebook/facebook-android-sdk) and add it as a library project in your application
8. You can also modify your FB_APP_ID n Constants.java file. Read our blog post for more help about creating facebook app and its configuration.
9. Build and Run 



# Design Details:

__Initialize Services:__

Initialization has been done in App42ServiceApi.java

```
     ServiceAPI serviceApi = new ServiceAPI(Constants.apiKey,
  			Constants.secretKey);
		this.storageService = serviceApi.buildStorageService();
		this.reviewService = serviceApi.buildReviewService();
		this.socialService = serviceApi.buildSocialService();
		this.uploadService = serviceApi.buildUploadService();
```

__Get Facebook Friends:__

This has been done in  App42ServiceApi.java

```
                   Social linkObj = socialService.linkUserFacebookAccount(userID,
							accessToken);
		   Social socialObj = socialService.getFacebookFriendsFromLinkUser(userID);
		   final ArrayList<Friends> friendList =socialObj.getFriendList();
```
__Share Photo With Friend:__

This has been done in  App42ServiceApi.java : First upload photo

```
                  	String photoID = "Id" + new Date().getTime();
	         	Upload uploadObj = uploadService.uploadFileForUser(photoID,
				jsonData.getString(Constants.keyOwner),
				jsonData.getString(Constants.keyUrl), UploadFileType.IMAGE,
				jsonData.getString(Constants.keyComment));
```
Than store photo information :

```
                  Storage response = storageService.insertJSONDocument(Constants.dbName,
				Constants.colName, jsonData.toString());
```

__Load My Album:__

This has been done in  App42ServiceApi.java : Shared photos

```
                  Storage  response = storageService.findDocumentByKeyValue(Constants.dbName,
					Constants.colName, Constants.keyOwnerId, userID);
```
Received photos:

```
                  Storage response =storageService.findDocumentByKeyValue(Constants.dbName,
					Constants.colName, Constants.keyReceiverId, userID);
```

__Load Comments:__

This has been done in  App42ServiceApi.java :

```
                  ArrayList<Review> allComments = reviewService.getCommentsByItem(photoID);
```

__Add Comments:__

This has been done in  App42ServiceApi.java :

```
            	reviewService.addComment(userID, photoID, comments);
```

