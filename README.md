<p align="center"><img src ="./readme/logo.png" /></p>

---------------

[![Build Status](https://travis-ci.org/Serchinastico/Lin.svg?branch=master)](https://travis-ci.org/Serchinastico/Lin)
[![codecov](https://codecov.io/gh/Serchinastico/Lin/branch/master/graph/badge.svg)](https://codecov.io/gh/Serchinastico/Lin)
[![jitpack](https://jitpack.io/v/Serchinastico/Lin.svg)](https://jitpack.io/#Serchinastico/Lin)

Lin is an Android Lint tool made simpler. It has two different goals:

1. To create a set of highly opinionated detectors to apply to your Android projects.
2. To offer a Kotlin DSL to write your own detectors in a much easier way.

## How to use

Add the JitPack repository to your build file:

```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Lin - Detectors

Add the `detectors` module dependencies to your project and the `dsl` module as part of the lint classpath:

```groovy
dependencies {
    lintChecks 'com.serchinastico.lin:detectors:0.0.1'
    lintClassPath 'com.serchinastico.lin:dsl:0.0.1'
}
```

### Lin - DSL (Domain Specific Language)

If you want to write your own detectors just add the `dsl` and `annotations` modules to your linting project:

```groovy
dependencies {
    compileOnly 'com.serchinastico.lin:dsl:0.0.1'
    compileOnly 'com.serchinastico.lin:annotations:0.0.1'
}
```

## How to write your own detectors

Lin offers a DSL (Domain Specific Language) to write your own detectors easily. The API is focused on representing your rules as concisely as possible. Let's bisect an example of a detector to understand how it works:

```kotlin
@Detector
fun noElseInSwitchWithEnumOrSealed() = detector(
    // Define the issue:
    issue(
        // 1. What files should the detector check
        Scope.JAVA_FILE_SCOPE,
        // 2. A brief description of the issue
        "There should not be else/default branches on a switch statement checking for enum/sealed class values",
        // 3. A more in-detail explanation of why are we detecting the issue
        "Adding an else/default branch breaks extensibility because it won't let you know if there is a missing " +
                "implementation when adding new types to the enum/sealed class",
        // The category this issue falls into
        Category.CORRECTNESS
    )
) {
    /* The rule definition using the DSL. Define the
     * AST node you want to look for and include a
     * suchThat definition returning true when you want 
     * your rule to report an issue.
     * The best way to see what nodes you have
     * available is by using your IDE autocomplete
     * function.
    */
    switch {
        suchThat { node ->
            val classReferenceType = node.expression?.getExpressionType() ?: (return@suchThat false)

            if (!classReferenceType.isEnum && !classReferenceType.isSealed) {
                return@suchThat false
            }

            node.clauses.any { clause -> clause.isElseBranch }
        }
    }
}
```

### Quantifiers

You can specify your rules using quantifiers, that is, numeric restrictions to how many times you are expecting a specific rule to appear in order to be reported.

```kotlin
@Detector
fun noMoreThanOneGsonInstance() = detector(
    issue(
        Scope.JAVA_FILE_SCOPE,
        "Gson should only be initialized only once",
        """Creating multiple instances of Gson may hurt performance and it's a common mistake to instantiate it for
            | simple serialization/deserialization. Use a single instance, be it with a classic singleton pattern or
            | other mechanism your dependency injector framework provides. This way you can also share the common
            | type adapters.
        """.trimMargin(),
        Category.PERFORMANCE
    ),
    // We can use anyOf to report if any of the rules
    // included is found.
    anyOf(
        // This rule will only report if more than one
        // file has any call expression matching the 
        // suchThat predicate.
        file(moreThan(1)) { callExpression { suchThat { it.isGsonConstructor } } },
        // On the other hand, this rule will only 
        // report if there is any file with more than
        // one call expression matching the suchThat
        // predicate.
        file { callExpression(moreThan(1)) { suchThat { it.isGsonConstructor } } }
    )
)
```