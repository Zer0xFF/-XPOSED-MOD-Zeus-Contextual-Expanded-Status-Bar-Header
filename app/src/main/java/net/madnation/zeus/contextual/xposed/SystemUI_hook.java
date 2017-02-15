package net.madnation.zeus.contextual.xposed;

import android.content.res.XModuleResources;
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

        String layoutName = "status_bar_expanded_header";
        int padding = 0;
        if (lpparam.res.getIdentifier("zz_moto_status_bar_expanded_header", "layout", "com.android.systemui") != 0) {
          layoutName = "zz_moto_status_bar_expanded_header";
        } else if (lpparam.res.getIdentifier("asus_status_bar_expanded_header", "layout", "com.android.systemui") != 0) {
            layoutName = "asus_status_bar_expanded_header";
            int dimens_id = lpparam.res.getIdentifier("asus_quicksetting_panel_header_padding_bottom", "dimen", "com.android.systemui");
            if (dimens_id != 0) {
                padding = (int) lpparam.res.getDimension(dimens_id);
            }
        }


        final int finalPadding = padding;
        lpparam.res.hookLayout("com.android.systemui", "layout", layoutName, new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(final LayoutInflatedParam liparam) throws Throwable {
                ViewGroup navbar = (ViewGroup) liparam.view.findViewById(liparam.res.getIdentifier("header", "id", "com.android.systemui"));
                if (finalPadding != 0) {
                    navbar.setPadding(navbar.getPaddingLeft(), navbar.getPaddingTop(), navbar.getPaddingRight(), 0);
                }
                ViewGroup VG = (ViewGroup) liparam.view;
                boolean isImageView = VG.getChildAt(0).getClass().getName().equals(net.madnation.zeus.contextual.xposed.TopCropImageView.class.getName());

                if (!isImageView) {
                    XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, lpparam.res);
                    TopCropImageView IV = new TopCropImageView(VG.getContext(), modRes, 0);
                    IV.setMinimumWidth(VG.getWidth());
                    IV.setMinimumHeight(VG.getHeight() + finalPadding);

                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    navbar.addView(IV, 0, p);
                }
            }
        });
    }
}
