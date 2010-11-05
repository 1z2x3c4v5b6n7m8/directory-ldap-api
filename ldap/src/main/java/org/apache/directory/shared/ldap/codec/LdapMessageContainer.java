/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.directory.shared.ldap.codec;


import org.apache.directory.shared.asn1.ber.AbstractContainer;
import org.apache.directory.shared.ldap.codec.controls.AbstractControl;
import org.apache.directory.shared.ldap.message.AbandonRequest;
import org.apache.directory.shared.ldap.message.AddRequest;
import org.apache.directory.shared.ldap.message.AddResponse;
import org.apache.directory.shared.ldap.message.BindRequest;
import org.apache.directory.shared.ldap.message.BindResponse;
import org.apache.directory.shared.ldap.message.CompareRequest;
import org.apache.directory.shared.ldap.message.CompareResponse;
import org.apache.directory.shared.ldap.message.DeleteRequest;
import org.apache.directory.shared.ldap.message.DeleteResponse;
import org.apache.directory.shared.ldap.message.ExtendedRequest;
import org.apache.directory.shared.ldap.message.ExtendedResponse;
import org.apache.directory.shared.ldap.message.IntermediateResponse;
import org.apache.directory.shared.ldap.message.Message;
import org.apache.directory.shared.ldap.message.ModifyDnRequest;
import org.apache.directory.shared.ldap.message.ModifyDnResponse;
import org.apache.directory.shared.ldap.message.ModifyRequest;
import org.apache.directory.shared.ldap.message.ModifyResponse;
import org.apache.directory.shared.ldap.message.SearchRequest;
import org.apache.directory.shared.ldap.message.SearchResultDone;
import org.apache.directory.shared.ldap.message.SearchResultEntry;
import org.apache.directory.shared.ldap.message.SearchResultReference;
import org.apache.directory.shared.ldap.message.UnbindRequest;
import org.apache.directory.shared.ldap.message.spi.BinaryAttributeDetector;


