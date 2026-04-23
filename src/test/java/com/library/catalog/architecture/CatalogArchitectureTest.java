package com.library.catalog.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class CatalogArchitectureTest {

    private static final String BASE_PKG = "com.library.catalog";

    private static JavaClasses classes;

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PKG);
    }

    @Test
    void domain_does_not_depend_on_spring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..");
        rule.check(classes);
    }

    @Test
    void domain_does_not_depend_on_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PKG + ".infrastructure..");
        rule.check(classes);
    }

    @Test
    void domain_does_not_depend_on_interfaces_layer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PKG + ".interfaces..");
        rule.check(classes);
    }

    @Test
    void application_does_not_depend_on_infrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".application..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PKG + ".infrastructure..");
        rule.check(classes);
    }

    @Test
    void application_does_not_depend_on_interfaces_layer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".application..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PKG + ".interfaces..");
        rule.check(classes);
    }

    @Test
    void interfaces_layer_does_not_access_persistence_directly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".interfaces..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PKG + ".infrastructure.persistence..");
        rule.check(classes);
    }

    @Test
    void infrastructure_does_not_depend_on_interfaces_layer() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE_PKG + ".infrastructure..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE_PKG + ".interfaces..");
        rule.check(classes);
    }
}
