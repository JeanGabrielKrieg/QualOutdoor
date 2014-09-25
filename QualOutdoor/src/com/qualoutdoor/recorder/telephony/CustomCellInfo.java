package com.qualoutdoor.recorder.telephony;

import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.qualoutdoor.recorder.QualOutdoorRecorderApp;
import com.qualoutdoor.recorder.R;
import com.qualoutdoor.recorder.Utils;

import android.os.Build;
import android.os.Bundle;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrength;
import android.util.Log;

/**
 * Implementation of ICellInfo using a Bundle to store data.
 * 
 * The most convenient way to construct a CustomCellInfo is to use the static
 * method `buildFromCellInfo(CellInfo cell)` which take as argument a CellInfo
 * and try to grab as much information as possible from it, trying to cast it to
 * the different subclasses (CellInfoLte etc.).
 * 
 * @author Gaborit Nicolas
 */
public class CustomCellInfo implements ICellInfo {

    /**
     * This bundle hold all the cell infos. We are using bundles because they
     * let us provide partial informations. Plus they are easily passed between
     * activities.
     */
    protected Bundle infoBundle;

    /** The name of the cells */
    private static final String[] radioNames = QualOutdoorRecorderApp
            .getAppResources().getStringArray(R.array.radio_type_name);

    /* *************************************
     * The bundle keys
     */
    /** Stores the cell type code. Holds an int. */
    protected static final String CELL_TYPE = "cell_type";
    /** Stores the timestamp value. Holds a long integer. */
    protected static final String TIMESTAMP = "timestamp";
    /** Stores if the cell is registered. Holds a boolean. */
    protected static final String IS_REGISTERED = "is_registered";
    /**
     * Stores the signal strength. Holds a bundle containing the signal strength
     * values.
     */
    protected static final String SIGNAL_STRENGTH = "signal_strength";
    /** Stores the Cell ID. Holds an int. */
    protected static final String CID = "cid";
    /** Stores the Location Area Code (GSM, WCDMA). Holds an int. */
    protected static final String LAC = "lac";
    /** Stores the Mobile Country Code. Holds an int. */
    protected static final String MCC = "mcc";
    /** Stores the Mobile Network Code. Holds an int. */
    protected static final String MNC = "mnc";
    /** Stores the Primary Scrambling Code (WCDMA). Holds an int. */
    protected static final String PSC = "psc";
    /** Stores the Physical Cell ID (LTE). Holds an int. */
    protected static final String PCI = "pci";
    /** Stores the Tracking Area Code (LTE). Holds an int. */
    protected static final String TAC = "tac";
    /** Store the Timing Advance (LTE). Holds an int. */
    protected static final String TA = "ta";

    /** Create an empty CustomCellInfo */
    public CustomCellInfo() {
        // Initialize a new empty bundle
        infoBundle = new Bundle();
    }

    /**
     * Create a new CustomCellInfo from a bundle containing the necessary
     * informations.
     * 
     * @param infos
     *            The bundle that contains all the known informations about the
     *            cell.
     */
    public CustomCellInfo(Bundle infos) {
        // initialize empty
        this();
        // Copy the infos
        infoBundle.putAll(infos);
    }

    /**
     * Create a new CustomCellInfo from the Android CellInfo implementation.
     * This only initialize the generic fields from the CellInfo class.
     * 
     * @param cell
     *            The CellInfo to initialize from.
     */
    protected CustomCellInfo(CellInfo cell) {
        // initialize empty
        this();
        // Timestamp the data
        infoBundle.putLong(TIMESTAMP, cell.getTimeStamp());
        // Indicate if this cell is registered
        infoBundle.putBoolean(IS_REGISTERED, cell.isRegistered());
    }

