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

package net.holmes.core.business.configuration;

import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import net.holmes.core.business.media.model.RootNode;
import net.holmes.core.common.parameter.ConfigurationParameter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * XML configuration dao implementation.
 */
public final class XmlConfigurationDaoImpl implements ConfigurationDao {

    private static final String CONF_FILE_NAME = "config.xml";
    private static final String CONF_DIR = "conf";
    private final String localHolmesDataDir;
    private final XStream xstream;
    private XmlRootNode rootNode = null;

    /**
     * Instantiates a new xml configuration.
     *
     * @param localHolmesDataDir local Holmes data directory
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Inject
    public XmlConfigurationDaoImpl(@Named("localHolmesDataDir") final String localHolmesDataDir) throws IOException {
        this.localHolmesDataDir = localHolmesDataDir;

        // Instantiates a new XStream
        this.xstream = new XStream(new DomDriver("UTF-8"));

        // Define XStream aliases
        this.xstream.alias("config", XmlRootNode.class);
        this.xstream.alias("node", ConfigurationNode.class);
        this.xstream.ignoreUnknownElements();

        // Load configuration
        loadConfig();
    }

    /**
     * Get Holmes configuration file path.
     *
     * @return configuration file path
     */
    private Path getConfigFile() {
        Path confPath = Paths.get(localHolmesDataDir, CONF_DIR);
        if (Files.isDirectory(confPath) || confPath.toFile().mkdirs())
            return Paths.get(confPath.toString(), CONF_FILE_NAME);

        throw new RuntimeException("Failed to create " + confPath);
    }

    /**
     * Load configuration from Xml file.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void loadConfig() throws IOException {
        boolean configLoaded = false;

        Path confFile = getConfigFile();
        if (Files.isReadable(confFile))
            try (InputStream in = new FileInputStream(confFile.toFile())) {
                // Load configuration from XML
                rootNode = (XmlRootNode) xstream.fromXML(in);
                configLoaded = true;
            } catch (FileNotFoundException e) {
                //Ignore
            }

        if (rootNode == null) rootNode = new XmlRootNode();
        rootNode.checkDefaultValues();
        rootNode.checkParameters();

        // Save default config if nothing is loaded
        if (!configLoaded) saveConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveConfig() throws IOException {
        try (OutputStream out = new FileOutputStream(getConfigFile().toFile())) {
            // Save configuration to XML
            xstream.toXML(rootNode, out);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ConfigurationNode> getNodes(final RootNode rootNode) {
        List<ConfigurationNode> nodes;
        switch (rootNode) {
            case AUDIO:
                nodes = this.rootNode.getAudioFolders();
                break;
            case PICTURE:
                nodes = this.rootNode.getPictureFolders();
                break;
            case PODCAST:
                nodes = this.rootNode.getPodcasts();
                break;
            case VIDEO:
                nodes = this.rootNode.getVideoFolders();
                break;
            default:
                nodes = Lists.newArrayList();
                break;
        }
        return nodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getParameter(final ConfigurationParameter<T> parameter) {
        String value = this.rootNode.getParameter(parameter.getName());
        if (value != null)
            return parameter.parse(value);
        return parameter.getDefaultValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void setParameter(final ConfigurationParameter<T> parameter, T value) {
        this.rootNode.setParameter(parameter.getName(), parameter.format(value));
    }
}
