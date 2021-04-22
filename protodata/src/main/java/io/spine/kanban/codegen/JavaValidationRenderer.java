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
import com.google.common.collect.ImmutableSet;
import io.spine.protodata.Ast;
import io.spine.protodata.language.CommonLanguages;
import io.spine.protodata.renderer.Renderer;
import io.spine.protodata.renderer.SourceSet;
import io.spine.validation.MessageValidation;

import java.nio.file.Path;

public final class JavaValidationRenderer extends Renderer {

    public JavaValidationRenderer() {
        super(ImmutableSet.of(CommonLanguages.getJava()));
    }

    @Override
    protected void doRender(SourceSet sources) {
        select(MessageValidation.class).all().forEach(validation -> {
            Path javaFile = Ast.javaFile(validation.getType(), validation.getDeclaringFile());
            sources.file(javaFile)
                   .at(new Validate(validation.getName()))
                   .add(rulesToCode(validation));
        });
    }

    private ImmutableList<String> rulesToCode(MessageValidation validation) {
        System.out.println(validation);
        return ImmutableList.of();
    }
}
