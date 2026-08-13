package com.brainbooster.architecture;

import com.brainbooster.BrainBoosterBackendApplication;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@SuppressWarnings("unused")
@AnalyzeClasses(
        packagesOf = BrainBoosterBackendApplication.class,
        importOptions = ImportOption.DoNotIncludeTests.class)
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

    @ArchTest
    static final ArchRule services_should_not_access_security_context_directly =
            noClasses()
                    .that()
                    .haveSimpleNameEndingWith("Service")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.security.core.context.."
                    )
                    .because(
                            "services should obtain authenticated users through CurrentUserProvider");


    @ArchTest
    static final ArchRule rest_controllers_should_have_controller_suffix =
            classes()
                    .that()
                    .areAnnotatedWith(RestController.class)
                    .should()
                    .haveSimpleNameEndingWith("Controller")
                    .because(
                            "REST controllers should follow a consistent naming convention");

    @ArchTest
    static final ArchRule classes_with_service_suffix_should_be_annotated_with_service =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Service")
                    .should()
                    .beAnnotatedWith(Service.class)
                    .because(
                            "classes named as services should be Spring services");

    @ArchTest
    static final ArchRule repositories_should_be_interfaces =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Repository")
                    .should()
                    .beInterfaces()
                    .because(
                            "Spring Data repositories should be declared as interfaces");

    @ArchTest
    static final ArchRule repositories_should_not_depend_on_services =
            noClasses()
                    .that()
                    .haveSimpleNameEndingWith("Repository")
                    .should()
                    .dependOnClassesThat()
                    .haveSimpleNameEndingWith("Service")
                    .because(
                            "the persistence layer must not depend on the service layer");

    @ArchTest
    static final ArchRule user_services_should_not_depend_on_flashcardset_module =
            noClasses()
                    .that()
                    .resideInAPackage("com.brainbooster.user..")
                    .and()
                    .haveSimpleNameEndingWith("Service")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.brainbooster.flashcardset..")
                    .because(
                            "user services should not contain flashcard set responsibilities");

    @ArchTest
    static final ArchRule authorization_policies_should_reside_in_authorization_package =
            classes()
                    .that()
                    .haveSimpleNameEndingWith("Policy")
                    .should()
                    .resideInAPackage(
                            "com.brainbooster.security.authorization..")
                    .because(
                            "authorization policies should be centralized");

    @ArchTest
    static final ArchRule production_code_should_not_use_field_injection =
            noFields()
                    .should()
                    .beAnnotatedWith(Autowired.class)
                    .because("dependencies should be injected through constructors");
}
