package http;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslContext;
import http.handler.HttpHandler;


/**
 * Initializer for HTTP server. Socket Based Channel.
 * sslCtx: Self Signed Certificate Context
 * HttpInitializer(SslContext): Receives previously setup SSL and sets local var.
 * initChannel(SocketChannel): Sets up Http pipeline. SSL -> Decoder -> Handler -> Encoder
 */
public class HttpInitializer extends ChannelInitializer<SocketChannel> {

    private final SslContext sslCtx;

    public HttpInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        if (sslCtx != null) {
            p.addLast(sslCtx.newHandler(ch.alloc()));
        }
        p.addLast(new HttpRequestDecoder());
        p.addLast(new HttpResponseEncoder());
        p.addLast(new HttpContentCompressor(1));
        p.addLast(new HttpHandler());
    }
}