package com.gumingnc.mars_support.inspection;

import com.gumingnc.mars_support.utils.KoneConfigUtil;
import com.intellij.codeInspection.*;
import com.intellij.json.psi.*;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class KoneMarsInspection extends LocalInspectionTool {
    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        final var d = PsiElementVisitor.EMPTY_VISITOR;

        if (!KoneConfigUtil.isConfigFile(holder.getFile())) {
            return d;
        }

        var marConfig = KoneConfigUtil.resolveMarsConfig(holder.getFile());
        if (marConfig != null) {
            if (!(marConfig.mars.getLastChild() instanceof JsonObject)) {
                holder.registerProblem(marConfig.mars.getLastChild(), "Should be an object");
                return d;
            }

            if (marConfig.appId == null) {
                holder.registerProblem(marConfig.mars.getFirstChild(), "Missing appId filed");
            } else if (!(marConfig.appId instanceof JsonStringLiteral)) {
                holder.registerProblem(marConfig.appId, "The appId should be a string");
            } else if (marConfig.getValidAppId().isEmpty()) {
                holder.registerProblem(marConfig.appId, "The appId cannot match: ^[a-z0-9]+(-[a-z0-9]+)*$");
            }

            if (marConfig.type != null) {
                if (!(marConfig.type instanceof JsonStringLiteral)) {
                    holder.registerProblem(marConfig.type, "The type should be a string");
                } else if (marConfig.getValidType().isEmpty()) {
                    holder.registerProblem(marConfig.type, "The type can only be: app, main, static");
                }
            }
        }

        return d;
    }
}

