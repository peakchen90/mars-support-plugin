package com.gumingnc.mars_support.utils;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KoneConfigUtil {
    public final static String FileName = "kone.config.json";

    public static boolean isResolveMarsConfig = false;

    public static @Nullable MarsConfig marsConfig;

    public static @Nullable PsiFile resolveConfigFile(@Nullable PsiElement element) {
        var root = FsUtil.findRoot(element);
        if (root == null) {
            return null;
        }

        return root.findFile(FileName);
    }

    public static @Nullable MarsConfig resolveMarsConfig(@Nullable PsiElement element) {
        isResolveMarsConfig = true;

        var file = resolveConfigFile(element);

        if (file != null) {
            var obj = PsiTreeUtil.findChildOfType(file, JsonObject.class, true);
            if (obj != null) {
                var marsProperty = obj.findProperty("mars");
                if (marsProperty != null) {
                    marsConfig = new MarsConfig(marsProperty, null, null);
                    var valueExpr = marsProperty.getLastChild();
                    if (valueExpr instanceof JsonObject) {
                        var appIdProperty = ((JsonObject) valueExpr).findProperty("appId");
                        if (appIdProperty != null) {
                            marsConfig.appId = appIdProperty.getLastChild();
                        }

                        var typeProperty = ((JsonObject) valueExpr).findProperty("type");
                        if (typeProperty != null) {
                            marsConfig.type = typeProperty.getLastChild();
                        }

                    }
                }
            }
        }

        return marsConfig;
    }

    public static class MarsConfig {
        public @NotNull JsonProperty mars;
        public @Nullable PsiElement appId;
        public @Nullable PsiElement type;

        public MarsConfig(@NotNull JsonProperty mars, @Nullable PsiElement appId, @Nullable PsiElement type) {
            this.mars = mars;
            this.appId = appId;
            this.type = type;
        }

        public @NotNull String getAppId() {
            if (appId instanceof JsonStringLiteral) {
                return ((JsonStringLiteral) appId).getValue();
            }
            return "";
        }

        public @NotNull String getValidAppId() {
            var _appId = getAppId();
            if (_appId.matches("^[a-z0-9]+(-[a-z0-9]+)*$")) {
                return _appId;
            }
            return "";
        }

        public @NotNull String getType() {
            if (type instanceof JsonStringLiteral) {
                return ((JsonStringLiteral) type).getValue();
            }
            return "";
        }

        public @NotNull String getValidType() {
            var _type = getType();
            if (_type.equals("app") || _type.equals("main") || _type.equals("static")) {
                return _type;
            }
            return "";
        }
    }
}
