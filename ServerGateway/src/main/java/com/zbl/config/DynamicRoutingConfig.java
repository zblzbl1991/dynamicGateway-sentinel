package com.zbl.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executor;

@Configuration
@RefreshScope
public class DynamicRoutingConfig implements ApplicationEventPublisherAware {
	private final Logger logger = LoggerFactory.getLogger(DynamicRoutingConfig.class);
	@Value("${spring.cloud.nacos.dynamic-data-id}")
	private String DATA_ID;
	@Value("${spring.cloud.nacos.config.server-addr}")
	private String nacosServerUrl;
	private static final String Group = "DEFAULT_GROUP";


	@Autowired
	private RouteDefinitionWriter routeDefinitionWriter;

	private ApplicationEventPublisher applicationEventPublisher;

	@Bean
	public void refreshRouting() throws NacosException {
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.SERVER_ADDR, nacosServerUrl);
//		properties.put(PropertyKeyConst.NAMESPACE, "public");
		ConfigService configService = NacosFactory.createConfigService(properties);
		//初始化获取当前路由配置
		String config = initDynamicRoutes(configService);
		configService.addListener(DATA_ID, Group, new Listener() {
			@Override
			public Executor getExecutor() {
				return null;
			}
			@Override
			public void receiveConfigInfo(String configInfo) {
				logger.info(configInfo);

					List<RouteDefinition> definitions = JSON.parseArray(configInfo, RouteDefinition.class);
				definitions.forEach(definition->update(definition));
			}
		});
	}

	/**
	 * 初始化动态路由配置
	 * @param configService
	 * @return
	 * @throws NacosException
	 */
	private String initDynamicRoutes(ConfigService configService) throws NacosException {
		String configInfo = configService.getConfig(DATA_ID, Group, 5000);
		List<RouteDefinition> definitions = JSON.parseArray(configInfo, RouteDefinition.class);
		definitions.forEach(definition->update(definition));
		return configInfo;
	}

	public String update(RouteDefinition routeDefinition){
		try {
			this.routeDefinitionWriter.delete(Mono.just(routeDefinition.getId()));
		} catch (Exception e) {
			return "update fail,not find route  routeId: "+routeDefinition.getId();
		}
		try {
			routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
			this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
			return "success";
		} catch (Exception e) {
			return "update route  fail";
		}
	}
	//删除路由
	public Mono<ResponseEntity<Object>> delete(String id) {
		return this.routeDefinitionWriter.delete(Mono.just(id))
				.then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
				.onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
	}
	//增加路由
	public String add(RouteDefinition definition) {
		routeDefinitionWriter.save(Mono.just(definition)).subscribe();
		this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
		return "success";
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public static void main(String[] args) throws NacosException, InterruptedException {
		String serverAddr = "localhost";
		String dataId = "gateway";
		String group = "DEFAULT_GROUP";
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
		ConfigService configService = NacosFactory.createConfigService(properties);
		String content = configService.getConfig(dataId, group, 5000);
		System.out.println(content);
		configService.addListener(dataId, group, new Listener() {
			@Override
			public void receiveConfigInfo(String configInfo) {
				System.out.println("recieve:" + configInfo);
			}

			@Override
			public Executor getExecutor() {
				return null;
			}
		});

		boolean isPublishOk = configService.publishConfig(dataId, group, "content");
		System.out.println(isPublishOk);

		Thread.sleep(3000);
		content = configService.getConfig(dataId, group, 5000);
		System.out.println(content);

		boolean isRemoveOk = configService.removeConfig(dataId, group);
		System.out.println(isRemoveOk);
		Thread.sleep(3000);

		content = configService.getConfig(dataId, group, 5000);
		System.out.println(content);
		Thread.sleep(300000);

	}
}
