/*
 * Copyright 2021, TeamDev. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and/or binary forms, with or without
 * modification, must retain the above copyright notice and the following
 * disclaimer.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package io.spine.kanban.codegen;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.CodeBlock;
import io.spine.protodata.Field;
import io.spine.protodata.File;
import io.spine.protodata.FilePath;
import io.spine.protodata.ProtobufSourceFile;
import io.spine.protodata.Type;
import io.spine.protodata.TypeName;
import io.spine.protodata.language.CommonLanguages;
import io.spine.protodata.renderer.Renderer;
import io.spine.protodata.renderer.SourceSet;
import io.spine.validate.ConstraintViolation;
import io.spine.validate.ValidationException;
import io.spine.validation.BinaryOperation;
import io.spine.validation.CompositeRule;
import io.spine.validation.MessageValidation;
import io.spine.validation.Rule;
import io.spine.validation.RuleOrComposite;
import io.spine.validation.Sign;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.BinaryOperator;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.kanban.codegen.Poet.lines;
import static io.spine.protodata.Ast.javaFile;
import static io.spine.protodata.Type.KindCase.PRIMITIVE;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.validation.BinaryOperation.AND;
import static io.spine.validation.BinaryOperation.OR;
import static io.spine.validation.BinaryOperation.XOR;
import static io.spine.validation.Sign.EQUAL;
import static io.spine.validation.Sign.GREATER_OR_EQUAL;
import static io.spine.validation.Sign.GREATER_THAN;
import static io.spine.validation.Sign.LESS_OR_EQUAL;
import static io.spine.validation.Sign.LESS_THAN;
import static io.spine.validation.Sign.NOT_EQUAL;
import static java.lang.String.format;

/**
 * A {@link Renderer} for the validation code in Java.
 *
 * <p>Inserts code into the {@link Validate} insertion point.
 *
 * <p>The generated code assumes there is a variable called {@code result}. Its type is the type of
 * the validated message. The variable holds the value of the message to validate.
 *
 * <p>The generated code is a number of code lines. It does not contain declarations (clsses,
 * methods, etc.).
 *
 * <p>If the validation rules are broken, throws a {@link io.spine.validate.ValidationException}.
 */
@SuppressWarnings("unused") // Loaded by ProtoData via reflection.
public final class JavaValidationRenderer extends Renderer {

    private static final int INDENT_LEVEL = 2;
    private static final String VIOLATIONS = "violations";

    private static final ImmutableMap<Sign, BinaryOperator<String>> OBJECT_COMPARISON_SIGNS =
            ImmutableMap.of(
                    EQUAL, (left, right) -> format("%s.equals(%s)", left, right),
                    NOT_EQUAL, (left, right) -> format("!%s.equals(%s)", left, right)
            );

    private static final ImmutableMap<Sign, BinaryOperator<String>> PRIMITIVE_COMPARISON_SIGNS =
            ImmutableMap.<Sign, BinaryOperator<String>>builder()
                        .put(EQUAL, (left, right) -> format("%s == %s", left, right))
                        .put(NOT_EQUAL, (left, right) -> format("%s != %s", left, right))
                        .put(GREATER_THAN, (left, right) -> format("%s > %s", left, right))
                        .put(LESS_THAN, (left, right) -> format("%s < %s", left, right))
                        .put(GREATER_OR_EQUAL, (left, right) -> format("%s < %s", left, right))
                        .put(LESS_OR_EQUAL, (left, right) -> format("%s < %s", left, right))
                        .build();

    private static final ImmutableMap<BinaryOperation, BinaryOperator<String>> BOOLEAN_OPERATIONS =
            ImmutableMap.of(
                    AND, (left, right) -> format("(%s) && (%s)", left, right),
                    OR, (left, right) -> format("(%s) || (%s)", left, right),
                    XOR, (left, right) -> format("(%s) ^ (%s)", left, right)
            );

    private @MonotonicNonNull TypeSystem typeSystem;

    public JavaValidationRenderer() {
        super(ImmutableSet.of(CommonLanguages.getJava()));
    }

    @Override
    protected void doRender(SourceSet sources) {
        this.typeSystem = bakeTypeSystem();
        select(MessageValidation.class)
                .all()
                .stream()
                .filter(validation -> validation.getRuleCount() > 0)
                .forEach(validation -> {
                    File protoFile = findProtoFile(validation.getType().getFile());
                    Path javaFile = javaFile(validation.getType(), protoFile);
                    sources.file(javaFile)
                           .at(new Validate(validation.getName()))
                           .add(rulesToCode(validation), INDENT_LEVEL);
                });
    }

    private File findProtoFile(FilePath path) {
        return select(ProtobufSourceFile.class)
                .withId(path)
                .orElseThrow(() -> newIllegalArgumentException(
                        "No such Protobuf file: `%s`.",
                        path.getValue()
                )).getFile();
    }

    private TypeSystem bakeTypeSystem() {
        Set<ProtobufSourceFile> files = select(ProtobufSourceFile.class).all();
        TypeSystem.Builder types = TypeSystem.newBuilder();
        for (ProtobufSourceFile file : files) {
            file.getTypeMap().values().forEach(type -> types.put(file.getFile(), type));
            file.getEnumTypeMap().values().forEach(type -> types.put(file.getFile(), type));
        }
        return types.build();
    }

    private ImmutableList<String> rulesToCode(MessageValidation validation) {
        MessageReference result = new MessageReference("result");
        CodeBlock.Builder code = CodeBlock.builder();
        code.add(prepareViolationAccumulator());
        code.add(generateValidationCode(validation, result));
        code.add(throwValidationException());
        return lines(code.build());
    }

