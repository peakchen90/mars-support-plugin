package com.gumingnc.mars_support.codeInsight;

import com.gumingnc.mars_support.utils.KoneConfigUtil;
import com.gumingnc.mars_support.utils.RoutesUtil;
import com.intellij.codeInsight.hints.*;
import com.intellij.lang.Language;
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment;
import com.intellij.lang.javascript.psi.impl.JSFileImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class RoutePathInlayProvider implements InlayHintsProvider {
    private static final SettingsKey settingsKey = new SettingsKey("mars.implicit.route.path.hint");

    @Override
    public boolean isVisibleInSettings() {
        return true;
    }

    @NotNull
    @Override
    public SettingsKey getKey() {
        return settingsKey;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "Mars route hints";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return null;
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull Object o) {
        return changeListener -> new JPanel();
    }

    @NotNull
    @Override
    public Object createSettings() {
        return new NoSettings();
    }

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull Object o,
                                               @NotNull InlayHintsSink inlayHintsSink) {
        return new RoutePathInlayHintsCollector(editor);
    }

    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        return true;
    }

    private class RoutePathInlayHintsCollector extends FactoryInlayHintsCollector {
        public RoutePathInlayHintsCollector(@NotNull Editor editor) {
            super(editor);
        }

        @Override
        public boolean collect(@NotNull PsiElement element, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
            ES6ExportDefaultAssignment defaultExportExpr = null;

            if (element instanceof JSFileImpl) {
                defaultExportExpr = ((JSFileImpl) element).findChildByClass(ES6ExportDefaultAssignment.class);
            }

            if (defaultExportExpr == null) {
                return false;
            }

            var info = RoutesUtil.getInstance(element).get(element.getContainingFile());
            if (info != null) {
                var marsConfig = KoneConfigUtil.getMarsConfig(element);
                if (marsConfig == null || !marsConfig.isAppType()) {
                    return false;
                }
                var appId = marsConfig.getValidAppId();

                var start = defaultExportExpr.getFirstChild().getTextOffset();
                var componentDeclaration = info.componentDeclaration;
                var pathDeclaration = info.pathDeclaration;
                var description = info.getDescription();
                var path = info.getValidPath();

                var hintText = "Path: " + ((appId.isEmpty() || path.isEmpty()) ? "invalid" : "/mars/" + appId + path);
                if (!description.isEmpty()) {
                    hintText = String.format("%s (%s)", hintText, description);
                }
                var target = path.isEmpty() ? componentDeclaration : pathDeclaration;

                var factory = getFactory();
                var presentation = factory.smallText(hintText);
                presentation = factory.psiSingleReference(presentation, () -> target);
                presentation = factory.roundWithBackground(presentation);
                inlayHintsSink.addBlockElement(start, false, true, 100, presentation);
            }

            return false;
        }
    }
}
