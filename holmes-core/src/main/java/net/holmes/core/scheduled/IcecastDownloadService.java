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

package net.holmes.core.scheduled;

import com.google.common.util.concurrent.AbstractScheduledService;
import net.holmes.core.common.configuration.Configuration;
import net.holmes.core.common.configuration.Parameter;
import net.holmes.core.media.dao.IcecastDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * Icecast directory download service.
 */
public class IcecastDownloadService extends AbstractScheduledService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IcecastDownloadService.class);
    private final IcecastDao icecastDao;
    private final int downloadDelayMinutes;

    /**
     * Instantiates a new Icecast directory download service.
     *
     * @param icecastDao    Icecast DAO
     * @param configuration configuration
     */
    @Inject
    public IcecastDownloadService(final IcecastDao icecastDao, final Configuration configuration) {
        this.icecastDao = icecastDao;
        this.downloadDelayMinutes = configuration.getIntParameter(Parameter.ICECAST_YELLOW_PAGE_DOWNLOAD_DELAY_MINUTES);

    }

    @Override
    protected void runOneIteration() {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("Start Icecast directory download");
        if (icecastDao.downloadYellowPage()) icecastDao.parseYellowPage();
        if (LOGGER.isDebugEnabled()) LOGGER.debug("End Icecast directory download");
    }

    @Override
    protected Scheduler scheduler() {
        if (downloadDelayMinutes > 0)
            return Scheduler.newFixedDelaySchedule(0, downloadDelayMinutes, TimeUnit.MINUTES);
        return null;
    }
}