# slfj4-caller-info-plugin
Maven plugin to inject caller-info (Method name + Source code line number) to all [SLF4J Logger](https://www.slf4j.org/api/org/slf4j/Logger.html) log statement invocations (info, warn, error, debug, trace).

This is done by injecting `MDC.put(...)` calls before every SLF4J log invocation, putting the method name and source code line number hardcoded into the compiled `.class` files. This allows to conveniently print out where exactly the log statement originates from for every single log statement, without any overhead or performance loss, by simply adding the MDC parameter `callerMethod` and `callerLine` to your logging-pattern configuration. It can therefore be used with any SLF4J implementation, such as `logback`, `log4j2`, etc.

Since this plugins executes before runtime, there is no additional cost to calculate the current method + line number like when using the [%method](https://logback.qos.ch/manual/layouts.html#method) or [%line](https://logback.qos.ch/manual/layouts.html#line) parameter in your logging pattern.

## Usage
Add the plugin to your `pom.xml`:
```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.philkes</groupId>
            <artifactId>slf4j-caller-info-maven-plugin</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <configuration>
            <!-- Optional, the shown default values are used if not present--> 
                <methodMdcParameter>callerMethod</methodMdcParameter>
                <lineMdcParameter>callerLine</lineMdcParameter>
                <!-- Whether or not to inject method and/or source code line number -->
                <injectMethod>true</injectMethod>
                <injectLineNumber>true</injectLineNumber>
                <!-- Regex for specifying which packages/classfiles should be injected into -->
                <filterClasses>.*</filterClasses>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>inject-caller-info</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

```
The plugin is executed on a `mvn clean install` after the ` maven-compiler-plugin` or can be explicitly run with:
```shell
mvn slf4j-caller-info:inject
```


## Dependencies
- [ASM](https://asm.ow2.io/) for Java bytecode manipulation
- [Apache Commons IO](https://commons.apache.org/proper/commons-io/) for FileUtils
- Built with Java 17


# TODOs 
- Runtime speed comparison to [Logging Pattern Caller Information](https://logging.apache.org/log4j/2.x/performance.html) 
- 

This project is licensed under the terms of the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.txt).