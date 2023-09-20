# slfj4-caller-info-maven-plugin-benchmark
This maven project is for benchmarking Log4j2 and Logback performance of `slf4j-caller-info-maven-plugin` in comparison
to using the caller location information in your logging pattern. The benchmarks are implemented with [Java Microbenchmark
 Harness](https://github.com/openjdk/jmh). All benchmarks are based on the [log4j-perf](https://github.com/apache/logging-log4j2)
 module to be able to compare the here shown results with [Log4j Performance](https://logging.apache.org/log4j/2.x/performance.html).

## Results

### Runtime Performance

<img src="./results/results.png" width="500">

We can see a massive performance gain in comparison to using logback/log4j2 caller location pattern. Logging the caller location
 with this plugin speeds asynchronous logging by ~4 times. The actual slow down of logging
 the caller location with this plugin in comparison to not logging the caller at all is only ~8% (logback) and ~9% (log4j2).
For more detailed JMH results analysis, you can view the `.json` result files (see `results/`) online with the [JMH Visualizer](https://jmh.morethan.io/).  

### Compilation Performance
As for the time it takes the `inject` goal to execute, the compilation time of the plugin is evaluated by generating Java projects with n classes, that all contain 5 SLF4J log-statements and running the plugin on these projects and averaging the plugin's execution time.

<img src="./results/results-compiletime.png" width="500">

We can see that the plugin's execution time obviously goes up the more classes and `SLF4J` log statements there are in the source code,
but even for a project with 10,000 classes the compilation time is only ~2 seconds.

## Usage
Build the `benchmarks.jar` file for log4j, logback, compiletime modules:
```shell
mvn clean install
```
To run log4j2, logback, compiletime benchmarks:
```shell
java -jar log4j2/target/benchmarks.jar -rf json -rff results/jmh-results-log4j2.json ; \
java -jar logback/target/benchmarks.jar -rf json -rff results/jmh-results-logback.json ; \
java -jar compiletime/target/benchmarks.jar -rf json -rff results/jmh-results-compiletime.json
```

This project is licensed under the terms of the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.txt).