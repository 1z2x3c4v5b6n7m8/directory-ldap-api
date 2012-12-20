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
package org.apache.directory.shared.ldap.extras.extended.ads_impl.certGeneration;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.ldap.codec.api.ExtendedRequestDecorator;
import org.apache.directory.api.ldap.codec.api.ExtendedRequestFactory;
import org.apache.directory.api.ldap.codec.api.ExtendedResponseDecorator;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.ExtendedRequest;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.shared.ldap.extras.extended.CertGenerationRequestImpl;
import org.apache.directory.shared.ldap.extras.extended.CertGenerationResponseImpl;
import org.apache.directory.shared.ldap.extras.extended.CertGenerationRequest;
import org.apache.directory.shared.ldap.extras.extended.CertGenerationResponse;


/**
 * An {@link ExtendedRequestFactory} for creating cancel extended request response 
 * pairs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertGenerationFactory
    implements ExtendedRequestFactory<CertGenerationRequest, CertGenerationResponse>
{
    private LdapApiService codec;


    public CertGenerationFactory( LdapApiService codec )
    {
        this.codec = codec;
    }


    /**
     * {@inheritDoc}
     */
    public String getOid()
    {
        return CertGenerationRequest.EXTENSION_OID;
    }


    /**
     * {@inheritDoc}
     */
    public CertGenerationRequest newRequest()
    {
        return new CertGenerationRequestDecorator( codec, new CertGenerationRequestImpl() );
    }


    /**
     * {@inheritDoc}
     */
    public CertGenerationResponse newResponse( byte[] encodedValue ) throws DecoderException
    {
        CertGenerationResponseDecorator response = new CertGenerationResponseDecorator( codec,
            new CertGenerationResponseImpl() );
        response.setResponseValue( encodedValue );
        return response;
    }


    /**
     * {@inheritDoc}
     */
    public CertGenerationRequest newRequest( byte[] value )
    {
        CertGenerationRequestDecorator req = new CertGenerationRequestDecorator( codec, new CertGenerationRequestImpl() );
        req.setRequestValue( value );
        return req;
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedRequestDecorator<CertGenerationRequest, CertGenerationResponse> decorate(
        ExtendedRequest<?> modelRequest )
    {
        if ( modelRequest instanceof CertGenerationRequestDecorator )
        {
            return ( CertGenerationRequestDecorator ) modelRequest;
        }

        return new CertGenerationRequestDecorator( codec, ( CertGenerationRequest ) modelRequest );
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedResponseDecorator<CertGenerationResponse> decorate( ExtendedResponse decoratedMessage )
    {
        if ( decoratedMessage instanceof CertGenerationResponseDecorator )
        {
            return ( CertGenerationResponseDecorator ) decoratedMessage;
        }

        return new CertGenerationResponseDecorator( codec, ( CertGenerationResponse ) decoratedMessage );
    }
}
