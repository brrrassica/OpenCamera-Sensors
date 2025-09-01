package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Log;

import java.util.List;

/**
 * Stub implementation of PanoramaProcessor that does nothing.
 * Used when HDR and Panorama features are disabled.
 */
public class PanoramaProcessorStub {
    private static final String TAG = "PanoramaProcessorStub";
    
    public interface PanoramaCallback {
        void onPanoramaCompleted(String filename);
        void onPanoramaError();
    }
    
    public PanoramaProcessorStub(Context context, Object hdrProcessor) {
        Log.d(TAG, "Panorama feature is disabled");
    }
    
    public void startNewPanorama(boolean is_front_facing, boolean mirror) {
        Log.d(TAG, "startNewPanorama: Panorama feature is disabled");
    }
    
    public void addImage(Bitmap bitmap, List<float []> gyro_rotation_matrix, float camera_view_angle_x, float camera_view_angle_y, boolean is_front_facing, boolean mirror) {
        Log.d(TAG, "addImage: Panorama feature is disabled");
    }
    
    public void processPanorama() {
        Log.d(TAG, "processPanorama: Panorama feature is disabled");
    }
    
    public void cancelPanorama() {
        Log.d(TAG, "cancelPanorama: Panorama feature is disabled");
    }
    
    public void onDestroy() {
        Log.d(TAG, "onDestroy: Panorama feature is disabled");
    }
    
    public void setCallback(PanoramaCallback callback) {
        Log.d(TAG, "setCallback: Panorama feature is disabled");
    }
    
    public boolean panoramaExists() {
        Log.d(TAG, "panoramaExists: Panorama feature is disabled");
        return false;
    }
    
    public PointF getPanoramaCenter() {
        Log.d(TAG, "getPanoramaCenter: Panorama feature is disabled");
        return new PointF(0, 0);
    }
}
