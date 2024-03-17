package io.nimbly.tzatziki.view.features.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import icons.ActionIcons
import io.cucumber.tagexpressions.Expression
import io.nimbly.tzatziki.services.tzFileService
import io.nimbly.tzatziki.view.features.FeaturePanel

@Suppress("MissingActionUpdateThread")
class FilterTagAction(val panel: FeaturePanel) : ToggleAction() {
    init {
        this.templatePresentation.text = "Filter per tags"
        this.templatePresentation.icon = ActionIcons.FILTER
    }
    override fun isSelected(e: AnActionEvent): Boolean {
        return panel.project.tzFileService().filterByTags
    }
    override fun setSelected(e: AnActionEvent, state: Boolean) {

        val tzService = panel.project.tzFileService()

        val exp: Expression?
        if (state) {
            exp = tzService.tagExpression()
        } else {
            exp = null
        }

        tzService.filterByTags = state
        tzService.updateTagsFilter(exp)

        panel.filterByTag(state)
    }

    // Compatibility : introduced 2022.2.4
    //override fun getActionUpdateThread() = ActionUpdateThread.BGT
}