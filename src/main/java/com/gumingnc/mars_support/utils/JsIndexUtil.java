package com.gumingnc.mars_support.utils;

import org.jetbrains.annotations.NotNull;

public class JsIndexUtil {
    public static boolean isJsExtension(String ext) {
        if (ext == null) {
            return false;
        }
        return ext.equals("tsx") || ext.equals("ts") || ext.equals("jsx") || ext.equals("js");
    }

    public static boolean isJsIndex(String baseName) {
        if (baseName == null) {
            return false;
        }
        return baseName.equals("index.tsx") || baseName.equals("index.ts") || baseName.equals("index.jsx") || baseName.equals("index.js");
    }

    @NotNull
    private String path = "";

    public JsIndexUtil(String path) {
        if (path == null) {
            this.path = "";
        } else {
            this.path = path;
        }
    }

    public @NotNull String getBaseName() {
        var index = path.lastIndexOf("/");
        if (index >= 0) {
            return path.substring(index + 1);
        }
        return path;
    }

    public @NotNull String getBaseNameWithoutExt() {
        var baseName = getBaseName();
        var index = baseName.lastIndexOf(".");
        if (index >= 0) {
            return baseName.substring(0, index);
        }
        return baseName;
    }

    public @NotNull String getExtension() {
        var baseName = getBaseName();
        var index = baseName.lastIndexOf(".");
        if (index >= 0) {
            return baseName.substring(index + 1);
        }
        return "";
    }

    public boolean isJsExtension() {
        var ext = getExtension();
        return JsIndexUtil.isJsExtension(ext);
    }

    public @NotNull String removeExtension() {
        var ext = getExtension();
        if (ext.isEmpty()) {
            return path;
        }
        return path.substring(0, path.length() - ext.length() - 1);
    }
}
