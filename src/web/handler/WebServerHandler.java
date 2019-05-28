package web.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.http.cookie.Cookie;
import web.request.GET;
import web.request.POST;
import web.request.Type;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpUtil.*;
import static io.netty.handler.codec.http.HttpVersion.*;



/**
 * Handles a server-side channel.
 */
public class WebServerHandler extends SimpleChannelInboundHandler<Object> {

    HttpRequest request;

    Map<String, Object> attributes = new HashMap<>();

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            if (is100ContinueExpected(request)) {
                write100(ctx);
            }

            attributes.clear();

            HttpHeaders headers = request.headers();
            attributes.put("type", request.method());
            attributes.put("version", request.protocolVersion());
            attributes.put("host", headers.get(HttpHeaderNames.HOST, "unknown"));
            attributes.put("uri", request.uri());
            attributes.put("response-type", headers.get("Accept").split(",")[0]);
            attributes.put("user-agent", headers.get("User-Agent"));
            attributes.put("keep-alive", headers.get("Connection").equals("keep-alive"));

            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> params = queryStringDecoder.parameters();
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

    Type getRequestType(HttpMethod type) {
        switch (type.name()) {
            case "GET":
                return new GET();
            case "POST":
                return new POST();
        }
        return null;
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {

        boolean keepAlive = isKeepAlive(request);

        StringBuilder buf = getRequestType((HttpMethod) attributes.get("type")).response(attributes);

        HttpResponseStatus status = buf.length() == 0 ? NOT_FOUND : OK;

        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, currentObj.decoderResult().isSuccess()? status : BAD_REQUEST,
                Unpooled.copiedBuffer(buf.toString(), CharsetUtil.UTF_8));

        String contentType = attributes.get("response-type").toString();

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);

        if (keepAlive) {

            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        // Encode the cookie.
        String cookieString = request.headers().get(HttpHeaderNames.COOKIE);
        if (cookieString != null) {
            Set<Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieString);
            if (!cookies.isEmpty()) {
                // Reset the cookies if necessary.
                for (Cookie cookie: cookies) {
                    response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
                }
            }
        } else {
            // Browser sent no cookie.  Add some.
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key1", "value1"));
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("key2", "value2"));
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }
}
