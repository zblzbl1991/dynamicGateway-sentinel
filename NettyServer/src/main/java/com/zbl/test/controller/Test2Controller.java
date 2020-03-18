package com.zbl.test.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;

public class Test2Controller {

	public static void main(String[] args) throws IOException {
		PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:com/zbl/*/controller/**.class");
		for (Resource resource : resources) {
			System.out.println(resource.getDescription());
		}
	}
}