/**
 * The LdapMessage container stores all the messages decoded by the Asn1Decoder.
 * When dealing with an incoding PDU, we will obtain a LdapMessage in the
 * container.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LdapMessageContainer extends AbstractContainer
{
    /** The internal ldap message */
    private Message message;

    /** checks if attribute is binary */
    private final BinaryAttributeDetector binaryAttributeDetector;

    /** The message ID */
    private int messageId;

    /** The current control */
    private AbstractControl currentControl;


    /**
     * Creates a new LdapMessageContainer object. We will store ten grammars,
     * it's enough ...
     */
    public LdapMessageContainer()
    {
        this( new BinaryAttributeDetector()
        {
            public boolean isBinary( String attributeId )
            {
                return false;
            }
        } );
    }


    /**
     * Creates a new LdapMessageContainer object. We will store ten grammars,
     * it's enough ...
     *
     * @param binaryAttributeDetector checks if an attribute is binary
     */
    public LdapMessageContainer( BinaryAttributeDetector binaryAttributeDetector )
    {
        super();
        this.stateStack = new int[10];
        this.grammar = LdapMessageGrammar.getInstance();
        this.binaryAttributeDetector = binaryAttributeDetector;
        setTransition( LdapStatesEnum.START_STATE );
    }


    /**
     * @return Returns the ldapMessage.
     */
    public Message getMessage()
    {
        return message;
    }


    /**
     * @return Returns the AbandonRequest stored in the container
     */
    public AbandonRequest getAbandonRequest()
    {
        return ( AbandonRequest ) message;
    }


    /**
     * @return Returns the AddRequest stored in the container
     */
    public AddRequest getAddRequest()
    {
        return ( AddRequest ) message;
    }


    /**
     * @return Returns the AddResponse stored in the container
     */
    public AddResponse getAddResponse()
    {
        return ( AddResponse ) message;
    }


    /**
     * @return Returns the BindRequest stored in the container
     */
    public BindRequest getBindRequest()
    {
        return ( BindRequest ) message;
    }


    /**
     * @return Returns the BindResponse stored in the container
     */
    public BindResponse getBindResponse()
    {
        return ( BindResponse ) message;
    }


    /**
     * @return Returns the CompareRequest stored in the container
     */
    public CompareRequest getCompareRequest()
    {
        return ( CompareRequest ) message;
    }


    /**
     * @return Returns the CompareResponse stored in the container
     */
    public CompareResponse getCompareResponse()
    {
        return ( CompareResponse ) message;
    }


    /**
     * @return Returns the DelRequest stored in the container
     */
    public DeleteRequest getDeleteRequest()
    {
        return ( DeleteRequest ) message;
    }


    /**
     * @return Returns the DelResponse stored in the container
     */
    public DeleteResponse getDeleteResponse()
    {
        return ( DeleteResponse ) message;
    }


    /**
     * @return Returns the ExtendedRequest stored in the container
     */
    public ExtendedRequest getExtendedRequest()
    {
        return ( ExtendedRequest ) message;
    }


    /**
     * @return Returns the ExtendedResponse stored in the container
     */
    public ExtendedResponse getExtendedResponse()
    {
        return ( ExtendedResponse ) message;
    }


    /**
     * @return Returns the IntermediateResponse stored in the container
     */
    public IntermediateResponse getIntermediateResponse()
    {
        return ( IntermediateResponse ) message;
    }


    /**
     * @return Returns the ModifyRequest stored in the container
     */
    public ModifyRequest getModifyRequest()
    {
        return ( ModifyRequest ) message;
    }


    /**
     * @return Returns the ModifyResponse stored in the container
     */
    public ModifyResponse getModifyResponse()
    {
        return ( ModifyResponse ) message;
    }


    /**
     * @return Returns the ModifyDnRequest stored in the container
     */
    public ModifyDnRequest getModifyDnRequest()
    {
        return ( ModifyDnRequest ) message;
    }


    /**
     * @return Returns the ModifyDnResponse stored in the container
     */
    public ModifyDnResponse getModifyDnResponse()
    {
        return ( ModifyDnResponse ) message;
    }


    /**
     * @return Returns the SearchRequest stored in the container
     */
    public SearchRequest getSearchRequest()
    {
        return ( SearchRequest ) message;
    }


    /**
     * @return Returns the SearchResultEntry stored in the container
     */
    public SearchResultEntry getSearchResultEntry()
    {
        return ( SearchResultEntry ) message;
    }


    /**
     * @return Returns the SearchResultReference stored in the container
     */
    public SearchResultReference getSearchResultReference()
    {
        return ( SearchResultReference ) message;
    }


    /**
     * @return Returns the SearchResultDone stored in the container
     */
    public SearchResultDone getSearchResultDone()
    {
        return ( SearchResultDone ) message;
    }


    /**
     * @return Returns the UnbindRequest stored in the container
     */
    public UnbindRequest getUnbindRequest()
    {
        return ( UnbindRequest ) message;
    }


    /**
     * Set a Message Object into the container. It will be completed by the
     * ldapDecoder.
     * 
     * @param message The message to set.
     */
    public void setMessage( Message message )
    {
        this.message = message;
    }


    /**
     * {@inheritDoc}
     */
    public void clean()
    {
        super.clean();

        message = null;
        messageId = 0;
        currentControl = null;
        decodeBytes = 0;
    }


    /**
     * @return Returns true if the attribute is binary.
     * @param id checks if an attribute id is binary
     */
    public boolean isBinary( String id )
    {
        return binaryAttributeDetector.isBinary( id );
    }


    /**
     * @return The message ID
     */
    public int getMessageId()
    {
        return messageId;
    }


    /**
     * Set the message ID
     * @param messageId the id of the message
     */
    public void setMessageId( int messageId )
    {
        this.messageId = messageId;
    }


    /**
     * @return the current control being created
     */
    public AbstractControl getCurrentControl()
    {
        return currentControl;
    }


    /**
     * Store a newly created control
     * @param currentControl The control to store
     */
    public void setCurrentControl( AbstractControl currentControl )
    {
        this.currentControl = currentControl;
    }
}
