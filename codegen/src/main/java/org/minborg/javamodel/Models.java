package org.minborg.javamodel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class Models {

    private Models() {
    }

    // General Models for arity

    // Todo: consider removing this interface
    public sealed interface OrMore<T>
            permits AbstractOrMore, OneOrMore, ZeroOrMore {
        List<T> all();
    }

    public sealed interface OneOrMore<T> extends OrMore<T> {
        T first();

        List<T> rest();

        static <T> OneOrMore<T> of(List<? extends T> list) {
            return new OneOrMoreImpl<>(list);
        }
    }

    public sealed interface ZeroOrMore<T> extends OrMore<T> {
        static <T> ZeroOrMore<T> of(List<? extends T> list) {
            return new ZeroOrMoreImpl<>(list);
        }
    }

    // Model support for (one of) Enums
    public interface HasName {
        String name();
    }

    interface Model {}

    // JLS:  https://docs.oracle.com/javase/specs/jls/se20/html/jls-7.html#jls-7.3


    // 7.3. Compilation Units

    public sealed interface CompilationUnit extends Model
            permits OrdinaryCompilationUnit, ModularCompilationUnit {
        ZeroOrMore<ImportDeclaration> importDeclarations();
    }

    public record OrdinaryCompilationUnit(
            Optional<PackageDeclaration> packageDeclaration,
            ZeroOrMore<ImportDeclaration> importDeclarations,
            ZeroOrMore<TopLevelClassOrInterfaceDeclaration> topLevelClassOrInterfaceDeclarations
    ) implements CompilationUnit, Model {
    }

    public record ModularCompilationUnit(
            ZeroOrMore<ImportDeclaration> importDeclarations,
            ModuleDeclaration moduleDeclaration
    ) implements CompilationUnit, Model {
    }

    // 7.4. Package Declarations

    public record PackageDeclaration(
            ZeroOrMore<PackageModifier> packageModifiers,
            OneOrMore<Identifier> identifiers
    ) implements Model {
    }

    public record PackageModifier(Annotation annotation)
            implements Model {
    }


    // 7.5. Import Declarations

    public sealed interface ImportDeclaration
            extends Model
            permits SingleTypeImportDeclaration,
            TypeImportOnDemandDeclaration,
            SingleStaticImportDeclaration,
            StaticImportOnDemandDeclaration {}

    public record SingleTypeImportDeclaration(TypeName typeName)
            implements ImportDeclaration, Model {}

    public record TypeImportOnDemandDeclaration(PackageOrTypeName typeName)
            implements ImportDeclaration, Model {}

    public record SingleStaticImportDeclaration(TypeName typeName, Identifier identifier)
            implements ImportDeclaration, Model {}

    public record StaticImportOnDemandDeclaration(TypeName typeName)
            implements ImportDeclaration, Model {}


    // 7.6. Top Level Class and Interface Declarations

    public sealed interface TopLevelClassOrInterfaceDeclaration
            extends Model
            permits ClassDeclaration,
            InterfaceDeclaration {
    }

    public sealed interface ClassDeclaration
            extends Model
            permits NormalClassDeclaration,
            EnumDeclaration,
            RecordDeclaration {
    }

    public record NormalClassDeclaration(
            ZeroOrMore<ClassModifier> classModifiers,
            TypeIdentifier typeIdentifier,
            Optional<TypeParameters> typeParameters,
            Optional<ClassExtends> classExtends,
            Optional<ClassImplements> classImplements,
            Optional<ClassPermits> classPermits,
            ClassBody classBody)
    implements ClassDeclaration, Model {}


    sealed interface ClassModifier
         extends Model
         permits Annotation, ClassModifierKeywords{}

    // (one of)
    public enum ClassModifierKeywords implements HasName, ClassModifier, Model {
        PUBLIC,
        PROTECTED,
        PRIVATE,
        ABSTRACT,
        STATIC,
        FINAL,
        SEALED,
        NON_SEALED("non-sealed"),
        STRICTFP;

        private final String name;

        ClassModifierKeywords(String name) {
            this.name = name;
        }

        ClassModifierKeywords() {
            this.name = toString().toLowerCase(Locale.ROOT);
        }
    }

    // 8.1.2. Generic Classes and Type Parameters

    public record TypeParameters(
            TypeParameterList typeParameterList
    ) implements Model {}

    public record TypeParameterList(
            OneOrMore<TypeParameter> typeParameters
    ) implements Model {}

    public record TypeParameter(
            ZeroOrMore<TypeParameterModifier> typeParameterModifiers,
            TypeIdentifier typeIdentifier,
            Optional<TypeBound> typeBound
    ) implements Model {}

    TypeParameterModifier()


    // Expressions

    interface Expression extends Model {}


    // 9.1. Interface Declarations

    public sealed interface InterfaceDeclaration permits
            NormalInterfaceDeclaration,
            AnnotationInterfaceDeclaration {}

    public record NormalInterfaceDeclaration()


    // 14.2. Blocks

    public record Block(Optional<BlockStatements> blockStatements)
            implements Model, Statement {
    }

    public record BlockStatements(OneOrMore<BlockStatement> blockStatements)
        implements Model, Statement {
    }

    public sealed interface BlockStatement
        permits LocalClassOrInterfaceDeclaration,
                LocalVariableDeclarationStatement,
                Statement {}

    // 14.3. Local Class and Interface Declarations

    public sealed interface LocalClassOrInterfaceDeclaration
            permits ClassDeclaration,
            NormalInterfaceDeclaration {
    }



    // 14.5. Statements

    interface Statement extends Model {}

    public record IfThenStatement(Expression expression, Statement statement)
            implements Statement, Model {
    }

    public record IfThenElseStatement(Expression expression,
                                      StatementNoShortIf statementNoShortIf,
                                      Statement statement)
            implements Statement, Model {
    }

    public record IfThenElseStatementNoShortIf(Expression expression,
                                               StatementNoShortIf statementNoShortIf,
                                               StatementNoShortIf elseStatementNoShortIf)
            implements Statement, Model {

    }

    public interface StatementNoShortIf

        StatementWithoutTrailingSubstatement
                LabeledStatementNoShortIf
                IfThenElseStatementNoShortIf
                WhileStatementNoShortIf
                ForStatementNoShortIf


    public interface StatementWithoutTrailingSubstatement
            extends Expression, Model {}

                EmptyStatement
                ExpressionStatement
                AssertStatement
                SwitchStatement
                DoStatement
                BreakStatement
                ContinueStatement
                ReturnStatement
                SynchronizedStatement
                ThrowStatement
                TryStatement
                YieldStatement

    //


    // Implementations

    sealed static abstract class AbstractOrMore<T>
            implements OrMore<T> {

        protected final List<T> all;

        AbstractOrMore(List<? extends T> all) {
            this.all = new ArrayList<>(all);
        }

        @Override
        public List<T> all() {
            return Collections.unmodifiableList(all);
        }
    }

    static final class OneOrMoreImpl<T>
            extends AbstractOrMore<T>
            implements OneOrMore<T> {

        OneOrMoreImpl(List<? extends T> all) {
            super(requireListNonEmpty(all));
        }

        @Override
        public T first() {
            return all.getFirst();
        }

        @Override
        public List<T> rest() {
            return Collections.unmodifiableList(all.subList(1, all.size()));
        }

        private static <T> List<? extends T> requireListNonEmpty(List<? extends T> list) {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("List was empty: " + list);
            }
            return list;
        }

    }

    static final class ZeroOrMoreImpl<T>
            extends AbstractOrMore<T>
            implements ZeroOrMore<T> {

        ZeroOrMoreImpl(List<? extends T> all) {
            super(all);
        }
    }

}
