package net.madnation.zeus.contextual.xposed;


import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.yalantis.ucrop.UCrop;

import net.yazeed44.imagepicker.model.ImageEntry;
import net.yazeed44.imagepicker.util.Picker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {
    private static final int READ_PERMISSION = 0;
    private static final String PACKAGE_NAME = "net.madnation.zeus.contextual.xposed";
    private static final int IMAGE_PICKER = 1010;
    private static int IMAGE_REQ = -1;
    private ViewHolder VH;
    private SettingsManager sm;

    private final int MORNING_REQ = 2001;
    private final int AFTERNOON_REQ = 2002;
    private final int EVENING_REQ = 2003;
    private final int NIGHT_REQ = 2004;
    private static final int FROZEN_REQ = 2005;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, READ_PERMISSION);
        } else {
            Startup();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        PackageManager pkg = this.getPackageManager();
        if (pkg.getComponentEnabledSetting(new ComponentName(this, PACKAGE_NAME + ".show_ic")) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            MenuItem mi = menu.findItem(R.id.HideShow);
            mi.setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.RestartUI:
                (new RestartSystemUI()).execute();
                return true;
            case R.id.HideShow:
                PackageManager pkg = this.getPackageManager();
                if (!item.isChecked()) {
                    pkg.setComponentEnabledSetting(new ComponentName(this, PACKAGE_NAME + ".show_ic"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                } else {
                    pkg.setComponentEnabledSetting(new ComponentName(this, PACKAGE_NAME + ".show_ic"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                }
                item.setChecked(!item.isChecked());
                return true;
            case R.id.AboutUs:
                Intent intent = new Intent(this, AboutUs.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class RestartSystemUI extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            boolean suAvailable = Shell.SU.available();
            if (suAvailable) {
                List<String> suResult = Shell.SU.run(new String[]{"pkill -l 9 -f com.android.systemui", "killall com.android.systemui"});
            }
            return null;
        }
    }

    private void Startup() {
        VH = new ViewHolder(this);

        sm = new SettingsManager(true);
        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);

        boolean enableCustomImages = sm.getBooleanPref(SettingsManager.PREF_ENABLE_CUSTOM_IMAGES);
        VH.setTextView(enableCustomImages);
        cb.setChecked(enableCustomImages);

        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sm.setBooleanPref(SettingsManager.PREF_ENABLE_CUSTOM_IMAGES, isChecked);
                VH.setTextView(isChecked);
            }
        });
        cb.setEnabled(true);

        CheckBox freezeCb = (CheckBox) findViewById(R.id.freeze_checkBox);

        final boolean frozenEnabled = sm.getBooleanPref(SettingsManager.PREF_ENABLE_FROZEN);
        freezeCb.setChecked(frozenEnabled);

        freezeCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sm.setBooleanPref(SettingsManager.PREF_ENABLE_FROZEN, isChecked);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Startup();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Permission Request");
                    builder.setMessage("Without Read Permission, You wouldn't be able to choose you're own background.");

                    builder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            }
                    );

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                return;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(data);
            Bitmap bitmap;
            String filePath;
            try {
                InputStream inputStream = getContentResolver().openInputStream(resultUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                bitmap = Bitmap.createScaledBitmap(bitmap, 512, nh, true);
                filePath = resultUri.getPath();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if (bitmap != null) {
                switch (IMAGE_REQ) {
                    default:
                    case MORNING_REQ://"Morning":
                        VH.morningIV.setImageBitmap(bitmap);
                        break;
                    case AFTERNOON_REQ://"Afternoon":
                        VH.afternoonIV.setImageBitmap(bitmap);
                        break;
                    case EVENING_REQ://"Evening":
                        VH.eveningIV.setImageBitmap(bitmap);
                        break;
                    case NIGHT_REQ://"Night":
                        VH.nightIV.setImageBitmap(bitmap);
                        break;
                    case FROZEN_REQ:
                        VH.freezeIV.setImageBitmap(bitmap);
                        break;
                }
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            final Throwable cropError = UCrop.getError(data);
            if (cropError != null) cropError.printStackTrace();
        }

        if (resultCode == RESULT_OK && requestCode == IMAGE_PICKER) {
            ImageEditor(this, data.getData());
        }
    }

    private void ImageEditor(Context context, Uri srcURI) {
        String img_name;
        switch (IMAGE_REQ) {
            default:
            case MORNING_REQ://"Morning":
                img_name = "MORNING_BG";
                break;
            case AFTERNOON_REQ://"Afternoon":
                img_name = "AFTERNOON_BG";
                break;
            case EVENING_REQ://"Evening":
                img_name = "EVENING_BG";
                break;
            case NIGHT_REQ://"Night":
                img_name = "NIGHT_BG";
                break;
            case FROZEN_REQ:
                img_name = "FROZEN_BG";
                break;
        }

        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/ZCESH_BG/" + img_name + "/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        File nomedia = new File(dir.getAbsolutePath(), ".nomedia");
        if (!nomedia.exists()) {
            try {
                nomedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Uri destURI = Uri.fromFile(new File(dir.getAbsolutePath() + "/" + RNDHash() + "_" + img_name + ".jpg"));
        UCrop.of(srcURI, destURI).withAspectRatio(14, 8).start((Activity) context);
    }

    private String RNDHash() {
        return (String) UUID.randomUUID().toString().replaceAll("-", "").subSequence(0, 7);

    }

    class ViewHolder {
        private final View morningView, afternoonView, eveningView, nightView, freezeView;

        private final TextView morningTV, afternoonTV, eveningTV, nightTV, freezeTV;

        public final TopCropImageView morningIV, afternoonIV, eveningIV, nightIV, freezeIV;

        private boolean isEnable = false;

        private final View.OnClickListener CL = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (isEnable) {
                    String resName = v.getContext().getResources().getResourceEntryName(v.getId());
                    switch (resName) {
                        case "Morning":
                            IMAGE_REQ = MORNING_REQ;
                            break;
                        case "Afternoon":
                            IMAGE_REQ = AFTERNOON_REQ;
                            break;
                        case "Evening":
                            IMAGE_REQ = EVENING_REQ;
                            break;
                        case "Night":
                            IMAGE_REQ = NIGHT_REQ;
                            break;
                        case "Frozen":
                            IMAGE_REQ = FROZEN_REQ;
                            break;
                    }
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("Choose Option");
                        String options[] = new String[]{"Add Image", "Remove Image(s)", "Reset To Default"};
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) {
                                    //Show selector
                                    final Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    final PackageManager mgr = getPackageManager();
                                    List<ResolveInfo> list = mgr.queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);
                                    if (list.size() > 0) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                                        builder.setTitle("Pick a image picker");
                                        String options[] = new String[]{"Built-in Image Picker", "External Image Picker"};
                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which == 0) {
                                                    new Picker.Builder(v.getContext(), new MyPickListener(v.getContext()), R.style.MIP)
                                                            .setPickMode(Picker.PickMode.SINGLE_IMAGE)
                                                            .disableCaptureImageFromCamera()
                                                            .build()
                                                            .startActivity();
                                                } else {
                                                    startActivityForResult(i, IMAGE_PICKER);
                                                }
                                            }
                                        });
                                        builder.show();
                                    } else {
                                        new Picker.Builder(v.getContext(), new MyPickListener(v.getContext()), R.style.MIP)
                                                .setPickMode(Picker.PickMode.SINGLE_IMAGE)
                                                .disableCaptureImageFromCamera()
                                                .build()
                                                .startActivity();
                                    }
                                } else if (which == 1) {
                                    final String resName = v.getContext().getResources().getResourceEntryName(v.getId());
                                    final String folderName = resName.toUpperCase() + "_BG";
                                    Intent intent = new Intent(v.getContext(), FileBrowser.class);
                                    intent.putExtra("folderName", folderName);
                                    startActivity(intent);

                                } else if (which == 2) {

                                    final String resName = v.getContext().getResources().getResourceEntryName(v.getId());
                                    final String folderName = resName.toUpperCase() + "_BG";
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v.getContext());
                                    alertDialogBuilder.setMessage("Are you sure, you're going to reset the image?");

                                    alertDialogBuilder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/ZCESH_BG/" + folderName);
                                            if (dir.isDirectory()) {
                                                String[] children = dir.list();
                                                for (int i = 0; i < children.length; i++) {
                                                    File subfile = new File(dir, children[i]);
                                                    if (subfile.isFile()) {
                                                        subfile.delete();
                                                    }
                                                }
                                            }
                                            switch (resName) {
                                                case "Morning":
                                                    VH.morningIV.setBG(0);
                                                    break;
                                                case "Afternoon":
                                                    VH.afternoonIV.setBG(1);
                                                    break;
                                                case "Evening":
                                                    VH.eveningIV.setBG(2);
                                                    break;
                                                case "Night":
                                                    VH.nightIV.setBG(3);
                                                    break;
                                                case "Frozen":
                                                    VH.freezeIV.setBG(4);
                                                    break;
                                            }
                                        }
                                    });

                                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });

                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                }
                            }
                        });
                        builder.show();
                    }
                }
            }
        };

        ViewHolder(MainActivity itemView) {
            morningTV = (TextView) itemView.findViewById(R.id.Morning_textview);
            afternoonTV = (TextView) itemView.findViewById(R.id.Afternoon_textview);
            eveningTV = (TextView) itemView.findViewById(R.id.Evening_textview);
            nightTV = (TextView) itemView.findViewById(R.id.Night_textview);
            freezeTV = (TextView) itemView.findViewById(R.id.Frozen_textview);


            morningView = itemView.findViewById(R.id.Morning);
            afternoonView = itemView.findViewById(R.id.Afternoon);
            eveningView = itemView.findViewById(R.id.Evening);
            nightView = itemView.findViewById(R.id.Night);
            freezeView = itemView.findViewById(R.id.Frozen);

            morningIV = (TopCropImageView) itemView.findViewById(R.id.MorningIV);
            afternoonIV = (TopCropImageView) itemView.findViewById(R.id.AfternoonIV);
            eveningIV = (TopCropImageView) itemView.findViewById(R.id.EveningIV);
            nightIV = (TopCropImageView) itemView.findViewById(R.id.NightIV);
            freezeIV = (TopCropImageView) itemView.findViewById(R.id.FrozenIV);

            morningView.setOnClickListener(CL);
            afternoonView.setOnClickListener(CL);
            eveningView.setOnClickListener(CL);
            nightView.setOnClickListener(CL);
            freezeView.setOnClickListener(CL);

            morningIV.setBG(0);
            afternoonIV.setBG(1);
            eveningIV.setBG(2);
            nightIV.setBG(3);
            freezeIV.setBG(4);
        }

        void setTextView(boolean isEnable) {
            this.isEnable = isEnable;
            morningTV.setTextColor(isEnable ? Color.BLACK : Color.GRAY);
            afternoonTV.setTextColor(isEnable ? Color.BLACK : Color.GRAY);
            eveningTV.setTextColor(isEnable ? Color.BLACK : Color.GRAY);
            nightTV.setTextColor(isEnable ? Color.BLACK : Color.GRAY);

            freezeTV.setTextColor(isEnable ? Color.WHITE : Color.GRAY);
        }
    }

    class MyPickListener implements Picker.PickListener {

        private Context context;

        public MyPickListener(Context context) {
            this.context = context;
        }

        @Override
        public void onPickedSuccessfully(ArrayList<ImageEntry> images) {
            ImageEditor(context, Uri.fromFile(new File(images.get(0).path)));
        }

        @Override
        public void onCancel() {

        }
    }
}

