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

import io.spine.protodata.PrimitiveType;
import io.spine.protodata.Type;
import io.spine.protodata.TypeName;
import io.spine.validation.EnumValue;
import io.spine.validation.MessageValue;
import io.spine.validation.Value;
import org.jetbrains.annotations.NotNull;

import static com.google.protobuf.ByteString.EMPTY;
import static io.spine.protodata.Ast.typeUrl;
import static io.spine.protodata.PrimitiveType.PT_UNKNOWN;
import static io.spine.protodata.PrimitiveType.TYPE_BYTES;
import static io.spine.protodata.PrimitiveType.TYPE_STRING;
import static io.spine.protodata.PrimitiveType.UNRECOGNIZED;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;

public final class NotSetValue {

    /**
     * Prevents the utility class instantiation.
     */
    private NotSetValue() {
    }

    public static Value forType(Type type) {
        Type.KindCase kind = type.getKindCase();
        switch (kind) {
            case MESSAGE:
                return messageValue(type);
            case ENUMERATION:
                return enumValue(type);
            case PRIMITIVE:
                return primitiveValue(type);
            case KIND_NOT_SET:
            default:
                throw new IllegalArgumentException("Field type unknown.");
        }
    }

    @NotNull
    private static Value primitiveValue(Type type) {
        PrimitiveType primitiveType = type.getPrimitive();
        if (primitiveType == PT_UNKNOWN || primitiveType == UNRECOGNIZED) {
            throw newIllegalArgumentException("Unknown type `%s`.", primitiveType);
        }
        if (primitiveType == TYPE_STRING) {
            return Value.newBuilder()
                        .setStringValue("")
                        .vBuild();
        }
        if (primitiveType == TYPE_BYTES) {
            return Value.newBuilder()
                        .setBytesValue(EMPTY)
                        .vBuild();
        }
        throw newIllegalStateException(
                "Fields of type `%s` do not support `(required)` validation.",
                primitiveType
        );
    }

    @NotNull
    private static Value messageValue(Type type) {
        TypeName msgName = type.getMessage();
        return Value.newBuilder()
                    .setMessageValue(MessageValue.newBuilder()
                                                 .setTypeUrl(typeUrl(msgName))
                                                 .buildPartial())
                    .vBuild();
    }

    private static Value enumValue(Type type) {
        TypeName enumName = type.getEnumeration();
        return Value.newBuilder()
                    .setEnumValue(EnumValue.newBuilder()
                                           .setTypeUrl(typeUrl(enumName))
                                           .buildPartial())
                    .vBuild();
    }
}