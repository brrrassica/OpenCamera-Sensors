package net.sourceforge.opencamera;

import net.sourceforge.opencamera.cameracontroller.CameraController;
import net.sourceforge.opencamera.cameracontroller.RawImage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

/** Handles the saving (and any required processing) of photos.
 */
public class ImageSaver extends Thread {
    private static final String TAG = "ImageSaver";

    private final Paint p = new Paint();

    private final MainActivity main_activity;

    /* We use a separate count n_images_to_save, rather than just relying on the queue size, so we can take() an image from queue,
     * but only decrement the count when we've finished saving the image.
     * In general, n_images_to_save represents the number of images still to process, including ones currently being processed.
     * Therefore we should always have n_images_to_save >= queue.size().
     * Also note, main_activity.imageQueueChanged() should be called on UI thread after n_images_to_save increases or
     * decreases.
     * Access to n_images_to_save should always be synchronized to this (i.e., the ImageSaver class).
     * n_real_images_to_save excludes "Dummy" requests, and should also be synchronized, and modified
     * at the same time as n_images_to_save.
     */
    private int n_images_to_save = 0;
    private int n_real_images_to_save = 0;
    private final int queue_capacity;
    private final BlockingQueue<Request> queue;
    private final static int queue_cost_jpeg_c = 1; // also covers WEBP
    private final static int queue_cost_dng_c = 6;
    //private final static int queue_cost_dng_c = 1;

    // Should be same as MainActivity.app_is_paused, but we keep our own copy to make threading easier (otherwise, all
    // accesses of MainActivity.app_is_paused would need to be synchronized).
    // Access to app_is_paused should always be synchronized to this (i.e., the ImageSaver class).
    private boolean app_is_paused = true;

    // for testing; must be volatile for test project reading the state
    // n.b., avoid using static, as static variables are shared between different instances of an application,
    // and won't be reset in subsequent tests in a suite!
    public static volatile boolean test_small_queue_size; // needs to be static, as it needs to be set before activity is created to take effect
    public volatile boolean test_slow_saving;
    public volatile boolean test_queue_blocked;

    static class Request {
        enum Type {
            JPEG, // also covers WEBP
            RAW,
            DUMMY
        }
        final Type type;
        enum ProcessType {
            NORMAL,
            HDR,
            AVERAGE,
            PANORAMA
        }
        final ProcessType process_type; // for type==JPEG
        final boolean force_suffix; // affects filename suffixes for saving jpeg_images: if true, filenames will always be appended with a suffix like _0, even if there's only 1 image in jpeg_images
        final int suffix_offset; // affects filename suffixes for saving jpeg_images, when force_suffix is true or there are multiple images in jpeg_images: the suffixes will be offset by this number
        enum SaveBase {
            SAVEBASE_NONE,
            SAVEBASE_FIRST,
            SAVEBASE_ALL,
            SAVEBASE_ALL_PLUS_DEBUG // for PANORAMA
        }
        final SaveBase save_base; // whether to save the base images, for process_type HDR, AVERAGE or PANORAMA
        /* jpeg_images: for jpeg (may be null otherwise).
         * If process_type==HDR, this should be 1 or 3 images, and the images are combined/converted to a HDR image (if there's only 1
         * image, this uses fake HDR or "DRO").
         * If process_type==NORMAL, then multiple images are saved sequentially.
         */
        final List<byte []> jpeg_images;
        final RawImage raw_image; // for raw
        final boolean image_capture_intent;
        final Uri image_capture_intent_uri;
        final boolean using_camera2;
        /* image_format allows converting the standard JPEG image into another file format.
#		 */
        enum ImageFormat {
            STD, // leave unchanged from the standard JPEG format
            WEBP,
            PNG
        }
        ImageFormat image_format;
        int image_quality;
        boolean do_auto_stabilise;
        final double level_angle; // in degrees
        final List<float []> gyro_rotation_matrix; // used for panorama (one 3x3 matrix per jpeg_images entry), otherwise can be null
        boolean panorama_dir_left_to_right; // used for panorama
        float camera_view_angle_x; // used for panorama
        float camera_view_angle_y; // used for panorama
        final boolean is_front_facing;
        boolean mirror;
        final Date current_date;
        final String preference_hdr_contrast_enhancement; // for HDR
        final int iso; // not applicable for RAW image
        final long exposure_time; // not applicable for RAW image
        final float zoom_factor; // not applicable for RAW image
        String preference_stamp;
        String preference_textstamp;
        final int font_size;
        final int color;
        final String pref_style;
        final String preference_stamp_dateformat;
        final String preference_stamp_timeformat;
        final String preference_stamp_gpsformat;
        final String preference_stamp_geo_address;
        final String preference_stamp_units_distance;
        final boolean panorama_crop; // used for panorama
        final boolean store_location;
        final Location location;
        final boolean store_geo_direction;
        final double geo_direction; // in radians
        final boolean store_ypr; // whether to store geo_angle, pitch_angle, level_angle in USER_COMMENT if exif (for JPEGs)
        final double pitch_angle; // the pitch that the phone is at, in degrees
        final String custom_tag_artist;
        final String custom_tag_copyright;
        final int sample_factor; // sampling factor for thumbnail, higher means lower quality

