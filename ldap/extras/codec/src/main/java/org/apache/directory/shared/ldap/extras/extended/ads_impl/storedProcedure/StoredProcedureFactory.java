/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.shared.ldap.extras.extended.ads_impl.storedProcedure;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.shared.ldap.codec.api.ExtendedRequestDecorator;
import org.apache.directory.shared.ldap.codec.api.ExtendedRequestFactory;
import org.apache.directory.shared.ldap.codec.api.ExtendedResponseDecorator;
import org.apache.directory.shared.ldap.codec.api.LdapApiService;
import org.apache.directory.shared.ldap.extras.extended.StoredProcedureRequest;
import org.apache.directory.shared.ldap.extras.extended.StoredProcedureResponse;
import org.apache.directory.shared.ldap.extras.extended.StoredProcedureResponseImpl;
import org.apache.directory.shared.ldap.model.message.ExtendedRequest;
import org.apache.directory.shared.ldap.model.message.ExtendedResponse;


/**
 * An {@link ExtendedRequestFactory} for creating cancel extended request response 
 * pairs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoredProcedureFactory implements ExtendedRequestFactory<StoredProcedureRequest, StoredProcedureResponse>
{
    private LdapApiService codec;


    /**
     * Creates a new instance of StoredProcedureFactory.
     *
     * @param codec
     */
    public StoredProcedureFactory( LdapApiService codec )
    {
        this.codec = codec;
    }


    /**
     * {@inheritDoc}
     */
    public String getOid()
    {
        return StoredProcedureRequest.EXTENSION_OID;
    }


    /**
     * {@inheritDoc}
     */
    public StoredProcedureRequest newRequest()
    {
        return new StoredProcedureRequestDecorator( codec );
    }


    /**
     * {@inheritDoc}
     */
    public StoredProcedureResponse newResponse( byte[] encodedValue ) throws DecoderException
    {
        StoredProcedureResponseDecorator response = new StoredProcedureResponseDecorator( codec,
            new StoredProcedureResponseImpl() );
        response.setResponseValue( encodedValue );
        return response;
    }


    /**
     * {@inheritDoc}
     */
    public StoredProcedureRequest newRequest( byte[] value )
    {
        StoredProcedureRequestDecorator req = new StoredProcedureRequestDecorator( codec );

        if ( value != null )
        {
            req.setRequestValue( value );
        }
        return req;
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedRequestDecorator<StoredProcedureRequest, StoredProcedureResponse> decorate(
        ExtendedRequest<?> modelRequest )
    {
        if ( modelRequest instanceof StoredProcedureRequestDecorator )
        {
            return ( StoredProcedureRequestDecorator ) modelRequest;
        }

        return new StoredProcedureRequestDecorator( codec, ( StoredProcedureRequest ) modelRequest );
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedResponseDecorator<StoredProcedureResponse> decorate( ExtendedResponse decoratedMessage )
    {
        if ( decoratedMessage instanceof StoredProcedureResponseDecorator )
        {
            return ( StoredProcedureResponseDecorator ) decoratedMessage;
        }

        return new StoredProcedureResponseDecorator( codec, ( StoredProcedureResponse ) decoratedMessage );
    }
}
