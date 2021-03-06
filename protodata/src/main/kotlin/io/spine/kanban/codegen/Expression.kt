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
import io.spine.protobuf.TypeConverter
import io.spine.protodata.Field
import io.spine.protodata.Field.CardinalityCase
import io.spine.protodata.Field.CardinalityCase.LIST
import io.spine.protodata.Field.CardinalityCase.MAP
import io.spine.protodata.Field.CardinalityCase.SINGLE
import io.spine.protodata.FieldName
import io.spine.protodata.camelCase
import kotlin.reflect.KClass

private val immutableListClass = ClassName(ImmutableList::class)
private val immutableMapClass = ClassName(ImmutableMap::class)

/**
 * A piece of Java code which yields a value.
 */
sealed class Expression(private val code: String) {

    /**
     * Prints this Java expression.
     */
    fun toCode(): String = code

    final override fun toString(): String = toCode()

    /**
     * Obtains an `Expression` which wraps this `Expression`'s value in a `com.google.protobuf.Any`.
     */
    fun packToAny(): Expression {
        val type = ClassName(TypeConverter::class)
        return type.call("toAny", arguments = listOf(this))
    }
}

/**
 * An literal `null` expression.
 */
object Null : Expression("null")

/**
 * A string literal.
 *
 * Represented as the same value as the given string, wrapped in quotation marks. No extra character
 * escaping is perfomed.
 */
class LiteralString(value: String) : Expression("\"$value\"")

private val byteStringClass = ByteString::class.qualifiedName!!

/**
 * An expression which yields the given byte string.
 */
class LiteralBytes(bytes: ByteString) : Expression(
    "$byteStringClass.copyFrom(new bytes[]{${bytes.toByteArray().joinToString()}})"
)

/**
 * An expression represented by the given string.
 *
 * No extra processing is done upon the given code.
 */
class Literal(value: Any) : Expression(value.toString())

/**
 * A fully qualified Java class name.
 *
 * In Java, a class name is not a valid expression. Use one of the methods of this class to create
 * an expression from this class name.
 */
class ClassName
/**
 * Creates a class name from the given FQN.
 */
constructor(private val className: String) {

    /**
     * Obtains the class name of the given Java class.
     */
    constructor(cls: Class<*>) : this(cls.canonicalName)

    /**
     * Obtains the Java class name of the given Kotlin class.
     */
    constructor(klass: KClass<*>) : this(klass.java)

    /**
     * Constructs an expression which creates a new builder for this class.
     *
     * Example: `ClassName("com.acme.Bird").newBuilder()` yields "`com.acme.Bird.newBuilder()`".
     */
    fun newBuilder(): MethodCall =
        call("newBuilder")

    /**
     * Constructs an expression which obtains the default instance for this class.
     *
     * Example: `ClassName("com.acme.Bird").getDefaultInstance()` yields
     * "`com.acme.Bird.getDefaultInstance()`".
     */
    fun getDefaultInstance(): MethodCall =
        call("getDefaultInstance")

    /**
     * Constructs an expression which obtains the Protobuf enum value by the given number from this
     * class.
     *
     * Example: `ClassName("com.acme.Bird").enumValue(1)` yields
     * "`com.acme.Bird.forNumber(1)`".
     */
    fun enumValue(number: Int): MethodCall =
        call("forNumber", listOf(Literal(number)))


    /**
     * Constructs a call to a static method of this class.
     *
     * @param name the name of the method
     * @param arguments the method arguments
     * @param generics the method type parameters
     */
    fun call(
        name: String,
        arguments: List<Expression> = listOf(),
        generics: List<ClassName> = listOf()
    ): MethodCall =
        MethodCall(Literal(className), name, arguments, generics)
}

/**
 * An expression representing a reference to a Protobuf message value.
 */
class MessageReference(label: String) : Expression(label) {

    /**
     * Obtains a [FieldAccess] to the [field] of this message.
     */
    fun field(field: Field): FieldAccess =
        FieldAccess(this, field.name, field.cardinalityCase)
}

/**
 * A selector for an access method for a Protobuf message field.
 *
 * Depending on the field type, may allow to generate getters and setters for the field.
 */
