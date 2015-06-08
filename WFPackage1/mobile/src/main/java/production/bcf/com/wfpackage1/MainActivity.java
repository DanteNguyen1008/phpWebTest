package production.bcf.com.wfpackage1;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.wearable.companion.WatchFaceCompanion;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.Scanner;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ConfigActivity";
    private static final boolean D = true;

    /*static variables*/
    public static final String CONFIG_PATH = "/theball/pathconfig";
    private static final String CONFIG_TEXT_COLOR = "config.text.color";

    /*private*/
    private GoogleApiClient mGoogleApiClient;
    private String mPeerId;
    private Node mWearableNode = null;

    /*message handling for receiving data from wear*/
    private MessageApi.MessageListener mMessageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent m) {
            if (D) Log.d(TAG, "onMessageReceived: " + m.getPath());
            Scanner s = new Scanner(m.getPath());
            String command = s.next();
            if (command.equals("switch")) {

            }
        }
    };

    /*call back listener for wear connecting*/
    private ResultCallback<DataApi.DataItemResult> mDataResultCallBack =
            new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                        DataItem configDataItem = dataItemResult.getDataItem();
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                        DataMap config = dataMapItem.getDataMap();
                        setUpConfigView(config);
                    } else {
                        // If DataItem with the current config can't be retrieved, select the default items on
                        // each picker.
                        setUpConfigView(null);
                    }
                }
            };

    /*hanlding connection to wear*/
    private GoogleApiClient.ConnectionCallbacks mGoogConnectionCallbacks
            = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle connectionHint) {
            /*connected with the wear app*/
            if (D) Log.d(TAG, "onConnected: " + connectionHint);
            /*find and assign the wear node*/
            findWearableNode();
            Wearable.MessageApi.addListener(mGoogleApiClient, mMessageListener);

            if (mPeerId != null) {
                /*add call back listener*/
                Uri.Builder builder = new Uri.Builder();
                Uri uri = builder.scheme("wear").path(CONFIG_PATH).authority(mPeerId).build();
                Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(mDataResultCallBack);
            } else {
                displayNoConnectedDeviceDialog();
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            /*connection suspended*/
            if (D) Log.d(TAG, "onConnectionSuspended: " + cause);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPeerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(mGoogConnectionCallbacks)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "Google API Connection failed");
                    }
                })
                .addApi(Wearable.API)
                .build();

        sendConfigUpdateMessage(CONFIG_TEXT_COLOR, "#FF00FF");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /*looking for wear node*/
    private void findWearableNode() {
        /*set callback to get call when find out wear node*/
        PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                if (result.getNodes().size() > 0) {
                    mWearableNode = result.getNodes().get(0);
                    if (D)
                        Log.d(TAG, "Found wearable: name=" + mWearableNode.getDisplayName() + ", " +
                                "id=" + mWearableNode.getId());
                } else {
                    mWearableNode = null;
                }
            }
        });
    }

    /**
     * Set up views based on the received data
     *
     * @param config
     */
    private void setUpConfigView(DataMap config) {
        Log.d(TAG, "New data set up " + config);
    }

    private void displayNoConnectedDeviceDialog() {
        Log.d(TAG, "No device connected");
    }

    /**
     * Send config data to wear
     *
     * @param configKey
     * @param value
     */
    private void sendConfigUpdateMessage(final String configKey, final Object value) {
        if (mPeerId != null) {
            DataMap config = new DataMap();
            if (value instanceof Integer)
                config.putInt(configKey, (int) value);
            else
                config.putString(configKey, value.toString());

            byte[] rawData = config.toByteArray();
            PendingResult<MessageApi.SendMessageResult> pending = Wearable.MessageApi.sendMessage(mGoogleApiClient, mPeerId, CONFIG_PATH, rawData);
            pending.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    if (sendMessageResult.getStatus().isSuccess()) {
                        /*success situation handler*/
                        Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                                + value.toString() + " successful");
                    } else {
                        /*fail*/
                        Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                                + value.toString() + " Fail");
                    }
                }
            });
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Sent watch face config message: " + configKey + " -> "
                        + value.toString());
            }
        }
    }
}
