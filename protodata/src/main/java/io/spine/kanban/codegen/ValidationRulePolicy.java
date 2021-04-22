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

import com.google.protobuf.DescriptorProtos.FieldOptions;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Extension;
import io.spine.core.External;
import io.spine.protodata.Field;
import io.spine.protodata.FieldOptionDiscovered;
import io.spine.protodata.MessageType;
import io.spine.protodata.Option;
import io.spine.protodata.ProtobufSourceFile;
import io.spine.protodata.plugin.Policy;
import io.spine.server.event.React;
import io.spine.server.model.Nothing;
import io.spine.server.tuple.EitherOf2;
import io.spine.validation.AddRule;
import io.spine.validation.Rule;
import io.spine.validation.Value;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.spine.option.OptionsProto.required;
import static io.spine.protodata.Ast.typeUrl;
import static io.spine.util.Exceptions.newIllegalArgumentException;
import static io.spine.util.Exceptions.newIllegalStateException;
import static io.spine.validation.Sign.NOT_EQUAL;

public final class ValidationRulePolicy extends Policy {

    @React
    EitherOf2<AddRule, Nothing> on(@External FieldOptionDiscovered event) {
        Option option = event.getOption();
        if (isOption(option, required)) {
            ProtobufSourceFile file = select(ProtobufSourceFile.class)
                    .withId(event.getFile())
                    .orElseThrow(() -> newIllegalArgumentException(
                            "Unknown file `%s`.", event.getFile()
                                                       .getValue()
                    ));
            MessageType type = file.getTypeMap()
                                   .get(typeUrl(event.getType()));
            Field field = type.getFieldList()
                              .stream()
                              .filter(f -> f.getName()
                                            .equals(event.getField()))
                              .findAny()
                              .orElseThrow(() -> newIllegalArgumentException(
                                      "Unknown field `%s`.", event.getField()
                              ));
            return EitherOf2.withA(addRequiredRule(field));
        }
        return EitherOf2.withB(nothing());
    }

    private static AddRule addRequiredRule(Field field) {
        Value defaultValue = NotSetValueValue.forType(field.getType());
        Rule rule = Rule.newBuilder()
                        .setField(field)
                        .setSign(NOT_EQUAL)
                        .setOtherValue(defaultValue)
                        .build();
        return AddRule.newBuilder()
                      .setType(field.getDeclaringType())
                      .setRule(rule)
                      .vBuild();
    }

    private static boolean isOption(Option option, Extension<FieldOptions, ?> extension) {
        FieldDescriptor descriptor = extension.getDescriptor();
        return option.getName().equals(descriptor.getName())
                && option.getNumber() == extension.getNumber();
    }
}
