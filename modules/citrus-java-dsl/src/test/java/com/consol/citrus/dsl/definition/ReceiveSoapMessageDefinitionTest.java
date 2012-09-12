/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.actions.ReceiveMessageAction;
import com.consol.citrus.dsl.TestNGCitrusTestBuilder;
import com.consol.citrus.message.*;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.builder.StaticMessageContentBuilder;
import com.consol.citrus.validation.xml.XmlMessageValidationContext;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.actions.ReceiveSoapMessageAction;
import com.consol.citrus.ws.message.WebServiceReplyMessageReceiver;
import org.easymock.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.*;

/**
 * @author Christoph Deppisch
 */
public class ReceiveSoapMessageDefinitionTest {
    
    private MessageReceiver messageReceiver = EasyMock.createMock(MessageReceiver.class);
    
    private ApplicationContext applicationContext = EasyMock.createMock(ApplicationContext.class);
    
    private Resource resource = EasyMock.createMock(Resource.class);
    
    private SoapAttachment testAttachment = new SoapAttachment();
    
    /**
     * Setup test attachment.
     */
    @BeforeClass
    public void setup() {
        testAttachment.setContentId("attachment01");
        testAttachment.setContent("This is an attachment");
        testAttachment.setContentType("text/plain");
        testAttachment.setCharsetName("UTF-8");
    }
    
    @Test
    public void testSoapAttachment() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .soap()
                    .message(MessageBuilder.withPayload("Foo").setHeader("operation", "foo").build())
                    .attatchment(testAttachment);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveSoapMessageAction.class);
        
        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveSoapMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof StaticMessageContentBuilder);
        Assert.assertEquals(((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).getMessage().getPayload(), "Foo");
        Assert.assertTrue(((StaticMessageContentBuilder<?>)validationContext.getMessageBuilder()).getMessage().getHeaders().containsKey("operation"));
        
        Assert.assertNull(action.getAttachmentResource());
        Assert.assertEquals(action.getAttachmentData(), testAttachment.getContent());
        Assert.assertEquals(action.getControlAttachment().getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getControlAttachment().getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getControlAttachment().getCharsetName(), testAttachment.getCharsetName());
    }
    
    @Test
    public void testSoapAttachmentData() {
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>")
                    .soap()
                    .attatchment(testAttachment.getContentId(), testAttachment.getContentType(), testAttachment.getContent());
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveSoapMessageAction.class);
        
        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveSoapMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadData(), "<TestRequest><Message>Hello World!</Message></TestRequest>");
        
        Assert.assertNull(action.getAttachmentResource());
        Assert.assertEquals(action.getAttachmentData(), testAttachment.getContent());
        Assert.assertEquals(action.getControlAttachment().getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getControlAttachment().getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getControlAttachment().getCharsetName(), testAttachment.getCharsetName());
    }
    
    @Test
    public void testSoapAttachmentResource() {
        final Resource attachmentResource = EasyMock.createMock(Resource.class);
        
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive(messageReceiver)
                    .payload(resource)
                    .soap()
                    .attatchment(testAttachment.getContentId(), testAttachment.getContentType(), attachmentResource);
            }
        };
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 1);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveSoapMessageAction.class);
        
        ReceiveSoapMessageAction action = ((ReceiveSoapMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveSoapMessageAction.class.getSimpleName());
        
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getValidationContexts().size(), 1);
        Assert.assertEquals(action.getValidationContexts().get(0).getClass(), XmlMessageValidationContext.class);
        
        XmlMessageValidationContext validationContext = (XmlMessageValidationContext) action.getValidationContexts().get(0);
        
        Assert.assertTrue(validationContext.getMessageBuilder() instanceof PayloadTemplateMessageBuilder);
        Assert.assertEquals(((PayloadTemplateMessageBuilder)validationContext.getMessageBuilder()).getPayloadResource(), resource);
        
        Assert.assertEquals(action.getAttachmentResource(), attachmentResource);
        Assert.assertNull(action.getAttachmentData());
        Assert.assertEquals(action.getControlAttachment().getContentId(), testAttachment.getContentId());
        Assert.assertEquals(action.getControlAttachment().getContentType(), testAttachment.getContentType());
        Assert.assertEquals(action.getControlAttachment().getCharsetName(), testAttachment.getCharsetName());
    }
    
    @Test
    public void testReceiveBuilderWithReceiverName() {
        WebServiceReplyMessageReceiver replyMessageReceiver = EasyMock.createMock(WebServiceReplyMessageReceiver.class);
        
        TestNGCitrusTestBuilder builder = new TestNGCitrusTestBuilder() {
            @Override
            public void configure() {
                receive("replyMessageReceiver")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
                
                receive("fooMessageReceiver")
                    .payload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            }
        };
        
        builder.setApplicationContext(applicationContext);
        
        reset(applicationContext);
        
        expect(applicationContext.getBean("replyMessageReceiver", MessageReceiver.class)).andReturn(replyMessageReceiver).once();
        expect(applicationContext.getBean("fooMessageReceiver", MessageReceiver.class)).andReturn(messageReceiver).once();
        
        replay(applicationContext);
        
        builder.configure();
        
        Assert.assertEquals(builder.getTestCase().getActions().size(), 2);
        Assert.assertEquals(builder.getTestCase().getActions().get(0).getClass(), ReceiveSoapMessageAction.class);
        Assert.assertEquals(builder.getTestCase().getActions().get(1).getClass(), ReceiveMessageAction.class);
        
        ReceiveMessageAction action = ((ReceiveSoapMessageAction)builder.getTestCase().getActions().get(0));
        Assert.assertEquals(action.getName(), ReceiveSoapMessageAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageReceiver(), replyMessageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        action = ((ReceiveMessageAction)builder.getTestCase().getActions().get(1));
        Assert.assertEquals(action.getName(), ReceiveMessageAction.class.getSimpleName());
        Assert.assertEquals(action.getMessageReceiver(), messageReceiver);
        Assert.assertEquals(action.getMessageType(), MessageType.XML.name());
        
        verify(applicationContext);
    }
    
}
