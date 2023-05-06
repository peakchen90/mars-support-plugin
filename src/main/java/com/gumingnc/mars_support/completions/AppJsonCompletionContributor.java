package com.gumingnc.mars_support.completions;

import com.intellij.codeInsight.completion.*;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class AppJsonCompletionContributor extends CompletionContributor {
    public AppJsonCompletionContributor() {
        extend(
                CompletionType.BASIC,
                PlatformPatterns.psiElement(),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                        result.getPrefixMatcher();
                    }

                    //                    public void addCompletions(@NotNull CompletionParameters parameters,
                    //                                               @NotNull ProcessingContext context,
                    //                                               @NotNull CompletionResultSet resultSet) {
                    //                        resultSet.addElement(LookupElementBuilder.create("Hello"));
                    //                    }
                }
        );
    }
}
