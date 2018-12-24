package com.serchinastico.lin.processor

import com.google.auto.service.AutoService
import com.serchinastico.lin.annotations.Detector
import com.serchinastico.lin.dsl.IssueBuilder
import com.serchinastico.lin.dsl.LinDetector
import com.serchinastico.lin.dsl.LinVisitor
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation


@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(DetectorProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedAnnotationTypes("com.serchinastico.lin.annotations.Detector")
class DetectorProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        val LIN_DETECTOR_CLASS_NAME = ClassName("com.serchinastico.lin.dsl", "LinDetector")
        val ISSUE_CLASS_NAME = ClassName("com.android.tools.lint.detector.api", "Issue")
        val DETECTOR_CLASS_NAME = ClassName("com.android.tools.lint.detector.api", "Detector")
        val UAST_SCANNER_CLASS_NAME = ClassName("com.android.tools.lint.detector.api.Detector", "UastScanner")
        val U_ELEMENT_HANDLER_CLASS_NAME = ClassName("com.android.tools.lint.client.api", "UElementHandler")
        val U_ELEMENT_OUT_CLASS_NAME = WildcardTypeName.producerOf(ClassName("org.jetbrains.uast", "UElement"))
        val CLASS_CLASS_NAME = ClassName("java.lang", "Class")
        val LIST_CLASS_NAME = ClassName("kotlin.collections", "List")
        val ENUM_SET_CLASS_NAME = ClassName("java.util", "EnumSet")
        val SCOPE_CLASS_NAME = ClassName("com.android.tools.lint.detector.api", "Scope")
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        roundEnvironment ?: return false

        val annotatedElements = roundEnvironment.getElementsAnnotatedWith(Detector::class.java)

        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (ensureOutputDirectoryExists(generatedSourcesRoot)) return false

        annotatedElements
            .mapNotNull { it as? ExecutableElement }
            .forEach { element ->
                val returnTypeName = element.returnType.asTypeName()

                if (returnTypeName != LIN_DETECTOR_CLASS_NAME) {
                    printError("${Detector::class.simpleName} annotation has to be applied to a function returning a ${LinDetector::class.simpleName} instance.")
                    return false
                }

                val issueId = element.simpleName.toString().capitalize()
                val packageName = processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()

                val className = "${issueId}Detector"
                val detectorFile = FileSpec.builder(packageName, className)
                    .addImport("com.serchinastico.lin.dsl", "LinVisitor")
                    .addImport("com.serchinastico.lin.dsl", "report")
                    .addType(
                        TypeSpec.classBuilder(className)
                            .superclass(DETECTOR_CLASS_NAME)
                            .addSuperinterface(UAST_SCANNER_CLASS_NAME)
                            .addCompanionObject {
                                addDetectorProperty(element.simpleName.toString())
                                    .addIssueBuilderProperty()
                                    .addIssueProperty(className)
                            }
                            .addVisitorProperty()
                            .addDidReportFileVisitor()
                            .addGetApplicableFilesFunction()
                            .addGetApplicableUastTypes()
                            .addAfterCheckEachProject()
                            .addCreateUastHandler()
                            .build()
                    )
                    .build()

                val kotlinFileObject =
                    processingEnv.filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "$className.kt")
                val writer = kotlinFileObject.openWriter()
                detectorFile.writeTo(writer)
                writer.close()
            }

        return true
    }

    private fun TypeSpec.Builder.addCompanionObject(block: TypeSpec.Builder.() -> TypeSpec.Builder): TypeSpec.Builder =
        addType(TypeSpec.companionObjectBuilder().block().build())

    private fun TypeSpec.Builder.addDetectorProperty(detectorText: String): TypeSpec.Builder =
        addProperty(
            PropertySpec.builder("detector", LinDetector::class)
                .delegate(
                    CodeBlock.builder()
                        .beginControlFlow("lazy")
                        .add("$detectorText()")
                        .endControlFlow()
                        .build()
                )
                .build()
        )

    private fun TypeSpec.Builder.addIssueBuilderProperty(): TypeSpec.Builder =
        addProperty(
            PropertySpec.builder("issueBuilder", IssueBuilder::class)
                .delegate(
                    CodeBlock.builder()
                        .beginControlFlow("lazy")
                        .add("detector.issueBuilder")
                        .endControlFlow()
                        .build()
                )
                .build()
        )

    private fun TypeSpec.Builder.addIssueProperty(className: String): TypeSpec.Builder =
        addProperty(
            PropertySpec.builder("issue", ISSUE_CLASS_NAME)
                .delegate(
                    CodeBlock.builder()
                        .beginControlFlow("lazy")
                        .add("detector.issueBuilder.build($className::class)")
                        .endControlFlow()
                        .build()
                )
                .build()
        )

    private fun TypeSpec.Builder.addVisitorProperty(): TypeSpec.Builder =
        addProperty(
            PropertySpec.builder("projectVisitor", LinVisitor::class)
                .initializer("LinVisitor(detector)")
                .build()
        )

    private fun TypeSpec.Builder.addDidReportFileVisitor(): TypeSpec.Builder =
        addProperty(
            PropertySpec.builder("didReportWithFileVisitor", Boolean::class)
                .mutable(true)
                .initializer("false")
                .build()
        )

    private fun TypeSpec.Builder.addGetApplicableFilesFunction(): TypeSpec.Builder =
        addFunction(
            FunSpec.builder("getApplicableFiles")
                .addModifiers(KModifier.OVERRIDE)
                .addCode("return issueBuilder.scope")
                .returns(ENUM_SET_CLASS_NAME.parameterizedBy(SCOPE_CLASS_NAME))
                .build()
        )

    private fun TypeSpec.Builder.addGetApplicableUastTypes(): TypeSpec.Builder =
        addFunction(
            FunSpec.builder("getApplicableUastTypes")
                .addModifiers(KModifier.OVERRIDE)
                .addCode("return detector.applicableTypes")
                .returns(LIST_CLASS_NAME.parameterizedBy(CLASS_CLASS_NAME.parameterizedBy(U_ELEMENT_OUT_CLASS_NAME)))
                .build()
        )

    private fun TypeSpec.Builder.addAfterCheckEachProject(): TypeSpec.Builder =
        addFunction(
            FunSpec.builder("afterCheckEachProject")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    "context",
                    ClassName("com.android.tools.lint.detector.api", "Context")
                )
                .addCode(
                    """
                        |val reportedNodes = projectVisitor.reportedNodes
                        |if (!didReportWithFileVisitor && reportedNodes.isNotEmpty()) {
                        |   context.report(issue, reportedNodes.first())
                        |}
                    """.trimMargin()
                )
                .build()
        )

    private fun TypeSpec.Builder.addCreateUastHandler(): TypeSpec.Builder =
        addFunction(
            FunSpec.builder("createUastHandler")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(
                    "context",
                    ClassName("com.android.tools.lint.detector.api", "JavaContext")
                )
                .addCode(
                    "return %L",
                    getElementHandlerType()
                )
                .returns(
                    ClassName("com.android.tools.lint.client.api", "UElementHandler").copy(nullable = true)
                )
                .build()
        )

    private fun getElementHandlerType(): TypeSpec =
        TypeSpec.anonymousClassBuilder()
            .superclass(U_ELEMENT_HANDLER_CLASS_NAME)
            .addFunction(
                FunSpec.builder("visitFile")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        "node",
                        ClassName("org.jetbrains.uast", "UFile")
                    )
                    .addShouldReportFunction()
                    .build()
            )
            .build()

    private fun FunSpec.Builder.addShouldReportFunction(): FunSpec.Builder =
        addCode(
            """ |val fileVisitor = LinVisitor(detector)
                |node.accept(fileVisitor)
                |val reportedNodes = fileVisitor.reportedNodes
                |if (reportedNodes.isNotEmpty()) {
                |   context.report(issue, reportedNodes.first())
                |   didReportWithFileVisitor = true
                |}
                |projectVisitor += fileVisitor
            """.trimMargin()
        )

    private fun ensureOutputDirectoryExists(generatedSourcesRoot: String): Boolean =
        if (generatedSourcesRoot.isEmpty()) {
            printError("Can't find the target directory for generated Kotlin files.")
            true
        } else {
            false
        }

    private fun printError(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
    }
}