class FieldAccess
internal constructor(
    private val message: Expression,
    private val name: FieldName,
    private val cardinality: CardinalityCase = SINGLE
) {

    /**
     * A getter expression for the associated field.
     */
    val getter: MethodCall
        get() {
            val simpleAccess = MethodCall(message, getterName)
            return when (cardinality) {
                LIST -> immutableListClass.call("copyOf", listOf(simpleAccess))
                MAP -> immutableMapClass.call("copyOf", listOf(simpleAccess))
                else -> simpleAccess
            }
        }

    /**
     * Constructs a setter expression for the associated field.
     */
    fun setter(value: Expression): MethodCall =
        MethodCall(message, setterName, listOf(value))

    /**
     * Constructs an `addField(..)` expression for the associated field.
     */
    fun add(value: Expression): MethodCall =
        MethodCall(message, addName, listOf(value))

    /**
     * Constructs an `addAllField(..)` expression for the associated field.
     */
    fun addAll(value: Expression): MethodCall =
        MethodCall(message, addAllName, listOf(value))

    /**
     * Constructs an `putField(..)` expression for the associated field.
     */
    fun put(key: Expression, value: Expression): MethodCall =
        MethodCall(message, putName, listOf(key, value))

    private val getterName: String
        get() = when (cardinality) {
            LIST -> getListName
            MAP -> getMapName
            else -> prefixed("get")
        }

    private val getListName: String
        get() = "get${name.value.camelCase()}List"

    private val getMapName: String
        get() = "get${name.value.camelCase()}Map"

    private val setterName: String
        get() = when (cardinality) {
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
        "$prefix${name.value.camelCase()}"

    override fun toString(): String {
        return "FieldAccess[$message#${name.value}]"
    }
}

/**
 * An expression of a Java method call.
 *
 * Can be a static or an instance method. In the case of the former, the receiver expression
 * is a class name. In the case of the latter — the receiver object.
 */
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

    /**
     * Constructs an expression of another method call with the result of this call as the receiver.
     */
    @JvmOverloads
    fun chain(name: String, arguments: List<Expression> = listOf()): MethodCall =
        MethodCall(this, name, arguments)

    /**
     * Constructs an expression chaining a setter call.
     */
    fun chainSet(name: FieldName, value: Expression): MethodCall =
        FieldAccess(this, name).setter(value)

    /**
     * Constructs an expression chaining a setter call.
     */
    fun chainSet(name: String, value: Expression): MethodCall =
        FieldAccess(this, fieldName(name)).setter(value)

    /**
     * Constructs an expression chaining a call of an `addField(..)` method.
     */
    fun chainAdd(name: String, value: Expression): MethodCall =
        FieldAccess(this, fieldName(name)).add(value)

    /**
     * Constructs an expression chaining a call of the `build()` method.
     */
    fun chainBuild(): MethodCall =
        chain("build")

    private fun fieldName(value: String) = FieldName
        .newBuilder()
        .setValue(value)
        .build()
}

/**
 * Constructs an expression of a list of the given [expressions].
 *
 * The resulting expression always yields an instance of Guava `ImmutableList`.
 */
fun listExpression(expressions: List<Expression>): MethodCall =
    immutableListClass.call("of", expressions)

/**
 * Constructs an expression of a map of the given [expressions].
 *
 * The resulting expression always yields an instance of Guava `ImmutableMap`.
 *
 * @param expressions the expressions representing the entries
 * @param keyType the type of the keys of the map;
 *                must be non-null if the map is not empty, may be `null` otherwise
 * @param valueType the type of the values of the map;
 *                  must be non-null if the map is not empty, may be `null` otherwise
 */
fun mapExpression(expressions: Map<Expression, Expression>,
                  keyType: ClassName?,
                  valueType: ClassName?): MethodCall {
    if (expressions.isEmpty()) {
        return immutableMapClass.call("of")
    }
    checkNotNull(keyType)
    checkNotNull(valueType)
    var call = immutableMapClass.call("builder", generics = listOf(keyType, valueType))
    expressions.forEach { (k, v) ->
        call = call.chain("put", listOf(k, v))
    }
    return call.chainBuild()
}

/**
 * Formats these class names as type arguments, including the angle brackets.
 */
private fun List<ClassName>.genericTypes() =
    if (isNotEmpty()) "<${joinToString()}>" else ""

/**
 * Formats these expressions as method parameters, not including the brackets.
 */
private fun List<Expression>.formatParams() =
    joinToString { it.toCode() }
