/*
 * Copyright (C) 2012-2014  Cedric Cheneau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.holmes.core.business.streaming.session;

import net.holmes.core.common.exception.HolmesException;

/**
 * Unknown streaming session exception
 */
public class UnknownSessionException extends HolmesException {

    /**
     * Instantiates a new unknown streaming session exception.
     *
     * @param deviceId device Id
     */
    public UnknownSessionException(final String deviceId) {
        super("Unknown streaming session for device " + deviceId);
    }
}
