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
package org.apache.directory.api.ldap.extras.extended.ads_impl.cancel;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.ExtendedRequestDecorator;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.extras.extended.cancel.CancelRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Decorator for CancelRequests.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CancelRequestDecorator extends ExtendedRequestDecorator<CancelRequest> implements
    CancelRequest
{
    private static final Logger LOG = LoggerFactory.getLogger( CancelRequestDecorator.class );


    public CancelRequestDecorator( LdapApiService codec, CancelRequest decoratedMessage )
    {
        super( codec, decoratedMessage );
    }


    public int getCancelId()
    {
        return getDecorated().getCancelId();
    }


    public void setCancelId( int cancelId )
    {
        if ( cancelId == getCancelId() )
        {
            return;
        }

        this.requestValue = null;
        getDecorated().setCancelId( cancelId );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getRequestValue()
    {
        if ( requestValue == null )
        {
            try
            {
                Cancel cancel = new Cancel();
                cancel.setCancelId( getDecorated().getCancelId() );

                requestValue = cancel.encode().array();
            }
            catch ( EncoderException e )
            {
                LOG.error( I18n.err( I18n.ERR_04164 ), e );
                throw new RuntimeException( e );
            }
        }

        return requestValue;
    }


    /**
     * Sets the extended request's <b>requestValue</b> portion of the PDU.
     *
     * @param payload byte array of data encapsulating ext. req. parameters
     */
    @Override
    public void setRequestValue( byte[] requestValue )
    {
        CancelDecoder decoder = new CancelDecoder();

        try
        {
            Cancel cancel = ( Cancel ) decoder.decode( requestValue );

            if ( requestValue != null )
            {
                this.requestValue = new byte[requestValue.length];
                System.arraycopy( requestValue, 0, this.requestValue, 0, requestValue.length );
            }
            else
            {
                this.requestValue = null;
            }

            getDecorated().setCancelId( cancel.getCancelId() );
        }
        catch ( DecoderException e )
        {
            LOG.error( I18n.err( I18n.ERR_04165 ), e );
            throw new RuntimeException( e );
        }
    }
}
