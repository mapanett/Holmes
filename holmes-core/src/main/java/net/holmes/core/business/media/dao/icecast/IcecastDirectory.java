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

package net.holmes.core.business.media.dao.icecast;

import java.util.Set;

/**
 * Icecast directory.
 */
public class IcecastDirectory {
    private final Set<IcecastEntry> entries;

    /**
     * Instantiates a new Icecast directory.
     *
     * @param entries Icecast entries
     */
    public IcecastDirectory(Set<IcecastEntry> entries) {
        this.entries = entries;
    }

    public Set<IcecastEntry> getEntries() {
        return entries;
    }
}
