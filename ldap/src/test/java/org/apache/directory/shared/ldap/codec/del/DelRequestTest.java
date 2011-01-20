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
package org.apache.directory.shared.ldap.codec.del;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;
import java.util.Map;

import org.apache.directory.junit.tools.Concurrent;
import org.apache.directory.junit.tools.ConcurrentJunitRunner;
import org.apache.directory.shared.asn1.DecoderException;
import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.asn1.ber.Asn1Decoder;
import org.apache.directory.shared.asn1.ber.Asn1Container;
import org.apache.directory.shared.ldap.codec.LdapMessageContainer;
import org.apache.directory.shared.ldap.codec.ResponseCarryingException;
import org.apache.directory.shared.ldap.message.DeleteRequest;
import org.apache.directory.shared.ldap.message.DeleteRequestImpl;
import org.apache.directory.shared.ldap.message.DeleteResponseImpl;
import org.apache.directory.shared.ldap.message.LdapEncoder;
import org.apache.directory.shared.ldap.message.Message;
import org.apache.directory.shared.ldap.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.message.control.Control;
import org.apache.directory.shared.ldap.util.StringTools;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test the DelRequest codec
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrent()
public class DelRequestTest
{
    /** The encoder instance */
    LdapEncoder encoder = new LdapEncoder();


    /**
     * Test the decoding of a full DelRequest
     */
    @Test
    public void testDecodeDelRequestSuccess()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x27 );

        stream.put( new byte[]
            { 0x30,
                0x25, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., delRequest DelRequest, ...
                // DelRequest ::= [APPLICATION 10] LDAPDN;
                0x4A, 0x20, 'c', 'n', '=', 't', 'e', 's', 't', 'M', 'o', 'd', 'i', 'f', 'y', ',', 'o', 'u', '=', 'u',
                's', 'e', 'r', 's', ',', 'o', 'u', '=', 's', 'y', 's', 't', 'e', 'm' } );

        String decodedPdu = StringTools.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode a DelRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded DelRequest PDU
        DeleteRequest delRequest = ( ( LdapMessageContainer ) ldapMessageContainer ).getDeleteRequest();

        assertEquals( 1, delRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", delRequest.getName().toString() );

        // Check the length
        DeleteRequest internalDeleteRequest = new DeleteRequestImpl( delRequest.getMessageId() );
        internalDeleteRequest.setName( delRequest.getName() );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( internalDeleteRequest );

            // Check the length
            assertEquals( 0x27, bb.limit() );

            String encodedPdu = StringTools.dumpBytes( bb.array() );

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }


    /**
     * Test the decoding of a full DelRequest
     */
    @Test
    public void testDecodeDelRequestBadDN()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x27 );

        stream.put( new byte[]
            { 0x30,
                0x25, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., delRequest DelRequest, ...
                // DelRequest ::= [APPLICATION 10] LDAPDN;
                0x4A, 0x20, 'c', 'n', ':', 't', 'e', 's', 't', 'M', 'o', 'd', 'i', 'f', 'y', ',', 'o', 'u', '=', 'u',
                's', 'e', 'r', 's', ',', 'o', 'u', '=', 's', 'y', 's', 't', 'e', 'm' } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode a DelRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            assertTrue( de instanceof ResponseCarryingException );
            Message response = ( ( ResponseCarryingException ) de ).getResponse();
            assertTrue( response instanceof DeleteResponseImpl );
            assertEquals( ResultCodeEnum.INVALID_DN_SYNTAX, ( ( DeleteResponseImpl ) response ).getLdapResult()
                .getResultCode() );
            return;
        }

        fail( "We should not reach this point" );
    }


    /**
     * Test the decoding of an empty DelRequest
     */
    @Test
    public void testDecodeDelRequestEmpty()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x07 );

        stream.put( new byte[]
            { 0x30, 0x05, // LDAPMessage ::= SEQUENCE {
                0x02, 0x01, 0x01, // messageID MessageID
                // CHOICE { ..., delRequest DelRequest, ...
                // DelRequest ::= [APPLICATION 10] LDAPDN;
                0x4A, 0x00 // Empty DN
            } );

        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode a DelRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
            fail( "We should never reach this point !!!" );
        }
        catch ( DecoderException de )
        {
            assertTrue( true );
        }
    }


    /**
     * Test the decoding of a full DelRequest with controls
     */
    @Test
    public void testDecodeDelRequestSuccessWithControls()
    {
        Asn1Decoder ldapDecoder = new Asn1Decoder();

        ByteBuffer stream = ByteBuffer.allocate( 0x44 );

        stream.put( new byte[]
            {
                0x30,
                0x42, // LDAPMessage ::= SEQUENCE {
                0x02,
                0x01,
                0x01, // messageID MessageID
                // CHOICE { ..., delRequest DelRequest, ...
                // DelRequest ::= [APPLICATION 10] LDAPDN;
                0x4A, 0x20, 'c', 'n', '=', 't', 'e', 's', 't', 'M', 'o', 'd', 'i', 'f', 'y', ',', 'o', 'u', '=', 'u',
                's', 'e', 'r', 's', ',', 'o', 'u', '=', 's', 'y', 's', 't', 'e', 'm', ( byte ) 0xA0,
                0x1B, // A control
                0x30, 0x19, 0x04, 0x17, 0x32, 0x2E, 0x31, 0x36, 0x2E, 0x38, 0x34, 0x30, 0x2E, 0x31, 0x2E, 0x31, 0x31,
                0x33, 0x37, 0x33, 0x30, 0x2E, 0x33, 0x2E, 0x34, 0x2E, 0x32 } );

        String decodedPdu = StringTools.dumpBytes( stream.array() );
        stream.flip();

        // Allocate a LdapMessage Container
        Asn1Container ldapMessageContainer = new LdapMessageContainer();

        // Decode a DelRequest PDU
        try
        {
            ldapDecoder.decode( stream, ldapMessageContainer );
        }
        catch ( DecoderException de )
        {
            de.printStackTrace();
            fail( de.getMessage() );
        }

        // Check the decoded DelRequest PDU
        DeleteRequest delRequest = ( ( LdapMessageContainer ) ldapMessageContainer ).getDeleteRequest();

        assertEquals( 1, delRequest.getMessageId() );
        assertEquals( "cn=testModify,ou=users,ou=system", delRequest.getName().toString() );

        // Check the Control
        Map<String, Control> controls = delRequest.getControls();

        assertEquals( 1, controls.size() );

        Control control = controls.get( "2.16.840.1.113730.3.4.2" );
        assertEquals( "2.16.840.1.113730.3.4.2", control.getOid() );
        assertEquals( "", StringTools.dumpBytes( ( byte[] ) control.getValue() ) );

        DeleteRequest internalDeleteRequest = new DeleteRequestImpl( delRequest.getMessageId() );
        internalDeleteRequest.setName( delRequest.getName() );
        internalDeleteRequest.addControl( control );

        // Check the encoding
        try
        {
            ByteBuffer bb = encoder.encodeMessage( internalDeleteRequest );

            // Check the length
            assertEquals( 0x44, bb.limit() );

            String encodedPdu = StringTools.dumpBytes( bb.array() );

            assertEquals( encodedPdu, decodedPdu );
        }
        catch ( EncoderException ee )
        {
            ee.printStackTrace();
            fail( ee.getMessage() );
        }
    }

}
