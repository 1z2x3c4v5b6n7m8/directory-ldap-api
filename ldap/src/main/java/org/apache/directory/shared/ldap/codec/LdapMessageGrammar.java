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


import org.apache.directory.shared.asn1.DecoderException;
import org.apache.directory.shared.asn1.ber.Asn1Container;
import org.apache.directory.shared.asn1.ber.grammar.AbstractGrammar;
import org.apache.directory.shared.asn1.ber.grammar.Action;
import org.apache.directory.shared.asn1.ber.grammar.Grammar;
import org.apache.directory.shared.asn1.ber.grammar.GrammarAction;
import org.apache.directory.shared.asn1.ber.grammar.GrammarTransition;
import org.apache.directory.shared.asn1.ber.tlv.BooleanDecoder;
import org.apache.directory.shared.asn1.ber.tlv.BooleanDecoderException;
import org.apache.directory.shared.asn1.ber.tlv.IntegerDecoder;
import org.apache.directory.shared.asn1.ber.tlv.IntegerDecoderException;
import org.apache.directory.shared.asn1.ber.tlv.LongDecoder;
import org.apache.directory.shared.asn1.ber.tlv.LongDecoderException;
import org.apache.directory.shared.asn1.ber.tlv.TLV;
import org.apache.directory.shared.asn1.ber.tlv.UniversalTag;
import org.apache.directory.shared.asn1.ber.tlv.Value;
import org.apache.directory.shared.asn1.util.OID;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.codec.actions.AttributeDescAction;
import org.apache.directory.shared.ldap.codec.actions.ControlValueAction;
import org.apache.directory.shared.ldap.codec.actions.ControlsInitAction;
import org.apache.directory.shared.ldap.codec.actions.ErrorMessageAction;
import org.apache.directory.shared.ldap.codec.actions.InitAndFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitApproxMatchFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitAssertionValueFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitAttributeDescFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitAttributeDescListAction;
import org.apache.directory.shared.ldap.codec.actions.InitEqualityMatchFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitExtensibleMatchFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitGreaterOrEqualFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitLessOrEqualFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitNotFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitOrFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitPresentFilterAction;
import org.apache.directory.shared.ldap.codec.actions.InitReferralsAction;
import org.apache.directory.shared.ldap.codec.actions.InitSubstringsFilterAction;
import org.apache.directory.shared.ldap.codec.actions.MatchedDNAction;
import org.apache.directory.shared.ldap.codec.actions.ModifyAttributeValueAction;
import org.apache.directory.shared.ldap.codec.actions.ReferralAction;
import org.apache.directory.shared.ldap.codec.actions.ResponseAction;
import org.apache.directory.shared.ldap.codec.actions.ResponseNameAction;
import org.apache.directory.shared.ldap.codec.actions.ResultCodeAction;
import org.apache.directory.shared.ldap.codec.actions.SearchResultAttributeValueAction;
import org.apache.directory.shared.ldap.codec.actions.ServerSASLCredsAction;
import org.apache.directory.shared.ldap.codec.actions.StoreAnyAction;
import org.apache.directory.shared.ldap.codec.actions.StoreFinalAction;
import org.apache.directory.shared.ldap.codec.actions.StoreMatchValueAction;
import org.apache.directory.shared.ldap.codec.actions.StoreReferenceAction;
import org.apache.directory.shared.ldap.codec.actions.StoreTypeMatchingRuleAction;
import org.apache.directory.shared.ldap.codec.actions.ValueAction;
import org.apache.directory.shared.ldap.codec.controls.ControlFactory;
import org.apache.directory.shared.ldap.codec.decorators.MessageDecorator;
import org.apache.directory.shared.ldap.codec.decorators.ModifyRequestDecorator;
import org.apache.directory.shared.ldap.codec.decorators.SearchRequestDecorator;
import org.apache.directory.shared.ldap.codec.decorators.SearchResultEntryDecorator;
import org.apache.directory.shared.ldap.codec.search.ExtensibleMatchFilter;
import org.apache.directory.shared.ldap.codec.search.SubstringFilter;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.filter.SearchScope;
import org.apache.directory.shared.ldap.model.message.AbandonRequest;
import org.apache.directory.shared.ldap.model.message.AbandonRequestImpl;
import org.apache.directory.shared.ldap.model.message.AddRequest;
import org.apache.directory.shared.ldap.model.message.AddRequestImpl;
import org.apache.directory.shared.ldap.model.message.AddResponse;
import org.apache.directory.shared.ldap.model.message.AddResponseImpl;
import org.apache.directory.shared.ldap.model.message.AliasDerefMode;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindRequestImpl;
import org.apache.directory.shared.ldap.model.message.BindResponse;
import org.apache.directory.shared.ldap.model.message.BindResponseImpl;
import org.apache.directory.shared.ldap.model.message.CompareRequest;
import org.apache.directory.shared.ldap.model.message.CompareRequestImpl;
import org.apache.directory.shared.ldap.model.message.CompareResponse;
import org.apache.directory.shared.ldap.model.message.CompareResponseImpl;
import org.apache.directory.shared.ldap.model.message.Control;
import org.apache.directory.shared.ldap.model.message.DeleteRequest;
import org.apache.directory.shared.ldap.model.message.DeleteRequestImpl;
import org.apache.directory.shared.ldap.model.message.DeleteResponse;
import org.apache.directory.shared.ldap.model.message.DeleteResponseImpl;
import org.apache.directory.shared.ldap.model.message.ExtendedRequest;
import org.apache.directory.shared.ldap.model.message.ExtendedRequestImpl;
import org.apache.directory.shared.ldap.model.message.ExtendedResponse;
import org.apache.directory.shared.ldap.model.message.ExtendedResponseImpl;
import org.apache.directory.shared.ldap.model.message.IntermediateResponse;
import org.apache.directory.shared.ldap.model.message.IntermediateResponseImpl;
import org.apache.directory.shared.ldap.model.message.LdapResult;
import org.apache.directory.shared.ldap.model.message.Message;
import org.apache.directory.shared.ldap.model.message.ModifyDnRequest;
import org.apache.directory.shared.ldap.model.message.ModifyDnRequestImpl;
import org.apache.directory.shared.ldap.model.message.ModifyDnResponse;
import org.apache.directory.shared.ldap.model.message.ModifyDnResponseImpl;
import org.apache.directory.shared.ldap.model.message.ModifyRequest;
import org.apache.directory.shared.ldap.model.message.ModifyRequestImpl;
import org.apache.directory.shared.ldap.model.message.ModifyResponse;
import org.apache.directory.shared.ldap.model.message.ModifyResponseImpl;
import org.apache.directory.shared.ldap.model.message.Referral;
import org.apache.directory.shared.ldap.model.message.ReferralImpl;
import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.model.message.ResultResponse;
import org.apache.directory.shared.ldap.model.message.SearchRequest;
import org.apache.directory.shared.ldap.model.message.SearchRequestImpl;
import org.apache.directory.shared.ldap.model.message.SearchResultDone;
import org.apache.directory.shared.ldap.model.message.SearchResultDoneImpl;
import org.apache.directory.shared.ldap.model.message.SearchResultEntry;
import org.apache.directory.shared.ldap.model.message.SearchResultEntryImpl;
import org.apache.directory.shared.ldap.model.message.SearchResultReference;
import org.apache.directory.shared.ldap.model.message.SearchResultReferenceImpl;
import org.apache.directory.shared.ldap.model.message.UnbindRequest;
import org.apache.directory.shared.ldap.model.message.UnbindRequestImpl;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.ldap.model.name.Rdn;
import org.apache.directory.shared.util.StringConstants;
import org.apache.directory.shared.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class implements the LdapMessage message. All the actions are declared
 * in this class. As it is a singleton, these declaration are only done once. If
 * an action is to be added or modified, this is where the work is to be done !
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class LdapMessageGrammar extends AbstractGrammar
{
    // ~ Static fields/initializers
    // -----------------------------------------------------------------

    /** The logger */
    static final Logger LOG = LoggerFactory.getLogger( LdapMessageGrammar.class );

    /** A speedup for logger */
    static final boolean IS_DEBUG = LOG.isDebugEnabled();

    /** The instance of grammar. LdapMessageGrammar is a singleton */
    private static Grammar instance = new LdapMessageGrammar();


    // ~ Constructors
    // -------------------------------------------------------------------------------

    /**
     * Creates a new LdapMessageGrammar object.
     */
    private LdapMessageGrammar()
    {

        setName( LdapMessageGrammar.class.getName() );

        // Create the transitions table
        super.transitions = new GrammarTransition[LdapStatesEnum.LAST_LDAP_STATE.ordinal()][256];

        // ============================================================================================
        // Transition from START to LdapMessage
        // ============================================================================================
        // This is the starting state :
        // LDAPMessage --> SEQUENCE { ... 
        //
        // We have a LDAPMessage, and the tag must be 0x30. 
        //
        // The next state will be LDAP_MESSAGE_STATE
        //
        // We will just check that the length is not null
        super.transitions[LdapStatesEnum.START_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.START_STATE, LdapStatesEnum.LDAP_MESSAGE_STATE, UniversalTag.SEQUENCE.getValue(),
            new GrammarAction( "LdapMessage initialization" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // The Length should not be null
                    if ( tlv.getLength() == 0 )
                    {
                        LOG.error( I18n.err( I18n.ERR_04066 ) );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( I18n.err( I18n.ERR_04067 ) );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from LdapMessage to Message ID
        // --------------------------------------------------------------------------------------------
        // LDAPMessage --> ... MessageId ...
        //
        // Checks that MessageId is in [0 .. 2147483647] and store the value in
        // the LdapMessage Object
        //
        // (2147483647 = Integer.MAX_VALUE)
        // The next state will be MESSAGE_ID_STATE
        //
        // The message ID will be temporarely stored in the container, because we can't store it
        // into an object.
        super.transitions[LdapStatesEnum.LDAP_MESSAGE_STATE.ordinal()][UniversalTag.INTEGER.getValue()] = new GrammarTransition(
            LdapStatesEnum.LDAP_MESSAGE_STATE, LdapStatesEnum.MESSAGE_ID_STATE, UniversalTag.INTEGER.getValue(),
            new GrammarAction( "Store MessageId" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // The current TLV should be a integer
                    // We get it and store it in MessageId
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // The Length should not be null
                    if ( tlv.getLength() == 0 )
                    {
                        LOG.error( I18n.err( I18n.ERR_04068 ) );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( I18n.err( I18n.ERR_04069 ) );
                    }

                    Value value = tlv.getValue();

                    try
                    {
                        int messageId = IntegerDecoder.parse( value, 0, Integer.MAX_VALUE );

                        ldapMessageContainer.setMessageId( messageId );

                        if ( IS_DEBUG )
                        {
                            LOG.debug( "Ldap Message Id has been decoded : " + messageId );
                        }
                    }
                    catch ( IntegerDecoderException ide )
                    {
                        LOG.error( I18n.err( I18n.ERR_04070, Strings.dumpBytes(value.getData()), ide
                            .getLocalizedMessage() ) );

                        // This will generate a PROTOCOL_ERROR                        
                        throw new DecoderException( ide.getMessage() );
                    }
                }
            } );

        // ********************************************************************************************
        // We have a ProtocolOp :
        // If the Tag is 0x42, then it's an UnBindRequest.
        // If the Tag is 0x4A, then it's a DelRequest.
        // If the Tag is 0x50, then it's an AbandonRequest.
        // If the Tag is 0x60, then it's a BindRequest.
        // If the Tag is 0x61, then it's a BindResponse.
        // If the Tag is 0x63, then it's a SearchRequest.
        // If the Tag is 0x64, then it's a SearchResultEntry.
        // If the Tag is 0x65, then it's a SearchResultDone
        // If the Tag is 0x66, then it's a ModifyRequest
        // If the Tag is 0x67, then it's a ModifyResponse.
        // If the Tag is 0x68, then it's an AddRequest.
        // If the Tag is 0x69, then it's an AddResponse.
        // If the Tag is 0x6B, then it's a DelResponse.
        // If the Tag is 0x6C, then it's a ModifyDNRequest.
        // If the Tag is 0x6D, then it's a ModifyDNResponse.
        // If the Tag is 0x6E, then it's a CompareRequest
        // If the Tag is 0x6F, then it's a CompareResponse.
        // If the Tag is 0x73, then it's a SearchResultReference.
        // If the Tag is 0x77, then it's an ExtendedRequest.
        // If the Tag is 0x78, then it's an ExtendedResponse.
        //
        // We create the associated object in this transition, and store it into the container.
        // ********************************************************************************************

        // --------------------------------------------------------------------------------------------
        // Transition from Message ID to UnBindRequest Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... UnBindRequest ...
        // unbindRequest ::= [APPLICATION 2] NULL
        // We have to switch to the UnBindRequest grammar
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.UNBIND_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.UNBIND_REQUEST_STATE, LdapConstants.UNBIND_REQUEST_TAG,
            new GrammarAction( "Unbind Request initialization" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Create the UnbindRequest LdapMessage instance and store it in the container
                    UnbindRequest unbindRequest = new UnbindRequestImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( unbindRequest );

                    TLV tlv = ldapMessageContainer.getCurrentTLV();
                    int expectedLength = tlv.getLength();

                    // The Length should be null
                    if ( expectedLength != 0 )
                    {
                        LOG.error( I18n.err( I18n.ERR_04071, Integer.valueOf( expectedLength ) ) );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( I18n.err( I18n.ERR_04072 ) );
                    }

                    // We can quit now
                    ldapMessageContainer.setGrammarEndAllowed( true );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // transition from UnBindRequest Message to Controls.
        // --------------------------------------------------------------------------------------------
        //         unbindRequest   UnbindRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        super.transitions[LdapStatesEnum.UNBIND_REQUEST_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.UNBIND_REQUEST_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Message ID to DelRequest Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... DelRequest ...
        // delRequest ::= [APPLICATION 10] LDAPDN
        //
        // We store the Dn to bve deleted into the DelRequest object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.DEL_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.DEL_REQUEST_STATE, LdapConstants.DEL_REQUEST_TAG,
            new GrammarAction( "Init del Request" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Create the DeleteRequest LdapMessage instance and store it in the container
                    DeleteRequest delRequest = new DeleteRequestImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( delRequest );

                    // And store the Dn into it
                    // Get the Value and store it in the DelRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // Dn
                    Dn entry = null;

                    if ( tlv.getLength() == 0 )
                    {
                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( I18n.err( I18n.ERR_04073 ) );
                    }
                    else
                    {
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            entry = new Dn( dnStr );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            String msg = I18n.err( I18n.ERR_04074, dnStr, Strings.dumpBytes(dnBytes), ine
                                .getLocalizedMessage() );
                            LOG.error( msg );

                            DeleteResponseImpl response = new DeleteResponseImpl( delRequest.getMessageId() );
                            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                                Dn.EMPTY_DN, ine );
                        }

                        delRequest.setName( entry );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Deleting Dn {}", entry );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // transition from DelRequest Message to Controls.
        // --------------------------------------------------------------------------------------------
        //         delRequest   DelRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        super.transitions[LdapStatesEnum.DEL_REQUEST_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.DEL_REQUEST_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Message ID to AbandonRequest Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... AbandonRequest ...
        // AbandonRequest ::= [APPLICATION 16] MessageID
        //
        // Create the AbandonRequest object, and store the ID in it
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.ABANDON_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.ABANDON_REQUEST_STATE, LdapConstants.ABANDON_REQUEST_TAG,
            new GrammarAction( "Init Abandon Request" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Create the AbandonRequest LdapMessage instance and store it in the container
                    AbandonRequest abandonRequest = new AbandonRequestImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( abandonRequest );

                    // The current TLV should be a integer
                    // We get it and store it in MessageId
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    Value value = tlv.getValue();

                    if ( ( value == null ) || ( value.getData() == null ) )
                    {
                        String msg = I18n.err( I18n.ERR_04075 );
                        LOG.error( msg );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( msg );
                    }

                    try
                    {
                        int abandonnedMessageId = IntegerDecoder.parse( value, 0, Integer.MAX_VALUE );

                        abandonRequest.setAbandoned( abandonnedMessageId );

                        if ( IS_DEBUG )
                        {
                            LOG
                                .debug( "AbandonMessage Id has been decoded : {}", Integer
                                    .valueOf( abandonnedMessageId ) );
                        }

                        ldapMessageContainer.setGrammarEndAllowed( true );

                        return;
                    }
                    catch ( IntegerDecoderException ide )
                    {
                        LOG.error( I18n
                            .err( I18n.ERR_04076, Strings.dumpBytes(value.getData()), ide.getMessage() ) );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( ide.getMessage() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // transition from AbandonRequest Message to Controls.
        // --------------------------------------------------------------------------------------------
        //         abandonRequest   AbandonRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        super.transitions[LdapStatesEnum.ABANDON_REQUEST_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ABANDON_REQUEST_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Message ID to BindRequest Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... BindRequest ...
        // BindRequest ::= [APPLICATION 0] SEQUENCE { ...
        //
        // We have to allocate a BindRequest
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.BIND_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.BIND_REQUEST_STATE, LdapConstants.BIND_REQUEST_TAG,
            new GrammarAction( "Init BindRequest" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Create the BindRequest LdapMessage instance and store it in the container
                    BindRequest bindRequest = new BindRequestImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( bindRequest );

                    // We will check that the request is not null
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04077 );
                        LOG.error( msg );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( msg );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from BindRequest to version
        // --------------------------------------------------------------------------------------------
        // BindRequest ::= [APPLICATION 0] SEQUENCE {
        //     version                 INTEGER (1 ..  127),
        //     ....
        //
        // The Ldap version is parsed and stored into the BindRequest object
        super.transitions[LdapStatesEnum.BIND_REQUEST_STATE.ordinal()][UniversalTag.INTEGER.getValue()] = new GrammarTransition(
            LdapStatesEnum.BIND_REQUEST_STATE, LdapStatesEnum.VERSION_STATE, UniversalTag.INTEGER.getValue(),
            new GrammarAction( "Store version" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    BindRequest bindRequestMessage = ldapMessageContainer.getBindRequest();

                    // The current TLV should be a integer between 1 and 127
                    // We get it and store it in Version
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    Value value = tlv.getValue();

                    try
                    {
                        int version = IntegerDecoder.parse( value, 1, 127 );

                        if ( IS_DEBUG )
                        {
                            LOG.debug( "Ldap version ", Integer.valueOf( version ) );
                        }

                        bindRequestMessage.setVersion3( version == 3 );
                    }
                    catch ( IntegerDecoderException ide )
                    {
                        LOG.error( I18n
                            .err( I18n.ERR_04078, Strings.dumpBytes(value.getData()), ide.getMessage() ) );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( ide.getMessage() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from version to name
        // --------------------------------------------------------------------------------------------
        // BindRequest ::= [APPLICATION 0] SEQUENCE {
        //     ....
        //     name                    LDAPDN,
        //     ....
        //
        // The Ldap version is parsed and stored into the BindRequest object
        super.transitions[LdapStatesEnum.VERSION_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.VERSION_STATE, LdapStatesEnum.NAME_STATE, UniversalTag.OCTET_STRING.getValue(), new GrammarAction(
                "Store Bind Name value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    BindRequest bindRequestMessage = ldapMessageContainer.getBindRequest();

                    // Get the Value and store it in the BindRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length name
                    if ( tlv.getLength() == 0 )
                    {
                        bindRequestMessage.setName( Dn.EMPTY_DN );
                    }
                    else
                    {
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            Dn dn = new Dn( dnStr );
                            bindRequestMessage.setName( dn );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            String msg = "Incorrect Dn given : " + dnStr + " (" + Strings.dumpBytes(dnBytes)
                                + ") is invalid";
                            LOG.error( "{} : {}", msg, ine.getMessage() );

                            BindResponseImpl response = new BindResponseImpl( bindRequestMessage.getMessageId() );

                            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                                Dn.EMPTY_DN, ine );
                        }
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( " The Bind name is {}", bindRequestMessage.getName() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from name to Simple Authentication
        // --------------------------------------------------------------------------------------------
        // BindRequest ::= [APPLICATION 0] SEQUENCE {
        //     ....
        //     authentication          AuthenticationChoice }
        //
        // AuthenticationChoice ::= CHOICE {
        //     simple                  [0] OCTET STRING,
        //     ...
        //
        // We have to create an Authentication Object to store the credentials.
        super.transitions[LdapStatesEnum.NAME_STATE.ordinal()][LdapConstants.BIND_REQUEST_SIMPLE_TAG] = new GrammarTransition(
            LdapStatesEnum.NAME_STATE, LdapStatesEnum.SIMPLE_STATE, LdapConstants.BIND_REQUEST_SIMPLE_TAG,
            new GrammarAction( "Store Bind Simple Authentication value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    BindRequest bindRequestMessage = ldapMessageContainer.getBindRequest();
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Allocate the Authentication Object
                    bindRequestMessage.setSimple( true );

                    // We have to handle the special case of a 0 length simple
                    if ( tlv.getLength() == 0 )
                    {
                        bindRequestMessage.setCredentials( StringConstants.EMPTY_BYTES );
                    }
                    else
                    {
                        bindRequestMessage.setCredentials( tlv.getValue().getData() );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "The simple authentication is : {}", Strings.dumpBytes(bindRequestMessage
                                .getCredentials()) );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // transition from Simple Authentication to Controls.
        // --------------------------------------------------------------------------------------------
        //         bindRequest   BindRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        super.transitions[LdapStatesEnum.SIMPLE_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.SIMPLE_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from name to SASL Authentication
        // --------------------------------------------------------------------------------------------
        // BindRequest ::= [APPLICATION 0] SEQUENCE {
        //     ....
        //     authentication          AuthenticationChoice }
        //
        // AuthenticationChoice ::= CHOICE {
        //     ...
        //     sasl                  [3] SaslCredentials }
        //     ...
        //
        // We have to create an Authentication Object to store the credentials.
        super.transitions[LdapStatesEnum.NAME_STATE.ordinal()][LdapConstants.BIND_REQUEST_SASL_TAG] = new GrammarTransition(
            LdapStatesEnum.NAME_STATE, LdapStatesEnum.SASL_STATE, LdapConstants.BIND_REQUEST_SASL_TAG,
            new GrammarAction( "Initialize Bind SASL Authentication" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    BindRequest bindRequestMessage = ldapMessageContainer.getBindRequest();
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We will check that the sasl is not null
                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04079 );
                        LOG.error( msg );

                        BindResponseImpl response = new BindResponseImpl( bindRequestMessage.getMessageId() );

                        throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_CREDENTIALS,
                            bindRequestMessage.getName(), null );
                    }

                    bindRequestMessage.setSimple( false );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "The SaslCredential has been created" );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from SASL Authentication to Mechanism
        // --------------------------------------------------------------------------------------------
        // SaslCredentials ::= SEQUENCE {
        //     mechanism   LDAPSTRING,
        //     ...
        //
        // We have to store the mechanism.
        super.transitions[LdapStatesEnum.SASL_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.SASL_STATE, LdapStatesEnum.MECHANISM_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store SASL mechanism" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    BindRequest bindRequestMessage = ldapMessageContainer.getBindRequest();
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length
                    // mechanism
                    if ( tlv.getLength() == 0 )
                    {
                        bindRequestMessage.setSaslMechanism( "" );
                    }
                    else
                    {
                        bindRequestMessage.setSaslMechanism( Strings.utf8ToString(tlv.getValue().getData()) );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "The mechanism is : {}", bindRequestMessage.getSaslMechanism() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from Mechanism to Credentials
        // --------------------------------------------------------------------------------------------
        // SaslCredentials ::= SEQUENCE {
        //     ...
        //     credentials OCTET STRING OPTIONAL }
        //
        // We have to store the mechanism.
        super.transitions[LdapStatesEnum.MECHANISM_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.MECHANISM_STATE, LdapStatesEnum.CREDENTIALS_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store SASL credentials" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    BindRequest bindRequestMessage = ldapMessageContainer.getBindRequest();

                    // Get the Value and store it in the BindRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length
                    // credentials
                    if ( tlv.getLength() == 0 )
                    {
                        bindRequestMessage.setCredentials( StringConstants.EMPTY_BYTES );
                    }
                    else
                    {
                        bindRequestMessage.setCredentials( tlv.getValue().getData() );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "The credentials are : {}", Strings.dumpBytes(bindRequestMessage
                                .getCredentials()) );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // transition from from Mechanism to Controls.
        // --------------------------------------------------------------------------------------------
        //         bindRequest   BindRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        super.transitions[LdapStatesEnum.MECHANISM_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.MECHANISM_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // transition from credentials to Controls.
        // --------------------------------------------------------------------------------------------
        //         bindRequest   BindRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        super.transitions[LdapStatesEnum.CREDENTIALS_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.CREDENTIALS_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from MessageId to BindResponse message 
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... BindResponse ...
        // BindResponse ::= [APPLICATION 1] SEQUENCE { ...
        // We have to switch to the BindResponse grammar
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.BIND_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.BIND_RESPONSE_STATE, LdapConstants.BIND_RESPONSE_TAG,
            new GrammarAction( "Init BindReponse" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the BindResponse Object
                    BindResponse bindResponse = new BindResponseImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( bindResponse );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from BindResponse message to Result Code BR 
        // --------------------------------------------------------------------------------------------
        // BindResponse ::= [APPLICATION 1] SEQUENCE {
        //     COMPONENTS OF LDAPResult,
        //     ...
        //
        // LDAPResult ::= SEQUENCE {
        //     resultCode ENUMERATED { 
        //         ...
        // 
        // Stores the result code into the Bind Response object
        super.transitions[LdapStatesEnum.BIND_RESPONSE_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.BIND_RESPONSE_STATE, LdapStatesEnum.RESULT_CODE_BR_STATE, UniversalTag.ENUMERATED.getValue(),
            new ResultCodeAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Result Code BR to Matched Dn BR
        // --------------------------------------------------------------------------------------------
        // LDAPResult ::= SEQUENCE {
        //     ...
        //     matchedDN LDAPDN,
        //     ...
        //
        // Stores the matched Dn
        super.transitions[LdapStatesEnum.RESULT_CODE_BR_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.RESULT_CODE_BR_STATE, LdapStatesEnum.MATCHED_DN_BR_STATE, UniversalTag.OCTET_STRING.getValue(),
            new MatchedDNAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Matched Dn BR to Error Message BR
        // --------------------------------------------------------------------------------------------
        // LDAPResult ::= SEQUENCE {
        //     ...
        //     errorMessage LDAPString,
        //     ...
        //
        // Stores the error message
        super.transitions[LdapStatesEnum.MATCHED_DN_BR_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.MATCHED_DN_BR_STATE, LdapStatesEnum.ERROR_MESSAGE_BR_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ErrorMessageAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Error Message BR to Server SASL credentials 
        // --------------------------------------------------------------------------------------------
        // BindResponse ::= APPLICATION 1] SEQUENCE {
        //     ...
        //     serverSaslCreds [7] OCTET STRING OPTIONAL }
        //
        // Stores the sasl credentials 
        super.transitions[LdapStatesEnum.ERROR_MESSAGE_BR_STATE.ordinal()][LdapConstants.SERVER_SASL_CREDENTIAL_TAG] = new GrammarTransition(
            LdapStatesEnum.ERROR_MESSAGE_BR_STATE, LdapStatesEnum.SERVER_SASL_CREDENTIALS_STATE,
            LdapConstants.SERVER_SASL_CREDENTIAL_TAG, new ServerSASLCredsAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Error Message BR to Referrals BR 
        // --------------------------------------------------------------------------------------------
        // LDAPResult ::= SEQUENCE {
        //     ...
        //     referral   [3] Referral OPTIONNAL }
        //
        // Initialiaze the referrals list 
        super.transitions[LdapStatesEnum.ERROR_MESSAGE_BR_STATE.ordinal()][LdapConstants.LDAP_RESULT_REFERRAL_SEQUENCE_TAG] = new GrammarTransition(
            LdapStatesEnum.ERROR_MESSAGE_BR_STATE, LdapStatesEnum.REFERRALS_BR_STATE,
            LdapConstants.LDAP_RESULT_REFERRAL_SEQUENCE_TAG, new InitReferralsAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referrals BR to Referral BR 
        // --------------------------------------------------------------------------------------------
        // Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI (RFC 4511)
        // URI ::= LDAPString
        //
        // Add a first Referral
        super.transitions[LdapStatesEnum.REFERRALS_BR_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.REFERRALS_BR_STATE, LdapStatesEnum.REFERRAL_BR_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ReferralAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referral BR to Referral BR 
        // --------------------------------------------------------------------------------------------
        // Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI (RFC 4511)
        // URI ::= LDAPString
        //
        // Adda new Referral
        super.transitions[LdapStatesEnum.REFERRAL_BR_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.REFERRAL_BR_STATE, LdapStatesEnum.REFERRAL_BR_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ReferralAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referral BR to Server SASL Credentials 
        // --------------------------------------------------------------------------------------------
        // Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI (RFC 4511)
        // URI ::= LDAPString
        //
        // Adda new Referral
        super.transitions[LdapStatesEnum.REFERRAL_BR_STATE.ordinal()][LdapConstants.SERVER_SASL_CREDENTIAL_TAG] = new GrammarTransition(
            LdapStatesEnum.REFERRAL_BR_STATE, LdapStatesEnum.SERVER_SASL_CREDENTIALS_STATE,
            LdapConstants.SERVER_SASL_CREDENTIAL_TAG, new ServerSASLCredsAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referral BR to Controls 
        // --------------------------------------------------------------------------------------------
        //         bindResponse   BindResponse,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Adda new Referral
        super.transitions[LdapStatesEnum.REFERRAL_BR_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.REFERRAL_BR_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Error Message BR to controls 
        // --------------------------------------------------------------------------------------------
        //         bindResponse   BindResponse,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        //  
        super.transitions[LdapStatesEnum.ERROR_MESSAGE_BR_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ERROR_MESSAGE_BR_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Server SASL credentials to Controls 
        // --------------------------------------------------------------------------------------------
        //         bindResponse   BindResponse,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        super.transitions[LdapStatesEnum.SERVER_SASL_CREDENTIALS_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.SERVER_SASL_CREDENTIALS_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Result Code to Matched Dn
        // --------------------------------------------------------------------------------------------
        // LDAPResult ::= SEQUENCE {
        //     ...
        //     matchedDN LDAPDN,
        //     ...
        //
        // Stores the matched Dn
        super.transitions[LdapStatesEnum.RESULT_CODE_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.RESULT_CODE_STATE, LdapStatesEnum.MATCHED_DN_STATE, UniversalTag.OCTET_STRING.getValue(),
            new MatchedDNAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Matched Dn to Error Message
        // --------------------------------------------------------------------------------------------
        // LDAPResult ::= SEQUENCE {
        //     ...
        //     errorMessage LDAPString,
        //     ...
        //
        // Stores the error message
        super.transitions[LdapStatesEnum.MATCHED_DN_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.MATCHED_DN_STATE, LdapStatesEnum.ERROR_MESSAGE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ErrorMessageAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Error Message to Referrals
        // --------------------------------------------------------------------------------------------
        // LDAPResult ::= SEQUENCE {
        //     ...
        //     referral   [3] Referral OPTIONNAL }
        //
        // Initialiaze the referrals list 
        super.transitions[LdapStatesEnum.ERROR_MESSAGE_STATE.ordinal()][LdapConstants.LDAP_RESULT_REFERRAL_SEQUENCE_TAG] = new GrammarTransition(
            LdapStatesEnum.ERROR_MESSAGE_STATE, LdapStatesEnum.REFERRALS_STATE,
            LdapConstants.LDAP_RESULT_REFERRAL_SEQUENCE_TAG, new GrammarAction( "Init referrals list" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // If we hae a Referrals sequence, then it should not be empty
                    // sasl credentials
                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04080 );
                        LOG.error( msg );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( msg );
                    }

                    Message response = ldapMessageContainer.getMessage();
                    LdapResult ldapResult = ( ( ResultResponse ) response ).getLdapResult();

                    Referral referral = new ReferralImpl();

                    ldapResult.setReferral( referral );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from Referrals to Referral 
        // --------------------------------------------------------------------------------------------
        // Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI (RFC 4511)
        // URI ::= LDAPString
        //
        // Add a first Referral
        super.transitions[LdapStatesEnum.REFERRALS_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.REFERRALS_STATE, LdapStatesEnum.REFERRAL_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ReferralAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referral to Referral 
        // --------------------------------------------------------------------------------------------
        // Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI (RFC 4511)
        // URI ::= LDAPString
        //
        // Adda new Referral
        super.transitions[LdapStatesEnum.REFERRAL_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.REFERRAL_STATE, LdapStatesEnum.REFERRAL_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ReferralAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referral to Controls 
        // --------------------------------------------------------------------------------------------
        //         xxxResponse   xxxResponse,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Adda new Referral
        super.transitions[LdapStatesEnum.REFERRAL_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.REFERRAL_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Error Message to controls 
        // --------------------------------------------------------------------------------------------
        //         xxxResponse   xxxResponse,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        //  
        super.transitions[LdapStatesEnum.ERROR_MESSAGE_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ERROR_MESSAGE_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from MessageId to SearchResultEntry Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... SearchResultEntry ...
        // SearchResultEntry ::= [APPLICATION 4] SEQUENCE { ...
        //
        // Initialize the searchResultEntry object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.SEARCH_RESULT_ENTRY_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.SEARCH_RESULT_ENTRY_STATE,
            LdapConstants.SEARCH_RESULT_ENTRY_TAG, new GrammarAction( "Init SearchResultEntry" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the SearchResultEntry Object
                    SearchResultEntry searchResultEntry = new SearchResultEntryImpl( ldapMessageContainer
                        .getMessageId() );
                    ldapMessageContainer.setMessage( searchResultEntry );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from SearchResultEntry Message to ObjectName
        // --------------------------------------------------------------------------------------------
        // SearchResultEntry ::= [APPLICATION 4] SEQUENCE { ...
        // objectName LDAPDN,
        // ...
        //
        // Store the object name.
        super.transitions[LdapStatesEnum.SEARCH_RESULT_ENTRY_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.SEARCH_RESULT_ENTRY_STATE, LdapStatesEnum.OBJECT_NAME_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store search result entry object name Value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    SearchResultEntry searchResultEntry = ldapMessageContainer.getSearchResultEntry();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    Dn objectName = Dn.EMPTY_DN;

                    // Store the value.
                    if ( tlv.getLength() == 0 )
                    {
                        searchResultEntry.setObjectName( objectName );
                    }
                    else
                    {
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            objectName = new Dn( dnStr );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            // This is for the client side. We will never decode LdapResult on the server
                            String msg = "The Dn " + Strings.dumpBytes(dnBytes) + "is invalid : "
                                + ine.getMessage();
                            LOG.error( "{} : {}", msg, ine.getMessage() );
                            throw new DecoderException( msg, ine );
                        }

                        searchResultEntry.setObjectName( objectName );
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Search Result Entry Dn found : {}", searchResultEntry.getObjectName() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from ObjectName to AttributesSR
        // --------------------------------------------------------------------------------------------
        // SearchResultEntry ::= [APPLICATION 4] SEQUENCE { ...
        // ...
        // attributes PartialAttributeList }
        //
        // PartialAttributeList ::= *SEQUENCE* OF SEQUENCE {
        // ...
        //
        // We may have no attributes. Just allows the grammar to end
        super.transitions[LdapStatesEnum.OBJECT_NAME_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.OBJECT_NAME_STATE, LdapStatesEnum.ATTRIBUTES_SR_STATE, UniversalTag.SEQUENCE.getValue(),
            new GrammarAction( "Pop and end allowed" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    container.setGrammarEndAllowed( true );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from AttributesSR to PartialAttributesList
        // --------------------------------------------------------------------------------------------
        // SearchResultEntry ::= [APPLICATION 4] SEQUENCE { ...
        // ...
        // attributes PartialAttributeList }
        //
        // PartialAttributeList ::= SEQUENCE OF *SEQUENCE* {
        // ...
        //
        // nothing to do
        super.transitions[LdapStatesEnum.ATTRIBUTES_SR_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTES_SR_STATE, LdapStatesEnum.PARTIAL_ATTRIBUTES_LIST_STATE,
            UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from AttributesSR to Controls
        // --------------------------------------------------------------------------------------------
        //     searchResultEntry SearchResultEntry,
        //     ... },
        // controls   [0] Controls OPTIONAL }
        //
        // Initialize the controls
        super.transitions[LdapStatesEnum.ATTRIBUTES_SR_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTES_SR_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from PartialAttributesList to typeSR
        // --------------------------------------------------------------------------------------------
        // SearchResultEntry ::= [APPLICATION 4] SEQUENCE { ...
        // ...
        // attributes PartialAttributeList }
        //
        // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
        //     type  AttributeDescription,
        //     ...
        //
        // Store the attribute's name.
        super.transitions[LdapStatesEnum.PARTIAL_ATTRIBUTES_LIST_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.PARTIAL_ATTRIBUTES_LIST_STATE, LdapStatesEnum.TYPE_SR_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store search result entry object name Value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    SearchResultEntryDecorator searchResultEntry = ldapMessageContainer.getSearchResultEntry();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    String type = "";

                    // Store the name
                    if ( tlv.getLength() == 0 )
                    {
                        // The type can't be null
                        String msg = I18n.err( I18n.ERR_04081 );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }
                    else
                    {
                        type = getType(tlv.getValue().getData());

                        try
                        {
                            searchResultEntry.addAttribute( type );
                        }
                        catch ( LdapException ine )
                        {
                            // This is for the client side. We will never decode LdapResult on the server
                            String msg = "The Attribute type " + type + "is invalid : " + ine.getMessage();
                            LOG.error( "{} : {}", msg, ine.getMessage() );
                            throw new DecoderException( msg, ine );
                        }
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Attribute type : {}", type );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from typeSR to ValsSR
        // --------------------------------------------------------------------------------------------
        // SearchResultEntry ::= [APPLICATION 4] SEQUENCE { ...
        // ...
        // attributes PartialAttributeList }
        //
        // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // We may have no value. Just allows the grammar to end
        super.transitions[LdapStatesEnum.TYPE_SR_STATE.ordinal()][UniversalTag.SET.getValue()] = new GrammarTransition(
            LdapStatesEnum.TYPE_SR_STATE, LdapStatesEnum.VALS_SR_STATE, UniversalTag.SET.getValue(), new GrammarAction(
                "Grammar end allowed" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    container.setGrammarEndAllowed( true );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from ValsSR to AttributeValueSR
        // --------------------------------------------------------------------------------------------
        // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // AttributeValue ::= OCTET STRING
        // 
        // Store the attribute value
        super.transitions[LdapStatesEnum.VALS_SR_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.VALS_SR_STATE, LdapStatesEnum.ATTRIBUTE_VALUE_SR_STATE, UniversalTag.OCTET_STRING.getValue(),
            new SearchResultAttributeValueAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from ValsSR to PartialAttributesList
        // --------------------------------------------------------------------------------------------
        // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        // 
        // Loop when we don't have any attribute value. Nothing to do
        super.transitions[LdapStatesEnum.VALS_SR_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.VALS_SR_STATE, LdapStatesEnum.PARTIAL_ATTRIBUTES_LIST_STATE, UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from ValsSR to Controls
        // --------------------------------------------------------------------------------------------
        //     searchResultEntry SearchResultEntry,
        //     ... },
        // controls   [0] Controls OPTIONAL }
        //
        // Initialize the controls
        super.transitions[LdapStatesEnum.VALS_SR_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.VALS_SR_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AttributeValueSR to AttributeValueSR 
        // --------------------------------------------------------------------------------------------
        // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // AttributeValue ::= OCTET STRING
        // 
        // Store the attribute value
        super.transitions[LdapStatesEnum.ATTRIBUTE_VALUE_SR_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_VALUE_SR_STATE, LdapStatesEnum.ATTRIBUTE_VALUE_SR_STATE,
            UniversalTag.OCTET_STRING.getValue(), new SearchResultAttributeValueAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AttributeValueSR to PartialAttributesList
        // --------------------------------------------------------------------------------------------
        // PartialAttributeList ::= SEQUENCE OF SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        // 
        // Loop when we don't have any attribute value. Nothing to do
        super.transitions[LdapStatesEnum.ATTRIBUTE_VALUE_SR_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_VALUE_SR_STATE, LdapStatesEnum.PARTIAL_ATTRIBUTES_LIST_STATE,
            UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from AttributeValueSR to Controls
        // --------------------------------------------------------------------------------------------
        //     searchResultEntry SearchResultEntry,
        //     ... },
        // controls   [0] Controls OPTIONAL }
        //
        // Initialize the controls
        super.transitions[LdapStatesEnum.ATTRIBUTE_VALUE_SR_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_VALUE_SR_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // SearchResultDone Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... SearchResultDone ...
        // SearchResultDone ::= [APPLICATION 5] SEQUENCE { ...
        // 
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.SEARCH_RESULT_DONE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.SEARCH_RESULT_DONE_STATE,
            LdapConstants.SEARCH_RESULT_DONE_TAG, new GrammarAction( "Init search Result Done" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the SearchResultDone Object
                    SearchResultDone searchResultDone = new SearchResultDoneImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( searchResultDone );

                    LOG.debug( "Search Result Done found" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // SearchResultDone Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... SearchResultDone ...
        // SearchResultDone ::= [APPLICATION 5] LDAPResult
        //
        // LDAPResult ::= SEQUENCE {
        //     resultCode    ENUMERATED {
        //         ...
        // 
        // Stores the result code
        super.transitions[LdapStatesEnum.SEARCH_RESULT_DONE_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.SEARCH_RESULT_DONE_STATE, LdapStatesEnum.RESULT_CODE_STATE, UniversalTag.ENUMERATED.getValue(),
            new ResultCodeAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Message ID to ModifyRequest Message
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ModifyRequest ...
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE { ...
        //
        // Creates the Modify Request object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.MODIFY_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.MODIFY_REQUEST_STATE, LdapConstants.MODIFY_REQUEST_TAG,
            new GrammarAction( "Init ModifyRequest" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the ModifyRequest Object
                    ModifyRequest modifyRequest = new ModifyRequestImpl( ldapMessageContainer.getMessageId() );
                    ModifyRequestDecorator modifyRequestDecorator = new ModifyRequestDecorator( modifyRequest );
                    ldapMessageContainer.setMessage( modifyRequestDecorator );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from ModifyRequest Message to Object
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     object    LDAPDN,
        //     ...
        //
        // Stores the object Dn
        super.transitions[LdapStatesEnum.MODIFY_REQUEST_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.MODIFY_REQUEST_STATE, LdapStatesEnum.OBJECT_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store Modify request object Value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    ModifyRequestDecorator modifyRequestDecorator = ldapMessageContainer.getModifyRequestDecorator();
                    ModifyRequest modifyRequest = (ModifyRequest)modifyRequestDecorator.getDecoratedMessage();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    Dn object = Dn.EMPTY_DN;

                    // Store the value.
                    if ( tlv.getLength() == 0 )
                    {
                        ((ModifyRequest)modifyRequestDecorator.getDecoratedMessage()).setName( object );
                    }
                    else
                    {
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            object = new Dn( dnStr );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            String msg = "Invalid Dn given : " + dnStr + " (" + Strings.dumpBytes(dnBytes)
                                + ") is invalid";
                            LOG.error( "{} : {}", msg, ine.getMessage() );

                            ModifyResponseImpl response = new ModifyResponseImpl( modifyRequest.getMessageId() );
                            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                                Dn.EMPTY_DN, ine );
                        }

                        modifyRequest.setName( object );
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Modification of Dn {}", modifyRequest.getName() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from Object to modifications
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification *SEQUENCE OF* SEQUENCE {
        //     ...
        //
        // Initialize the modifications list
        super.transitions[LdapStatesEnum.OBJECT_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.OBJECT_STATE, LdapStatesEnum.MODIFICATIONS_STATE, UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from modifications to modification sequence
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification SEQUENCE OF *SEQUENCE* {
        //     ...
        //
        // Nothing to do
        super.transitions[LdapStatesEnum.MODIFICATIONS_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.MODIFICATIONS_STATE, LdapStatesEnum.MODIFICATIONS_SEQ_STATE, UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from modification sequence to operation
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification SEQUENCE OF SEQUENCE {
        //         operation  ENUMERATED {
        //             ...
        //
        // Store operation type
        super.transitions[LdapStatesEnum.MODIFICATIONS_SEQ_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.MODIFICATIONS_SEQ_STATE, LdapStatesEnum.OPERATION_STATE, UniversalTag.ENUMERATED.getValue(),
            new GrammarAction( "Store operation type" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    ModifyRequestDecorator modifyRequestDecorator = ldapMessageContainer.getModifyRequestDecorator();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Decode the operation type
                    int operation = 0;

                    try
                    {
                        operation = IntegerDecoder.parse( tlv.getValue(), 0, 2 );
                    }
                    catch ( IntegerDecoderException ide )
                    {
                        String msg = I18n.err( I18n.ERR_04082, Strings.dumpBytes(tlv.getValue().getData()) );
                        LOG.error( msg );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( msg );
                    }

                    // Store the current operation.
                    modifyRequestDecorator.setCurrentOperation( operation );

                    if ( IS_DEBUG )
                    {
                        switch ( operation )
                        {
                            case LdapConstants.OPERATION_ADD:
                                LOG.debug( "Modification operation : ADD" );
                                break;

                            case LdapConstants.OPERATION_DELETE:
                                LOG.debug( "Modification operation : DELETE" );
                                break;

                            case LdapConstants.OPERATION_REPLACE:
                                LOG.debug( "Modification operation : REPLACE" );
                                break;
                        }
                    }

                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from operation to modification
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification SEQUENCE OF SEQUENCE {
        //             ...
        //         modification   AttributeTypeAndValues }
        //
        // AttributeTypeAndValues ::= SEQUENCE {
        //     ...
        //
        // Nothing to do
        super.transitions[LdapStatesEnum.OPERATION_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.OPERATION_STATE, LdapStatesEnum.MODIFICATION_STATE, UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from modification to TypeMod
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification SEQUENCE OF SEQUENCE {
        //             ...
        //         modification   AttributeTypeAndValues }
        //
        // AttributeTypeAndValues ::= SEQUENCE {
        //     type AttributeDescription,
        //     ...
        //
        // Stores the type
        super.transitions[LdapStatesEnum.MODIFICATION_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.MODIFICATION_STATE, LdapStatesEnum.TYPE_MOD_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store type" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    ModifyRequestDecorator modifyRequestDecorator = ldapMessageContainer.getModifyRequestDecorator();
                    ModifyRequest modifyRequest = (ModifyRequest)modifyRequestDecorator.getDecoratedMessage();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Store the value. It can't be null
                    String type = null;

                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04083 );
                        LOG.error( msg );

                        ModifyResponseImpl response = new ModifyResponseImpl( modifyRequest.getMessageId() );
                        throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX,
                            modifyRequest.getName(), null );
                    }
                    else
                    {
                        type = getType(tlv.getValue().getData());
                        modifyRequestDecorator.addAttributeTypeAndValues( type );
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Modifying type : {}", type );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from TypeMod to vals
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification SEQUENCE OF SEQUENCE {
        //             ...
        //         modification   AttributeTypeAndValues }
        //
        // AttributeTypeAndValues ::= SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // Initialize the list of values
        super.transitions[LdapStatesEnum.TYPE_MOD_STATE.ordinal()][UniversalTag.SET.getValue()] = new GrammarTransition(
            LdapStatesEnum.TYPE_MOD_STATE, LdapStatesEnum.VALS_STATE, UniversalTag.SET.getValue(), new GrammarAction(
                "Init Attribute vals" )
            {
                public void action( Asn1Container container )
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // If the length is null, we store an empty value
                    if ( tlv.getLength() == 0 )
                    {
                        LOG.debug( "No vals for this attribute" );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    LOG.debug( "Some vals are to be decoded" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from vals to Attribute Value
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification SEQUENCE OF SEQUENCE {
        //             ...
        //         modification   AttributeTypeAndValues }
        //
        // AttributeTypeAndValues ::= SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // AttributeValue ::= OCTET STRING
        //
        // Stores a value
        super.transitions[LdapStatesEnum.VALS_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.VALS_STATE, LdapStatesEnum.ATTRIBUTE_VALUE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ModifyAttributeValueAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from vals to ModificationsSeq
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification SEQUENCE OF *SEQUENCE* {
        //             ...
        //         modification   AttributeTypeAndValues }
        //
        // AttributeTypeAndValues ::= SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // AttributeValue ::= OCTET STRING
        //
        // Nothing to do
        super.transitions[LdapStatesEnum.VALS_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.VALS_STATE, LdapStatesEnum.MODIFICATIONS_SEQ_STATE, UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from vals to Controls
        // --------------------------------------------------------------------------------------------
        //     modifyRequest ModifyRequest,
        //     ... },
        // controls   [0] Controls OPTIONAL }
        //
        // Nothing to do
        super.transitions[LdapStatesEnum.VALS_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.VALS_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Attribute Value to Attribute Value
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification SEQUENCE OF SEQUENCE {
        //             ...
        //         modification   AttributeTypeAndValues }
        //
        // AttributeTypeAndValues ::= SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // AttributeValue ::= OCTET STRING
        //
        // Stores a value
        super.transitions[LdapStatesEnum.ATTRIBUTE_VALUE_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_VALUE_STATE, LdapStatesEnum.ATTRIBUTE_VALUE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ModifyAttributeValueAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Attribute Value to ModificationsSeq
        // --------------------------------------------------------------------------------------------
        // ModifyRequest ::= [APPLICATION 6] SEQUENCE {
        //     ...
        //     modification SEQUENCE OF *SEQUENCE* {
        //             ...
        //         modification   AttributeTypeAndValues }
        //
        // AttributeTypeAndValues ::= SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // AttributeValue ::= OCTET STRING
        //
        // Nothing to do
        super.transitions[LdapStatesEnum.ATTRIBUTE_VALUE_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_VALUE_STATE, LdapStatesEnum.MODIFICATIONS_SEQ_STATE, UniversalTag.SEQUENCE.getValue(),
            null );

        // --------------------------------------------------------------------------------------------
        // Transition from Attribute Value to Controls
        // --------------------------------------------------------------------------------------------
        //     modifyRequest ModifyRequest,
        //     ... },
        // controls   [0] Controls OPTIONAL }
        //
        // Nothing to do
        super.transitions[LdapStatesEnum.ATTRIBUTE_VALUE_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_VALUE_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // ModifyResponse Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ModifyResponse ...
        // ModifyResponse ::= [APPLICATION 7] SEQUENCE { ...
        // We have to switch to the ModifyResponse grammar
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.MODIFY_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.MODIFY_RESPONSE_STATE, LdapConstants.MODIFY_RESPONSE_TAG,
            new GrammarAction( "Init ModifyResponse" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the ModifyResponse Object
                    ModifyResponse modifyResponse = new ModifyResponseImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( modifyResponse );

                    LOG.debug( "Modify response" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // ModifyResponse Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ModifyResponse ...
        // ModifyResponse ::= [APPLICATION 7] LDAPResult
        //
        // LDAPResult ::= SEQUENCE {
        //     resultCode    ENUMERATED {
        //         ...
        // 
        // Stores the result code
        super.transitions[LdapStatesEnum.MODIFY_RESPONSE_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.MODIFY_RESPONSE_STATE, LdapStatesEnum.RESULT_CODE_STATE, UniversalTag.ENUMERATED.getValue(),
            new ResultCodeAction() );

        // --------------------------------------------------------------------------------------------
        // AddRequest Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... AddRequest ...
        // AddRequest ::= [APPLICATION 8] SEQUENCE { ...
        //
        // Initialize the AddRequest object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.ADD_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.ADD_REQUEST_STATE, LdapConstants.ADD_REQUEST_TAG,
            new GrammarAction( "Init addRequest" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the AddRequest Object
                    int messageId = ldapMessageContainer.getMessageId();
                    AddRequest addRequest = new AddRequestImpl( messageId );
                    ldapMessageContainer.setMessage( addRequest );

                    // We will check that the request is not null
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04084 );
                        LOG.error( msg );

                        // Will generate a PROTOCOL_ERROR
                        throw new DecoderException( msg );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from Add Request to Entry
        // --------------------------------------------------------------------------------------------
        // AddRequest ::= [APPLICATION 8] SEQUENCE {
        //     entry           LDAPDN,
        //     ...
        //
        // Stores the Dn
        super.transitions[LdapStatesEnum.ADD_REQUEST_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.ADD_REQUEST_STATE, LdapStatesEnum.ENTRY_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store add request object Value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    AddRequest addRequest = ldapMessageContainer.getAddRequest();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Store the entry. It can't be null
                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04085 );
                        LOG.error( msg );

                        AddResponseImpl response = new AddResponseImpl( addRequest.getMessageId() );

                        // I guess that trying to add an entry which Dn is empty is a naming violation...
                        // Not 100% sure though ...
                        throw new ResponseCarryingException( msg, response, ResultCodeEnum.NAMING_VIOLATION,
                            Dn.EMPTY_DN, null );
                    }
                    else
                    {
                        Dn entryDn = null;
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            entryDn = new Dn( dnStr );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            String msg = "Invalid Dn given : " + dnStr + " (" + Strings.dumpBytes(dnBytes)
                                + ") is invalid";
                            LOG.error( "{} : {}", msg, ine.getMessage() );

                            AddResponseImpl response = new AddResponseImpl( addRequest.getMessageId() );
                            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                                Dn.EMPTY_DN, ine );
                        }

                        addRequest.setEntryDn( entryDn );
                    }

                    LOG.debug( "Adding an entry with Dn : {}", addRequest.getEntry() );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from Entry to Attributes
        // --------------------------------------------------------------------------------------------
        // AddRequest ::= [APPLICATION 8] SEQUENCE {
        //     ...
        //    attributes AttributeList }
        //
        // AttributeList ::= SEQUENCE OF ... 
        //
        // Initialize the attribute list
        super.transitions[LdapStatesEnum.ENTRY_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.ENTRY_STATE, LdapStatesEnum.ATTRIBUTES_STATE, UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from Attributes to Attribute
        // --------------------------------------------------------------------------------------------
        // AttributeList ::= SEQUENCE OF SEQUENCE {
        //
        // We don't do anything in this transition. The attribute will be created when we met the type
        super.transitions[LdapStatesEnum.ATTRIBUTES_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTES_STATE, LdapStatesEnum.ATTRIBUTE_STATE, UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from Attribute to type
        // --------------------------------------------------------------------------------------------
        // AttributeList ::= SEQUENCE OF SEQUENCE {
        //     type    AttributeDescription,
        //     ...
        //
        // AttributeDescription LDAPString
        //
        // We store the type in the current attribute
        super.transitions[LdapStatesEnum.ATTRIBUTE_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_STATE, LdapStatesEnum.TYPE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store attribute type" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    AddRequest addRequest = ldapMessageContainer.getAddRequest();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Store the type. It can't be null.
                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04086 );
                        LOG.error( msg );

                        AddResponseImpl response = new AddResponseImpl( addRequest.getMessageId() );

                        throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX,
                            addRequest.getEntry().getDn(), null );
                    }

                    String type = getType(tlv.getValue().getData());

                    try
                    {
                        addRequest.addAttributeType( type );
                    }
                    catch ( LdapException ne )
                    {
                        String msg = I18n.err( I18n.ERR_04087 );
                        LOG.error( msg );

                        AddResponseImpl response = new AddResponseImpl( addRequest.getMessageId() );
                        throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX,
                            addRequest.getEntry().getDn(), ne );
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Adding type {}", type );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from type to vals
        // --------------------------------------------------------------------------------------------
        // AttributeList ::= SEQUENCE OF SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // Nothing to do here.
        super.transitions[LdapStatesEnum.TYPE_STATE.ordinal()][UniversalTag.SET.getValue()] = new GrammarTransition(
            LdapStatesEnum.TYPE_STATE, LdapStatesEnum.VALUES_STATE, UniversalTag.SET.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from vals to Value
        // --------------------------------------------------------------------------------------------
        // AttributeList ::= SEQUENCE OF SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // AttributeValue OCTET STRING
        //
        // Store the value into the current attribute
        super.transitions[LdapStatesEnum.VALUES_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.VALUES_STATE, LdapStatesEnum.VALUE_STATE, UniversalTag.OCTET_STRING.getValue(), new ValueAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Value to Value
        // --------------------------------------------------------------------------------------------
        // AttributeList ::= SEQUENCE OF SEQUENCE {
        //     ...
        //     vals SET OF AttributeValue }
        //
        // AttributeValue OCTET STRING
        //
        // Store the value into the current attribute
        super.transitions[LdapStatesEnum.VALUE_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.VALUE_STATE, LdapStatesEnum.VALUE_STATE, UniversalTag.OCTET_STRING.getValue(), new ValueAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Value to Attribute
        // --------------------------------------------------------------------------------------------
        // AttributeList ::= SEQUENCE OF SEQUENCE {
        //
        // Nothing to do here.
        super.transitions[LdapStatesEnum.VALUE_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.VALUE_STATE, LdapStatesEnum.ATTRIBUTE_STATE, UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from Value to Controls
        // --------------------------------------------------------------------------------------------
        // AttributeList ::= SEQUENCE OF SEQUENCE {
        //
        // Initialize the controls
        super.transitions[LdapStatesEnum.VALUE_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.VALUE_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // AddResponse Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... AddResponse ...
        // AddResponse ::= [APPLICATION 9] LDAPResult
        // 
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.ADD_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.ADD_RESPONSE_STATE, LdapConstants.ADD_RESPONSE_TAG,
            new GrammarAction( "Init AddResponse" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the AddResponse Object
                    AddResponse addResponse = new AddResponseImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( addResponse );

                    // We will check that the request is not null
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    int expectedLength = tlv.getLength();

                    if ( expectedLength == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04088 );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }

                    LOG.debug( "Add Response" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // AddResponse Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... AddResponse ...
        // AddResponse ::= [APPLICATION 9] LDAPResult
        //
        // LDAPResult ::= SEQUENCE {
        //     resultCode    ENUMERATED {
        //         ...
        // 
        // Stores the result code
        super.transitions[LdapStatesEnum.ADD_RESPONSE_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.ADD_RESPONSE_STATE, LdapStatesEnum.RESULT_CODE_STATE, UniversalTag.ENUMERATED.getValue(),
            new ResultCodeAction() );

        // --------------------------------------------------------------------------------------------
        // DelResponse Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... DelResponse ...
        // DelResponse ::= [APPLICATION 11] LDAPResult
        // We have to switch to the DelResponse grammar
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.DEL_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.DEL_RESPONSE_STATE, LdapConstants.DEL_RESPONSE_TAG,
            new GrammarAction( "Init DelResponse" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the DelResponse Object
                    DeleteResponse delResponse = new DeleteResponseImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( delResponse );

                    LOG.debug( "Del response " );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // DelResponse Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... DelResponse ...
        // DelResponse ::= [APPLICATION 11] LDAPResult
        //
        // LDAPResult ::= SEQUENCE {
        //     resultCode    ENUMERATED {
        //         ...
        // 
        // Stores the result code
        super.transitions[LdapStatesEnum.DEL_RESPONSE_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.DEL_RESPONSE_STATE, LdapStatesEnum.RESULT_CODE_STATE, UniversalTag.ENUMERATED.getValue(),
            new ResultCodeAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from MessageID to ModifydDNRequest Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ModifyDNRequest ...
        // ModifyDNRequest ::= [APPLICATION 12] SEQUENCE { ...
        //
        // Create the ModifyDNRequest Object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.MODIFY_DN_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.MODIFY_DN_REQUEST_STATE,
            LdapConstants.MODIFY_DN_REQUEST_TAG, new GrammarAction( "Init Modify Dn Request" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the ModifyDNRequest Object
                    ModifyDnRequest modifyDnRequest = new ModifyDnRequestImpl( ldapMessageContainer
                        .getMessageId() );
                    ldapMessageContainer.setMessage( modifyDnRequest );

                    LOG.debug( "ModifyDn request" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from ModifydDNRequest Message to EntryModDN
        // --------------------------------------------------------------------------------------------
        // ModifyDNRequest ::= [APPLICATION 12] SEQUENCE { ...
        //     entry LDAPDN,
        //     ...
        //
        // Stores the entry Dn
        super.transitions[LdapStatesEnum.MODIFY_DN_REQUEST_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.MODIFY_DN_REQUEST_STATE, LdapStatesEnum.ENTRY_MOD_DN_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store entry" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    ModifyDnRequest modifyDNRequest = ldapMessageContainer.getModifyDnRequest();

                    // Get the Value and store it in the modifyDNRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // Dn
                    Dn entry = null;

                    if ( tlv.getLength() == 0 )
                    {
                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( I18n.err( I18n.ERR_04089 ) );
                    }
                    else
                    {
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            entry = new Dn( dnStr );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            String msg = "Invalid Dn given : " + dnStr + " (" + Strings.dumpBytes(dnBytes)
                                + ") is invalid";
                            LOG.error( "{} : {}", msg, ine.getMessage() );

                            ModifyDnResponseImpl response = new ModifyDnResponseImpl( modifyDNRequest.getMessageId() );
                            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                                Dn.EMPTY_DN, ine );
                        }

                        modifyDNRequest.setName( entry );
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Modifying Dn {}", entry );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from EntryModDN to NewRDN
        // --------------------------------------------------------------------------------------------
        // ModifyDNRequest ::= [APPLICATION 12] SEQUENCE { ...
        //     ...
        //     newrdn  RelativeRDN,
        //     ...
        //
        // RelativeRDN :: LDAPString
        //
        // Stores the new Rdn
        super.transitions[LdapStatesEnum.ENTRY_MOD_DN_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.ENTRY_MOD_DN_STATE, LdapStatesEnum.NEW_RDN_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store new Rdn" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    ModifyDnRequest modifyDnRequest = ldapMessageContainer.getModifyDnRequest();

                    // Get the Value and store it in the modifyDNRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // newDN
                    Rdn newRdn = null;

                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04090 );
                        LOG.error( msg );

                        ModifyDnResponseImpl response = new ModifyDnResponseImpl( modifyDnRequest.getMessageId() );
                        throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                            modifyDnRequest.getName(), null );
                    }
                    else
                    {
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            Dn dn = new Dn( dnStr );
                            newRdn = dn.getRdn( 0 );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            String msg = "Invalid new Rdn given : " + dnStr + " (" + Strings.dumpBytes(dnBytes)
                                + ") is invalid";
                            LOG.error( "{} : {}", msg, ine.getMessage() );

                            ModifyDnResponseImpl response = new ModifyDnResponseImpl( modifyDnRequest.getMessageId() );
                            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                                modifyDnRequest.getName(), ine );
                        }

                        modifyDnRequest.setNewRdn( newRdn );
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Modifying with new Rdn {}", newRdn );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from NewRDN to DeleteOldRDN
        // --------------------------------------------------------------------------------------------
        // ModifyDNRequest ::= [APPLICATION 12] SEQUENCE { ...
        //     ...
        //     deleteoldrdn BOOLEAN,
        //     ...
        //
        // Stores the deleteOldRDN flag
        super.transitions[LdapStatesEnum.NEW_RDN_STATE.ordinal()][UniversalTag.BOOLEAN.getValue()] = new GrammarTransition(
            LdapStatesEnum.NEW_RDN_STATE, LdapStatesEnum.DELETE_OLD_RDN_STATE, UniversalTag.BOOLEAN.getValue(),
            new GrammarAction( "Store matching dnAttributes Value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    ModifyDnRequest modifyDnRequest = ldapMessageContainer.getModifyDnRequest();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We get the value. If it's a 0, it's a FALSE. If it's
                    // a FF, it's a TRUE. Any other value should be an error,
                    // but we could relax this constraint. So if we have
                    // something
                    // which is not 0, it will be interpreted as TRUE, but we
                    // will generate a warning.
                    Value value = tlv.getValue();

                    try
                    {
                        modifyDnRequest.setDeleteOldRdn( BooleanDecoder.parse( value ) );
                    }
                    catch ( BooleanDecoderException bde )
                    {
                        LOG.error( I18n
                            .err( I18n.ERR_04091, Strings.dumpBytes(value.getData()), bde.getMessage() ) );

                        // This will generate a PROTOCOL_ERROR                        
                        throw new DecoderException( bde.getMessage() );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        if ( modifyDnRequest.getDeleteOldRdn() )
                        {
                            LOG.debug( " Old Rdn attributes will be deleted" );
                        }
                        else
                        {
                            LOG.debug( " Old Rdn attributes will be retained" );
                        }
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from DeleteOldRDN to NewSuperior
        // --------------------------------------------------------------------------------------------
        // ModifyDNRequest ::= [APPLICATION 12] SEQUENCE { ...
        //     ...
        //     newSuperior [0] LDAPDN OPTIONAL }
        //
        // Stores the new superior
        super.transitions[LdapStatesEnum.DELETE_OLD_RDN_STATE.ordinal()][LdapConstants.MODIFY_DN_REQUEST_NEW_SUPERIOR_TAG] = new GrammarTransition(
            LdapStatesEnum.DELETE_OLD_RDN_STATE, LdapStatesEnum.NEW_SUPERIOR_STATE,
            LdapConstants.MODIFY_DN_REQUEST_NEW_SUPERIOR_TAG, new GrammarAction( "Store new superior" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    ModifyDnRequest modifyDnRequest = ldapMessageContainer.getModifyDnRequest();

                    // Get the Value and store it in the modifyDNRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // Dn
                    Dn newSuperior = Dn.EMPTY_DN;

                    if ( tlv.getLength() == 0 )
                    {

                        if ( modifyDnRequest.getDeleteOldRdn() )
                        {
                            // This will generate a PROTOCOL_ERROR
                            throw new DecoderException( I18n.err( I18n.ERR_04092 ) );
                        }
                        else
                        {
                            LOG.warn( "The new superior is null, so we will change the entry" );
                        }

                        modifyDnRequest.setNewSuperior( newSuperior );
                    }
                    else
                    {
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            newSuperior = new Dn( dnStr );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            String msg = "Invalid new superior Dn given : " + dnStr + " ("
                                + Strings.dumpBytes(dnBytes) + ") is invalid";
                            LOG.error( "{} : {}", msg, ine.getMessage() );

                            ModifyDnResponseImpl response = new ModifyDnResponseImpl( modifyDnRequest.getMessageId() );
                            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                                modifyDnRequest.getName(), ine );
                        }

                        modifyDnRequest.setNewSuperior( newSuperior );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "New superior Dn {}", newSuperior );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from DeleteOldRDN to Controls
        // --------------------------------------------------------------------------------------------
        //     modifyDNRequest ModifyDNRequest,
        //     ... },
        // controls   [0] Controls OPTIONAL }
        //
        // Stores the new superior
        super.transitions[LdapStatesEnum.DELETE_OLD_RDN_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.DELETE_OLD_RDN_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from DeleteOldRDN to Controls
        // --------------------------------------------------------------------------------------------
        //     modifyDNRequest ModifyDNRequest,
        //     ... },
        // controls   [0] Controls OPTIONAL }
        //
        // Stores the new superior
        super.transitions[LdapStatesEnum.NEW_SUPERIOR_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.NEW_SUPERIOR_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from MessageID to ModifyDNResponse Message.
        // --------------------------------------------------------------------------------------------
        // ModifyDNResponse ::= [APPLICATION 13] SEQUENCE {
        //     ...
        //
        // Creates the ModifyDNResponse
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.MODIFY_DN_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.MODIFY_DN_RESPONSE_STATE,
            LdapConstants.MODIFY_DN_RESPONSE_TAG, new GrammarAction( "Init ModifyDNResponse" )
            {
                public void action( Asn1Container container )
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the ModifyDnResponse Object
                    ModifyDnResponse modifyDnResponse = new ModifyDnResponseImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( modifyDnResponse );

                    LOG.debug( "Modify Dn response " );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from ModifyDNResponse Message to Result Code
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ModifyDNResponse ...
        // ModifyDNResponse ::= [APPLICATION 13] LDAPResult
        //
        // LDAPResult ::= SEQUENCE {
        //     resultCode    ENUMERATED {
        //         ...
        // 
        // Stores the result co        //     modifyDNRequest ModifyDNRequest,
        //     ... },
        // controls   [0] Controls OPTIONAL }
        super.transitions[LdapStatesEnum.MODIFY_DN_RESPONSE_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.MODIFY_DN_RESPONSE_STATE, LdapStatesEnum.RESULT_CODE_STATE, UniversalTag.ENUMERATED.getValue(),
            new ResultCodeAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Message ID to CompareResquest
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... CompareRequest ...
        // 
        // CompareRequest ::= [APPLICATION 14] SEQUENCE {
        // ...
        //
        // Initialize the Compare Request object 
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.COMPARE_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.COMPARE_REQUEST_STATE, LdapConstants.COMPARE_REQUEST_TAG,
            new GrammarAction( "Init Compare Request" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the CompareRequest Object
                    CompareRequest compareRequest = new CompareRequestImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( compareRequest );

                    LOG.debug( "Compare Request" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from CompareResquest to entryComp
        // --------------------------------------------------------------------------------------------
        // CompareRequest ::= [APPLICATION 14] SEQUENCE {
        //     entry    LDAPDN,
        //     ...
        //
        // Stores the compared Dn
        super.transitions[LdapStatesEnum.COMPARE_REQUEST_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.COMPARE_REQUEST_STATE, LdapStatesEnum.ENTRY_COMP_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store entry" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    CompareRequest compareRequest = ldapMessageContainer.getCompareRequest();

                    // Get the Value and store it in the CompareRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();
                    Dn entry = null;

                    // We have to handle the special case of a 0 length matched
                    // Dn
                    if ( tlv.getLength() == 0 )
                    {
                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( I18n.err( I18n.ERR_04089 ) );
                    }
                    else
                    {
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            entry = new Dn( dnStr );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            String msg = "Invalid Dn given : " + dnStr + " (" + Strings.dumpBytes(dnBytes)
                                + ") is invalid";
                            LOG.error( "{} : {}", msg, ine.getMessage() );

                            CompareResponseImpl response = new CompareResponseImpl( compareRequest.getMessageId() );
                            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                                Dn.EMPTY_DN, ine );
                        }

                        compareRequest.setName( entry );
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Comparing Dn {}", entry );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from entryComp to ava
        // --------------------------------------------------------------------------------------------
        // CompareRequest ::= [APPLICATION 14] SEQUENCE {
        //     ...
        //     ava AttributeValueAssertion }
        //
        // AttributeValueAssertion ::= SEQUENCE {
        // 
        // Nothing to do
        super.transitions[LdapStatesEnum.ENTRY_COMP_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.ENTRY_COMP_STATE, LdapStatesEnum.AVA_STATE, UniversalTag.SEQUENCE.getValue(), null );

        // --------------------------------------------------------------------------------------------
        // Transition from ava to AttributeDesc
        // --------------------------------------------------------------------------------------------
        // AttributeValueAssertion ::= SEQUENCE {
        //     attributeDesc AttributeDescription,
        //     ...
        //
        // AttributeDescription LDAPString
        // 
        // Stores the attribute description
        super.transitions[LdapStatesEnum.AVA_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.AVA_STATE, LdapStatesEnum.ATTRIBUTE_DESC_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store attribute desc" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Get the CompareRequest Object
                    CompareRequest compareRequest = ldapMessageContainer.getCompareRequest();

                    // Get the Value and store it in the CompareRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // Dn
                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04093 );
                        LOG.error( msg );
                        CompareResponseImpl response = new CompareResponseImpl( compareRequest.getMessageId() );

                        throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_ATTRIBUTE_SYNTAX,
                            compareRequest.getName(), null );
                    }

                    String type = getType(tlv.getValue().getData());
                    compareRequest.setAttributeId( type );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Comparing attribute description {}", compareRequest.getAttributeId() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from AttributeDesc to Assertion Value
        // --------------------------------------------------------------------------------------------
        // AttributeValueAssertion ::= SEQUENCE {
        //     ...
        //     assertionValue AssertionValue }
        //
        // AssertionValue OCTET STRING
        // 
        // Stores the attribute value
        super.transitions[LdapStatesEnum.ATTRIBUTE_DESC_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_DESC_STATE, LdapStatesEnum.ASSERTION_VALUE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store assertion value" )
            {
                public void action( Asn1Container container )
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Get the CompareRequest Object
                    CompareRequest compareRequest = ldapMessageContainer.getCompareRequest();

                    // Get the Value and store it in the CompareRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length value
                    if ( tlv.getLength() == 0 )
                    {
                        compareRequest.setAssertionValue( "" );
                    }
                    else
                    {
                        if ( ldapMessageContainer.isBinary( compareRequest.getAttributeId() ) )
                        {
                            compareRequest.setAssertionValue( tlv.getValue().getData() );

                            if ( IS_DEBUG )
                            {
                                LOG.debug( "Comparing attribute value {}", Strings.dumpBytes(compareRequest
                                        .getAssertionValue().getBytes()) );
                            }
                        }
                        else
                        {
                            compareRequest.setAssertionValue( Strings.utf8ToString(tlv.getValue().getData()) );

                            if ( LOG.isDebugEnabled() )
                            {
                                LOG.debug( "Comparing attribute value {}", compareRequest.getAssertionValue() );
                            }
                        }
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value to Controls
        // --------------------------------------------------------------------------------------------
        // AttributeValueAssertion ::= SEQUENCE {
        //     ...
        //     assertionValue AssertionValue }
        //
        // AssertionValue OCTET STRING
        // 
        // Stores the attribute value
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // CompareResponse Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... CompareResponse ...
        // CompareResponse ::= [APPLICATION 15] LDAPResult
        // We have to switch to the CompareResponse grammar
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.COMPARE_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.COMPARE_RESPONSE_STATE, LdapConstants.COMPARE_RESPONSE_TAG,
            new GrammarAction( "Init CompareResponse" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the CompareResponse Object
                    CompareResponse compareResponse = new CompareResponseImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( compareResponse );

                    // We will check that the request is not null
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04094 );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }

                    LOG.debug( "Compare response " );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // CompareResponse Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... CompareResponse ...
        // CompareResponse ::= [APPLICATION 15] LDAPResult
        //
        // LDAPResult ::= SEQUENCE {
        //     resultCode    ENUMERATED {
        //         ...
        // 
        // Stores the result code
        super.transitions[LdapStatesEnum.COMPARE_RESPONSE_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.COMPARE_RESPONSE_STATE, LdapStatesEnum.RESULT_CODE_STATE, UniversalTag.ENUMERATED.getValue(),
            new ResultCodeAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from MessageID to SearchResultReference Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... SearchResultReference ...
        // SearchResultReference ::= [APPLICATION 19] SEQUENCE OF LDAPURL
        //
        // Initialization of SearchResultReference object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.SEARCH_RESULT_REFERENCE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.SEARCH_RESULT_REFERENCE_STATE,
            LdapConstants.SEARCH_RESULT_REFERENCE_TAG, new GrammarAction( "Init SearchResultReference" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the SearchResultReference Object
                    SearchResultReference searchResultReference = new SearchResultReferenceImpl( ldapMessageContainer
                        .getMessageId() );
                    ldapMessageContainer.setMessage( searchResultReference );

                    LOG.debug( "SearchResultReference response " );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from SearchResultReference Message to Reference
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... SearchResultReference ...
        // SearchResultReference ::= [APPLICATION 19] SEQUENCE OF LDAPURL
        //
        // Initialization of SearchResultReference object
        super.transitions[LdapStatesEnum.SEARCH_RESULT_REFERENCE_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.SEARCH_RESULT_REFERENCE_STATE, LdapStatesEnum.REFERENCE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new StoreReferenceAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Reference to Reference
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... SearchResultReference ...
        // SearchResultReference ::= [APPLICATION 19] SEQUENCE OF LDAPURL
        //
        // Initialization of SearchResultReference object
        super.transitions[LdapStatesEnum.REFERENCE_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.REFERENCE_STATE, LdapStatesEnum.REFERENCE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new StoreReferenceAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Reference to Controls
        // --------------------------------------------------------------------------------------------
        //     searchResultReference SearchResultReference,
        //     ... },
        // controls   [0] Controls OPTIONAL }
        //
        // Initialization the controls
        super.transitions[LdapStatesEnum.REFERENCE_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.REFERENCE_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Message Id to ExtendedRequest Message
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedRequest ...
        // ExtendedRequest ::= [APPLICATION 23] SEQUENCE {
        //
        // Creates the ExtendedRequest object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.EXTENDED_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.EXTENDED_REQUEST_STATE, LdapConstants.EXTENDED_REQUEST_TAG,
            new GrammarAction( "Init Extended Request" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the ExtendedRequest Object
                    ExtendedRequest extendedRequest = new ExtendedRequestImpl( ldapMessageContainer
                        .getMessageId() );
                    ldapMessageContainer.setMessage( extendedRequest );

                    LOG.debug( "Extended request" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from ExtendedRequest Message to RequestName
        // --------------------------------------------------------------------------------------------
        // ExtendedRequest ::= [APPLICATION 23] SEQUENCE {
        //     requestName [0] LDAPOID,
        //     ...
        //
        // Stores the name
        super.transitions[LdapStatesEnum.EXTENDED_REQUEST_STATE.ordinal()][LdapConstants.EXTENDED_REQUEST_NAME_TAG] = new GrammarTransition(
            LdapStatesEnum.EXTENDED_REQUEST_STATE, LdapStatesEnum.REQUEST_NAME_STATE,
            LdapConstants.EXTENDED_REQUEST_NAME_TAG, new GrammarAction( "Store name" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // We can allocate the ExtendedRequest Object
                    ExtendedRequest extendedRequest = ldapMessageContainer.getExtendedRequest();

                    // Get the Value and store it in the ExtendedRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // OID
                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04095 );
                        LOG.error( msg );
                        // This will generate a PROTOCOL_ERROR                        
                        throw new DecoderException( msg );
                    }
                    else
                    {
                        byte[] requestNameBytes = tlv.getValue().getData();

                        try
                        {
                            String requestName = Strings.utf8ToString(requestNameBytes);

                            if ( !OID.isOID( requestName ) )
                            {

                                String msg = "The Request name is not a valid OID : "
                                    + Strings.utf8ToString(requestNameBytes) + " ("
                                    + Strings.dumpBytes(requestNameBytes) + ") is invalid";
                                LOG.error( msg );

                                // throw an exception, we will get a PROTOCOL_ERROR
                                throw new DecoderException( msg );
                            }

                            extendedRequest.setRequestName( requestName );
                        }
                        catch ( DecoderException de )
                        {
                            String msg = "The Request name is not a valid OID : "
                                + Strings.utf8ToString(requestNameBytes) + " ("
                                + Strings.dumpBytes(requestNameBytes) + ") is invalid";
                            LOG.error( "{} : {}", msg, de.getMessage() );

                            // Rethrow the exception, we will get a PROTOCOL_ERROR
                            throw de;
                        }
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "OID read : {}", extendedRequest.getRequestName() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from RequestName to RequestValue
        // --------------------------------------------------------------------------------------------
        // ExtendedRequest ::= [APPLICATION 23] SEQUENCE {
        //     ...
        //     requestValue  [1] OCTET STRING OPTIONAL }
        //
        // Stores the value
        super.transitions[LdapStatesEnum.REQUEST_NAME_STATE.ordinal()][LdapConstants.EXTENDED_REQUEST_VALUE_TAG] = new GrammarTransition(
            LdapStatesEnum.REQUEST_NAME_STATE, LdapStatesEnum.REQUEST_VALUE_STATE,
            LdapConstants.EXTENDED_REQUEST_VALUE_TAG, new GrammarAction( "Store value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // We can allocate the ExtendedRequest Object
                    ExtendedRequest extendedRequest = ldapMessageContainer.getExtendedRequest();

                    // Get the Value and store it in the ExtendedRequest
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // value
                    if ( tlv.getLength() == 0 )
                    {
                        extendedRequest.setRequestValue( StringConstants.EMPTY_BYTES );
                    }
                    else
                    {
                        extendedRequest.setRequestValue( tlv.getValue().getData() );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Extended value : {}", extendedRequest.getRequestValue() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from RequestName to Controls
        // --------------------------------------------------------------------------------------------
        //         extendedRequest   EtendedRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Stores the value
        super.transitions[LdapStatesEnum.REQUEST_NAME_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.REQUEST_NAME_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from RequestValue to Controls
        // --------------------------------------------------------------------------------------------
        //         extendedRequest   EtendedRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Stores the value
        super.transitions[LdapStatesEnum.REQUEST_VALUE_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.REQUEST_VALUE_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from MessageId to ExtendedResponse Message.
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedResponse ...
        // ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
        //
        // Creates the ExtendeResponse object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.EXTENDED_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.EXTENDED_RESPONSE_STATE,
            LdapConstants.EXTENDED_RESPONSE_TAG, new GrammarAction( "Init Extended Reponse" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the ExtendedResponse Object
                    ExtendedResponse extendedResponse = new ExtendedResponseImpl( ldapMessageContainer.getMessageId() );
                    ldapMessageContainer.setMessage( extendedResponse );

                    LOG.debug( "Extended Response" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from ExtendedResponse Message to Result Code ER
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedResponse ...
        // ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
        //     COMPONENTS OF LDAPResult,
        //     ...
        //
        // Stores the result code
        super.transitions[LdapStatesEnum.EXTENDED_RESPONSE_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.EXTENDED_RESPONSE_STATE, LdapStatesEnum.RESULT_CODE_ER_STATE, UniversalTag.ENUMERATED.getValue(),
            new ResultCodeAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Result Code ER to Matched Dn ER
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedResponse ...
        // ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
        //     COMPONENTS OF LDAPResult,
        //     ...
        //
        // 
        super.transitions[LdapStatesEnum.RESULT_CODE_ER_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.RESULT_CODE_ER_STATE, LdapStatesEnum.MATCHED_DN_ER_STATE, UniversalTag.OCTET_STRING.getValue(),
            new MatchedDNAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Matched Dn ER to Error Message ER
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedResponse ...
        // ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
        //     COMPONENTS OF LDAPResult,
        //     ...
        //
        // 
        super.transitions[LdapStatesEnum.MATCHED_DN_ER_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.MATCHED_DN_ER_STATE, LdapStatesEnum.ERROR_MESSAGE_ER_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ErrorMessageAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Error Message ER to Referrals ER 
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedResponse ...
        // ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
        //     COMPONENTS OF LDAPResult,
        //     ...
        //
        // 
        super.transitions[LdapStatesEnum.ERROR_MESSAGE_ER_STATE.ordinal()][LdapConstants.LDAP_RESULT_REFERRAL_SEQUENCE_TAG] = new GrammarTransition(
            LdapStatesEnum.ERROR_MESSAGE_ER_STATE, LdapStatesEnum.REFERRALS_ER_STATE,
            LdapConstants.LDAP_RESULT_REFERRAL_SEQUENCE_TAG, new InitReferralsAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referrals ER to Referral ER 
        // --------------------------------------------------------------------------------------------
        // Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI (RFC 4511)
        // URI ::= LDAPString
        //
        // Add a first Referral
        super.transitions[LdapStatesEnum.REFERRALS_ER_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.REFERRALS_ER_STATE, LdapStatesEnum.REFERRAL_ER_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ReferralAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referral ER to Referral ER 
        // --------------------------------------------------------------------------------------------
        // Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI (RFC 4511)
        // URI ::= LDAPString
        //
        // Adda new Referral
        super.transitions[LdapStatesEnum.REFERRAL_ER_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.REFERRAL_ER_STATE, LdapStatesEnum.REFERRAL_ER_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ReferralAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referral ER to ResponseName 
        // --------------------------------------------------------------------------------------------
        // Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI (RFC 4511)
        // URI ::= LDAPString
        //
        // Adda new Referral
        super.transitions[LdapStatesEnum.REFERRAL_ER_STATE.ordinal()][LdapConstants.EXTENDED_RESPONSE_RESPONSE_NAME_TAG] = new GrammarTransition(
            LdapStatesEnum.REFERRAL_ER_STATE, LdapStatesEnum.RESPONSE_NAME_STATE,
            LdapConstants.EXTENDED_RESPONSE_RESPONSE_NAME_TAG, new ResponseNameAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referral ER to Response 
        // --------------------------------------------------------------------------------------------
        // Referral ::= SEQUENCE SIZE (1..MAX) OF uri URI (RFC 4511)
        // URI ::= LDAPString
        //
        // Adda new Referral
        super.transitions[LdapStatesEnum.REFERRAL_ER_STATE.ordinal()][LdapConstants.EXTENDED_RESPONSE_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.REFERRAL_ER_STATE, LdapStatesEnum.RESPONSE_STATE,
            LdapConstants.EXTENDED_RESPONSE_RESPONSE_TAG, new ResponseAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Referral ER to Controls 
        // --------------------------------------------------------------------------------------------
        //         extendedResponse   ExtendedResponse,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Adda new Referral
        super.transitions[LdapStatesEnum.REFERRAL_ER_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.REFERRAL_ER_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Error Message ER to Controls 
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedResponse ...
        // ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
        //     COMPONENTS OF LDAPResult,
        //     ...
        //
        // 
        super.transitions[LdapStatesEnum.ERROR_MESSAGE_ER_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ERROR_MESSAGE_ER_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Error Message ER to ResponseName 
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedResponse ...
        // ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
        //     COMPONENTS OF LDAPResult,
        //     responseName   [10] LDAPOID OPTIONAL,
        //     ...
        //
        // Stores the response name
        super.transitions[LdapStatesEnum.ERROR_MESSAGE_ER_STATE.ordinal()][LdapConstants.EXTENDED_RESPONSE_RESPONSE_NAME_TAG] = new GrammarTransition(
            LdapStatesEnum.ERROR_MESSAGE_ER_STATE, LdapStatesEnum.RESPONSE_NAME_STATE,
            LdapConstants.EXTENDED_RESPONSE_RESPONSE_NAME_TAG, new ResponseNameAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Response Name to Response 
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedResponse ...
        // ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
        //     ...
        //     responseName   [10] LDAPOID OPTIONAL,
        //     response       [11] OCTET STRING OPTIONAL}
        //
        // Stores the response
        super.transitions[LdapStatesEnum.RESPONSE_NAME_STATE.ordinal()][LdapConstants.EXTENDED_RESPONSE_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.RESPONSE_NAME_STATE, LdapStatesEnum.RESPONSE_STATE,
            LdapConstants.EXTENDED_RESPONSE_RESPONSE_TAG, new ResponseAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from ResponseName to Controls 
        // --------------------------------------------------------------------------------------------
        //         extendedRequest   EtendedRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Init the controls
        super.transitions[LdapStatesEnum.RESPONSE_NAME_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.RESPONSE_NAME_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Error Message ER to Response 
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... ExtendedResponse ...
        // ExtendedResponse ::= [APPLICATION 24] SEQUENCE {
        //     COMPONENTS OF LDAPResult,
        //     ...
        //     response       [11] OCTET STRING OPTIONAL}
        //
        // Stores the response
        super.transitions[LdapStatesEnum.ERROR_MESSAGE_ER_STATE.ordinal()][LdapConstants.EXTENDED_RESPONSE_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.ERROR_MESSAGE_ER_STATE, LdapStatesEnum.RESPONSE_STATE,
            LdapConstants.EXTENDED_RESPONSE_RESPONSE_TAG, new ResponseAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Response to Controls 
        // --------------------------------------------------------------------------------------------
        //         extendedRequest   EtendedRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Init the controls
        super.transitions[LdapStatesEnum.RESPONSE_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.RESPONSE_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Message Id to IntermediateResponse Message
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... IntermediateResponse ...
        // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
        //
        // Creates the IntermediateResponse object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.INTERMEDIATE_RESPONSE_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.INTERMEDIATE_RESPONSE_STATE,
            LdapConstants.INTERMEDIATE_RESPONSE_TAG, new GrammarAction( "Init Intermediate Response" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the IntermediateResponse Object
                    IntermediateResponse intermediateResponse = new IntermediateResponseImpl( ldapMessageContainer
                        .getMessageId() );
                    ldapMessageContainer.setMessage( intermediateResponse );

                    LOG.debug( "Intermediate Response" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from IntermediateResponse Message to ResponseName
        // --------------------------------------------------------------------------------------------
        // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
        //     responseName [0] LDAPOID OPTIONAL,
        //     ...
        //
        // Stores the name
        super.transitions[LdapStatesEnum.INTERMEDIATE_RESPONSE_STATE.ordinal()][LdapConstants.INTERMEDIATE_RESPONSE_NAME_TAG] = new GrammarTransition(
            LdapStatesEnum.INTERMEDIATE_RESPONSE_STATE, LdapStatesEnum.INTERMEDIATE_RESPONSE_NAME_STATE,
            LdapConstants.INTERMEDIATE_RESPONSE_NAME_TAG, new GrammarAction( "Store response name" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {

                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // We can get the IntermediateResponse Object
                    IntermediateResponse intermediateResponse = ldapMessageContainer.getIntermediateResponse();

                    // Get the Value and store it in the IntermediateResponse
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // OID.
                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04095 );
                        LOG.error( msg );
                        // This will generate a PROTOCOL_ERROR                        
                        throw new DecoderException( msg );
                    }
                    else
                    {
                        byte[] responseNameBytes = tlv.getValue().getData();

                        String oidStr = Strings.utf8ToString(responseNameBytes);

                        if ( OID.isOID( oidStr ) )
                        {
                            OID.isOID( oidStr );
                            intermediateResponse.setResponseName( oidStr );
                        }
                        else
                        {
                            String msg = "The Intermediate Response name is not a valid OID : "
                                + Strings.utf8ToString(responseNameBytes) + " ("
                                + Strings.dumpBytes(responseNameBytes) + ") is invalid";
                            LOG.error( "{} : {}", msg, oidStr );

                            // Rethrow the exception, we will get a PROTOCOL_ERROR
                            throw new DecoderException( msg );
                        }
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "OID read : {}", intermediateResponse.getResponseName() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from IntermediateResponse Message to ResponseValue (ResponseName is null)
        // --------------------------------------------------------------------------------------------
        // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
        //     ...
        //     responseValue [1] OCTET STRING OPTIONAL
        //     }
        //
        // Stores the value
        super.transitions[LdapStatesEnum.INTERMEDIATE_RESPONSE_STATE.ordinal()][LdapConstants.INTERMEDIATE_RESPONSE_VALUE_TAG] = new GrammarTransition(
            LdapStatesEnum.INTERMEDIATE_RESPONSE_STATE, LdapStatesEnum.INTERMEDIATE_RESPONSE_VALUE_STATE,
            LdapConstants.INTERMEDIATE_RESPONSE_VALUE_TAG, new GrammarAction( "Store response value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // We can get the IntermediateResponse Object
                    IntermediateResponse intermediateResponse = ldapMessageContainer.getIntermediateResponse();

                    // Get the Value and store it in the IntermediateResponse
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // value
                    if ( tlv.getLength() == 0 )
                    {
                        intermediateResponse.setResponseValue( StringConstants.EMPTY_BYTES );
                    }
                    else
                    {
                        intermediateResponse.setResponseValue( tlv.getValue().getData() );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Value read : {}", Strings.dumpBytes(intermediateResponse.getResponseValue()) );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from ResponseName to ResponseValue
        // --------------------------------------------------------------------------------------------
        // IntermediateResponse ::= [APPLICATION 25] SEQUENCE {
        //     ...
        //     responseValue  [1] OCTET STRING OPTIONAL }
        //
        // Stores the value
        super.transitions[LdapStatesEnum.INTERMEDIATE_RESPONSE_NAME_STATE.ordinal()][LdapConstants.INTERMEDIATE_RESPONSE_VALUE_TAG] = new GrammarTransition(
            LdapStatesEnum.INTERMEDIATE_RESPONSE_NAME_STATE, LdapStatesEnum.INTERMEDIATE_RESPONSE_VALUE_STATE,
            LdapConstants.INTERMEDIATE_RESPONSE_VALUE_TAG, new GrammarAction( "Store value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // We can allocate the ExtendedRequest Object
                    IntermediateResponse intermediateResponse = ldapMessageContainer.getIntermediateResponse();

                    // Get the Value and store it in the IntermediateResponse
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to handle the special case of a 0 length matched
                    // value
                    if ( tlv.getLength() == 0 )
                    {
                        intermediateResponse.setResponseValue( StringConstants.EMPTY_BYTES );
                    }
                    else
                    {
                        intermediateResponse.setResponseValue( tlv.getValue().getData() );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Response value : {}", intermediateResponse.getResponseValue() );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from ResponseName to Controls
        // --------------------------------------------------------------------------------------------
        //         intermediateResponse   IntermediateResponse,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Stores the value
        super.transitions[LdapStatesEnum.INTERMEDIATE_RESPONSE_NAME_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.INTERMEDIATE_RESPONSE_NAME_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from ResponseValue to Controls
        // --------------------------------------------------------------------------------------------
        //         intermediateResponse   IntermediateResponse,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Stores the value
        super.transitions[LdapStatesEnum.INTERMEDIATE_RESPONSE_VALUE_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.INTERMEDIATE_RESPONSE_VALUE_STATE, LdapStatesEnum.CONTROLS_STATE,
            LdapConstants.CONTROLS_TAG, new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Controls
        // --------------------------------------------------------------------------------------------
        Action addControl = new GrammarAction( "Add Control" )
        {
            public void action( Asn1Container container ) throws DecoderException
            {
                LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                TLV tlv = ldapMessageContainer.getCurrentTLV();
                int expectedLength = tlv.getLength();

                // The Length should be null
                if ( expectedLength == 0 )
                {
                    String msg = I18n.err( I18n.ERR_04096 );
                    LOG.error( msg );

                    // This will generate a PROTOCOL_ERROR
                    throw new DecoderException( msg );
                }
            }
        };

        // ============================================================================================
        // Transition from Controls to Control
        // ============================================================================================
        // ...
        // Controls ::= SEQUENCE OF Control
        //  ...
        //
        // Initialize the controls 
        super.transitions[LdapStatesEnum.CONTROLS_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.CONTROLS_STATE, LdapStatesEnum.CONTROL_STATE, UniversalTag.SEQUENCE.getValue(), addControl );

        // ============================================================================================
        // Transition from Control to ControlType
        // ============================================================================================
        // Control ::= SEQUENCE {
        //     ...
        //
        // Create a new Control object, and store it in the message Container
        super.transitions[LdapStatesEnum.CONTROL_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.CONTROL_STATE, LdapStatesEnum.CONTROL_TYPE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Set Control Type" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Store the type
                    // We have to handle the special case of a 0 length OID
                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04097 );
                        LOG.error( msg );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( msg );
                    }

                    byte[] value = tlv.getValue().getData();
                    String oidValue = Strings.asciiBytesToString(value);

                    // The OID is encoded as a String, not an Object Id
                    if ( !OID.isOID(oidValue) )
                    {
                        LOG.error( I18n.err( I18n.ERR_04098, Strings.dumpBytes(value) ) );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( I18n.err( I18n.ERR_04099, oidValue ) );
                    }

                    Message message = ldapMessageContainer.getMessage();

                    Control control = ControlFactory.createControl( oidValue );

                    message.addControl( control );

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Control OID : " + oidValue );
                    }
                }
            } );

        // ============================================================================================
        // Transition from ControlType to Control Criticality
        // ============================================================================================
        // Control ::= SEQUENCE {
        //     ...
        //     criticality BOOLEAN DEFAULT FALSE,
        //     ...
        //
        // Store the value in the control object created before
        super.transitions[LdapStatesEnum.CONTROL_TYPE_STATE.ordinal()][UniversalTag.BOOLEAN.getValue()] = new GrammarTransition(
            LdapStatesEnum.CONTROL_TYPE_STATE, LdapStatesEnum.CRITICALITY_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Set Criticality" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Get the current control
                    Control control = null;

                    MessageDecorator message = ldapMessageContainer.getMessage();
                    control = message.getCurrentControl();

                    // Store the criticality
                    // We get the value. If it's a 0, it's a FALSE. If it's
                    // a FF, it's a TRUE. Any other value should be an error,
                    // but we could relax this constraint. So if we have
                    // something
                    // which is not 0, it will be interpreted as TRUE, but we
                    // will generate a warning.
                    Value value = tlv.getValue();

                    try
                    {
                        control.setCritical( BooleanDecoder.parse( value ) );
                    }
                    catch ( BooleanDecoderException bde )
                    {
                        LOG.error( I18n
                            .err( I18n.ERR_04100, Strings.dumpBytes(value.getData()), bde.getMessage() ) );

                        // This will generate a PROTOCOL_ERROR
                        throw new DecoderException( bde.getMessage() );
                    }

                    // We can have an END transition
                    ldapMessageContainer.setGrammarEndAllowed( true );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Control criticality : " + control.isCritical() );
                    }
                }
            } );

        // ============================================================================================
        // Transition from Control Criticality to Control Value
        // ============================================================================================
        // Control ::= SEQUENCE {
        //     ...
        //     controlValue OCTET STRING OPTIONAL }
        //
        // Store the value in the control object created before
        super.transitions[LdapStatesEnum.CRITICALITY_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.CRITICALITY_STATE, LdapStatesEnum.CONTROL_VALUE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ControlValueAction() );

        // ============================================================================================
        // Transition from Control Type to Control Value
        // ============================================================================================
        // Control ::= SEQUENCE {
        //     ...
        //     controlValue OCTET STRING OPTIONAL }
        //
        // Store the value in the control object created before
        super.transitions[LdapStatesEnum.CONTROL_TYPE_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.CONTROL_TYPE_STATE, LdapStatesEnum.CONTROL_VALUE_STATE, UniversalTag.OCTET_STRING.getValue(),
            new ControlValueAction() );

        // ============================================================================================
        // Transition from Control Type to Control
        // ============================================================================================
        // Control ::= SEQUENCE {
        //     ...
        //     controlValue OCTET STRING OPTIONAL }
        //
        // Store the value in the control object created before
        super.transitions[LdapStatesEnum.CONTROL_TYPE_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.CONTROL_TYPE_STATE, LdapStatesEnum.CONTROL_STATE, UniversalTag.SEQUENCE.getValue(), addControl );

        // ============================================================================================
        // Transition from Control Criticality to Control
        // ============================================================================================
        // Control ::= SEQUENCE {
        //     ...
        //     controlValue OCTET STRING OPTIONAL }
        //
        // Store the value in the control object created before
        super.transitions[LdapStatesEnum.CRITICALITY_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.CRITICALITY_STATE, LdapStatesEnum.CONTROL_STATE, UniversalTag.SEQUENCE.getValue(), addControl );

        // ============================================================================================
        // Transition from Control Value to Control
        // ============================================================================================
        // Control ::= SEQUENCE {
        //     ...
        //     controlValue OCTET STRING OPTIONAL }
        //
        // Store the value in the control object created before
        super.transitions[LdapStatesEnum.CONTROL_VALUE_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.CONTROL_VALUE_STATE, LdapStatesEnum.CONTROL_STATE, UniversalTag.SEQUENCE.getValue(), addControl );

        // --------------------------------------------------------------------------------------------
        // Transition from message ID to SearchRequest Message
        // --------------------------------------------------------------------------------------------
        // LdapMessage ::= ... SearchRequest ...
        // SearchRequest ::= [APPLICATION 3] SEQUENCE { ...
        //
        // Initialize the searchRequest object
        super.transitions[LdapStatesEnum.MESSAGE_ID_STATE.ordinal()][LdapConstants.SEARCH_REQUEST_TAG] = new GrammarTransition(
            LdapStatesEnum.MESSAGE_ID_STATE, LdapStatesEnum.SEARCH_REQUEST_STATE, LdapConstants.SEARCH_REQUEST_TAG,
            new GrammarAction( "Init SearchRequest" )
            {
                public void action( Asn1Container container )
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    // Now, we can allocate the SearchRequest Object
                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    SearchRequest searchRequest = new SearchRequestImpl( ldapMessageContainer.getMessageId() );
                    SearchRequestDecorator searchRequestDecorator = new SearchRequestDecorator( searchRequest );

                    searchRequestDecorator.setTlvId( tlv.getId());
                    ldapMessageContainer.setMessage( searchRequest );
                    ldapMessageContainer.setMessageDecorator( searchRequestDecorator );

                    LOG.debug( "Search Request" );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from SearchRequest Message to BaseObject
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     baseObject LDAPDN,
        //     ...
        //
        // We have a value for the base object, we will store it in the message
        super.transitions[LdapStatesEnum.SEARCH_REQUEST_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.SEARCH_REQUEST_STATE, LdapStatesEnum.BASE_OBJECT_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "store base object value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    SearchRequestDecorator searchRequestDecorator = ldapMessageContainer.getSearchRequestDecorator();
                    SearchRequest searchRequest = searchRequestDecorator.getSearchRequest();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to check that this is a correct Dn
                    Dn baseObject = null;

                    // We have to handle the special case of a 0 length base
                    // object,
                    // which means that the search is done from the default
                    // root.
                    if ( tlv.getLength() != 0 )
                    {
                        byte[] dnBytes = tlv.getValue().getData();
                        String dnStr = Strings.utf8ToString(dnBytes);

                        try
                        {
                            baseObject = new Dn( dnStr );
                        }
                        catch ( LdapInvalidDnException ine )
                        {
                            String msg = "Invalid root Dn given : " + dnStr + " (" + Strings.dumpBytes(dnBytes)
                                + ") is invalid";
                            LOG.error( "{} : {}", msg, ine.getMessage() );

                            SearchResultDoneImpl response = new SearchResultDoneImpl( searchRequest.getMessageId() );
                            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                                Dn.EMPTY_DN, ine );
                        }
                    }
                    else
                    {
                        baseObject = Dn.EMPTY_DN;
                    }

                    searchRequest.setBase(baseObject);

                    LOG.debug( "Searching with root Dn : {}", baseObject );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from BaseObject to Scope
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     scope ENUMERATED {
        //         baseObject   (0),
        //         singleLevel  (1),
        //         wholeSubtree (2) },
        //     ...
        //
        // We have a value for the scope, we will store it in the message
        super.transitions[LdapStatesEnum.BASE_OBJECT_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.BASE_OBJECT_STATE, LdapStatesEnum.SCOPE_STATE, UniversalTag.ENUMERATED.getValue(),
            new GrammarAction( "store scope value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    SearchRequest searchRequest = ldapMessageContainer.getSearchRequestDecorator().getSearchRequest();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to check that this is a correct scope
                    Value value = tlv.getValue();
                    int scope = 0;

                    try
                    {
                        scope = IntegerDecoder.parse( value, LdapConstants.SCOPE_BASE_OBJECT,
                            LdapConstants.SCOPE_WHOLE_SUBTREE );
                    }
                    catch ( IntegerDecoderException ide )
                    {
                        String msg = I18n.err( I18n.ERR_04101, value.toString() );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }

                    searchRequest.setScope( SearchScope.getSearchScope(scope) );

                    if ( IS_DEBUG )
                    {
                        switch ( scope )
                        {
                            case LdapConstants.SCOPE_BASE_OBJECT:
                                LOG.debug( "Searching within BASE_OBJECT scope " );
                                break;

                            case LdapConstants.SCOPE_SINGLE_LEVEL:
                                LOG.debug( "Searching within SINGLE_LEVEL scope " );
                                break;

                            case LdapConstants.SCOPE_WHOLE_SUBTREE:
                                LOG.debug( "Searching within WHOLE_SUBTREE scope " );
                                break;
                        }
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from Scope to DerefAlias
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     derefAliases ENUMERATED {
        //         neverDerefAliases   (0),
        //         derefInSearching    (1),
        //         derefFindingBaseObj (2),
        //         derefAlways         (3) },
        //     ...
        //
        // We have a value for the derefAliases, we will store it in the message
        super.transitions[LdapStatesEnum.SCOPE_STATE.ordinal()][UniversalTag.ENUMERATED.getValue()] = new GrammarTransition(
            LdapStatesEnum.SCOPE_STATE, LdapStatesEnum.DEREF_ALIAS_STATE, UniversalTag.ENUMERATED.getValue(),
            new GrammarAction( "store derefAliases value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    SearchRequest searchRequest = ldapMessageContainer.getSearchRequestDecorator().getSearchRequest();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We have to check that this is a correct derefAliases
                    Value value = tlv.getValue();
                    int derefAliases = 0;

                    try
                    {
                        derefAliases = IntegerDecoder.parse(value, LdapConstants.NEVER_DEREF_ALIASES,
                                LdapConstants.DEREF_ALWAYS);
                    }
                    catch ( IntegerDecoderException ide )
                    {
                        String msg = I18n.err( I18n.ERR_04102, value.toString() );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }

                    searchRequest.setDerefAliases( AliasDerefMode.getDerefMode( derefAliases ) );

                    if ( IS_DEBUG )
                    {
                        switch ( derefAliases )
                        {
                            case LdapConstants.NEVER_DEREF_ALIASES:
                                LOG.debug( "Handling object strategy : NEVER_DEREF_ALIASES" );
                                break;

                            case LdapConstants.DEREF_IN_SEARCHING:
                                LOG.debug( "Handling object strategy : DEREF_IN_SEARCHING" );
                                break;

                            case LdapConstants.DEREF_FINDING_BASE_OBJ:
                                LOG.debug( "Handling object strategy : DEREF_FINDING_BASE_OBJ" );
                                break;

                            case LdapConstants.DEREF_ALWAYS:
                                LOG.debug( "Handling object strategy : DEREF_ALWAYS" );
                                break;
                        }
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from DerefAlias to SizeLimit
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     sizeLimit INTEGER (0 .. maxInt),
        //     ...
        //
        // We have a value for the sizeLimit, we will store it in the message
        super.transitions[LdapStatesEnum.DEREF_ALIAS_STATE.ordinal()][UniversalTag.INTEGER.getValue()] = new GrammarTransition(
            LdapStatesEnum.DEREF_ALIAS_STATE, LdapStatesEnum.SIZE_LIMIT_STATE, UniversalTag.INTEGER.getValue(),
            new GrammarAction( "store sizeLimit value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    SearchRequest searchRequest = ldapMessageContainer.getSearchRequestDecorator().getSearchRequest();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // The current TLV should be a integer
                    // We get it and store it in sizeLimit
                    Value value = tlv.getValue();
                    long sizeLimit = 0;

                    try
                    {
                        sizeLimit = LongDecoder.parse( value, 0, Integer.MAX_VALUE );
                    }
                    catch ( LongDecoderException lde )
                    {
                        String msg = I18n.err( I18n.ERR_04103, value.toString() );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }

                    searchRequest.setSizeLimit( sizeLimit );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "The sizeLimit value is set to {} objects", Long.valueOf( sizeLimit ) );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from SizeLimit to TimeLimit
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     timeLimit INTEGER (0 .. maxInt),
        //     ...
        //
        // We have a value for the timeLimit, we will store it in the message
        super.transitions[LdapStatesEnum.SIZE_LIMIT_STATE.ordinal()][UniversalTag.INTEGER.getValue()] = new GrammarTransition(
            LdapStatesEnum.SIZE_LIMIT_STATE, LdapStatesEnum.TIME_LIMIT_STATE, UniversalTag.INTEGER.getValue(),
            new GrammarAction( "store timeLimit value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    SearchRequest searchRequest = ldapMessageContainer.getSearchRequestDecorator().getSearchRequest();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // The current TLV should be a integer
                    // We get it and store it in timeLimit
                    Value value = tlv.getValue();

                    int timeLimit = 0;

                    try
                    {
                        timeLimit = IntegerDecoder.parse( value, 0, Integer.MAX_VALUE );
                    }
                    catch ( IntegerDecoderException ide )
                    {
                        String msg = I18n.err( I18n.ERR_04104, value.toString() );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }

                    searchRequest.setTimeLimit( timeLimit );

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "The timeLimit value is set to {} seconds", Integer.valueOf( timeLimit ) );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from TimeLimit to TypesOnly
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     typesOnly BOOLEAN,
        //     ...
        //
        // We have a value for the typesOnly, we will store it in the message.
        super.transitions[LdapStatesEnum.TIME_LIMIT_STATE.ordinal()][UniversalTag.BOOLEAN.getValue()] = new GrammarTransition(
            LdapStatesEnum.TIME_LIMIT_STATE, LdapStatesEnum.TYPES_ONLY_STATE, UniversalTag.BOOLEAN.getValue(),
            new GrammarAction( "store typesOnly value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    SearchRequest searchRequest = ldapMessageContainer.getSearchRequestDecorator().getSearchRequest();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // We get the value. If it's a 0, it's a FALSE. If it's
                    // a FF, it's a TRUE. Any other value should be an error,
                    // but we could relax this constraint. So if we have
                    // something
                    // which is not 0, it will be interpreted as TRUE, but we
                    // will generate a warning.
                    Value value = tlv.getValue();

                    try
                    {
                        searchRequest.setTypesOnly( BooleanDecoder.parse( value ) );
                    }
                    catch ( BooleanDecoderException bde )
                    {
                        LOG.error( I18n
                            .err( I18n.ERR_04105, Strings.dumpBytes(value.getData()), bde.getMessage() ) );

                        throw new DecoderException( bde.getMessage() );
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "The search will return {}", ( searchRequest.getTypesOnly() ? "only attributs type"
                            : "attributes types and values" ) );
                    }
                }
            } );

        //============================================================================================
        // Search Request And Filter
        // This is quite complicated, because we have a tree structure to build,
        // and we may have many elements on each node. For instance, considering the 
        // search filter :
        // (& (| (a = b) (c = d)) (! (e = f)) (attr =* h))
        // We will have to create an And filter with three children :
        //  - an Or child,
        //  - a Not child
        //  - and a Present child.
        // The Or child will also have two children.
        //
        // We know when we have a children while decoding the PDU, because the length
        // of its parent has not yet reached its expected length.
        //
        // This search filter :
        // (&(|(objectclass=top)(ou=contacts))(!(objectclass=ttt))(objectclass=*top))
        // is encoded like this :
        //                              +----------------+---------------+
        //                              | ExpectedLength | CurrentLength |
        //+-----------------------------+----------------+---------------+
        //|A0 52                        | 82             | 0             | new level 1
        //|   A1 24                     | 82 36          | 0 0           | new level 2
        //|      A3 12                  | 82 36 18       | 0 0 0         | new level 3
        //|         04 0B 'objectclass' | 82 36 18       | 0 0 13        |
        //|         04 03 'top'         | 82 36 18       | 0 20 18       | 
        //|                             |       ^               ^        |
        //|                             |       |               |        |
        //|                             |       +---------------+        |
        //+-----------------------------* end level 3 -------------------*
        //|      A3 0E                  | 82 36 14       | 0 0 0         | new level 3
        //|         04 02 'ou'          | 82 36 14       | 0 0 4         |
        //|         04 08 'contacts'    | 82 36 14       | 38 36 14      | 
        //|                             |    ^  ^             ^  ^       |
        //|                             |    |  |             |  |       |
        //|                             |    |  +-------------|--+       |
        //|                             |    +----------------+          |
        //+-----------------------------* end level 3, end level 2 ------*
        //|   A2 14                     | 82 20          | 38 0          | new level 2
        //|      A3 12                  | 82 20 18       | 38 0 0        | new level 3
        //|         04 0B 'objectclass' | 82 20 18       | 38 0 13       | 
        //|         04 03 'ttt'         | 82 20 18       | 60 20 18      |
        //|                             |    ^  ^             ^  ^       |
        //|                             |    |  |             |  |       |
        //|                             |    |  +-------------|--+       |
        //|                             |    +----------------+          |
        //+-----------------------------* end level 3, end level 2 ------*
        //|   A4 14                     | 82 20          | 60 0          | new level 2
        //|      04 0B 'objectclass'    | 82 20          | 60 13         |
        //|      30 05                  | 82 20          | 60 13         |
        //|         82 03 'top'         | 82 20          | 82 20         | 
        //|                             | ^  ^             ^  ^          |
        //|                             | |  |             |  |          |
        //|                             | |  +-------------|--+          |
        //|                             | +----------------+             |
        //+-----------------------------* end level 2, end level 1 ------*
        //+-----------------------------+----------------+---------------+
        //
        // When the current length equals the expected length of the parent PDU,
        // then we are able to 'close' the parent : it has all its children. This
        // is propagated through all the tree, until either there are no more
        // parents, or the expected length of the parent is different from the
        // current length.

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG,
            new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG,
            new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG,
            new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init Equality filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE,
            LdapConstants.EQUALITY_MATCH_FILTER_TAG, new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE,
            LdapConstants.SUBSTRINGS_FILTER_TAG, new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE,
            LdapConstants.GREATER_OR_EQUAL_FILTER_TAG, new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE,
            LdapConstants.LESS_OR_EQUAL_FILTER_TAG, new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init Present Match filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.APPROX_MATCH_STATE, LdapConstants.APPROX_MATCH_FILTER_TAG,
            new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from TypesOnly to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Extensible Match filter
        super.transitions[LdapStatesEnum.TYPES_ONLY_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPES_ONLY_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE,
            LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG, new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG, new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG, new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG, new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE, LdapConstants.EQUALITY_MATCH_FILTER_TAG,
            new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE, LdapConstants.SUBSTRINGS_FILTER_TAG,
            new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE, LdapConstants.GREATER_OR_EQUAL_FILTER_TAG,
            new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE, LdapConstants.LESS_OR_EQUAL_FILTER_TAG,
            new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.APPROX_MATCH_STATE, LdapConstants.APPROX_MATCH_FILTER_TAG,
            new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from AND to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.AND_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.AND_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE, LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG,
            new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG, new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG, new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG, new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE, LdapConstants.EQUALITY_MATCH_FILTER_TAG,
            new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE, LdapConstants.SUBSTRINGS_FILTER_TAG,
            new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE, LdapConstants.GREATER_OR_EQUAL_FILTER_TAG,
            new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE, LdapConstants.LESS_OR_EQUAL_FILTER_TAG,
            new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.APPROX_MATCH_STATE, LdapConstants.APPROX_MATCH_FILTER_TAG,
            new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from OR to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.OR_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.OR_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE, LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG,
            new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG, new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG, new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG, new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE, LdapConstants.EQUALITY_MATCH_FILTER_TAG,
            new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE, LdapConstants.SUBSTRINGS_FILTER_TAG,
            new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE, LdapConstants.GREATER_OR_EQUAL_FILTER_TAG,
            new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE, LdapConstants.LESS_OR_EQUAL_FILTER_TAG,
            new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init present filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.APPROX_MATCH_STATE, LdapConstants.APPROX_MATCH_FILTER_TAG,
            new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from NOT to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init extensible match filter
        super.transitions[LdapStatesEnum.NOT_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.NOT_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE, LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG,
            new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Equality match to Attribute Desc Filter
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch  [3] AttributeValueAssertion,
        //     ...
        //
        // AttributeValueAssertion ::= SEQUENCE {
        //     attributeDesc   AttributeDescription,
        //     ...
        //
        // Init Attribute Desc filter
        super.transitions[LdapStatesEnum.EQUALITY_MATCH_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.EQUALITY_MATCH_STATE, LdapStatesEnum.ATTRIBUTE_DESC_FILTER_STATE,
            UniversalTag.OCTET_STRING.getValue(), new InitAttributeDescFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Attribute Desc Filter to Assertion Value Filter
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch  [3] AttributeValueAssertion,
        //     ...
        //
        // AttributeValueAssertion ::= SEQUENCE {
        //     ...
        //     assertionValue   AssertionValue }
        //
        // Init Assertion Value filter
        super.transitions[LdapStatesEnum.ATTRIBUTE_DESC_FILTER_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_DESC_FILTER_STATE, LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE,
            UniversalTag.OCTET_STRING.getValue(), new InitAssertionValueFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG,
            new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG,
            new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG,
            new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE,
            LdapConstants.EQUALITY_MATCH_FILTER_TAG, new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE,
            LdapConstants.SUBSTRINGS_FILTER_TAG, new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE,
            LdapConstants.GREATER_OR_EQUAL_FILTER_TAG, new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE,
            LdapConstants.LESS_OR_EQUAL_FILTER_TAG, new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init present filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.PRESENT_STATE,
            LdapConstants.PRESENT_FILTER_TAG, new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.APPROX_MATCH_STATE,
            LdapConstants.APPROX_MATCH_FILTER_TAG, new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Assertion Value Filter filter
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE,
            LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG, new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Assertion Value Filter to Attribute Description List
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter      Filter,
        //     attributes  AttributeDescriptionList }
        //
        // AttributeDescriptionList ::= SEQUENCE OF
        //     AttributeDescription
        //
        // Init attribute description list
        super.transitions[LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.ASSERTION_VALUE_FILTER_STATE, LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE,
            UniversalTag.SEQUENCE.getValue(), new InitAttributeDescListAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Attribute Description List to AttributeDescription
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter      Filter,
        //     attributes  AttributeDescriptionList }
        //
        // AttributeDescriptionList ::= SEQUENCE OF
        //     AttributeDescription
        //
        // Store attribute description
        super.transitions[LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE, LdapStatesEnum.ATTRIBUTE_DESCRIPTION_STATE,
            UniversalTag.OCTET_STRING.getValue(), new AttributeDescAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Attribute Description List to Controls
        // --------------------------------------------------------------------------------------------
        //         searchRequest   SearchRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        // Empty attribute description list, with controls
        super.transitions[LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Attribute Description to AttributeDescription
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter      Filter,
        //     attributes  AttributeDescriptionList }
        //
        // AttributeDescriptionList ::= SEQUENCE OF
        //     AttributeDescription
        //
        // Store attribute description
        super.transitions[LdapStatesEnum.ATTRIBUTE_DESCRIPTION_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_DESCRIPTION_STATE, LdapStatesEnum.ATTRIBUTE_DESCRIPTION_STATE,
            UniversalTag.OCTET_STRING.getValue(), new AttributeDescAction() );

        // --------------------------------------------------------------------------------------------
        // transition from Attribute Description to Controls.
        // --------------------------------------------------------------------------------------------
        //         searchRequest   SearchRequest,
        //         ... },
        //     controls       [0] Controls OPTIONAL }
        //
        super.transitions[LdapStatesEnum.ATTRIBUTE_DESCRIPTION_STATE.ordinal()][LdapConstants.CONTROLS_TAG] = new GrammarTransition(
            LdapStatesEnum.ATTRIBUTE_DESCRIPTION_STATE, LdapStatesEnum.CONTROLS_STATE, LdapConstants.CONTROLS_TAG,
            new ControlsInitAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Greater Or Equal to Attribute Desc Filter
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // AttributeValueAssertion ::= SEQUENCE {
        //     attributeDesc   AttributeDescription,
        //     ...
        //
        // Init Attribute Desc filter
        super.transitions[LdapStatesEnum.GREATER_OR_EQUAL_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.GREATER_OR_EQUAL_STATE, LdapStatesEnum.ATTRIBUTE_DESC_FILTER_STATE,
            UniversalTag.OCTET_STRING.getValue(), new InitAttributeDescFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Less Or Equal to Attribute Desc Filter
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     lessOrEqual  [6] AttributeValueAssertion,
        //     ...
        //
        // AttributeValueAssertion ::= SEQUENCE {
        //     attributeDesc   AttributeDescription,
        //     ...
        //
        // Init Attribute Desc filter
        super.transitions[LdapStatesEnum.LESS_OR_EQUAL_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.LESS_OR_EQUAL_STATE, LdapStatesEnum.ATTRIBUTE_DESC_FILTER_STATE,
            UniversalTag.OCTET_STRING.getValue(), new InitAttributeDescFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Substrings to typeSubstring
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     substrings  [4] SubstringFilter,
        //     ...
        //
        // SubstringFilter ::= SEQUENCE {
        //     type   AttributeDescription,
        //     ...
        //
        // Init substring type
        super.transitions[LdapStatesEnum.SUBSTRING_FILTER_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.SUBSTRING_FILTER_STATE, LdapStatesEnum.TYPE_SUBSTRING_STATE, UniversalTag.OCTET_STRING.getValue(),
            new GrammarAction( "Store substring filter type" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    SearchRequestDecorator searchRequestDecorator = ldapMessageContainer.getSearchRequestDecorator();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Store the value.
                    SubstringFilter substringFilter = ( SubstringFilter ) searchRequestDecorator.getTerminalFilter();

                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04106 );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }
                    else
                    {
                        String type = getType(tlv.getValue().getData());
                        substringFilter.setType( type );

                        // We now have to get back to the nearest filter which
                        // is not terminal.
                        searchRequestDecorator.setTerminalFilter( substringFilter );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from typeSubstring to substrings
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     substrings  [4] SubstringFilter,
        //     ...
        //
        // SubstringFilter ::= SEQUENCE {
        //     ...
        //     substrings SEQUENCE OF CHOICE {
        //     ...
        //
        // Init substring type
        super.transitions[LdapStatesEnum.TYPE_SUBSTRING_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.TYPE_SUBSTRING_STATE, LdapStatesEnum.SUBSTRINGS_STATE, UniversalTag.SEQUENCE.getValue(),
            new GrammarAction( "Substring Filter substringsSequence " )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    if ( tlv.getLength() == 0 )
                    {
                        LOG.error( I18n.err( I18n.ERR_04107 ) );
                        throw new DecoderException( "The substring sequence is empty" );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from substrings to Initial
        // --------------------------------------------------------------------------------------------
        // SubstringFilter ::= SEQUENCE {
        //     ...
        //     substrings SEQUENCE OF CHOICE {
        //         initial  [0] LDAPSTRING,
        //         ...
        //
        // Store initial value
        super.transitions[LdapStatesEnum.SUBSTRINGS_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_INITIAL_TAG] = new GrammarTransition(
            LdapStatesEnum.SUBSTRINGS_STATE, LdapStatesEnum.INITIAL_STATE, LdapConstants.SUBSTRINGS_FILTER_INITIAL_TAG,
            new GrammarAction( "Store substring filter initial Value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    SearchRequestDecorator searchRequestDecorator = ldapMessageContainer.getSearchRequestDecorator();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Store the value.
                    SubstringFilter substringFilter = ( SubstringFilter ) searchRequestDecorator.getTerminalFilter();

                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04108 );
                        LOG.error( msg );
                        throw new DecoderException( msg );
                    }

                    substringFilter.setInitialSubstrings( Strings.utf8ToString(tlv.getValue().getData()) );

                    // We now have to get back to the nearest filter which is
                    // not terminal.
                    searchRequestDecorator.unstackFilters( container );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from substrings to any
        // --------------------------------------------------------------------------------------------
        // SubstringFilter ::= SEQUENCE {
        //     ...
        //     substrings SEQUENCE OF CHOICE {
        //         ...
        //         any  [1] LDAPSTRING,
        //         ...
        //
        // Store substring any type
        super.transitions[LdapStatesEnum.SUBSTRINGS_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_ANY_TAG] = new GrammarTransition(
            LdapStatesEnum.SUBSTRINGS_STATE, LdapStatesEnum.ANY_STATE, LdapConstants.SUBSTRINGS_FILTER_ANY_TAG,
            new StoreAnyAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from substrings to final
        // --------------------------------------------------------------------------------------------
        // SubstringFilter ::= SEQUENCE {
        //     ...
        //     substrings SEQUENCE OF CHOICE {
        //         ...
        //         final  [2] LDAPSTRING }
        //
        // Store substring final type
        super.transitions[LdapStatesEnum.SUBSTRINGS_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_FINAL_TAG] = new GrammarTransition(
            LdapStatesEnum.SUBSTRINGS_STATE, LdapStatesEnum.FINAL_STATE, LdapConstants.SUBSTRINGS_FILTER_FINAL_TAG,
            new StoreFinalAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to any
        // --------------------------------------------------------------------------------------------
        // SubstringFilter ::= SEQUENCE {
        //     ...
        //     substrings SEQUENCE OF CHOICE {
        //         ...
        //         any  [1] LDAPSTRING,
        //         ...
        //
        // Store substring any type
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_ANY_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.ANY_STATE, LdapConstants.SUBSTRINGS_FILTER_ANY_TAG,
            new StoreAnyAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to final
        // --------------------------------------------------------------------------------------------
        // SubstringFilter ::= SEQUENCE {
        //     ...
        //     substrings SEQUENCE OF CHOICE {
        //         ...
        //         final  [2] LDAPSTRING }
        //
        // Store substring final type
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_FINAL_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.FINAL_STATE, LdapConstants.SUBSTRINGS_FILTER_FINAL_TAG,
            new StoreFinalAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to Attribute Description List
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter      Filter,
        //     attributes  AttributeDescriptionList }
        //
        // AttributeDescriptionList ::= SEQUENCE OF
        //     AttributeDescription
        //
        // Init attribute description list
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE, UniversalTag.SEQUENCE.getValue(),
            new InitAttributeDescListAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG,
            new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG,
            new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG,
            new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE, LdapConstants.EQUALITY_MATCH_FILTER_TAG,
            new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE, LdapConstants.SUBSTRINGS_FILTER_TAG,
            new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE,
            LdapConstants.GREATER_OR_EQUAL_FILTER_TAG, new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE, LdapConstants.LESS_OR_EQUAL_FILTER_TAG,
            new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init present filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.APPROX_MATCH_STATE, LdapConstants.APPROX_MATCH_FILTER_TAG,
            new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from initial to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Assertion Value Filter filter
        super.transitions[LdapStatesEnum.INITIAL_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.INITIAL_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE,
            LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG, new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to final
        // --------------------------------------------------------------------------------------------
        // SubstringFilter ::= SEQUENCE {
        //     ...
        //     substrings SEQUENCE OF CHOICE {
        //         ...
        //         final  [2] LDAPSTRING }
        //
        // Store substring final type
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_FINAL_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.FINAL_STATE, LdapConstants.SUBSTRINGS_FILTER_FINAL_TAG,
            new StoreFinalAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to any
        // --------------------------------------------------------------------------------------------
        // SubstringFilter ::= SEQUENCE {
        //     ...
        //     substrings SEQUENCE OF CHOICE {
        //         ...
        //         any  [1] LDAPSTRING 
        //         ...
        //
        // Store substring any type
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_ANY_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.ANY_STATE, LdapConstants.SUBSTRINGS_FILTER_ANY_TAG,
            new StoreAnyAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to Attribute Description List
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter      Filter,
        //     attributes  AttributeDescriptionList }
        //
        // AttributeDescriptionList ::= SEQUENCE OF
        //     AttributeDescription
        //
        // Init attribute description list
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE, UniversalTag.SEQUENCE.getValue(),
            new InitAttributeDescListAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG, new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG, new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG, new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE, LdapConstants.EQUALITY_MATCH_FILTER_TAG,
            new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE, LdapConstants.SUBSTRINGS_FILTER_TAG,
            new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE, LdapConstants.GREATER_OR_EQUAL_FILTER_TAG,
            new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE, LdapConstants.LESS_OR_EQUAL_FILTER_TAG,
            new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init present filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.APPROX_MATCH_STATE, LdapConstants.APPROX_MATCH_FILTER_TAG,
            new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from any to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Assertion Value Filter filter
        super.transitions[LdapStatesEnum.ANY_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.ANY_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE, LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG,
            new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to Attribute Description List
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter      Filter,
        //     attributes  AttributeDescriptionList }
        //
        // AttributeDescriptionList ::= SEQUENCE OF
        //     AttributeDescription
        //
        // Init attribute description list
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE, UniversalTag.SEQUENCE.getValue(),
            new InitAttributeDescListAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG,
            new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG, new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG,
            new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE, LdapConstants.EQUALITY_MATCH_FILTER_TAG,
            new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE, LdapConstants.SUBSTRINGS_FILTER_TAG,
            new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE,
            LdapConstants.GREATER_OR_EQUAL_FILTER_TAG, new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE, LdapConstants.LESS_OR_EQUAL_FILTER_TAG,
            new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init present filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.APPROX_MATCH_STATE, LdapConstants.APPROX_MATCH_FILTER_TAG,
            new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from final to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Assertion Value Filter filter
        super.transitions[LdapStatesEnum.FINAL_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.FINAL_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE,
            LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG, new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG,
            new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG,
            new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG,
            new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE, LdapConstants.EQUALITY_MATCH_FILTER_TAG,
            new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE, LdapConstants.SUBSTRINGS_FILTER_TAG,
            new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE,
            LdapConstants.GREATER_OR_EQUAL_FILTER_TAG, new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE, LdapConstants.LESS_OR_EQUAL_FILTER_TAG,
            new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init present filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.APPROX_MATCH_STATE, LdapConstants.APPROX_MATCH_FILTER_TAG,
            new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Assertion Value Filter filter
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE,
            LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG, new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Present Filter to Attribute Description List
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter      Filter,
        //     attributes  AttributeDescriptionList }
        //
        // AttributeDescriptionList ::= SEQUENCE OF
        //     AttributeDescription
        //
        // Init attribute description list
        super.transitions[LdapStatesEnum.PRESENT_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.PRESENT_STATE, LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE, UniversalTag.SEQUENCE.getValue(),
            new InitAttributeDescListAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Approx Match to Attribute Desc Filter
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch  [8] AttributeValueAssertion,
        //     ...
        //
        // AttributeValueAssertion ::= SEQUENCE {
        //     attributeDesc   AttributeDescription,
        //     ...
        //
        // Init Attribute Desc filter
        super.transitions[LdapStatesEnum.APPROX_MATCH_STATE.ordinal()][UniversalTag.OCTET_STRING.getValue()] = new GrammarTransition(
            LdapStatesEnum.APPROX_MATCH_STATE, LdapStatesEnum.ATTRIBUTE_DESC_FILTER_STATE,
            UniversalTag.OCTET_STRING.getValue(), new InitAttributeDescFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Extensible Match to MatchingRule
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion }
        //
        // MatchingRuleAssertion ::= SEQUENCE {
        //     matchingRule [1] MatchingRuleId OPTIONAL,
        //     ...
        //
        // Store the matching rule ID 
        super.transitions[LdapStatesEnum.EXTENSIBLE_MATCH_STATE.ordinal()][LdapConstants.MATCHING_RULE_ID_TAG] = new GrammarTransition(
            LdapStatesEnum.EXTENSIBLE_MATCH_STATE, LdapStatesEnum.MATCHING_RULE_STATE,
            LdapConstants.MATCHING_RULE_ID_TAG, new GrammarAction( "Store matching rule Value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    SearchRequestDecorator searchRequest = ldapMessageContainer.getSearchRequestDecorator();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Store the value.
                    ExtensibleMatchFilter extensibleMatchFilter = ( ExtensibleMatchFilter )
                            searchRequest.getTerminalFilter();

                    if ( tlv.getLength() == 0 )
                    {
                        String msg = I18n.err( I18n.ERR_04109 );
                        LOG.error( msg );

                        // It will generate a PROTOCOL_ERROR
                        throw new DecoderException( I18n.err( I18n.ERR_04109 ) );
                    }
                    else
                    {
                        extensibleMatchFilter.setMatchingRule( Strings.utf8ToString(tlv.getValue().getData()) );
                    }
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from Extensible Match to type matching rule
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion }
        //
        // MatchingRuleAssertion ::= SEQUENCE {
        //     ...
        //     type [2] AttributeDescription OPTIONAL,
        //     ...
        //
        // Store the matching rule ID 
        super.transitions[LdapStatesEnum.EXTENSIBLE_MATCH_STATE.ordinal()][LdapConstants.MATCHING_RULE_TYPE_TAG] = new GrammarTransition(
            LdapStatesEnum.EXTENSIBLE_MATCH_STATE, LdapStatesEnum.TYPE_MATCHING_RULE_STATE,
            LdapConstants.MATCHING_RULE_TYPE_TAG, new StoreTypeMatchingRuleAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from Extensible Match to match value
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion }
        //
        // MatchingRuleAssertion ::= SEQUENCE {
        //     ...
        //     matchValue [3] AssertionValue,
        //     ...
        //
        // Store the matching rule ID 
        super.transitions[LdapStatesEnum.EXTENSIBLE_MATCH_STATE.ordinal()][LdapConstants.MATCH_VALUE_TAG] = new GrammarTransition(
            LdapStatesEnum.EXTENSIBLE_MATCH_STATE, LdapStatesEnum.MATCH_VALUE_STATE, LdapConstants.MATCH_VALUE_TAG,
            new StoreMatchValueAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from matching rule to type matching rule
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion }
        //
        // MatchingRuleAssertion ::= SEQUENCE {
        //     ...
        //     type [2] AttributeDescription OPTIONAL,
        //     ...
        //
        // Store the matching rule ID 
        super.transitions[LdapStatesEnum.MATCHING_RULE_STATE.ordinal()][LdapConstants.MATCHING_RULE_TYPE_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCHING_RULE_STATE, LdapStatesEnum.TYPE_MATCHING_RULE_STATE,
            LdapConstants.MATCHING_RULE_TYPE_TAG, new StoreTypeMatchingRuleAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from matching rule to match value
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion }
        //
        // MatchingRuleAssertion ::= SEQUENCE {
        //     ...
        //     matchValue [3] AssertionValue,
        //     ...
        //
        // Store the matching rule ID 
        super.transitions[LdapStatesEnum.MATCHING_RULE_STATE.ordinal()][LdapConstants.MATCH_VALUE_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCHING_RULE_STATE, LdapStatesEnum.MATCH_VALUE_STATE, LdapConstants.MATCH_VALUE_TAG,
            new StoreMatchValueAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from matching type to match value
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion }
        //
        // MatchingRuleAssertion ::= SEQUENCE {
        //     ...
        //     matchValue [3] AssertionValue,
        //     ...
        //
        // Store the matching rule ID 
        super.transitions[LdapStatesEnum.TYPE_MATCHING_RULE_STATE.ordinal()][LdapConstants.MATCH_VALUE_TAG] = new GrammarTransition(
            LdapStatesEnum.TYPE_MATCHING_RULE_STATE, LdapStatesEnum.MATCH_VALUE_STATE, LdapConstants.MATCH_VALUE_TAG,
            new StoreMatchValueAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to dnAttributes
        // --------------------------------------------------------------------------------------------
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion }
        //
        // MatchingRuleAssertion ::= SEQUENCE {
        //     ...
        //     dnAttributes [4] BOOLEAN DEFAULT FALSE }
        //
        // Store the dnAttributes flag 
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.DN_ATTRIBUTES_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.DN_ATTRIBUTES_STATE,
            LdapConstants.DN_ATTRIBUTES_FILTER_TAG, new GrammarAction( "Store matching dnAttributes Value" )
            {
                public void action( Asn1Container container ) throws DecoderException
                {
                    LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;
                    SearchRequestDecorator searchRequest = ldapMessageContainer.getSearchRequestDecorator();

                    TLV tlv = ldapMessageContainer.getCurrentTLV();

                    // Store the value.
                    ExtensibleMatchFilter extensibleMatchFilter = ( ExtensibleMatchFilter ) searchRequest.getTerminalFilter();

                    // We get the value. If it's a 0, it's a FALSE. If it's
                    // a FF, it's a TRUE. Any other value should be an error,
                    // but we could relax this constraint. So if we have
                    // something
                    // which is not 0, it will be interpreted as TRUE, but we
                    // will generate a warning.
                    Value value = tlv.getValue();

                    try
                    {
                        extensibleMatchFilter.setDnAttributes( BooleanDecoder.parse( value ) );
                    }
                    catch ( BooleanDecoderException bde )
                    {
                        LOG.error( I18n
                            .err( I18n.ERR_04110, Strings.dumpBytes(value.getData()), bde.getMessage() ) );

                        throw new DecoderException( bde.getMessage() );
                    }

                    if ( IS_DEBUG )
                    {
                        LOG.debug( "Dn Attributes : {}", Boolean.valueOf( extensibleMatchFilter.isDnAttributes() ) );
                    }

                    // unstack the filters if needed
                    searchRequest.unstackFilters( ldapMessageContainer );
                }
            } );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG,
            new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG,
            new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG,
            new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE,
            LdapConstants.EQUALITY_MATCH_FILTER_TAG, new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE,
            LdapConstants.SUBSTRINGS_FILTER_TAG, new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE,
            LdapConstants.GREATER_OR_EQUAL_FILTER_TAG, new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE,
            LdapConstants.LESS_OR_EQUAL_FILTER_TAG, new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init present filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.APPROX_MATCH_STATE, LdapConstants.APPROX_MATCH_FILTER_TAG,
            new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Assertion Value Filter filter
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE,
            LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG, new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from match value to Attribute Description List
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter      Filter,
        //     attributes  AttributeDescriptionList }
        //
        // AttributeDescriptionList ::= SEQUENCE OF
        //     AttributeDescription
        //
        // Init attribute description list
        super.transitions[LdapStatesEnum.MATCH_VALUE_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.MATCH_VALUE_STATE, LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE,
            UniversalTag.SEQUENCE.getValue(), new InitAttributeDescListAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to AND filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     and             [0] SET OF Filter,
        //     ...
        //
        // Init AND filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.AND_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.AND_STATE, LdapConstants.AND_FILTER_TAG,
            new InitAndFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to OR filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     or              [1] SET OF Filter,
        //     ...
        //
        // Init OR filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.OR_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.OR_STATE, LdapConstants.OR_FILTER_TAG,
            new InitOrFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to NOT filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     not             [2] SET OF Filter,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.NOT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.NOT_STATE, LdapConstants.NOT_FILTER_TAG,
            new InitNotFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to Equality Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     equalityMatch   [3] AttributeValueAssertion,
        //     ...
        //
        // Init NOT filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.EQUALITY_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.EQUALITY_MATCH_STATE,
            LdapConstants.EQUALITY_MATCH_FILTER_TAG, new InitEqualityMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to Substrings filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     substrings     [4] SubstringFilter,
        //     ...
        //
        // Init Substrings filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.SUBSTRINGS_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.SUBSTRING_FILTER_STATE,
            LdapConstants.SUBSTRINGS_FILTER_TAG, new InitSubstringsFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to GreaterOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     greaterOrEqual  [5] AttributeValueAssertion,
        //     ...
        //
        // Init Greater Or Equal filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.GREATER_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.GREATER_OR_EQUAL_STATE,
            LdapConstants.GREATER_OR_EQUAL_FILTER_TAG, new InitGreaterOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to LessOrEqual filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     LessOrEqual    [6] AttributeValueAssertion,
        //     ...
        //
        // Init Less Or Equal filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.LESS_OR_EQUAL_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.LESS_OR_EQUAL_STATE,
            LdapConstants.LESS_OR_EQUAL_FILTER_TAG, new InitLessOrEqualFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to Present filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     present        [7] AttributeDescription,
        //     ...
        //
        // Init present filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.PRESENT_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.PRESENT_STATE, LdapConstants.PRESENT_FILTER_TAG,
            new InitPresentFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to Approx Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     approxMatch     [8] AttributeValueAssertion,
        //     ...
        //
        // Init Approx Match filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.APPROX_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.APPROX_MATCH_STATE,
            LdapConstants.APPROX_MATCH_FILTER_TAG, new InitApproxMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to Extensible Match filter
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter Filter,
        //     ...
        //
        // Filter ::= CHOICE {
        //     ...
        //     extensibleMatch  [9] MatchingRuleAssertion,
        //     ...
        //
        // Init Assertion Value Filter filter
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.EXTENSIBLE_MATCH_STATE,
            LdapConstants.EXTENSIBLE_MATCH_FILTER_TAG, new InitExtensibleMatchFilterAction() );

        // --------------------------------------------------------------------------------------------
        // Transition from dnAttributes to Attribute Description List
        // --------------------------------------------------------------------------------------------
        // SearchRequest ::= [APPLICATION 3] SEQUENCE {
        //     ...
        //     filter      Filter,
        //     attributes  AttributeDescriptionList }
        //
        // AttributeDescriptionList ::= SEQUENCE OF
        //     AttributeDescription
        //
        // Init attribute description list
        super.transitions[LdapStatesEnum.DN_ATTRIBUTES_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] = new GrammarTransition(
            LdapStatesEnum.DN_ATTRIBUTES_STATE, LdapStatesEnum.ATTRIBUTE_DESCRIPTION_LIST_STATE,
            UniversalTag.SEQUENCE.getValue(), new InitAttributeDescListAction() );
    }


    // ~ Methods
    // ------------------------------------------------------------------------------------

    /**
     * Get the instance of this grammar
     * 
     * @return An instance on the LdapMessage Grammar
     */
    public static Grammar getInstance()
    {
        return instance;
    }

    /**
     * Build an AttributeType froma byte array. An AttributeType contains
     * only chars within [0-9][a-z][A-Z][-.].
     *
     * @param bytes The bytes containing the AttributeType
     * @return The AttributeType as a String
     */
    public static String getType( byte[] bytes )
    {
        if ( bytes == null )
        {
            return null;
        }

        char[] chars = new char[bytes.length];
        int pos = 0;

        for ( byte b:bytes )
        {
            chars[pos++] = ( char ) b;
        }

        return new String( chars );
    }
}
