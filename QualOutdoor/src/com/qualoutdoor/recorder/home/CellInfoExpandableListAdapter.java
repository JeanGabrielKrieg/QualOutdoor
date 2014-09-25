package com.qualoutdoor.recorder.home;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.qualoutdoor.recorder.QualOutdoorRecorderApp;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.telephony.ICellInfo;
import com.qualoutdoor.recorder.telephony.ViewCellInfo;

/**
 * This adapter can feed a ListView with a cell info list, creating the
 * appropriate ViewCellInfo and sorting them into categories (GSM, LTE, etc.)
 * 
 * @author Gaborit Nicolas
 */

public class CellInfoExpandableListAdapter extends BaseExpandableListAdapter {

    /** The context that will contain the ViewCellInfo */
    private final Context context;

    /**
     * The array of all the cell info list, ordered by cell type. The index
     * corresponds to the cell type as specified in ICellInfo
     */
    @SuppressWarnings("unchecked")
    private final ArrayList<ICellInfo>[] cellArray = new ArrayList[MAX_CATEGORIES];

    /** The list containing only the non empty cell groups indexes */
    private ArrayList<Integer> groupIndexes;

    /** The maximum number of categories */
    private static final int MAX_CATEGORIES = 5;

    /** The titles of the categories */
    private static final String[] categoryTitles = QualOutdoorRecorderApp
            .getAppResources().getStringArray(R.array.radio_type_name);

    /**
     * Create an adapter with the given cell info list.
     * 
     * @param context
     *            The context used to create the views
     * @param cellInfos
     *            The data source
     */
    public CellInfoExpandableListAdapter(Context context,
            List<ICellInfo> cellInfos) {
        // Keep a reference to the context
        this.context = context;

        // Initialize the groups
        this.groupIndexes = new ArrayList<Integer>(MAX_CATEGORIES);
        
        // Initiliaze the lists
        for (int i = 0; i < MAX_CATEGORIES; i++) {
            // Initiliaze empty
            cellArray[i] = new ArrayList<ICellInfo>(0);
        }

        // Update the data set
        updateDataSet(cellInfos);
    }

    /**
     * Update the cell info list, replacing it with the given one.
     * 
     * @param cellInfos
     *            The new list of CellInfo
     */
    public void updateDataSet(List<ICellInfo> cellInfos) {
        // Clear the cell info lists
        for (List<ICellInfo> list : cellArray) {
            list.clear();
        }

        // Add all the cell infos into the corresponding categories
        for (ICellInfo cell : cellInfos) {
            cellArray[cell.getCellType()].add(cell);
        }

        // Clear the groups list
        groupIndexes.clear();

        // Update the groups to display
        for (int i = 0; i < MAX_CATEGORIES; i++) {
            // If non empty, add it
            if (!cellArray[i].isEmpty())
                groupIndexes.add(i);
        }

        // Notify data set changed
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        // Return the number of non empty list
        return groupIndexes.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        // Return the number of cell info in the given group
        return cellArray[groupIndexes.get(groupPosition)].size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // Return the group in position groupPosition
        return cellArray[groupIndexes.get(groupPosition)];
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // Return the desired child in the desired group
        return cellArray[groupIndexes.get(groupPosition)].get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        // Find the groupPosition-th group that is non empty
        int nonEmptyCount = 0;
        // Go through the categories, finding which are not empty
        int groupId = -1;
        for (int i = 0; i < MAX_CATEGORIES; i++) {
            // Count if not empty
            if (!cellArray[i].isEmpty())
                nonEmptyCount++;
            // Have we found the groupPosition-th group ?
            if (nonEmptyCount == groupPosition + 1) {
                groupId = i;
                break;
            }
        }
        return groupId;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        // The ID is the position in the sub group
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        // No we have dynamic lists
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        // Get the cell list index
        int groupIndex = groupIndexes.get(groupPosition);

        // The title of the category
        String title = cellArray[groupIndex].size() + " "
                + categoryTitles[groupIndex];

        // The resulting view initialized with the previous view
        View result = convertView;

        // If a view for the group already existed
        if (convertView == null) {
            // Get a layout inflater
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Inflate a new group view from the corresponding XML
            result = inflater.inflate(R.layout.view_cell_info_list_group, null);
        }

        // Get the header text view
        TextView headerView = (TextView) result.findViewById(R.id.list_header);
        // Update the header title
        headerView.setText(title);

        return result;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        // Get the corresponding cell info
        ICellInfo cell = cellArray[groupIndexes.get(groupPosition)]
                .get(childPosition);

        // The resulting view
        ViewCellInfo result;

        // If a view does not already exist or is not a ViewCellInfo, create a
        // new one
        if (convertView == null || (convertView instanceof ViewCellInfo)) {
            // Create a new ViewCellInfo from our context
            result = new ViewCellInfo(context);
        } else {
            // Recycle the old view
            result = (ViewCellInfo) convertView;
        }

        // Update the view with the latest cell info
        result.updateCellInfo(cell);

        return result;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        // No we cannot select the childs
        return false;
    }
}