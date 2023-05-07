package com.gumingnc.mars_support.inspections;

import com.gumingnc.mars_support.utils.AppConfigUtil;
import com.gumingnc.mars_support.utils.FsUtil;
import com.intellij.codeInspection.*;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.json.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class ConfigRoutesInspection extends LocalInspectionTool {
    private final RemoveWhitespaceQuickFix removeWhitespaceQuickFix = new RemoveWhitespaceQuickFix();
    private final ConvertRelativePathQuickFix convertRelativePathQuickFix = new ConvertRelativePathQuickFix();
    private final ShorterPathQuickFix shorterPathQuickFix = new ShorterPathQuickFix();

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        PsiFile appJsonFile = AppConfigUtil.getAppJsonFile(holder.getFile());

        if (appJsonFile == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        final var context = appJsonFile.getParent();
        final var routePathSet = new HashSet<String>();

        return new JsonElementVisitor() {
            @Override
            public void visitProperty(@NotNull JsonProperty o) {
                if (AppConfigUtil.checkRoutesComponentProperty(o)) {
                    // 必须为字符串
                    var valueExpression = o.getLastChild();
                    if (!(valueExpression instanceof JsonStringLiteral)) {
                        holder.registerProblem(valueExpression, "Route component should be a string");
                        return;
                    }

                    // 不能为空字符
                    var value = ((JsonStringLiteral) valueExpression).getValue();
                    var trimedValue = value.trim();
                    if (trimedValue.isEmpty()) {
                        holder.registerProblem(valueExpression, "Route component should be a non-empty string");
                        return;
                    }

                    // 存在空白字符
                    if (value.length() != trimedValue.length()) {
                        holder.registerProblem(valueExpression, "Cannot be leading and trailing whitespace characters", ProblemHighlightType.WARNING, removeWhitespaceQuickFix);
                    }

                    // 路径不存在
                    var target = FsUtil.resolveIndexFile(context, trimedValue);
                    if (target == null) {
                        holder.registerProblem(valueExpression, "Cannot resolve route component: " + trimedValue);
                        return;
                    }

                    // 路径可以更短
                    var fsUtil = new FsUtil(trimedValue);
                    var parsedPath = fsUtil.parse();
                    if (fsUtil.hasJsExtension() || parsedPath.basenameWithoutExt.equals("index") || trimedValue.endsWith("/")) {
                        holder.registerProblem(valueExpression, "Route component can be shorter", ProblemHighlightType.WEAK_WARNING, shorterPathQuickFix);
                    }

                    // 路径需以 ./ 开始
                    if (trimedValue.startsWith("../")) {
                        holder.registerProblem(valueExpression, "Route component should in the src/ directory", ProblemHighlightType.WARNING);
                    } else if (!trimedValue.startsWith("./")) {
                        holder.registerProblem(valueExpression, "Route component should start with ./", convertRelativePathQuickFix);
                    }

                } else if (AppConfigUtil.checkRoutesPathProperty(o)) {
                    var valueExpression = o.getLastChild();
                    if (!(valueExpression instanceof JsonStringLiteral)) {
                        holder.registerProblem(valueExpression, "Route path should be a string");
                        return;
                    }

                    var value = ((JsonStringLiteral) valueExpression).getValue();
                    if (value.isEmpty()) {
                        holder.registerProblem(valueExpression, "Route path should be a non-empty string");
                        return;
                    }

                    if (!value.matches("^(/[-\\w]+)+$")) {
                        holder.registerProblem(valueExpression, "Route path cannot match: ^(/[-\\w]+)+$");
                        return;
                    }

                    if (routePathSet.contains(value)) {
                        holder.registerProblem(valueExpression, "Duplicate route path: " + value);
                        return;
                    }

                    // 收集 route path
                    routePathSet.add(value);

                } else if (AppConfigUtil.checkRoutesProperty(o)) {
                    var valueExpression = o.getLastChild();
                    if (!(valueExpression instanceof JsonArray)) {
                        holder.registerProblem(valueExpression, "Routes should be an array");
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
            var fsUtil = new FsUtil(rawValue);
            var newValue = "";

            if (fsUtil.getBasenameWithoutExt().equals("index")) {
                newValue = fsUtil.getDirname();
            } else {
                newValue = fsUtil.removeExtension();
            }

            if (!newValue.startsWith("./") && !newValue.startsWith("../")) {
                newValue = "./" + newValue;
            }

            if (newValue.endsWith("/")) {
                newValue = newValue.substring(0, newValue.length() - 1);
            }

            var newElement = generator.createStringLiteral(newValue);
            element.replace(newElement);
        }
    }
}

