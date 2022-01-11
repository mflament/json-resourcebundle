package com.infine.test.springi18n;

import java.util.ListResourceBundle;
import java.util.Objects;

public class JSONResourceBundle extends ListResourceBundle {

    private final Object[][] contents;
    private final long lastModified;

    public JSONResourceBundle(Object[][] contents, long lastModified) {
        this.contents = Objects.requireNonNull(contents, "contents is null");
        this.lastModified = lastModified;
    }

    public long getLastModified() {
        return lastModified;
    }

    @Override
    protected Object[][] getContents() {
        return contents;
    }


}
