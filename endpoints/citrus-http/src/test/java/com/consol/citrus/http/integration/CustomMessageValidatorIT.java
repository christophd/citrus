/*
 * Copyright 2006-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.http.integration;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.exceptions.ValidationException;
import com.consol.citrus.http.client.HttpClient;
import com.consol.citrus.http.config.annotation.HttpClientConfig;
import com.consol.citrus.http.config.annotation.HttpServerConfig;
import com.consol.citrus.http.server.HttpServer;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import com.consol.citrus.validation.xml.DomXmlMessageValidator;
import com.consol.citrus.validation.xml.XpathMessageValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.StopServerAction.Builder.stop;
import static com.consol.citrus.container.Assert.Builder.assertException;
import static com.consol.citrus.container.FinallySequence.Builder.doFinally;
import static com.consol.citrus.dsl.XpathSupport.xpath;
import static com.consol.citrus.http.actions.HttpActionBuilder.http;

/**
 * @author Christoph Deppisch
 * @since 2.7.5
 */
public class CustomMessageValidatorIT extends TestNGCitrusSpringSupport {

    @CitrusEndpoint
    @HttpClientConfig(requestUrl = "http://localhost:7177")
    private HttpClient httpClient;

    @CitrusEndpoint
    @HttpServerConfig(port = 7177, autoStart = true)
    private HttpServer httpServer;

    @Test
    @CitrusTest
    public void test() {
        this.getClass().getResource("/citrus.properties");

        given(doFinally().actions(
                stop(httpServer)
        ));

        when(http().client(httpClient)
                .send()
                .post("/")
                .message()
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .body("<doc text=\"hello\"/>")
                .fork(true));

        then(http().server(httpServer)
                .receive()
                .post("/")
                .message()
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .validators(new DomXmlMessageValidator(), new XpathMessageValidator())
                .validate(xpath()
                        .expression("//doc/@text", "hello")));

        then(http().server(httpServer)
                .send()
                .response(HttpStatus.OK));
    }

    @Test
    @CitrusTest
    public void testFailure() {
        then(doFinally().actions(
                stop(httpServer)
        ));

        when(http().client(httpClient)
                .send()
                .post("/")
                .message()
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .body("<doc text=\"hello\"/>")
                .fork(true));

        then(assertException()
            .exception(ValidationException.class)
            .when(http().server(httpServer)
                .receive()
                .post("/")
                .message()
                .contentType(MediaType.APPLICATION_XML_VALUE)
                .validators(new DomXmlMessageValidator(), new XpathMessageValidator())
                .validate(xpath()
                        .expression("//doc/@text", "nothello"))));
    }

}
