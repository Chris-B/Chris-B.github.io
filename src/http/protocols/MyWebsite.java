package http.protocols;

import file.FileHandler;
import http.HttpProtocol;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.Map;
import java.util.Set;

public class MyWebsite extends HttpProtocol {

    public MyWebsite() {
        super("view");
    }

    @Override
    public void onLoad() {}

    public void addAttributes(HttpRequest request, Map<String, Object> attributes) {
        super.addAttributes(request, attributes);
    }

    private byte[] GET(Map<String, Object> attributes) {
        String path = attributes.get("uri").toString();
        if (!path.startsWith("/"))//Safety feature
            return null;
        if (path.equals("/"))
            path = path.concat("index.html");
        byte[] fBytes = FileHandler.getFileBytes("view".concat(path));
        return fBytes;
    }

    private byte[] POST(Map<String, Object> attributes) {
        return null;
    }

    @Override
    public byte[] response(Map<String, Object> attributes) {
        String reqType = attributes.get("type").toString();
        byte[] response = null;
        switch (reqType) {
            case "GET":
                response = GET(attributes);
                break;
            case "POST":
                response = POST(attributes);
                break;
        }
        if (response != null && attributes.get("response-type").toString().contains("html"))
            response = processHtml(response);
        return response;
    }

    private byte[] processHtml(byte[] buf) {
        byte[] hBytes = FileHandler.getFileBytes("view/header.html");
        byte[] fBytes = FileHandler.getFileBytes("view/footer.html");
        String header = new String(hBytes);
        String footer = new String(fBytes);
        String body = new String(buf);
        body = body.replace("{header}", header);
        body = body.replace("{footer}", footer);
        return body.getBytes();
    }

    @Override
    public void responseHeaders(FullHttpResponse response, String cookieString, Map<String, Object> attributes, boolean keepAlive) {
        String contentType = attributes.get("response-type").toString();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
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
    }

}
