package io.nimbly.tzatziki

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.IdeActions.*
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionManager
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import io.nimbly.tzatziki.TzTModuleListener.AbstractWriteActionHandler

class TzTModuleListener : ProjectManagerListener {

    override fun projectOpened(project: Project) {
        if (!handlerInitialized) {
            initTypedHandler()
            handlerInitialized = true
        }
    }

    private fun initTypedHandler() {

        val actionManager = EditorActionManager.getInstance()

        actionManager.replaceHandler(FormatterHandler(ACTION_EDITOR_DELETE))
        actionManager.replaceHandler(FormatterHandler(ACTION_EDITOR_BACKSPACE))

        actionManager.replaceHandler(FormatterHandler(ACTION_EDITOR_CUT))
        actionManager.replaceHandler(FormatterHandler(ACTION_EDITOR_PASTE))

        actionManager.replaceHandler(TabHandler(ACTION_EDITOR_TAB))
        actionManager.replaceHandler(TabHandler(EDITOR_UNINDENT_SELECTION))
    }

    private class FormatterHandler(actionId : String) : AbstractWriteActionHandler(actionId) {
        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
            doDefault(editor, caret, dataContext)
            editor.findTable(editor.caretModel.offset)?.format()
        }
    }

    private class TabHandler(actionId : String) : AbstractWriteActionHandler(actionId) {
        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext) {
            if (!editor.navigateInTable(getActionId() == ACTION_EDITOR_TAB, editor))
                doDefault(editor, caret, dataContext)
        }
    }

    abstract class AbstractWriteActionHandler(private val id: String) : EditorWriteActionHandler() {
        private val orginHandler = EditorActionManager.getInstance().getActionHandler(id)
        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext)
            = doDefault(editor, caret, dataContext)
        open fun doDefault(editor: Editor, caret: Caret?, dataContext: DataContext?)
            = orginHandler.execute(editor, caret, dataContext)
        fun getActionId()
            = id
    }

    companion object {
        private var handlerInitialized = false
        private const val EDITOR_UNINDENT_SELECTION = "EditorUnindentSelection"
    }
}

private fun EditorActionManager.replaceHandler(handler: AbstractWriteActionHandler) {
    setActionHandler(handler.getActionId(), handler)

}
