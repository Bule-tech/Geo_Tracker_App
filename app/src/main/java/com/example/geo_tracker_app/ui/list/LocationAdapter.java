package com.example.geo_tracker_app.ui.list;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.geo_tracker_app.R;
import com.example.geo_tracker_app.model.SavedLocation;

import java.util.List;

/**
 * LocationAdapter Class is used to display the saved locations 
 * when the user goes to view locations.
 */
public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {

    /**
     * Listener interface for location deletion events.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button is clicked for a location.
         * @param location The SavedLocation to be deleted.
         */
        void onDeleteClick(SavedLocation location);
    }

    /**
     * Listener interface for navigation events.
     */
    public interface OnNavigateClickListener {
        /**
         * Called when the navigate button is clicked for a location.
         * @param location The SavedLocation to navigate to.
         */
        void onNavigateClick(SavedLocation location);
    }

    /**
     * Listener interface for geofence registration events.
     */
    public interface OnGeofenceClickListener {
        /**
         * Called when the register geofence button is clicked for a location.
         * @param location The SavedLocation for which to register the geofence.
         */
        void onGeofenceClick(SavedLocation location);
    }

    private List<SavedLocation> locationList;
    private final OnDeleteClickListener deleteClickListener;
    private final OnNavigateClickListener navigateClickListener;
    private final OnGeofenceClickListener geofenceClickListener;

    /**
     * Constructs a new LocationAdapter.
     * @param locationList List of locations to display.
     * @param deleteClickListener Listener for delete clicks.
     * @param navigateClickListener Listener for navigate clicks.
     * @param geofenceClickListener Listener for geofence clicks.
     */
    public LocationAdapter(List<SavedLocation> locationList,
                           OnDeleteClickListener deleteClickListener,
                           OnNavigateClickListener navigateClickListener,
                           OnGeofenceClickListener geofenceClickListener) {
        this.locationList = locationList;
        this.deleteClickListener = deleteClickListener;
        this.navigateClickListener = navigateClickListener;
        this.geofenceClickListener = geofenceClickListener;
    }

    /**
     * Updates the data set and refreshes the adapter.
     * @param locationList The new list of locations.
     */
    public void setLocationList(List<SavedLocation> locationList) {
        this.locationList = locationList;
        notifyDataSetChanged();
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * @param parent The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new LocationViewHolder.
     */
    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    /**
     * onBindViewHolder Method is used to display the saved locations
     * by extracting them in the app database.
     * @param holder The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        SavedLocation location = locationList.get(position);

        holder.tvLocationName.setText(location.getName());
        holder.tvCoordinates.setText("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
        holder.tvRadius.setText("Radius: " + location.getRadius() + " meters");

        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(location));
        holder.btnNavigate.setOnClickListener(v -> navigateClickListener.onNavigateClick(location));
        holder.btnRegisterGeofence.setOnClickListener(v -> geofenceClickListener.onGeofenceClick(location));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items.
     */
    @Override
    public int getItemCount() {
        return locationList == null ? 0 : locationList.size();
    }

    /**
     * ViewHolder class for location items.
     */
    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocationName, tvCoordinates, tvRadius;
        Button btnDelete, btnNavigate, btnRegisterGeofence;

        /**
         * LocationViewHolder Method is used to display the saved locations 
         * when the user goes to view locations.
         * @param itemView The view for the individual item.
         */
        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
            tvRadius = itemView.findViewById(R.id.tvRadius);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
            btnRegisterGeofence = itemView.findViewById(R.id.btnRegisterGeofence);
        }
    }
}
