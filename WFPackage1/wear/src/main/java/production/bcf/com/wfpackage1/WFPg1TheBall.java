package production.bcf.com.wfpackage1;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.bcf.watchface.bcfwearcore.BaseWatchFaceConfigService;
import com.bcf.watchface.bcfwearcore.BaseWatchFaceService;
import com.bcf.watchface.bcfwearcore.WatchFaceUtility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by annguyenquocduy on 6/6/15.
 */

public class WFPg1TheBall extends BaseWatchFaceConfigService{

    private static final String TAG = "WFPg1TheBall";
    public static final String CONFIG_PATH = "/theball/pathconfig";

    @Override
    public BaseWatchFaceService.Engine onCreateEngine() {
        return new Engine();
    }

    public class Engine extends BaseWatchFaceConfigService.Engine {

        private static final int BG_COLOR = Color.BLACK;
        private static final int TEXT_COLOR = Color.WHITE;

        private static final String CONFIG_TEXT_COLOR = "config.text.color";

        private Paint mPtCenterTime, mPtSmallTime;

        private int textColor = 0;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            IS_DEBUG_DRAW = true;

            if(textColor == 0)
                textColor = TEXT_COLOR;

            /*create and fill color for bg*/
            Resources resources = WFPg1TheBall.this.getResources();
            Drawable backgroundDrawable = resources.getDrawable(R.drawable.bg_ball_rounded);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            mPtCenterTime = WatchFaceUtility.createTextPaint(textColor, Typeface.DEFAULT);
            mPtCenterTime.setTextSize(47f);

            mPtSmallTime = WatchFaceUtility.createTextPaint(textColor, Typeface.DEFAULT);
            mPtSmallTime.setTextSize(30f);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);

            int width = bounds.width();
            int height = bounds.height();
            float centerX = width / 2f;
            float centerY = height / 2f;

            /*draw center time*/
            canvas.drawText(WatchFaceUtility.formatTwoDigitNumber(mTime.hour) + ":" +
                            WatchFaceUtility.formatTwoDigitNumber(mTime.minute),
                    centerX - 58, centerY + 15, mPtCenterTime);

            /*top left hour*/
            canvas.save();
            canvas.translate(78, 87);
            canvas.rotate(-30f);
            canvas.drawText(WatchFaceUtility.formatTwoDigitNumber(mTime.hour - 1),
                    0, 0, mPtSmallTime);
            canvas.restore();

            /*bottom left hour*/
            canvas.save();
            canvas.translate(70, 248);
            canvas.rotate(30f);
            canvas.drawText(WatchFaceUtility.formatTwoDigitNumber(mTime.hour + 1),
                    0, 0, mPtSmallTime);
            canvas.restore();

            /*top right minute*/
            canvas.save();
            canvas.translate(209, 71);
            canvas.rotate(30f);
            int topRightMinutes = mTime.minute + 1 > 60 ? 1 : mTime.minute + 1;
            canvas.drawText(WatchFaceUtility.formatTwoDigitNumber(topRightMinutes),
                    0, 0, mPtSmallTime);
            canvas.restore();

            /*top right minute*/
            canvas.save();
            canvas.translate(220, 266);
            canvas.rotate(-30f);
            int bottomRightMinutes = mTime.minute - 1 < 0 ? 59 : mTime.minute - 1;
            canvas.drawText(WatchFaceUtility.formatTwoDigitNumber(bottomRightMinutes),
                    0, 0, mPtSmallTime);
            canvas.restore();

            drawDebugLines(canvas, centerX, centerY, width, height);
        }

        @Override
        protected void setUpDebugPaint() {
            debugPaint = new Paint();
            debugPaint.setColor(Color.RED);
            debugPaint.setStrokeWidth(1.f);
            debugPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        protected MessageApi.MessageListener setUpMessageListener() {
            return new MessageApi.MessageListener() {
                @Override
                public void onMessageReceived(MessageEvent messageEvent) {
                    Log.d(TAG, "onMessageReceived " + messageEvent.toString());
                }
            };
        }

        @Override
        protected GoogleApiClient setUpGoogleAPI() {
            return new GoogleApiClient.Builder(WFPg1TheBall.this)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle connectionHint) {
                            Log.d(TAG, "onConnected: " + connectionHint);
                            findPhoneNode();
                            if (getMessageListener() != null)
                                Wearable.MessageApi.addListener(getGoogleApiClient(), getMessageListener());
                            if (getDataListener() != null)
                                Wearable.DataApi.addListener(getGoogleApiClient(), getDataListener());
                            updateConfigDataItemAndUiOnStartup();
                        }

                        @Override
                        public void onConnectionSuspended(int cause) {
                            Log.d(TAG, "onConnectionSuspended: " + cause);
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.d(TAG, "onConnectionFailed: " + result);
                        }
                    })
                    .addApi(Wearable.API)
                    .build();
        }

        @Override
        protected void setDefaultValuesForMissingConfigKeys(DataMap config) {
            addKeyIfMissing(config, CONFIG_TEXT_COLOR, TEXT_COLOR);
        }

        @Override
        protected String getPathConfig() {
            return CONFIG_PATH;
        }

        @Override
        protected void updateUiForConfigDataMap(DataMap config) {
            if(config == null)
                return;

            String sTextColor = config.getString(CONFIG_TEXT_COLOR);
            if(sTextColor == null || sTextColor.length() <= 0)
                return;

            textColor = Color.parseColor(sTextColor);

            mPtCenterTime.setColor(textColor);
            mPtSmallTime.setColor(textColor);

            postInvalidate();
        }
    }
}
