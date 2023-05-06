package com.gumingnc.mars_support.inspections;

import com.gumingnc.mars_support.utils.AppConfigUtil;
import com.gumingnc.mars_support.utils.JsIndexUtil;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.json.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class ComponentPathInspection extends LocalInspectionTool {
    private final RemoveWhitespaceQuickFix removeWhitespaceQuickFix = new RemoveWhitespaceQuickFix();
    private final ConvertRelativePathQuickFix convertRelativePathQuickFix = new ConvertRelativePathQuickFix();
    private final ShorterPathQuickFix shorterPathQuickFix = new ShorterPathQuickFix();

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        PsiFile appJsonFile = AppConfigUtil.getAppJsonFile(holder.getFile());

        if (appJsonFile == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        var context = appJsonFile.getParent();

        return new JsonElementVisitor() {
            @Override
            public void visitProperty(@NotNull JsonProperty o) {
                if (AppConfigUtil.checkRoutesComponentProperty(o)) {
                    // 必须为字符串
                    var valueExpression = o.getLastChild();
                    if (!(valueExpression instanceof JsonStringLiteral)) {
                        holder.registerProblem(valueExpression, "Component should be a string");
                        return;
                    }

                    // 不能为空字符
                    var value = ((JsonStringLiteral) valueExpression).getValue();
                    var trimValue = value.trim();
                    if (trimValue.isEmpty()) {
                        holder.registerProblem(valueExpression, "Component should be a non-empty string");
                        return;
                    }

                    // 存在空白字符
                    if (value.length() != trimValue.length()) {
                        holder.registerProblem(valueExpression, "Cannot be leading and trailing whitespace characters", ProblemHighlightType.WARNING, removeWhitespaceQuickFix);
                    }

                    // 路径不存在
                    var target = JsIndexUtil.resolveIndexFile(context, trimValue);
                    if (target == null) {
                        holder.registerProblem(valueExpression, "Cannot resolve component: " + trimValue);
                        return;
                    }

                    // 路径可以更短
                    var indexUtil = new JsIndexUtil(trimValue);
                    if (indexUtil.hasJsExtension() || indexUtil.getBasenameWithoutExt().equals("index")) {
                        holder.registerProblem(valueExpression, "Path can be shorter", ProblemHighlightType.WARNING, shorterPathQuickFix);
                    }

                    // 路径不以 ./ 开始
                    if (!trimValue.startsWith("./") && !trimValue.startsWith("../")) {
                        holder.registerProblem(valueExpression, "Component should start with ./", convertRelativePathQuickFix);
                    }
                }
            }
        };
    }

    // 移除空字符
    private static class RemoveWhitespaceQuickFix implements LocalQuickFix {
        @Override
        public @IntentionName @NotNull String getName() {
            return "Remove leading and trailing whitespace characters";
        }

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            var element = (JsonStringLiteral) descriptor.getPsiElement();
            var generator = new JsonElementGenerator(project);
            var newElement = generator.createStringLiteral(element.getValue().trim());
            element.replace(newElement);
        }
    }

    // 转换为相对路径
    private static class ConvertRelativePathQuickFix implements LocalQuickFix {
        @Override
        public @IntentionName @NotNull String getName() {
            return "Convert to a standard relative path";
        }

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            var element = (JsonStringLiteral) descriptor.getPsiElement();
            var generator = new JsonElementGenerator(project);
            var rawValue = element.getValue().trim();
            if (rawValue.charAt(0) == '/') {
                rawValue = rawValue.substring(1);
            }
            var newElement = generator.createStringLiteral("./" + rawValue);
            element.replace(newElement);
        }
    }

    // 转换更短路径
    private static class ShorterPathQuickFix implements LocalQuickFix {
        @Override
        public @IntentionName @NotNull String getName() {
            return "Convert to a shorter path";
        }

        @Override
        public @IntentionFamilyName @NotNull String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            var element = (JsonStringLiteral) descriptor.getPsiElement();
            var generator = new JsonElementGenerator(project);
            var rawValue = element.getValue().trim();
            var indexUtil = new JsIndexUtil(rawValue);
            var newValue = "";

            if (indexUtil.getBasenameWithoutExt().equals("index")) {
                newValue = indexUtil.getDirname();
            } else {
                newValue = indexUtil.removeExtension();
            }

            if (!newValue.startsWith("./")) {
                newValue = "./" + newValue;
            }

            var newElement = generator.createStringLiteral(newValue);
            element.replace(newElement);
        }
    }
}

