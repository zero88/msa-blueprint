package io.zero88.qwe.http.server.config;

import io.zero88.qwe.IConfig;
import io.zero88.qwe.http.server.BasePaths;
import io.zero88.qwe.http.server.RouterConfig;
import io.zero88.qwe.http.server.download.DownloadFileHandler;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class FileDownloadConfig extends AbstractRouterConfig implements IConfig, RouterConfig {

    public static final String NAME = "__download__";

    private String handlerClass = DownloadFileHandler.class.getName();

    public FileDownloadConfig() {
        super(NAME, FileStorageConfig.class);
    }

    @Override
    protected @NonNull String defaultPath() {
        return BasePaths.ROOT_DOWNLOAD_PATH;
    }

}
