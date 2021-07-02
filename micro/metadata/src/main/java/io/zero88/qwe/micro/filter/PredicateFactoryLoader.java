package io.zero88.qwe.micro.filter;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.vertx.core.ServiceHelper;

import lombok.Getter;

public final class PredicateFactoryLoader {

    /**
     * Includes record predicate factory
     *
     * @see RecordPredicateFactory
     */
    @Getter
    private final Collection<RecordPredicateFactory> predicatesFactories;

    public PredicateFactoryLoader() {
        this.predicatesFactories = ServiceHelper.loadFactories(RecordPredicateFactory.class);
    }

}
