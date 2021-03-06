package io.zero88.qwe.http.server.gateway;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.zero88.utils.Urls;
import io.vertx.ext.web.Router;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.Status;
import io.zero88.qwe.exceptions.QWEException;
import io.zero88.qwe.http.server.HttpServerPluginContext;
import io.zero88.qwe.http.server.HttpSystem.GatewaySystem;
import io.zero88.qwe.http.server.RouterCreator;
import io.zero88.qwe.http.server.ServerInfo;
import io.zero88.qwe.http.server.handler.DynamicContextDispatcher;
import io.zero88.qwe.http.server.rest.api.DynamicRestApi;
import io.zero88.qwe.micro.monitor.ServiceGatewayMonitor;

import lombok.NonNull;

public interface DynamicRouterRegister extends ServiceGatewayMonitor, GatewaySystem {

    default @NonNull Logger logger() {
        return LoggerFactory.getLogger(RouterCreator.class);
    }

    default boolean registerRouter(Record record) {
        try {
            DynamicRestApi api = DynamicRestApi.create(record);
            ServerInfo serverInfo = sharedData().getData(HttpServerPluginContext.SERVER_INFO_DATA_KEY);
            Router router = serverInfo.getRouter();
            String gatewayPath = Urls.combinePath(serverInfo.getApiPath(), serverInfo.getServicePath());
            List<String> paths = api.alternativePaths()
                                    .orElse(Collections.singleton(api.path()))
                                    .stream()
                                    .map(p -> Urls.combinePath(gatewayPath, p))
                                    .sorted(Comparator.reverseOrder())
                                    .collect(Collectors.toList());
            if (record.getStatus() == Status.UP) {
                DynamicContextDispatcher handler = DynamicContextDispatcher.create(api, getDiscovery(), gatewayPath);
                paths.forEach(path -> {
                    logger().info(decor("Enable dynamic route | API[{}] | Order[{}] | Path[{}]"), api.name(),
                                  api.order(), path);
                    router.route(path).order(api.order()).handler(handler).enable();
                });
            } else {
                paths.forEach(path -> {
                    logger().info(decor("Disable dynamic route | API: {} | Path: {}"), api.name(), path);
                    router.route(path).disable();
                });
            }
            return true;
        } catch (QWEException e) {
            logger().warn(decor("Cannot register Dynamic service"), e);
            return false;
        }
    }

}
