# 监控

## 使用方式

* 引入maven依赖
* spring扫描到com.wsss包，加载包内的类
## 监控点
* 所有的系统入口，包括http接口，dubbo接口，mq消费等
* 所有的系统出口，包括rpc调用，redis调用，mysql调用等
* 比较重要的缓存的缓存命中率
## 默认监控
* 引入包后，可以自动对RPC接口增加调用监控，默认是开启的，可以通过一下配置关闭
  * actuator.rpc.dubbo.enhance.enable:false dubbo监控开关
  * actuator.db.mybatis.enhance.enable:false mybatis监控开关
  * actuator.http.enhance.enable:false http监控开关
  * actuator.http.feign.enhance.enable:false feign监控开关
  * actuator.redis.template.enhance.enable:false redis监控开关
  * actuator.bean.definition.remove.enable:false 移除默认jvmThread监控开关
* 监控key的生成规则为
  * dubbo服务端：p_应用名_类名_方法名
  * dubbo客户端：c_应用名_类名_方法名
  * http接口：http_应用名_类名, tag:方法名
  * mybatis：mybatis_mapper_应用名_类名, tag:方法名
  * redis：redis_metrics_应用名, tag:方法名
  * feign:feign_应用名_类名, tag:方法名
* 注意事项
  * 本监控包未将任何maven依赖传递下去，需要自行添加依赖
  * dubbo监控仅测试过2.7.23版本，其他版本可能不兼容
  * 本版本仅支持jdk8及以下版本，jdk17请使用另一个版本

### 手动编码对要监控的点增加监控
````
// 耗时+计次监控
Monitor.TimeContext timeContext = Monitor.timer("key");
try {
    // 业务操作
} catch (Exception e) {
    // 错误率
    timeContext.error();
    log.error("downFromS3 error",e);
} finally {
    timeContext.end();
}
````
````

// 计次监控，不传num默认为1
Monitor.counter("key").end(num);
Monitor.counter("key").end();

````
````
// 非静态方法，需要注入
monitor.gauge("key",Object,ToDoubleFunction)
````
### 注解方式加监控,仅限spring管理的bean
````
// key为class的simpleName+_+methodName
@TimerMonitor
public void test() {
    
}
````