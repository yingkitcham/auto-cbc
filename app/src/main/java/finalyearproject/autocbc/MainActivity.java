package finalyearproject.autocbc;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main";
    private static final String mainDir = "Auto-CBC";
    public ListView lv;
    public MyDBHandler db;
    private sampleAdapter data;
    private SampleList dataModel;
    private ArrayList<SampleList> searchResults;
    private ArrayList<SampleList> originalValues;
    EditText inputSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv = (ListView) findViewById(R.id.sampleList);
        inputSearch = (EditText) findViewById(R.id.inputSearch);
        originalValues=new ArrayList<SampleList>();
        showRecord();
        //initialize the folder for blood bank storage
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + mainDir);
        boolean success = true;
        if (!folder.exists()) {
            success =folder.mkdirs();
        }
        if (success) {
            Log.d(TAG,"Folder created!");
            Toast.makeText(MainActivity.this, "First time initialization, Blood Sample Bank folder is created!" ,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Failed!" ,
                    Toast.LENGTH_LONG).show();
        }
        /**
         * Enabling Search Filter
         * */
        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                showSearchResults();
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // longclick context menu
        menu.setHeaderTitle("Select an Action");
        MenuInflater samplelist_inflater = getMenuInflater();
        samplelist_inflater.inflate(R.menu.contextmenu_listsample, menu);
        //menu.getItem(2).setEnabled(false);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final ArrayList<SampleList> mSamplelist = new ArrayList<>(db.getAllSamples());
        switch(item.getItemId())
        {
            case R.id.rename_id:
                final String oldSample= mSamplelist.get(info.position).get_sampleName();
                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                new AlertDialog.Builder(this)
                        .setTitle("Rename Blood Sample")
                        .setMessage("Please enter the new blood sample name for "+ oldSample +" :")
                        .setView(input)
                        .setCancelable(false)
                        .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String samplenameEt = input.getText().toString().trim();
                                String currentDate = getDate();
                                String readpath = Environment.getExternalStorageDirectory() +
                                        File.separator + mainDir;
                                String sameTemp = new String();
                                File f = new File(readpath);
                                if (f.exists()){
                                    File file[] = f.listFiles();
                                    for (int i = 0; i<file.length;i++){
                                        if (Objects.equals(file[i].getName(), samplenameEt)){
                                            sameTemp = samplenameEt;
                                        }
                                    }
                                }
                                if(TextUtils.isEmpty(samplenameEt)) {
                                    Toast.makeText(MainActivity.this, "The sample name cannot be empty!",
                                            Toast.LENGTH_SHORT).show();
                                } else if(Objects.equals(sameTemp,samplenameEt)){
                                    Toast.makeText(MainActivity.this, "The sample name cannot be same as others!",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    //**********************
                                    File oldFolder = new File(Environment.getExternalStorageDirectory() +
                                            File.separator + mainDir + File.separator + oldSample);
                                    File newFolder = new File(Environment.getExternalStorageDirectory() +
                                            File.separator + mainDir + File.separator + samplenameEt);
                                    boolean success = oldFolder.renameTo(newFolder);
                                    if (!newFolder.exists()) {
                                        success = newFolder.mkdirs();
                                    }
                                    if (success) {
                                        Toast.makeText(MainActivity.this, "Blood smeared image " + samplenameEt +
                                                " is renamed!", Toast.LENGTH_SHORT).show();
                                        db.updateSample(new SampleList(oldSample,samplenameEt,currentDate));
                                        showRecord();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Unable to create blood sample bank folder!"
                                                , Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                break;
            case R.id.delete_id:
                String point_to_folder = mSamplelist.get(info.position).get_sampleName();
                Toast.makeText(MainActivity.this, "The blood sample folder " +
                                point_to_folder ,
                        Toast.LENGTH_SHORT).show();
                // final confirmation
                final AlertDialog.Builder alert_deletion = new AlertDialog.Builder(MainActivity.this);
                alert_deletion.setTitle("Select an Action");
                alert_deletion.setMessage("Are you sure you want to delete "+point_to_folder+" ?");
                alert_deletion.setPositiveButton("agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // define folder delete name
                        String point_to_folder = mSamplelist.get(info.position).get_sampleName();
                        File deletefolder = new File(Environment.getExternalStorageDirectory()+
                                File.separator + mainDir + File.separator + point_to_folder);
                        // delete folder
                        deleteRecursive(deletefolder);
                        db.deleteSample(point_to_folder);
                        //show record
                        showRecord();
                    }
                });

                alert_deletion.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Delete Cancelled", Toast.LENGTH_SHORT).show();

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

    @Override
    public void onResume() {
        super.onResume();
        showRecord();
    }

    public void showRecord(){
        db = new MyDBHandler(this);
        final ArrayList<SampleList> mSamples = new ArrayList<>(db.getAllSamples());
        data =new sampleAdapter(this, mSamples);
        lv.setAdapter(data);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //dataModel = mSamples.get(position);
                //Toast.makeText(getApplicationContext(),String.valueOf(dataModel.get_id()), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,ViewListActivity.class);
                String fldrName = mSamples.get(position).get_sampleName();
                intent.putExtra("_folderName",fldrName);
                startActivity(intent);
            }
        });
        registerForContextMenu(lv);
    }


    public void showSearchResults(){
        db = new MyDBHandler(this);
        String search = inputSearch.getText().toString().trim();
        final ArrayList<SampleList> mSamples = new ArrayList<>(db.getSearchResult(search));
        data =new sampleAdapter(this, mSamples);
        lv.setAdapter(data);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //dataModel = mSamples.get(position);
                //Toast.makeText(getApplicationContext(),String.valueOf(dataModel.get_id()), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,ViewListActivity.class);
                String fldrName = mSamples.get(position).get_sampleName();
                intent.putExtra("_folderName",fldrName);
                startActivity(intent);
            }
        });
        registerForContextMenu(lv);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.samplemenulist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add_new_sample:
                createNewSample();
                return true;
            case R.id.resetData:
                resetSamples();
                return true;
            case R.id.about:
                aboutUs();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void resetSamples(){
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to reset ALL the data?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        File dir = new File(Environment.getExternalStorageDirectory()+ File.separator + mainDir);
                        deleteRecursive(dir);
                        db.resetSamples();
                        showRecord();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }
        fileOrDirectory.delete();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    public void createNewSample(){
        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(100, 100, 100, 100);
        input.setLayoutParams(lp);
        new AlertDialog.Builder(this)
                .setTitle("Add New Blood Sample")
                .setMessage("Please enter the blood sample name:")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String samplenameEt = input.getText().toString().trim();
                        String readpath = Environment.getExternalStorageDirectory() +
                                File.separator + mainDir;
                        String sameTemp = new String();
                        File f = new File(readpath);
                        if (f.exists()){
                            File file[] = f.listFiles();
                            for (int i = 0; i<file.length;i++){
                                if (Objects.equals(file[i].getName(), samplenameEt)){
                                    sameTemp = samplenameEt;
                                }
                            }
                        }
                        String currentDate = getDate();
                        if(TextUtils.isEmpty(samplenameEt)) {
                            Toast.makeText(MainActivity.this, "The sample name cannot be empty!",
                                    Toast.LENGTH_SHORT).show();
                        } else if(Objects.equals(sameTemp,samplenameEt)){
                            Toast.makeText(MainActivity.this, "The sample name cannot be same as others!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            //**********************
                            File foldersample = new File(Environment.getExternalStorageDirectory() +
                                    File.separator + mainDir + File.separator + samplenameEt);
                            boolean success = true;
                            if (!foldersample.exists()) {
                                success = foldersample.mkdirs();
                            }
                            if (success) {
                                Toast.makeText(MainActivity.this, "Blood sample " + samplenameEt +
                                        " is added!", Toast.LENGTH_SHORT).show();
                                db.addSample(new SampleList(samplenameEt,currentDate));
                                showRecord();
                            } else {
                                Toast.makeText(MainActivity.this, "Unable to create blood sample bank folder!"
                                        , Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void aboutUs(){
        Intent intent = new Intent(MainActivity.this,About.class);
        startActivity(intent);
    }

}
