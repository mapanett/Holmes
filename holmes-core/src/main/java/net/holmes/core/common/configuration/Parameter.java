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

/**
 * Configuration parameters.
 */
public enum Parameter {

    PREPEND_PODCAST_ENTRY_NAME("prepend_podcast_entry_name", "false"), // 
    ENABLE_SYSTRAY("enable_systray", "true"), //
    ENABLE_UPNP("enable_upnp", "true"), //
    ENABLE_EXTERNAL_SUBTITLES("enable_external_subtitles", "true"), //
    ENABLE_CONTENT_RESOLUTION("enable_content_resolution", "true"), //
    ENABLE_ICECAST_DIRECTORY("enable_icecast_directory", "true"), //
    PODCAST_CACHE_MAX_ELEMENTS("podcast_cache_max_elements", "50"), //
    PODCAST_CACHE_EXPIRE_HOURS("podcast_cache_expire_hours", "2"), //
    IMAGE_CACHE_MAX_ELEMENTS("image_cache_max_elements", "5000"), //
    IMAGE_CACHE_EXPIRE_HOURS("image_cache_expire_hours", "2"), //
    LOCAL_CACHE_CLEAN_DELAY_MINUTES("local_cache_clean_delay_minutes", "60"), //
    MEDIA_INDEX_CLEAN_DELAY_MINUTES("media_index_clean_delay_minutes", "15"), //
    MEDIA_SCAN_ALL_DELAY_MINUTES("media_scan_all_delay_minutes", "0"), //
    UPNP_SERVICE_PORT("upnp_service_port", String.valueOf(Configuration.DEFAULT_UPNP_SERVICE_PORT)), //
    ICONS_IN_SYSTRAY_MENU("icons_in_systray_menu", "true"),//
    ICECAST_YELLOW_PAGE_URL("icecast_yellow_page_url", "http://dir.xiph.org/yp.xml"), //
    ICECAST_YELLOW_PAGE_DOWNLOAD_DELAY_HOURS("icecast_yellow_page_download_delay_hours", "24"),//
    ICECAST_GENRE_LIST("icecast_genre_list", "70s,80s,90s,adult,alternative,ambient,anime,bass,best,blues,chill,christian,classic,classical,club,college,community,contemporary,country,dance,deep,disco,dj,downtempo,drum,dubstep,eclectic,electro,folk,fun,funk,game,gospel,hip hop,hiphop,hit,house,indie,instrumental,international,jazz,jpop,jrock,latin,live,lounge,metal,minecraft,minimal,misc,mix,mixed,music,musique,news,oldies,pop,progressive,promodj,punk,radio,rap,reggae,religious,rnb,rock,salsa,scanner,smooth,soul,sport,sports,talk,techno,top,trance,urban,various,webradio,world");

    private final String name;
    private final String defaultValue;

    /**
     * Instantiates a new parameter.
     *
     * @param name         parameter name
     * @param defaultValue parameter default value
     */
    Parameter(final String name, final String defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
