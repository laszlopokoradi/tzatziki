/*
 * CUCUMBER +
 * Copyright (C) 2023  Maxime HAMM - NIMBLY CONSULTING - Maxime.HAMM@nimbly-consulting.com
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

package io.nimbly.tzatziki.inspections

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import io.nimbly.tzatziki.TOGGLE_CUCUMBER_PL
import io.nimbly.tzatziki.Tzatziki
import io.nimbly.tzatziki.references.getCucumberStepDefinition
import io.nimbly.tzatziki.util.descriptionRange
import org.jetbrains.plugins.cucumber.inspections.GherkinInspection
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor
import org.jetbrains.plugins.cucumber.psi.GherkinStep

class TzDeprecatedStepInspection : GherkinInspection() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return object : GherkinElementVisitor() {

            override fun visitStep(step: GherkinStep) {

                super.visitStep(step)

                if (!TOGGLE_CUCUMBER_PL)
                    return

                val definition = getCucumberStepDefinition(step)
                    ?: return

                val element = definition.element
                    ?: return

                val deprecated = Tzatziki().extensionList.find {
                    it.isDeprecated(element)
                }

                if (deprecated !=null) {

                    val range = step.descriptionRange
                    holder.registerProblem(
                        step,
                        "Deprecated step",
                        ProblemHighlightType.LIKE_DEPRECATED,
                        range)
                }
            }
        }
    }
}