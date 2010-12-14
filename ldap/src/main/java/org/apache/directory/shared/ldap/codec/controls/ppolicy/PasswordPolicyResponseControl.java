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

package org.apache.directory.shared.ldap.codec.controls.ppolicy;


import java.nio.ByteBuffer;

import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.asn1.ber.tlv.TLV;
import org.apache.directory.shared.asn1.ber.tlv.UniversalTag;
import org.apache.directory.shared.asn1.ber.tlv.Value;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.codec.controls.AbstractControl;


/**
 * PasswordPolicyResponseControl.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PasswordPolicyResponseControl extends AbstractControl
{

    /** time before expiration of the password */
    private int timeBeforeExpiration = -1;

    /** number of remaining grace authentications */
    private int graceAuthNsRemaining = -1;

    /** number representing the password policy error */
    private PasswordPolicyErrorEnum ppolicyError;

    // Storage for computed lengths
    private transient int valueLength = 0;
    private transient int ppolicySeqLength = 0;
    private transient int warningLength = 0;
    private transient int timeBeforeExpirationTagLength;
    private transient int graceAuthNsRemainingTagLength;
    
    public PasswordPolicyResponseControl()
    {
        super( PasswordPolicyRequestControl.CONTROL_OID );
    }


    @Override
    public int computeLength()
    {
        if ( timeBeforeExpiration >= 0 )
        {
            timeBeforeExpirationTagLength = TLV.getNbBytes( timeBeforeExpiration );
            warningLength = 1 + TLV.getNbBytes( timeBeforeExpirationTagLength ) + timeBeforeExpirationTagLength;
        }
        else if ( graceAuthNsRemaining >= 0 )
        {
            graceAuthNsRemainingTagLength = TLV.getNbBytes( graceAuthNsRemaining );
            warningLength = 1 + TLV.getNbBytes( graceAuthNsRemainingTagLength ) + graceAuthNsRemainingTagLength;
        }

        if ( warningLength != 0 )
        {
            ppolicySeqLength = 1 + TLV.getNbBytes( warningLength ) + warningLength;
        }

        if ( ppolicyError != null )
        {
            ppolicySeqLength += 1 + 1 + 1;
        }
        
        if ( ppolicySeqLength > 0 )
        {
            valueLength = 1 + TLV.getNbBytes( ppolicySeqLength ) + ppolicySeqLength;
        }

        return super.computeLength( valueLength );
    }


    @Override
    public ByteBuffer encode( ByteBuffer buffer ) throws EncoderException
    {
        if ( buffer == null )
        {
            throw new EncoderException( I18n.err( I18n.ERR_04023 ) );
        }

        // Encode the Control envelop
        super.encode( buffer );

        if ( valueLength > 0 )
        {
            // Encode the OCTET_STRING tag
            buffer.put( UniversalTag.OCTET_STRING.getValue() );
            buffer.put( TLV.getBytes( valueLength ) );
        }

        if ( ( timeBeforeExpiration < 0 ) && ( graceAuthNsRemaining < 0 ) && ( ppolicyError == null ) )
        {
            return buffer;
        }
        else
        {
            // Encode the Sequence tag
            buffer.put( UniversalTag.SEQUENCE.getValue() );
            buffer.put( TLV.getBytes( ppolicySeqLength ) );

            if ( warningLength > 0 )
            {
                // Encode the Warning tag
                buffer.put( (byte)PasswordPolicyResponseControlTags.PPOLICY_WARNING_TAG.getValue() );
                buffer.put( TLV.getBytes( warningLength ) );

                if ( timeBeforeExpiration >= 0 )
                {
                    buffer.put( ( byte ) PasswordPolicyResponseControlTags.TIME_BEFORE_EXPIRATION_TAG.getValue() );
                    buffer.put( TLV.getBytes( timeBeforeExpirationTagLength ) );
                    buffer.put( Value.getBytes( timeBeforeExpiration ) );
                }
                else if ( graceAuthNsRemaining >= 0 )
                {
                    buffer.put( ( byte ) PasswordPolicyResponseControlTags.GRACE_AUTHNS_REMAINING_TAG.getValue() );
                    buffer.put( TLV.getBytes( graceAuthNsRemainingTagLength ) );
                    buffer.put( Value.getBytes( graceAuthNsRemaining ) );
                }
            }
    
            if ( ppolicyError != null )
            {
                buffer.put( (byte)PasswordPolicyResponseControlTags.PPOLICY_ERROR_TAG.getValue() );
                buffer.put( ( byte ) 0x01 );
                buffer.put( Value.getBytes( ppolicyError.getValue() ) );
            }
        }

        return buffer;
    }


    public int getTimeBeforeExpiration()
    {
        return timeBeforeExpiration;
    }


    public void setTimeBeforeExpiration( int timeBeforeExpiration )
    {
        this.timeBeforeExpiration = timeBeforeExpiration;
    }


    public int getGraceAuthNsRemaining()
    {
        return graceAuthNsRemaining;
    }


    public void setGraceAuthNsRemaining( int graceAuthNsRemaining )
    {
        this.graceAuthNsRemaining = graceAuthNsRemaining;
    }


    public PasswordPolicyErrorEnum getPasswordPolicyError()
    {
        return ppolicyError;
    }


    public void setPasswordPolicyError( PasswordPolicyErrorEnum ppolicyError )
    {
        this.ppolicyError = ppolicyError;
    }


    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "  PasswordPolicyResponse control :\n" );
        sb.append( "   oid          : '" ).append( getOid() ).append( '\n' );
        
        if ( timeBeforeExpiration >= 0 )
        {
            sb.append( "   timeBeforeExpiration          : '" ).append( timeBeforeExpiration ).append( '\n' );
        }
        else if ( graceAuthNsRemaining >= 0 )
        {
            sb.append( "   graceAuthNsRemaining          : '" ).append( graceAuthNsRemaining ).append( '\n' );
        }

        if ( ppolicyError != null )
        {
            sb.append( "   ppolicyError          : '" ).append( ppolicyError.toString() ).append( '\n' );
        }

        return sb.toString();
    }

}
