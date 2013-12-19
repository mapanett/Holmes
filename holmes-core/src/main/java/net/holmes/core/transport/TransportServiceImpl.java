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

package net.holmes.core.transport;

import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AbstractScheduledService;
import net.holmes.core.common.configuration.Configuration;
import net.holmes.core.media.model.AbstractNode;
import net.holmes.core.transport.airplay.AirplayDevice;
import net.holmes.core.transport.device.Device;
import net.holmes.core.transport.device.DeviceDao;
import net.holmes.core.transport.device.DeviceStreamer;
import net.holmes.core.transport.device.UnknownDeviceException;
import net.holmes.core.transport.event.StreamingEvent;
import net.holmes.core.transport.session.SessionDao;
import net.holmes.core.transport.session.StreamingSession;
import net.holmes.core.transport.session.UnknownSessionException;
import net.holmes.core.transport.upnp.UpnpDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static net.holmes.core.common.configuration.Parameter.STREAMING_STATUS_UPDATE_DELAY_SECONDS;
import static net.holmes.core.transport.session.SessionStatus.*;

/**
 * Transport service implementation.
 */
public class TransportServiceImpl implements TransportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransportServiceImpl.class);

    private final DeviceDao deviceDao;
    private final SessionDao sessionDao;
    private final DeviceStreamer upnpStreamer;
    private final DeviceStreamer airplayStreamer;

    /**
     * Instantiates a new transport service implementation.
     *
     * @param deviceDao       device DAO
     * @param sessionDao      session DAO
     * @param upnpStreamer    upnp streamer
     * @param airplayStreamer airplay streamer
     */
    @Inject
    public TransportServiceImpl(final Configuration configuration, final DeviceDao deviceDao, final SessionDao sessionDao,
                                @Named("upnp") final DeviceStreamer upnpStreamer,
                                @Named("airplay") final DeviceStreamer airplayStreamer) {
        this.deviceDao = deviceDao;
        this.sessionDao = sessionDao;
        this.upnpStreamer = upnpStreamer;
        this.airplayStreamer = airplayStreamer;

        //Start session status update task
        new UpdateSessionStatusService(configuration.getIntParameter(STREAMING_STATUS_UPDATE_DELAY_SECONDS)).startAsync();
    }

    @Override
    public void addDevice(final Device device) {
        LOGGER.info("Add device {}", device);
        deviceDao.addDevice(device);
    }

    @Override
    public void removeDevice(final String deviceId) {
        LOGGER.info("Remove device {}", deviceId);
        deviceDao.removeDevice(deviceId);
        sessionDao.removeDevice(deviceId);
    }

    @Override
    public Collection<Device> findDevices(final String hostAddress) {
        return deviceDao.findDevices(hostAddress);
    }

    @Override
    public Collection<Device> getDevices() {
        return deviceDao.getDevices();
    }

    @Override
    public StreamingSession getSession(final String deviceId) throws UnknownSessionException {
        return sessionDao.getSession(deviceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void play(final String deviceId, final String contentUrl, final AbstractNode node) throws UnknownDeviceException {
        Device device = deviceDao.getDevice(deviceId);
        sessionDao.initSession(deviceId, contentUrl, node.getName());
        getStreamer(device).play(device, contentUrl, node);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void stop(final String deviceId) throws UnknownDeviceException {
        Device device = deviceDao.getDevice(deviceId);
        getStreamer(device).stop(device);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void pause(final String deviceId) throws UnknownDeviceException {
        Device device = deviceDao.getDevice(deviceId);
        getStreamer(device).pause(device);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void resume(final String deviceId) throws UnknownDeviceException {
        Device device = deviceDao.getDevice(deviceId);
        getStreamer(device).resume(device);
    }

    @SuppressWarnings("unchecked")
    private void updateStatus(final String deviceId) throws UnknownDeviceException {
        Device device = deviceDao.getDevice(deviceId);
        getStreamer(device).updateStatus(device);
    }

    /**
     * Handle streaming event.
     *
     * @param event streaming event
     */
    @Subscribe
    public void handleStreamingEvent(final StreamingEvent event) {
        if (LOGGER.isDebugEnabled()) LOGGER.debug("handle streaming event: {}", event);
        try {
            if (event.isSuccess())
                // Update streaming session
                switch (event.getType()) {
                    case PLAY:
                    case RESUME:
                        sessionDao.updateSessionStatus(event.getDeviceId(), PLAYING);
                        break;
                    case STOP:
                        sessionDao.updateSessionStatus(event.getDeviceId(), WAITING);
                        break;
                    case PAUSE:
                        sessionDao.updateSessionStatus(event.getDeviceId(), PAUSED);
                        break;
                    case STATUS:
                        sessionDao.updateSessionPosition(event.getDeviceId(), event.getPosition(), event.getDuration());
                        break;
                    default:
                        break;
                }
            else
                LOGGER.error("Device: {} - Error:{}", event.getDeviceId(), event.getErrorMessage());

        } catch (UnknownSessionException e) {
            LOGGER.error(e.getMessage());
        }
    }

    /**
     * Get streamer associated to device
     *
     * @param device device
     * @return streamer
     */
    private DeviceStreamer getStreamer(final Device device) {
        if (device instanceof UpnpDevice)
            return upnpStreamer;
        else if (device instanceof AirplayDevice)
            return airplayStreamer;
        throw new IllegalArgumentException("Unknown device type " + device);
    }

    /**
     * Scheduled task to update session status.
     */
    private class UpdateSessionStatusService extends AbstractScheduledService {
        private final int updateStatusDelay;

        /**
         * Instantiates a new update session status service.
         *
         * @param updateStatusDelay update delay.
         */
        public UpdateSessionStatusService(final int updateStatusDelay) {
            this.updateStatusDelay = updateStatusDelay;
        }

        @Override
        protected void runOneIteration() throws Exception {
            Map<String, StreamingSession> sessions = sessionDao.getSessions();
            for (Map.Entry<String, StreamingSession> session : sessions.entrySet())
                if (session.getValue().getStatus() == PLAYING)
                    updateStatus(session.getKey());
        }

        @Override
        protected Scheduler scheduler() {
            return updateStatusDelay > 0 ? Scheduler.newFixedDelaySchedule(updateStatusDelay, updateStatusDelay, SECONDS) : null;
        }
    }
}
