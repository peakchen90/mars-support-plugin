package com.gumingnc.mars_support.reference;

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

                var parent = literalElement.getParent();
                if (parent instanceof JsonProperty && parent.getLastChild() == literalElement && "component".equals(((JsonProperty) parent).getName())) {
                    parent = parent.getParent();
                    if (parent instanceof JsonObject) {
                        parent = parent.getParent(); // routes array
                        if (parent instanceof JsonArray) {
                            parent = parent.getParent(); // route property
                            if (parent instanceof JsonProperty && "routes".equals(((JsonProperty) parent).getName())) {
                                if (parent.getParent() instanceof JsonObject && parent.getParent().getParent() instanceof JsonFile) {
                                    if (AppConfigUtil.checkAppJson(parent)) {
                                        var referenceSet = new ComponentFileReferenceSet(literalElement);
                                        return referenceSet.getAllReferences();
                                    }
                                }
                            }
                        }
                    }
                }

                return PsiReference.EMPTY_ARRAY;
            }
        });
    }
}
