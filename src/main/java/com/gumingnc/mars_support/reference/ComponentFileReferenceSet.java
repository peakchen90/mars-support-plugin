package com.gumingnc.mars_support.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class ComponentFileReferenceSet extends FileReferenceSet {
    public ComponentFileReferenceSet(@NotNull PsiElement element) {
        super(element);
    }

    @Override
    public @Nullable FileReference createFileReference(TextRange range, int index, String text) {
        return new ComponentFileReference(this, range, index, text);
    }
}

class ComponentFileReference extends FileReference {
    public ComponentFileReference(@NotNull FileReferenceSet fileReferenceSet, TextRange range, int index, String text) {
        super(fileReferenceSet, range, index, text);
    }

    @Override
    protected void innerResolveInContext(@NotNull String text, @NotNull PsiFileSystemItem context, Collection<ResolveResult> result, boolean caseSensitive) {
        PsiElement targetPsiElement = null;

        if (isLast() && context.isDirectory()) {
            PsiFileSystemItem targetDir = null;
            for (var item : context.getChildren()) {
                var fsItem = (PsiFileSystemItem) item;
                // 省略索引文件
                if (fsItem.isDirectory() && text.equals(fsItem.getName())) {
                    targetDir = fsItem;
                    break;
                }
                // 省略文件后缀
                if (!fsItem.isDirectory() && text.equals(fsItem.getVirtualFile().getNameWithoutExtension())) {
                    targetPsiElement = fsItem;
                    break;
                }
            }

            if (targetDir != null) {
                for (var item : targetDir.getChildren()) {
                    var file = ((PsiFileSystemItem) item).getVirtualFile();
                    var basename = file.getNameWithoutExtension();
                    var ext = file.getExtension();
                    if (!file.isDirectory() && "index".equals(basename) && ("tsx".equals(ext) || "ts".equals(ext) || "jsx".equals(ext) || "js".equals(ext))) {
                        targetPsiElement = item;
                        break;
                    }
                }
            }
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
        return super.rename(newName);
    }
}
