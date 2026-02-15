package com.epic64.elem

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceProvider
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression

/**
 * Provides CSS class references for string literals that are class parameters
 * in Elem function calls.
 */
class ElemCssClassReferenceProvider : PsiReferenceProvider() {

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        if (element !is StringLiteralExpression) {
            return PsiReference.EMPTY_ARRAY
        }

        // Check if this string is a class parameter in an Elem function
        if (!isElemClassParameter(element)) {
            return PsiReference.EMPTY_ARRAY
        }

        val classValue = element.contents
        if (classValue.isBlank()) {
            return PsiReference.EMPTY_ARRAY
        }

        // Split by whitespace to handle multiple classes like "btn btn-primary"
        val classes = classValue.split(Regex("\\s+")).filter { it.isNotBlank() }

        if (classes.isEmpty()) {
            return PsiReference.EMPTY_ARRAY
        }

        // Create a reference for each CSS class
        val references = mutableListOf<PsiReference>()
        var currentOffset = 1 // Start after opening quote

        for (className in classes) {
            val classStart = classValue.indexOf(className, currentOffset - 1)
            if (classStart >= 0) {
                references.add(
                    ElemCssClassReference(
                        element,
                        className,
                        classStart + 1, // +1 for the opening quote
                        className.length
                    )
                )
                currentOffset = classStart + className.length + 1
            }
        }

        return references.toTypedArray()
    }

    /**
     * Check if the string literal is a 'class' parameter in an Elem function call.
     */
    private fun isElemClassParameter(element: StringLiteralExpression): Boolean {
        // Navigate up: StringLiteral -> ParameterList -> FunctionReference
        val parameterList = element.parent as? ParameterList ?: return false
        val functionRef = parameterList.parent as? FunctionReference ?: return false

        // Check if this is an Elem function
        val functionName = functionRef.name ?: return false
        if (functionName !in ElemFunctions.FUNCTIONS_WITH_CLASS_PARAM) {
            return false
        }

        // Check if this is a named parameter called 'class'
        // In PHP 8+ named arguments: div(class: 'value')
        val parameters = parameterList.parameters
        for ((index, param) in parameters.withIndex()) {
            if (param === element) {
                // Check if this is a named argument
                val paramName = getNamedArgumentName(element)
                if (paramName == ElemFunctions.CLASS_PARAM_NAME) {
                    return true
                }
                // Also check by position for older-style calls
                // The class parameter is typically at index 1 for most functions
                // (after 'id' parameter)
                break
            }
        }

        return false
    }

    /**
     * Get the name of a named argument if this is one.
     * In PHP 8+: function(name: 'value') - returns "name"
     */
    private fun getNamedArgumentName(element: PsiElement): String? {
        // Look for the argument name in the PSI tree
        // Named arguments have a specific structure in PHP PSI
        val parent = element.parent ?: return null

        // Try to find the argument name from siblings or parent structure
        var sibling = element.prevSibling
        while (sibling != null) {
            val text = sibling.text
            if (text == ":") {
                // Found the colon, the previous sibling should be the name
                val nameSibling = sibling.prevSibling
                while (nameSibling != null) {
                    val nameText = nameSibling.text.trim()
                    if (nameText.isNotEmpty() && nameText != "," && !nameText.startsWith("(")) {
                        return nameText
                    }
                    break
                }
            }
            sibling = sibling.prevSibling
        }

        return null
    }
}
