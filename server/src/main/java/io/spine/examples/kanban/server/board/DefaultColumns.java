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

import com.google.common.collect.ImmutableList;
import io.spine.examples.kanban.BoardInit.DefaultColumn;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;

/** Provides utility methods for dealing with default columns. */
public final class DefaultColumns {

    /** Prevents instantiation of this utility class. */
    private DefaultColumns() {
    }

    /** Returns an ordered list of default columns for the provided board. */
    public static ImmutableList<DefaultColumn> all() {
        return Arrays.stream(DefaultColumn.values())
                     .filter(c -> c != DefaultColumn.UNRECOGNIZED)
                     .collect(toImmutableList());
    }

    /** Obtains the number of default columns. */
    public static int count() {
        return DefaultColumn.values().length - 1;
    }

    /** Transforms the enum value into a column title. */
    public static String nameFor(DefaultColumn column) {
        checkNotNull(column);

        String lowerCase = column.name()
                                 .replace('_', ' ')
                                 .toLowerCase();
        return toTitleCase(lowerCase);
    }

    /**
     * Transforms the passed string to the Title Case.
     *
     * <p>Examples of transformations:
     * <ul>
     *   <li>"lorem" -> "Lorem"
     *   <li>"ipsum dolor" -> "Ipsum Dolor"
     *   <li>"SIT AMET" -> "SIT AMET"
     * </ul>
     */
    private static String toTitleCase(String input) {
        StringBuilder res = new StringBuilder(input.length());
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            res.append(c);
        }

        return res.toString();
    }
}
