# JConomy API

The public extension API for [JConomy](../README.md). Use this to extend JConomy from a separate plugin jar — without modifying or forking JConomy itself.

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

Create a class that implements `JConomyExtension`:

```java
public class MyExtension implements JConomyExtension {

    @Override
    public String getName() {
        return "my-extension";
    }

    @Override
    public void configureServices(JConomyServiceBuilder builder) {
        // Register your services here
        builder.addSingleton(AccountRepository.class, MyAccountRepository.class);
        builder.addSingleton(AccountNameRepository.class, MyAccountNameRepository.class);
    }
}
```

Package the class in a jar and drop it in `plugins/JConomy/extensions/`. JConomy will load it automatically on startup.

### Register your extension entry point

JConomy discovers extensions using Java ServiceLoader. Your jar must include this descriptor file:

`META-INF/services/org.jconomy.JConomyExtension`

The file content should list one implementation class per line:

```text
com.example.MyExtension
com.example.AnotherExtension
```

You can register multiple `JConomyExtension` implementations in one jar.
JConomy also supports loading from any number of jars in `plugins/JConomy/extensions/`.

## Snapshots

> [!WARNING]
> Snapshot builds are **unstable** and may change or break at any time. Do not use them in production.

To resolve snapshot versions, add the Central snapshot repository to your project:

```xml
<repositories>
    <repository>
        <id>central-snapshots</id>
        <url>https://central.sonatype.com/repository/maven-snapshots/</url>
        <snapshots><enabled>true</enabled></snapshots>
        <releases><enabled>false</enabled></releases>
    </repository>
</repositories>
```
