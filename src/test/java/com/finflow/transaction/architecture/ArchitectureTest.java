package com.finflow.transaction.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * Arxitektura qoidalari kod bilan bir joyda yashaydi.
 * Code review'da "sen repository'ni controller'ga chaqiribsan" deb aytish kerak emas —
 * CI aytadi.
 */
@AnalyzeClasses(packages = "com.finflow.transaction",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule controllersMustNotTouchRepositories =
            noClasses().that().resideInAPackage("..controller..")
                    .should().dependOnClassesThat().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule domainMustNotDependOnSpringWeb =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework.web..");

    @ArchTest
    static final ArchRule domainMustNotDependOnDto =
            noClasses().that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..dto..");

    /** Pul hech qachon double/float'da saqlanmaydi. Bu qoida buzilsa CI qizaradi. */
    @ArchTest
    static final ArchRule noDoubleInDomain =
            noFields().that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                    .should().haveRawType(double.class)
                    .because("Monetary values must use BigDecimal");

    @ArchTest
    static final ArchRule noFloatInDomain =
            noFields().that().areDeclaredInClassesThat().resideInAPackage("..domain..")
                    .should().haveRawType(float.class)
                    .because("Monetary values must use BigDecimal");

    @ArchTest
    static final ArchRule repositoriesMustNotDependOnServices =
            noClasses().that().resideInAPackage("..repository..")
                    .should().dependOnClassesThat().resideInAPackage("..service..");
}
