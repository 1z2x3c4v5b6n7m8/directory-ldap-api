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
package org.apache.directory.shared.ldap.extras.extended.ads_impl.gracefulDisconnect;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.ldap.codec.api.ExtendedRequestFactory;
import org.apache.directory.api.ldap.codec.api.ExtendedResponseDecorator;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.codec.api.UnsolicitedResponseFactory;
import org.apache.directory.api.ldap.model.message.ExtendedResponse;
import org.apache.directory.shared.ldap.extras.extended.GracefulDisconnectResponse;
import org.apache.directory.shared.ldap.extras.extended.GracefulDisconnectResponseImpl;


/**
 * An {@link ExtendedRequestFactory} for creating cancel extended request response 
 * pairs.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class GracefulDisconnectFactory implements UnsolicitedResponseFactory<GracefulDisconnectResponse>
{
    private LdapApiService codec;


    public GracefulDisconnectFactory( LdapApiService codec )
    {
        this.codec = codec;
    }


    /**
     * {@inheritDoc}
     */
    public String getOid()
    {
        return GracefulDisconnectResponse.EXTENSION_OID;
    }


    /**
     * {@inheritDoc}
     */
    public GracefulDisconnectResponse newRequest()
    {
        return new GracefulDisconnectResponseDecorator( codec, new GracefulDisconnectResponseImpl() );
    }


    /**
     * {@inheritDoc}
     */
    public GracefulDisconnectResponse newResponse( byte[] encodedValue ) throws DecoderException
    {
        return new GracefulDisconnectResponseDecorator( codec, encodedValue );
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedResponseDecorator<GracefulDisconnectResponse> decorate( ExtendedResponse decoratedMessage )
    {
        if ( decoratedMessage instanceof GracefulDisconnectResponseDecorator )
        {
            return ( GracefulDisconnectResponseDecorator ) decoratedMessage;
        }

        return new GracefulDisconnectResponseDecorator( codec, ( GracefulDisconnectResponse ) decoratedMessage );
    }
}
