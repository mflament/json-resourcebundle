package com.infine.test.springi18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JSONResourceBundleControlTest {

    @BeforeEach
    void resetBundles() {
        ResourceBundle.clearCache();
    }

    @Test
    void testFromClasspath() {
        test("a.b.TestBundle");
        test("classpath:a/b/TestBundle");
    }

    @Test
    void testFromFile() {
        test("file:src/test/resources/a/b/TestBundle");
    }

    @Test
    void testStringify() {
        JSONResourceBundleControl control = createControl(true);
        ResourceBundle bundle = ResourceBundle.getBundle("a.b.TestBundle", Locale.FRANCE, control);
        bundle = ResourceBundle.getBundle("a.b.TestBundle", control);
        assertEquals("true", bundle.getObject("bool"));
        assertEquals("3.14", bundle.getObject("decimal"));
        assertEquals("42", bundle.getObject("int"));
    }

    private void test(String bundleName) {
        JSONResourceBundleControl control = createControl(false);
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, Locale.FRANCE, control);
        assertEquals("La valeur de la clé 1 lue du classpath", bundle.getString("key1"));
        assertEquals("The value of key 2 from classpath", bundle.getString("key2"));
        assertEquals("La valeur de la clé 1 de l'object", bundle.getString("object.key1"));
        assertEquals("The value of key 2 from object", bundle.getString("object.key2"));

        bundle = ResourceBundle.getBundle("a.b.TestBundle", Locale.GERMAN, control);
        assertEquals("The value of key 1 from classpath", bundle.getString("key1"));
        assertEquals("The value of key 2 from classpath", bundle.getString("key2"));

        bundle = ResourceBundle.getBundle("a.b.TestBundle", control);
        assertEquals("The value of key 1 from object", bundle.getString("object.key1"));
        assertEquals("The value of key 2 from object", bundle.getString("object.key2"));
        assertEquals("The first value of array", bundle.getString("array.0"));
        assertEquals("The second value of array", bundle.getString("array.1"));

        assertEquals(true, bundle.getObject("bool"));
        assertEquals(3.14, bundle.getObject("decimal"));
        assertEquals(42, bundle.getObject("int"));
    }

    private JSONResourceBundleControl createControl(boolean stringify) {
        Locale.setDefault(Locale.US);
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        ObjectMapper objectMapper = new ObjectMapper();
        return new JSONResourceBundleControl(resourceLoader, objectMapper, 10, stringify);
    }

}