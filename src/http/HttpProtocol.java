package http;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import java.util.Map;

public abstract class HttpProtocol {

    protected String baseDirectory;
    protected HttpConfig httpConfig;

    public HttpProtocol(String directory) {
        this.baseDirectory = directory;
        this.httpConfig = HttpConfig.serialize(baseDirectory.concat("/config.json"));
    }

    public abstract void onLoad();

    public void addAttributes(HttpRequest request, Map<String, Object> attributes) {
        HttpHeaders headers = request.headers();
        attributes.put("type", request.method());
        attributes.put("uri", request.uri());
        if (headers.get("Accept") != null)
            attributes.put("response-type", headers.get("Accept").split(",")[0]);
        else
            attributes.put("response-type", "*/*");
        attributes.put("user-agent", headers.get("User-Agent"));
        attributes.put("keep-alive", headers.get("Connection").equals("keep-alive"));
    }

    public abstract byte[] response(Map<String, Object> attributes);
    public abstract void responseHeaders(FullHttpResponse response, String cookies, Map<String, Object> attributes, boolean keepAlive);

}
