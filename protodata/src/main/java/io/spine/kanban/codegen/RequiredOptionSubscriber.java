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
import io.spine.option.OptionsProto;
import io.spine.protodata.Field;
import io.spine.protodata.FieldOptionDiscovered;
import io.spine.protodata.MessageType;
import io.spine.protodata.ProtobufSourceFile;
import io.spine.protodata.subscriber.CodeEnhancement;
import io.spine.protodata.subscriber.Subscriber;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.google.common.collect.Iterables.getOnlyElement;
import static io.spine.protodata.AstExtensionKt.typeUrl;

public final class RequiredOptionSubscriber extends Subscriber<FieldOptionDiscovered> {

    public RequiredOptionSubscriber() {
        super(FieldOptionDiscovered.class);
    }

    @NotNull
    @Override
    public Iterable<CodeEnhancement> process(@NotNull FieldOptionDiscovered event) {
        if (event.getOption().getNumber() == OptionsProto.required.getNumber()) {
            Set<ProtobufSourceFile> files = select(ProtobufSourceFile.class)
                    .withId(event.getFile())
                    .execute();
            ProtobufSourceFile file = getOnlyElement(files);
            MessageType type = file.getTypeOrThrow(typeUrl(event.getType()));
            Field field = type.getFieldList()
                              .stream()
                              .filter(f -> f.getName()
                                            .equals(event.getField()))
                              .findAny()
                              .orElseThrow(() -> new IllegalStateException(
                                      "Field `" + event.getField().getValue() + "` not found."
                              ));
            return ImmutableList.of(new CheckFieldIsSet(field));
        }
        return ImmutableList.of();
    }
}
