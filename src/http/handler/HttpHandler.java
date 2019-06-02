package http.handler;

import http.HttpProtocol;
import http.protocols.MyWebsite;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;


import java.util.HashMap;
import java.util.Map;


import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpUtil.*;
import static io.netty.handler.codec.http.HttpVersion.*;



/**
 * Handles a server-side channel.
 */
public class HttpHandler extends SimpleChannelInboundHandler<Object> {

    HttpRequest request;

    Map<String, Object> attributes;

    HttpProtocol protocol;

    public HttpHandler() {
        attributes = new HashMap<>();
        protocol = new MyWebsite();
        protocol.onLoad();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (is100ContinueExpected(request)) {
                write100(ctx);
            }

            attributes.clear();

            protocol.addAttributes(request, attributes);

        }

        if (msg instanceof HttpContent) {
            if (msg instanceof LastHttpContent) {
                LastHttpContent trailer = (LastHttpContent) msg;
                writeResponse(trailer, ctx);
            }
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static void write100(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {

        byte[] buf = protocol.response(attributes);

        HttpResponseStatus status = buf == null ? NOT_FOUND : OK;

        if (buf == null)
            buf = new byte[0];

        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.decoderResult().isSuccess()? status : BAD_REQUEST,
                Unpooled.copiedBuffer(buf));

        boolean keepAlive = isKeepAlive(request);

        // Encode the cookie.
        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);

        protocol.responseHeaders(response, cookieString, attributes, keepAlive);

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }
}
