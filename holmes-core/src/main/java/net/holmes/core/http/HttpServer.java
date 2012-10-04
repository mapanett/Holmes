/**
* Copyright (C) 2012  Cedric Cheneau
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
package net.holmes.core.http;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import net.holmes.core.IServer;
import net.holmes.core.configuration.IConfiguration;
import net.holmes.core.util.log.InjectLogger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;

/**
 * HTTP server main class  
 */
public final class HttpServer implements IServer {
    @InjectLogger
    private Logger logger;

    public static final String HTTP_SERVER_NAME = "Holmes http server";

    private ChannelGroup allChannels = null;
    private ServerBootstrap bootstrap = null;
    private final IChannelPipelineFactory pipelineFactory;
    private final IConfiguration configuration;

    @Inject
    public HttpServer(IChannelPipelineFactory pipelineFactory, IConfiguration configuration) {
        this.pipelineFactory = pipelineFactory;
        this.configuration = configuration;
        // Init channel group
        allChannels = new DefaultChannelGroup(HttpServer.class.getName());
    }

    /* (non-Javadoc)
     * @see net.holmes.core.IServer#start()
     */
    @Override
    public void start() {
        if (logger.isInfoEnabled()) logger.info("Starting HTTP server");

        InetSocketAddress bindAddress = new InetSocketAddress(configuration.getHttpServerPort());

        // Configure the server.
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        // Set up the event pipeline factory.
        pipelineFactory.setChannelGroup(allChannels);
        bootstrap.setPipelineFactory(pipelineFactory);

        // Bind and start server to accept incoming connections.
        allChannels.add(bootstrap.bind(bindAddress));

        if (logger.isInfoEnabled()) logger.info("HTTP server bound on " + bindAddress);
    }

    /* (non-Javadoc)
     * @see net.holmes.core.IServer#stop()
     */
    @Override
    public void stop() {
        if (logger.isInfoEnabled()) logger.info("Stopping HTTP server");

        // Stop the server
        if (bootstrap != null) {
            allChannels.close();
            bootstrap.releaseExternalResources();
        }

        if (logger.isInfoEnabled()) logger.info("HTTP server stopped");
    }
}
