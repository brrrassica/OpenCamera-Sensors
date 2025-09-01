package net.sourceforge.opencamera;

import android.graphics.Bitmap;
import android.renderscript.RenderScript;
import android.util.Log;

/**
 * Stub implementation of HDRProcessor that disables HDR functionality.
 */
public class HDRProcessorStub {
    private static final String TAG = "HDRProcessorStub";
    
    public HDRProcessorStub() {
        Log.d(TAG, "HDR functionality is disabled");
    }
    
    public void onDestroy() {
        // No-op
    }
    
    public static class AvgData {
        // Stub implementation
    }
    
    public AvgData processAvg(Bitmap bitmap1, Bitmap bitmap2, float avg_factor, int iso, float zoom_factor) {
        Log.d(TAG, "HDR processAvg called but HDR is disabled");
        return new AvgData();
    }
    
    public void updateAvg(AvgData avg_data, int width, int height, Bitmap new_bitmap, 
                         float avg_factor, int iso, float zoom_factor) {
        Log.d(TAG, "HDR updateAvg called but HDR is disabled");
    }
}
