# 项目简介 #
借鉴ajax的理念，让我们biz层的代码也可以是一种异步+并行的模式进行处理，提升我们系统的单位时间内的吞吐量和tps。
  * 异步的方式直接返回对象，减少减少I/O等待的时间
  * 以多线程并行执行,提升系统的吞吐量，减少因异步后出现的等待时间。
主要适用于：I/O密集型应用，低CPU计算

# 工程包依赖 #
  * cglib 2.2
  * asm 3.1
  * spring 2.5
  * slf4j and log4j

# 主要功能介绍 #

## 使用例子 ##
### 例子1(基于Spring的FactoryBean使用) ###
```
<!-- async相关配置 -->
<bean id="asyncLoadExecutor" class="com.agapple.asyncload.AsyncLoadExecutor" init-method="initital" destroy-method="destory">
	<property name="poolSize" value="10" />
	<property name="acceptCount" value="100" />
	<property name="mode" value="REJECT" />
	<property name="needThreadLocalSupport" value="true" />
</bean>
<bean id="asyncLoadMethodMatch" class="com.agapple.asyncload.impl.AsyncLoadPerl5RegexpMethodMatcher" >
	<property name="patterns">
		<list>
			<value>(.*)RemoteModel(.*)</value>
		</list>
	</property>
	<property name="excludedPatterns">
		<list>
			<value>(.*)listRemoteModel(.*)</value>
		</list>
	</property>
	<property name="excludeOveride" value="false" />
</bean>
<bean id="asyncLoadConfig" class="com.agapple.asyncload.AsyncLoadConfig">
	<property name="defaultTimeout" value="3000" />
	<property name="matches">
		<map>
			<entry key-ref="asyncLoadMethodMatch" value="2000" />
		</map>
	</property>
</bean>
<!-- 异步加载模FactoryBean -->
<bean id="asyncLoadTestFactoryBean" class="com.agapple.asyncload.impl.spring.AsyncLoadFactoryBean">
	<property name="targetClass" value="com.agapple.asyncload.domain.AsyncLoadTestService" /><!-- 指定具体的代理目标class -->
	<property name="target">
		<ref bean="asyncLoadTestService" />
	</property>
	<property name="executor" ref="asyncLoadExecutor" />
	<property name="config" ref="asyncLoadConfig" />
</bean>
```

说明：
  * 客户端直接依赖注入的对象asyncLoadTestFactoryBean进行使用
  * 可以配置对应的asyncLoadMethodMatch，定义自己关注的切入点。哪些方法需要或者不需做并行加载。

### 例子2(基于Spring的template模板模式使用) ###
```
<!-- 异步加载模板类 -->
<bean id="asyncLoadTemplate" class="com.agapple.asyncload.impl.template.AsyncLoadTemplate" >
	<property name="executor" ref="asyncLoadExecutor" />
	<property name="defaultTimeout" value="3000" />
</bean>
```

**使用代码：**
```
AsyncLoadTestModel model2 = asyncLoadTemplate.execute(new AsyncLoadCallback<AsyncLoadTestModel>() {

            public AsyncLoadTestModel doAsyncLoad() {
                // 总共sleep 2000ms
                return asyncLoadTestService.getRemoteModel("ljhtest", 1000);
            }
        });
        asyncLoadTestService.getRemoteModel("ljhtest", 1000);
```

说明：
  * 依赖了asyncLoadExecutor配置
  * 允许编程式的使用asyncload机制
  * 允许自定义异步加载的执行体AsyncLoadCallback。

### 例子3(基于Spring的advise模式使用) ###
```
<!--  并行加载拦截器 -->
<bean id="asyncLoadInterceptor" class="com.alibaba.pivot.common.asyncload.impl.spring.AsyncLoadInterceptor" >
	<property name="asyncLoadTemplate" ref="asyncLoadTemplate" />
</bean>
<!-- 拦截器配置 -->
<!-- 对datafeeder进行并行加载拦截，使用cglib -->
<bean class="org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator">
	<property name="proxyTargetClass" value="true" />
	<property name="beanNames">
		<list>
			<value>*DataFeeder</value>
		</list>
	</property>
	<property name="interceptorNames">
		<list>
		    <value>asyncLoadInterceptor</value>
		</list>
	</property>
</bean>
```
说明：
  * 依赖asyncLoadTemplate配置
  * 结合BeanNameAutoProxyCreator可以无嵌入的修改当前的应用使其支持/不支持并行加载模式。

### 例子4(直接编程式) ###
```
// 初始化config
AsyncLoadConfig config = new AsyncLoadConfig(3 * 1000l);
// 初始化executor
AsyncLoadExecutor executor = new AsyncLoadExecutor(10, 100);
executor.initital();
// 初始化proxy
AsyncLoadEnhanceProxy<AsyncLoadTestService> proxy = new AsyncLoadEnhanceProxy<AsyncLoadTestService>();
proxy.setService(asyncLoadTestService);
proxy.setConfig(config);
proxy.setExecutor(executor);
// 执行测试
AsyncLoadTestService service = proxy.getProxy();
AsyncLoadTestModel model1 = service.getRemoteModel("first", 1000); // 每个请求sleep 1000ms
AsyncLoadTestModel model2 = service.getRemoteModel("two", 1000); // 每个请求sleep 1000ms

long start = 0, end = 0;
start = System.currentTimeMillis();
System.out.println(model1.getDetail());
end = System.currentTimeMillis();
Assert.assertTrue((end - start) > 500l); // 第一次会阻塞, 响应时间会在1000ms左右

start = System.currentTimeMillis();
System.out.println(model2.getDetail());
end = System.currentTimeMillis();
Assert.assertTrue((end - start) < 500l); // 第二次不会阻塞，因为第一个已经阻塞了1000ms
// 销毁executor
executor.destory();
```

这里有几个概念，后续补上