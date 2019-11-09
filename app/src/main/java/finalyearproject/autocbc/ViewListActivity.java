package finalyearproject.autocbc;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class ViewListActivity extends AppCompatActivity {
    private ListView lvViews;
    private viewAdapter adapter;
    private List<ViewList> mViewList;
    private static final String mainDir = "Auto-CBC";
    private String fldr;
    private File sav_captures;
    public MyDBHandler db;
    private String fileName;
    private viewAdapter data;
    private ViewList dataModel;
    private int PICK_IMAGE_REQUEST = 3;
    String picturePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        // > 23API permission issue, must have code segment below.
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // call string from another activity
        Bundle bundle = getIntent().getExtras();
        fldr = bundle.getString("_folderName");

        // set title
        TextView bloodsampleview_name = (TextView)findViewById(R.id.folderName);
        bloodsampleview_name.setText("Sample Views of " + fldr);

        // directory
        //String readpath_view = Environment.getExternalStorageDirectory() +
        //       File.separator + mainDir + File.separator + fldr;

        //Toast.makeText(ViewListActivity.this, readpath_view, Toast.LENGTH_SHORT).show();

        lvViews = (ListView) findViewById(R.id.viewList);
        showView();
    }

    @Override
    public void onResume() {
        super.onResume();
        showView();
    }

    public void showView(){
        db = new MyDBHandler(this);
        final ArrayList<ViewList> mViews = new ArrayList<>(db.getAllViews(fldr));
        data =new viewAdapter(getApplicationContext(), mViews);
        lvViews.setAdapter(data);
        lvViews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dataModel = mViews.get(position);
                //Toast.makeText(getApplicationContext(),String.valueOf(dataModel.get_viewName()), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ViewListActivity.this,ImageViewActivity.class);
                String fileName = String.valueOf(dataModel.get_viewName());
                intent.putExtra("_fileName",fileName);
                startActivity(intent);
            }
        });
        registerForContextMenu(lvViews);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.viewmenulist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.capture:
                captureCamera();
                return true;
            case R.id.add_exist:
                addImage();
                return true;
            case R.id.resetData:
                resetViews();
                return true;
            case R.id.about:
                aboutUs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void resetViews(){
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to reset ALL the Views?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        File dir = new File(Environment.getExternalStorageDirectory()+ File.separator + mainDir
                        + File.separator + fldr);
                        deleteFilesOnly(dir);
                        db.resetViews(fldr);
                        showView();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void deleteFilesOnly(File files) {
        for(File file: files.listFiles())
            if (!file.isDirectory())
                file.delete();
        }

    public void addImage(){
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST);
    }


    public void captureCamera () {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String readpath_view = Environment.getExternalStorageDirectory() +
                File.separator + mainDir + File.separator + fldr;
        int number = db.countMaxId();
        fileName = "IMG_" + (number + 1);
        sav_captures = new File(readpath_view + File.separator + fileName + ".jpg");
        Uri tempuri = Uri.fromFile(sav_captures) ;
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempuri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1);
        startActivityForResult(intent,0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST &&
                resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            String[] projection = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();

            Log.d(TAG, DatabaseUtils.dumpCursorToString(cursor));

            int columnIndex = cursor.getColumnIndex(projection[0]);
            picturePath = cursor.getString(columnIndex); // returns null

            cursor.close();

            String readpath_view = Environment.getExternalStorageDirectory() +
                    File.separator + mainDir + File.separator + fldr;
            sav_captures = new File(readpath_view + File.separator);
            File file = new File(picturePath);
            try {
                moveFile(file,sav_captures);
                Toast.makeText(this,"Captured image is saved in the folder at "
                        + sav_captures.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                db.addView(new ViewList(fileName,fldr,getDate()));
                showView();
            } catch (IOException ex){
                Toast.makeText(this,"Failed to move the image from existing.",Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }

        } else if(requestCode == 0) {
            switch(resultCode){
                case Activity.RESULT_OK:
                    if(sav_captures.exists()) {
                        Toast.makeText(this,"Captured image is saved in the folder at "
                                + sav_captures.getAbsolutePath(),Toast.LENGTH_SHORT).show();
                        db.addView(new ViewList(fileName,fldr,getDate()));
                        showView();
                    } else {
                        Toast.makeText(this,"There is an error on saving the image.",Toast.LENGTH_SHORT).show();
                    } break;
                case Activity.RESULT_CANCELED:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // longclick context menu
        menu.setHeaderTitle("Select an Action");
        MenuInflater samplelist_inflater = getMenuInflater();
        samplelist_inflater.inflate(R.menu.contextmenu_listview, menu);
        //menu.getItem(2).setEnabled(false);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final ArrayList<ViewList> mViewsList = new ArrayList<>(db.getAllViews(fldr));
        switch(item.getItemId())
        {
            case R.id.delete_img:
                final String imgData = mViewsList.get(info.position).get_viewName();
                final String point_to_img = imgData+".jpg";
                final String point_to_img_r = imgData+"_R.jpg";
//                Toast.makeText(ViewListActivity.this, "The blood sample folder " +
//                                point_to_img ,
//                        Toast.LENGTH_SHORT).show();
                // final confirmation
                final AlertDialog.Builder alert_deletion = new AlertDialog.Builder(ViewListActivity.this);
                alert_deletion.setTitle("Select an Action");
                alert_deletion.setMessage("Are you sure you want to delete "+point_to_img+" ?");
                alert_deletion.setPositiveButton("agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // define folder delete name
                        File deletefolder = new File(Environment.getExternalStorageDirectory()+
                                File.separator + mainDir + File.separator + fldr +File.separator + point_to_img);
                        File deletefolder_r = new File(Environment.getExternalStorageDirectory()+
                                File.separator + mainDir + File.separator + fldr +File.separator + point_to_img_r);
                        // delete folder
                        deletefolder.delete();
                        if (deletefolder_r.exists())
                        deletefolder_r.delete();
                        db.deleteView(imgData);
                        //show record
                        showView();
                    }
                });

                alert_deletion.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ViewListActivity.this, "Delete Cancelled", Toast.LENGTH_SHORT).show();

                    }
                });
                // show alert for user prompt
                AlertDialog ad = alert_deletion.create();
                ad.show();
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return super.onContextItemSelected(item);

    }


    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private void moveFile(File file, File dir) throws IOException {
        int number = db.countMaxId();
        fileName = "IMG_" + (number + 1);
        File newFile = new File(dir, fileName+".jpg");
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
            file.delete();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }

    }

    public void aboutUs(){
        Intent intent = new Intent(ViewListActivity.this,About.class);
        startActivity(intent);
    }


}
