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

package net.holmes.core.http.handler;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultChannelPromise;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedFile;
import net.holmes.core.common.mimetype.MimeTypeManager;
import net.holmes.core.test.TestModule;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.io.File;

import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HttpUIRequestHandlerTest {

    private Injector injector;
    @Inject
    private MimeTypeManager mimeTypeManager;

    @Before
    public void setUp() {
        injector = Guice.createInjector(new TestModule());
        injector.injectMembers(this);
    }

    private HttpUIRequestHandler getHandler() {
        HttpUIRequestHandler httpUIRequestHandler = new HttpUIRequestHandler(mimeTypeManager, System.getProperty("java.io.tmpdir"));
        injector.injectMembers(httpUIRequestHandler);
        return httpUIRequestHandler;
    }

    @Test
    public void testAccept() {
        HttpUIRequestHandler httpUIRequestHandler = getHandler();
        assertTrue(httpUIRequestHandler.accept(new DefaultFullHttpRequest(HTTP_1_1, GET, "/")));
        assertFalse(httpUIRequestHandler.accept(new DefaultFullHttpRequest(HTTP_1_1, GET, "/backend/something")));
    }

    @Test
    public void testProcessRequest() throws Exception {
        File indexHtml = File.createTempFile("index", ".html");
        indexHtml.deleteOnExit();
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HOST, "localhost");

        FullHttpRequest request = createMock(FullHttpRequest.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        expect(request.headers()).andReturn(headers).atLeastOnce();
        expect(request.getUri()).andReturn("/" + indexHtml.getName()).atLeastOnce();
        expect(request.getProtocolVersion()).andReturn(HTTP_1_1).atLeastOnce();
        expect(request.getMethod()).andReturn(GET).atLeastOnce();

        Channel channel = createMock(Channel.class);

        expect(context.write(isA(HttpResponse.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.write(isA(ChunkedFile.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.writeAndFlush(isA(LastHttpContent.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();

        replay(request, context, channel);
        HttpUIRequestHandler httpUIRequestHandler = getHandler();
        httpUIRequestHandler.channelRead0(context, request);
        verify(request, context, channel);
    }

    @Test
    public void testProcessRequestPost() throws Exception {
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        FullHttpRequest request = createMock(FullHttpRequest.class);

        expect(request.getMethod()).andReturn(POST).atLeastOnce();
        expect(context.fireChannelRead(request)).andReturn(context).atLeastOnce();
        replay(request, context);
        try {
            HttpUIRequestHandler httpUIRequestHandler = getHandler();
            httpUIRequestHandler.channelRead0(context, request);
        } finally {
            verify(request, context);
        }
    }

    @Test
    public void testProcessRequestWithoutKeepAlive() throws Exception {
        File indexHtml = File.createTempFile("index", ".html");
        indexHtml.deleteOnExit();
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add(HOST, "localhost");
        headers.add(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);

        FullHttpRequest request = createMock(FullHttpRequest.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        expect(request.headers()).andReturn(headers).atLeastOnce();
        expect(request.getUri()).andReturn("/" + indexHtml.getName()).atLeastOnce();
        expect(request.getMethod()).andReturn(GET).atLeastOnce();

        Channel channel = createMock(Channel.class);

        expect(context.write(isA(HttpResponse.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.write(isA(ChunkedFile.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();
        expect(context.writeAndFlush(isA(LastHttpContent.class))).andReturn(new DefaultChannelPromise(channel)).atLeastOnce();

        replay(request, context, channel);
        HttpUIRequestHandler httpUIRequestHandler = getHandler();
        httpUIRequestHandler.channelRead0(context, request);
        verify(request, context, channel);
    }

    @Test
    public void testProcessRequestBadMimeType() throws Exception {

        FullHttpRequest request = createMock(FullHttpRequest.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);
        expect(request.getUri()).andReturn("/index.html1").atLeastOnce();
        expect(request.getMethod()).andReturn(GET).atLeastOnce();

        Channel channel = createMock(Channel.class);

        expect(context.fireChannelRead(request)).andReturn(context).atLeastOnce();

        replay(request, context, channel);
        HttpUIRequestHandler httpUIRequestHandler = getHandler();
        httpUIRequestHandler.channelRead0(context, request);
        verify(request, context, channel);
    }

    @Test(expected = HttpRequestException.class)
    public void testProcessRequestNonExistingIndex() throws Exception {
        FullHttpRequest request = createMock(FullHttpRequest.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);

        expect(request.getUri()).andReturn("/").atLeastOnce();
        expect(request.getMethod()).andReturn(GET).atLeastOnce();

        replay(request, context);
        try {
            HttpUIRequestHandler httpUIRequestHandler = getHandler();
            httpUIRequestHandler.channelRead0(context, request);
        } finally {
            verify(request, context);
        }
    }

    @Test(expected = HttpRequestException.class)
    public void testProcessRequestEmptyFile() throws Exception {
        FullHttpRequest request = createMock(FullHttpRequest.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);

        expect(request.getUri()).andReturn("/").atLeastOnce();
        expect(request.getMethod()).andReturn(GET).atLeastOnce();

        replay(request, context);
        try {
            HttpUIRequestHandler httpUIRequestHandler = getHandler();
            httpUIRequestHandler.channelRead0(context, request);
        } finally {
            verify(request, context);
        }
    }

    @Test(expected = HttpRequestException.class)
    public void testProcessRequestNonExistingFile() throws Exception {
        FullHttpRequest request = createMock(FullHttpRequest.class);
        ChannelHandlerContext context = createMock(ChannelHandlerContext.class);

        expect(request.getUri()).andReturn("/badFile.html").atLeastOnce();
        expect(request.getMethod()).andReturn(GET).atLeastOnce();

        replay(request, context);
        try {
            HttpUIRequestHandler httpUIRequestHandler = getHandler();
            httpUIRequestHandler.channelRead0(context, request);
        } finally {
            verify(request, context);
        }
    }
}