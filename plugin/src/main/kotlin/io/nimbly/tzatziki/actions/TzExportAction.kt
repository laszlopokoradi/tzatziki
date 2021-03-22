package io.nimbly.tzatziki.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.refactoring.suggested.startOffset
import com.intellij.testFramework.writeChild
import io.nimbly.tzatziki.pdf.*
import io.nimbly.tzatziki.psi.getModule
import io.nimbly.tzatziki.util.*
import org.jetbrains.plugins.cucumber.psi.*
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes.*
import org.jetbrains.plugins.cucumber.psi.impl.*
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference
import java.io.ByteArrayOutputStream

class TzExportAction : TzAction(), DumbAware {

    override fun actionPerformed(event: AnActionEvent) {

        //FIXME : Format stuff available from file tree contextual menu
        //FIXME : Add export to pdf to  file tree contextual menu
        //FIXME : Bug when file ends with

        //TODO : Manage triple quotes ?

        //TODO Later : Set a parameter to decide whether or not to print the comments
        //TODO Later : Traduire les mots clef au moment d'editer

        val file = event.getData(CommonDataKeys.PSI_FILE) ?: return
        val project = event.getData(CommonDataKeys.PROJECT) ?: return
        if (file !is GherkinFile) return
        val module = file.getModule() ?: return
        val outputDirectory = CompilerPaths.getModuleOutputDirectory(module, true) ?: return

        // Prepare pdf generator
        val generator = PdfBuilder(
            PdfStyle(
                bottomCenter = file.name,
                bodyFontSize = "25px"),
            TableOfContents())

        // Customize styles
        generator.style.contentStyle = //language=CSS
            """
                * { font-size: 16px; margin: 0 0 0 0 }
                p { margin: 0 }

                table { margin-top: 10px; margin-left: 10px; margin-right: 10px;
                    max-width: 100%; }
                table, th, td {  
                    font-size: 14px; 
                    vertical-align: top; 
                    border: 1px solid midnightblue;  
                    border-collapse: collapse;  
                }  
                th, td { padding: 5px; white-space: break-spaces; }
                th { color: chocolate }
                
                div { display: inline-block; }
                
                .feature { margin-left: 5px; }
                .featureTitle { font-size: 24px; margin-bottom: 10px; }
                .featureHeader { margin-left: 5px; font-weight: bolder }
                
                .rule { margin-left: 10px; }
                .ruleTitle { font-size: 24px; margin-bottom: 10px; }
                
                .scenario { margin-left: 15px; }
                .scenarioTitle { font-size: 20px; border-bottom: 10px;  }
                
                .step { margin-left: 20px; }
                .stepParameter { color: chocolate; font-weight: bolder }
                
                .examples { margin-left: 20px; }
                .stepKeyword { color: grey; }
                .comment { color: grey}
                
                """.trimIndent()

        // Build as Html
        val visitor = TzatizkiVisitor(generator)
        file.accept(visitor)
        visitor.closeAllTags()

        // Generate Pdf
        val output = ByteArrayOutputStream()
        buildPdf(generator, output)

        // Create file and open it
        val newFile = outputDirectory.writeChild("${file.name}.pdf", output.toByteArray())
        OpenFileDescriptor(project, newFile).navigate(true)
    }

    override fun update(event: AnActionEvent) {
        super.update(event)
        if (event.presentation.isVisible) {
            event.presentation.isEnabled = true
        }
    }

    override fun isDumbAware() = true

    private class TzatizkiVisitor(val generator: PdfBuilder): GherkinElementVisitor(), PsiRecursiveVisitor {

        private val stackTags = mutableListOf<String>()
        private val context = mutableListOf<PsiElement>()
        private var stepParams: List<TextRange>? = null

        override fun visitElement(elt: PsiElement) {

            ProgressIndicatorProvider.checkCanceled()

            fun append() {
                if (elt !is LeafPsiElement) return
                if (context.isRow() && elt.text == "|") return
                if (elt.elementType == STEP_PARAMETER_BRACE) return

                if (elt.elementType == FEATURE_KEYWORD || context.isFeature())
                    return span("featureTitle") { append(elt.text) }

                if (elt.elementType == RULE_KEYWORD || context.isRule())
                    return span("ruleTitle") { append(elt.text) }

                if (elt.elementType == SCENARIO_KEYWORD || elt.elementType == SCENARIO_OUTLINE_KEYWORD || context.isScenario())
                    return span("scenarioTitle") { append(elt.text) }

                if (elt.elementType == STEP_KEYWORD)
                    return span("stepKeyword") { append(elt.text) }

                if (context.isStep() && stepParams?.isNotEmpty() == true) {

                    val shiftedSlices = stepParams!!
                        .filter { it.startOffset > elt.startOffset }
                        .map { it.shiftLeft(elt.startOffset) }

                    elt.text.shopUp(shiftedSlices)
                        .forEach {
                            if (it.isInterRange)
                                append(it.text)
                            else
                                span("stepParameter") { append(it.text) }
                        }
                    return
                }

                append(elt.text)
            }

            append()

            context.push(elt)
            elt.acceptChildren(this)
            context.pop()
        }

