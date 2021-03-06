/*
Copyright 2016-2018 Jan Henzel, Patrick Jauernig, Dennis Werner

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.secuso.privacyfriendlywifimanager.logic.preconditions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

import org.secuso.privacyfriendlywifimanager.logic.types.PrimitiveCellInfo;
import org.secuso.privacyfriendlywifimanager.logic.types.PrimitiveCellInfoTreeSet;
import org.secuso.privacyfriendlywifimanager.logic.util.Logger;
import org.secuso.privacyfriendlywifimanager.logic.util.SerializationHelper;
import org.secuso.privacyfriendlywifimanager.logic.util.StaticContext;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * A condition that is true when the user is connected to a known cell.
 */
public class CellLocationCondition extends Precondition {
    private static final String TAG = CellLocationCondition.class.getCanonicalName();
    private static final long serialVersionUID = 1111115571759500839L;
    public final int MIN_CELLS = 2;
    public final double MIN_CELL_PERCENTAGE = 0.2;
    private String bssid;
    private Set<PrimitiveCellInfo> relatedCells; // cells

    /**
     * Creates a new {@link CellLocationCondition}.
     *
     * @param bssid The BSSID (which should be equivalent to a MAC address from this perspective
     */
    public CellLocationCondition(String bssid) {
        this(bssid, new HashSet<PrimitiveCellInfo>());
    }

    /**
     * Creates a new {@link CellLocationCondition}.
     *
     * @param bssid        The BSSID (which should be equivalent to a MAC address from this perspective
     * @param relatedCells All related cells.
     */
    public CellLocationCondition(String bssid, Set<PrimitiveCellInfo> relatedCells) {
        initialize(bssid, relatedCells);
    }

    /**
     * Initializes the object with the given parameters.
     *
     * @param bssid        The BSSID (which should be equivalent to a MAC address from this perspective
     * @param relatedCells All related cells.
     */
    public void initialize(String bssid, Set<PrimitiveCellInfo> relatedCells) {
        this.bssid = bssid;
        this.relatedCells = relatedCells;
    }

    /**
     * @see SerializationHelper
     */
    public void initialize(Context context, Object[] args) throws IOException {
        if (args[0] instanceof String && args[1] instanceof Set) {
            initialize((String) args[0], (Set<PrimitiveCellInfo>) args[1]);
        } else {
            throw new IOException(SerializationHelper.SERIALIZATION_ERROR);
        }
    }

    protected Object[] getPersistentFields() {
        return new Object[]{this.bssid, this.relatedCells};
    }

    /* GETTERS & SETTERS */

    /**
     * Returns the number of related cells.
     *
     * @return the number of related cells.
     */
    public int getNumberOfRelatedCells() {
        return this.relatedCells.size();
    }

    /**
     * Returns the related cells.
     *
     * @return all related cells.
     */
    public Set<PrimitiveCellInfo> getRelatedCells() {
        return this.relatedCells;
    }

    /**
     * Returns the BSSID.
     *
     * @return the BSSID.
     */
    public String getBssid() {
        return this.bssid;
    }

    /**
     * Adds the k best surrounding cells to the {@code relatedCells} using
     * {@code addKBestSurroundingCells(PrimitiveCellInfoTreeSet cells, int k)}.
     *
     * @param context A context to use.
     * @param k       Defines how many surrounding cells should be added.
     * @return True if a cell has been added, false otherwise.
     */
    public boolean addKBestSurroundingCells(Context context, int k) {
        return addKBestSurroundingCells(PrimitiveCellInfo.getAllCells(context), k);
    }

    /**
     * Adds the k best surrounding cells to the {@code relatedCells}
     *
     * @param cells The cells to add.
     * @param k     Defines how many surrounding cells should be added.
     * @return True if a cell has been added, false otherwise.
     */
    public boolean addKBestSurroundingCells(PrimitiveCellInfoTreeSet cells, int k) {
        Logger.d(TAG, "Adding " + k + " best of " + cells.size() + " surrounding cells.");
        boolean modified = false;
        int i = 0;
        for (PrimitiveCellInfo cell : cells) {
            if (i >= k) {
                break;
            }

            if (this.relatedCells.contains(cell)) {
                for (PrimitiveCellInfo cellToUpdate : this.relatedCells) {
                    if (cellToUpdate.equals(cell)) {
                        Logger.d(TAG, "Cell already known, updating range.");
                        modified |= cellToUpdate.updateRange(cell.getSignalStrength());
                        break;
                    }
                }
            } else {
                Logger.d(TAG, "Adding new cell.");
                modified |= this.relatedCells.add(cell);
            }

            i++;
        }

        if (modified) {
            this.notifyChanged();

            return true;
        }

        return false;
    }