    private static CodeBlock prepareViolationAccumulator() {
        return CodeBlock.of("$T<$T> $N = new $T<>();",
                            ArrayList.class,
                            ConstraintViolation.class,
                            VIOLATIONS,
                            ArrayList.class);
    }

    private CodeBlock generateValidationCode(MessageValidation validation, MessageReference result) {
        CodeBlock.Builder code = CodeBlock.builder();
        for (RuleOrComposite rule : validation.getRuleList()) {
            CodeBlock block = codeFor(rule, result, validation.getName());
            code.add(block);
        }
        return code.build();
    }

    private static CodeBlock throwValidationException() {
        CodeBlock.Builder code = CodeBlock.builder();
        code.beginControlFlow("if (!$N.isEmpty())", VIOLATIONS);
        code.addStatement("throw new $T($N)", ValidationException.class, VIOLATIONS);
        code.endControlFlow();
        return code.build();
    }

    private CodeBlock codeFor(RuleOrComposite rule,
                              MessageReference result,
                              TypeName declaringType) {
        switch (rule.getKindCase()) {
            case RULE:
                Rule simpleRule = rule.getRule();
                return codeForRule(simpleRule, result);
            case COMPOSITE:
                CompositeRule composite = rule.getComposite();
                return codeForComposite(composite, result, declaringType);
            case KIND_NOT_SET:
            default:
                return CodeBlock.of("");
        }
    }

    private CodeBlock codeForRule(Rule rule, MessageReference msg) {
        Field field = rule.getField();
        Expression fieldValue = msg.field(field).getGetter();
        Expression otherValue = typeSystem.valueToJava(rule.getOtherValue());
        String binaryCondition = conditionOfRule(rule, fieldValue, otherValue);
        return CodeBlock
                .builder()
                .beginControlFlow("if (!($L))", binaryCondition)
                .add(ErrorMessage.forRule(rule.getErrorMessage(), fieldValue.toCode(), otherValue.toCode())
                                 .createViolation(field, fieldValue, VIOLATIONS))
                .endControlFlow()
                .build();
    }

    private CodeBlock codeForComposite(CompositeRule rule,
                                       MessageReference msg,
                                       TypeName declaringType) {
        String binaryCondition = conditionOfComposite(rule, msg);
        return CodeBlock
                .builder()
                .beginControlFlow("if (!(%s))", binaryCondition)
                .add(errorForComposite(rule, msg).createCompositeViolation(declaringType, VIOLATIONS))
                .endControlFlow()
                .build();
    }

    private ErrorMessage errorFor(RuleOrComposite rule, MessageReference result) {
        switch (rule.getKindCase()) {
            case RULE:
                Rule simpleRule = rule.getRule();
                return errorForRule(result, simpleRule);
            case COMPOSITE:
                CompositeRule composite = rule.getComposite();
                return errorForComposite(composite, result);
            case KIND_NOT_SET:
            default:
                throw new IllegalArgumentException("Empty rule.");
        }
    }

    private ErrorMessage errorForRule(MessageReference result, Rule simpleRule) {
        String fieldValue = result.field(simpleRule.getField())
                                  .getGetter()
                                  .toCode();
        String otherValue = typeSystem.valueToJava(simpleRule.getOtherValue()).toCode();
        return ErrorMessage.forRule(simpleRule.getErrorMessage(), fieldValue, otherValue);
    }

    private ErrorMessage errorForComposite(CompositeRule composite, MessageReference result) {
        ErrorMessage leftError = errorFor(composite.getLeft(), result);
        ErrorMessage rightError = errorFor(composite.getRight(), result);
        return ErrorMessage.forComposite(composite.getErrorMessage(),
                                         leftError.toString(),
                                         rightError.toString(),
                                         composite.getOperation());
    }

    private String conditionOf(RuleOrComposite rule, MessageReference msg) {
        if (rule.hasRule()) {
            Rule simpleRule = rule.getRule();
            Field field = simpleRule.getField();
            Expression fieldValue = msg.field(field).getGetter();
            Expression otherValue = typeSystem.valueToJava(simpleRule.getOtherValue());
            return conditionOfRule(simpleRule, fieldValue, otherValue);
        } else {
            return conditionOfComposite(rule.getComposite(), msg);
        }
    }

    private static String conditionOfRule(Rule rule,
                                          Expression fieldValue,
                                          Expression otherValue) {
        Field field = rule.getField();
        Type type = field.getType();
        ImmutableMap<Sign, BinaryOperator<String>> signs;
        if (type.getKindCase() == PRIMITIVE) {
            signs = PRIMITIVE_COMPARISON_SIGNS;
        } else {
            signs = OBJECT_COMPARISON_SIGNS;
        }
        BinaryOperator<String> comparison = signs.get(rule.getSign());
        checkNotNull(comparison);
        String binaryCondition = comparison.apply(fieldValue.toCode(), otherValue.toCode());
        return binaryCondition;
    }

    private String conditionOfComposite(CompositeRule rule, MessageReference msg) {
        String left = conditionOf(rule.getLeft(), msg);
        String right = conditionOf(rule.getRight(), msg);
        BinaryOperator<String> binaryOp = BOOLEAN_OPERATIONS.get(rule.getOperation());
        checkNotNull(binaryOp);
        String condition = binaryOp.apply(left, right);
        return condition;
    }
}
