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

package net.holmes.core.business.media.model;

import com.google.common.base.Objects;

/**
 * Icecast genre node.
 */
public class IcecastGenreNode extends AbstractNode {

    private final String genre;

    /**
     * Instantiates a new Icecast genre node.
     *
     * @param id       node id
     * @param parentId parent node id
     * @param name     node name
     * @param genre    genre
     */
    public IcecastGenreNode(final String id, final String parentId, final String name, final String genre) {
        super(NodeType.TYPE_ICECAST_GENRE, id, parentId, name);
        this.genre = genre;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id, parentId, name, type, modifiedDate, iconUrl, genre);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }

        final IcecastGenreNode other = (IcecastGenreNode) obj;
        return Objects.equal(this.genre, other.genre);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("type", type)
                .add("parentId", parentId)
                .add("name", name)
                .add("modifiedDate", modifiedDate)
                .add("iconUrl", iconUrl)
                .add("genre", genre)
                .toString();
    }
}
