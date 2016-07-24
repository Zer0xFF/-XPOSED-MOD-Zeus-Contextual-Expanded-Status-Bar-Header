package net.madnation.zeus.contextual.xposed;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class FileBrowser extends AppCompatActivity implements ActionMode.Callback, GridView.OnItemClickListener {
    GridView gv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);

        gv = (GridView) findViewById(R.id.gridView);
        gv.startActionMode(this);

        LinearLayout root = (LinearLayout) findViewById(R.id.LinearLayout);
        root.post(new Runnable() {
            public void run() {
                loadDirectory();

            }
        });
    }

    public void loadDirectory() {
        String folderName = getIntent().getExtras().getString("folderName");
        File dir = new File(Environment.getExternalStorageDirectory().getPath() + "/ZCESH_BG/" + folderName);


        if (dir.isDirectory()) {
            ArrayList<ImageSelection> images = new ArrayList<>();

            for (File childfile : dir.listFiles()) {
                if (childfile.isFile()) {
                    if (childfile.getName().toLowerCase().contains(".jpg") || childfile.getName().toLowerCase().contains(".png")) {
                        images.add(new ImageSelection(childfile));
                    }
                }
            }
            if (!images.isEmpty() && images.size() > 0) {
                int dim;
                Point size = getSize();
                if (size.x > size.y) {
                    dim = size.y / 2;
                } else if (size.x < size.y) {
                    dim = size.x / 2;
                } else {
                    dim = 300;
                }

                final FileAdapter adapter = new FileAdapter(this, R.layout.file_item, dim, images);

                gv.setColumnWidth(dim);
                gv.setNumColumns(-1);
                gv.setOnItemClickListener(this);
                gv.setAdapter(adapter);
                gv.invalidate();

            } else {
                gv.setAdapter(null);
                Toast.makeText(this, "No Image(s) Found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private Point getSize() {
        Rect windowSize = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(windowSize);
        return new Point(windowSize.width(), windowSize.height());
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_delete:
                for (int i = 0; i < gv.getCount(); i++) {
                    ImageSelection image = (ImageSelection) gv.getItemAtPosition(i);
                    if (image.isSelected()) {
                        image.get().delete();
                    }
                }
                loadDirectory();
                return true;
            case R.id.menu_cancel:
                for (int i = 0; i < gv.getCount(); i++) {
                    ImageSelection image = (ImageSelection) gv.getItemAtPosition(i);
                    if (image.isSelected()) {
                        image.setSelection(false);
                    }
                }
                if (gv != null && gv.getAdapter() != null)
                    ((FileAdapter) gv.getAdapter()).notifyDataSetChanged();
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.image_remove, menu);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        finish();
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((ImageSelection) gv.getItemAtPosition(position)).toggleSelection();
        ((FileAdapter) gv.getAdapter()).notifyDataSetChanged();
    }
}
