package io.zero88.qwe.http.server.rest.api;

import java.util.Optional;
import java.util.Set;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.types.HttpEndpoint;
import io.vertx.servicediscovery.types.HttpLocation;
import io.zero88.qwe.http.server.BasePaths;
import io.zero88.qwe.utils.PriorityUtils;

import lombok.NonNull;

public interface DynamicHttpRestApi extends DynamicRestApi {

    static DynamicHttpRestApi create(@NonNull Record record) {
        final HttpLocation location = record.getLocation().mapTo(HttpLocation.class);
        final int order = PriorityUtils.priorityOrder(location.getRoot().length());
        final String path = BasePaths.addWildcards(location.getRoot());
        return new DynamicHttpRestApi() {
            @Override
            public String path() { return path; }

            @Override
            public int order() { return order; }

            @Override
            public boolean useRequestData() { return true; }

            @Override
            public @NonNull String name() { return record.getName(); }

            @Override
            public JsonObject byMetadata() { return record.getMetadata(); }
        };
    }

    @Override
    default Optional<Set<String>> alternativePaths() { return Optional.empty(); }

    @Override
    default String serviceType() { return HttpEndpoint.TYPE; }

}
