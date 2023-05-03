package com.gumingnc.mars_support.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;

public class AppConfigUtil {
     public final static String[] FileNames = {"src", "app.json"};
//    public final static String[] FileNames = {"src", "tsconfig.json"};

    /**
     * 判断是 src/app.json 配置文件
     */
    public static boolean checkAppJson(PsiElement element) {
        if (element != null && !(element instanceof PsiFileSystemItem)) {
            element = element.getContainingFile();
        }

        if (!(element instanceof PsiFile) || ((PsiFile) element).isDirectory()) {
            return false;
        }

        PsiFileSystemItem current = (PsiFile) element;
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


        if (current != null) {
            return current.getVirtualFile().getPath().equals(current.getProject().getBasePath());
        }

        return false;
    }
}
