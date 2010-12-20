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

package org.apache.directory.shared.dsmlv2.engine;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.apache.directory.shared.asn1.DecoderException;
import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.dsmlv2.Dsmlv2Parser;
import org.apache.directory.shared.dsmlv2.reponse.AddResponseDsml;
import org.apache.directory.shared.dsmlv2.reponse.AuthResponseDsml;
import org.apache.directory.shared.dsmlv2.reponse.BatchResponseDsml;
import org.apache.directory.shared.dsmlv2.reponse.CompareResponseDsml;
import org.apache.directory.shared.dsmlv2.reponse.DelResponseDsml;
import org.apache.directory.shared.dsmlv2.reponse.ErrorResponse;
import org.apache.directory.shared.dsmlv2.reponse.ErrorResponse.ErrorResponseType;
import org.apache.directory.shared.dsmlv2.reponse.ExtendedResponseDsml;
import org.apache.directory.shared.dsmlv2.reponse.ModDNResponseDsml;
import org.apache.directory.shared.dsmlv2.reponse.ModifyResponseDsml;
import org.apache.directory.shared.dsmlv2.reponse.SearchResponseDsml;
import org.apache.directory.shared.dsmlv2.reponse.SearchResultEntryDsml;
import org.apache.directory.shared.dsmlv2.reponse.SearchResultReferenceDsml;
import org.apache.directory.shared.dsmlv2.request.BatchRequest;
import org.apache.directory.shared.dsmlv2.request.BatchRequest.OnError;
import org.apache.directory.shared.dsmlv2.request.BatchRequest.Processing;
import org.apache.directory.shared.dsmlv2.request.BatchRequest.ResponseOrder;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.message.MessageTypeEnum;
import org.apache.directory.shared.ldap.cursor.Cursor;
import org.apache.directory.shared.ldap.exception.LdapException;
import org.apache.directory.shared.ldap.message.*;
import org.apache.directory.shared.ldap.name.DN;
import org.apache.directory.shared.ldap.util.StringTools;
import org.xmlpull.v1.XmlPullParserException;