    /**
     * Create a new CustomCellInfo from the Android CellInfoGsm implementation.
     * 
     * @param cell
     *            The CellInfoGsm to initialize from.
     */
    public CustomCellInfo(CellInfoGsm cell) {
        // Initialize the the generic fields from the CellInfo class
        this((CellInfo) cell);
        // We have a GSM type of cell
        infoBundle.putInt(CELL_TYPE, ICellInfo.CELL_GSM);
        // Initialize Signal Strength
        putSignalStrength(cell.getCellSignalStrength());
        // Initialize Cell identity
        {
            // Get the cell identity
            CellIdentityGsm cellId = cell.getCellIdentity();
            // Fill the available fields
            infoBundle.putInt(CID, cellId.getCid());
            infoBundle.putInt(LAC, cellId.getLac());
            infoBundle.putInt(MCC, cellId.getMcc());
            infoBundle.putInt(MNC, cellId.getMnc());
        }
    }

    /**
     * Create a new CustomCellInfo from the Android CellInfoLte implementation.
     * 
     * @param cell
     *            The CellInfoLte to initialize from.
     */
    public CustomCellInfo(CellInfoLte cell) {
        // Initialize the the generic fields from the CellInfo class
        this((CellInfo) cell);
        // We have a LTE type of cell
        infoBundle.putInt(CELL_TYPE, ICellInfo.CELL_LTE);
        // Initialize Signal Strength
        putSignalStrength(cell.getCellSignalStrength());
        // Initialize Cell identity
        {
            // Get the cell identity
            CellIdentityLte cellId = cell.getCellIdentity();
            // Fill the available fields
            infoBundle.putInt(CID, cellId.getCi());
            infoBundle.putInt(MCC, cellId.getMcc());
            infoBundle.putInt(MNC, cellId.getMnc());
            infoBundle.putInt(TAC, cellId.getTac());
            infoBundle.putInt(PCI, cellId.getPci());
            infoBundle.putInt(TA, cell.getCellSignalStrength()
                    .getTimingAdvance());
        }
    }

    /**
     * Create a new CustomCellInfo from the Android CellInfoCdma implementation.
     * 
     * @param cell
     *            The CellInfoCdma to initialize from.
     */
    public CustomCellInfo(CellInfoCdma cell) {
        // Initialize the the generic fields from the CellInfo class
        this((CellInfo) cell);
        // We have a CDMA type of cell
        infoBundle.putInt(CELL_TYPE, ICellInfo.CELL_CDMA);
        // Initialize Signal Strength
        putSignalStrength(cell.getCellSignalStrength());
        // Initialize Cell identity
        {
            // Get the cell identity
            // CellIdentityCdma cellId = cell.getCellIdentity();
            // Fill the available fields
            // TODO The CDMA cells are actually very different from the
            // others...
        }
    }

    /** Parse and put the signal strength into the bundle */
    protected void putSignalStrength(CellSignalStrength cellSS) {
        // Parse the CellSignalStrength by creating a CustomSignalStrength
        CustomSignalStrength ss = new CustomSignalStrength(cellSS);
        // Bundle it and store it
        infoBundle.putBundle(SIGNAL_STRENGTH, ss.getBundle());
    }

