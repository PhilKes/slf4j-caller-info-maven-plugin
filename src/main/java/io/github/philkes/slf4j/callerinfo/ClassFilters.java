package io.github.philkes.slf4j.callerinfo;

import java.util.*;
import java.util.stream.Collectors;

public class ClassFilters {

    public static final ClassFilters DEFAULT_FILTERS;
    public static final String CLASS_FILE_REGEX_PATTERN = ".*%s\\.class";

    static {
        DEFAULT_FILTERS = new ClassFilters();
        DEFAULT_FILTERS.setIncludes(Collections.singletonList(".*"));
        DEFAULT_FILTERS.setExcludes(Collections.emptyList());
    }

    private List<String> includes;
    private List<String> excludes;

    public ClassFilters() {
        includes = new ArrayList<>();
        excludes = new ArrayList<>();
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public String getIncludeRegex() {
        return includes.isEmpty() ? null :
                String.format(CLASS_FILE_REGEX_PATTERN,
                        includes.stream().collect(Collectors.joining("|", "(", ")")));
    }

    public String getExcludeRegex() {
        return excludes.isEmpty() ? null :
                String.format(CLASS_FILE_REGEX_PATTERN,
                        excludes.stream().collect(Collectors.joining("|", "(", ")")));
    }

    @Override
    public String toString() {
        return String.format("ClassFilters{ includes='%s', excludes='%s'}", getIncludeRegex(), getExcludeRegex());
    }
}
