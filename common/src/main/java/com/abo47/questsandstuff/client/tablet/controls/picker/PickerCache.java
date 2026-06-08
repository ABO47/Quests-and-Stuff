package com.abo47.questsandstuff.client.tablet.controls.picker;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PickerCache<OwnerKey, Source, QueryKey, QueryValue> {
    private OwnerKey ownerKey;
    private Source source;
    private boolean sourceCached;
    private QueryKey queryKey;
    private QueryValue queryValue;
    private boolean queryCached;

    public synchronized Source source(OwnerKey ownerKey, Supplier<Source> builder) {
        if (!sourceCached || !Objects.equals(this.ownerKey, ownerKey)) {
            this.ownerKey = ownerKey;
            source = builder.get();
            sourceCached = true;
            clearQuery();
        }
        return source;
    }

    public synchronized QueryValue query(
            OwnerKey ownerKey,
            QueryKey queryKey,
            Supplier<Source> sourceBuilder,
            Function<Source, QueryValue> queryBuilder
    ) {
        Source currentSource = source(ownerKey, sourceBuilder);
        if (queryCached && Objects.equals(this.queryKey, queryKey)) {
            return queryValue;
        }
        this.queryKey = queryKey;
        queryValue = queryBuilder.apply(currentSource);
        queryCached = true;
        return queryValue;
    }

    public synchronized void invalidate() {
        ownerKey = null;
        source = null;
        sourceCached = false;
        clearQuery();
    }

    private void clearQuery() {
        queryKey = null;
        queryValue = null;
        queryCached = false;
    }
}
