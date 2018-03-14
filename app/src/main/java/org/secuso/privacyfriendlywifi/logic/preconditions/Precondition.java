/*
 This file is part of Privacy Friendly Wifi Manager.
 Privacy Friendly Wifi Manager is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.
 Privacy Friendly Wifi Manager is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Wifi Manager. If not, see <http://www.gnu.org/licenses/>.
 */

package org.secuso.privacyfriendlywifi.logic.preconditions;

import org.secuso.privacyfriendlywifi.logic.util.SerializationHelper;

import java.io.Serializable;

/**
 * Interface representing a precondition to check for.
 */
public abstract class Precondition extends SerializationHelper implements Serializable {
    private static final long serialVersionUID = 6055669715368490046L;
    protected boolean isActive; // by default a user created precondition is active

    /**
     * Creates a new Precondition. This is active by default.
     */
    public Precondition() {
        this(true);
    }

    /**
     * Creates a new Precondition.
     * @param isActive True if the precondition should be active, false otherwise.
     */
    public Precondition(boolean isActive) {
        this.isActive = isActive;
    }

    /**
     * Notifies listeners about a changed data set.
     */
    protected void notifyChanged() {
        this.setChanged();
        this.notifyObservers();
    }

    /**
     * Checks the active state of the precondition.
     *
     * @return Returns the active state of the precondition.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the active state of the precondition.
     * If a precondition is not active, {@code check(Object obj)} returns always false.
     *
     * @param isActive True if the precondition should be evaluated, false otherwise.
     */
    public void setActive(boolean isActive) {
        this.isActive = isActive;
        this.notifyChanged();
    }


    /**
     * Check whether the precondition applies.
     *
     * @return True, if the precondition applies.
     */
    boolean check(Object... obj) {
        return this.isActive;
    }
}
