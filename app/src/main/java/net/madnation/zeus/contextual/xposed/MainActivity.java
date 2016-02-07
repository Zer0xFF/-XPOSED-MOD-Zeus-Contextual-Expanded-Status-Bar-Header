package net.madnation.zeus.contextual.xposed;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class MainActivity extends AppCompatActivity {
    private static final int READ_PERMISSION = 0;
    private static final String PACKAGE_NAME = "net.madnation.zeus.contextual.xposed";
    ViewHolder VH;
    final int MORNING_REQ = 2001;
    final int AFTERNOON_REQ = 2002;
    final int EVENING_REQ = 2003;
    final int NIGHT_REQ = 2004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION);
        } else {
            Startup();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.RestartUI:
                (new RestartSystemUI()).execute();
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
                List<String> suResult = Shell.SU.run(new String[]{"pkill -l 9 -f com.android.systemui"});
            }
            return null;
        }
    }

    private void Startup() {
        VH = new ViewHolder(this);

        final SharedPreferences sp = getSharedPreferences(PACKAGE_NAME, MODE_WORLD_READABLE);

        if (sp.getString("MORNING_BG", null)!=null) {
            String BG = sp.getString("MORNING_BG", null);
            if (BG != null) {
                File file = new File(BG);
                if (file.exists()) {
                    Bitmap img = BitmapFactory.decodeFile(BG);
                    VH.morningIV.setImageBitmap(img);
                }

            }
        }
        if (sp.getString("EVENING_BG", null)!=null){String BG = sp.getString("EVENING_BG", null);
            if (BG != null) {
                File file = new File(BG);
                if (file.exists()) {
                    Bitmap img = BitmapFactory.decodeFile(BG);
                    VH.eveningIV.setImageBitmap(img);
                }

            }
        }
        if (sp.getString("AFTERNOON_BG", null)!=null){String BG = sp.getString("AFTERNOON_BG", null);
            if (BG != null) {
                File file = new File(BG);
                if (file.exists()) {
                    Bitmap img = BitmapFactory.decodeFile(BG);
                    VH.afternoonIV.setImageBitmap(img);
                }

            }
        }
        if (sp.getString("NIGHT_BG", null)!=null){String BG = sp.getString("NIGHT_BG", null);
            if (BG != null) {
                File file = new File(BG);
                if (file.exists()) {
                    Bitmap img = BitmapFactory.decodeFile(BG);
                    VH.nightIV.setImageBitmap(img);
                }

            }
        }

        CheckBox cb = (CheckBox) findViewById(R.id.checkBox);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                VH.setTextView(isChecked);
                sp.edit().putBoolean("isCustom", isChecked).apply();
            }
        });
        cb.setChecked(sp.getBoolean("isCustom", false));
        cb.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
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
        if (resultCode == RESULT_OK) {
            Bitmap bitmap;
            String filePath;
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(inputStream);
                int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                bitmap = Bitmap.createScaledBitmap(bitmap, 512, nh, true);

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(
                        selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
            if (bitmap != null) {
                SharedPreferences sp = getSharedPreferences(PACKAGE_NAME, MODE_WORLD_READABLE);
                switch (requestCode) {
                    default:
                    case MORNING_REQ://"Morning":
                        VH.morningIV.setImageBitmap(bitmap);
                        sp.edit().putString("MORNING_BG", filePath).apply();
                        break;
                    case AFTERNOON_REQ://"Afternoon":
                        VH.afternoonIV.setImageBitmap(bitmap);
                        sp.edit().putString("AFTERNOON_BG", filePath).apply();
                        break;
                    case EVENING_REQ://"Evening":
                        VH.eveningIV.setImageBitmap(bitmap);
                        sp.edit().putString("EVENING_BG", filePath).apply();
                        break;
                    case NIGHT_REQ://"Night":
                        VH.nightIV.setImageBitmap(bitmap);
                        sp.edit().putString("NIGHT_BG", filePath).apply();
                        break;
                }
            }
        }
    }

    class ViewHolder {
        private final View morningView;
        private final View afternoonView;
        private final View eveningView;
        private final View nightView;

        private final TextView morningTV;
        private final TextView afternoonTV;
        private final TextView eveningTV;
        private final TextView nightTV;

        public final ImageView morningIV;
        public final ImageView afternoonIV;
        public final ImageView eveningIV;
        public final ImageView nightIV;

        private boolean isEnable = false;

        private View.OnClickListener CL = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnable) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    String resName = v.getContext().getResources().getResourceEntryName(v.getId());
                    switch (resName) {
                        case "Morning":
                            ((MainActivity) v.getContext()).startActivityForResult(intent, MORNING_REQ);
                            break;
                        case "Afternoon":
                            ((MainActivity) v.getContext()).startActivityForResult(intent, AFTERNOON_REQ);
                            break;
                        case "Evening":
                            ((MainActivity) v.getContext()).startActivityForResult(intent, EVENING_REQ);
                            break;
                        case "Night":
                            ((MainActivity) v.getContext()).startActivityForResult(intent, NIGHT_REQ);
                            break;
                    }
                    //Show selector
                }
            }
        };

        ViewHolder(MainActivity itemView) {
            morningTV = (TextView) itemView.findViewById(R.id.Morning_textview);
            afternoonTV = (TextView) itemView.findViewById(R.id.Afternoon_textview);
            eveningTV = (TextView) itemView.findViewById(R.id.Evening_textview);
            nightTV = (TextView) itemView.findViewById(R.id.Night_textview);


            morningView = itemView.findViewById(R.id.Morning);
            afternoonView = itemView.findViewById(R.id.Afternoon);
            eveningView = itemView.findViewById(R.id.Evening);
            nightView = itemView.findViewById(R.id.Night);

            morningIV = (ImageView) itemView.findViewById(R.id.MorningIV);
            afternoonIV = (ImageView) itemView.findViewById(R.id.AfternoonIV);
            eveningIV = (ImageView) itemView.findViewById(R.id.EveningIV);
            nightIV = (ImageView) itemView.findViewById(R.id.NightIV);

            morningView.setOnClickListener(CL);
            afternoonView.setOnClickListener(CL);
            eveningView.setOnClickListener(CL);
            nightView.setOnClickListener(CL);
        }

        void setTextView(boolean isEnable) {
            this.isEnable = isEnable;
            getSharedPreferences(PACKAGE_NAME, MODE_WORLD_READABLE).edit().putBoolean("isCustom", isEnable).apply();
            if (isEnable) {
                morningTV.setTextColor(Color.BLACK);
                afternoonTV.setTextColor(Color.BLACK);
                eveningTV.setTextColor(Color.BLACK);
                nightTV.setTextColor(Color.BLACK);
            } else {
                morningTV.setTextColor(Color.GRAY);
                afternoonTV.setTextColor(Color.GRAY);
                eveningTV.setTextColor(Color.GRAY);
                nightTV.setTextColor(Color.GRAY);
            }
        }
    }
}

