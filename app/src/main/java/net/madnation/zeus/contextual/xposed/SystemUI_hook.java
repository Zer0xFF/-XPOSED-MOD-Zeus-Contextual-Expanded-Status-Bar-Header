package net.madnation.zeus.contextual.xposed;

import android.content.res.XModuleResources;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Calendar;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
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
        final XSharedPreferences prefs = new XSharedPreferences("net.madnation.zeus.contextual.xposed", "net.madnation.zeus.contextual.xposed");
        prefs.reload();
        prefs.makeWorldReadable();
        XposedBridge.log("pref loc " + prefs.getFile().getAbsolutePath());

        final XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, lpparam.res);

        String layoutName;
        if (lpparam.res.getIdentifier("asus_status_bar_expanded_header", "layout", "com.android.systemui") != 0) {
            layoutName = "asus_status_bar_expanded_header";
        } else {
            layoutName = "status_bar_expanded_header";
        }


        lpparam.res.hookLayout("com.android.systemui", "layout", layoutName, new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(final LayoutInflatedParam liparam) throws Throwable {
                ViewGroup navbar = (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier("header", "id", "com.android.systemui"));
                ViewGroup VG = (ViewGroup) liparam.view;
                boolean isImageView = VG.getChildAt(0).getClass().getName() == net.madnation.zeus.contextual.xposed.TopCropImageView.class.getName();

                TopCropImageView IV;
                if (!isImageView) {
                    IV = new TopCropImageView(VG.getContext(), modRes, prefs);
                    IV.setMinimumWidth(VG.getWidth());
                    IV.setMinimumHeight(VG.getHeight());

                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -1);
                    navbar.addView(IV, 0, p);
                }
            }
        });
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
