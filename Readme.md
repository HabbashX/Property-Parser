# 🛠️ Property Parser 1.1

A flexible and powerful Java library to load, validate, and manage application properties with advanced features like type conversion, validation rules, dynamic file references, and more.

---

## ✨ Features

- 🔍 Read/write `.properties` files
- 🔧 Type conversion with generic support
- ✅ Inline lambda-based validation rules
- 🧩 Easy integration with other Java apps
- 🔄 Support for referencing properties from external files (e.g., `file.properties:some.key`)
---

---
## ✨New Features
- 🧩 Property Parser now supports BigDecimal and BigInteger
- 🧩 now you can inject an instance of any serialized object into target field just by define the file name that the serialized object been stored in. (example.properties = object.dat)

---

## 📦 Installation
```
  <dependency>
     <groupId>com.habbashx</groupId>
     <artifactId>property-parser</artifactId>
     <version>1.0</version>
   </dependency>
```

## Property Injector Usage:

```java
import com.habbashx.annotation.DecryptWith;
import com.habbashx.annotation.DefaultValue;
import com.habbashx.annotation.InjectList;
import com.habbashx.annotation.InjectPrefix;
import com.habbashx.annotation.InjectProperty;
import com.habbashx.annotation.Required;
import com.habbashx.annotation.UseConverter;
import com.habbashx.injector.PropertyInjector;

import java.io.File;
import java.time.LocalDate;

public class Example {

    @InjectProperty("settings.database.url")
    @Required
    private String databaseURL;

    @InjectProperty("settings.database.user")
    private String user;

    @InjectProperty("settings.database.password")
    @DecryptWith(PasswordDecryptor.class)
    @Required
    private String password;

    @InjectList("settings.users")
    private List<String> usersList;

    @InjectProperty("settings.database.localDate")
    @UseConverter(LocalDateConverter.class)
    private LocalDate localDate;

    @InjectPrefix("settings.database")
    private PrefixExample prefixExample;

    public static void main(String[] args) {
        PropertyInjector propertyInjector = new PropertyInjector(new File("settings.properties"));
        Example exampleInstance = new Example();

        propertyInjector.inject(exampleInstance);
    }
}

class PrefixExample {
    @InjectProperty("url")
    @Required
    private String databaseURL;

    @InjectProperty("user")
    private String user;

    @InjectProperty("password")
    @DecryptWith(PasswordDecryptor.class)
    @Required
    private String password;

    @InjectProperty("localDate")
    @UseConverter(LocalDateConverter.class)
    private LocalDate localDate;
    
    @InjectList("users")
    private List<String> usersList;


}
```
