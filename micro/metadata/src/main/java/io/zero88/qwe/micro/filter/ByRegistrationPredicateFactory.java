package io.zero88.qwe.micro.filter;

import java.util.function.Predicate;

import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.Record;

public final class ByRegistrationPredicateFactory implements ByPredicateFactory, DefaultPredicateFactory {

    @Override
    public String by() {
        return DEFAULT_INDICATOR;
    }

    @Override
    public Predicate<Record> apply(String registration, SearchFlag searchFlag, JsonObject filter) {
        return record -> registration.equalsIgnoreCase(record.getRegistration());
    }

}
