package finalyearproject.autocbc;

/**
 * Created Cham Ying Kit on 9/3/2017.
 */

public class SampleList {
    private int _id;
    private String _sampleName;
    private String _date_created;
    private String _date_modified;
    private String _sampleName_new;


    public SampleList(){

    }

    public SampleList(int id, String sampleName,  String date_created, String date_modified){
        this._id = id;
        this._sampleName = sampleName;
        this._date_created = date_created;
        this._date_modified = date_modified;
    }

    public SampleList(String sampleName, String date_created) {
        this._sampleName = sampleName;
        this._date_created = date_created;
    }

    public SampleList( String sampleName, String sampleName_new, String date_created) {
        this._sampleName = sampleName;
        this._sampleName_new = sampleName_new;
        this._date_created = date_created;
    }

    public String get_sampleName_new() {
        return _sampleName_new;
    }

    public void set_sampleName_new(String _sampleName_new) {
        this._sampleName_new = _sampleName_new;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public void set_sampleName(String _sampleName) {
        this._sampleName = _sampleName;
    }

    // setting Date
    public void set_date_created(String date){
        this._date_created = date;
    }

    // setting Time
    public void set_date_modified(String time){
        this._date_modified = time;
    }

    // getting Date
    public String get_date_created(){
        return this._date_created;
    }

    // getting Time
    public String get_date_modified(){
        return this._date_modified;
    }

    public String get_sampleName() {
        return _sampleName;
    }

    public int get_id() {
        return _id;
    }
}
