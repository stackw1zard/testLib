package com.stackwizards.mcq_wizard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stackwizards.mcq_wizard.R;
import com.stackwizards.mcq_wizard.entity.WizardUser;

import java.util.ArrayList;
import java.util.Map;

public class CustomAdapter extends BaseAdapter {
    Context context;
    String countryList[];
    int flags[];
    LayoutInflater inflter;

    String categories[];
    Integer[] points;
    WizardUser wizardUser;

//    public CustomAdapter(Context applicationContext, String[] countryList) {
//        this.context = context;
//        this.countryList = countryList;
//        this.flags = flags;
//        inflter = (LayoutInflater.from(applicationContext));
//    }


    public CustomAdapter(Context context, WizardUser wizardUser) {
        this.context = context;
        this.wizardUser = wizardUser;
        ArrayList<String> categoryiesArray = new ArrayList<>();
        ArrayList<Integer> pointsArray = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wizardUser.getNotes().entrySet()) {
            categoryiesArray.add(entry.getKey());
            pointsArray.add(entry.getValue());
            // ...
        }
        categories = categoryiesArray.toArray(new String[categoryiesArray.size()]);
        points = pointsArray.toArray(new Integer[pointsArray.size()]);
        inflter = (LayoutInflater.from(context));


    }

    @Override
    public int getCount() {
//        return categories.length;
        return 3;

    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.listview_adapter_leaderboard, null);
        TextView country = (TextView) view.findViewById(R.id.status_text_view);
        ImageView icon = (ImageView) view.findViewById(R.id.status_icon);

//        country.setText("xx");
//        String category = categories[i].replace("_icon","");
//        country.setText(category );
//        TextView pts = (TextView) view.findViewById(R.id.status_point_view);
//        pts.setText(points[i].toString());
//        icon.setImageResource(R.drawable.microcontroller);


        return view;
    }

}