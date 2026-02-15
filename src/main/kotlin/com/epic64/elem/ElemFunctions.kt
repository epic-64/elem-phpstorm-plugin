package com.epic64.elem

/**
 * Configuration for Elem helper functions and their class parameter positions.
 */
object ElemFunctions {

    /**
     * Map of Elem function names to their class parameter name.
     * All these functions accept a 'class' named parameter.
     */
    val FUNCTIONS_WITH_CLASS_PARAM: Set<String> = setOf(
        // Core functions from Epic64\Elem namespace
        "div", "span", "p", "a", "h",
        "img", "button", "input", "form", "label",
        "ul", "ol", "li",
        "table", "tr", "td", "th",
        "el", "nav", "header", "footer", "section", "article", "aside", "main",
        "textarea", "select", "option",
        "body", "html", "head"
    )

    /**
     * The namespace for Elem functions
     */
    const val ELEM_NAMESPACE = "Epic64\\Elem"

    /**
     * Parameter name for CSS classes
     */
    const val CLASS_PARAM_NAME = "class"
}
