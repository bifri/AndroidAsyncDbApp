package com.ai.planetsdb.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.ai.planetsdb.R;
import com.ai.planetsdb.provider.PlanetsContract;

public class PlanetsListCursorAdapter extends CursorAdapter {

    @SuppressWarnings("unused")
    private static final String TAG = PlanetsListCursorAdapter.class.getSimpleName();

    private LayoutInflater mInflater;

    public PlanetsListCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mInflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View newView;
        ViewHolder holder = new ViewHolder();

        newView = mInflater.inflate(R.layout.list_item_planets, parent, false);
        holder.planetName = (TextView) newView.findViewById(R.id.list_textView_planetName);
        holder.distance = (TextView) newView.findViewById(R.id.list_textView_distance);
        holder.discoverer = (TextView) newView.findViewById(R.id.list_textView_discoverer);
        holder.diameter = (TextView) newView.findViewById(R.id.list_textView_diameter);
        holder.atmosphere = (TextView) newView.findViewById(R.id.list_textView_atmosphere);
        newView.setTag(holder);

        return newView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();

        int column = cursor.getColumnIndexOrThrow(PlanetsContract.PLANET_NAME);
        if (!cursor.isNull(column)) {
            holder.planetName.setText(cursor.getString(column));
        } else {
            holder.planetName.setText("");
        }

        column = cursor.getColumnIndexOrThrow(PlanetsContract.DISTANCE_FROM_EARTH);
        if (!cursor.isNull(column)) {
            holder.distance.setText(String.format("%,.0f", cursor.getDouble(column)));
        } else {
            holder.distance.setText("");
        }

        column = cursor.getColumnIndexOrThrow(PlanetsContract.DISCOVERER);
        if (!cursor.isNull(column)) {
            holder.discoverer.setText(cursor.getString(column));
        } else {
            holder.discoverer.setText("");
        }

        column = cursor.getColumnIndexOrThrow(PlanetsContract.DIAMETER);
        if (!cursor.isNull(column)) {
            holder.diameter.setText(String.format("%,.0f", cursor.getDouble(column)));
        } else {
            holder.diameter.setText("");
        }

        column = cursor.getColumnIndexOrThrow(PlanetsContract.HAS_ATMOSPHERE);
        if (!cursor.isNull(column)) {
            boolean hasAtmosphere = cursor.getInt(column) > 0;
            holder.atmosphere.setText(hasAtmosphere ? "yes" : "no");
        } else {
            holder.atmosphere.setText("no");
        }
    }

    private static class ViewHolder {
        TextView planetName;
        TextView distance;
        TextView discoverer;
        TextView diameter;
        TextView atmosphere;
    }
}
