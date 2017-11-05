package net.madnation.zeus.contextual.xposed;

import android.content.res.XModuleResources;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;

public class SystemUI_hook implements IXposedHookZygoteInit, IXposedHookInitPackageResources {
    private static final String PACKAGE_NAME = "net.madnation.zeus.contextual.xposed";
    private static String MODULE_PATH = null;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(final XC_InitPackageResources.InitPackageResourcesParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui"))
            return;

        String element_id = "header";
        String layoutName = "status_bar_expanded_header";
        int padding = 0;

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            layoutName = "qs_panel";
            element_id = "quick_settings_container";
        }
        else
        {
            if(lpparam.res.getIdentifier("zz_moto_status_bar_expanded_header", "layout", "com.android.systemui") != 0)
            {
                layoutName = "zz_moto_status_bar_expanded_header";
            }
            else if(lpparam.res.getIdentifier("asus_status_bar_expanded_header", "layout", "com.android.systemui") != 0)
            {
                layoutName = "asus_status_bar_expanded_header";
                int dimens_id = lpparam.res.getIdentifier("asus_quicksetting_panel_header_padding_bottom", "dimen", "com.android.systemui");
                if(dimens_id != 0)
                {
                    padding = (int) lpparam.res.getDimension(dimens_id);
                }
            }
        }

        final int finalPadding = padding;
        final String finalElement_id = element_id;
        lpparam.res.hookLayout("com.android.systemui", "layout", layoutName, new XC_LayoutInflated()
        {
            @Override
            public void handleLayoutInflated(final LayoutInflatedParam liparam) throws Throwable {
                ViewGroup navbar = (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier(finalElement_id, "id", "com.android.systemui"));
                if (finalPadding != 0) {
                    navbar.setPadding(navbar.getPaddingLeft(), navbar.getPaddingTop(), navbar.getPaddingRight(), 0);
                }
                boolean isImageView = navbar.getChildAt(0).getClass().getName().equals(net.madnation.zeus.contextual.xposed.TopCropImageView.class.getName());

                if (!isImageView) {
                    int statusbar_height = (int) liparam.res.getDimension(liparam.res.getIdentifier("status_bar_header_height", "dimen", "com.android.systemui"));
                    XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, lpparam.res);
                    TopCropImageView IV = new TopCropImageView(navbar.getContext(), modRes, 0);
                    IV.setMinimumWidth(navbar.getWidth());
                    IV.setMinimumHeight(statusbar_height + finalPadding);

                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusbar_height + finalPadding);
                    navbar.addView(IV, 0, p);
                }
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                {
                    View ref = liparam.view.findViewById(liparam.res.getIdentifier("brightness_slider", "id", "com.android.systemui"));
                    View brightness_slider = (View) ref.getParent();
                    if(brightness_slider.getLayoutParams() instanceof LinearLayout.LayoutParams)
                    {
                        int twelve_dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, liparam.res.getDisplayMetrics());
                        ((LinearLayout.LayoutParams) brightness_slider.getLayoutParams()).height -= twelve_dp;
                    }

                }
            }
        });
    }
}
