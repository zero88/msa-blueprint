package io.zero88.qwe.rpc.mock;

import io.zero88.qwe.component.SharedDataLocalProxy;
import io.zero88.qwe.event.EventbusClient;
import io.zero88.qwe.rpc.GatewayServiceInvoker;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MockServiceInvoker implements GatewayServiceInvoker {

    private final String gatewayAddress;
    private final EventbusClient client;
    private final String destination;

    @Override
    public @NonNull String gatewayAddress() {
        return gatewayAddress;
    }

    @Override
    public @NonNull String destination() {
        return destination;
    }

    @Override
    public String requester() {
        return "discovery.test";
    }

    @Override
    public String serviceLabel() {
        return "Mock Service";
    }

    @Override
    public EventbusClient transporter() {
        return client;
    }

    @Override
    public @NonNull SharedDataLocalProxy sharedData() {
        return null;
    }

}