# UnityNativeSharing
This library(Plugin) is use for sharing image/video with hashtag/message on social media platform from unity app using native sharing option.
Now, for sharing from unity you need to create a class extending MonoBehaviour and use below code to share image/video with hashtag
and create jar file of above project and add it to your unity project.
  
  
 
 public void Share()
	{
 		AndroidJavaObject sharingImageVideo = null;
		AndroidJavaObject activityContext = null;
			
      using(AndroidJavaClass activityClass = new AndroidJavaClass("com.unity3d.player.UnityPlayer"))
			{
				activityContext = activityClass.GetStatic < AndroidJavaObject > ("currentActivity");
			}
			using(AndroidJavaClass pluginClass = new AndroidJavaClass("com.ram.unitynativesharing.UnitySharingPlugin")) //packagename with classname
			{
				if (pluginClass != null)
				{
					sharingImageVideo = pluginClass.CallStatic < AndroidJavaObject > ("instance");
					sharingImageVideo.Call("setContext", activityContext);
				
           //Sharing image/video on twitter with hashtag 
						sharingImageVideo.Call ("shareContent_on_Twitter", activityContext, image/video path, type, hashTag);
					
				}
			}
	}
  
  For sharing image/video on twitter, call  "shareContent_on_Twitter" method. In that pass "activityContext" with image / video uri path 
  and "type" if passing then type is  "image/*" or if passing video then type "video/*" and at end hashtag text.
  
  
  For sharing image/video on instgram call "shareContent_on_Instgram" method. In that pass "activityContext" with image / video uri path 
  and "type" if passing then type is  "image/*" or if passing video then type "video/*" and at end hashtag text.
  
  For sharing image/video on whatsapp call "shareContent_on_Whatsapp" method. In that pass "activityContext" with image / video uri path 
  and "type" if passing then type is  "image/*" or if passing video then type "video/*" and hashtag/message text and phone number of user.
  
  For sharing on Facebook install facebook sdk and call "shareImage_on_Facebook_Sdk" method to share image with imagepath uri and hashtag/message.
  For sharing video on Facebook call method "shareVideo_on_Facebook_Sdk" and pass video uri and hastag/message
  
  In Androidmanifest.xml file of Unity, add file-provide as mentioned below, 
  in authorities attribute add package name of your app, if you are changing it.
  
  <provider
               android:name="android.support.v4.content.FileProvider"
               android:authorities="com.ram.unitynativesharing.UnitySSContentProvider"
               android:exported="false"
               android:grantUriPermissions="true">
      <meta-data
          android:name="android.support.FILE_PROVIDER_PATHS"
          android:resource="@xml/provider_paths" />
    </provider>
 
