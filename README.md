<p align="center"><img src ="./readme/logo.png" /></p>

---------------

[![Build Status](https://travis-ci.org/Serchinastico/Lin.svg?branch=master)](https://travis-ci.org/Serchinastico/Lin)
[![codecov](https://codecov.io/gh/Serchinastico/Lin/branch/master/graph/badge.svg)](https://codecov.io/gh/Serchinastico/Lin)
[![jitpack](https://jitpack.io/v/Serchinastico/Lin.svg)](https://jitpack.io/#Serchinastico/Lin)

Lin is an Android Lint tool made simpler. It has two different goals:

1. To create a set of highly opinionated rules to apply to your Android projects.
2. To offer a Kotlin DSL to write your own rules in a much easier way.

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

### Lin-Rules

Add the rules module dependencies to your project and the dsl module as part of the lint classpath:

```groovy
dependencies {
    lintChecks 'com.serchinastico.lin:rules:0.0.1'
    lintClassPath 'com.serchinastico.lin:dsl:0.0.1'
}
```

### Lin-DSL

If you want to write your own rules just add the dsl and annotations modules to your linting project:

```groovy
dependencies {
    compileOnly 'com.serchinastico.lin:dsl:0.0.1'
    compileOnly 'com.serchinastico.lin:annotations:0.0.1'
}
```

## How to write your own rules

Lin offers a DSL (Domain Specific Language) to write your own rules easily. The API is focused on representing your rules as concisely as possible. Let's bisect an example of a rule to understand how it works:

```kotlin
@Rule
fun noElseInSwitchWithEnumOrSealed() = rule(
    // Define the issue:
    issue(
        // 1. What files should the rule check
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
     * function
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
