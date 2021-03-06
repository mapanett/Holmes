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

package net.holmes.core.business.media.dao;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import net.holmes.core.business.configuration.ConfigurationDao;
import net.holmes.core.business.configuration.ConfigurationNode;
import net.holmes.core.business.media.dao.icecast.IcecastDao;
import net.holmes.core.business.media.dao.icecast.IcecastEntry;
import net.holmes.core.business.media.dao.icecast.IcecastGenre;
import net.holmes.core.business.media.dao.index.MediaIndexDao;
import net.holmes.core.business.media.dao.index.MediaIndexElement;
import net.holmes.core.business.media.model.*;
import net.holmes.core.business.mimetype.MimeTypeManager;
import net.holmes.core.common.MediaType;
import net.holmes.core.common.MimeType;
import net.holmes.core.common.exception.HolmesException;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static net.holmes.core.business.media.dao.index.MediaIndexElementFactory.buildConfigMediaIndexElement;
import static net.holmes.core.business.media.model.AbstractNode.NodeType.TYPE_ICECAST_ENTRY;
import static net.holmes.core.business.media.model.AbstractNode.NodeType.TYPE_UNKNOWN;
import static net.holmes.core.business.media.model.RootNode.ICECAST;
import static net.holmes.core.business.media.model.RootNode.PODCAST;
import static net.holmes.core.common.ConfigurationParameter.PODCAST_CACHE_EXPIRE_HOURS;
import static net.holmes.core.common.ConfigurationParameter.PODCAST_CACHE_MAX_ELEMENTS;
import static net.holmes.core.common.FileUtils.*;
import static net.holmes.core.common.MediaType.TYPE_RAW_URL;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Media dao implementation.
 */
public class MediaDaoImpl implements MediaDao {
    private static final Logger LOGGER = getLogger(MediaDaoImpl.class);
    private final ConfigurationDao configurationDao;
    private final MimeTypeManager mimeTypeManager;
    private final MediaIndexDao mediaIndexDao;
    private final IcecastDao icecastDao;
    private final Cache<String, List<AbstractNode>> podcastCache;

