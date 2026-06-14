# JConomy API

The public expansion API for [JConomy](../README.md). Use this to extend JConomy from a separate plugin jar — without modifying or forking JConomy itself.

Full API documentation is available as Javadoc.

## What you can do with the API

- Register custom services (storage backends, formatters, repositories) via dependency injection
- Receive a callback once all services are ready via `onServicesReady()`

## Getting started

Add `jconomy-api` as a Maven dependency (scope `provided`):

```xml
<dependency>
    <groupId>com.jellyrekt.jconomy</groupId>
    <artifactId>jconomy-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Create a class that implements `JConomyExpansion` (or extends `AbstractJConomyExpansion` for a no-op default):

```java
public class MyExpansion extends AbstractJConomyExpansion {

    @Override
    public String getName() {
        return "my-expansion";
    }

    @Override
    public void configureServices(JConomyServiceBuilder builder) {
        // Register your services here
        builder.addSingleton(AccountRepository.class, MyAccountRepository.class);
        builder.addSingleton(AccountNameRepository.class, MyAccountNameRepository.class);
    }
}
```

Package the class in a jar and drop it in `plugins/JConomy/modules/`. JConomy will load it automatically on startup.
