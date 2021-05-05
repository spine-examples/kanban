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

@file:JvmName("Expressions")

package io.spine.kanban.codegen

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.protobuf.ByteString
import com.google.protobuf.Field
import io.spine.protodata.Field.CardinalityCase
import io.spine.protodata.Field.CardinalityCase.LIST
import io.spine.protodata.Field.CardinalityCase.MAP
import io.spine.protodata.Field.CardinalityCase.SINGLE
import io.spine.protodata.FieldName

sealed class Expression(private val code: String) {

    fun toCode(): String = code

    final override fun toString(): String = toCode()
}

object Null : Expression("null")

class LiteralString(value: String) : Expression("\"$value\"")

private val byteStringClass = ByteString::class.qualifiedName!!

class LiteralBytes(bytes: ByteString) : Expression(
    "$byteStringClass.copyFrom(new bytes[]{${bytes.toByteArray().joinToString()}})"
)

class Literal(value: Any) : Expression(value.toString())

class ClassName(className: String) : Expression(className) {

    constructor(cls: Class<*>) : this(cls.canonicalName)

    fun newBuilder(): MethodCall =
        call("newBuilder")

    fun getDefaultInstance(): MethodCall =
        call("getDefaultInstance")

    fun enumValue(number: Int): MethodCall =
        call("forNumber", listOf(Literal(number)))

    fun call(
        name: String,
        arguments: List<Expression> = listOf(),
        generics: List<ClassName> = listOf()
    ): MethodCall =
        MethodCall(this, name, arguments, generics)
}

class MessageReference(label: String) : Expression(label) {

    fun field(name: FieldName): FieldAccess = FieldAccess(this, name)
}

class FieldAccess
internal constructor(private val message: Expression,
                     private val name: FieldName,
                     private val cardinality: CardinalityCase = SINGLE) {

    val getter: MethodCall
        get() = MethodCall(message, getterName)

    fun setter(value: Expression): MethodCall =
        MethodCall(message, setterName, listOf(value))

    fun add(value: Expression): MethodCall =
        MethodCall(message, addName, listOf(value))

    fun addAll(value: Expression): MethodCall =
        MethodCall(message, addAllName, listOf(value))

    fun put(key: Expression, value: Expression): MethodCall =
        MethodCall(message, putName, listOf(key, value))

    fun putAll(value: Expression): MethodCall =
        MethodCall(message, putAllName, listOf(value))

    private val getterName: String
        get() = when(cardinality) {
            LIST -> getListName
            MAP -> getMapName
            else -> prefixed("get")
        }

    private val getListName: String
        get() = "get${name.value.CamelCase()}List"

    private val getMapName: String
        get() = "get${name.value.CamelCase()}Map"

    private val setterName: String
        get() = when(cardinality) {
            LIST -> addAllName
            MAP -> putAllName
            else -> prefixed("set")
        }

    private val addName: String
        get() = prefixed("add")

    private val addAllName: String
        get() = prefixed("addAll")

    private val putName: String
        get() = prefixed("put")

    private val putAllName: String
        get() = prefixed("putAll")

    private fun prefixed(prefix: String) =
        "$prefix${name.value.CamelCase()}"

    override fun toString(): String {
        return "FieldAccess[$message#${name.value}]"
    }
}

class MethodCall
@JvmOverloads
constructor(
    receiver: Expression,
    name: String,
    arguments: List<Expression> = listOf(),
    generics: List<ClassName> = listOf()
) : Expression(
    "${receiver.toCode()}.${generics.genericTypes()}$name(${arguments.formatParams()})"
) {

    @JvmOverloads
    fun chain(name: String, arguments: List<Expression> = listOf()): MethodCall =
        MethodCall(this, name, arguments)

    fun chainGet(name: FieldName): MethodCall =
        FieldAccess(this, name).getter

    fun chainGet(name: String): MethodCall =
        FieldAccess(this, fieldName(name)).getter

    fun chainSet(name: FieldName, value: Expression): MethodCall =
        FieldAccess(this, name).setter(value)

    fun chainSet(name: String, value: Expression): MethodCall =
        FieldAccess(this, fieldName(name)).setter(value)

    fun chainAddAll(name: FieldName, value: Expression): MethodCall =
        FieldAccess(this, name).addAll(value)

    fun chainAddAll(name: String, value: Expression): MethodCall =
        FieldAccess(this, fieldName(name)).addAll(value)

    fun chainAdd(name: FieldName, value: Expression): MethodCall =
        FieldAccess(this, name).add(value)

    fun chainAdd(name: String, value: Expression): MethodCall =
        FieldAccess(this, fieldName(name)).add(value)

    fun chainBuild() : MethodCall =
        chain("build")

    private fun fieldName(value: String) = FieldName
        .newBuilder()
        .setValue(value)
        .build()
}

private val immutableListClass = ImmutableList::class.qualifiedName!!
private val immutableMapClass = ImmutableMap::class.qualifiedName!!

fun listExpression(expressions: List<Expression>): MethodCall =
    ClassName(immutableListClass).call("of", expressions)

fun mapExpression(expressions: Map<Expression, Expression>,
                  keyType: ClassName?,
                  valueType: ClassName?
): MethodCall {
    val className = ClassName(immutableMapClass)
    if (expressions.isEmpty()) {
        return className.call("of")
    }
    checkNotNull(keyType)
    checkNotNull(valueType)
    var call = className.call("builder", generics = listOf(keyType, valueType))
    expressions.forEach { k, v ->
        call = call.chain("put", listOf(k, v))
    }
    return call.chain("build")
}

private fun List<ClassName>.genericTypes() =
    if (isNotEmpty()) "<${joinToString()}>" else ""

private fun String.CamelCase() =
    split("_")
        .filter { it.isNotBlank() }
        .joinToString(separator = "") { it.capitalize() }

private fun List<Expression>.formatParams() =
    joinToString { it.toCode() }
