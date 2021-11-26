# Getting Started

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
deliver.pulsar.scheme=pulsar
deliver.pulsar.host=127.0.0.1
deliver.pulsar.port=6650
```
define PulsarClient
```java
@Resource
private PulsarClient client;
```
use Producer
```java
// define producer
@Resource
private IProducer<TEntry> producer;

// init producer
producer.init(TEntry.class, "topic", "producer");

// send message
producer.send(new TEntry());

// send delay
producer.sendAfter(new TEntry(), 60, TimeUnit.SECONDS);

// send a time
producer.sendAt(new TEntry(), Instant.parse("2021-11-03T08:36:00.000Z").toEpochMilli());
```
use Consumer
```java
// define consumer
@Resource
IConsumer<TEntry> consumer;

// register receive process
consumer.reg(entry -> {
    System.out.printf("receive message, time:" + new Date() + " entry:" + EntryUtils.toString(entry) + "%n");
    return true;
});

// start long-term receive
consumer.start(TEntry.class, ConsumeMode.Longtime, SubscriptionType.Shared, 1, "topic", "subscribe");

// start scheduled receive
consumer.start(TEntry.class, ConsumeMode.Longtime, SubscriptionType.Scheduled, 1, "topic", "subscribe");
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