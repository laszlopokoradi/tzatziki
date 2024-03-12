package io.nimbly.i18n.translation

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import io.nimbly.i18n.util.*
import java.util.*
import javax.swing.SwingUtilities

object TranslationManager {

    private val listeners: MutableList<TranslationListener> = mutableListOf()
    private var findUsages: Set<Pair<PsiElement, Int>>? = null

    fun registerListener(listener: TranslationListener) {
        listeners.add(listener)
    }

    fun translate(
        targetLanguage: String,
        sourceLanguage: String,
        text: String,
        format: EFormat,
        style: EStyle,
        origin: Origin?,
        project: Project?
    ): GTranslation? {

        val t = text.unescapeStyle(style)
        val translationText = t.unescapeFormat(format, false)

        val translation =
            if (format.preserveQuotes && translationText.surroundedWith("\n"))
                googleTranslate(targetLanguage, sourceLanguage, translationText.removeSurrounding("\""))
                    ?.apply { this.translated = this.translated.surround("\"")}
            else
                googleTranslate(targetLanguage, sourceLanguage, translationText)


        if (translation != null) {

            translation.translated =

                translation.translated
                    .replace("„", "\"")
                    .postTranslation(format)
                    .escapeStyle(style, Locale(sourceLanguage))

            val event = TranslationEvent(translation)

            updateListenersAfterTranslation(project, event)
        }

        if (origin?.element != null && origin.editor != null) {
            findUsages = null
            updateListenersAfterUsagesCollected(project)
            SwingUtilities.invokeLater {
                findUsages = findUsages(origin.element, origin.editor, GlobalSearchScope.allScope(origin.element.project))
                updateListenersAfterUsagesCollected(project)
            }
        }
        else {
            findUsages = null
            updateListenersAfterUsagesCollected(project)
        }


        return translation
    }

    private fun updateListenersAfterTranslation(project: Project?, event: TranslationEvent) {

        if (project != null) {
            DumbService.getInstance(project).smartInvokeLater {
                listeners.forEach { it.onTranslation(event) }
            }
        } else {
            listeners.forEach { it.onTranslation(event) }
        }
    }

    private fun updateListenersAfterUsagesCollected(project: Project?) {

        val usages = getUsages()
        if (project != null) {
            DumbService.getInstance(project).smartInvokeLater {
                listeners.forEach { it.onUsagesCollected(usages) }
            }
        } else {
            listeners.forEach { it.onUsagesCollected(usages) }
        }
    }

    fun getUsages(): Set<PsiElement> {
        return findUsages?.map { it.first }?.toSet() ?: emptySet()
    }
}

interface TranslationListener {
    fun onTranslation(event: TranslationEvent)
    fun onUsagesCollected(usages: Set<PsiElement>)
}

class TranslationEvent(
    val translation: GTranslation
)

class Origin(
    val element: PsiElement?,
    val editor: Editor?,
) {
    companion object {
        fun from(element: PsiElement?, editor: Editor?): Origin? {
            if (element == null && editor == null)
                return null
            return Origin(element, editor)
        }
    }
}
