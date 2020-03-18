package com.zbl;


import com.zbl.websocket.WebsocketFramHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@SpringBootApplication
@EnableDiscoveryClient
public class NettyServerApplication implements CommandLineRunner {
	public static void main(String[] args) {
		SpringApplication.run(NettyServerApplication.class,args);
	}

	@Override
	public void run(String... args) throws Exception {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		EventLoopGroup bososGroup = new NioEventLoopGroup(1);
		EventLoopGroup workGroup = new NioEventLoopGroup(8);
		try {

			serverBootstrap.group(bososGroup, workGroup);
			serverBootstrap.channel(NioServerSocketChannel.class);
			serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
			serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					//http编码和解码
					pipeline.addLast(new HttpServerCodec());
					//添加块处理器
					pipeline.addLast(new ChunkedWriteHandler());
					//添加聚合
					pipeline.addLast(new HttpObjectAggregator(8192));
					//添加websocket处理器
					pipeline.addLast(new WebSocketServerProtocolHandler("/websocket"));
					//处理业务的handler
					pipeline.addLast(new WebsocketFramHandler());
				}
			});
			ChannelFuture bind = serverBootstrap.bind(19001);
			bind.channel().closeFuture().sync();
		}catch (Exception e){
			System.out.println("启动失败");
		}finally {
			bososGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}
}
