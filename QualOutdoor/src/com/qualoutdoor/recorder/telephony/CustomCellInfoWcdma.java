package com.qualoutdoor.recorder.telephony;

import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;

/**
 * Extension of CustomCellInfo that can parse WCDMA cells
 * 
 * @author Gaborit Nicolas
 */
public class CustomCellInfoWcdma extends CustomCellInfo {

    /**
     * Create a new CustomCellInfoWcdma from the Android CellInfo
     * implementation. This only initialize the generic fields from the CellInfo
     * class.
     * 
     * @param cell
     *            The CellInfo to initialize from.
     */
    private CustomCellInfoWcdma(CellInfo cell) {
        super(cell);
    }


    /**
     * Create a new CustomCellInfoWcdma from the Android CellInfoWcdma
     * implementation.
     * 
     * @param cell
     *            The CellInfoWcdma to initialize from.
     */
    public CustomCellInfoWcdma(CellInfoWcdma cell) {
        // Initialize the the generic fields from the CellInfo class
        this((CellInfo) cell);
        // We have a WCDMA type of cell
        infoBundle.putInt(CELL_TYPE, ICellInfo.CELL_WCDMA);
        // Initialize Signal Strength
        putSignalStrength(cell.getCellSignalStrength());
        // Initialize Cell identity
        {
            // Get the cell identity
            CellIdentityWcdma cellId = cell.getCellIdentity();
            // Fill the available fields
            infoBundle.putInt(CID, cellId.getCid());
            infoBundle.putInt(MCC, cellId.getMcc());
            infoBundle.putInt(MNC, cellId.getMnc());
            infoBundle.putInt(LAC, cellId.getLac());
            infoBundle.putInt(PSC, cellId.getPsc());
        }
    }

    /**
     * Create a new CustomCellInfo from an Android CellInfo, trying to
     * detect the actual type of the provided CellInfo (CellInfoLte,
     * CellInfoGsm, CellInfoWcdma or CellInfoCdma).
     * 
     * @param cell
     *            The CellInfo to inspect thoroughly
     * @return A CustomCellInfo containing all the information that could be
     *         grabbed from the input CellInfo.
     */
    public static CustomCellInfo buildFromCellInfo(CellInfo cell) {
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
        } else if (cell instanceof CellInfoWcdma) {
            // Call the WCDMA constructor
            result = new CustomCellInfoWcdma((CellInfoWcdma) cell);
        } else {
            // We only have a generic CellInfo...
            result = new CustomCellInfo(cell);
        }
        return result;
    }
}
