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
package org.apache.directory.api.ldap.extras.extended.ads_impl.certGeneration;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.ExtendedRequestDecorator;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.extras.extended.CertGenerationRequest;
import org.apache.directory.api.ldap.extras.extended.CertGenerationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Decorator for CancelRequests.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CertGenerationRequestDecorator
    extends ExtendedRequestDecorator<CertGenerationRequest, CertGenerationResponse>
    implements CertGenerationRequest
{
    private static final Logger LOG = LoggerFactory.getLogger( CertGenerationRequestDecorator.class );

    private CertGenerationObject certGenObj;


    public CertGenerationRequestDecorator( LdapApiService codec, CertGenerationRequest decoratedMessage )
    {
        super( codec, decoratedMessage );
        certGenObj = new CertGenerationObject( decoratedMessage );
    }


    public CertGenerationObject getCertGenerationObject()
    {
        return certGenObj;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setRequestValue( byte[] requestValue )
    {
        CertGenerationDecoder decoder = new CertGenerationDecoder();

        try
        {
            certGenObj = ( CertGenerationObject ) decoder.decode( requestValue );

            if ( requestValue != null )
            {
                this.requestValue = new byte[requestValue.length];
                System.arraycopy( requestValue, 0, this.requestValue, 0, requestValue.length );
            }
            else
            {
                this.requestValue = null;
            }
        }
        catch ( DecoderException e )
        {
            LOG.error( I18n.err( I18n.ERR_04165 ), e );
            throw new RuntimeException( e );
        }
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
                requestValue = certGenObj.encode().array();
            }
            catch ( EncoderException e )
            {
                LOG.error( I18n.err( I18n.ERR_04167 ), e );
                throw new RuntimeException( e );
            }
        }

        if ( requestValue == null )
        {
            return null;
        }

        final byte[] copy = new byte[requestValue.length];
        System.arraycopy( requestValue, 0, copy, 0, requestValue.length );
        return copy;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CertGenerationResponse getResultResponse()
    {
        return getDecorated().getResultResponse();
    }


    /**
     * {@inheritDoc}
     */
    public String getTargetDN()
    {
        return getDecorated().getTargetDN();
    }


    /**
     * {@inheritDoc}
     */
    public void setTargetDN( String targetDN )
    {
        getDecorated().setTargetDN( targetDN );
    }


    /**
     * {@inheritDoc}
     */
    public String getIssuerDN()
    {
        return getDecorated().getIssuerDN();
    }


    /**
     * {@inheritDoc}
     */
    public void setIssuerDN( String issuerDN )
    {
        getDecorated().setIssuerDN( issuerDN );
    }


    /**
     * {@inheritDoc}
     */
    public String getSubjectDN()
    {
        return getDecorated().getSubjectDN();
    }


    /**
     * {@inheritDoc}
     */
    public void setSubjectDN( String subjectDN )
    {
        getDecorated().setSubjectDN( subjectDN );
    }


    /**
     * {@inheritDoc}
     */
    public String getKeyAlgorithm()
    {
        return getDecorated().getKeyAlgorithm();
    }


    /**
     * {@inheritDoc}
     */
    public void setKeyAlgorithm( String keyAlgorithm )
    {
        getDecorated().setKeyAlgorithm( keyAlgorithm );
    }
}
