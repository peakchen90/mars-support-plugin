package com.gumingnc.mars_support.utils;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class RoutesUtil {
    private static @Nullable RoutesUtil instance;

    public static @NotNull RoutesUtil getInstance(@Nullable PsiElement element) {
        if (instance == null) {
            instance = collectRouteMap(element);
        }
        return instance;
    }

    public static class RouteInfo {
        public @NotNull JsonStringLiteral componentDeclaration;
        public @NotNull PsiFile componentFile;
        public @NotNull String path = "";

        public RouteInfo(@NotNull JsonStringLiteral componentDeclaration, @NotNull PsiFile componentFile) {
            this.componentFile = componentFile;
            this.componentDeclaration = componentDeclaration;

            var property = componentDeclaration.getParent();
            if (property instanceof JsonProperty) {
                var obj = property.getParent();
                if (obj instanceof JsonObject) {
                    var pathProperty = ((JsonObject) obj).findProperty("path");
                    if (pathProperty != null) {
                        var valueExpr = pathProperty.getLastChild();
                        if (valueExpr instanceof JsonStringLiteral) {
                            this.path = ((JsonStringLiteral) valueExpr).getValue();
                        }
                    }
                }
            }
        }
    }

    // 收集 routes
    public static RoutesUtil collectRouteMap(@Nullable PsiElement element) {
        var result = new RoutesUtil();
        if (element == null) {
            return result;
        }

        final var projectPath = element.getProject().getBasePath();
        PsiDirectory root = null;

        try {
            var current = element.getContainingFile().getParent();
            while (current != null) {
                if (current.getVirtualFile().getPath().equals(projectPath)) {
                    root = current;
                    break;
                }
                current = current.getParent();
            }
        } catch (Exception ignored) {
        }

        if (root == null) {
            return result;
        }

        var srcDir = root.findSubdirectory("src");
        if (srcDir == null) {
            return result;
        }
        var file = srcDir.findFile("app.json");
        if (file == null) {
            return result;
        }

        var obj = PsiTreeUtil.findChildOfType(file, JsonObject.class, true);
        if (obj != null) {
            var routes = obj.findProperty("routes");
            if (routes != null) {
                var routesExpr = routes.getLastChild();
                if (routesExpr instanceof JsonArray) {
                    for (var item : routesExpr.getChildren()) {
                        if (item instanceof JsonObject) {
                            var component = ((JsonObject) item).findProperty("component");
                            if (component != null) {
                                var componentValue = component.getLastChild();
                                if (componentValue instanceof JsonStringLiteral) {
                                    var targetFile = FsUtil.resolveIndexFile(srcDir, ((JsonStringLiteral) componentValue).getValue());
                                    if (targetFile != null) {
                                        result.set(targetFile, (JsonStringLiteral) componentValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        return result;
    }

    private final @NotNull HashMap<PsiFile, RouteInfo> map;

    public RoutesUtil() {
        this.map = new HashMap<>();
    }

    public void reset() {
        map.clear();
    }

    public void set(@NotNull PsiFile componentFile, @NotNull JsonStringLiteral componentLiteral) {
        map.put(componentFile, new RouteInfo(componentLiteral, componentFile));
    }

    public @Nullable RouteInfo get(PsiFile componentFile) {
        if (componentFile != null) {
            return map.get(componentFile);
        }
        return null;
    }
}
