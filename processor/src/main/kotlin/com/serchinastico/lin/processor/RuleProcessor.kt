package com.serchinastico.lin.processor

import com.google.auto.service.AutoService
import com.serchinastico.lin.annotations.Rule
import com.serchinastico.lin.dsl.IssueBuilder
import com.serchinastico.lin.dsl.LinRule
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
@SupportedOptions(RuleProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
@SupportedAnnotationTypes("com.serchinastico.lin.annotations.Rule")
class RuleProcessor : AbstractProcessor() {

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

        val LIN_RULE_CLASS_NAME = ClassName("com.serchinastico.lin.dsl", "LinRule")
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

        val annotatedElements = roundEnvironment.getElementsAnnotatedWith(Rule::class.java)

        val generatedSourcesRoot: String = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME].orEmpty()
        if (ensureOutputDirectoryExists(generatedSourcesRoot)) return false

        annotatedElements
            .mapNotNull { it as? ExecutableElement }
            .forEach { element ->
                val returnTypeName = element.returnType.asTypeName()

                if (returnTypeName != LIN_RULE_CLASS_NAME) {
                    printError("${Rule::class.simpleName} annotation has to be applied to a function returning a ${LinRule::class.simpleName} instance.")
                    return false
                }

                val issueId = element.simpleName.toString().capitalize()
                val packageName = processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()

                val className = "${issueId}Detector"
                val ruleFile = FileSpec.builder(packageName, className)
                    .addImport("com.serchinastico.lin.dsl", "report")
                    .addType(
                        TypeSpec.classBuilder(className)
                            .superclass(DETECTOR_CLASS_NAME)
                            .addSuperinterface(UAST_SCANNER_CLASS_NAME)
                            .addCompanionObject {
                                addRuleProperty(element.simpleName.toString())
                                    .addIssueBuilderProperty()
                                    .addIssueProperty(className)
                            }
                            .addGetApplicableFilesFunction()
                            .addGetApplicableUastTypes()
                            .addCreateUastHandler()
                            .build()
                    )
                    .build()

                val kotlinFileObject =
                    processingEnv.filer.createResource(StandardLocation.SOURCE_OUTPUT, packageName, "$className.kt")
                val writer = kotlinFileObject.openWriter()
                ruleFile.writeTo(writer)
                writer.close()
            }

        return true
    }

    private fun TypeSpec.Builder.addCompanionObject(block: TypeSpec.Builder.() -> TypeSpec.Builder): TypeSpec.Builder =
        addType(TypeSpec.companionObjectBuilder().block().build())

    private fun TypeSpec.Builder.addRuleProperty(ruleText: String): TypeSpec.Builder =
        addProperty(
            PropertySpec.builder("rule", LinRule::class)
                .delegate(
                    CodeBlock.builder()
                        .beginControlFlow("lazy")
                        .add("$ruleText()")
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
                        .add("rule.issueBuilder")
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
                        .add("rule.issueBuilder.build($className::class)")
                        .endControlFlow()
                        .build()
                )
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
                .addCode("return rule.applicableTypes")
                .returns(LIST_CLASS_NAME.parameterizedBy(CLASS_CLASS_NAME.parameterizedBy(U_ELEMENT_OUT_CLASS_NAME)))
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
            .addFunction(
                FunSpec.builder("visitSwitchExpression")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        "node",
                        ClassName("org.jetbrains.uast", "USwitchExpression")
                    )
                    .addShouldReportFunction()
                    .build()
            )
            .addFunction(
                FunSpec.builder("visitCallExpression")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        "node",
                        ClassName("org.jetbrains.uast", "UCallExpression")
                    )
                    .addShouldReportFunction()
                    .build()
            )
            .addFunction(
                FunSpec.builder("visitField")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(
                        "node",
                        ClassName("org.jetbrains.uast", "UField")
                    )
                    .addShouldReportFunction()
                    .build()
            )
            .build()

    private fun FunSpec.Builder.addShouldReportFunction(): FunSpec.Builder =
        addCode(
            """
                |if (rule.shouldReport(node)) {
                |   context.report(issue)
                |}
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