/**
* Copyright (C) 2012-2013  Cedric Cheneau
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
package net.holmes.core.backend.backbone.handler;

import java.util.Collection;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.holmes.core.backend.backbone.BackboneManager;
import net.holmes.core.backend.backbone.response.ConfigurationFolder;
import net.holmes.core.configuration.Configuration;

@Path("/backend/backbone/pictureFolders")
public class PictureFoldersHandler {

    private final Configuration configuration;
    private final BackboneManager backboneManager;

    @Inject
    public PictureFoldersHandler(Configuration configuration, BackboneManager backboneManager) {
        this.backboneManager = backboneManager;
        this.configuration = configuration;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ConfigurationFolder> getPictureFolders() {
        return backboneManager.getFolders(configuration.getPictureFolders());
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationFolder getPictureFolder(@PathParam("id") String id) {
        return backboneManager.getFolder(id, configuration.getPictureFolders(), false);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationFolder addPictureFolder(ConfigurationFolder folder) {
        backboneManager.addFolder(folder, configuration.getPictureFolders(), false);
        return folder;
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationFolder editPictureFolder(@PathParam("id") String id, ConfigurationFolder folder) {
        backboneManager.editFolder(id, folder, configuration.getPictureFolders(), false);
        return folder;
    }

    @DELETE
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConfigurationFolder removePictureFolder(@PathParam("id") String id) {
        backboneManager.removeFolder(id, configuration.getPictureFolders(), false);
        return new ConfigurationFolder(id, null, null);
    }
}