        override fun visitWhiteSpace(space: PsiWhiteSpace) {
            if (!context.isTable()) {
                append(space.text)
            }
            super.visitElement(space)
        }

        override fun visitFeature(feature: GherkinFeature) {
            p("feature") { super.visitFeature(feature) }

            if (feature != (feature.parent as GherkinFile).features.last())
                generator.breakPage()
        }

        override fun visitRule(rule: GherkinRule?)
            = p("rule") { super.visitRule(rule) }

        override fun visitFeatureHeader(header: GherkinFeatureHeaderImpl)
            = p("featureHeader") { super.visitFeatureHeader(header) }

        override fun visitScenarioOutline(outline: GherkinScenarioOutline)
            = nobreak { p("scenario") { super.visitScenarioOutline(outline) } }

        override fun visitScenario(scenario: GherkinScenario)
            = nobreak { p("scenario") { super.visitScenario(scenario) } }

        override fun visitStep(step: GherkinStep)
            = p("step") {
                stepParams = loadStepParams(step)
                super.visitStep(step)
                stepParams = null
            }

        override fun visitStepParameter(gherkinStepParameter: GherkinStepParameterImpl?)
            = span("stepParameter") { super.visitStepParameter(gherkinStepParameter) }

        override fun visitExamplesBlock(block: GherkinExamplesBlockImpl)
            = nobreak { p("examples") { super.visitExamplesBlock(block) } }

        override fun visitTable(table: GherkinTableImpl)
            = nobreak { tag("table") { super.visitTable(table) } }

        override fun visitTableHeaderRow(row: GherkinTableHeaderRowImpl)
            = tag("tr") { super.visitTableHeaderRow(row) }

        override fun visitTableRow(row: GherkinTableRowImpl)
            = tag("tr") { super.visitTableRow(row) }

        override fun visitGherkinTableCell(cell: GherkinTableCell)
            = tag(if (context.isHeader()) "th" else "td") {
                tag("span") {
                    super.visitGherkinTableCell(cell)
                }
            }

        override fun visitComment(comment: PsiComment) {
            //Do not export comments
        }

        private fun append(string: String): TzatizkiVisitor {
            generator.append(string.escape())
            return this
        }

        private fun span(clazz: String, function: () -> Unit) {
            generator.append("<span class='$clazz'>")
            stackTags.push("span")
            function()
            close("span")
        }

        fun tag(tag: String, function: () -> Unit) {
            generator.append("<$tag>")
            stackTags.push(tag)
            function()
            close(tag)
        }

        fun p(clazz: String, function: () -> Unit) {
            generator.append("<p class='$clazz'>")
            stackTags.push("p")
            function()
            close("p")
        }

        private fun nobreak(function: () -> Unit) {
            generator.paragraphStarts()
            function()
            generator.paragraphEnds()
        }

        private fun close(tag: String? = null): TzatizkiVisitor {
            val pop = stackTags.pop()
            if (tag != null && tag != pop)
                throw Exception("HTML tag malformed!")
            generator.append("</$pop>")
            return this
        }

        fun closeAllTags() {
            while (stackTags.isNotEmpty()) {
                close()
            }
        }
    }
}


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

private fun <E> MutableList<E>.isFeature() = peek() is GherkinFeature
private fun <E> MutableList<E>.isRule() = peek() is GherkinRule
private fun <E> MutableList<E>.isScenario() = peek() is GherkinScenario || peek() is GherkinScenarioOutline
private fun <E> MutableList<E>.isStep() = peek() is GherkinStep

private fun <E> MutableList<E>.isTable() = peek() is GherkinTable
private fun <E> MutableList<E>.isHeader() = peek() is GherkinTableHeaderRowImpl
private fun <E> MutableList<E>.isCell() = peek() is GherkinTableCell
private fun <E> MutableList<E>.isRow() = peek() is GherkinTableRow
