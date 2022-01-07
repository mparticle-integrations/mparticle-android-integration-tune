package com.mparticle.kits.mobileapptracker;

import android.content.Context;
import android.provider.Settings;

import com.mparticle.internal.MPUtility;
import com.mparticle.kits.KitUtils;

public class MATDeferredDplinkr {
    private String advertiserId;
    private String conversionKey;
    private String packageName;
    private String googleAdvertisingId;
    private int isLATEnabled;
    private String androidId;
    private String userAgent;
    private MATDeeplinkListener listener;

    private static volatile MATDeferredDplinkr dplinkr;
    
    private MATDeferredDplinkr() {
        advertiserId = null;
        conversionKey = null;
        packageName = null;
        googleAdvertisingId = null;
        isLATEnabled = 0;
        androidId = null;
        userAgent = null;
        listener = null;
    }
    
    public static synchronized MATDeferredDplinkr initialize(String advertiserId, String conversionKey, String packageName) {
        dplinkr = new MATDeferredDplinkr();
        dplinkr.advertiserId = advertiserId;
        dplinkr.conversionKey = conversionKey;
        dplinkr.packageName = packageName;
        return dplinkr;
    }
    
    public void setAdvertiserId(String advertiserId) {
        dplinkr.advertiserId = advertiserId;
    }
    
    public String getAdvertiserId() {
        return dplinkr.advertiserId;
    }
    
    public void setConversionKey(String conversionKey) {
        dplinkr.conversionKey = conversionKey;
    }
    
    public String getConversionKey() {
        return dplinkr.conversionKey;
    }
    
    public void setPackageName(String packageName) {
        dplinkr.packageName = packageName;
    }
    
    public String getPackageName() {
        return dplinkr.packageName;
    }
    
    public void setUserAgent(String userAgent) {
        dplinkr.userAgent = userAgent;
    }
    
    public String getUserAgent() {
        return dplinkr.userAgent;
    }
    
    public void setGoogleAdvertisingId(String googleAdvertisingId, int isLATEnabled) {
        dplinkr.googleAdvertisingId = googleAdvertisingId;
        dplinkr.isLATEnabled = isLATEnabled;
    }
    
    public String getGoogleAdvertisingId() {
        return dplinkr.googleAdvertisingId;
    }
    
    public int getGoogleAdTrackingLimited() {
        return dplinkr.isLATEnabled;
    }
    
    public void setAndroidId(String androidId) {
        dplinkr.androidId = androidId;
    }
    
    public String getAndroidId() {
        return dplinkr.androidId;
    }
    
    public void setListener(MATDeeplinkListener listener) {
        dplinkr.listener = listener;
    }
    
    public MATDeeplinkListener getListener() {
        return dplinkr.listener;
    }
    
    public void checkForDeferredDeeplink(final Context context, final MATUrlRequester urlRequester) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                // If advertiser ID, conversion key, or package name were not set, return
                if (dplinkr.advertiserId == null || dplinkr.conversionKey == null || dplinkr.packageName == null) {
                    if (listener != null) {
                        listener.didFailDeeplink("Advertiser ID, conversion key, or package name not set");
                    }
                }
                if (dplinkr.googleAdvertisingId == null) {
                    MPUtility.AdIdInfo adIdInfo = MPUtility.getAdIdInfo(context);

                    if (adIdInfo != null && adIdInfo.advertiser == MPUtility.AdIdInfo.Advertiser.GOOGLE) {
                        dplinkr.googleAdvertisingId = adIdInfo.id;
                        dplinkr.isLATEnabled = adIdInfo.isLimitAdTrackingEnabled ? 1 : 0;
                    }else {
                        dplinkr.setAndroidId(KitUtils.getAndroidID(context));
                    }
                }
                // If no device identifiers collected, return
                if (dplinkr.googleAdvertisingId == null && dplinkr.androidId == null) {
                    if (listener != null) {
                        listener.didFailDeeplink("No device identifiers collected");
                    }
                }
                
                // Query for deeplink url
                urlRequester.requestDeeplink(dplinkr);
            }
        }).start();
    }
}
