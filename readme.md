# Getting Started

### Reference Documentation

For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.5.4/maven-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.5.4/maven-plugin/reference/html/#build-image)

### Use Pulsar
maven reference
```xml
<dependency>
    <groupId>com.touscm</groupId>
    <artifactId>deliver-pulsar-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
config properties
```properties
deliver.pulsar.url=pulsar://127.0.0.1:6650
```
or
```properties
deliver.pulsar.host=127.0.0.1
deliver.pulsar.port=6650
```
define PulsarClient
```java
@Resource
private PulsarClient client;
```

### Use Access Deliver
maven reference
```xml
<dependency>
    <groupId>com.touscm</groupId>
    <artifactId>deliver-access</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
add Spring Boot scan path
```java
@SpringBootApplication(scanBasePackages = {"com.touscm.deliver"})
```
config properties
```properties
deliver.pulsar.host=127.0.0.1
deliver.pulsar.port=6650
deliver.pulsar.access-topic=test-service-access
deliver.pulsar.access-subscribe=test-service-subscribe
```
define IAccessDeliver
```java
public class Test{
    @Autowired
    private IAccessDeliver accessDeliver;

    public Object saveAccess(@Autowired HttpServletRequest request) {
        String method = request.getMethod(), ip = request.getRemoteAddr();
        String url = request.getRequestURI(), userAgent = request.getHeader("User-Agent");

        AccessEntry entry = new AccessEntry()
                .setAccessName("test").setAccessTime(new Date()).setAccessType("type")
                .setMethod(method).setAccessUrl(url).setIp(ip).setUserAgent(userAgent);
        return accessDeliver.process(entry);
    }
}
```