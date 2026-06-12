# JConomy API

The public expansion API for [JConomy](../README.md). Use this to extend JConomy from a separate plugin jar — without modifying or forking JConomy itself.

Full API documentation is available as Javadoc.

## What you can do with the API

- Register custom services (storage backends, formatters, repositories) via dependency injection
- Provide a `DataImporter` to migrate balances from another economy plugin on first startup
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
        builder.addSingleton(DataImporter.class, MyImporter.class);
    }
}
```

Package the class in a jar and drop it in `plugins/JConomy/modules/`. JConomy will load it automatically on startup.

## Importer note

`DataImporter.importData()` is called once per startup, but JConomy tracks completion in `config.yml` under `is-importer-completed.<id>` and skips importers that have already run. Your importer's `getId()` return value is used as the key. Set the value back to `false` in config to re-run.
