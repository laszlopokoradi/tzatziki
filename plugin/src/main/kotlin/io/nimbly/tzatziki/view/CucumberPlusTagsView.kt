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
package io.nimbly.tzatziki.view

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.psi.PsiDocumentManager
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanelWithEmptyText
import io.nimbly.tzatziki.psi.getGherkinScope
import io.nimbly.tzatziki.util.findAllTags
import java.awt.BorderLayout
import java.awt.FlowLayout
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CucumberPlusTagsView(private val project: Project)
    : SimpleToolWindowPanel(true, false) {

    init {
        setContent(initTagsPanel())
    }

    private fun initTagsPanel(): JPanel {

        val p = JBPanelWithEmptyText()
        p.layout = BorderLayout(10, 10)
        p.border = EmptyBorder(10, 10, 10, 10)
        p.withEmptyText("No tags found")

        val title = JBLabel("""<html>
            Only the scenarios with the selected tags will be triggered.<br/><br/>
            <b>Select Tags</b> :
            </html>""".trimMargin())
        p.add(title, BorderLayout.PAGE_START)

        lateinit var tagsPanel: JPanel

        fun refresh() {
            DumbService.getInstance(project).smartInvokeLater {
                PsiDocumentManager.getInstance(project).performWhenAllCommitted() {
                    val newTagsPanel = newTagPanel()
                    p.remove(tagsPanel)
                    p.add(newTagsPanel, BorderLayout.CENTER)
                    tagsPanel = newTagsPanel
                }
            }
        }

        DumbService.getInstance(project).smartInvokeLater {

            // First tag list initialization
            tagsPanel = newTagPanel()
            p.add(tagsPanel, BorderLayout.CENTER)

            // Listen to file refreshing
            EditorFactory.getInstance().eventMulticaster.addDocumentListener(object : DocumentListener {
                override fun documentChanged(event: DocumentEvent) = refresh()
            }, project)
        }

        //p.add(JBLabel("Enjoy Cucumber+ !"), BorderLayout.PAGE_END)

        return p
    }

    private fun newTagPanel(): JPanel {

        val tags = findAllTags(project, project.getGherkinScope()).sortedBy { it.tag.name.toUpperCase() }

        val p = JBPanelWithEmptyText()
        p.layout = FlowLayout(FlowLayout.LEFT)

        tags.forEach { tag ->
            val t = JBCheckBox(tag.tag.name)
            p.add(t)
        }

        return p
    }

}