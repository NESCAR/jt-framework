package io.github.hylexus.jt808.samples.customized.config;

import io.github.hylexus.jt.exception.MsgEscapeException;
import io.github.hylexus.jt808.codec.BytesEncoder;
import io.github.hylexus.jt808.converter.MsgTypeParser;
import io.github.hylexus.jt808.ext.AuthCodeValidator;
import io.github.hylexus.jt808.samples.customized.converter.LocationUploadMsgBodyConverter2;
import io.github.hylexus.jt808.samples.customized.handler.LocationInfoUploadMsgHandler;
import io.github.hylexus.jt808.support.MsgHandlerMapping;
import io.github.hylexus.jt808.support.RequestMsgBodyConverterMapping;
import io.github.hylexus.jt808.support.netty.Jt808ChannelHandlerAdapter;
import io.github.hylexus.jt808.support.netty.Jt808ServerConfigure;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * @author hylexus
 * Created At 2019-09-22 3:43 下午
 */
@Slf4j
@Configuration
public class Jt808Config extends Jt808ServerConfigure {

    /**
     * 配置boot strap
     * <pre>
     *     ChannelOption.SO_BACKLOG：tcp/ip协议listen函数中的backlog参数，初始化服务端可连接队列
     *     ChannelOption.SO_REUSEADDR：对应于套接字选项中的SO_REUSEADDR，这个参数表示允许重复使用本地地址和端口
     *     ChannelOption.SO_KEEPALIVE：对应于套接字选项中的SO_KEEPALIVE，该参数用于设置TCP连接，当设置该选项以后，连接会测试链接的状态（比如2小时发送一个测试包）
     *     ChannelOption.SO_SNDBUF和ChannelOption.SO_RCVBUF：发送和接受缓存区
     *     ChannelOption.SO_LINGER：对应于套接字选项中的SO_LINGER，可以阻塞close()的调用时间，直到数据完全发送
     *     ChannelOption.TCP_NODELAY：对应于套接字选项中的TCP_NODELAY，Nagle算法是将小的数据包组装为更大的帧然后进行发送，而不是输入一次发送一次,因此在数据包不足的时候会等待其他数据的到了，组装成大的数据包进行发送
     *     参考：https://blog.csdn.net/zhongzunfa/article/details/94590670
     * </pre>
     * @param serverBootstrap boot strap
     */
    @Override
    public void configureServerBootstrap(ServerBootstrap serverBootstrap) {
        super.configureServerBootstrap(serverBootstrap);
    }

    /**
     * 配置套接字通道
     * @param ch 通道
     * @param jt808ChannelHandlerAdapter 处理器
     */
    @Override
    public void configureSocketChannel(SocketChannel ch, Jt808ChannelHandlerAdapter jt808ChannelHandlerAdapter) {
        super.configureSocketChannel(ch, jt808ChannelHandlerAdapter);
    }

    /**
     * 配置消息转化器
     * @param mapping 转化器映射
     */
    @Override
    public void configureMsgConverterMapping(RequestMsgBodyConverterMapping mapping) {
        super.configureMsgConverterMapping(mapping);
        mapping.registerConverter(Jt808MsgType.CLIENT_LOCATION_INFO_UPLOAD, new LocationUploadMsgBodyConverter2());
    }

    /**
     * 配置消息处理器，可以配置多个
     * @param mapping 消息处理映射
     */
    @Override
    public void configureMsgHandlerMapping(MsgHandlerMapping mapping) {
        super.configureMsgHandlerMapping(mapping);
        mapping.registerHandler(Jt808MsgType.CLIENT_LOCATION_INFO_UPLOAD, new LocationInfoUploadMsgHandler());
    }

    /**
     * 提供Bytes解码器
     * @return BytesEncoder
     */
    @Override
    public BytesEncoder supplyBytesEncoder() {
        return new BytesEncoder() {

            private final BytesEncoder bytesEncoder = new BytesEncoder.DefaultBytesEncoder();

            @Override
            public byte[] doEscapeForReceive(byte[] bytes, int start, int end) throws MsgEscapeException {
                return bytesEncoder.doEscapeForReceive(bytes, start, end);
            }

            @Override
            public byte[] doEscapeForSend(byte[] bytes, int start, int end) throws MsgEscapeException {
                return bytesEncoder.doEscapeForSend(bytes, start, end);
            }
        };
    }

    /**
     * 提供验证码鉴权
     * @return 验证码鉴权
     */
    @Override
    public AuthCodeValidator supplyAuthCodeValidator() {
        return (session, requestMsgMetadata, authRequestMsgBody) -> {
            final String terminalId = session.getTerminalId();
            final String authCode = authRequestMsgBody.getAuthCode();
            // 从其他服务验证鉴权码是否正确
            log.info("AuthCode validate for terminal : {} with authCode : {}, result: {}", terminalId, authCode, true);
            return true;
        };
    }

    @Override
    public MsgTypeParser supplyMsgTypeParser() {
        return new Jt808MsgTypeParser();
    }

}
