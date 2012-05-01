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
package org.apache.directory.shared.ldap.codec.decorators;


import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.asn1.ber.tlv.TLV;
import org.apache.directory.shared.asn1.ber.tlv.BerValue;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.codec.api.LdapApiService;
import org.apache.directory.shared.ldap.codec.api.LdapConstants;
import org.apache.directory.shared.ldap.model.exception.MessageException;
import org.apache.directory.shared.ldap.model.message.BindRequest;
import org.apache.directory.shared.ldap.model.message.BindResponse;
import org.apache.directory.shared.ldap.model.message.Control;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.util.Strings;


/**
 * A decorator for the BindRequest message
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BindRequestDecorator extends SingleReplyRequestDecorator<BindRequest, BindResponse>
    implements BindRequest
{
    /** The bind request length */
    private int bindRequestLength;

    /** The SASL Mechanism length */
    private int saslMechanismLength;

    /** The SASL credentials length */
    private int saslCredentialsLength;


    /**
     * Makes a BindRequest a MessageDecorator.
     *
     * @param decoratedMessage the decorated BindRequests.
     */
    public BindRequestDecorator( LdapApiService codec, BindRequest decoratedMessage )
    {
        super( codec, decoratedMessage );
    }


    /**
     * Stores the encoded length for the BindRequest
     * @param bindRequestLength The encoded length
     */
    public void setBindRequestLength( int bindRequestLength )
    {
        this.bindRequestLength = bindRequestLength;
    }


    /**
     * @return The encoded BindRequest's length
     */
    public int getBindRequestLength()
    {
        return bindRequestLength;
    }


    /**
     * Stores the encoded length for the SaslCredentials
     * @param saslCredentialsLength The encoded length
     */
    public void setSaslCredentialsLength( int saslCredentialsLength )
    {
        this.saslCredentialsLength = saslCredentialsLength;
    }


    /**
     * @return The encoded SaslCredentials's length
     */
    public int getSaslCredentialsLength()
    {
        return saslCredentialsLength;
    }


    /**
     * Stores the encoded length for the Mechanism
     * @param saslMechanismLength The encoded length
     */
    public void setSaslMechanismLength( int saslMechanismLength )
    {
        this.saslMechanismLength = saslMechanismLength;
    }


    /**
     * @return The encoded SaslMechanism's length
     */
    public int getSaslMechanismLength()
    {
        return saslMechanismLength;
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest setMessageId( int messageId )
    {
        super.setMessageId( messageId );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest addControl( Control control ) throws MessageException
    {
        return ( BindRequest ) super.addControl( control );
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest addAllControls( Control[] controls ) throws MessageException
    {
        return ( BindRequest ) super.addAllControls( controls );
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest removeControl( Control control ) throws MessageException
    {
        return ( BindRequest ) super.removeControl( control );
    }


    //-------------------------------------------------------------------------
    // The BindRequest methods
    //-------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public boolean isSimple()
    {
        return getDecorated().isSimple();
    }


    /**
     * {@inheritDoc}
     */
    public boolean getSimple()
    {
        return getDecorated().getSimple();
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest setSimple( boolean isSimple )
    {
        getDecorated().setSimple( isSimple );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    public byte[] getCredentials()
    {
        return getDecorated().getCredentials();
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest setCredentials( String credentials )
    {
        getDecorated().setCredentials( credentials );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest setCredentials( byte[] credentials )
    {
        getDecorated().setCredentials( credentials );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    public String getName()
    {
        return getDecorated().getName();
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest setName( String name )
    {
        getDecorated().setName( name );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    public Dn getDn()
    {
        return getDecorated().getDn();
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest setDn( Dn dn )
    {
        getDecorated().setDn( dn );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isVersion3()
    {
        return getDecorated().isVersion3();
    }


    /**
     * {@inheritDoc}
     */
    public boolean getVersion3()
    {
        return getDecorated().getVersion3();
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest setVersion3( boolean isVersion3 )
    {
        getDecorated().setVersion3( isVersion3 );

        return this;
    }


    /**
     * {@inheritDoc}
     */
    public String getSaslMechanism()
    {
        return getDecorated().getSaslMechanism();
    }


    /**
     * {@inheritDoc}
     */
    public BindRequest setSaslMechanism( String saslMechanism )
    {
        getDecorated().setSaslMechanism( saslMechanism );

        return this;
    }


    //-------------------------------------------------------------------------
    // The Decorator methods
    //-------------------------------------------------------------------------
    /**
     * Compute the BindRequest length 
     * 
     * BindRequest : 
     * <pre>
     * 0x60 L1 
     *   | 
     *   +--> 0x02 0x01 (1..127) version 
     *   +--> 0x04 L2 name 
     *   +--> authentication 
     *   
     * L2 = Length(name)
     * L3/4 = Length(authentication) 
     * Length(BindRequest) = Length(0x60) + Length(L1) + L1 + Length(0x02) + 1 + 1 + 
     *      Length(0x04) + Length(L2) + L2 + Length(authentication)
     * </pre>
     */
    public int computeLength()
    {
        int bindRequestLength = 1 + 1 + 1; // Initialized with version

        Dn dn = getDn();

        if ( !Dn.isNullOrEmpty( dn ) )
        {
            // A DN has been provided

            bindRequestLength += 1 + TLV.getNbBytes( Dn.getNbBytes( dn ) )
                + Dn.getNbBytes( dn );
        }
        else
        {
            // No DN has been provided, let's use the name as a string instead

            String name = getName();

            if ( Strings.isEmpty( name ) )
            {
                name = "";
            }

            bindRequestLength += 1 + TLV.getNbBytes( name.getBytes().length )
                + name.getBytes().length;
        }

        byte[] credentials = getCredentials();

        // The authentication
        if ( isSimple() )
        {
            // Compute a SimpleBind operation
            if ( credentials != null )
            {
                bindRequestLength += 1 + TLV.getNbBytes( credentials.length ) + credentials.length;
            }
            else
            {
                bindRequestLength += 1 + 1;
            }
        }
        else
        {
            byte[] mechanismBytes = Strings.getBytesUtf8( getSaslMechanism() );
            int saslMechanismLength = 1 + TLV.getNbBytes( mechanismBytes.length ) + mechanismBytes.length;
            int saslCredentialsLength = 0;

            if ( credentials != null )
            {
                saslCredentialsLength = 1 + TLV.getNbBytes( credentials.length ) + credentials.length;
            }

            int saslLength = 1 + TLV.getNbBytes( saslMechanismLength + saslCredentialsLength ) + saslMechanismLength
                + saslCredentialsLength;

            bindRequestLength += saslLength;

            // Store the mechanism and credentials lengths
            setSaslMechanismLength( saslMechanismLength );
            setSaslCredentialsLength( saslCredentialsLength );
        }

        setBindRequestLength( bindRequestLength );

        // Return the result.
        return 1 + TLV.getNbBytes( bindRequestLength ) + bindRequestLength;
    }


    /**
     * Encode the BindRequest message to a PDU. 
     * 
     * BindRequest : 
     * <pre>
     * 0x60 LL 
     *   0x02 LL version         0x80 LL simple 
     *   0x04 LL name           /   
     *   authentication.encode() 
     *                          \ 0x83 LL mechanism [0x04 LL credential]
     * </pre>
     * 
     * @param buffer The buffer where to put the PDU
     * @return The PDU.
     */
    public ByteBuffer encode( ByteBuffer buffer ) throws EncoderException
    {
        try
        {
            // The BindRequest Tag
            buffer.put( LdapConstants.BIND_REQUEST_TAG );
            buffer.put( TLV.getBytes( getBindRequestLength() ) );

        }
        catch ( BufferOverflowException boe )
        {
            throw new EncoderException( I18n.err( I18n.ERR_04005 ) );
        }

        // The version (LDAP V3 only)
        BerValue.encode( buffer, 3 );

        Dn dn = getDn();

        if ( !Dn.isNullOrEmpty( dn ) )
        {
            // A DN has been provided

            BerValue.encode( buffer, Dn.getBytes( dn ) );
        }
        else
        {
            // No DN has been provided, let's use the name as a string instead

            String name = getName();

            if ( Strings.isEmpty( name ) )
            {
                name = "";
            }

            BerValue.encode( buffer, name.getBytes() );
        }

        byte[] credentials = getCredentials();

        // The authentication
        if ( isSimple() )
        {
            // Simple authentication
            try
            {
                // The simpleAuthentication Tag
                buffer.put( ( byte ) LdapConstants.BIND_REQUEST_SIMPLE_TAG );

                if ( credentials != null )
                {
                    buffer.put( TLV.getBytes( credentials.length ) );

                    if ( credentials.length != 0 )
                    {
                        buffer.put( credentials );
                    }
                }
                else
                {
                    buffer.put( ( byte ) 0 );
                }
            }
            catch ( BufferOverflowException boe )
            {
                String msg = I18n.err( I18n.ERR_04005 );
                throw new EncoderException( msg );
            }
        }
        else
        {
            // SASL Bind
            try
            {
                // The saslAuthentication Tag
                buffer.put( ( byte ) LdapConstants.BIND_REQUEST_SASL_TAG );

                byte[] mechanismBytes = Strings.getBytesUtf8( getSaslMechanism() );

                buffer.put( TLV
                    .getBytes( getSaslMechanismLength() + getSaslCredentialsLength() ) );

                BerValue.encode( buffer, mechanismBytes );

                if ( credentials != null )
                {
                    BerValue.encode( buffer, credentials );
                }
            }
            catch ( BufferOverflowException boe )
            {
                String msg = I18n.err( I18n.ERR_04005 );
                throw new EncoderException( msg );
            }
        }

        return buffer;
    }
}
