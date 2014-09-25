package com.qualoutdoor.recorder.charting;

import java.util.ArrayList;
import java.util.List;

import android.webkit.JavascriptInterface;

import com.qualoutdoor.recorder.QualOutdoorRecorderApp;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.telephony.ICellInfo;
import com.qualoutdoor.recorder.telephony.ISignalStrength;

/** This adapter allows to display a ICellInfo list inside a BarChart */
public class CellsChartAdapter implements BarChartAdapter {

    /** The titles of the radio type */
    private static final String[] radioNames = QualOutdoorRecorderApp
            .getAppResources().getStringArray(R.array.radio_type_name);

    /** The cells list */
    ArrayList<ICellInfo> cells = new ArrayList<ICellInfo>();

    /**
     * Update the cell info list, replacing it with the given one.
     * 
     * @param cellInfos
     *            The new list of CellInfo
     */
    public void updateDataSet(List<ICellInfo> cellInfos) {
        // Remove all the old ones
        cells.clear();
        // Add all the cells that does have signal strength info
        for(ICellInfo cell : cellInfos) {
            if(cell.getSignalStrength().getDbm() != ISignalStrength.UNKNOWN_DBM)
                // Add it
                cells.add(cell);
        }
    }

    @Override
    @JavascriptInterface
    public float getValue(int i) {
        // Return the dBm of the given cell
        int dBm = cells.get(i).getSignalStrength().getDbm();
        if (dBm == ISignalStrength.UNKNOWN_DBM) {
            // TODO...
        }
        return dBm;
    }

    @Override
    @JavascriptInterface
    public String getName(int i) {
        ICellInfo cell = cells.get(i);
        // The name to display
        String name = radioNames[cell.getCellType()];
        if (cell.isRegistered())
            // Bold
            return "<b>" + name + "</b>";
        else
            // Normal
            return name; 
    }

    @Override
    @JavascriptInterface
    public String getLabel(int i) {
        // Get the cell
        ICellInfo cell = cells.get(i);
        // The string to display
        String label = "";

        /* We will try to get any ID that could represent the cell */
        // Do we have a known cell ?
        if (cell.getCellType() != ICellInfo.CELL_UNKNOWN) {
            // Try to get an id
            int id = cell.getCid();
            // If don't have one
            if (id == Integer.MAX_VALUE) {
                switch (cell.getCellType()) {
                case ICellInfo.CELL_LTE:
                    id = cell.getPci();
                    // Have we got something ?
                    if (id != Integer.MAX_VALUE) {
                        // Then write a label
                        label = "PCI: " + id;
                    }
                    break;
                case ICellInfo.CELL_WCDMA:
                    id = cell.getPsc();
                    // Have we got something ?
                    if (id != Integer.MAX_VALUE) {
                        // Then write a label
                        label = "PSC: " + id;
                    }
                }
            } else {
                label = "CID: " + id;
            }
        }

        return label;
    }

    @Override
    @JavascriptInterface
    public int getGroup(int i) {
        // We use the radio type as group id
        return cells.get(i).getCellType();
    }

    @Override
    @JavascriptInterface
    public int size() {
        // Return the size of the cells list
        return cells.size();
    }

    @Override
    @JavascriptInterface
    public String getTooltip(int i) {
        // Get the string of the cell
        String cell = cells.get(i).toString();
        // Replace \n with <br/>
        return cell.replaceAll("(\r\n|\n)", "<br/>");
    }

}
