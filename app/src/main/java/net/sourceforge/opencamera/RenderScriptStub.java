package net.sourceforge.opencamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Script;
import android.renderscript.Type;
import android.util.Log;

public class RenderScriptStub {
    private static final String TAG = "RenderScriptStub";
    
    public static class ScriptC_histogram_compute {
        public ScriptC_histogram_compute(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    public static class ScriptC_pyramid_blending {
        public ScriptC_pyramid_blending(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    public static class ScriptC_feature_detector {
        public ScriptC_feature_detector(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    public static class ScriptC_process_avg {
        public ScriptC_process_avg(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    public static class ScriptC_create_mtb {
        public ScriptC_create_mtb(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    public static class ScriptC_align_mtb {
        public ScriptC_align_mtb(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    public static class ScriptC_histogram_adjust {
        public ScriptC_histogram_adjust(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    public static class ScriptC_avg_brighten {
        public ScriptC_avg_brighten(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    public static class ScriptC_calculate_sharpness {
        public ScriptC_calculate_sharpness(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    public static class ScriptC_process_hdr {
        public ScriptC_process_hdr(RenderScript rs) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
    
    // Stub for RenderScript class
    public static class RenderScriptStubContext {
        private static RenderScript instance;
        
        public static RenderScript create(Context context) {
            if (instance == null) {
                instance = RenderScript.create(context);
            }
            return instance;
        }
    }
    
    // Stub for Allocation class
    public static class AllocationStub {
        public static Allocation createFromBitmap(RenderScript rs, Bitmap b) {
            Log.d(TAG, "HDR functionality is disabled");
            return null;
        }
        
        public static Allocation createTyped(RenderScript rs, Type type) {
            Log.d(TAG, "HDR functionality is disabled");
            return null;
        }
    }
    
    // Stub for Type class
    public static class TypeStub {
        public static Type createX(RenderScript rs, Element e, int width, int height) {
            Log.d(TAG, "HDR functionality is disabled");
            return null;
        }
    }
    
    // Stub for Script class
    public static class ScriptStub {
        public static final int SCRIPT_PRIORITY_DEFAULT = 0;
        
        public static void setTimeZone(RenderScript rs, String timeZone) {
            Log.d(TAG, "HDR functionality is disabled");
        }
    }
}
