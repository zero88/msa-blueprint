package io.zero88.qwe.http.server.config;

import java.util.Objects;

import io.zero88.qwe.IConfig;
import io.zero88.qwe.http.server.RouterConfig;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
abstract class AbstractRouterConfig implements IConfig, RouterConfig {

    @Accessors(fluent = true)
    private final String key;
    @Accessors(fluent = true)
    private final Class<? extends IConfig> parent;
    @Accessors(chain = true)
    private boolean enabled;
    @Accessors(chain = true)
    private String path;

    protected abstract @NonNull String defaultPath();

    protected AbstractRouterConfig(String key, Class<? extends IConfig> parent) {
        this(key, parent, false, null);
    }

    protected AbstractRouterConfig(String key, Class<? extends IConfig> parent, boolean enabled, String path) {
        this.key = key;
        this.parent = parent;
        this.enabled = enabled;
        this.path = Objects.toString(path, defaultPath());
    }

}
