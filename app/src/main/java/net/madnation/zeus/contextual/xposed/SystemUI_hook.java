package net.madnation.zeus.contextual.xposed;

import android.content.res.XModuleResources;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Calendar;
import java.util.Random;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class SystemUI_hook implements IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private static String MODULE_PATH = null;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(final XC_InitPackageResources.InitPackageResourcesParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;


        final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, lpparam.res);
        lpparam.res.hookLayout("com.android.systemui", "layout", "status_bar_expanded_header", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(final LayoutInflatedParam liparam) throws Throwable {
                final Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        ViewGroup navbar = (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier("header", "id", "com.android.systemui"));
                        ViewGroup VG = (ViewGroup) liparam.view;
                        boolean isImageView = VG.getChildAt(0).getClass().getName() == net.madnation.zeus.contextual.xposed.TopCropImageView.class.getName();

                        TopCropImageView IV;
                        if (!isImageView) {
                            IV = new TopCropImageView(liparam.view.getContext());
                            IV.setMinimumWidth(VG.getWidth());
                            IV.setMinimumHeight(VG.getHeight());

                            IV.setImageDrawable(modRes.getDrawable(getBackgroundID(), VG.getContext().getTheme()));
                            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -1);
                            navbar.addView(IV, 0, p);
                        } else {
                            IV = (TopCropImageView) VG.getChildAt(0);
                            IV.setImageDrawable(modRes.getDrawable(getBackgroundID(), VG.getContext().getTheme()));
                        }
                        handler.postDelayed(this, nextUpdate());
                    }
                };
                handler.post(runnable);
            }
        });
    }

    public int getBackgroundID() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        int[] drawerIDarr = {R.drawable.morning_banna_leaf_threeheadedmonkey};
        if (timeOfDay >= 3 && timeOfDay < 12) {
            drawerIDarr = new int[]{
                    R.drawable.morning_banna_leaf_threeheadedmonkey,
                    R.drawable.morning_niall_stopford,
                    R.drawable.morning_dew_boris_mitendorfer_photography,
            };
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            drawerIDarr = new int[]{
                    R.drawable.afternoon_brooklyn_bridge_andrew_mace,
                    R.drawable.afternoon_delight_james_marvin_phelps,
                    R.drawable.afternoon_morocco_trey_ratcliff,
            };
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            drawerIDarr = new int[]{
                    R.drawable.evening_castelfalfi_bernd_thaller,
                    R.drawable.evening_chicago_james_clear,
                    R.drawable.evening_singapore_jurek_d,
            };
        } else if ((timeOfDay >= 21 && timeOfDay < 24) || (timeOfDay >= 0 && timeOfDay < 3)) {
            drawerIDarr = new int[]{
                    R.drawable.night_chicago_justin_brown,
                    R.drawable.night_canary_islands_i_k_o,
                    R.drawable.night_starry_night_shawn_harquail,
            };
        }
        return drawerIDarr[new Random().nextInt(drawerIDarr.length)];
    }

    public int nextUpdate() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        int mins = c.get(Calendar.MINUTE);
        int timeDelay = 0;
        if (timeOfDay >= 3 && timeOfDay < 12) {
            timeDelay = (12 - timeOfDay - 1) * 3600000 + (60 - mins) * 60000;
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            timeDelay = (16 - timeOfDay - 1) * 3600000 + (60 - mins) * 60000;
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            timeDelay = (21 - timeOfDay - 1) * 3600000 + (60 - mins) * 60000;
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            timeDelay = (24 - timeOfDay - 1) * 3600000 + (60 - mins) * 60000;
        } else if (timeOfDay >= 0 && timeOfDay < 3) {
            timeDelay = (3 - timeOfDay - 1) * 3600000 + (60 - mins) * 60000;
        }
        XposedBridge.log("SystemUI: Next Contextual BG Change " + timeDelay);
        return timeDelay + 60000;
    }
}
