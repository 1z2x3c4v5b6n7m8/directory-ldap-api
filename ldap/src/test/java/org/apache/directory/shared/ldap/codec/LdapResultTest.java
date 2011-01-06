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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Collection;

import org.apache.directory.junit.tools.Concurrent;
import org.apache.directory.junit.tools.ConcurrentJunitRunner;
import org.apache.directory.shared.asn1.DecoderException;
import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.asn1.ber.Asn1Decoder;
import org.apache.directory.shared.asn1.ber.Asn1Container;
import org.apache.directory.shared.ldap.message.AddResponse;
import org.apache.directory.shared.ldap.codec.message.LdapEncoder;
import org.apache.directory.shared.ldap.message.Referral;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.apache.directory.shared.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * A test for LdapResults. We will use a AddResponse message to test the
 * LdapResult part
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrent()
public class LdapResultTest
{
    /** The encoder instance */
    LdapEncoder encoder = new LdapEncoder();


    // ~ Methods
    // ------------------------------------------------------------------------------------

    /**
     * Test the decoding of a AddResponse with no LdapResult
     */
    @Test
    public void testDecodeAddResponseEmptyResultCode()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x10 );

        stream.put( new byte[]
            { 0x30, 0x0E, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                0x69, 0x02, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x00 // Empty resultCode
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode a AddResponse message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddResponse with no LdapResult
     */
    @Test
    public void testDecodeAddResponseEmptyResultCodeAbove90()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x10 );

        stream.put( new byte[]
            { 0x30, 0x0E, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                0x69, 0x02, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x03, 0x01, 0x01 // resultCode too high
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode a AddResponse message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddResponse with all the different result codes
     */
    @Test
    public void testDecodeAddResponseEmptyResultCodesOK()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0E );

        byte[] buffer = new byte[]
            { 0x30, 0x0C, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                0x69, 0x07, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x01, 0x00, // resultCode success
                0x04, 0x00, // matchedDN LDAPDN,
                0x04, 0x00 // errorMessage LDAPString,
            };

        for ( int i = 0; i < 91; i++ )
        {
            buffer[9] = ( byte ) i;
            stream.put( buffer );
            stream.flip();

            // Allocate a LdapMessage Container
            Asn1Container ldapMessageContainer = new LdapMessageContainer();

            // Decode a AddResponse PDU
            try
            {
                ldapDecoder.decode( stream, ldapMessageContainer );
            }
            catch ( DecoderException de )
            {
                fail( "We should never reach this point" );
            }

            stream.clear();
        }

        assertTrue( true );
    }


    /**
     * Test the decoding of a AddResponse with no matched DN
     */
    @Test
    public void testDecodeAddResponseEmptyResultCodeNoMatchedDN()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0A );

        stream.put( new byte[]
            { 0x30, 0x08, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                0x69, 0x03, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x01, 0x00, // resultCode success
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode a AddResponse message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddResponse with no error message
     */
    @Test
    public void testDecodeAddResponseEmptyResultCodeNoErrorMsg()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0C );

        stream.put( new byte[]
            { 0x30, 0x0A, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                0x69, 0x05, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x01, 0x00, // resultCode success
                0x04, 0x00 // Empty matched DN
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode a AddResponse message
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of a AddResponse with a valid LdapResult
     */
    @Test
    public void testDecodeAddResponseEmptyResultCodeOK()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x0E );

        stream.put( new byte[]
            { 0x30, 0x0C, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                0x69, 0x07, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x01, 0x00, // resultCode success
                0x04, 0x00, // Empty matched DN
                0x04, 0x00 // Empty errorMessage
            } );

        String decodedPdu = Strings.dumpBytes(stream.array());
        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode the AddResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded AddResponse
        AddResponse addResponse = ( ( LdapMessageContainer ) ldapMessageContainer ).getAddResponse();

        assertEquals( 1, addResponse.getMessageId() );
        assertEquals( ResultCodeEnum.SUCCESS, addResponse.getLdapResult().getResultCode() );
        assertEquals( "", addResponse.getLdapResult().getMatchedDn().getName() );
        assertEquals( "", addResponse.getLdapResult().getErrorMessage() );

        try
        {
            ByteBuffer bb = encoder.encodeMessage( addResponse );

            // Check the length
            assertEquals( 0x0E, bb.limit() );

            String encodedPdu = Strings.dumpBytes(bb.array());

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a AddResponse with a valid LdapResult with referral
     */
    @Test
    public void testDecodeAddResponseEmptyResultCodeOKReferral()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x1A );

        stream.put( new byte[]
            { 0x30, 0x18, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                0x69, 0x13, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x01, 0x0A, // resultCode success (Referral)
                0x04, 0x00, // Empty matched DN
                0x04, 0x00, // Empty errorMessage
                ( byte ) 0xA3, 0x0A, 0x04, 0x08, 'l', 'd', 'a', 'p', ':', '/', '/', '/' } );

        String decodedPdu = Strings.dumpBytes(stream.array());
        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode the AddResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded AddResponse
        AddResponse addResponse = ( ( LdapMessageContainer ) ldapMessageContainer ).getAddResponse();

        assertEquals( 1, addResponse.getMessageId() );
        assertEquals( ResultCodeEnum.REFERRAL, addResponse.getLdapResult().getResultCode() );
        assertEquals( "", addResponse.getLdapResult().getMatchedDn().getName() );
        assertEquals( "", addResponse.getLdapResult().getErrorMessage() );

        Referral referral = addResponse.getLdapResult().getReferral();

        assertNotNull( referral );
        assertEquals( 1, referral.getLdapUrls().size() );
        Collection<String> ldapUrls = referral.getLdapUrls();

        assertTrue( ldapUrls.contains( "ldap:///" ) );

        try
        {
            ByteBuffer bb = encoder.encodeMessage( addResponse );

            // Check the length
            assertEquals( 0x1A, bb.limit() );

            String encodedPdu = Strings.dumpBytes(bb.array());

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a AddResponse with a valid LdapResult with referrals
     */
    @Test
    public void testDecodeAddResponseEmptyResultCodeOKReferrals()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x24 );

        stream.put( new byte[]
            { 0x30,
                0x22, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01,
                0x01, // messageID MessageID
                0x69,
                0x1D, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x01,
                0x0A, // resultCode success (Referral)
                0x04,
                0x00, // Empty matched DN
                0x04,
                0x00, // Empty errorMessage
                ( byte ) 0xA3, 0x14, 0x04, 0x08, 'l', 'd', 'a', 'p', ':', '/', '/', '/', 0x04, 0x08, 'l', 'd', 'a',
                'p', ':', '/', '/', '/' } );

        String decodedPdu = Strings.dumpBytes(stream.array());
        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode the AddResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded AddResponse
        AddResponse addResponse = ( ( LdapMessageContainer ) ldapMessageContainer ).getAddResponse();

        assertEquals( 1, addResponse.getMessageId() );
        assertEquals( ResultCodeEnum.REFERRAL, addResponse.getLdapResult().getResultCode() );
        assertEquals( "", addResponse.getLdapResult().getMatchedDn().getName() );
        assertEquals( "", addResponse.getLdapResult().getErrorMessage() );

        Referral referral = addResponse.getLdapResult().getReferral();

        assertNotNull( referral );

        assertEquals( 2, referral.getLdapUrls().size() );

        Collection<String> ldapUrls = referral.getLdapUrls();

        for ( String ldapUrl : ldapUrls )
        {
            assertEquals( "ldap:///", ldapUrl );
        }

        try
        {
            ByteBuffer bb = encoder.encodeMessage( addResponse );

            // Check the length
            assertEquals( 0x24, bb.limit() );

            String encodedPdu = Strings.dumpBytes(bb.array());

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a AddResponse with a valid LdapResult with referrals
     * and an empty referral
     */
    @Test
    public void testDecodeAddResponseEmptyResultCodeEmptyReferral()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x1C );

        stream.put( new byte[]
            { 0x30, 0x1A, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                0x69, 0x15, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x01, 0x0A, // resultCode success (Referral)
                0x04, 0x00, // Empty matched DN
                0x04, 0x00, // Empty errorMessage
                ( byte ) 0xA3, 0x0C, 0x04, 0x08, 'l', 'd', 'a', 'p', ':', '/', '/', '/', 0x04, 0x00 } );

        String decodedPdu = Strings.dumpBytes(stream.array());
        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode the AddResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded AddResponse
        AddResponse addResponse = ( ( LdapMessageContainer ) ldapMessageContainer ).getAddResponse();

        assertEquals( 1, addResponse.getMessageId() );
        assertEquals( ResultCodeEnum.REFERRAL, addResponse.getLdapResult().getResultCode() );
        assertEquals( "", addResponse.getLdapResult().getMatchedDn().getName() );
        assertEquals( "", addResponse.getLdapResult().getErrorMessage() );

        Referral referral = addResponse.getLdapResult().getReferral();

        assertNotNull( referral );

        assertEquals( 2, referral.getLdapUrls().size() );

        Collection<String> ldapUrls = referral.getLdapUrls();

        String[] expected = new String[]
            { "ldap:///", "" };
        int i = 0;

        for ( String ldapUrl : ldapUrls )
        {
            assertEquals( expected[i], ldapUrl );
            i++;
        }

        try
        {
            ByteBuffer bb = encoder.encodeMessage( addResponse );

            // Check the length
            assertEquals( 0x1C, bb.limit() );

            String encodedPdu = Strings.dumpBytes(bb.array());

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a AddResponse with a valid LdapResult and an invalid
     * transition after the referral sequence
     */
    @Test
    public void testDecodeAddResponseEmptyResultCodeEmptyReferrals()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x10 );

        stream.put( new byte[]
            { 0x30, 0x0E, // LDAPMessage ::=SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                0x69, 0x09, // CHOICE { ..., addResponse AddResponse, ...
                0x0A, 0x01, 0x0A, // resultCode success (Referral)
                0x04, 0x00, // Empty matched DN
                0x04, 0x00, // Empty errorMessage
                ( byte ) 0xA3, 0x00, } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode the AddResponse PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
            return;
        }

        fail( "We should not reach this point" );
    }
}
