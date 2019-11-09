package finalyearproject.autocbc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;


import java.util.ArrayList;

/**
 * Created by ASUS on 11/3/2017.
 */

public class sampleAdapter extends ArrayAdapter<SampleList> implements Filterable{
    Context context;
    ArrayList<SampleList> mSamples;
    private ArrayList<SampleList> mOriginalValues; // Original Values
    private ArrayList<SampleList> mDisplayedValues;    // Values to be displayed
    LayoutInflater inflater;

    public sampleAdapter(Context context, ArrayList<SampleList> samples){
        super(context, R.layout.listsample, samples);
        this.context = context;
        this.mSamples = samples;
    }

    public class Holder{
        TextView id;
        TextView samplesName;
        TextView date_created;
        TextView date_modified;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        SampleList data = getItem(position);
        String newDate;
        if (data.get_date_modified()== null){
            newDate = "-";
        } else {
            newDate =data.get_date_modified();
        }
        Holder viewHolder;

        if (convertView == null){
            viewHolder = new Holder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.listsample,parent,false);
            viewHolder.id = (TextView) convertView.findViewById(R.id.txtView0);
            viewHolder.samplesName = (TextView) convertView.findViewById(R.id.txtView1);
            viewHolder.date_created = (TextView) convertView.findViewById(R.id.txtView2);
            viewHolder.date_modified = (TextView) convertView.findViewById(R.id.txtView3);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Holder) convertView.getTag();
        }
        viewHolder.id.setText("ID: "+ data.get_id());
        viewHolder.samplesName.setText("Sample Name: "+data.get_sampleName());
        viewHolder.date_created.setText("Date Created: "+data.get_date_created());
        viewHolder.date_modified.setText("Date Modified: "+newDate);

        // Return the completed view to render on screen
        return convertView;
    }

}
