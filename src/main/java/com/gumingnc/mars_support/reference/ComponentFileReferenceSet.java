package com.gumingnc.mars_support.reference;

import com.gumingnc.mars_support.utils.JsIndexUtil;
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
            for (var item : context.getChildren()) {
                var fsItem = (PsiFileSystemItem) item;

                // 省略索引文件
                if (fsItem.isDirectory() && text.equals(fsItem.getName())) {
                    for (var child : fsItem.getChildren()) {
                        var file = ((PsiFileSystemItem) child).getVirtualFile();
                        if (!file.isDirectory() && JsIndexUtil.isJsIndex(file.getName())) {
                            targetPsiElement = child;
                            break;
                        }
                    }
                    break;
                }

                // 省略文件后缀
                if (!fsItem.isDirectory()) {
                    var virtualFile = fsItem.getVirtualFile();
                    if (text.equals(virtualFile.getNameWithoutExtension()) && JsIndexUtil.isJsExtension(virtualFile.getExtension())) {
                        targetPsiElement = fsItem;
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
        // 如果可能，省略后缀或索引文件名
        if (isLast() && newName != null) {
            var lastReference = getLastFileReference();
            if (lastReference != null) {
                var oldExtname = new JsIndexUtil(lastReference.getText()).getExtension();
                var indexUtil = new JsIndexUtil(newName);

                // 之前没有后缀名
                if (oldExtname.isEmpty() && indexUtil.isJsExtension()) {
                    newName = indexUtil.removeExtension();
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