/**
 * This is the DSMLv2Engine. It can be use to execute operations on a LDAP Server and get the results of these operations.
 * The format used for request and responses is the DSMLv2 format.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class Dsmlv2Engine
{
    /** The user. */
    private String user;

    /** The password. */
    private String password;

    /** The LDAP connection */
    private LdapConnection connection;

    /** The DSVMv2 parser. */
    private Dsmlv2Parser parser;

    /** The continue on error flag. */
    private boolean continueOnError;

    /** The exit flag. */
    private boolean exit = false;

    /** The batch request. */
    private BatchRequest batchRequest;

    /** The batch response. */
    private BatchResponseDsml batchResponse;


    /**
     * Creates a new instance of Dsmlv2Engine.
     * 
     * @param host the server host
     * @param port the server port
     * @param user the server admin DN
     * @param password the server admin's password
     */
    public Dsmlv2Engine( String host, int port, String user, String password )
    {
        this.user = user;
        this.password = password;

        connection = new LdapNetworkConnection( host, port );
    }


    /**
     * Processes the file given and return the result of the operations
     * 
     * @param dsmlInput 
     *      the DSMLv2 formatted request input
     * @return
     *      the XML response in DSMLv2 Format
     * @throws XmlPullParserException
     *      if an error occurs in the parser
     */
    public String processDSML( String dsmlInput ) throws XmlPullParserException
    {
        parser = new Dsmlv2Parser();
        parser.setInput( dsmlInput );

        return processDSML();
    }


    /**
     * Processes the file given and return the result of the operations
     * 
     * @param fileName 
     *      the path to the file
     * @return 
     *      the XML response in DSMLv2 Format
     * @throws XmlPullParserException
     *      if an error occurs in the parser
     * @throws FileNotFoundException
     *      if the file does not exist
     */
    public String processDSMLFile( String fileName ) throws XmlPullParserException, FileNotFoundException
    {
        parser = new Dsmlv2Parser();
        parser.setInputFile( fileName );

        return processDSML();
    }


    /**
     * Processes the file given and return the result of the operations
     * 
     * @param inputStream 
     *      contains a raw byte input stream of possibly unknown encoding (when inputEncoding is null).
     * @param inputEncoding 
     *      if not null it MUST be used as encoding for inputStream
     * @return 
     *      the XML response in DSMLv2 Format
     * @throws XmlPullParserException
     *      if an error occurs in the parser
     */
    public String processDSML( InputStream inputStream, String inputEncoding ) throws XmlPullParserException
    {
        parser = new Dsmlv2Parser();
        parser.setInput( inputStream, inputEncoding );
        return processDSML();
    }


    /**
     * Processes the Request document
     * 
     * @return the XML response in DSMLv2 Format
     */
    private String processDSML()
    {
        batchResponse = new BatchResponseDsml();

        // Binding to LDAP Server
        try
        {
            bind( 1 );
        }
        catch ( Exception e )
        {
            // Unable to connect to server
            // We create a new ErrorResponse and return the XML response.
            ErrorResponse errorResponse = new ErrorResponse( 0, ErrorResponseType.COULD_NOT_CONNECT, e
                .getLocalizedMessage() );
            batchResponse.addResponse( errorResponse );
            return batchResponse.toDsml();
        }

        // Processing BatchRequest:
        //    - Parsing and Getting BatchRequest
        //    - Getting and registering options from BatchRequest
        try
        {
            processBatchRequest();
        }
        catch ( XmlPullParserException e )
        {
            // We create a new ErrorResponse and return the XML response.
            ErrorResponse errorResponse = new ErrorResponse( 0, ErrorResponseType.MALFORMED_REQUEST, I18n.err(
                I18n.ERR_03001, e.getLocalizedMessage(), e.getLineNumber(), e.getColumnNumber() ) );
            batchResponse.addResponse( errorResponse );
            return batchResponse.toDsml();
        }

        // Processing each request:
        //    - Getting a new request
        //    - Checking if the request is well formed
        //    - Sending the request to the server
        //    - Getting and converting reponse(s) as XML
        //    - Looping until last request
        Message request = null;

        try
        {
            request = parser.getNextRequest();
        }
        catch ( XmlPullParserException e )
        {
            // We create a new ErrorResponse and return the XML response.
            ErrorResponse errorResponse = new ErrorResponse( 0, ErrorResponseType.MALFORMED_REQUEST, I18n.err(
                I18n.ERR_03001, e.getLocalizedMessage(), e.getLineNumber(), e.getColumnNumber() ) );
            batchResponse.addResponse( errorResponse );
            return batchResponse.toDsml();
        }

        while ( request != null ) // (Request == null when there's no more request to process)
        {
            // Checking the request has a requestID attribute if Processing = Parallel and ResponseOrder = Unordered
            if ( ( batchRequest.getProcessing().equals( Processing.PARALLEL ) )
                && ( batchRequest.getResponseOrder().equals( ResponseOrder.UNORDERED ) )
                && ( request.getMessageId() <= 0 ) )
            {
                // Then we have to send an errorResponse
                ErrorResponse errorResponse = new ErrorResponse( 0, ErrorResponseType.MALFORMED_REQUEST, I18n
                    .err( I18n.ERR_03002 ) );
                batchResponse.addResponse( errorResponse );
                return batchResponse.toDsml();
            }

            try
            {
                processRequest( request );
            }
            catch ( Exception e )
            {
                // We create a new ErrorResponse and return the XML response.
                ErrorResponse errorResponse = new ErrorResponse( 0, ErrorResponseType.GATEWAY_INTERNAL_ERROR, I18n.err(
                    I18n.ERR_03003, e.getMessage() ) );
                batchResponse.addResponse( errorResponse );
                return batchResponse.toDsml();
            }

            // Checking if we need to exit processing (if an error has occurred if onError == Exit)
            if ( exit )
            {
                break;
            }

            // Getting next request
            try
            {
                request = parser.getNextRequest();
            }
            catch ( XmlPullParserException e )
            {
                // We create a new ErrorResponse and return the XML response.
                ErrorResponse errorResponse = new ErrorResponse( 0, ErrorResponseType.MALFORMED_REQUEST, I18n.err(
                    I18n.ERR_03001, e.getLocalizedMessage(), e.getLineNumber(), e.getColumnNumber() ) );
                batchResponse.addResponse( errorResponse );
                return batchResponse.toDsml();
            }
        }

        return batchResponse.toDsml();
    }


    /**
     * Processes a single request
     * 
     * @param request the request to process
     */
    private void processRequest( Message request ) throws Exception
    {
        ResultCodeEnum resultCode = null;

        switch ( request.getType() )
        {
            case ABANDON_REQUEST:
                connection.abandon( ( AbandonRequest ) request );
                return;

            case ADD_REQUEST:
                AddResponse response = connection.add( ( AddRequest ) request );
                AddResponseDsml addResponseDsml = new AddResponseDsml( response );
                batchResponse.addResponse( addResponseDsml );

                break;

            case BIND_REQUEST:
                BindResponse bindResponse = connection.bind( ( BindRequest ) request );
                AuthResponseDsml authResponseDsml = new AuthResponseDsml( bindResponse );
                batchResponse.addResponse( authResponseDsml );

                break;

            case COMPARE_REQUEST:
                CompareResponse compareResponse = connection.compare( ( CompareRequest ) request );
                CompareResponseDsml compareResponseDsml = new CompareResponseDsml( compareResponse );
                batchResponse.addResponse( compareResponseDsml );

                break;

            case DEL_REQUEST:
                DeleteResponse delResponse = connection.delete( ( DeleteRequest ) request );
                DelResponseDsml delResponseDsml = new DelResponseDsml( delResponse );
                batchResponse.addResponse( delResponseDsml );

                break;

            case EXTENDED_REQUEST:
                ExtendedResponse extendedResponse = connection.extended( ( ExtendedRequest ) request );
                ExtendedResponseDsml extendedResponseDsml = new ExtendedResponseDsml( extendedResponse );
                batchResponse.addResponse( extendedResponseDsml );

                break;

            case MODIFY_REQUEST:
                ModifyResponse modifyResponse = connection.modify( ( ModifyRequest ) request );
                ModifyResponseDsml modifyResponseDsml = new ModifyResponseDsml( modifyResponse );
                batchResponse.addResponse( modifyResponseDsml );

                break;

            case MODIFYDN_REQUEST:
                ModifyDnResponse modifyDnResponse = connection.modifyDn( ( ModifyDnRequest ) request );
                ModDNResponseDsml modDNResponseDsml = new ModDNResponseDsml( modifyDnResponse );
                batchResponse.addResponse( modDNResponseDsml );

                break;

            case SEARCH_REQUEST:
                Cursor<Response> searchResponses = connection.search( ( SearchRequest ) request );

                while ( searchResponses.next() )
                {
                    Response searchResponse = searchResponses.get();
                    SearchResponseDsml searchResponseDsml = null;

                    int requestID = searchResponse.getMessageId();

                    if ( searchResponse.getType() == MessageTypeEnum.SEARCH_RESULT_ENTRY )
                    {
                        SearchResultEntry searchResultEntry = ( SearchResultEntry ) searchResponse;

                        SearchResultEntryDsml searchResultEntryDsml = new SearchResultEntryDsml( searchResultEntry );
                        searchResponseDsml = new SearchResponseDsml( searchResultEntryDsml );

                        if ( requestID > 0 )
                        {
                            searchResponseDsml.setMessageId( requestID );
                        }

                        searchResponseDsml.addResponse( searchResultEntryDsml );
                    }
                    else if ( searchResponse.getType() == MessageTypeEnum.SEARCH_RESULT_REFERENCE )
                    {
                        SearchResultReference searchResultReference = ( SearchResultReference ) searchResponse;

                        SearchResultReferenceDsml searchResultReferenceDsml = new SearchResultReferenceDsml(
                            searchResultReference );
                        searchResponseDsml = new SearchResponseDsml( searchResultReferenceDsml );

                        if ( requestID > 0 )
                        {
                            searchResponseDsml.setMessageId( requestID );
                        }

                        searchResponseDsml.addResponse( searchResultReferenceDsml );
                    }

                    batchResponse.addResponse( searchResponseDsml );
                }

                break;

            case UNBIND_REQUEST:
                connection.unBind();
                break;

            default:
                throw new IllegalStateException( "Unexpected request tpye " + request.getType() );
        }

        if ( ( !continueOnError ) && ( resultCode != ResultCodeEnum.SUCCESS )
            && ( resultCode != ResultCodeEnum.COMPARE_TRUE ) && ( resultCode != ResultCodeEnum.COMPARE_FALSE )
            && ( resultCode != ResultCodeEnum.REFERRAL ) )
        {
            // Turning on Exit flag
            exit = true;
        }
    }


    /**
     * Processes the BatchRequest
     * <ul>
     *     <li>Parsing and Getting BatchRequest</li>
     *     <li>Getting and registering options from BatchRequest</li>
     * </ul>
     *     
     * @throws XmlPullParserException
     *      if an error occurs in the parser
     */
    private void processBatchRequest() throws XmlPullParserException
    {
        // Parsing BatchRequest
        parser.parseBatchRequest();

        // Getting BatchRequest
        batchRequest = parser.getBatchRequest();

        if ( OnError.RESUME.equals( batchRequest.getOnError() ) )
        {
            continueOnError = true;
        }
        else if ( OnError.EXIT.equals( batchRequest.getOnError() ) )
        {
            continueOnError = false;
        }

        if ( batchRequest.getRequestID() != 0 )
        {
            batchResponse.setRequestID( batchRequest.getRequestID() );
        }
    }


    /**
     * Binds to the ldap server
     * 
     * @param messageId the message Id
     * @throws EncoderException
     * @throws org.apache.directory.shared.asn1.DecoderException
     * @throws IOException
     * @throws LdapInvalidDnException
     */
    private void bind( int messageId ) throws LdapException, EncoderException, DecoderException, IOException
    {
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setSimple( true );
        bindRequest.setCredentials( StringTools.getBytesUtf8( password ) );
        bindRequest.setName( new DN( user ) );
        bindRequest.setVersion3( true );
        bindRequest.setMessageId( messageId );

        BindResponse bindResponse = connection.bind( bindRequest );

        if ( bindResponse.getLdapResult().getResultCode() != ResultCodeEnum.SUCCESS )
        {
            System.err.println( "Error : " + bindResponse.getLdapResult().getErrorMessage() );
        }
    }
}
