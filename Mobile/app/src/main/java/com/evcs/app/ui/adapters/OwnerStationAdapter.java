package com.evcs.app.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.evcs.app.R;
import com.evcs.app.data.models.Station;
import java.util.ArrayList;
import java.util.List;

public class OwnerStationAdapter extends RecyclerView.Adapter<OwnerStationAdapter.StationViewHolder> {

    private List<Station> stationList = new ArrayList<>();

    @NonNull
    @Override
    public StationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_station, parent, false);
        return new StationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationViewHolder holder, int position) {
        Station station = stationList.get(position);
        holder.bind(station);
    }

    @Override
    public int getItemCount() {
        return stationList.size();
    }

    public void setStations(List<Station> stations) {
        this.stationList = stations;
        notifyDataSetChanged();
    }

    static class StationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewDetails;

        public StationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewStationName);
            textViewDetails = itemView.findViewById(R.id.textViewStationDetails);
        }

        public void bind(Station station) {
            textViewName.setText(station.getName());
            String details = "Type: " + station.getType() + " | Slots: " + station.getSlotCount();
            textViewDetails.setText(details);
        }
    }
}