    /**
     * Instantiates a new media dao implementation.
     *
     * @param configurationDao configuration dao
     * @param mimeTypeManager  mime type manager
     * @param mediaIndexDao    media index dao
     * @param icecastDao       Icecast dao
     */
    @Inject
    public MediaDaoImpl(final ConfigurationDao configurationDao, final MimeTypeManager mimeTypeManager, final MediaIndexDao mediaIndexDao,
                        final IcecastDao icecastDao) {
        this.configurationDao = configurationDao;
        this.mimeTypeManager = mimeTypeManager;
        this.mediaIndexDao = mediaIndexDao;
        this.icecastDao = icecastDao;
        this.podcastCache = CacheBuilder.newBuilder()
                .maximumSize(configurationDao.getParameter(PODCAST_CACHE_MAX_ELEMENTS))
                .expireAfterWrite(configurationDao.getParameter(PODCAST_CACHE_EXPIRE_HOURS), TimeUnit.HOURS)
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractNode getNode(String nodeId) {
        AbstractNode node = null;
        // Get node in mediaIndex
        MediaIndexElement indexElement = mediaIndexDao.get(nodeId);
        if (indexElement != null) {
            MediaType mediaType = MediaType.getByValue(indexElement.getMediaType());
            switch (mediaType) {
                case TYPE_PODCAST:
                    // Podcast node
                    node = new PodcastNode(nodeId, PODCAST.getId(), indexElement.getName(), indexElement.getPath());
                    break;
                case TYPE_ICECAST_GENRE:
                    // Icecast genre node
                    node = new IcecastGenreNode(nodeId, ICECAST.getId(), indexElement.getName(), indexElement.getPath());
                    break;
                case TYPE_RAW_URL:
                    // Raw Url node
                    node = new RawUrlNode(TYPE_UNKNOWN, nodeId, indexElement.getParentId(), indexElement.getName(), MimeType.valueOf(indexElement.getMimeType()), indexElement.getPath(), null);
                    break;
                default:
                    // File node
                    node = getFileNode(nodeId, indexElement, mediaType);
                    break;
            }
        } else {
            LOGGER.warn("[getNode] {} not found in media index", nodeId);
        }
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbstractNode> getChildNodes(String parentNodeId) {
        List<AbstractNode> childNodes = Lists.newArrayList();

        // Get node in mediaIndex
        MediaIndexElement indexElement = mediaIndexDao.get(parentNodeId);
        if (indexElement != null) {
            // Get media type
            MediaType mediaType = MediaType.getByValue(indexElement.getMediaType());
            switch (mediaType) {
                case TYPE_PODCAST:
                    // Get podcast entries
                    childNodes.addAll(getPodcastEntries(parentNodeId, indexElement.getPath()));
                    break;
                case TYPE_ICECAST_GENRE:
                    // Get Icecast entries
                    childNodes.addAll(getIcecastEntries(parentNodeId, indexElement.getPath()));
                    break;
                case TYPE_RAW_URL:
                    // Nothing
                    break;
                default:
                    // Get folder child nodes
                    childNodes.addAll(getFolderChildNodes(parentNodeId, indexElement.getPath(), mediaType));
                    break;
            }
        } else {
            LOGGER.error("[getChildNodes] {} node not found in media index", parentNodeId);
        }

        return childNodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbstractNode> getRootNodeChildren(final RootNode rootNode) {
        List<AbstractNode> nodes = Lists.newArrayList();
        if (rootNode == ICECAST) {
            // Add Icecast genres
            for (IcecastGenre genre : icecastDao.getGenres()) {
                // Upper case genre's first letter
                String genreName = Character.toUpperCase(genre.getName().charAt(0)) + genre.getName().substring(1);
                // Add Icecast genre to media index
                mediaIndexDao.put(genre.getId(), new MediaIndexElement(rootNode.getId(), rootNode.getMediaType().getValue(), null, genre.getName(), genre.getName(), rootNode.isLocalPath(), true));
                // Add child node
                nodes.add(new IcecastGenreNode(genre.getId(), rootNode.getId(), genreName, genre.getName()));
            }
        } else {
            // Add nodes defined in configuration
            for (ConfigurationNode configNode : configurationDao.getNodes(rootNode)) {
                // Add node to mediaIndex
                mediaIndexDao.put(configNode.getId(), buildConfigMediaIndexElement(rootNode, configNode));
                // Add child node
                if (rootNode == PODCAST) {
                    nodes.add(new PodcastNode(configNode.getId(), PODCAST.getId(), configNode.getLabel(), configNode.getPath()));
                } else {
                    nodes.add(new FolderNode(configNode.getId(), rootNode.getId(), configNode.getLabel(), new File(configNode.getPath())));
                }
            }
        }
        return nodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cleanUpCache() {
        podcastCache.cleanUp();
        mediaIndexDao.clean();
    }
    /**
     * Get file or folder node
     *
     * @param nodeId       node id
     * @param indexElement index element
     * @param mediaType    media type
     * @return file or folder node
     */
    private AbstractNode getFileNode(String nodeId, MediaIndexElement indexElement, MediaType mediaType) {
        AbstractNode node = null;
        File nodeFile = new File(indexElement.getPath());
        if (isValidFile(nodeFile)) {
            // Content node
            MimeType mimeType = mimeTypeManager.getMimeType(nodeFile.getName());
            if (mimeType != null) {
                node = buildContentNode(nodeId, indexElement.getParentId(), nodeFile, mediaType, mimeType);
            }
        } else if (isValidDirectory(nodeFile)) {
            // Folder node
            String nodeName = indexElement.getName() != null ? indexElement.getName() : nodeFile.getName();
            node = new FolderNode(nodeId, indexElement.getParentId(), nodeName, nodeFile);
        }
        return node;
    }

    /**
     * Get children of a folder node.
     *
     * @param parentId   parent node id
     * @param folderPath folder path
     * @param mediaType  media type
     * @return folder child nodes matching media type
     */
    private List<AbstractNode> getFolderChildNodes(final String parentId, final String folderPath, final MediaType mediaType) {
        List<AbstractNode> nodes = Lists.newArrayList();
        for (File file : listChildren(folderPath, true)) {
            // Add node to mediaIndex
            if (file.isDirectory()) {
                // Add folder node
                String nodeId = mediaIndexDao.add(new MediaIndexElement(parentId, mediaType.getValue(), null, file.getAbsolutePath(), null, true, false));
                nodes.add(new FolderNode(nodeId, parentId, file.getName(), file));
            } else {
                // Add content node
                addContentNode(nodes, parentId, file, mediaType);
            }
        }
        return nodes;
    }

    /**
     * Gets pod-cast entries. A pod-cast is a RSS.
     *
     * @param podcastId  podcast id
     * @param podcastUrl podcast url
     * @return entries parsed from pod-cast RSS feed
     */
    @SuppressWarnings("unchecked")
    private List<AbstractNode> getPodcastEntries(final String podcastId, final String podcastUrl) {
        try {
            return podcastCache.get(podcastUrl, new PodcastCacheCallable(podcastId, podcastUrl));
        } catch (ExecutionException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return Lists.newArrayListWithCapacity(0);
    }

    /**
     * Gets Icecast entries by genre.
     *
     * @param genre genre
     * @return Icecast entries
     */
    private Collection<AbstractNode> getIcecastEntries(final String parentNodeId, final String genre) {
        Collection<AbstractNode> result = Lists.newArrayList();
        for (IcecastEntry entry : icecastDao.getEntriesByGenre(genre)) {
            // Add entry to media index
            String nodeId = mediaIndexDao.add(new MediaIndexElement(parentNodeId, TYPE_RAW_URL.getValue(), entry.getType(), entry.getUrl(), entry.getName(), false, false));
            // Add Raw Url to result
            result.add(new RawUrlNode(TYPE_ICECAST_ENTRY, nodeId, parentNodeId, entry.getName(), MimeType.valueOf(entry.getType()), entry.getUrl(), null));
        }
        return result;
    }

    /**
     * Add content node to node list.
     *
     * @param nodes     node list
     * @param parentId  parent id
     * @param file      file
     * @param mediaType media type
     */
    private void addContentNode(final List<AbstractNode> nodes, final String parentId, final File file, final MediaType mediaType) {
        MimeType mimeType = mimeTypeManager.getMimeType(file.getName());
        if (mimeType != null) {
            // Add file node
            String nodeId = mediaIndexDao.add(new MediaIndexElement(parentId, mediaType.getValue(), mimeType.getMimeType(), file.getAbsolutePath(), null, true, false));
            ContentNode node = buildContentNode(nodeId, parentId, file, mediaType, mimeType);
            if (node != null) {
                nodes.add(node);
            }
        }
    }

    /**
     * Build content node.
     *
     * @param nodeId    node id
     * @param parentId  parent id
     * @param file      file
     * @param mediaType media type
     * @return content node
     */
    private ContentNode buildContentNode(final String nodeId, final String parentId, final File file, final MediaType mediaType, final MimeType mimeType) {
        // Check mime type
        return mimeType.getType() == mediaType || mimeType.isSubTitle() ? new ContentNode(nodeId, parentId, file.getName(), file, mimeType) : null;
    }

    /**
     * Podcast cache callable
     */
    private class PodcastCacheCallable implements Callable<List<AbstractNode>> {
        private final String podcastId;
        private final String podcastUrl;

        /**
         * Instantiates a new podcast cache callable.
         *
         * @param podcastId  podcast id
         * @param podcastUrl podcast URL
         */
        PodcastCacheCallable(final String podcastId, final String podcastUrl) {
            this.podcastId = podcastId;
            this.podcastUrl = podcastUrl;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<AbstractNode> call() throws HolmesException {
            // No entries in cache, read them from RSS feed
            // First remove children from media index
            mediaIndexDao.removeChildren(podcastId);

            // Then parse podcast
            return new PodcastParser() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public String addMediaIndexElement(MediaIndexElement mediaIndexElement) {
                    // Add element to media index
                    return mediaIndexDao.add(mediaIndexElement);
                }
            }.parse(podcastUrl, podcastId);
        }
    }
}
