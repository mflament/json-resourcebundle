package com.infine.test.springi18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class JSONResourceBundleControl extends ResourceBundle.Control {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONResourceBundleControl.class);

    private static final long DEFAULT_TTL = 1000;

    private static final String FORMAT = "json";

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final long ttl;
    private final boolean stringify;

    public JSONResourceBundleControl(ResourceLoader resourceLoader) {
        this(resourceLoader, new ObjectMapper(), DEFAULT_TTL, false);
    }

    public JSONResourceBundleControl(ResourceLoader resourceLoader, ObjectMapper objectMapper, long ttl, boolean stringify) {
        this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader is null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is null");
        this.ttl = ttl;
        this.stringify = stringify;
    }

    @Override
    public List<String> getFormats(String baseName) {
        Objects.requireNonNull(baseName, "baseName is null");
        List<String> res = new ArrayList<>();
        res.add(FORMAT);
        return res;
    }

    @Override
    public long getTimeToLive(String baseName, Locale locale) {
        return ttl;
    }

    @Override
    public boolean needsReload(String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime) {
        LOGGER.debug("needsReload({})", baseName);
        Locale.setDefault(Locale.ENGLISH);
        if (bundle instanceof JSONResourceBundle) {
            JSONResourceBundle jsonResourceBundle = (JSONResourceBundle) bundle;
            Resource resource = findResource(baseName, locale, format);
            if (resource == null)
                return true;
            try {
                return resource.lastModified() != jsonResourceBundle.getLastModified();
            } catch (IOException e) {
                // let the error be reported by next call to newBundle
                return true;
            }
        }
        return false;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IOException {
        Objects.requireNonNull(baseName, "baseName is null");
        if (!Objects.equals(format, FORMAT))
            throw new IllegalArgumentException("Unsupported format " + format);

        Resource resource = findResource(baseName, locale, format);
        if (resource == null)
            return null;

        LOGGER.debug("Loading bundle from {}", resource);
        return createBundle(resource);
    }

    private Resource findResource(String baseName, Locale locale, String format) {
        Resource resource;
        String resourceName;
        String protocol = getProtocol(baseName);
        if (protocol != null) {
            String bundleName = toBundleName(baseName.substring(protocol.length() + 1), locale);
            resourceName = protocol + ":" + bundleName + ".json";
        } else {
            String bundleName = toBundleName(baseName, locale);
            resourceName = toResourceName(bundleName, format);
        }
        resource = resourceLoader.getResource(resourceName);
        if (resource.exists())
            return resource;
        return null;
    }

    private String getProtocol(String baseName) {
        int index = baseName.indexOf(':');
        if (index > 0)
            return baseName.substring(0, index);
        return null;
    }

    private JSONResourceBundle createBundle(Resource resource) throws IOException {
        try (InputStream is = resource.getInputStream()) {
            JsonNode jsonNode = objectMapper.readTree(is);
            List<Object[]> pairs = new LinkedList<>();
            flatten(null, jsonNode, pairs);
            return new JSONResourceBundle(pairs.toArray(new Object[pairs.size()][]), resource.lastModified());
        }
    }

    private void flatten(String name, JsonNode jsonNode, List<Object[]> pairs) {
        if (jsonNode.isContainerNode()) {
            final String prefix = name != null ? name + "." : "";
            if (jsonNode instanceof ObjectNode) {
                ObjectNode objectNode = (ObjectNode) jsonNode;
                objectNode.fieldNames().forEachRemaining(n -> flatten(prefix + n, objectNode.get(n), pairs));
            } else if (jsonNode instanceof ArrayNode) {
                ArrayNode arrayNode = (ArrayNode) jsonNode;
                for (int i = 0; i < arrayNode.size(); i++) {
                    flatten(prefix + i, arrayNode.get(i), pairs);
                }
            } else {
                LOGGER.warn("Unhandled i18 JSON container node " + jsonNode.getClass().getName() + " ( " + jsonNode.getNodeType() + ")");
            }
        } else if (stringify || jsonNode instanceof TextNode) {
            pairs.add(new Object[]{name, jsonNode.asText()});
        } else if (jsonNode instanceof NumericNode) {
            pairs.add(new Object[]{name, jsonNode.numberValue()});
        } else if (jsonNode instanceof BooleanNode) {
            pairs.add(new Object[]{name, jsonNode.booleanValue()});
        } else {
            LOGGER.warn("Unhandled i18 JSON node " + jsonNode.getClass().getName() + " ( " + jsonNode.getNodeType() + ")");
        }
    }
}
