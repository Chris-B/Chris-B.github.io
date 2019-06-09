package http.protocols;

import file.FileHandler;
import http.HttpProtocol;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.util.Map;
import java.util.Set;

public class FarmerJohn extends HttpProtocol {

    public FarmerJohn() {
        super("farmer-john");
    }

    @Override
    public void onLoad() {}

    @Override
    public void addAttributes(HttpRequest request, Map<String, Object> attributes) {
        super.addAttributes(request, attributes);
    }

    private byte[] GET(Map<String, Object> attributes) {
        String path = attributes.get("uri").toString();
        if (!path.startsWith("/")
                || path.contains("compressed")
                || path.contains("json"))//Safety feature
            return null;
        if (path.equals("/"))
            path = path.concat("index.html");
        byte[] fBytes = FileHandler.getFileBytes(baseDirectory.concat(path));
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
        if (response != null
                && attributes.get("response-type") != null
                && attributes.get("response-type").toString().contains("html"))
            response = processHtml(response);
        return response;
    }

    private String updateHeader(String header) {
        return header.replace("{title}", httpConfig.title)
        .replace("{author}", httpConfig.author)
        .replace("{description}", httpConfig.description)
        .replace("{charset}", httpConfig.charset);
    }

    private byte[] processHtml(byte[] buf) {
        byte[] hBytes = FileHandler.getFileBytes(baseDirectory.concat("/header.html"));
        byte[] fBytes = FileHandler.getFileBytes(baseDirectory.concat("/footer.html"));
        String header = updateHeader(new String(hBytes));
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
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("farmer", "john"));
            response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode("chris", "eisah"));
        }
    }

}
