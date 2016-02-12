package net.madnation.zeus.contextual.xposed;

import android.content.Context;
import android.content.res.XModuleResources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import de.robv.android.xposed.XSharedPreferences;

/**
 * ImageView to display top-crop scale of an image view.
 *
 * @author Chris Arriola
 * @Source https://gist.github.com/arriolac/3843346
 */
public class TopCropImageView extends ImageView {

    private XModuleResources modRes = null;
    private XSharedPreferences prefs = null;
    private static int CURRENT_BG = -1;
    private final int MORNING_BG = 0;
    private final int AFTERNOON_BG = 1;
    private final int EVENING_BG = 2;
    private final int NIGHT_BG = 3;

    private final int MORNING_START = 3;
    private final int AFTERNOON_START = 12;
    private final int EVENING_START = 18;
    private final int NIGHT_START = 21;


    public TopCropImageView(Context context, XModuleResources modRes, XSharedPreferences prefs) {
        super(context);
        setScaleType(ScaleType.MATRIX);
        this.modRes = modRes;
        this.prefs = prefs;
    }

    public TopCropImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setScaleType(ScaleType.MATRIX);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //Log.e("Zeus_SystemUI", "OnDraw, Called");
        super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.e("Zeus_SystemUI", "onLayout, Called");
        if (prefs != null && modRes != null) {
            final int currentTime = currentTime();
            boolean isToUpdate = (CURRENT_BG == -1) || CURRENT_BG != currentTime;

            Log.e("Zeus_SystemUI", "isToUpdate, Called:" + isToUpdate);
            if (isToUpdate) {
                prefs.reload();
                boolean isCustom = prefs.getBoolean("isCustom", false);
                Log.e("Zeus_SystemUI", "Prefs, Called:" + isCustom);
                if (isCustom) {
                    setCustomBackground(currentTime);
                } else {
                    setRandomBackground(currentTime);
                }
            }
        }
        recomputeImgMatrix();
    }


    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        recomputeImgMatrix();
        Log.e("Zeus_SystemUI", "setFrame, Called");
        return super.setFrame(l, t, r, b);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        recomputeImgMatrix();
        Log.e("Zeus_SystemUI", "onSizeChanged, Called");
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void recomputeImgMatrix() {
        final Matrix matrix = getImageMatrix();

        float scale;
        final int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        if (getDrawable() == null) {
            return;
        }
        final int drawableWidth = getDrawable().getIntrinsicWidth();
        final int drawableHeight = getDrawable().getIntrinsicHeight();

        if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            scale = (float) viewHeight / (float) drawableHeight;
        } else {
            scale = (float) viewWidth / (float) drawableWidth;
        }

        if (drawableWidth * scale < viewWidth){
            scale = (float) viewWidth / (float) drawableWidth;
        } else if (drawableHeight * scale < viewHeight) {
            scale = (float) viewHeight / (float) drawableHeight;
        }

        matrix.setScale(scale, scale);
        setImageMatrix(matrix);
    }

    private int currentTime() {
        Calendar c = new GregorianCalendar();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        if (timeOfDay >= MORNING_START && timeOfDay < AFTERNOON_START) {
            return MORNING_BG;
        } else if (timeOfDay >= AFTERNOON_START && timeOfDay < EVENING_START) {
            return AFTERNOON_BG;
        } else if (timeOfDay >= EVENING_START && timeOfDay < NIGHT_START) {
            return EVENING_BG;
        } else if ((timeOfDay >= NIGHT_START && timeOfDay < 24) || (timeOfDay >= 0 && timeOfDay < MORNING_START)) {
            return NIGHT_BG;
        }
        return -1;
    }

    private void setRandomBackground(int currentTime) {
        int[] drawerIDarr;
        switch (currentTime) {
            default:
            case MORNING_BG:
                drawerIDarr = new int[]{
                        R.drawable.morning_banna_leaf_threeheadedmonkey,
                        R.drawable.morning_niall_stopford,
                        R.drawable.morning_dew_boris_mitendorfer_photography,
                };
                break;
            case AFTERNOON_BG:
                drawerIDarr = new int[]{
                        R.drawable.afternoon_brooklyn_bridge_andrew_mace,
                        R.drawable.afternoon_delight_james_marvin_phelps,
                        R.drawable.afternoon_morocco_trey_ratcliff,
                };
                break;
            case EVENING_BG:
                drawerIDarr = new int[]{
                        R.drawable.evening_castelfalfi_bernd_thaller,
                        R.drawable.evening_chicago_james_clear,
                        R.drawable.evening_singapore_jurek_d,
                };
                break;
            case NIGHT_BG:
                drawerIDarr = new int[]{
                        R.drawable.night_chicago_justin_brown,
                        R.drawable.night_canary_islands_i_k_o,
                        R.drawable.night_starry_night_shawn_harquail,
                };
                break;
        }
        try {//Catches Invalid resource ID Error, when restarting SystemUI after Module Update.
            setImageDrawable(modRes.getDrawable(drawerIDarr[new Random().nextInt(drawerIDarr.length)], this.getContext().getTheme()));
            CURRENT_BG = currentTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCustomBackground(int currentTime) {
        String BG = null;
        switch (currentTime) {
            case MORNING_BG:
                BG = "MORNING_BG";
                break;
            case AFTERNOON_BG:
                BG = "AFTERNOON_BG";
                break;
            case EVENING_BG:
                BG = "EVENING_BG";
                break;
            case NIGHT_BG:
                BG = "NIGHT_BG";
                break;
        }
        BG = prefs.getString(BG, null);
        if (BG != null) {
            File file = new File(BG);
            if (file.exists()) {
                Bitmap img = BitmapFactory.decodeFile(BG);
                CURRENT_BG = currentTime;
                setImageBitmap(img);
                return;
            }
        }
        setRandomBackground(currentTime);
    }
}