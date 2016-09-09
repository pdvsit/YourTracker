package com.app.adaptor;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.app.model.SiteModel;
import com.app.tracker.R;

import java.util.ArrayList;

/**
 * Created by VIJAY on 03-09-2016.
 */
public class SpinnerAdaptor extends ArrayAdapter<SiteModel> {

    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
    private ArrayList<SiteModel> values;

    public SpinnerAdaptor(Context context, int textViewResourceId,
                          ArrayList<SiteModel> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    public int getCount(){
        return values.size();
    }

    public SiteModel getItem(int position){
        return values.get(position);
    }

    public long getItemId(int position){
        return position;
    }


    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(values.get(position).getName());
        label.setSingleLine(false);
        label.setPadding(10, 10, 10, 10);
        //label.setBackgroundResource(R.drawable.round_edittext);

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setSingleLine(false);
        label.setTextSize(16);
        label.setPadding(10, 10, 10, 10);
        label.setText(values.get(position).getName());

        return label;
    }
}
