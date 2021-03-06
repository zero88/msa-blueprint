package io.zero88.qwe.http.client;

import java.util.Objects;

import io.github.zero88.utils.Urls;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;
import io.zero88.qwe.dto.msg.RequestData;
import io.zero88.qwe.dto.msg.ResponseData;
import io.zero88.qwe.http.HostInfo;
import io.zero88.qwe.http.HttpUtils;
import io.zero88.qwe.http.HttpUtils.HttpRequests;
import io.zero88.qwe.http.client.HttpClientConfig.HandlerConfig;
import io.zero88.qwe.http.client.handler.HttpErrorHandler;
import io.zero88.qwe.http.client.handler.HttpRequestMessageComposer;
import io.zero88.qwe.http.client.handler.HttpResponseTextHandler;

import lombok.NonNull;

final class HttpClientDelegateImpl extends ClientDelegate implements HttpClientDelegate {

    HttpClientDelegateImpl(@NonNull HttpClient client) {
        super(client);
    }

    HttpClientDelegateImpl(@NonNull Vertx vertx, HttpClientConfig config) {
        super(vertx, config);
    }

    @Override
    public Future<ResponseData> request(String path, HttpMethod method, RequestData requestData, boolean swallowError) {
        final RequestData reqData = decorator(requestData);
        final HostInfo hostInfo = getHostInfo();
        final HandlerConfig cfg = getHandlerConfig();
        return get().request(method, Urls.buildURL(path, HttpRequests.serializeQuery(reqData.filter())))
                    .flatMap(req -> onConnectionSuccess(req, reqData, cfg, swallowError))
                    .recover(HttpErrorHandler.create(hostInfo, cfg.getHttpErrorHandlerCls()))
                    //FIXME Cache?
                    .onSuccess(res -> HttpClientRegistry.getInstance().remove(hostInfo, false))
                    .onFailure(err -> HttpClientRegistry.getInstance().remove(hostInfo, false));
    }

    @Override
    public Future<ResponseData> upload(String path, String uploadFile) {
        return null;
    }

    @Override
    public Future<ResponseData> push(String path, ReadStream readStream, HttpMethod method) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Future<AsyncFile> download(String path, AsyncFile saveFile) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Future<WriteStream> pull(String path, WriteStream writeStream) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private RequestData decorator(RequestData requestData) {
        RequestData reqData = Objects.isNull(requestData) ? RequestData.builder().build() : requestData;
        final JsonObject headers = reqData.headers();
        if (!headers.containsKey(HttpUtils.NONE_CONTENT_TYPE) &&
            !headers.containsKey(HttpHeaders.CONTENT_TYPE.toString())) {
            headers.put(HttpHeaders.CONTENT_TYPE.toString(), HttpUtils.JSON_CONTENT_TYPE);
        }
        headers.remove(HttpUtils.NONE_CONTENT_TYPE);
        if (!headers.containsKey(HttpHeaders.USER_AGENT.toString())) {
            headers.put(HttpHeaders.USER_AGENT.toString(), this.getUserAgent());
        }
        return reqData;
    }

    private Future<ResponseData> onConnectionSuccess(HttpClientRequest req, RequestData reqData,
                                                     HandlerConfig handlerConfig, boolean swallowError) {
        if (logger().isDebugEnabled()) {
            logger().debug("Send HTTP request {}::{} | <{}>", req.getMethod(), req.absoluteURI(), reqData.toJson());
        } else {
            logger().info("Send HTTP request {}::{}", req.getMethod(), req.absoluteURI());
        }
        return HttpRequestMessageComposer.create(handlerConfig.getReqComposerCls())
                                         .apply(req, reqData)
                                         .send()
                                         .flatMap(HttpResponseTextHandler.create(swallowError,
                                                                                 handlerConfig.getRespTextHandlerCls()));
    }

}
