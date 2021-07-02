package io.zero88.qwe.storage.json;

import io.zero88.qwe.ComponentContext;
import io.zero88.qwe.ComponentVerticle;
import io.zero88.qwe.SharedDataLocalProxy;
import io.zero88.qwe.event.EventBusClient;
import io.zero88.qwe.storage.json.service.JsonStorageService;

import lombok.NonNull;

public final class JsonStorageVerticle extends ComponentVerticle<StorageConfig, ComponentContext> {

    JsonStorageVerticle(@NonNull SharedDataLocalProxy sharedData) {
        super(sharedData);
    }

    @Override
    public @NonNull Class<StorageConfig> configClass() {
        return StorageConfig.class;
    }

    @Override
    public @NonNull String configFile() {
        return "storage.json";
    }

    @Override
    public void onStart() {
        componentConfig.makeFullPath((String) sharedData().getData(SharedDataLocalProxy.APP_DATADIR_KEY));
        EventBusClient.create(sharedData())
                      .register(componentConfig.getServiceAddress(),
                                JsonStorageService.create(componentConfig, componentConfig.serviceHandlerClass()));
    }

}
