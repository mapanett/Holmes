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

package net.holmes.core.service.http;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import net.holmes.core.business.configuration.ConfigurationDao;
import net.holmes.core.common.MimeType;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.Values.CLOSE;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static net.holmes.core.common.ConfigurationParameter.HTTP_SERVER_CACHE_SECOND;
import static org.easymock.EasyMock.*;

public class HttpFileRequestHandlerTest {

    @Test
    public void testFileRequestHandler() throws Exception {
        File indexHtml = File.createTempFile("index", ".html");
        indexHtml.deleteOnExit();

        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HOST, "localhost");

        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        FullHttpRequest httpRequest = createMock(FullHttpRequest.class);
        Channel channel = createMock(Channel.class);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(60);
        expect(httpRequest.headers()).andReturn(headers).atLeastOnce();
        expect(httpRequest.getProtocolVersion()).andReturn(HTTP_1_1).atLeastOnce();
        expect(context.write(isA(HttpResponse.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.write(isA(ChunkedFile.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.writeAndFlush(isA(LastHttpContent.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();

        HttpFileRequest request = new HttpFileRequest(httpRequest, new File(indexHtml.getAbsolutePath()), MimeType.valueOf("text/html"), false);

        replay(context, httpRequest, channel, configurationDao);
        HttpFileRequestHandler handler = new HttpFileRequestHandler(configurationDao);
        handler.channelRead0(context, request);
        verify(context, httpRequest, channel, configurationDao);
    }

    @Test
    public void testFileRequestHandlerNoCache() throws Exception {
        File indexHtml = File.createTempFile("index", ".html");
        indexHtml.deleteOnExit();

        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HOST, "localhost");

        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        FullHttpRequest httpRequest = createMock(FullHttpRequest.class);
        Channel channel = createMock(Channel.class);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(0);
        expect(httpRequest.headers()).andReturn(headers).atLeastOnce();
        expect(httpRequest.getProtocolVersion()).andReturn(HTTP_1_1).atLeastOnce();
        expect(context.write(isA(HttpResponse.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.write(isA(ChunkedFile.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.writeAndFlush(isA(LastHttpContent.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();

        HttpFileRequest request = new HttpFileRequest(httpRequest, new File(indexHtml.getAbsolutePath()), MimeType.valueOf("text/html"), true);

        replay(context, httpRequest, channel, configurationDao);
        HttpFileRequestHandler handler = new HttpFileRequestHandler(configurationDao);
        handler.channelRead0(context, request);
        verify(context, httpRequest, channel, configurationDao);
    }

    @Test(expected = HttpFileRequestException.class)
    public void testFileRequestHandlerInvalidFile() throws Exception {
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HOST, "localhost");

        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        FullHttpRequest httpRequest = createMock(FullHttpRequest.class);

        HttpFileRequest request = new HttpFileRequest(httpRequest, new File("invalidFile"), MimeType.valueOf("text/html"), false);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(60);

        replay(context, httpRequest, configurationDao);
        try {
            HttpFileRequestHandler handler = new HttpFileRequestHandler(configurationDao);
            handler.channelRead0(context, request);
        } finally {
            verify(context, httpRequest, configurationDao);
        }
    }

    @Test
    public void testFileRequestHandlerWithOffset() throws Exception {
        File indexHtml = File.createTempFile("index", ".html");
        FileWriter fw = new FileWriter(indexHtml);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("some content in index.html");
        bw.close();
        indexHtml.deleteOnExit();

        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HOST, "localhost");
        headers.add(RANGE, "bytes=5-");

        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        FullHttpRequest httpRequest = createMock(FullHttpRequest.class);
        Channel channel = createMock(Channel.class);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(60);
        expect(httpRequest.headers()).andReturn(headers).atLeastOnce();
        expect(httpRequest.getProtocolVersion()).andReturn(HTTP_1_1).atLeastOnce();
        expect(context.write(isA(HttpResponse.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.write(isA(ChunkedFile.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.writeAndFlush(isA(LastHttpContent.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();

        HttpFileRequest request = new HttpFileRequest(httpRequest, new File(indexHtml.getAbsolutePath()), MimeType.valueOf("text/html"), false);

        replay(context, httpRequest, channel, configurationDao);
        HttpFileRequestHandler handler = new HttpFileRequestHandler(configurationDao);
        handler.channelRead0(context, request);
        verify(context, httpRequest, channel, configurationDao);
    }

    @Test(expected = HttpFileRequestException.class)
    public void testFileRequestHandlerWithEmptyOffset() throws Exception {
        File indexHtml = File.createTempFile("index", ".html");
        indexHtml.deleteOnExit();

        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HOST, "localhost");
        headers.add(RANGE, "");

        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        FullHttpRequest httpRequest = createMock(FullHttpRequest.class);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(60);
        expect(httpRequest.headers()).andReturn(headers).atLeastOnce();

        HttpFileRequest request = new HttpFileRequest(httpRequest, new File(indexHtml.getAbsolutePath()), MimeType.valueOf("text/html"), true);

        replay(context, httpRequest, configurationDao);
        try {
            HttpFileRequestHandler handler = new HttpFileRequestHandler(configurationDao);
            handler.channelRead0(context, request);
        } finally {
            verify(context, httpRequest, configurationDao);
        }
    }

    @Test(expected = HttpFileRequestException.class)
    public void testFileRequestHandlerWithBadOffset() throws Exception {
        File indexHtml = File.createTempFile("index", ".html");
        indexHtml.deleteOnExit();

        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HOST, "localhost");
        headers.add(RANGE, "bytes=5-");

        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        FullHttpRequest httpRequest = createMock(FullHttpRequest.class);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(60);
        expect(httpRequest.headers()).andReturn(headers).atLeastOnce();

        HttpFileRequest request = new HttpFileRequest(httpRequest, new File(indexHtml.getAbsolutePath()), MimeType.valueOf("text/html"), true);

        replay(context, httpRequest, configurationDao);
        try {
            HttpFileRequestHandler handler = new HttpFileRequestHandler(configurationDao);
            handler.channelRead0(context, request);
        } finally {
            verify(context, httpRequest, configurationDao);
        }
    }

    @Test
    public void testFileRequestHandlerWithoutKeepAlive() throws Exception {
        File indexHtml = File.createTempFile("index", ".html");
        indexHtml.deleteOnExit();

        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HOST, "localhost");
        headers.add(CONNECTION, CLOSE);

        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        FullHttpRequest httpRequest = createMock(FullHttpRequest.class);
        Channel channel = createMock(Channel.class);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(60);
        expect(httpRequest.headers()).andReturn(headers).atLeastOnce();
        expect(context.write(isA(HttpResponse.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.write(isA(ChunkedFile.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.writeAndFlush(isA(LastHttpContent.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();

        HttpFileRequest request = new HttpFileRequest(httpRequest, new File(indexHtml.getAbsolutePath()), MimeType.valueOf("text/html"), true);

        replay(context, httpRequest, channel, configurationDao);
        HttpFileRequestHandler handler = new HttpFileRequestHandler(configurationDao);
        handler.channelRead0(context, request);
        verify(context, httpRequest, channel, configurationDao);
    }

    @Test
    public void testExceptionCaughtHttpRequestException() throws Exception {
        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        Channel channel = createMock(Channel.class);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(60);
        expect(context.channel()).andReturn(channel).atLeastOnce();
        expect(channel.isActive()).andReturn(true).atLeastOnce();
        expect(channel.writeAndFlush(isA(Object.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        replay(context, channel, configurationDao);
        new HttpFileRequestHandler(configurationDao).exceptionCaught(context, new HttpFileRequestException("message", NOT_FOUND));
        verify(context, channel, configurationDao);
    }

    @Test
    public void testExceptionCaughtIoException() throws Exception {
        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        Channel channel = createMock(Channel.class);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(60);
        expect(context.channel()).andReturn(channel).atLeastOnce();
        expect(channel.isActive()).andReturn(true).atLeastOnce();
        expect(channel.writeAndFlush(isA(Object.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        replay(context, channel, configurationDao);
        new HttpFileRequestHandler(configurationDao).exceptionCaught(context, new IOException());
        verify(context, channel, configurationDao);
    }

    @Test
    public void testExceptionCaughtHttpRequestChannelInactive() throws Exception {
        ConfigurationDao configurationDao = createMock(ConfigurationDao.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        Channel channel = createMock(Channel.class);

        expect(configurationDao.getParameter(HTTP_SERVER_CACHE_SECOND)).andReturn(60);
        expect(context.channel()).andReturn(channel).atLeastOnce();
        expect(channel.isActive()).andReturn(false).atLeastOnce();
        replay(context, channel, configurationDao);
        new HttpFileRequestHandler(configurationDao).exceptionCaught(context, new HttpFileRequestException("message", NOT_FOUND));
        verify(context, channel, configurationDao);
    }
}
