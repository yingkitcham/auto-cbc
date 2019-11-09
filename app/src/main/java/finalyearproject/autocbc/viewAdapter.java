package finalyearproject.autocbc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ASUS on 12/3/2017.
 */

public class viewAdapter extends ArrayAdapter<ViewList> {
    Context context;
    ArrayList<ViewList> mViews;

    public viewAdapter(Context context, ArrayList<ViewList> views){
        super(context, R.layout.listsample, views);
        this.context = context;
        this.mViews = views;
    }

    public class Holder{
        TextView viewIdName;
        TextView dateCreated;
        TextView dateModified;
        TextView countResult;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        ViewList data = getItem(position);
        viewAdapter.Holder viewHolder;
        String newDate;
        String countRBC, countWBC, countPLT;
        if (data.get_date_analysed()!=null){
            newDate = data.get_date_analysed();
        } else {
            newDate ="-";
        }
        if (data.get_rbcCount()!=0){
            countRBC = String.valueOf(data.get_rbcCount());
            countWBC = String.valueOf(data.get_wbcCount());
            countPLT = String.valueOf(data.get_pltCount());
        } else {
            countRBC = "-";
            countWBC = "-";
            countPLT = "-";
        }

        if (convertView == null){
            viewHolder = new viewAdapter.Holder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listview,parent,false);
            viewHolder.viewIdName = (TextView) convertView.findViewById(R.id.txtView00);
            viewHolder.dateCreated = (TextView) convertView.findViewById(R.id.txtView11);
            viewHolder.dateModified = (TextView) convertView.findViewById(R.id.txtView22);
            viewHolder.countResult = (TextView) convertView.findViewById(R.id.txtView33);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (viewAdapter.Holder) convertView.getTag();
        }

        viewHolder.viewIdName.setText("Image Name: "+ data.get_viewName() + "     ID: "+ data.get_viewId());
        viewHolder.dateCreated.setText("Date Created: "+data.get_date_created());
        viewHolder.dateModified.setText("Date Analysed: "+newDate);
        viewHolder.countResult.setText("Numbers of RBC/WBC/PLT: "+countRBC+" / "+countWBC+" / "+countPLT);

        // Return the completed view to render on screen
        return convertView;
    }

}
