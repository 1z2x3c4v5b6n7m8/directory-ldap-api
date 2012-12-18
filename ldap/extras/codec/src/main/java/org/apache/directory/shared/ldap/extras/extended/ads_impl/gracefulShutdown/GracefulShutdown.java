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
package org.apache.directory.shared.ldap.extras.extended.ads_impl.gracefulShutdown;


import java.nio.ByteBuffer;

import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.asn1.ber.tlv.UniversalTag;
import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.ldap.extras.extended.ads_impl.gracefulDisconnect.GracefulAction;
import org.apache.directory.shared.ldap.extras.extended.ads_impl.gracefulDisconnect.GracefulActionConstants;


/**
 * An extended operation to proceed a graceful shutdown
 * 
 * <pre>
 *   GracefulShutdown ::= SEQUENCE
 *   {
 *       timeOffline     INTEGER (0..720) DEFAULT 0,
 *       delay       [0] INTEGER (0..86400) DEFAULT 0, 
 *   }
 * </pre>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class GracefulShutdown extends GracefulAction
{
    /** Length of the sequence */
    private int gracefulSequenceLength;


    /**
     * 
     * Creates a new instance of GracefulShutdown.
     *
     */
    public GracefulShutdown()
    {
        super();
    }


    /**
     * Compute the GracefulShutdown length 
     * 0x30 L1 
     *   | 
     *   +--> [0x02 0x0(1-4) [0..720] ] 
     *   +--> [0x80 0x0(1-3) [0..86400] ] 
     *   
     * L1 will always be &lt 11.
     */
    public int computeLength()
    {
        int gracefulLength = 1 + 1;
        gracefulSequenceLength = 0;

        if ( timeOffline != 0 )
        {
            gracefulSequenceLength += 1 + 1 + BerValue.getNbBytes( timeOffline );
        }

        if ( delay != 0 )
        {
            gracefulSequenceLength += 1 + 1 + BerValue.getNbBytes( delay );
        }

        return gracefulLength + gracefulSequenceLength;
    }


    /**
     * Encodes the gracefulShutdown extended operation.
     * 
     * @return A ByteBuffer that contains the encoded PDU
     * @throws org.apache.directory.api.asn1.EncoderException If anything goes wrong.
     */
    public ByteBuffer encode() throws EncoderException
    {
        // Allocate the bytes buffer.
        ByteBuffer bb = ByteBuffer.allocate( computeLength() );

        bb.put( UniversalTag.SEQUENCE.getValue() );
        bb.put( TLV.getBytes( gracefulSequenceLength ) );

        if ( timeOffline != 0 )
        {
            BerValue.encode( bb, timeOffline );
        }

        if ( delay != 0 )
        {
            bb.put( ( byte ) GracefulActionConstants.GRACEFUL_ACTION_DELAY_TAG );
            bb.put( ( byte ) BerValue.getNbBytes( delay ) );
            bb.put( BerValue.getBytes( delay ) );
        }
        return bb;
    }


    /**
     * Return a string representation of the graceful shutdown
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append( "Graceful Shutdown extended operation" );
        sb.append( "    TimeOffline : " ).append( timeOffline ).append( '\n' );
        sb.append( "    Delay : " ).append( delay ).append( '\n' );

        return sb.toString();
    }
}
