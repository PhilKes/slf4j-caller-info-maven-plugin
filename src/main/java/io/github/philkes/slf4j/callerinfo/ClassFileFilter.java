package io.github.philkes.slf4j.callerinfo;

import org.apache.commons.io.filefilter.AbstractFileFilter;

import java.io.File;

public class ClassFileFilter extends AbstractFileFilter {

    private final ClassFilters filters;

    public ClassFileFilter(ClassFilters filters) {
        this.filters = filters;
    }

    @Override
    public boolean accept(File file) {
        String included =  filters.getIncludeRegex();
        String excluded = filters.getExcludeRegex();
        String fileStr = file.toString();
        return included != null && fileStr.matches(included) && (excluded == null || !fileStr.matches(excluded));
    }

    @Override
    public String toString() {
        return "ClassFileFilter{" +
                "filters=" + filters +
                '}';
    }

}


