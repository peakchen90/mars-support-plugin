package com.gumingnc.mars_support.references;

import com.gumingnc.mars_support.utils.AppConfigUtil;
import com.intellij.json.psi.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class AppJsonReferenceContributor extends PsiReferenceContributor {
    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement(JsonStringLiteral.class), new PsiReferenceProvider() {
            @Override
            public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                final var literalElement = (JsonStringLiteral) element;
                var property = literalElement.getParent();

                if (property.getLastChild() == literalElement && AppConfigUtil.checkRoutesComponentProperty(property, true)) {
                    var referenceSet = new ComponentPathReferenceSet(literalElement);
                    return referenceSet.getAllReferences();
                }

                return PsiReference.EMPTY_ARRAY;
            }
        });
    }
}
