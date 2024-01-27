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

package io.nimbly.tzatziki.testdiscovery

import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.TestStatusListener
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import io.nimbly.tzatziki.TOGGLE_CUCUMBER_PL
import io.nimbly.tzatziki.psi.cell
import io.nimbly.tzatziki.psi.findColumnByName
import io.nimbly.tzatziki.psi.table
import io.nimbly.tzatziki.util.findElement
import io.nimbly.tzatziki.util.isExample
import org.jetbrains.plugins.cucumber.psi.GherkinPsiElement
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import org.jetbrains.plugins.cucumber.psi.GherkinTableRow

class TzTestStatusListener : TestStatusListener() {

    override fun testSuiteFinished(root: AbstractTestProxy?, project: Project?) {

        if (!TOGGLE_CUCUMBER_PL)
            return

        if (project==null || root==null || root.children.isEmpty())
            return

        val results = TzTestResult()

        ApplicationManager.getApplication().runReadAction {
            root.allTests
                .filter { it.children.isEmpty() }
                .filterIsInstance<SMTestProxy>()
                .filter { it.locationUrl != null }
                .forEach { test ->
                    val r = findTestSteps(test, project)
                    results.putAll(r)
                }
            TzTestRegistry.refresh(results)
        }

    }

    private fun findTestSteps(test: SMTestProxy, project: Project): TzTestResult {

        val results = TzTestResult()

        // Simple step
        val element = test.findElement(project)?.parent
        if (element is GherkinPsiElement)
            results[element] = test

        // Step from example
        if (test.parent.isExample(project)) {

            val row = test.parent.findElement(project)?.parent
            if (row !is GherkinTableRow)
                return results

            val step = test.findElement(project)?.parent
            if (step !is GherkinStep)
                return results

            step.paramsSubstitutions
                .mapNotNull { row.table.findColumnByName(it) }
                .map { row.cell(it) }
                .forEach { cell ->
                    results[cell] = test
                }
        }

        return results
    }

    override fun testSuiteFinished(root: AbstractTestProxy?) {
        //Nothing to do
    }
}
