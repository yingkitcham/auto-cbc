package finalyearproject.autocbc;

/**
 * Created by ASUS on 12/3/2017.
 */

public class ViewList {

    private int _viewId;
    private String _viewName;
    private String _sampleName;
    private int _rbcCount;
    private int _wbcCount;
    private int _pltCount;
    private String _time_elapsed;
    private String _date_created;
    private String _date_analysed;

    public ViewList() {
    }

    public ViewList(String _viewName, String _sampleName, int _rbcCount, int _wbcCount, int _pltCount, String _time_elapsed, String _date_analysed) {
        this._viewName = _viewName;
        this._sampleName = _sampleName;
        this._rbcCount = _rbcCount;
        this._wbcCount = _wbcCount;
        this._pltCount = _pltCount;
        this._time_elapsed = _time_elapsed;
        this._date_created = _date_analysed;
        this._date_analysed = _date_analysed;
    }

    public ViewList(int _viewId, String _viewName,
                    int _rbcCount, int _wbcCount, int _pltCount,
                    String _time_elapsed,
                    String _date_created, String _date_analysed) {
        this._viewId = _viewId;
        this._viewName = _viewName;
        this._rbcCount = _rbcCount;
        this._wbcCount = _wbcCount;
        this._pltCount = _pltCount;
        this._time_elapsed = _time_elapsed;
        this._date_created = _date_created;
        this._date_analysed = _date_analysed;
    }

    public ViewList(int _viewId, String _viewName, String _sampleName,
                    int _rbcCount, int _wbcCount,  int _pltCount, String _date_created, String _date_analysed) {
        this._viewId = _viewId;
        this._viewName = _viewName;
        this._sampleName = _sampleName;
        this._rbcCount = _rbcCount;
        this._wbcCount = _wbcCount;
        this._pltCount = _pltCount;
        this._date_created = _date_created;
        this._date_analysed = _date_analysed;
    }

    public ViewList(String _viewName, String _sampleName, String _date_created) {
        this._viewName = _viewName;
        this._sampleName = _sampleName;
        this._date_created = _date_created;
    }

    public ViewList (String _sampleName, String _date_created){
        this._sampleName = _sampleName;
        this._date_created = _date_created;
    }

    public int get_viewId() {
        return _viewId;
    }

    public String get_viewName() {
        return _viewName;
    }

    public String get_sampleName() {
        return _sampleName;
    }

    public int get_rbcCount() {
        return _rbcCount;
    }

    public int get_wbcCount() {
        return _wbcCount;
    }

    public int get_pltCount() {
        return _pltCount;
    }

    public String get_date_created() {
        return _date_created;
    }

    public String get_date_analysed() {
        return _date_analysed;
    }

    public void set_viewId(int _viewId) {
        this._viewId = _viewId;
    }

    public void set_viewName(String _viewName) {
        this._viewName = _viewName;
    }

    public void set_sampleName(String _sampleName) {
        this._sampleName = _sampleName;
    }

    public void set_rbcCount(int _rbcCount) {
        this._rbcCount = _rbcCount;
    }

    public void set_wbcCount(int _wbcCount) {
        this._wbcCount = _wbcCount;
    }

    public void set_pltCount(int _pltCount) {
        this._pltCount = _pltCount;
    }

    public void set_date_created(String _date_created) {
        this._date_created = _date_created;
    }

    public void set_date_analysed(String _date_analysed) {
        this._date_analysed = _date_analysed;
    }

    public String get_time_elapsed() {
        return _time_elapsed;
    }

    public void set_time_elapsed(String _time_elapsed) {
        this._time_elapsed = _time_elapsed;
    }
}


