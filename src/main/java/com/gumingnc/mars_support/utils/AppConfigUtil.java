package com.gumingnc.mars_support.utils;

import com.intellij.json.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import org.jetbrains.annotations.Nullable;

public class AppConfigUtil {
    public final static String[] FileNames = {"src", "app.json"};
    public final static String KoneConfigName = "kone.config.json";

    /**
     * 返回 src/app.json 配置文件
     */
    public static @Nullable PsiFile getAppJsonFile(PsiElement element) {
        PsiFile file;

        if (element == null) {
            return null;
        }

        if (element instanceof PsiFileSystemItem) {
            if (((PsiFileSystemItem) element).isDirectory()) {
                return null;
            }
            file = (PsiFile) element;
        } else {
            try {
                file = element.getContainingFile();
            } catch (Exception ignored) {
                return null;
            }
        }

        PsiFileSystemItem current = file;
        for (int i = FileNames.length - 1; i >= 0; i--) {
            if (current == null) {
                break;
            }

            var name = FileNames[i];
            if (name.equals(current.getName())) {
                current = current.getParent();
            } else {
                current = null;
                break;
            }
        }

        if (current != null && current.getVirtualFile().findChild(KoneConfigName) != null) {
            return file;
        }

        return null;
    }

    public static boolean checkRoutesComponentProperty(PsiElement element) {
        return checkRoutesComponentProperty(element, false);
    }

    /**
     * 判断是 routes[n].component property
     */
    public static boolean checkRoutesComponentProperty(PsiElement element, boolean checkPosition) {
        if (!(element instanceof JsonProperty)) {
            return false;
        }
        if (!("component".equals(((JsonProperty) element).getName()))) {
            return false;
        }

        var parent = element.getParent();
        if (parent instanceof JsonObject) {
            parent = parent.getParent(); // routes array
            if (parent instanceof JsonArray) {
                parent = parent.getParent(); // route property
                return checkRoutesProperty(parent, checkPosition);
            }
        }

        return false;
    }


    public static boolean checkRoutesPathProperty(PsiElement element) {
        return checkRoutesPathProperty(element, false);
    }

    /**
     * 判断是 routes[n].path property
     */
    public static boolean checkRoutesPathProperty(PsiElement element, boolean checkPosition) {
        if (!(element instanceof JsonProperty)) {
            return false;
        }
        if (!("path".equals(((JsonProperty) element).getName()))) {
            return false;
        }

        var parent = element.getParent();
        if (parent instanceof JsonObject) {
            parent = parent.getParent(); // routes array
            if (parent instanceof JsonArray) {
                parent = parent.getParent(); // route property
                return checkRoutesProperty(parent, checkPosition);
            }
        }

        return false;
    }


    public static boolean checkRoutesProperty(PsiElement element) {
        return checkRoutesProperty(element, false);
    }

    /**
     * 判断是 routes property
     */
    public static boolean checkRoutesProperty(PsiElement element, boolean checkPosition) {
        if (!(element instanceof JsonProperty)) {
            return false;
        }
        if (!("routes".equals(((JsonProperty) element).getName()))) {
            return false;
        }

        var parent = element.getParent();
        if (parent instanceof JsonObject) {
            parent = parent.getParent(); // JSONFile(<root>/src/app.json)
            if (parent instanceof JsonFile) {
                if (checkPosition) {
                    return AppConfigUtil.getAppJsonFile(parent) != null;
                }
                return true;
            }
        }

        return false;
    }
}
