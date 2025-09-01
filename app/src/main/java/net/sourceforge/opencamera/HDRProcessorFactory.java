package net.sourceforge.opencamera;

import android.content.Context;

public class HDRProcessorFactory {
    public static Object createHDRProcessor() {
        return new HDRProcessorStub();
    }
    
    public static Object createHDRProcessor(Context context) {
        return new HDRProcessorStub();
    }
}
