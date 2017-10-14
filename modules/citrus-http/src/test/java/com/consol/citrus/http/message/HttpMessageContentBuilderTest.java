/*
 * Copyright 2006-2017 the original author or authors.
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


package com.consol.citrus.http.message;

import com.consol.citrus.context.TestContext;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Simon Hofmann
 * @since 2.7.3
 */
public class HttpMessageContentBuilderTest {

    @Test
    public void testHeaderVariableSubstitution() {
        TestContext ctx = new TestContext();
        ctx.setVariable("testHeader", "foo");
        ctx.setVariable("testValue", "bar");

        HttpMessage msg = new HttpMessage("testPayload");
        msg.setHeader("${testHeader}", "${testValue}");

        HttpMessageContentBuilder builder = new HttpMessageContentBuilder(msg, new StaticMessageContentBuilder(msg));

        Message builtMessage = builder.buildMessageContent(ctx, String.valueOf(MessageType.XML));

        Assert.assertEquals(builtMessage.getHeaders().entrySet().size(), 3);
        Assert.assertEquals(builtMessage.getHeader("foo"), "bar");
    }
}
