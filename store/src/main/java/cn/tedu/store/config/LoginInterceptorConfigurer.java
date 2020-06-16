package cn.tedu.store.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import cn.tedu.store.interceptor.LoginInterceptor;


/**
 * 登录拦截器的配置类
 */
@Configuration
public class LoginInterceptorConfigurer 
	implements WebMvcConfigurer {

	@Override
	public void addInterceptors(
			InterceptorRegistry registry) {
		// 创建拦截器对象
		HandlerInterceptor interceptor 
			= new LoginInterceptor();
		
		//创建白名单
		List<String> excludePaths = new ArrayList<>();
		excludePaths.add("/js/**");
		excludePaths.add("/css/**");
		excludePaths.add("/bootstrap3/**");
		excludePaths.add("/images/**");
		
		excludePaths.add("/web/register.html");
		excludePaths.add("/web/login.html");
		excludePaths.add("/web/index.html");
		excludePaths.add("/web/product.html");
		
		excludePaths.add("/users/reg");
		excludePaths.add("/users/login");
		excludePaths.add("/districts/");
		excludePaths.add("/goods/**");
		
		//注册拦截器，并设置黑白名单
		registry.addInterceptor(interceptor)
			.addPathPatterns("/**")
			.excludePathPatterns(excludePaths);
	}

	
}
