/*
 * Copyright 2022, TeamDev. All rights reserved.
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

package io.spine.examples.kanban.web.server;

import io.spine.base.Production;
import io.spine.examples.kanban.server.KanbanContext;
import io.spine.server.BoundedContext;
import io.spine.server.CommandService;
import io.spine.server.QueryService;
import io.spine.server.ServerEnvironment;
import io.spine.server.SubscriptionService;
import io.spine.server.storage.memory.InMemoryStorageFactory;
import io.spine.server.transport.memory.InMemoryTransportFactory;
import io.spine.web.firebase.FirebaseClient;
import io.spine.web.firebase.query.FirebaseQueryBridge;
import io.spine.web.firebase.subscription.FirebaseSubscriptionBridge;

class Application {

    private final CommandService commandService;
    private final FirebaseQueryBridge queryBridge;
    private final FirebaseSubscriptionBridge subscriptionBridge;

    private static final Application INSTANCE = create();

    /**
     * Returns the application instance.
     */
    static Application application() {
        return INSTANCE;
    }

    private Application(
            CommandService commandService,
            QueryService queryService,
            SubscriptionService subscriptionService,
            FirebaseClient firebaseClient
    ) {
        this.commandService = commandService;
        this.queryBridge = newQueryBridge(queryService, firebaseClient);
        this.subscriptionBridge = newSubscriptionBridge(subscriptionService, firebaseClient);
    }

    private static FirebaseQueryBridge newQueryBridge(
            QueryService queryService,
            FirebaseClient firebaseClient
    ) {
        return FirebaseQueryBridge
                .newBuilder()
                .setQueryService(queryService)
                .setFirebaseClient(firebaseClient)
                .build();
    }

    private static FirebaseSubscriptionBridge newSubscriptionBridge(
            SubscriptionService subscriptionService,
            FirebaseClient firebaseClient
    ) {
        return FirebaseSubscriptionBridge
                .newBuilder()
                .setSubscriptionService(subscriptionService)
                .setFirebaseClient(firebaseClient)
                .build();
    }

    private static Application create() {
        configureEnvironment();

        BoundedContext context = KanbanContext.newBuilder().build();
        CommandService commandService = CommandService.withSingle(context);
        QueryService queryService = QueryService.withSingle(context);
        SubscriptionService subscriptionService = SubscriptionService.withSingle(context);

        Application application = new Application(
                commandService,
                queryService,
                subscriptionService,
                FirebaseClients.getInstance()
        );
        return application;
    }

    private static void configureEnvironment() {
        ServerEnvironment
                .when(Production.class)
                .use(InMemoryStorageFactory.newInstance())
                .use(InMemoryTransportFactory.newInstance());
    }

    FirebaseQueryBridge queryBridge() {
        return queryBridge;
    }

    CommandService commandService() {
        return commandService;
    }

    FirebaseSubscriptionBridge subscriptionBridge() {
        return subscriptionBridge;
    }
}
