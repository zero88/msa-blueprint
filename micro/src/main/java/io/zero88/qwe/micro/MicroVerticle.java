package io.zero88.qwe.micro;

import java.util.Objects;

import io.github.zero88.utils.Strings;
import io.vertx.core.Future;
import io.zero88.qwe.ComponentContext;
import io.zero88.qwe.ComponentVerticle;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.dto.msg.RequestFilter;
import io.zero88.qwe.event.EventBusClient;
import io.zero88.qwe.micro.monitor.ServiceGatewayAnnounceMonitor;
import io.zero88.qwe.micro.monitor.ServiceGatewayUsageMonitor;

import lombok.NonNull;

public final class MicroVerticle extends ComponentVerticle<MicroConfig, MicroContext> {

    MicroVerticle(SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    //TODO: Fix it
    @Override
    public Future<Void> onAsyncStop() {
        return componentContext().getDiscovery().unregister(new RequestFilter());
    }

    @Override
    public Class<MicroConfig> configClass() { return MicroConfig.class; }

    @Override
    public String configFile() { return "micro.json"; }

    @Override
    public MicroContext onSuccess(@NonNull ComponentContext context) {
        logger().info("Setup service discovery...");
        CircuitBreakerConfig breakerConfig = componentConfig.lookup(CircuitBreakerConfig.NAME,
                                                                    CircuitBreakerConfig.class);
        CircuitBreakerWrapper breaker = CircuitBreakerWrapper.create(vertx, breakerConfig);
        ServiceDiscoveryConfig discoveryConfig = componentConfig.lookupOrDefault(ServiceDiscoveryConfig.NAME,
                                                                                 ServiceDiscoveryConfig.class,
                                                                                 ServiceDiscoveryConfig::new);
        ServiceDiscoveryApi discoveryApi = new ServiceDiscoveryApiImpl(sharedData(), discoveryConfig, breaker);
        return new MicroContext(context).setup(setupGateway(discoveryApi, discoveryConfig));
    }

    private ServiceDiscoveryApi setupGateway(ServiceDiscoveryApi serviceDiscovery,
                                             ServiceDiscoveryConfig discoveryConfig) {
        ServiceGatewayConfig gwCfg = componentConfig.lookup(ServiceGatewayConfig.NAME, ServiceGatewayConfig.class);
        if (Objects.isNull(gwCfg) || !gwCfg.isEnabled()) {
            logger().info("Skip setup service discovery gateway");
            return serviceDiscovery;
        }
        vertx.eventBus()
             .consumer(discoveryConfig.getAnnounceAddress(),
                       ServiceGatewayAnnounceMonitor.create(sharedData(), serviceDiscovery,
                                                            gwCfg.getAnnounceMonitorClass()));
        if (Strings.isNotBlank(discoveryConfig.getUsageAddress())) {
            vertx.eventBus()
                 .consumer(discoveryConfig.getUsageAddress(),
                           ServiceGatewayUsageMonitor.create(sharedData(), serviceDiscovery,
                                                             gwCfg.getUsageMonitorClass()));
        }
        EventBusClient.create(sharedData()).register(gwCfg.getIndexAddress(), new ServiceLocator(serviceDiscovery));
        return serviceDiscovery;
    }

}
