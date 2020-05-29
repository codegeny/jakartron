# Jakartron

Jakartron is a small library built on top of CDI-SE and heavily inspired by CDI-Unit.
Its primary use is for testing applications targeting the Jakarta EE platform.

This library could be used to launch standalone applications à la Spring-Boot but the
maturity is not there yet as many shortcuts have been made that are good enough for
testing but certainly not for production-grade applications.

Jakartron is split in multiple modules which represent sub-specifications of the Jakarta EE specification.

In your `pom.xml`, import the module you need in your application:
```
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.codegeny.jakartron</groupId>
            <artifactId>jakartron-bom</artifactId>
            <version>1.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>jakarta.platform</groupId>
        <artifactId>jakarta.jakartaee-api</artifactId>
        <version>8.0.0</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>org.codegeny.jakartron</groupId>
        <artifactId>jakartron-jpa</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.codegeny.jakartron</groupId>
        <artifactId>jakartron-junit</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```
In your test:
```
@EnableCDI
@DataSourceDefinition(name = "mydb", className = "org.h2.jdbcx.JdbcDataSource", minPoolSize = 5, maxPoolSize = 25, url = "jdbc:h2:mem:mydb")
@PersistenceUnitDefinition(unitName = "tests", nonJtaDataSourceName = "mydb", transactionType = RESOURCE_LOCAL, managedClasses = JPADBTest.President.class, properties = {
        @Property(name = "javax.persistence.schema-generation.database.action", value = "create")
})
public class JPATest {

    @Entity(name = "President")
    @NamedQuery(name = "countPresidents", query = "select count(p) from President p")
    public static class President {

        @Id
        @GeneratedValue
        private Long id;

        President() {}

        public President(String name) {
            this.name = name;
        }

        private String name;

        public String getName() {
            return name;
        }
    }

    @PersistenceContext(unitName = "tests", type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;

    @Test
    public void test() {
        entityManager.getTransaction().begin();
        entityManager.persist(new President("G. Washington"));
        entityManager.persist(new President("A. Lincoln"));
        entityManager.getTransaction().commit();

        entityManager.clear();

        entityManager.getTransaction().begin();
        Assertions.assertEquals(2, entityManager.createNamedQuery("countPresidents", Number.class).getSingleResult().intValue());
        entityManager.getTransaction().rollback();
    }
}
```

## Annotations

Jakartron annotations must be put on your bootstrap class (or test class) and follow closely the semantics of the initializing methods present on the `SeContainerInitialize` class from CDI-SE.

`@AdditionalClasses` allows adding beans/interceptors/decorators/extensions classes that are not automatically discovered (either because `bean-discovery-mode="none"` is used or `@DisableDiscovery` is present).

`@AdditionalPackages` allows adding whole package (recursively or not).

`@EnabledAlternatives` allows enabling CDI alternatives.

`@DisableDiscovery` allows disabling the whole discovery process.

### Meta-annotations

All Jakartron annotations can be used as meta-annotations to group common features.

Instead of doing this:
```
@AdditionalClasses({Foo.class, Bar.class, FooBar.class})
@EnabledAlternatives(Baz.class)
public class MyFirstTest {}

@AdditionalClasses({Foo.class, Bar.class, FooBar.class})
@EnabledAlternatives(Baz.class)
public class MySecondTest {}
```
Prefer creating a meta-annotation like this:
```
@AdditionalClasses({Foo.class, Bar.class, FooBar.class})
@EnabledAlternatives(Baz.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableFoo {}

@EnableFoo
public class MyFirstTest {}

@EnableFoo
public class MySecondTest {}
```
Jakartron itselfs uses that feature for its `@EnableJPA|JTA|JMS|EJB|...` annotations.


## Auto-discovery

Jakartron modules are CDI extensions which are automatically discovered at runtime.

If your tests contains CDI alternatives or other beans, they may conflict or create ambiguity with the CDI beans present in your application.
Therefore, it is recommended (unless you know what you do) to disable auto-discovery in your test classpath by setting `bean-discovery-mode="none"` in your test `META-INF/beans.xml`:

```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0" bean-discovery-mode="none"
       xmlns="http://xmlns.jcp.org/xml/ns/javaee"
       xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/beans_2_0.xsd"/>
```

Putting this file in your test resources won't prevent CDI beans from being discovered in your main java folder.

It is also possible to **completely** disable auto-discovery by annotating your test class with `@DisableDiscovery`.
If you do that, you will need to manually add each bean with `@AdditionalClasses`.

## Modules

### Core

The core module contains the Jakartron annotations and the logic to initialize CDI by scanning those annotations on the given classes.

This module depends on Weld.

### JUnit 5

Use `@EnableCDI` on your test class (or a meta-annotation) to register the Jakartron JUnit 5 extension.

This extension will make sure your test class annotations are scanned and added to the CDI application definition.
This extension allows parameter injection in your JUnit test methods.

CDI interceptors can also be applied to test methods.

This module depends on JUnit 5.

### JTA

TODO

This module depends on Bitronix JTA Transaction Manager.

### JPA

TODO

This module depends on Hibernate ORM.

### JMS

TODO

### EJB

TODO

### Servlet

TODO

This module depends on Jetty.

### JAX-RS

TODO

This module depends on Resteasy.

### Validation

TODO

This module depends on Hibernate Validator.