package production.bcf.com.wfpackage1.service;

import android.os.Bundle;
import android.util.Log;

import com.bcf.watchface.bcfwearcore.WatchFaceUtility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

import production.bcf.com.wfpackage1.WFPg1TheBall;

/**
 * Created by annguyenquocduy on 1/13/15.
 */

public class ListenerService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "WatchFaceSerivceLS";
    private static final boolean D = true;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(messageEvent == null)
            return;

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API).build();
        }

        if(!mGoogleApiClient.isConnected()) {
            ConnectionResult connectioRS = mGoogleApiClient.blockingConnect(30,
                    TimeUnit.SECONDS);

            if(!connectioRS.isSuccess()) {
                if(D) Log.d(TAG, "Connection to google api was failed!");
            }
        }

        if(messageEvent.getPath().equals(WFPg1TheBall.CONFIG_PATH)) {
            byte[] data = messageEvent.getData();

            DataMap configKeyToOverride = DataMap.fromByteArray(data);

            if(D) Log.d(TAG, "Received Data config message " + configKeyToOverride);

            WatchFaceUtility.overwriteKeysInConfigDataMap(mGoogleApiClient,
                    configKeyToOverride, WFPg1TheBall.CONFIG_PATH);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if(D) Log.d(TAG, "Google API connected status " + bundle);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if(D) Log.d(TAG, "Google API connection suspended because " + cause);

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(D) Log.d(TAG, "Google API connecting failed status " + connectionResult);

    }
}