    /**
     * Create a new CustomCellInfo from an Android CellInfo, trying to detect
     * the actual type of the provided CellInfo (CellInfoLte, CellInfoGsm,
     * CellInfoWcdma or CellInfoCdma).
     * 
     * @param cell
     *            The CellInfo to inspect thoroughly
     * @return A CustomCellInfo containing all the information that could be
     *         grabbed from the input CellInfo.
     */
    public static CustomCellInfo buildFromCellInfo(CellInfo cell) {
        // If we can parse Wcdma cells, call the more recent builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return CustomCellInfoWcdma.buildFromCellInfo(cell);
        }
        // Initialize the result
        CustomCellInfo result = null;
        // Continue by inspecting which type of CellInfo we actually
        // have and cast accordingly.
        if (cell instanceof CellInfoCdma) {
            // Call the CDMA constructor
            result = new CustomCellInfo((CellInfoCdma) cell);
        } else if (cell instanceof CellInfoGsm) {
            // Call the GSM constructor
            result = new CustomCellInfo((CellInfoGsm) cell);
        } else if (cell instanceof CellInfoLte) {
            // Call the LTE constructor
            result = new CustomCellInfo((CellInfoLte) cell);
        } else {
            // We only have a generic CellInfo...
            result = new CustomCellInfo(cell);
        }
        return result;
    }

    /**
     * Return a bundle containing the cell information.
     * 
     * @return This cell info as a Bundle
     */
    public Bundle getBundle() {
        return this.infoBundle;
    }

    @Override
    public int getCellType() {
        // Return the cell type, or CELL_UNKNOWN if unknown
        return infoBundle.getInt(CELL_TYPE, ICellInfo.CELL_UNKNOWN);
    }

    @Override
    public long getTimeStamp() {
        return infoBundle.getLong(TIMESTAMP, Long.MAX_VALUE);
    }

    @Override
    public boolean isRegistered() {
        return infoBundle.getBoolean(IS_REGISTERED, false);
    }

    @Override
    public ISignalStrength getSignalStrength() {
        // Get the bundled signal strength and create a CustomSignalStrength
        // from it
        return new CustomSignalStrength(infoBundle.getBundle(SIGNAL_STRENGTH));
    }

    @Override
    public int getCid() {
        return infoBundle.getInt(CID, Integer.MAX_VALUE);
    }

    @Override
    public int getLac() {
        return infoBundle.getInt(LAC, Integer.MAX_VALUE);
    }

    @Override
    public int getMcc() {
        return infoBundle.getInt(MCC, Integer.MAX_VALUE);
    }

    @Override
    public int getMnc() {
        return infoBundle.getInt(MNC, Integer.MAX_VALUE);
    }

    @Override
    public int getPsc() {
        return infoBundle.getInt(PSC, Integer.MAX_VALUE);
    }

    @Override
    public int getPci() {
        return infoBundle.getInt(PCI, Integer.MAX_VALUE);
    }

    @Override
    public int getTac() {
        return infoBundle.getInt(TAC, Integer.MAX_VALUE);
    }

    @Override
    public int getTimingAdvance() {
        return infoBundle.getInt(TA, Integer.MAX_VALUE);
    }

    @Override
    public JSONObject toJSON() {
        // Convert the infoBundle into a JSON :
        return Utils.bundleToJSON(infoBundle);
    }

    /*
     * Warning, this method may seem dirty... but I ain't got no time for this
     * anymore
     */
    @Override
    public String toString() {
        // The resulting string
        String result = "";

        int type = getCellType();

        // Begins with the common infos
        result += "Cell type: " + radioNames[type] + "\n";
        if (isRegistered())
            result += "[Registered]\n";

        // CDMA and UNKNOWN don't have others infos
        if (type == CELL_CDMA || type == CELL_UNKNOWN)
            return result;

        result += "CID: " + stringify(getCid()) + "\n";
        result += "MCC: " + stringify(getMcc()) + "\n";
        result += "MNC: " + stringify(getMnc()) + "\n";

        if (type == CELL_GSM) {
            result += "LAC: " + stringify(getLac()) + "\n";
            return result;
        }

        if (type == CELL_WCDMA) {
            result += "LAC: " + stringify(getLac()) + "\n";
            result += "PSC: " + stringify(getPsc()) + "\n";
            return result;
        }
        
        if(type == CELL_LTE) {
            result += "TAC: " + stringify(getTac()) + "\n";
            result += "Timing Advance: " + stringify(getTimingAdvance()) + "\n";
            return result;
        }
        
        return result;
    }

    /**
     * Return a string representing the given value, or the cell_info_empty
     * string if unknown (Integer.MAX_VALUE)
     */
    private String stringify(int value) {
        if (value == Integer.MAX_VALUE) {
            return "?";
        } else {
            return value + "";
        }
    }
}
