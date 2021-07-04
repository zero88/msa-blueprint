package io.zero88.qwe.http.server.rest.provider;

import io.zero88.qwe.http.server.config.FileDownloadConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RestDownloadConfigProvider {

    @Getter
    private final FileDownloadConfig downloadConfig;

}
