package com.gumingnc.mars_support.completions;

import com.gumingnc.mars_support.utils.AppConfigUtil;
import com.gumingnc.mars_support.utils.FsUtil;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;


class RouteComponentCompletionContributor extends CompletionContributor {
    public RouteComponentCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement().inside(JsonStringLiteral.class), new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                                          @NotNull CompletionResultSet result) {
                var originalElement = parameters.getOriginalPosition();
                if (!(originalElement instanceof JsonStringLiteral)) {
                    originalElement = PsiTreeUtil.getParentOfType(originalElement, JsonStringLiteral.class, true);
                }

                final var property = PsiTreeUtil.getParentOfType(originalElement, JsonProperty.class, true);
                var appJsonFile = AppConfigUtil.getAppJsonFile(parameters.getOriginalFile());
                if (appJsonFile != null && AppConfigUtil.checkRoutesComponentProperty(property) &&
                    property.getLastChild() == originalElement) {
                    var element = parameters.getPosition();
                    var text = element.getText();
                    var cursorIndex = text.indexOf(CompletionUtilCore.DUMMY_IDENTIFIER);

                    if (text.length() < 2) {
                        return;
                    }

                    // 去掉引号
                    if (cursorIndex >= 1) {
                        text = text.substring(1, cursorIndex);
                    } else {
                        text = text.substring(1, text.length() - 1);
                    }

                    boolean hasTailDot;
                    var lastSlashIndex = text.lastIndexOf("/");
                    if (lastSlashIndex >= 0) {
                        hasTailDot = text.substring(lastSlashIndex + 1).trim().equals(".");
                    } else {
                        hasTailDot = text.trim().equals(".");
                    }

                    var parsedPath = new FsUtil(text).parse();
                    var dirname = parsedPath.dirname;
                    var basename = parsedPath.basename;
                    if (hasTailDot) {
                        if (dirname.isEmpty()) {
                            dirname = basename;
                        } else if (!basename.isEmpty()) {
                            dirname = dirname + "/" + basename;
                        }
                        basename = "";
                    }

                    var currentDir = FsUtil.resolveSubdirectory(appJsonFile.getParent(), dirname);
                    if (currentDir == null) {
                        return;
                    }

                    final var _result = result.withPrefixMatcher(basename);

                    for (var item : currentDir.getChildren()) {
                        var fsItem = ((PsiFileSystemItem) item);
                        var icon = fsItem.getIcon(Iconable.ICON_FLAG_VISIBILITY);
                        var subName = fsItem.getName();
                        var builder = LookupElementBuilder.create(subName).withIcon(icon);

                        if (!fsItem.isDirectory() && !new FsUtil(fsItem.getName()).hasJsExtension()) {
                            continue;
                        }

                        if (hasTailDot) {
                            if (!fsItem.isDirectory()) {
                                _result.addElement(builder.withInsertHandler(new InsertHandler<LookupElement>() {
                                    @Override
                                    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
                                        var textRange = element.getTextRange();
                                        var start = textRange.getStartOffset() + cursorIndex - 1;
                                        var end = start + 1;
                                        var document = context.getDocument();
                                        var newText = new TextRange(start, end).replace(document.getText(), "");
                                        document.setText(newText);
                                    }
                                }));
                            }
                        } else if (basename.isEmpty() || subName.contains(basename)) {
                            _result.addElement(builder);
                        }
                    }
                }
            }
        });
    }
}
