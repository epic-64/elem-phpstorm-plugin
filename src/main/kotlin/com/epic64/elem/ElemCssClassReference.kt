package com.epic64.elem

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.css.CssClass
import com.intellij.psi.css.CssSelector
import com.intellij.psi.css.CssSelectorList
import com.intellij.psi.css.impl.CssElementTypes
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

/**
 * A reference from a CSS class name in PHP code to its CSS definition.
 */
class ElemCssClassReference(
    element: StringLiteralExpression,
    private val className: String,
    private val startOffset: Int,
    private val length: Int
) : PsiReferenceBase<StringLiteralExpression>(element, TextRange(startOffset, startOffset + length), true) {

    override fun resolve(): PsiElement? {
        val project = element.project
        return findCssClass(project, className)
    }

    override fun getVariants(): Array<Any> {
        // Return all CSS classes for completion
        val project = element.project
        return findAllCssClasses(project).toTypedArray()
    }

    /**
     * Find a CSS class definition by name.
     */
    private fun findCssClass(project: Project, className: String): PsiElement? {
        val scope = GlobalSearchScope.projectScope(project)

        // Search for .css files
        val cssFiles = FilenameIndex.getAllFilesByExt(project, "css", scope)

        for (virtualFile in cssFiles) {
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue

            // Find all CSS class selectors in the file
            val classSelector = findClassInFile(psiFile, className)
            if (classSelector != null) {
                return classSelector
            }
        }

        return null
    }

    /**
     * Find a CSS class selector in a file.
     */
    private fun findClassInFile(psiFile: PsiFile, className: String): PsiElement? {
        // Walk through all elements looking for class selectors
        var result: PsiElement? = null

        psiFile.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (result != null) return

                // Look for CSS class selectors (e.g., .my-class)
                if (element is CssClass || element.text == ".$className") {
                    if (element.text == ".$className" ||
                        (element is CssClass && element.name == className)) {
                        result = element
                        return
                    }
                }

                // Also check text content for class selectors
                val text = element.text
                if (text.startsWith(".") && !text.contains("{") && !text.contains(" ")) {
                    val selectorName = text.substring(1)
                    if (selectorName == className) {
                        result = element
                        return
                    }
                }

                super.visitElement(element)
            }
        })

        return result
    }

    /**
     * Find all CSS class names in the project for completion.
     */
    private fun findAllCssClasses(project: Project): List<String> {
        val classes = mutableSetOf<String>()
        val scope = GlobalSearchScope.projectScope(project)

        val cssFiles = FilenameIndex.getAllFilesByExt(project, "css", scope)

        for (virtualFile in cssFiles) {
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: continue

            psiFile.accept(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    // Look for class selectors
                    val text = element.text
                    if (text.startsWith(".") && !text.contains("{") && !text.contains(" ") && text.length > 1) {
                        val className = text.substring(1).takeWhile { it.isLetterOrDigit() || it == '-' || it == '_' }
                        if (className.isNotEmpty()) {
                            classes.add(className)
                        }
                    }

                    if (element is CssClass) {
                        element.name?.let { classes.add(it) }
                    }

                    super.visitElement(element)
                }
            })
        }

        return classes.toList()
    }
}
