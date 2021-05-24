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

package io.spine.kanban.codegen

import com.squareup.javapoet.CodeBlock
import io.spine.kanban.codegen.ErrorMessage.Companion.forRule
import io.spine.protodata.Field
import io.spine.protodata.Type.KindCase.PRIMITIVE
import io.spine.protodata.TypeName
import io.spine.validation.BinaryOperation.AND
import io.spine.validation.BinaryOperation.OR
import io.spine.validation.BinaryOperation.XOR
import io.spine.validation.CompositeRule
import io.spine.validation.Rule
import io.spine.validation.RuleOrComposite
import io.spine.validation.RuleOrComposite.KindCase.COMPOSITE
import io.spine.validation.RuleOrComposite.KindCase.RULE
import io.spine.validation.Sign.EQUAL
import io.spine.validation.Sign.GREATER_OR_EQUAL
import io.spine.validation.Sign.GREATER_THAN
import io.spine.validation.Sign.LESS_OR_EQUAL
import io.spine.validation.Sign.LESS_THAN
import io.spine.validation.Sign.NOT_EQUAL

private val OBJECT_COMPARISON_SIGNS = mapOf(
    EQUAL to { left: String, right: String -> "$left.equals($right)" },
    NOT_EQUAL to { left: String, right: String -> "!$left.equals($right)" }
)

private val PRIMITIVE_COMPARISON_SIGNS = mapOf(
    EQUAL to { left: String, right: String -> "$left == $right" },
    NOT_EQUAL to { left: String, right: String -> "$left != $right" },
    GREATER_THAN to { left: String, right: String -> "$left > $right" },
    LESS_THAN to { left: String, right: String -> "$left < $right" },
    GREATER_OR_EQUAL to { left: String, right: String -> "$left < $right" },
    LESS_OR_EQUAL to { left: String, right: String -> "$left < $right" }
)

private val BOOLEAN_OPERATIONS = mapOf(
    AND to { left: String, right: String -> "($left) && ($right)" },
    OR to { left: String, right: String -> "($left) || ($right)" },
    XOR to { left: String, right: String -> "($left) ^ ($right)" }
)

internal sealed interface JavaCodeGenerator {

    fun code(): CodeBlock

    fun condition(): Expression

    fun error(): ErrorMessage
}

internal class SimpleRuleGenerator(
    private val rule: Rule,
    private val msg: MessageReference,
    private val typeSystem: TypeSystem,
    private val violationsList: String
) : JavaCodeGenerator {

    private val field: Field by lazy { rule.getField() }
    private val fieldValue: Expression by lazy { msg.field(field).getter }
    private val otherValue: Expression by lazy {  typeSystem.valueToJava(rule.getOtherValue()) }

    override fun code(): CodeBlock {
        val binaryCondition = condition()
        val errorMsg = error()
        return CodeBlock
            .builder()
            .beginControlFlow("if (!(\$L))", binaryCondition)
            .add(errorMsg.createViolation(field, fieldValue, violationsList))
            .endControlFlow()
            .build()
    }

    override fun condition(): Expression {
        val field = rule.field
        val type = field.type
        val signs = if (type.kindCase == PRIMITIVE) {
            PRIMITIVE_COMPARISON_SIGNS
        } else {
            OBJECT_COMPARISON_SIGNS
        }
        val compare = signs[rule.sign]!!
        return Literal(compare(fieldValue.toCode(), otherValue.toCode()))
    }

    override fun error(): ErrorMessage {
        return forRule(rule.errorMessage, fieldValue.toCode(), otherValue.toCode())
    }
}

internal class CompositeRuleGenerator(
    private val rule: CompositeRule,
    private val msg: MessageReference,
    private val declaringType: TypeName,
    private val typeSystem: TypeSystem,
    private val violationsList: String
) : JavaCodeGenerator {

    override fun code(): CodeBlock {
        val binaryCondition = condition()
        val error = error()
        return CodeBlock
            .builder()
            .beginControlFlow("if (!(%s))", binaryCondition)
            .add(error.createCompositeViolation(declaringType, violationsList))
            .endControlFlow()
            .build()
    }

    override fun condition(): Expression {
        val left = generatorFor(rule.left, msg, typeSystem, declaringType, violationsList)
            .condition()
        val right = generatorFor(rule.right, msg, typeSystem, declaringType, violationsList)
            .condition()
        val binaryOp = BOOLEAN_OPERATIONS[rule.operation]!!
        return Literal(binaryOp(left.toCode(), right.toCode()))
    }

    override fun error(): ErrorMessage {
        TODO("Not yet implemented")
    }
}

internal fun generatorFor(rule: RuleOrComposite, msg: MessageReference, typeSystem: TypeSystem, declaringType: TypeName, violationsList: String): JavaCodeGenerator =
    when(rule.kindCase) {
        RULE -> SimpleRuleGenerator(rule.rule, msg, typeSystem, violationsList)
        COMPOSITE -> CompositeRuleGenerator(rule.composite, msg, declaringType, typeSystem, violationsList)
        else -> throw IllegalArgumentException("Empty rule.")
    }