        Request(Type type,
                ProcessType process_type,
                boolean force_suffix,
                int suffix_offset,
                SaveBase save_base,
                List<byte []> jpeg_images,
                RawImage raw_image,
                boolean image_capture_intent, Uri image_capture_intent_uri,
                boolean using_camera2,
                ImageFormat image_format, int image_quality,
                boolean do_auto_stabilise, double level_angle, List<float []> gyro_rotation_matrix,
                boolean is_front_facing,
                boolean mirror,
                Date current_date,
                String preference_hdr_contrast_enhancement,
                int iso,
                long exposure_time,
                float zoom_factor,
                String preference_stamp, String preference_textstamp, int font_size, int color, String pref_style, String preference_stamp_dateformat, String preference_stamp_timeformat, String preference_stamp_gpsformat, String preference_stamp_geo_address, String preference_stamp_units_distance,
                boolean panorama_crop,
                boolean store_location, Location location, boolean store_geo_direction, double geo_direction,
                double pitch_angle, boolean store_ypr,
                String custom_tag_artist,
                String custom_tag_copyright,
                int sample_factor) {
            this.type = type;
            this.process_type = process_type;
            this.force_suffix = force_suffix;
            this.suffix_offset = suffix_offset;
            this.save_base = save_base;
            this.jpeg_images = jpeg_images;
            this.raw_image = raw_image;
            this.image_capture_intent = image_capture_intent;
            this.image_capture_intent_uri = image_capture_intent_uri;
            this.using_camera2 = using_camera2;
            this.image_format = image_format;
            this.image_quality = image_quality;
            this.do_auto_stabilise = do_auto_stabilise;
            this.level_angle = level_angle;
            this.gyro_rotation_matrix = gyro_rotation_matrix;
            this.is_front_facing = is_front_facing;
            this.mirror = mirror;
            this.current_date = current_date;
            this.preference_hdr_contrast_enhancement = preference_hdr_contrast_enhancement;
            this.iso = iso;
            this.exposure_time = exposure_time;
            this.zoom_factor = zoom_factor;
            this.preference_stamp = preference_stamp;
            this.preference_textstamp = preference_textstamp;
            this.font_size = font_size;
            this.color = color;
            this.pref_style = pref_style;
            this.preference_stamp_dateformat = preference_stamp_dateformat;
            this.preference_stamp_timeformat = preference_stamp_timeformat;
            this.preference_stamp_gpsformat = preference_stamp_gpsformat;
            this.preference_stamp_geo_address = preference_stamp_geo_address;
            this.preference_stamp_units_distance = preference_stamp_units_distance;
            this.panorama_crop = panorama_crop;
            this.store_location = store_location;
            this.location = location;
            this.store_geo_direction = store_geo_direction;
            this.geo_direction = geo_direction;
            this.pitch_angle = pitch_angle;
            this.store_ypr = store_ypr;
            this.custom_tag_artist = custom_tag_artist;
            this.custom_tag_copyright = custom_tag_copyright;
            this.sample_factor = sample_factor;
        }

