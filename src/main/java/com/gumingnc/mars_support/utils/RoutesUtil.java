package com.gumingnc.mars_support.utils;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
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
        public @NotNull PsiFile componentFile;
        public @NotNull JsonStringLiteral componentDeclaration;
        public @Nullable JsonStringLiteral pathDeclaration;
        public @Nullable JsonStringLiteral descriptionDeclaration;

        public static boolean validatePath(String path) {
            if (path == null) return false;
            return path.matches("^(/[-\\w]+)+$");
        }

        public RouteInfo(@NotNull JsonStringLiteral componentDeclaration, @NotNull PsiFile componentFile) {
            this.componentFile = componentFile;
            this.componentDeclaration = componentDeclaration;

            var property = componentDeclaration.getParent();
            if (property instanceof JsonProperty) {
                var obj = property.getParent();
                if (obj instanceof JsonObject) {
                    // find path property
                    var pathProperty = ((JsonObject) obj).findProperty("path");
                    if (pathProperty != null) {
                        var valueExpr = pathProperty.getLastChild();
                        if (valueExpr instanceof JsonStringLiteral) {
                            this.pathDeclaration = (JsonStringLiteral) valueExpr;
                        }
                    }

                    // find description property
                    var descriptionProperty = ((JsonObject) obj).findProperty("description");
                    if (descriptionProperty != null) {
                        var valueExpr = descriptionProperty.getLastChild();
                        if (valueExpr instanceof JsonStringLiteral) {
                            this.descriptionDeclaration = (JsonStringLiteral) valueExpr;
                        }
                    }
                }
            }
        }

        public @NotNull String getFilePath() {
            return componentFile.getVirtualFile().getPath();
        }

        public @NotNull String getPath() {
            if (pathDeclaration != null) {
                return pathDeclaration.getValue();
            }
            return "";
        }

        public @NotNull String getValidPath() {
            var _path = getPath();
            if (validatePath(_path)) {
                return _path;
            }
            return "";
        }

        public @NotNull String getDescription() {
            if (descriptionDeclaration != null) {
                return descriptionDeclaration.getValue();
            }
            return "";
        }
    }

    // 收集 routes
    public static RoutesUtil collectRouteMap(@Nullable PsiElement element) {
        var result = new RoutesUtil();
        var root = FsUtil.findRoot(element);
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
            var routesProperty = obj.findProperty("routes");
            if (routesProperty != null) {
                var routesExpr = routesProperty.getLastChild();
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
