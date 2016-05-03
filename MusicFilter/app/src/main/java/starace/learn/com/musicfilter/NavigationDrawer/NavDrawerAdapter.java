package starace.learn.com.musicfilter.NavigationDrawer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import starace.learn.com.musicfilter.R;

/**
 * Created by mstarace on 5/2/16.
 */
public class NavDrawerAdapter extends RecyclerView.Adapter{
    private static final String TAG_NAV_ADAPTER = "NavigationAdapter";
    private List<NaviagtionEntry> data;
    private LayoutInflater inflater;
    private ArrayList<Boolean> isCheckedArray = new ArrayList<>();

    public NavDrawerAdapter(Context context, List<NaviagtionEntry> data, ArrayList<Boolean> isCheckedArray) {
        this.data = data;
        this.isCheckedArray = isCheckedArray;
        Log.d(TAG_NAV_ADAPTER, "DATA IS : " + data.size());
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Creates a viewHolder based on viewType of current item
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView;

        Log.d(TAG_NAV_ADAPTER,"ON CREATE VIEW HOLDER VIEW TYPE: " + viewType);
        switch (viewType) {
            case 0:
                itemLayoutView = inflater.inflate(R.layout.navigation_divider, parent, false);
                DividerVH dividerViewHolder = new DividerVH(itemLayoutView);
                return dividerViewHolder;
            case 1:
                itemLayoutView = inflater.inflate(R.layout.navigation_item, parent, false);
                ItemVH itemViewHolder = new ItemVH(itemLayoutView);
                return itemViewHolder;
            case 2:
                itemLayoutView = inflater.inflate(R.layout.navigation_toggle, parent, false);
                ToggleVH toggleViewHolder = new ToggleVH(itemLayoutView);
                return toggleViewHolder;
        }

        return null;
    }

    /**
     * Creates the functionality of each view in the Nav Drawer based on item type
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final NaviagtionEntry item = data.get(position);
        Log.d(TAG_NAV_ADAPTER,"THIS IS THE ON BINDVIEWHOLDER position" + position );


        if (item instanceof NavigationItem) {
            ItemVH viewHolder = (ItemVH) holder;
            viewHolder.mTitle.setText(((NavigationItem) item).getTitle());
        }

        if (item instanceof NavigationToggle)  {
            final ToggleVH viewHolder = (ToggleVH) holder;
            viewHolder.mTitle.setText(((NavigationToggle) item).getTitle());

            viewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG_NAV_ADAPTER, position + " has been checked!!");
                    if (isChecked) {

                        viewHolder.mSwitch.setChecked(true);
                        isCheckedArray.set(position, true);
                        Log.d(TAG_NAV_ADAPTER, "ON CHECKED CHANGEArray value is " + isCheckedArray.get(position));
                    } else {

                        viewHolder.mSwitch.setChecked(false);
                        isCheckedArray.set(position, false);
                        Log.d(TAG_NAV_ADAPTER, "ON CHECKED CHANGEArray value is FALSE");

                    }
                }
            });

            if (!isCheckedArray.get(position)) {
                Log.d(TAG_NAV_ADAPTER, "Array value is FALSE " + position);
                viewHolder.mSwitch.setChecked(false);
            } else {
                Log.d(TAG_NAV_ADAPTER, "Array value is TRUE" + position);
                viewHolder.mSwitch.setChecked(true);
            }

        }
    }

    @Override
    public int getItemViewType(int position) {

        if (data.get(position) instanceof NavigationDivider) {
            return 0;
        }
        if (data.get(position) instanceof NavigationItem) {
            return 1;
        }
        if (data.get(position) instanceof NavigationToggle) {
            return 2;
        }
        return -1;

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * Gets the Array of Booleans based on toggle items position to the
     * Fragment
     * @return
     */
    public ArrayList<Boolean> getIsCheckedArray() {
        return isCheckedArray;
    }


    class DividerVH extends RecyclerView.ViewHolder {
        public DividerVH(View itemView) {
            super(itemView);
        }
    }

    class ItemVH extends RecyclerView.ViewHolder {
        final TextView mTitle;

        public ItemVH(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.nav_item_title);
        }
    }

    class ToggleVH extends RecyclerView.ViewHolder {
        final TextView mTitle;
        final Switch mSwitch;

        public ToggleVH(View itemView) {
            super(itemView);
            mTitle = (TextView) itemView.findViewById(R.id.nav_item_title);
            mSwitch = (Switch) itemView.findViewById(R.id.nav_switch);
        }
    }
}
