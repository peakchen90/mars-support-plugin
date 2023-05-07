package com.gumingnc.mars_support.icons;

import com.gumingnc.mars_support.utils.AppConfigUtil;
import com.intellij.ide.IconProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AppConfigIconProvider extends IconProvider {
    @Override
    @Nullable
    public Icon getIcon(@NotNull PsiElement element, int flags) {
        if (AppConfigUtil.getAppJsonFile(element) != null) {
            return Icons.Launch;
        }
        return null;
    }
}
