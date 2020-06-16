package cn.tedu.store.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimeElapsedAspect {

	@Around("execution(* cn.tedu.store.service.impl.*.*(..))")
	public Object process(ProceedingJoinPoint pjp) throws Throwable {
		// 记录起始时间
		long start = System.currentTimeMillis();
		
		// 执行任务
		Object obj = pjp.proceed();
		
		// 记录结束时间
		long end = System.currentTimeMillis();
		
		// 统计耗时
		System.err.println("耗时: " + (end - start) +"ms.");
	
		// 返回
		return obj;
	}
}
