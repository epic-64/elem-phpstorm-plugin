package com.epic64.elem

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.jetbrains.php.lang.PhpLanguage
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.FunctionReference
import com.jetbrains.php.lang.psi.elements.impl.ParameterListImpl

/**
 * Reference contributor that provides CSS class references in Elem function calls.
 *
 * This enables Ctrl+Click navigation from CSS class names in PHP code like:
 *   div(class: 'my-class')
 * to the corresponding CSS class definition.
 */
class ElemCssClassReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // Match string literals in PHP
        registrar.registerReferenceProvider(
            getPattern(),
            ElemCssClassReferenceProvider()
        )
    }

    private fun getPattern(): PsiElementPattern.Capture<StringLiteralExpression> {
        return PlatformPatterns.psiElement(StringLiteralExpression::class.java)
            .withLanguage(PhpLanguage.INSTANCE)
    }
}
