package http;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Map;

public abstract class HttpProtocol {

    public abstract void onLoad();
    public abstract void addAttributes(HttpRequest request, Map<String, Object> attributes);
    public abstract byte[] response(Map<String, Object> attributes);
    public abstract void responseHeaders(FullHttpResponse response, String cookies, Map<String, Object> attributes, boolean keepAlive);

}
