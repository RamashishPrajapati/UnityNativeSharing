package com.ram.unitynativesharing;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.MessageDialog;
import com.facebook.share.widget.ShareDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ramashish Prajapati on 01-06-2019
 **/
public class UnitySharingPlugin {

    CallbackManager callbackManager;
    ShareDialog shareDialog;
    Context context;
    private static UnitySharingPlugin instance;
    Activity activity;
    private static String authority = null;
    String appnotinstall = "Please install the app to share the content";
    String nocontenttoshare = "No content to share";
    String somethingwentwrong = "Something went wrong";

    public UnitySharingPlugin() {
        this.instance = this;
    }

    public static UnitySharingPlugin instance() {
        if (instance == null) {
            instance = new UnitySharingPlugin();
        }
        return instance;
    }


    public void setContext(Context context) {
        this.context = context;

    }

    public void initFacebook(Context context) {
        FacebookSdk.sdkInitialize(context);
    }

    private boolean appInstalledOrNot(String uri) {
        boolean app_installed = false;
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (Exception e) {
            e.printStackTrace();
            app_installed = false;
        }
        return app_installed;
    }

    private static String GetAuthority(Context context) {

        if (authority == null) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PROVIDERS);
                ProviderInfo[] providers = packageInfo.providers;
                if (providers != null) {
                    for (ProviderInfo provider : providers) {
                        if (provider.name.equals(FileProvider.class.getName()) && provider.packageName.equals(context.getPackageName()) && provider.authority.length() > 0) {
                            authority = provider.authority;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("Unity", "Exception:", e);
                Toast.makeText(context, "catch GetAuthority " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        return authority;
    }


    public void showMessage(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }


    public void setFbMethod(final Activity activity) {
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(activity);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
        this.activity = activity;
    }



    public void shareImage_on_Facebook_Sdk(String imagepath, String message) {
        final InputStream imageStream;
        final Bitmap selectedImage;
        try {
            imageStream = context.getContentResolver().openInputStream(Uri.fromFile(new File(imagepath)));
            selectedImage = BitmapFactory.decodeStream(imageStream);

            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(selectedImage)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .setShareHashtag(new ShareHashtag.Builder().setHashtag(message).build())
                    .build();

            shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
            MessageDialog.show((Activity) context, content);

            if (shareDialog.canShow(SharePhotoContent.class)) {
                shareDialog.show(content);
            } else {
                showMessage( "Please install the app to share the content");
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void shareVideo_on_Facebook_Sdk(String videolink, String message) {

        if (!TextUtils.isEmpty(videolink)) {
            Uri uri = Uri.fromFile(new File(videolink));

            ShareVideo shareVideo = new ShareVideo.Builder()
                    .setLocalUrl(uri)
                    .build();

            ShareVideoContent content = new ShareVideoContent.Builder()
                    .setVideo(shareVideo)
                    .setShareHashtag(new ShareHashtag.Builder().setHashtag(message).build())
                    .build();

            // shareDialog.show(content, ShareDialog.Mode.AUTOMATIC);
            if (shareDialog.canShow(ShareVideoContent.class)) {
                shareDialog.show(content);
            } else {
                showMessage( "Please install the app to share the content");
            }
        } else {
            showMessage("Video is not selected");
        }
    }

    /*Share image and video with hashtag and text on twitter*/
    public void shareContent_On_Twitter(Context context, String shareUri, String type, String message) {

        try {
            if (GetAuthority(context) == null) {
                showMessage("Something went wrong");
                return;
            }

            List<Intent> targetShareIntent = new ArrayList<Intent>();
            if (appInstalledOrNot("com.twitter.android")) {
                PackageManager packageManager = context.getPackageManager();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType(type);
                List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0);
                for (int i = 0; i < resolveInfoList.size(); i++) {
                    ResolveInfo resolveInfo = resolveInfoList.get(i);
                    String packageName = resolveInfo.activityInfo.packageName;
                    if (packageName.contains("com.twitter.android")) {
                        if (resolveInfo.activityInfo.name.equalsIgnoreCase("com.twitter.composer.ComposerActivity")) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
                            intent.setAction(Intent.ACTION_SEND);
                            intent.setType(type);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra(Intent.EXTRA_TEXT, message);
                            Uri screenshotUri = FileProvider.getUriForFile(context, authority, new File(shareUri));
                            intent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                            intent.setPackage(packageName);
                            targetShareIntent.add(intent);

                            if (!targetShareIntent.isEmpty()) {
                                Intent chooserIntent = Intent.createChooser(targetShareIntent.remove(0), "Choose app to share");
                                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntent.toArray(new Parcelable[]
                                        {
                                        }));
                                context.startActivity(chooserIntent);
                            } else {
                                showMessage("Something went wrong");
                            }
                        }
                    }
                }
            } else
                showMessage( "Please install the app to share the content");
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("in catch" + e.getMessage());
        }
    }

    /*To share image and video without text on Instagram*/
    private void shareContent_on_Instgram(Context context, String shareUri, String type, String subPackageName) {
        try {
            if (GetAuthority(context) == null) {
                showMessage("Something went wrong");
                return;
            }

            if (appInstalledOrNot("com.instagram.android")) {

                List<Intent> targetShareIntents = new ArrayList<Intent>();
                PackageManager packageManager = context.getPackageManager();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType(type);

                List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(sendIntent, 0);
                for (int j = 0; j < resolveInfoList.size(); j++) {
                    ResolveInfo resInfo = resolveInfoList.get(j);
                    String system_packageName = resInfo.activityInfo.packageName;

                    if (system_packageName.contains("com.instagram.android")) {

                        Intent intent = new Intent();
                        if (!TextUtils.isEmpty(subPackageName))
                            intent.setComponent(new ComponentName(system_packageName, subPackageName));

                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType(type);//Set MIME Type
                        Uri uri = FileProvider.getUriForFile(context, authority, new File(shareUri));
                        intent.putExtra(Intent.EXTRA_STREAM, uri);// Pur Image to intent
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setPackage(system_packageName);
                        targetShareIntents.add(intent);

                        if (!targetShareIntents.isEmpty()) {
                            Intent chooserIntent = Intent.createChooser(targetShareIntents.remove(0), "Choose app to share");
                            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetShareIntents.toArray(new Parcelable[]{}));
                            context.startActivity(chooserIntent);
                        } else {
                        }
                        break;
                    }
                    else {
                        showMessage( "Please install the app to share the content");
                    }
                }

            } else {
                showMessage( "Please install the app to share the content");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*To share image and video with text and hashtag on Whatsapp */
    public void openWhatsApp(Context context, String shareUri, String type, String message, String phone_number) {
        try {
            if (GetAuthority(context) == null) {
                showMessage("Something went wrong");
                return;
            }
            if (appInstalledOrNot("com.whatsapp")) {

                if (!TextUtils.isEmpty(phone_number)) {
                    String toNumber = phone_number;
                    Intent sendIntent = new Intent("android.intent.action.MAIN");
                    Uri uri = FileProvider.getUriForFile(context, authority, new File(shareUri));
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    sendIntent.putExtra("jid", toNumber + "@s.whatsapp.net");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, message);
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setPackage("com.whatsapp");
                    sendIntent.setType(type);
                    context.startActivity(sendIntent);
                } else {
                    showMessage("No mobile number to share");
                }
            } else {
                showMessage( "Please install the app to share the content");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