    /**
     * Check whether ACCESS_COARSE_LOCATION permission has been granted.
     *
     * @param context A context to use.
     * @return True if ACCESS_COARSE_LOCATION permission has been granted, false otherwise.
     */
    public static boolean hasCoarseLocationPermission(Context context) {
        return (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public boolean check(Object... obj) {
        if (super.check(obj)) {
            if (ContextCompat.checkSelfPermission(StaticContext.getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                boolean respectSignalStrength = (boolean) obj[1];
                Set<PrimitiveCellInfo> currentCells = (Set<PrimitiveCellInfo>) obj[0];
                HashSet<PrimitiveCellInfo> union = new HashSet<>();
                HashSet<PrimitiveCellInfo> unionRange = new HashSet<>();

                for (PrimitiveCellInfo cell : currentCells) { // union contains current cell information
                    for (PrimitiveCellInfo savedCell : this.relatedCells) {
                        if (savedCell.equals(cell)) {
                            union.add(savedCell);
                            if (savedCell.inRange(cell.getSignalStrength())) {
                                unionRange.add(savedCell);
                            }

                            break; // break inner loop to add next cell
                        }
                    }
                }

                debugCells(currentCells, union, unionRange); // TODO remove me


                Logger.d(TAG, "size(unionRange) = " + unionRange.size() + ". Use it for calculation: " + respectSignalStrength);
                Logger.d(TAG, "size(UNION(cells ^ related)) = " + union.size() + "; relatedCells.size() = " + this.relatedCells.size());

                // return true if there are enough known cells in neighborhood or if there are at least MIN_CELLS known cells
                boolean retVal = (((double) union.size() / (double) this.relatedCells.size()) > MIN_CELL_PERCENTAGE) || (union.size() >= MIN_CELLS);

                return (respectSignalStrength && unionRange.size() > 0) || (!respectSignalStrength && retVal);
            } else {
                Logger.e(TAG, "ACCESS_COARSE_LOCATION not granted.");
                return false;
            }
        }

        return false; // condition not active
    }

    private void debugCells(Set<PrimitiveCellInfo> currentCells, Set<PrimitiveCellInfo> union, Set<PrimitiveCellInfo> unionRange) {

        // TODO DEBUGGING, REMOVE!
        StringBuffer buf = new StringBuffer();
        buf.append("currentCells = {");
        for (PrimitiveCellInfo cell : currentCells) {
            buf.append(cell.getCellId()).append(":").append(cell.getSignalStrength()).append("dBm, ");
        }
        buf.delete(buf.length() - 2, buf.length() - 1);
        buf.append("}");
        Logger.d(TAG, buf.toString());

        buf = new StringBuffer();
        buf.append("union = {");
        for (PrimitiveCellInfo cell : union) {
            buf.append(cell.getCellId()).append(":").append(cell.getSignalStrength()).append("dBm, ");
        }
        buf.delete(buf.length() - 2, buf.length() - 1);
        buf.append("}");
        Logger.d(TAG, buf.toString());

        buf = new StringBuffer();
        buf.append("unionRange = {");
        for (PrimitiveCellInfo cell : unionRange) {
            buf.append(cell.getCellId()).append(":").append(cell.getSignalStrength()).append("dBm, ");
        }
        buf.delete(buf.length() - 2, buf.length() - 1);
        buf.append("}");
        Logger.d(TAG, buf.toString());

        buf = new StringBuffer();
        buf.append("this.relatedCells = {");
        for (PrimitiveCellInfo cell : this.relatedCells) {
            buf.append(cell.getCellId()).append(":").append(cell.getSignalStrength()).append("dBm, ");
        }
        buf.delete(buf.length() - 2, buf.length() - 1);
        buf.append("}");
        Logger.d(TAG, buf.toString());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof CellLocationCondition && this.getBssid().equals(((CellLocationCondition) o).getBssid());
    }
}
