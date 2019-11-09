package finalyearproject.autocbc;

/**
 * Created by Cham Ying Kit on 9/3/2017.
 */

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;

import java.util.ArrayList;
import java.util.List;

public class MyDBHandler extends SQLiteOpenHelper {

    //Database Properties
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Auto-CBC.db";
    //Table Sample Properties
    private static final String TABLE_SAMPLE = "Sample";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_SAMPLE = "_sampleName";
    private static final String KEY_DATE_CREATED = "_date_created";
    private static final String KEY_DATE_MODIFIED = "_time_modified";
    //Table View Properties
    private static final String TABLE_VIEW = "View";
    private static final String VIEW_ID = "_view_id";
    private static final String VIEW_NAME = "_view_name";
    private static final String VIEW_SAMPLE= "_view_sample";
    private static final String RBC_COUNT = "_rbc_count";
    private static final String WBC_COUNT = "_wbc_count";
    private static final String PLT_COUNT = "_plt_count";
    private static final String TIME_ELAPSED = "_time_elapsed";
    String query;
    String query1;

    public MyDBHandler(Context context) {
        super(context, DATABASE_NAME, null , DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        query = "CREATE TABLE "+ TABLE_SAMPLE + " ("+
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SAMPLE + " TEXT, " +
                KEY_DATE_CREATED + " TEXT, " +
                KEY_DATE_MODIFIED + " TEXT" +
                ");";
        query1 = "CREATE TABLE "+ TABLE_VIEW + " ("+
                VIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VIEW_NAME + " TEXT, " +
                VIEW_SAMPLE + " TEXT, " +
                RBC_COUNT + " INTEGER, " +
                WBC_COUNT + " INTEGER, " +
                PLT_COUNT + " INTEGER, " +
                TIME_ELAPSED +" TEXT, " +
                KEY_DATE_CREATED + " TEXT, " +
                KEY_DATE_MODIFIED + " TEXT" +
                ");";
        db.execSQL(query);
        db.execSQL(query1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIEW);
        onCreate(db);
    }

    public void resetSamples(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAMPLE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VIEW);
        onCreate(db);
    }

