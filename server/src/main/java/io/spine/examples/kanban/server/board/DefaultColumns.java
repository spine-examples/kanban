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

package io.spine.examples.kanban.server.board;

import io.spine.examples.kanban.BoardInit.DefaultColumn;

import java.util.ArrayList;
import java.util.List;

/** Provides utility methods for dealing with default columns. */
final class DefaultColumns {

    /** Prevents instantiation of this utility class. */
    private DefaultColumns() {
    }

    /**
     * Returns a list of default columns.
     *
     * <p> Excludes the enum entry generated by {@code Protoc} for deserializing
     * unknown values.
     */
    static List<DefaultColumn> all() {
        List<DefaultColumn> all = new ArrayList<>();

        for(DefaultColumn column: DefaultColumn.values()) {
            if(isDomainValue(column)) {
                all.add(column);
            }
        }

        return all;
    }

    /**
     * Tells whether the provided enum entry is a domain-related value.
     *
     * <p> Protoc generates one more enum entry, which is used for deserializing
     * unknown values. This method helps to find out if an enum entry is actually
     * declared in {@code .proto}(hence is a domain value).
     */
    private static boolean isDomainValue(DefaultColumn column) {
        return column != DefaultColumn.UNRECOGNIZED;
    }

    /**
     * Obtains the number of default columns.
     */
    static int count() {
        return DefaultColumn.values().length - 1;
    }
}
