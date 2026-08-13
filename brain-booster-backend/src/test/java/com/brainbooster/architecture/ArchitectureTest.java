package com.brainbooster.architecture;

import com.brainbooster.BrainBoosterBackendApplication;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packagesOf = BrainBoosterBackendApplication.class,
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule services_should_not_depend_on_spring_web =
            noClasses()
                    .that()
                    .haveSimpleNameEndingWith("Service")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web..",
                            "jakarta.servlet..")
                    .because(
                            "services should not depend on the HTTP/web layer");

    @ArchTest
    static final ArchRule controllers_should_not_depend_on_repositories =
            noClasses()
                    .that()
                    .haveSimpleNameEndingWith("Controller")
                    .should()
                    .dependOnClassesThat()
                    .haveSimpleNameEndingWith("Repository")
                    .because(
                            "controllers should delegate persistence operations to services");

    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
            noClasses()
                    .that()
                    .haveSimpleNameEndingWith("Service")
                    .should()
                    .dependOnClassesThat()
                    .haveSimpleNameEndingWith("Controller")
                    .because("services must not depend on the web layer");
}
