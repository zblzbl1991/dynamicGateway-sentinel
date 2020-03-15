//package com.zbl.websocket;
//
//
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.channel.ChannelFuture;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.codec.http.HttpObjectAggregator;
//import io.netty.handler.codec.http.HttpServerCodec;
//import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
//import io.netty.handler.logging.LogLevel;
//import io.netty.handler.logging.LoggingHandler;
//import io.netty.handler.stream.ChunkedWriteHandler;
//
////@SpringBootApplication
//public class WebsocketApplication  {
//	public static void main(String[] args) throws Exception {
////		SpringApplication.run(WebsocketApplication.class,args);
//		WebsocketApplication websocketApplication = new WebsocketApplication();
//		websocketApplication.run("start");
//	}
//
//	public void run(String... args) throws Exception {
//		ServerBootstrap serverBootstrap = new ServerBootstrap();
//		EventLoopGroup bososGroup = new NioEventLoopGroup(1);
//		EventLoopGroup workGroup = new NioEventLoopGroup(8);
//		try {
//
//
//			serverBootstrap.group(bososGroup, workGroup);
//			serverBootstrap.channel(NioServerSocketChannel.class);
//			serverBootstrap.handler(new LoggingHandler(LogLevel.INFO));
//			serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
//				@Override
//				protected void initChannel(SocketChannel ch) throws Exception {
//					ChannelPipeline pipeline = ch.pipeline();
//					//http编码和解码
//					pipeline.addLast(new HttpServerCodec());
//					//添加块处理器
//					pipeline.addLast(new ChunkedWriteHandler());
//					//添加聚合
//					pipeline.addLast(new HttpObjectAggregator(8192));
//					//添加websocket处理器
//					pipeline.addLast(new WebSocketServerProtocolHandler("/websocket"));
//					//处理业务的handler
//					pipeline.addLast(new WebsocketFramHandler());
//				}
//			});
//			ChannelFuture bind = serverBootstrap.bind(19000);
//			bind.channel().closeFuture().sync();
//		}catch (Exception e){
//			System.out.println("启动失败");
//		}finally {
//			bososGroup.shutdownGracefully();
//			workGroup.shutdownGracefully();
//		}
//	}
//
//
//}
