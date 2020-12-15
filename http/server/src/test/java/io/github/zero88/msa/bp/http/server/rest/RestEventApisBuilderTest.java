package io.github.zero88.msa.bp.http.server.rest;

import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.zero88.msa.bp.exceptions.InitializerError;
import io.github.zero88.msa.bp.http.server.mock.MockApiDefinition;

public class RestEventApisBuilderTest {

    @Test
    public void test_no_register_data() {
        Assertions.assertThrows(InitializerError.class, () -> new RestEventApisBuilder().validate());
    }

    @Test
    public void test_register_null() {
        Assertions.assertThrows(NullPointerException.class,
                                () -> new RestEventApisBuilder().register((Class<RestEventApi>) null));
    }

    @Test
    public void test_register_one_api() {
        Set<Class<? extends RestEventApi>> validate = new RestEventApisBuilder().register(
            MockApiDefinition.MockRestEventApi.class).validate();
        Assertions.assertEquals(1, validate.size());
    }

    @Test
    public void test_register_many_same_api() {
        Set<Class<? extends RestEventApi>> validate = new RestEventApisBuilder().register(
            MockApiDefinition.MockRestEventApi.class, MockApiDefinition.MockRestEventApi.class).validate();
        Assertions.assertEquals(1, validate.size());
    }

}