        /** Returns a copy of this object. Note that it is not a deep copy - data such as JPEG and RAW
         *  data will not be copied.
         */
        Request copy() {
            return new Request(this.type,
                    this.process_type,
                    this.force_suffix,
                    this.suffix_offset,
                    this.save_base,
                    this.jpeg_images,
                    this.raw_image,
                    this.image_capture_intent, this.image_capture_intent_uri,
                    this.using_camera2,
                    this.image_format, this.image_quality,
                    this.do_auto_stabilise, this.level_angle, this.gyro_rotation_matrix,
                    this.is_front_facing,
                    this.mirror,
                    this.current_date,
                    this.preference_hdr_contrast_enhancement,
                    this.iso,
                    this.exposure_time,
                    this.zoom_factor,
                    this.preference_stamp, this.preference_textstamp, this.font_size, this.color, this.pref_style, this.preference_stamp_dateformat, this.preference_stamp_timeformat, this.preference_stamp_gpsformat, this.preference_stamp_geo_address, this.preference_stamp_units_distance,
                    this.panorama_crop, this.store_location, this.location, this.store_geo_direction, this.geo_direction,
                    this.pitch_angle, this.store_ypr,
                    this.custom_tag_artist,
                    this.custom_tag_copyright,
                    this.sample_factor);
        }
    }

    ImageSaver(MainActivity main_activity) {
        if( MyDebug.LOG )
            Log.d(TAG, "ImageSaver");
        this.main_activity = main_activity;

        ActivityManager activityManager = (ActivityManager) main_activity.getSystemService(Activity.ACTIVITY_SERVICE);
        this.queue_capacity = computeQueueSize(activityManager.getLargeMemoryClass());
        this.queue = new ArrayBlockingQueue<>(queue_capacity); // since we remove from the queue and then process in the saver thread, in practice the number of background photos - including the one being processed - is one more than the length of this queue

        p.setAntiAlias(true);
    }

    /** Returns the length of the image saver queue. In practice, the number of images that can be taken at once before the UI
     *  blocks is 1 more than this, as 1 image will be taken off the queue to process straight away.
     */
    public int getQueueSize() {
        return this.queue_capacity;
    }

    /** Compute a sensible size for the queue, based on the device's memory (large heap).
     */
    public static int computeQueueSize(int large_heap_memory) {
        if( MyDebug.LOG )
            Log.d(TAG, "large max memory = " + large_heap_memory + "MB");
        int max_queue_size;
        if( MyDebug.LOG )
            Log.d(TAG, "test_small_queue_size?: " + test_small_queue_size);
        if( test_small_queue_size ) {
            large_heap_memory = 0;
        }

        if( large_heap_memory >= 512 ) {
            // This should be at least 5*(queue_cost_jpeg_c+queue_cost_dng_c)-1 so we can take a burst of 5 photos
            // (e.g., in expo mode) with RAW+JPEG without blocking (we subtract 1, as the first image can be immediately
            // taken off the queue).
            // This should also be at least 19 so we can take a burst of 20 photos with JPEG without blocking (we subtract 1,
            // as the first image can be immediately taken off the queue).
            // This should be at most 70 for large heap 512MB (estimate based on reserving 160MB for post-processing and HDR
            // operations, then estimate a JPEG image at 5MB).
            max_queue_size = 34;
        }
        else if( large_heap_memory >= 256 ) {
            // This should be at most 19 for large heap 256MB.
            max_queue_size = 12;
        }
        else if( large_heap_memory >= 128 ) {
            // This should be at least 1*(queue_cost_jpeg_c+queue_cost_dng_c)-1 so we can take a photo with RAW+JPEG
            // without blocking (we subtract 1, as the first image can be immediately taken off the queue).
            // This should be at most 8 for large heap 128MB (allowing 80MB for post-processing).
            max_queue_size = 8;
        }
        else {
            // This should be at least 1*(queue_cost_jpeg_c+queue_cost_dng_c)-1 so we can take a photo with RAW+JPEG
            // without blocking (we subtract 1, as the first image can be immediately taken off the queue).
            max_queue_size = 6;
        }
        //max_queue_size = 1;
        //max_queue_size = 3;
        if( MyDebug.LOG )
            Log.d(TAG, "max_queue_size = " + max_queue_size);
        return max_queue_size;
    }

    /** Computes the cost for a particular request.
     *  Note that for RAW+DNG mode, computeRequestCost() is called twice for a given photo (one for each
     *  of the two requests: one RAW, one JPEG).
     * @param is_raw Whether RAW/DNG or JPEG.
     * @param n_images This is the number of JPEG or RAW images that are in the request.
     */
    public static int computeRequestCost(boolean is_raw, int n_images) {
        if( MyDebug.LOG ) {
            Log.d(TAG, "computeRequestCost");
            Log.d(TAG, "is_raw: " + is_raw);
            Log.d(TAG, "n_images: " + n_images);
        }
        int cost;
        if( is_raw )
            cost = n_images * queue_cost_dng_c;
        else {
            cost = n_images * queue_cost_jpeg_c;
            //cost = (n_images > 1 ? 2 : 1) * queue_cost_jpeg_c;
        }
        return cost;
    }