    public void resetViews(String sample){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+ TABLE_VIEW +" WHERE "+ VIEW_SAMPLE + "=\"" + sample+ "\";");
    }

    //Add new row to the database
    public void addSample(SampleList sampleList){
        ContentValues values = new ContentValues();
        values.put(COLUMN_SAMPLE,sampleList.get_sampleName());
        values.put(KEY_DATE_CREATED,sampleList.get_date_created());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_SAMPLE,null,values);
        db.close();
    }

    public void updateSample(SampleList sampleList){
        ContentValues values = new ContentValues();
        values.put(COLUMN_SAMPLE, sampleList.get_sampleName_new());
        values.put(KEY_DATE_MODIFIED, sampleList.get_date_created());
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_SAMPLE,values, COLUMN_SAMPLE+ "=\""+sampleList.get_sampleName()+"\"" ,null);
        db.close();
        continueUpdate(sampleList);
    }

    public void continueUpdate(SampleList sampleList){
        ContentValues values = new ContentValues();
        values.put(VIEW_SAMPLE, sampleList.get_sampleName_new());
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_VIEW,values, VIEW_SAMPLE+ "=\""+sampleList.get_sampleName()+"\"" ,null);
        db.close();
    }

    //Delete a row from the database
    public void deleteSample(String sampleName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+ TABLE_SAMPLE +" WHERE "+ COLUMN_SAMPLE + "=\"" + sampleName+ "\";");
        db.execSQL("DELETE FROM "+ TABLE_VIEW +" WHERE "+ VIEW_SAMPLE + "=\"" + sampleName+ "\";");
    }

    public int countMaxId(){
        int maxID = 0;
        String selectQuery = "SELECT  * FROM " + TABLE_VIEW
                + " ORDER BY " + VIEW_ID + " DESC LIMIT 1;";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor mCount= db.rawQuery(selectQuery,null);
        if (mCount.moveToFirst()){
            do{
                maxID = mCount.getInt(0);
            } while (mCount.moveToNext());
        }
        return maxID;
    }


    // Getting All Samples
    public List<SampleList> getAllSamples() {
        List<SampleList> dataList = new ArrayList<SampleList>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SAMPLE
                + " ORDER BY " + COLUMN_ID + " DESC;";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SampleList data = new SampleList();
                data.set_id(Integer.parseInt(cursor.getString(0)));
                data.set_sampleName(cursor.getString(1));
                data.set_date_created(cursor.getString(2));
                data.set_date_modified(cursor.getString(3));
                // Adding contact to list
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        // return contact list
        return dataList;
    }

    public List<SampleList> getSearchResult(String search){
        List<SampleList> dataList = new ArrayList<SampleList>();
        // Select All Query
        String selectQuery = "SELECT * FROM "+ TABLE_SAMPLE + " WHERE "
                + COLUMN_SAMPLE + " LIKE \"%"+ search +"%\""
                + " ORDER BY " + COLUMN_ID + " DESC;";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SampleList data = new SampleList();
                data.set_id(Integer.parseInt(cursor.getString(0)));
                data.set_sampleName(cursor.getString(1));
                data.set_date_created(cursor.getString(2));
                data.set_date_modified(cursor.getString(3));
                // Adding contact to list
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        // return contact list
        return dataList;
    }

    public void addView(ViewList viewList){
        ContentValues values = new ContentValues();
        values.put(VIEW_NAME,viewList.get_viewName());
        values.put(VIEW_SAMPLE,viewList.get_sampleName());
        values.put(KEY_DATE_CREATED,viewList.get_date_created());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_VIEW,null,values);
        db.close();
        updateSampleDateModified(viewList);
    }

    public void updateResult(ViewList viewList){
        ContentValues values = new ContentValues();
        values.put(RBC_COUNT,viewList.get_rbcCount());
        values.put(WBC_COUNT,viewList.get_wbcCount());
        values.put(PLT_COUNT,viewList.get_pltCount());
        values.put(TIME_ELAPSED,viewList.get_time_elapsed());
        values.put(KEY_DATE_MODIFIED,viewList.get_date_analysed());
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_VIEW,values, VIEW_NAME+ "=\""+viewList.get_viewName()+"\"" ,null);
        db.close();
        updateSampleDateModified(viewList);
    }


    public void updateSampleDateModified(ViewList viewList){
        ContentValues values = new ContentValues();
        values.put(KEY_DATE_MODIFIED,viewList.get_date_created());
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_SAMPLE,values, COLUMN_SAMPLE+ "=\""+viewList.get_sampleName()+"\"" ,null);
        db.close();
    }

    public void deleteView(String viewName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+ TABLE_VIEW +" WHERE "+ VIEW_NAME + "=\"" + viewName+ "\";");
    }

    public ViewList getImgView(String fileName){
        ViewList data = new ViewList();
        String selectQuery = "SELECT * FROM " + TABLE_VIEW
                + " WHERE "+ VIEW_NAME +"=\"" + fileName+ "\";";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                data.set_viewId(Integer.parseInt(cursor.getString(0)));
                data.set_viewName(cursor.getString(1));
                data.set_sampleName(cursor.getString(2));
                if (cursor.getString(3)!=null) {
                    data.set_rbcCount(Integer.parseInt(cursor.getString(3)));
                    data.set_wbcCount(Integer.parseInt(cursor.getString(4)));
                    data.set_pltCount(Integer.parseInt(cursor.getString(5)));
                    data.set_time_elapsed(cursor.getString(6));
                } else {
                    data.set_rbcCount(0);
                    data.set_wbcCount(0);
                    data.set_time_elapsed("0.0");
                    //data.set_pltCount(0);
                }
                data.set_date_created(cursor.getString(7));
                data.set_date_analysed(cursor.getString(8));
            } while (cursor.moveToNext());
        }
        return data;
    }

    public List<ViewList> getAllViews(String fldrname) {
        List<ViewList> dataList = new ArrayList<ViewList>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_VIEW
                + " WHERE "+ VIEW_SAMPLE +"=\"" + fldrname+ "\""
                + " ORDER BY " + VIEW_ID + " DESC;";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ViewList data = new ViewList();
                data.set_viewId(Integer.parseInt(cursor.getString(0)));
                data.set_viewName(cursor.getString(1));
                data.set_sampleName(cursor.getString(2));
                if (cursor.getString(3)!=null) {
                    data.set_rbcCount(Integer.parseInt(cursor.getString(3)));
                    data.set_wbcCount(Integer.parseInt(cursor.getString(4)));
                    data.set_pltCount(Integer.parseInt(cursor.getString(5)));
                    data.set_time_elapsed(cursor.getString(6));
                } else {
                    data.set_rbcCount(0);
                    data.set_wbcCount(0);
                    data.set_time_elapsed("0.0");
                    //data.set_pltCount(0);
                }
                data.set_date_created(cursor.getString(7));
                data.set_date_analysed(cursor.getString(8));
                // Adding contact to list
                dataList.add(data);
            } while (cursor.moveToNext());
        }

        // return contact list
        return dataList;
    }

}
