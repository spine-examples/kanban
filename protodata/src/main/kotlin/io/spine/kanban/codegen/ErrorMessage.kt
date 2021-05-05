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

import io.spine.validation.RuleOrComposite

private const val VALUE = "value"
private const val OTHER = "other"
private const val LEFT = "left"
private const val RIGHT = "right"
private const val OPERATION = "operation"

class ErrorMessage
private constructor(private val value: String) {

    companion object {

        @JvmStatic
        fun forRuleOrComposite(rule: RuleOrComposite, message: MessageReference) {
            if (rule.hasRule()) {
                val simpleRule = rule.rule
                forRule(simpleRule.errorMessage, message.field(simpleRule.field.name).getter.toCode(), )
            }
        }

        @JvmStatic
        @JvmOverloads
        fun forRule(format: String, value: String = "", other: String = "") =
            ErrorMessage(
                format
                    .replacePlaceholder(VALUE, value)
                    .replacePlaceholder(OTHER, other)
            )

        @JvmStatic
        @JvmOverloads
        fun forComposite(format: String,
                         left: String = "",
                         right: String = "",
                         operation: String = "") =
            ErrorMessage(
                format
                    .replacePlaceholder(LEFT, left)
                    .replacePlaceholder(RIGHT, right)
                    .replacePlaceholder(OPERATION, operation)
            )
    }

    override fun toString() = value
}

private fun String.replacePlaceholder(placeholder: String, newValue: String): String {
    val formattedPlaceholder = "{$placeholder}"
    return replace(formattedPlaceholder, newValue)
}
