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
package com.consol.citrus.channel.selector;

import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.core.MessageSelector;
import org.springframework.util.StringUtils;
import org.springframework.xml.namespace.QNameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.ls.LSException;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.util.XMLUtils;

/**
 * Message selector accepts XML messages according to specified root element QName.
 * 
 * @author Christoph Deppisch
 */
public class RootQNameMessageSelector implements MessageSelector {

    /** Target message XML root QName to look for */
    private QName rootQName;
    
    /** Special identifyer combining header selector with this implementation */
    public static final String ROOT_QNAME_HEADER_SELECTOR = "root-element";
    
    /** Logger */
    private static Logger log = LoggerFactory.getLogger(RootQNameMessageSelector.class);
    
    /**
     * Default constructor using fields.
     */
    public RootQNameMessageSelector(String qNameString) {
        if (QNameUtils.validateQName(qNameString)) {
            this.rootQName = QNameUtils.parseQNameString(qNameString);
        } else {
            throw new CitrusRuntimeException("Invalid root QName string '" + qNameString + "'");
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean accept(Message<?> message) {
        Document doc;
        
        try {
            doc = XMLUtils.parseMessagePayload(message.getPayload().toString());
        } catch (LSException e) {
            log.warn("Root QName message selector ignoring not well-formed XML message payload", e);
            return false; // non XML message - not accepted
        }
        
        if (StringUtils.hasText(rootQName.getNamespaceURI())) {
            return rootQName.equals(QNameUtils.getQNameForNode(doc.getFirstChild())); 
        } else {
            return rootQName.getLocalPart().equals(doc.getFirstChild().getLocalName());
        }
    }

}
