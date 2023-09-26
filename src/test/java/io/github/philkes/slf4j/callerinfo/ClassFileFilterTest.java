package io.github.philkes.slf4j.callerinfo;


import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ClassFileFilterTest {

    private static ClassFilters classFilters;
    private static ClassFileFilter classFileFilter;

    @Before
    public void setup() {
        classFilters = new ClassFilters();
        classFileFilter = new ClassFileFilter(classFilters);
    }

    @Test
    public void testIncludeAndExclude() {
        classFilters.setIncludes(Arrays.asList("Excluded", "Included", "Included2"));
        classFilters.setExcludes(Arrays.asList("Test", "Excluded"));
        assertTrue(classFileFilter.accept(new File("some/path/Included.class")));
        assertTrue(classFileFilter.accept(new File("some/path/Included2.class")));
        assertFalse(classFileFilter.accept(new File("some/path/Excluded.class")));
        assertFalse(classFileFilter.accept(new File("some/path/Test.class")));
        assertFalse(classFileFilter.accept(new File("some/path/Some/Random/Class.class")));
    }

    @Test
    public void testIncludeAndExcludeEmpty() {
        classFilters.setIncludes(Collections.emptyList());
        classFilters.setExcludes(Collections.emptyList());
        assertFalse(classFileFilter.accept(new File("some/path/Included.class")));
        assertFalse(classFileFilter.accept(new File("some/path/Some/Random/Class.class")));
    }

    @Test
    public void testExcludeEmpty() {
        classFilters.setIncludes(Arrays.asList("Included", "Included2"));
        classFilters.setExcludes(Collections.emptyList());
        assertTrue(classFileFilter.accept(new File("some/path/Included.class")));
        assertTrue(classFileFilter.accept(new File("some/path/Included2.class")));
        assertFalse(classFileFilter.accept(new File("some/path/Some/Random/Class.class")));
    }

    @Test
    public void testDefaultFilters() {
        classFileFilter = new ClassFileFilter(ClassFilters.DEFAULT_FILTERS);
        assertTrue(classFileFilter.accept(new File("some/path/Included.class")));
        assertTrue(classFileFilter.accept(new File("some/path/Excluded.class")));
        assertTrue(classFileFilter.accept(new File("some/path/Some/Random/Class.class")));
    }
}
