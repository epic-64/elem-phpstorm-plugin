package com.epic64.elem

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.css.CssClass
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

/**
 * Completion contributor that provides CSS class name suggestions
 * when typing in 'class' parameters of Elem functions.
 */
class ElemCssClassCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement()
                .withLanguage(PhpLanguage.INSTANCE)
                .inside(StringLiteralExpression::class.java),
            ElemCssClassCompletionProvider()
        )
    }
}

/**
 * Provides CSS class completions for Elem function calls.
 */
class ElemCssClassCompletionProvider : CompletionProvider<CompletionParameters>() {

    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        result: CompletionResultSet
    ) {
        val position = parameters.position
        val stringLiteral = findParentStringLiteral(position) ?: return

        // Check if we're in an Elem class parameter
        if (!isElemClassParameter(stringLiteral)) {
            return
        }

        val project = parameters.editor.project ?: return
        val scope = GlobalSearchScope.projectScope(project)

        // Collect all CSS classes from the project
        val cssClasses = mutableSetOf<String>()
        val cssFiles = FilenameIndex.getAllFilesByExt(project, "css", scope)

        for (virtualFile in cssFiles) {
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue
            collectCssClasses(psiFile, cssClasses)
        }

        // Add completion items for each CSS class
        for (className in cssClasses) {
            result.addElement(
                LookupElementBuilder.create(className)
                    .withIcon(AllIcons.Xml.Css_class)
                    .withTypeText("CSS class")
                    .withBoldness(true)
            )
        }
    }

    private fun findParentStringLiteral(element: PsiElement): StringLiteralExpression? {
        var current: PsiElement? = element
        while (current != null) {
            if (current is StringLiteralExpression) {
                return current
            }
            current = current.parent
        }
        return null
    }

    private fun isElemClassParameter(element: StringLiteralExpression): Boolean {
        val parameterList = element.parent as? ParameterList ?: return false
        val functionRef = parameterList.parent as? FunctionReference ?: return false

        val functionName = functionRef.name ?: return false
        if (functionName !in ElemFunctions.FUNCTIONS_WITH_CLASS_PARAM) {
            return false
        }

        // Check if this is the 'class' named parameter
        var sibling = element.prevSibling
        while (sibling != null) {
            val text = sibling.text?.trim()
            if (text == ":") {
                val nameSibling = sibling.prevSibling
                if (nameSibling != null) {
                    val nameText = nameSibling.text?.trim()
                    if (nameText == ElemFunctions.CLASS_PARAM_NAME) {
                        return true
                    }
                }
                break
            }
            sibling = sibling.prevSibling
        }

        return false
    }

    private fun collectCssClasses(psiFile: PsiFile, classes: MutableSet<String>) {
        psiFile.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is CssClass) {
                    element.name?.let { classes.add(it) }
                }

                // Also parse text for class selectors
                val text = element.text
                if (text.startsWith(".") && text.length > 1) {
                    val className = text.substring(1)
                        .takeWhile { it.isLetterOrDigit() || it == '-' || it == '_' }
                    if (className.isNotEmpty() && !className.contains("{")) {
                        classes.add(className)
                    }
                }

                super.visitElement(element)
            }
        })
    }
}
