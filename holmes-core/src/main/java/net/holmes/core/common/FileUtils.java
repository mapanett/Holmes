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

package net.holmes.core.common;

import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;

/**
 * File utility class.
 */
public final class FileUtils {

    /**
     * Private constructor
     */
    private FileUtils() {
    }

    /**
     * Check if file is readable.
     *
     * @param file file
     * @return true if this is a valid file
     */
    public static boolean isValidFile(File file) {
        return file.isFile() && file.canRead() && !file.isHidden();
    }

    /**
     * Check if file is a readable directory.
     *
     * @param fileName file name
     * @return true if this is a valid directory
     */
    public static boolean isValidDirectory(String fileName) {
        return isValidDirectory(new File(fileName));
    }

    /**
     * Check if file is a readable directory.
     *
     * @param file file
     * @return true if this is a valid directory
     */
    public static boolean isValidDirectory(File file) {
        return file.isDirectory() && file.canRead() && !file.isHidden();
    }

    /**
     * List readable child folders and files.
     *
     * @param parentPath   parent directory path
     * @param includeFiles include files in result
     * @return child files
     */
    public static List<File> listChildren(String parentPath, boolean includeFiles) {
        List<File> fileList = Lists.newArrayList();
        File[] children = new File(parentPath).listFiles();
        if (children != null) {
            for (File child : children) {
                if (includeFiles && isValidFile(child)) {
                    fileList.add(child);
                } else if (isValidDirectory(child) && child.listFiles() != null) {
                    fileList.add(child);
                }
            }
        }
        return fileList;
    }
}
