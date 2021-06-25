/*
 * CUCUMBER +
 * Copyright (C) 2021  Maxime HAMM - NIMBLY CONSULTING - maxime.hamm.pro@gmail.com
 *
 * This document is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package io.nimbly.tzatziki.psi

import com.intellij.openapi.module.Module
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.refactoring.suggested.startOffset
import io.nimbly.tzatziki.references.TzCucumberStepReference
import org.jetbrains.plugins.cucumber.psi.GherkinFileType
import org.jetbrains.plugins.cucumber.psi.GherkinPsiUtil
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference

fun loadStepParams(step: GherkinStep): List<TextRange> {
    val references = step.references
    if (references.size != 1 || references[0] !is CucumberStepReference) {
        return emptyList()
    }
    val reference = references[0] as CucumberStepReference
    val definition = reference.resolveToDefinition()
    if (definition != null) {
        return GherkinPsiUtil.buildParameterRanges(step, definition, reference.rangeInElement.startOffset)
            ?.map { it.shiftRight(step.startOffset) }
            ?: emptyList()
    }
    return emptyList()
}

fun getCucumberStepDefinition(element: PsiElement): AbstractStepDefinition? {
    element.references.forEach { ref ->
        val def = when (ref) {
                is CucumberStepReference -> ref.resolveToDefinition()
                is TzCucumberStepReference -> ref.resolveToDefinition()
                else -> null
            }

        if (def!=null)
            return def
    }
    return null
}

val GherkinStep.descriptionRange: TextRange
    get() {
        val indexOfFirst = this.text.indexOfFirst { it == ' ' }
        if (indexOfFirst <0)
            return TextRange.EMPTY_RANGE
        var start = indexOfFirst + 1
        start += this.text.substring(start).indexOfFirst { it != ' ' }
        val eol = this.text.indexOfFirst { it == '\n' }
        return TextRange(
            start,
            if (eol > 0) eol else this.textLength
        )
    }

val GherkinStep.description
    get() = descriptionRange.substring(this.text)

fun Module.getGherkinScope()
    = GlobalSearchScope.getScopeRestrictedByFileTypes(
        GlobalSearchScope.moduleScope(this), GherkinFileType.INSTANCE)
