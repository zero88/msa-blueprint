package io.zero88.qwe.http.server.mock;

import io.zero88.qwe.http.server.rest.api.AbstractRestEventApi;

public class MockRestEventApi extends AbstractRestEventApi {

    @Override
    public MockRestEventApi initRouter() {
        this.addRouter("http.server.test", "/test/events", "/:event_id");
        return this;
    }

}
