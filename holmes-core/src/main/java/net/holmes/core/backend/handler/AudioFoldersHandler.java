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

package net.holmes.core.backend.handler;

import net.holmes.core.backend.BackendManager;
import net.holmes.core.backend.response.ConfigurationFolder;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.Collection;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static net.holmes.core.business.media.model.RootNode.AUDIO;

/**
 * Handler for audio folders REST requests.
 */
@Path("/backend/audioFolders")
public final class AudioFoldersHandler extends AbstractFolderHandler {

    /**
     * Instantiates a new audio folders handler.
     *
     * @param backendManager backend manager
     */
    @Inject
    public AudioFoldersHandler(final BackendManager backendManager) {
        super(backendManager, AUDIO);
    }

    /**
     * Get audio folders.
     *
     * @return audio folders
     */
    @GET
    @Produces(APPLICATION_JSON)
    public Collection<ConfigurationFolder> getAudioFolders() {
        return getFolders();
    }

    /**
     * Get audio folder.
     *
     * @param id audio folder id
     * @return audio folder
     */
    @GET
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public ConfigurationFolder getAudioFolder(@PathParam("id") final String id) {
        return getFolder(id);
    }

    /**
     * Add audio folder.
     *
     * @param folder audio folder to add
     * @return added audio folder
     */
    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public ConfigurationFolder addAudioFolder(final ConfigurationFolder folder) {
        return addFolder(folder);
    }

    /**
     * Edit audio folder.
     *
     * @param id     audio folder id
     * @param folder folder value
     * @return edited audio folder
     */
    @PUT
    @Path("/{id}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public ConfigurationFolder editAudioFolder(@PathParam("id") final String id, final ConfigurationFolder folder) {
        return editFolder(id, folder);
    }

    /**
     * Remove audio folder.
     *
     * @param id audio folder id to remove
     * @return removed audio folder
     */
    @DELETE
    @Path("/{id}")
    @Produces(APPLICATION_JSON)
    public ConfigurationFolder removeAudioFolder(@PathParam("id") final String id) {
        return removeFolder(id);
    }
}
