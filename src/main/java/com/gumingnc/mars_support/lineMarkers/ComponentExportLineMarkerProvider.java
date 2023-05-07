package com.gumingnc.mars_support.lineMarkers;

import com.gumingnc.mars_support.icons.Icons;
import com.gumingnc.mars_support.utils.RoutesUtil;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.ecmascript6.psi.ES6ExportDefaultAssignment;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ComponentExportLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        super.collectNavigationMarkers(element, result);
        if (!(element instanceof ES6ExportDefaultAssignment)) {
            return;
        }

        var info = RoutesUtil.getInstance(element).get(element.getContainingFile());

        if (info != null) {
            var componentDeclaration = info.componentDeclaration;
            var path = info.path;
            String tooltipText;
            if (path.isEmpty()) {
                tooltipText = "Route component";
            } else {
                tooltipText = "Route: " + path;
            }

            var builder = NavigationGutterIconBuilder.create(Icons.Mars).setTarget(componentDeclaration)
                                                     .setAlignment(GutterIconRenderer.Alignment.CENTER).setTooltipText(tooltipText);
            result.add(builder.createLineMarkerInfo(element.getFirstChild()));
        }
    }
}
