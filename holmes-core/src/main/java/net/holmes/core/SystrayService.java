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

package net.holmes.core;

import net.holmes.core.common.Service;
import net.holmes.core.common.SystemTrayIcon;
import net.holmes.core.common.configuration.Configuration;
import net.holmes.core.common.configuration.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ResourceBundle;

/**
 * Manages system tray icon.
 */
public final class SystrayService implements Service {
    private static final String HOLMES_SITE_URL = "http://ccheneau.github.io/Holmes/";
    private static final String HOLMES_WIKI_URL = "https://github.com/ccheneau/Holmes/wiki";
    private static final String MENU_ITEM_FONT = "MenuItem.font";
    private static final String MENU_ITEM_BOLD_FONT = "MenuItem.bold.font";
    private static final Logger LOGGER = LoggerFactory.getLogger(SystrayService.class);
    private final Configuration configuration;
    private final ResourceBundle resourceBundle;
    private final String localHolmesDataDir;

    /**
     * Instantiates a new systray service.
     *
     * @param configuration      configuration
     * @param resourceBundle     resource bundle
     * @param localHolmesDataDir local Holmes data directory
     */
    @Inject
    public SystrayService(final Configuration configuration, final ResourceBundle resourceBundle, @Named("localHolmesDataDir") String localHolmesDataDir) {
        this.configuration = configuration;
        this.resourceBundle = resourceBundle;
        this.localHolmesDataDir = localHolmesDataDir;
    }

    @Override
    public void start() {
        // Add system tray icon
        if (configuration.getParameter(Parameter.ENABLE_SYSTRAY) && initUIManager()) initSystemTrayMenu();
    }

    @Override
    public void stop() {
        // Nothing
    }

    /**
     * Initializes UI manager.
     *
     * @return true on success
     */
    private boolean initUIManager() {
        boolean result = true;
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // Add bold font for systray menu item
            Font menuItemFont = UIManager.getFont(MENU_ITEM_FONT);
            if (menuItemFont != null)
                UIManager.put(MENU_ITEM_BOLD_FONT, new FontUIResource(menuItemFont.getFamily(), Font.BOLD, menuItemFont.getSize()));

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            result = false;
        }
        return result;
    }

    /**
     * Initialize system tray menu.
     */
    private void initSystemTrayMenu() {
        // Check the SystemTray is supported
        if (!SystemTray.isSupported()) return;

        // Initialize systray icon
        final Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/logo.png"));
        final SystemTrayIcon holmesTrayIcon = new SystemTrayIcon(image, resourceBundle.getString("systray.title"));
        final SystemTray systemTray = SystemTray.getSystemTray();

        // Create a popup menu
        final JPopupMenu popupMenu = new JPopupMenu();

        // Quit Holmes menu item
        Icon holmesExitIcon = new ImageIcon(getClass().getResource("/icon-exit.png"));
        JMenuItem quitItem = new JMenuItem(resourceBundle.getString("systray.quit"), holmesExitIcon);
        quitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                try {
                    System.exit(0);
                } catch (SecurityException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        });

        // Holmes logs menu item
        Icon holmesLogsIcon = new ImageIcon(getClass().getResource("/icon-logs.png"));
        JMenuItem logsItem = new JMenuItem(resourceBundle.getString("systray.logs"), holmesLogsIcon);
        logsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (Desktop.isDesktopSupported()) {
                    try {

                        Desktop.getDesktop().open(Paths.get(localHolmesDataDir, "log", "holmes.log").toFile());
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });

        // Holmes ui menu item
        Icon holmesUiIcon = new ImageIcon(getClass().getResource("/icon-logo.png"));
        JMenuItem holmesUiItem = new JMenuItem(resourceBundle.getString("systray.holmes.ui"), holmesUiIcon);
        Font boldFont = UIManager.getFont(MENU_ITEM_BOLD_FONT);
        if (boldFont != null) holmesUiItem.setFont(boldFont);

        holmesUiItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        String holmesUrl = "http://localhost:" + configuration.getHttpServerPort() + "/";
                        Desktop.getDesktop().browse(new URI(holmesUrl));
                    } catch (IOException | URISyntaxException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });

        // Holmes site menu item
        Icon holmesSiteIcon = new ImageIcon(getClass().getResource("/icon-site.png"));
        JMenuItem holmesSiteItem = new JMenuItem(resourceBundle.getString("systray.holmes.home"), holmesSiteIcon);
        holmesSiteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(HOLMES_SITE_URL));
                    } catch (IOException | URISyntaxException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });

        // Holmes wiki menu item
        Icon holmesWikiIcon = new ImageIcon(getClass().getResource("/icon-info.png"));
        JMenuItem holmesWikiItem = new JMenuItem(resourceBundle.getString("systray.holmes.wiki"), holmesWikiIcon);
        holmesWikiItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(HOLMES_WIKI_URL));
                    } catch (IOException | URISyntaxException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
            }
        });

        // Add items to popup menu
        popupMenu.add(holmesUiItem);
        popupMenu.addSeparator();
        popupMenu.add(holmesSiteItem);
        popupMenu.add(holmesWikiItem);
        popupMenu.add(logsItem);
        popupMenu.addSeparator();
        popupMenu.add(quitItem);

        // Add tray icon
        holmesTrayIcon.setImageAutoSize(true);
        holmesTrayIcon.setPopupMenu(popupMenu);
        try {
            systemTray.add(holmesTrayIcon);
        } catch (AWTException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}