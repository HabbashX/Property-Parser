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

# 🧠 Code Enhancements & Improvements

## ⚡ Performance Optimizations
- Introduced `ReflectionCache` to cache declared fields per class.
- Reduced repeated reflection calls significantly.
- Improved injection speed by avoiding redundant metadata lookups.

---

## 🏗 Architecture Improvements
- Refactored system into **registry-based design**:
    - `PropertyConverterRegistry`
    - `PropertyDecryptorRegistry`
    - `ResolverRegistry`
- Clear separation of concerns between:
    - parsing
    - conversion
    - decryption
    - resolution

---

## 🔁 Converter System Upgrade
- Redesigned converter system to support:
    - generic `PropertyConverter<T>`
    - runtime-safe conversion with target type awareness
- Added automatic fallback instantiation for missing converters
- Improved type safety in conversion pipeline

---

## 🔐 Decryptor System Improvements
- Improved decryptor handling with centralized registry.
- Auto-instantiation support when decryptor is not registered.
- Cleaner annotation-driven flow using `@DecryptWith`.

---

## 🔄 Parser System Enhancements
- Improved `ParserFactory` fallback chain:
    - primitives + wrappers
    - enums
    - constructors with `String`
    - object fallback parsing
- Better handling of unsupported types with controlled exception flow.

---

## 🧩 Resolver System Upgrade
- Introduced `ResolverRegistry` for multi-resolver support.
- Supports chained resolution of values before injection.
- More flexible preprocessing pipeline for property values.

---

## 🧬 Design Improvements
- Moved from tightly-coupled injection logic → modular pipeline design.
- Each feature now operates independently via registry injection.
- Cleaner extensibility without modifying core injector logic.

---

## 🚀 Overall Improvements
- Reduced reflection overhead
- Improved modularity and maintainability
- Better separation between parsing, conversion, and resolution
- More extensible design for future plugins and extensions
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

# Version
1.1.2
