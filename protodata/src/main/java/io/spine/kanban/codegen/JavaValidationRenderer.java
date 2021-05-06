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
import io.spine.base.FieldPath;
import io.spine.protobuf.TypeConverter;
import io.spine.protodata.Field;
import io.spine.protodata.ProtobufSourceFile;
import io.spine.protodata.Type;
import io.spine.protodata.TypeName;
import io.spine.protodata.language.CommonLanguages;
import io.spine.protodata.renderer.Renderer;
import io.spine.protodata.renderer.SourceSet;
import io.spine.validate.ConstraintViolation;
import io.spine.validation.BinaryOperation;
import io.spine.validation.CompositeRule;
import io.spine.validation.MessageValidation;
import io.spine.validation.Rule;
import io.spine.validation.RuleOrComposite;
import io.spine.validation.Sign;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.BinaryOperator;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.kanban.codegen.ErrorMessage.forComposite;
import static io.spine.kanban.codegen.ErrorMessage.forRule;
import static io.spine.protodata.Ast.javaFile;
import static io.spine.protodata.Ast.typeUrl;
import static io.spine.protodata.Type.KindCase.PRIMITIVE;
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

public final class JavaValidationRenderer extends Renderer {

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
        bakeTypeSystem();
        select(MessageValidation.class)
                .all()
                .stream()
                .filter(validation -> validation.getRuleCount() > 0)
                .forEach(validation -> {
                    Path javaFile = javaFile(validation.getType(), validation.getDeclaringFile());
                    sources.file(javaFile)
                           .at(new Validate(validation.getName()))
                           .add(rulesToCode(validation));
                });
    }

    private void bakeTypeSystem() {
        Set<ProtobufSourceFile> files = select(ProtobufSourceFile.class).all();
        TypeSystem.Builder types = TypeSystem.newBuilder();
        for (ProtobufSourceFile file : files) {
            file.getTypeMap().values().forEach(type -> types.put(file.getFile(), type));
            file.getEnumTypeMap().values().forEach(type -> types.put(file.getFile(), type));
        }
        typeSystem = types.build();
    }

    private ImmutableList<String> rulesToCode(MessageValidation validation) {
        MessageReference result = new MessageReference("result");
        ImmutableList.Builder<String> lines = ImmutableList.builder();
        lines.add(CodeBlock.of("$T<$T> $N = $T.<$T>builder()",
                               ImmutableList.Builder.class, ConstraintViolation.class,
                               VIOLATIONS,
                               ImmutableList.Builder.class, ConstraintViolation.class).toString());
        for (RuleOrComposite rule : validation.getRuleList()) {
            CodeBlock block = codeFor(rule, result, validation.getName());
            lines.add(block.toString());
        }
        return lines.build();
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

    private ErrorMessage errorFor(RuleOrComposite rule, MessageReference result) {
        switch (rule.getKindCase()) {
            case RULE:
                Rule simpleRule = rule.getRule();
                String fieldValue = result.field(simpleRule.getField().getName())
                                          .getGetter()
                                          .toCode();
                String otherValue = typeSystem.valueToJava(simpleRule.getOtherValue()).toCode();
                return forRule(simpleRule.getErrorMessage(), fieldValue, otherValue);
            case COMPOSITE:
                CompositeRule composite = rule.getComposite();
                return errorFor(composite, result);
            case KIND_NOT_SET:
            default:
                throw new IllegalArgumentException("Empty rule.");
        }
    }

    private ErrorMessage errorFor(CompositeRule composite, MessageReference result) {
        ErrorMessage leftError = errorFor(composite.getLeft(), result);
        ErrorMessage rightError = errorFor(composite.getRight(), result);
        String operation = composite.getOperation()
                                    .name()
                                    .toLowerCase();
        return forComposite(composite.getErrorMessage(),
                            leftError.toString(),
                            rightError.toString(),
                            operation);
    }

    private CodeBlock codeForRule(Rule rule, MessageReference msg) {
        Field field = rule.getField();
        Expression fieldValue = msg.field(field.getName()).getGetter();
        Expression otherValue = typeSystem.valueToJava(rule.getOtherValue());
        String binaryCondition = conditionOf(rule, fieldValue, otherValue);
        return CodeBlock
                .builder()
                .beginControlFlow("if (!(%s))", binaryCondition)
                .add(createViolation(forRule(rule.getErrorMessage(),
                                             fieldValue.toCode(),
                                             otherValue.toCode()),
                                     field,
                                     fieldValue))
                .endControlFlow()
                .build();
    }

    private CodeBlock codeForComposite(CompositeRule rule,
                                       MessageReference msg,
                                       TypeName declaringType) {
        String binaryCondition = conditionOf(rule, msg);
        return CodeBlock
                .builder()
                .beginControlFlow("if (!(%s))", binaryCondition)
                .add(createCompositeViolation(errorFor(rule, msg), declaringType))
                .endControlFlow()
                .build();
    }

    private String conditionOf(RuleOrComposite rule, MessageReference msg) {
        if (rule.hasRule()) {
            Rule simpleRule = rule.getRule();
            Field field = simpleRule.getField();
            Expression fieldValue = msg.field(field.getName()).getGetter();
            Expression otherValue = typeSystem.valueToJava(simpleRule.getOtherValue());
            return conditionOf(simpleRule, fieldValue, otherValue);
        } else {
            return conditionOf(rule.getComposite(), msg);
        }
    }

    private static String conditionOf(Rule rule,
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

    private String conditionOf(CompositeRule rule, MessageReference msg) {
        String left = conditionOf(rule.getLeft(), msg);
        String right = conditionOf(rule.getRight(), msg);
        BinaryOperator<String> binaryOp = BOOLEAN_OPERATIONS.get(rule.getOperation());
        checkNotNull(binaryOp);
        String condition = binaryOp.apply(left, right);
        return condition;
    }

    private static CodeBlock createViolation(ErrorMessage error,
                                             Field field,
                                             Expression fieldValue) {
        TypeName type = field.getDeclaringType();
        Expression violationBuilder = buildViolation(error, type, field, fieldValue);
        return CodeBlock
                .builder()
                .add("$N.add($L)", VIOLATIONS, violationBuilder)
                .build();
    }

    private static CodeBlock createCompositeViolation(ErrorMessage error, TypeName type) {
        Expression violationBuilder = buildViolation(error, type, null, null);
        return CodeBlock
                .builder()
                .add("$N.add($L)", VIOLATIONS, violationBuilder)
                .build();
    }

    private static Expression pathFrom(Field field) {
        ClassName type = new ClassName(FieldPath.class);
        return type.newBuilder()
                   .chainAdd("field_name", new LiteralString(field.getName().getValue()))
                   .chainBuild();
    }

    private static Expression pack(Expression rawValue) {
        ClassName type = new ClassName(TypeConverter.class);
        return type.call("toAny", ImmutableList.of(rawValue), ImmutableList.of());
    }

    private static Expression buildViolation(ErrorMessage error,
                                             TypeName type,
                                             @Nullable Field field,
                                             @Nullable Expression fieldValue) {
        MethodCall violationBuilder = new ClassName(ConstraintViolation.class)
                .newBuilder()
                .chainSet("msg_format", new LiteralString(error.toString()))
                .chainSet("type_name", new LiteralString(typeUrl(type)));
        if (field != null) {
            violationBuilder = violationBuilder.chainSet("field_path", pathFrom(field));
        }
        if (fieldValue != null) {
            violationBuilder = violationBuilder.chainSet("field_value", pack(fieldValue));
        }
        return violationBuilder.chainBuild();
    }
}