    /** Computes the cost (in terms of number of slots on the image queue) of a new photo.
     * @param n_raw The number of JPEGs that will be taken.
     * @param n_jpegs The number of JPEGs that will be taken.
     */
    int computePhotoCost(int n_raw, int n_jpegs) {
        if( MyDebug.LOG ) {
            Log.d(TAG, "computePhotoCost");
            Log.d(TAG, "n_raw: " + n_raw);
            Log.d(TAG, "n_jpegs: " + n_jpegs);
        }
        int cost = 0;
        if( n_raw > 0 )
            cost += computeRequestCost(true, n_raw);
        if( n_jpegs > 0 )
            cost += computeRequestCost(false, n_jpegs);
        if( MyDebug.LOG )
            Log.d(TAG, "cost: " + cost);
        return cost;
    }

    // HDR and Panorama features are disabled
    private static final String HDR_DISABLED_MSG = "HDR feature is disabled";
    private static final String PANORAMA_DISABLED_MSG = "Panorama feature is disabled";

    // Stub method for HDR processing
    public Object getHDRProcessor() {
        if( MyDebug.LOG )
            Log.d(TAG, HDR_DISABLED_MSG);
        return null;
    }

    // Stub method for Panorama processing
    public Object getPanoramaProcessor() {
        if( MyDebug.LOG )
            Log.d(TAG, PANORAMA_DISABLED_MSG);
        return null;
    }

    // Stub method for HDR processing
    public boolean processHDR(List<byte []> images, boolean has_iso_exposure, int base_bitmap, int n_tiles_c, boolean release_bitmaps, boolean release_imgs, boolean force_suffix, int suffix_offset, boolean save_expo, boolean image_capture_intent, Uri image_capture_intent_uri, boolean using_camera2, Request.ImageFormat image_format, int image_quality, boolean do_auto_stabilise, double level_angle, boolean is_front_facing, boolean mirror, Date current_date, String preference_hdr_contrast_enhancement, int iso, long exposure_time, float zoom_factor, String preference_stamp, String preference_textstamp, int font_size, int color, String pref_style, String preference_stamp_dateformat, String preference_stamp_timeformat, String preference_stamp_gpsformat, String preference_stamp_geo_address, String preference_units_distance, boolean panorama_crop, boolean store_location, Location location, boolean store_geo_direction, double geo_direction, double pitch_angle, boolean store_ypr, String custom_tag_artist, String custom_tag_copyright, int sample_factor) {
        if( MyDebug.LOG )
            Log.d(TAG, "processHDR: " + HDR_DISABLED_MSG);
        return false;
    }

    // Stub method for Panorama processing
    public boolean processPanorama(List<byte []> images, List<float []> gyro_rotation_matrix, float camera_view_angle_x, float camera_view_angle_y, boolean is_front_facing, boolean mirror, Date current_date, int iso, long exposure_time, float zoom_factor, String preference_stamp, String preference_textstamp, int font_size, int color, String pref_style, String preference_stamp_dateformat, String preference_stamp_timeformat, String preference_stamp_gpsformat, String preference_stamp_geo_address, String preference_units_distance, boolean panorama_crop, boolean store_location, Location location, boolean store_geo_direction, double geo_direction, double pitch_angle, boolean store_ypr, String custom_tag_artist, String custom_tag_copyright, int sample_factor) {
        if( MyDebug.LOG )
            Log.d(TAG, "processPanorama: " + PANORAMA_DISABLED_MSG);
        return false;
    }

    // Stub method for checking if HDR is supported
    public static boolean supportsHDR() {
        if( MyDebug.LOG )
            Log.d(TAG, "supportsHDR: " + HDR_DISABLED_MSG);
        return false;
    }

    // Stub method for checking if Panorama is supported
    public static boolean supportsPanorama() {
        if( MyDebug.LOG )
            Log.d(TAG, "supportsPanorama: " + PANORAMA_DISABLED_MSG);
        return false;
    }
}
