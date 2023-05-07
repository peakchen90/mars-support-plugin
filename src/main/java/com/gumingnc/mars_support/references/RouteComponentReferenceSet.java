package com.gumingnc.mars_support.references;

import com.gumingnc.mars_support.utils.FsUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class RouteComponentReferenceSet extends FileReferenceSet {
    public RouteComponentReferenceSet(@NotNull PsiElement element) {
        super(element);
    }

    @Override
    public @Nullable FileReference createFileReference(TextRange range, int index, String text) {
        return new ComponentFileReference(this, range, index, text);
    }

    private static class ComponentFileReference extends FileReference {
        public ComponentFileReference(@NotNull FileReferenceSet fileReferenceSet, TextRange range, int index, String text) {
            super(fileReferenceSet, range, index, text);
        }

        @Override
        protected void innerResolveInContext(@NotNull String text, @NotNull PsiFileSystemItem context, Collection<ResolveResult> result, boolean caseSensitive) {
            PsiElement targetPsiElement = null;

            if (isLast() && context.isDirectory()) {
                targetPsiElement = FsUtil.resolveIndexFile((PsiDirectory) context, text);
            }

            if (targetPsiElement != null) {
                result.add(new PsiElementResolveResult(targetPsiElement));
            } else {
                super.innerResolveInContext(text, context, result, caseSensitive);
            }
        }

        @Override
        public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
            return super.handleElementRename(newElementName);
        }

        // 移动至另一个文件夹
        @Override
        public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
            return super.bindToElement(element);
        }

        @Override
        protected PsiElement rename(String newName) throws IncorrectOperationException {
            // 如果可能，省略后缀或索引文件名
            if (isLast() && newName != null) {
                var lastReference = getLastFileReference();
                if (lastReference != null) {
                    var oldExtname = new FsUtil(lastReference.getText()).getExtension();
                    var fsUtil = new FsUtil(newName);

                    // 之前没有后缀名
                    if (oldExtname.isEmpty() && fsUtil.hasJsExtension()) {
                        newName = fsUtil.removeExtension();
                    }
                }
            }

            // 保证路径以 ./ 开始
            if (newName != null && !newName.startsWith("./")) {
                newName = "./" + newName;
            }


            return super.rename(newName);
        }
    }
}

