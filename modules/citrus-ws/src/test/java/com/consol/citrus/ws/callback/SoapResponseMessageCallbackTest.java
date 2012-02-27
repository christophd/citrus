/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.ws.callback;

import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.easymock.EasyMock;
import org.springframework.integration.Message;
import org.springframework.ws.mime.Attachment;
import org.springframework.ws.soap.*;
import org.springframework.xml.transform.StringSource;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.consol.citrus.message.CitrusMessageHeaders;
import com.consol.citrus.ws.SoapAttachment;
import com.consol.citrus.ws.message.CitrusSoapMessageHeaders;
import com.consol.citrus.ws.message.callback.SoapResponseMessageCallback;

/**
 * @author Christoph Deppisch
 */
public class SoapResponseMessageCallbackTest {

    private SoapMessage soapResponse = EasyMock.createMock(SoapMessage.class);
    private SoapEnvelope soapEnvelope = EasyMock.createMock(SoapEnvelope.class);
    private SoapBody soapBody = EasyMock.createMock(SoapBody.class);
    private SoapHeader soapHeader = EasyMock.createMock(SoapHeader.class);
    
    private String responsePayload = "<testMessage>Hello</testMessage>";
    
    @Test
    public void testSoapBody() throws TransformerException, IOException {
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback();
        
        StringSource soapBodySource = new StringSource(responsePayload);
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).once();
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        expect(soapResponse.getSoapAction()).andReturn("").anyTimes();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        callback.doWithMessage(soapResponse);
        
        Message<?> responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertNull(responseMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION));
        Assert.assertNull(responseMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT));
        
        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }
    
    @Test
    public void testSoapAction() throws TransformerException, IOException {
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback();
        
        StringSource soapBodySource = new StringSource(responsePayload);
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).once();
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();
        
        expect(soapResponse.getSoapAction()).andReturn("soapOperation").anyTimes();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        callback.doWithMessage(soapResponse);
        
        Message<?> responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertNull(responseMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT));
        
        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }
    
    @Test
    public void testSoapHeaderContent() throws TransformerException, IOException {
        String soapHeaderContent = "<header>\n" +
        		"<operation>unitTest</operation>\n" +
        		"<messageId>123456789</messageId>\n" +
        		"</header>";
        
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback();
        
        StringSource soapBodySource = new StringSource(responsePayload);
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).once();
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(new StringSource(soapHeaderContent)).times(2);
        
        expect(soapResponse.getSoapAction()).andReturn("\"\"").anyTimes();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        callback.doWithMessage(soapResponse);
        
        Message<?> responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "");
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + soapHeaderContent);
        
        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }
    
    @Test
    public void testSoapHeader() throws TransformerException, IOException {
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback();
        
        StringSource soapBodySource = new StringSource(responsePayload);
        
        SoapHeaderElement soapHeaderElement = EasyMock.createMock(SoapHeaderElement.class);
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        soapHeaders.add(soapHeaderElement);
        
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);
        
        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).once();
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();
        
        expect(soapHeaderElement.getName()).andReturn(new QName("{http://citrusframework.org}citrus:messageId")).once();
        expect(soapHeaderElement.getText()).andReturn("123456789").once();
        
        expect(soapResponse.getSoapAction()).andReturn("soapOperation").anyTimes();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);
        
        callback.doWithMessage(soapResponse);
        
        Message<?> responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertEquals(responseMessage.getHeaders().get("{http://citrusframework.org}citrus:messageId"), "123456789");
        Assert.assertNull(responseMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT));
        
        verify(soapResponse, soapEnvelope, soapBody, soapHeader, soapHeaderElement);
    }
    
    @Test
    public void testSoapAttachment() throws TransformerException, IOException {
        SoapAttachment attachment = new SoapAttachment();
        attachment.setContentId("attContentId");
        attachment.setContent("This is a SOAP attachment\nwith multi-line");
        attachment.setContentType("plain/text");
        
        SoapResponseMessageCallback callback = new SoapResponseMessageCallback();
        
        StringSource soapBodySource = new StringSource(responsePayload);
        
        Set<SoapHeaderElement> soapHeaders = new HashSet<SoapHeaderElement>();
        Set<Attachment> soapAttachments = new HashSet<Attachment>();
        soapAttachments.add(attachment);
        
        reset(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        expect(soapResponse.getEnvelope()).andReturn(soapEnvelope).anyTimes();
        expect(soapResponse.getPayloadSource()).andReturn(soapBodySource).once();
        expect(soapResponse.getSoapHeader()).andReturn(soapHeader).anyTimes();
        expect(soapEnvelope.getHeader()).andReturn(soapHeader).anyTimes();
        expect(soapHeader.examineAllHeaderElements()).andReturn(soapHeaders.iterator()).once();
        expect(soapHeader.getSource()).andReturn(null).once();
        
        expect(soapResponse.getSoapAction()).andReturn("soapOperation").anyTimes();
        
        expect(soapResponse.getAttachments()).andReturn(soapAttachments.iterator()).once();
        
        replay(soapResponse, soapEnvelope, soapBody, soapHeader);
        
        callback.doWithMessage(soapResponse);
        
        Message<?> responseMessage = callback.getResponse();
        Assert.assertEquals(responseMessage.getPayload(), "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + responsePayload);
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusSoapMessageHeaders.SOAP_ACTION), "soapOperation");
        Assert.assertNull(responseMessage.getHeaders().get(CitrusMessageHeaders.HEADER_CONTENT));
        
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusSoapMessageHeaders.CONTENT_ID), attachment.getContentId());
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusSoapMessageHeaders.CONTENT_TYPE), attachment.getContentType());
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusSoapMessageHeaders.CONTENT), attachment.getContent());
        Assert.assertEquals(responseMessage.getHeaders().get(CitrusSoapMessageHeaders.CHARSET_NAME), "UTF-8");
        
        verify(soapResponse, soapEnvelope, soapBody, soapHeader);
    }
}
