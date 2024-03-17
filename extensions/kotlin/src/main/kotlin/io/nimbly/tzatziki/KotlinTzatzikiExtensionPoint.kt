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

package io.nimbly.tzatziki

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.xdebugger.XDebugSession
import com.intellij.xdebugger.breakpoints.XBreakpoint
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import io.nimbly.tzatziki.util.findUsages
import io.nimbly.tzatziki.util.getFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference

class KotlinTzatzikiExtensionPoint : TzatzikiExtensionPoint {

    override fun isDeprecated(element: PsiElement): Boolean {
        return element is PsiMethod && element.isDeprecated
    }

    override fun canRunStep(stepDefinitions: List<AbstractStepDefinition>): Boolean {
        return false
    }

    override fun findBreakpoint(source: PsiElement, stepDefinitions: List<AbstractStepDefinition>): TzBreakpoint? {
        return null
    }

    override fun initBreakpointListener(project: Project) {

        fun refresh(breakpoint: XBreakpoint<*>) {

            val sourcePosition = breakpoint.sourcePosition ?: return

            val vfile = sourcePosition.file
            if (!vfile.isValid) return
            val file = vfile.getFile(project) ?: return
            if (file !is KtFile) return

            val element = file.findElementAt(sourcePosition.offset) ?: return
            val function = PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java) ?: return

            // Find usages
            DumbService.getInstance(element.project).runReadActionInSmartMode {

                val usages = try {
                    findUsages(function)
                } catch (e: IndexNotReadyException) {
                    emptyList()
                } catch (e: Exception) {
                    if (e.cause !=null && e.cause is IndexNotReadyException)
                         emptyList()
                    else
                        throw e
                }

                usages
                    .asSequence()
                    .filterIsInstance<CucumberStepReference>()
                    .map { it.element }
                    .filterIsInstance<GherkinStep>()
                    .map { it.containingFile }
                    .toSet()
                    .forEach {
                        DaemonCodeAnalyzer.getInstance(project).restart(it)
                    }

            }

        }

        project.messageBus
            .connect()
            .subscribe(XBreakpointListener.TOPIC, object : XBreakpointListener<XBreakpoint<*>> {
                override fun breakpointChanged(breakpoint: XBreakpoint<*>) = refresh(breakpoint)
                override fun breakpointAdded(breakpoint: XBreakpoint<*>) = refresh(breakpoint)
                override fun breakpointRemoved(breakpoint: XBreakpoint<*>) = refresh(breakpoint)
                override fun breakpointPresentationUpdated(breakpoint: XBreakpoint<*>, session: XDebugSession?) = Unit
            })
    }
}
