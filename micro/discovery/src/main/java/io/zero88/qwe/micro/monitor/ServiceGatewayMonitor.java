package io.zero88.qwe.micro.monitor;

import java.util.Objects;

import io.github.zero88.repl.Arguments;
import io.github.zero88.repl.ReflectionClass;
import io.github.zero88.utils.Strings;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.zero88.qwe.HasLogger;
import io.zero88.qwe.HasSharedData;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.micro.ServiceDiscoveryApi;

import lombok.NonNull;

public interface ServiceGatewayMonitor extends Handler<Message<Object>>, HasSharedData, HasLogger {

    static <T extends ServiceGatewayMonitor> T create(@NonNull SharedDataLocalProxy proxy, ServiceDiscoveryApi wrapper,
                                                      String className, @NonNull Class<T> fallback) {
        final Arguments args = new Arguments().put(SharedDataLocalProxy.class, proxy)
                                              .put(ServiceDiscoveryApi.class, wrapper);
        if (fallback.getName().equals(className) || Strings.isBlank(className)) {
            return ReflectionClass.createObject(fallback, args);
        }
        T monitor = ReflectionClass.createObject(className, args);
        return Objects.isNull(monitor) ? ReflectionClass.createObject(fallback, args) : monitor;
    }

    @NonNull ServiceDiscoveryApi getDiscovery();

    String monitorName();

}
