package fr.adrienbrault.idea.symfony2plugin.templating.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.util.ProcessingContext;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.TwigHelper;
import fr.adrienbrault.idea.symfony2plugin.asset.AssetLookupElement;
import fr.adrienbrault.idea.symfony2plugin.asset.dic.AssetDirectoryReader;
import fr.adrienbrault.idea.symfony2plugin.asset.dic.AssetFile;
import fr.adrienbrault.idea.symfony2plugin.routing.RouteHelper;
import fr.adrienbrault.idea.symfony2plugin.routing.RouteLookupElement;
import fr.adrienbrault.idea.symfony2plugin.templating.util.TwigHtmlCompletionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TwigHtmlCompletionContributor extends CompletionContributor {

    public TwigHtmlCompletionContributor() {

        // add completion for href and provide twig insert handler
        // <a href="#">#</a>
        // <a href="{{ path('', {'foo' : 'bar'}) }}">#</a>
        extend(CompletionType.BASIC, TwigHtmlCompletionUtil.getHrefAttributePattern(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {

                if(!Symfony2ProjectComponent.isEnabled(parameters.getPosition())) {
                    return;
                }

                List<LookupElement> routesLookupElements = RouteHelper.getRoutesLookupElements(parameters.getPosition().getProject());
                for(LookupElement element: routesLookupElements) {
                    if(element instanceof RouteLookupElement) {
                        ((RouteLookupElement) element).withInsertHandler(TwigPathFunctionInsertHandler.getInstance());
                    }
                }

                resultSet.addAllElements(routesLookupElements);

            }
        });

        extend(CompletionType.BASIC, TwigHtmlCompletionUtil.getAssetCssAttributePattern(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {

                if(!Symfony2ProjectComponent.isEnabled(parameters.getPosition())) {
                    return;
                }

                for (AssetFile assetFile : new AssetDirectoryReader().setProject(parameters.getPosition().getProject()).setFilterExtension(TwigHelper.CSS_FILES_EXTENSIONS).setIncludeBundleDir(false).getAssetFiles()) {
                    resultSet.addElement(new AssetLookupElement(assetFile, parameters.getPosition().getProject()).withInsertHandler(TwigAssetFunctionInsertHandler.getInstance()));
                }

            }

        });

        extend(CompletionType.BASIC, TwigHtmlCompletionUtil.getAssetJsAttributePattern(), new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext processingContext, @NotNull CompletionResultSet resultSet) {

                if(!Symfony2ProjectComponent.isEnabled(parameters.getPosition())) {
                    return;
                }

                for (AssetFile assetFile : new AssetDirectoryReader().setProject(parameters.getPosition().getProject()).setFilterExtension(TwigHelper.JS_FILES_EXTENSIONS).setIncludeBundleDir(false).getAssetFiles()) {
                    resultSet.addElement(new AssetLookupElement(assetFile, parameters.getPosition().getProject()).withInsertHandler(TwigAssetFunctionInsertHandler.getInstance()));
                }

            }

        });

    }

}
