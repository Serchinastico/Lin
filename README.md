<p align="center"><img src ="./readme/logo.png" /></p>

---------------

[![Build Status](https://travis-ci.org/Serchinastico/Lin.svg?branch=master)](https://travis-ci.org/Serchinastico/Lin)
[![codecov](https://codecov.io/gh/Serchinastico/Lin/branch/master/graph/badge.svg)](https://codecov.io/gh/Serchinastico/Lin)
[![jitpack](https://jitpack.io/v/Serchinastico/Lin.svg)](https://jitpack.io/#Serchinastico/Lin)
[![Lint tool: Lin](https://img.shields.io/badge/Lint_tool-lin-2e99e9.svg?style=flat)](https://github.com/Serchinastico/Lin)

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
    lintChecks 'com.github.serchinastico.lin:detectors:0.0.4'
    lintClassPath 'com.github.serchinastico.lin:dsl:0.0.4'
}
```

### Lin - DSL (Domain Specific Language)

If you want to write your own detectors just add the `dsl` and `annotations` modules to your linting project:

```groovy
dependencies {
    compileOnly 'com.github.serchinastico.lin:dsl:0.0.4'
    compileOnly 'com.github.serchinastico.lin:annotations:0.0.4'
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

The list of available quantifiers is:

```kotlin
val any                  // The default quantifier, if a rule matches any number of times then it's reported
val all                  // It should appear in every single appearance of the node
fun times(times: Int)    // Match the rule an exact number of "times"
fun atMost(times: Int)   // matches <= "times"
fun atLeast(times: Int)  // matches >= "times"
fun lessThan(times: Int) // matches < "times"
fun moreThan(times: Int) // matches > "times"
val none                 // No matches
```

### Storage

Lin detectors can store and retrieve information from a provided map. This is really useful if you have dependant rules where one of them might depend on the value of another, e.g. an activity class name having the same name as the layout it renders.

Because Lin uses backtracking on the process of finding the best match for rules **it's highly discouraged to store information by yourself**, intead you should use the `storage` property provided in the `suchThat` block.

```kotlin
{
    import {
        suchThat { node ->
            val importedString = node.importReference?.asRenderString() ?: return@suchThat false
            val importedLayout = KOTLINX_SYNTHETIC_VIEW_IMPORT
                .matchEntire(importedString)
                ?.groups
                ?.get(1)
                ?.value ?: return@suchThat false
            // Here we have access to the LinContext object that holds
            // a reference to a map of values where you can store
            // string values.
            params["Imported Layout"] = importedLayout
            it.isSyntheticViewImport
        }
    }

    expression {
        suchThat { node ->
            // We retrieve the information we stored previously
            // It's the same value we stored when the rule returned
            // true so we are sure it's the one we need.
            val importedLayout = params["Imported Layout"] ?: return@suchThat false
            val usedLayout = LAYOUT_EXPRESSION.matchEntire(node.asRenderString())
                ?.groups
                ?.get(1)
                ?.value ?: return@suchThat false
            return usedLayout != importedLayout
        }
    }
}
```

The `storage` property is just a `MutableMap<String, String>`. The matching algorithm takes care of keeping the map in a coherent state while doing the search so that you won't find values stored in failing rules. **All siblings and child nodes will see stored values.**

It's also important to keep in mind that Lin will try to match rules in any order. The most important implication is that even if you define a rule in a specific order Lin might find matches in the opposite:

```kotlin
{
    expression {
        suchThat {
            storage["node"] = it.asRenderString() 
            true
        }
    }

    expression {
        suchThat { "MyExpression" == storage["node"] }
    }
}
```

Even if the expression storing things in the storage is defined before, that order is not honored when looking for the best match of rules, so it might happen that `storage["node"]` is null.

### Lin - Testing

Internally, Lin uses a DSL for tests that makes a bit easier the simplest scenarios. You can use it by adding the dependency to your project:

```groovy
dependencies {
    testCompile 'com.github.serchinastico.lin:test:0.0.4'
    // You might still need to load the official Android Lint dependencies for tests
    testCompile 'com.android.tools.lint:lint:26.3.0'
    testCompile 'com.android.tools.lint:lint-tests:26.3.0'
    testCompile 'com.android.tools:testutils:26.3.0'
}
```

Creating a test with the `test` module is pretty easy, just look at an example:

```kotlin
class SomeDetectorTest : LintTest {
    // Specify the issue we are covering, in this case an issue created with Lin
    override val issue = SomeDetector.issue
    
    @Test
    fun inJavaClass_whenSomethingHappens_detectsNoErrors() {
        // `expect` can load multiple files to the test project
        expect(
            someSharedFile,
            """
                |package foo;
                |
                |import java.util.Date;
                |
                |class TestClass {
                |   public void main(String[] args) {}
                |}
            """.inJava    // Specify the language in which the file is written e.g. `inJava` or `inKotlin`
        ) toHave NoErrors /* Three possible values here:
                           *   > `NoErrors`               No expected reports
                           *   > `SomeWarning(fileName)`  Expect at least one warning in the specified file
                           *   > `SomeError(fileName)`    Expect at least one error in the specified file
                           */
    }
}
```

Lin tests are used with this very same DSL so you can take a look to the `detectors` module tests to see many more examples.

### Badge

Show the world you're using Lin.

[![Lint tool: Lin](https://img.shields.io/badge/Lint_tool-lin-2e99e9.svg?style=flat)](https://github.com/Serchinastico/Lin)

```md
[![Lint tool: Lin](https://img.shields.io/badge/Lint_tool-lin-2e99e9.svg?style=flat)](https://github.com/Serchinastico/Lin)
```

