package com.gumingnc.mars_support.utils;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    // 解析 index 文件
    public static @Nullable PsiFile resolveIndexFile(PsiDirectory context, String filename) {
        if (context == null) {
            return null;
        }
        if (filename == null) {
            filename = "";
        }
        if (filename.startsWith("./")) {
            filename = filename.substring(2);
        }

        var indexUtil = new JsIndexUtil(filename);
        var parsedPath = indexUtil.parse();
        var dirname = parsedPath.dirname;
        var basename = parsedPath.basename;

        if (!parsedPath.dirname.isEmpty()) {
            var parts = dirname.split("/");
            for (var text : parts) {
                if (context == null) return null;
                if (text.isEmpty()) continue;
                if (text.equals("..")) {
                    context = context.getParent();
                } else {
                    context = context.findSubdirectory(text);
                }
            }
        }

        if (context == null) {
            return null;
        }

        var maybeSubdir = context.findSubdirectory(basename);
        if (maybeSubdir != null) {
            context = maybeSubdir;
            basename = "index";
        }

        // 带完整后缀名
        if (indexUtil.hasJsExtension()) {
            return context.findFile(basename);
        }

        // 省略文件后缀
        for (var item : context.getChildren()) {
            var virtualFile = ((PsiFileSystemItem) item).getVirtualFile();
            if (!virtualFile.isDirectory()) {
                var name = virtualFile.getNameWithoutExtension();
                var ext = virtualFile.getExtension();

                if (basename.equals(name) && JsIndexUtil.isJsExtension(ext)) {
                    return (PsiFile) item;
                }
            }
        }

        return null;
    }

    @NotNull
    private final String path;

    public JsIndexUtil(@NotNull String path) {
        this.path = path.trim();
    }

    public @NotNull ParsedPath parse() {
        var parts = path.split("/");

        var dirname = "";
        var basename = "";
        var basenameWithoutExt = "";
        var extname = "";

        var dirnameBuilder = new StringBuilder();

        for (int i = parts.length - 1; i >= 0; i--) {
            var item = parts[i];
            if (item.isEmpty() || item.equals(".")) {
                continue;
            }
            if (basename.isEmpty()) {
                basename = item;
            } else {
                dirnameBuilder.insert(0, item + "/");
            }
        }

        dirname = dirnameBuilder.toString();
        if (dirname.endsWith("/")) {
            dirname = dirname.substring(0, dirname.length() - 1);
        }

        var index = basename.lastIndexOf(".");
        if (index >= 0) {
            basenameWithoutExt = basename.substring(0, index);
            extname = basename.substring(index + 1);
        } else {
            basenameWithoutExt = basename;
            extname = "";
        }

        final var parsed = new ParsedPath();
        parsed.dirname = dirname;
        parsed.basename = basename;
        parsed.basenameWithoutExt = basenameWithoutExt;
        parsed.extension = extname;

        return parsed;
    }

    public @NotNull String getDirname() {
        return parse().dirname;
    }

    public @NotNull String getBasename() {
        return parse().basename;
    }

    public @NotNull String getBasenameWithoutExt() {
        return parse().basenameWithoutExt;
    }

    public @NotNull String getExtension() {
        return parse().extension;
    }

    public boolean hasJsExtension() {
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

    public static class ParsedPath {
        public @NotNull String dirname = "";
        public @NotNull String basename = "";
        public @NotNull String basenameWithoutExt = "";
        public @NotNull String extension = "";
    }
}
