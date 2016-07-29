package me.kaede.feya.media;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.os.Build;
import android.test.InstrumentationTestCase;
import android.util.Log;

/**
 * test get device media codecs' info
 * Created by Kaede on 16/7/27.
 */
public class MediaCodecInfoTest extends InstrumentationTestCase {

    public static final String TAG = "MediaCodecInfoTest";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void testMediaCodecInfo() {
        // list media codec
        int numCodec = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodec; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            Log.i(TAG, "[kaede][testMediaCodecInfo]" +
                    "=================================== " + codecInfo.getName() + " ===================================");
            boolean isEncoder = codecInfo.isEncoder();
            Log.d(TAG, "isEncoder = " + isEncoder);

            // print encoder codec info
            if (isEncoder) {
                // list codec support media types
                String[] types = codecInfo.getSupportedTypes();
                for (String type : types) {
                    Log.d(TAG, "codecInfo.getSupportedTypes = " + type);

                    // get capabilities of the given media type of the current codec
                    MediaCodecInfo.CodecCapabilities capabilitiesForType = codecInfo.getCapabilitiesForType(type);
                    Log.d(TAG, "default format = " + capabilitiesForType.getDefaultFormat().toString());

                    // get video capabilities
                    Log.d(TAG, "video capabilities :");
                    MediaCodecInfo.VideoCapabilities videoCapabilities = capabilitiesForType.getVideoCapabilities();
                    if (videoCapabilities != null) {
                        Log.v(TAG, "BitrateRange = " + videoCapabilities.getBitrateRange());
                        Log.v(TAG, "SupportedHeights = " + videoCapabilities.getSupportedHeights());
                        Log.v(TAG, "SupportedWidths = " + videoCapabilities.getSupportedWidths());
                        Log.v(TAG, "HeightAlignment = " + videoCapabilities.getHeightAlignment());
                        Log.v(TAG, "Width Alignment = " + videoCapabilities.getWidthAlignment());

                        // check if the given media type support certain heights, such 360px
                        boolean isSupported = videoCapabilities.getSupportedHeights().contains(360);
                        Log.d(TAG, "check if the given media type support certain heights, such 360px, " +
                                "isSupported = " + isSupported);
                        if (isSupported) {
                            Log.v(TAG, "SupportedWidthsFor(360) = " + videoCapabilities.getSupportedWidthsFor(360));
                            Log.v(TAG, "SupportedFrameRatesFor(x,360) = "
                                    + videoCapabilities.getSupportedFrameRatesFor(
                                    videoCapabilities.getSupportedWidthsFor(360).getUpper(), 360));
                        }
                    }

                    // get profile & level capabilities
                    Log.d(TAG, "  videoCapabilities profile levels:");
                    for (int k = 0; k < capabilitiesForType.profileLevels.length; k++) {
                        Log.v(TAG, "profile = " + capabilitiesForType.profileLevels[k].profile);
                        Log.v(TAG, "level = " + capabilitiesForType.profileLevels[k].level);
                    }
                }
            }
            Log.i(TAG,
                    "===================================================================================================");
        }
    }
}