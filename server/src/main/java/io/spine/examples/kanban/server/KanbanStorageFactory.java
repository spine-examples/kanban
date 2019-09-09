package io.spine.examples.kanban.server;

import io.spine.server.ContextSpec;
import io.spine.server.aggregate.Aggregate;
import io.spine.server.aggregate.AggregateStorage;
import io.spine.server.delivery.InboxStorage;
import io.spine.server.entity.Entity;
import io.spine.server.projection.Projection;
import io.spine.server.projection.ProjectionStorage;
import io.spine.server.storage.RecordStorage;
import io.spine.server.storage.StorageFactory;
import io.spine.server.storage.memory.InMemoryStorageFactory;

/**
 * An in-memory storage factory to use in the Kanban Board application.
 *
 * <p>This storage factory is sufficient for a simple example but is not eligible to use in a
 * production environment.
 *
 * <p>For the production scenarios consider using
 * <a href="https://github.com/SpineEventEngine/gcloud-java/">Spine library for
 * Google Cloud Datastore</a>.
 */
final class KanbanStorageFactory implements StorageFactory {

    private final InMemoryStorageFactory delegate = InMemoryStorageFactory.newInstance();

    @Override
    public <I> AggregateStorage<I>
    createAggregateStorage(ContextSpec ctx, Class<? extends Aggregate<I, ?, ?>> aggregateCls) {
        return delegate.createAggregateStorage(ctx, aggregateCls);
    }

    @Override
    public <I> RecordStorage<I>
    createRecordStorage(ContextSpec ctx, Class<? extends Entity<I, ?>> entityCls) {
        return delegate.createRecordStorage(ctx, entityCls);
    }

    @Override
    public <I> ProjectionStorage<I>
    createProjectionStorage(ContextSpec ctx, Class<? extends Projection<I, ?, ?>> projectionCls) {
        return delegate.createProjectionStorage(ctx, projectionCls);
    }

    @Override
    public InboxStorage createInboxStorage(boolean multitenant) {
        return delegate.createInboxStorage(multitenant);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
