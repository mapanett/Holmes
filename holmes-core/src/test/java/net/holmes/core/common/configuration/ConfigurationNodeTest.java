/*
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

package net.holmes.core.common.configuration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * ConfigurationNode Tester.
 */
public class ConfigurationNodeTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getId()
     */
    @Test
    public void testGetId() throws Exception {
        ConfigurationNode node = buildConfigurationNode("");
        assertEquals(node.getId(), "id");
    }

    /**
     * Method: getLabel()
     */
    @Test
    public void testGetLabel() throws Exception {
        ConfigurationNode node = buildConfigurationNode("");
        assertEquals(node.getLabel(), "label");
    }

    /**
     * Method: setLabel(final String label)
     */
    @Test
    public void testSetLabel() throws Exception {
        ConfigurationNode node = buildConfigurationNode("");
        node.setLabel("newLabel");
        assertEquals(node.getLabel(), "newLabel");
    }

    /**
     * Method: getPath()
     */
    @Test
    public void testGetPath() throws Exception {
        ConfigurationNode node = buildConfigurationNode("");
        assertEquals(node.getPath(), "path");
    }

    /**
     * Method: setPath(final String path)
     */
    @Test
    public void testSetPath() throws Exception {
        ConfigurationNode node = buildConfigurationNode("");
        node.setPath("newPath");
        assertEquals(node.getPath(), "newPath");
    }

    /**
     * Method: hashCode()
     */
    @Test
    public void testHashCode() throws Exception {
        ConfigurationNode node1 = buildConfigurationNode("");
        ConfigurationNode node2 = buildConfigurationNode("");
        ConfigurationNode node3 = buildConfigurationNode("3");
        assertNotNull(node1.hashCode());
        assertNotNull(node2.hashCode());
        assertNotNull(node3.hashCode());
        assertEquals(node1.hashCode(), node2.hashCode());
        assertNotEquals(node1.hashCode(), node3.hashCode());
    }

    /**
     * Method: equals(final Object obj)
     */
    @Test
    public void testEquals() throws Exception {
        ConfigurationNode node1 = buildConfigurationNode("");
        ConfigurationNode node2 = buildConfigurationNode("");
        ConfigurationNode node3 = buildConfigurationNode("3");
        assertEquals(node1, node2);
        assertNotEquals(node1, node3);
    }

    /**
     * Method: toString()
     */
    @Test
    public void testToString() throws Exception {
        ConfigurationNode node1 = buildConfigurationNode("");
        ConfigurationNode node2 = buildConfigurationNode("");
        assertNotNull(node1.toString());
        assertEquals(node1.toString(), node2.toString());
    }

    private ConfigurationNode buildConfigurationNode(String suffix) {
        return new ConfigurationNode("id" + suffix, "label" + suffix, "path" + suffix);
    }
}