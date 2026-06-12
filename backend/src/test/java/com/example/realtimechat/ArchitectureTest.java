package com.example.realtimechat;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.example.realtimechat",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllers_should_be_spring_controllers =
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().beAnnotatedWith(RestController.class)
                    .orShould().beAnnotatedWith(Controller.class);

    @ArchTest
    static final ArchRule controllers_should_not_depend_on_repositories =
            noClasses()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");

    @ArchTest
    static final ArchRule services_should_not_depend_on_controllers =
            noClasses()
                    .that().haveSimpleNameEndingWith("Service")
                    .should().dependOnClassesThat().haveSimpleNameEndingWith("Controller");

    @ArchTest
    static final ArchRule domain_modules_should_not_depend_on_websocket_adapter =
            noClasses()
                    .that().resideInAnyPackage(
                            "..auth..",
                            "..user..",
                            "..conversation..",
                            "..message..",
                            "..presence.."
                    )
                    .should().dependOnClassesThat().resideInAnyPackage("..websocket..");
}
