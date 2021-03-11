package io.nimbly.tzatziki.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import com.intellij.util.DocumentUtil
import org.jetbrains.plugins.cucumber.psi.*

fun Editor.findTable(offset: Int): GherkinTable? {
    val file = getFile() ?: return null

    val adjustedOffset =
        when (offset) {
            getLineStartOffset() -> offset+2
            getLineStartOffset()+1 -> offset+1
            getLineEndOffset() -> offset-1
            else -> offset
        }

    val element = file.findElementAt(adjustedOffset) ?: return null
    return PsiTreeUtil.getContextOfType(element, GherkinTable::class.java)
}

fun Editor.getFile(): PsiFile? {
    val project = project ?: return null
    return PsiDocumentManager.getInstance(project).getPsiFile(document)
}

fun Editor.navigateInTableWithEnter(offset: Int = caretModel.offset): Boolean {

    val row = getTableRowAt(offset) ?: return false
    val colIdx = getTableColumnIndexAt(offset) ?: return false
    val next = row.next() ?: return false
    if (next.psiCells.size <= colIdx) return false

    val cell = next.psiCells[colIdx]
    val pipe = cell.previousPipe() ?: return false

    caretModel.moveToOffset(pipe.textOffset +2)
    return true
}

fun Editor.addTableRow(offset: Int = caretModel.offset): Boolean {

    val colIdx = getTableColumnIndexAt(offset) ?: return false
    val table = findTable(offset) ?: return false
    val row = getTableRowAt(offset) ?: return false

    val insert = offset == getLineEndOffset(offset)

    ApplicationManager.getApplication().runWriteAction {

        val newRow = row.addRowAfter()

        var newCaret = newRow.textOffset + 1
        if (!insert)
            newCaret += colIdx * 2
        caretModel.moveToOffset(newCaret)

        CodeStyleManager.getInstance(project!!).reformatText(
            table.containingFile,
            table.textRange.startOffset, table.textRange.endOffset
        )

        caretModel.moveToOffset(caretModel.offset + 1)
    }

    return true
}

fun Editor.navigateInTableWithTab(way: Boolean, editor: Editor, offset: Int = editor.caretModel.offset): Boolean {

    val table = findTable(offset) ?: return false
    val row = getTableRowAt(offset) ?: return false
    val file = getFile() ?: return false
    val element = file.findElementAt(offset) ?: return false

    fun goRight() : Boolean {
        var el: PsiElement? =
            if (element is GherkinTableCell)
                element
            else if (element is LeafPsiElement && element.parent is GherkinTableCell)
                element.parent
            else if (element is LeafPsiElement && element.parent is GherkinTableRow)
                element.nextSibling
            else if (element is LeafPsiElement && element.parent is GherkinTable)
                element.nextSibling.firstChild
            else if (element is LeafPsiElement && element.nextSibling is GherkinTable)
                element.nextSibling.firstChild.firstChild
            else
                element.nextSibling

        var pipe: PsiElement? = null
        while (el != null) {
            if (el is LeafPsiElement && el.elementType == GherkinTokenTypes.PIPE) {
                pipe = el
                break
            }
            el = el.nextSibling
        }

        if (pipe == null)
            return false

        val target =
            run {
                val off = pipe!!.textOffset + 2
                if (off > editor.document.textLength)
                    return true
                if (editor.document.getLineNumber(offset) != editor.document.getLineNumber(off)) {
                    val nextRow = row.next()
                        ?: table.allRows().firstOrNull()!!
                    nextRow.psiCells.first().textOffset
                } else {
                    off
                }
            }

        editor.caretModel.moveToOffset(target)
        return true
    }

    fun goLeft() : Boolean {

        var el: PsiElement? =
            if (element is GherkinTableCell)
                element
            else if (element is LeafPsiElement && element.parent is GherkinTableCell)
                element.parent
            else if (element is LeafPsiElement && element.parent is GherkinTableRow)
                element.prevSibling
            else if (element is LeafPsiElement && element.prevSibling is GherkinTableRow)
                row.lastChild ?: return false
            else if (element is LeafPsiElement && element.prevSibling is GherkinFeature)
                row.lastChild
            else if (element is LeafPsiElement && element.parent is GherkinTable)
                element.parent.lastChild
            else
                element.prevSibling

        var pipe: PsiElement? = null
        while (el != null) {
            if (el is LeafPsiElement && (el as LeafPsiElement).elementType == GherkinTokenTypes.PIPE) {
                el = el!!.prevSibling
                break
            }
            el = el!!.prevSibling
        }
        while (el != null) {
            if (el is LeafPsiElement && (el as LeafPsiElement).elementType == GherkinTokenTypes.PIPE) {
                pipe = el
                break
            }
            el = el!!.prevSibling
        }

        val target =
            if (pipe == null) {
                val nextRow = row.previous()
                    ?: table.allRows().lastOrNull() !!
                nextRow.psiCells.last().textOffset
            }
            else {
                val off = pipe!!.textOffset + 2
                if (off > editor.document.textLength)
                    return true
                if (editor.document.getLineNumber(offset) != editor.document.getLineNumber(off)) {
                    val nextRow = row.next()
                        ?: table.allRows().firstOrNull()!!
                    nextRow.psiCells.first().textOffset
                } else {
                    off
                }
            }

        editor.caretModel.moveToOffset(target)
        return true
    }

    return if (way) goRight() else goLeft()
}

fun Editor.getTableColumnIndexAt(offset: Int): Int? {
    val file = getFile() ?: return null
    var element = file.findElementAt(offset) ?: return null
    if (element.parent is GherkinTableCell)
        element = element.parent

    var col = -1
    var el: PsiElement? = element
    while (el != null) {
        if (el.elementType == GherkinTokenTypes.PIPE)
            col++
        el = el.prevSibling
    }

    if (col<0 && element.prevSibling is GherkinTableRow) {
        col = element.prevSibling.children.count { it is GherkinTableCell }
    }

    return col
}

fun Editor.getTableRowAt(offset: Int): GherkinTableRow? {

    val file = getFile() ?: return null
    val element = file.findElementAt(
        if (getLineEndOffset() == offset) offset-1 else offset)

    var row = PsiTreeUtil.getContextOfType(element, GherkinTableRow::class.java)
    if (row == null && element?.nextSibling is GherkinTableRow)
        row = element.nextSibling as GherkinTableRow?
    if (row == null && element?.nextSibling is GherkinTable)
        row = element.nextSibling.firstChild as GherkinTableRow?

    return row
}

fun Editor.getLineEndOffset(offset: Int = caretModel.offset): Int {
    return DocumentUtil.getLineEndOffset(offset, document)
}

fun Editor.getLineStartOffset(offset: Int = caretModel.offset): Int {
    return DocumentUtil.getLineStartOffset(offset, document)
}
