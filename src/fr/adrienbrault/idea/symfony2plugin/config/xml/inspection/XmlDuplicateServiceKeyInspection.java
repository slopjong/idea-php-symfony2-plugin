package fr.adrienbrault.idea.symfony2plugin.config.xml.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlTag;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class XmlDuplicateServiceKeyInspection extends LocalInspectionTool {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {

        PsiFile psiFile = holder.getFile();
        if(Symfony2ProjectComponent.isEnabled(psiFile.getProject())) {
            visitRoot(psiFile, holder, "services", "service", "id");
        }

        return super.buildVisitor(holder, isOnTheFly);
    }

    protected void visitRoot(PsiFile psiFile, @NotNull ProblemsHolder holder, String root, String child, String tagName) {

        XmlDocument xmlDocument = PsiTreeUtil.getChildOfType(psiFile, XmlDocument.class);
        if(xmlDocument == null) {
            return;
        }

        Map<String, XmlAttribute> psiElementMap = new HashMap<String, XmlAttribute>();
        Set<XmlAttribute> yamlKeyValues = new HashSet<XmlAttribute>();

        for(XmlTag xmlTag: PsiTreeUtil.getChildrenOfTypeAsList(psiFile.getFirstChild(), XmlTag.class)) {
            if(xmlTag.getName().equals("container")) {
                for(XmlTag servicesTag: xmlTag.getSubTags()) {
                    if(servicesTag.getName().equals(root)) {
                        for(XmlTag parameterTag: servicesTag.getSubTags()) {
                            if(parameterTag.getName().equals(child)) {
                                XmlAttribute keyAttr = parameterTag.getAttribute(tagName);
                                if(keyAttr != null) {
                                    String parameterName = keyAttr.getValue();
                                    if(parameterName != null && StringUtils.isNotBlank(parameterName)) {
                                        if(psiElementMap.containsKey(parameterName)) {
                                            yamlKeyValues.add(psiElementMap.get(parameterName));
                                            yamlKeyValues.add(keyAttr);
                                        } else {
                                            psiElementMap.put(parameterName, keyAttr);
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }

        if(yamlKeyValues.size() > 0) {
            for(PsiElement psiElement: yamlKeyValues) {
                XmlAttributeValue valueElement = ((XmlAttribute) psiElement).getValueElement();
                if(valueElement != null) {
                    holder.registerProblem(valueElement, "Duplicate Key", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
                }
            }
        }

    }


}
