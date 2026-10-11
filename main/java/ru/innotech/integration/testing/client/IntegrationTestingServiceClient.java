package ru.innotech.integration.testing.client;

import com.innotech.client.asyncgateway.AsyncProcessControllerApi;
import com.innotech.client.asyncgateway.IntegrationTestingControllerApi;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;

@Component
@FeignClient(name = "async-process-service", url = "${async-service.url}")
public interface IntegrationTestingServiceClient extends IntegrationTestingControllerApi {}
