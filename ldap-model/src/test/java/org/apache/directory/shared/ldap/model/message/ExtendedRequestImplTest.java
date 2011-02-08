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
package org.apache.directory.shared.ldap.model.message;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;
import org.apache.directory.shared.ldap.model.exception.MessageException;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * TestCase for the ExtendedRequestImpl class.
 * 
 * @author <a href="mailto:dev@directory.apache.org"> Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class ExtendedRequestImplTest
{
    private static final Map<String, Control> EMPTY_CONTROL_MAP = new HashMap<String, Control>();


    /**
     * Tests the same object reference for equality.
     */
    @Test
    public void testEqualsSameObj()
    {
        ExtendedRequestImpl req = new ExtendedRequestImpl( 5 );
        assertTrue( req.equals( req ) );
    }


    /**
     * Tests for equality using exact copies.
     */
    @Test
    public void testEqualsExactCopy()
    {
        ExtendedRequestImpl req0 = new ExtendedRequestImpl( 5 );
        req0.setRequestName( "1.1.1.1" );
        req0.setRequestValue( "Hello World!".getBytes() );

        ExtendedRequestImpl req1 = new ExtendedRequestImpl( 5 );
        req1.setRequestName( "1.1.1.1" );
        req1.setRequestValue( "Hello World!".getBytes() );

        assertTrue( req0.equals( req1 ) );
        assertTrue( req1.equals( req0 ) );
    }


    /**
     * Tests the same object reference for equal hashCode.
     */
    @Test
    public void testHashCodeSameObj()
    {
        ExtendedRequestImpl req = new ExtendedRequestImpl( 5 );
        assertTrue( req.hashCode() == req.hashCode() );
    }


    /**
     * Tests for equal hashCode using exact copies.
     */
    @Test
    public void testHashCodeExactCopy()
    {
        ExtendedRequestImpl req0 = new ExtendedRequestImpl( 5 );
        req0.setRequestName( "1.1.1.1" );
        req0.setRequestValue( "Hello World!".getBytes() );

        ExtendedRequestImpl req1 = new ExtendedRequestImpl( 5 );
        req1.setRequestName( "1.1.1.1" );
        req1.setRequestValue( "Hello World!".getBytes() );

        assertTrue( req0.hashCode() == req1.hashCode() );
    }


    /**
     * Test for inequality when only the IDs are different.
     */
    @Test
    public void testNotEqualDiffId()
    {
        ExtendedRequestImpl req0 = new ExtendedRequestImpl( 7 );
        ExtendedRequestImpl req1 = new ExtendedRequestImpl( 5 );

        assertFalse( req0.equals( req1 ) );
        assertFalse( req1.equals( req0 ) );
    }


    /**
     * Test for inequality when only the OID is different.
     */
    @Test
    public void testNotEqualDiffOID()
    {
        ExtendedRequestImpl req0 = new ExtendedRequestImpl( 5 );
        req0.setRequestName( "1.1.1.1" );
        req0.setRequestValue( "Hello World!".getBytes() );

        ExtendedRequestImpl req1 = new ExtendedRequestImpl( 5 );
        req0.setRequestName( "1.2.2.1" );
        req0.setRequestValue( "Hello World!".getBytes() );

        assertFalse( req0.equals( req1 ) );
        assertFalse( req1.equals( req0 ) );
    }


    /**
     * Test for inequality when only the Assertion values are different.
     */
    @Test
    public void testNotEqualDiffValue()
    {
        ExtendedRequestImpl req0 = new ExtendedRequestImpl( 5 );
        req0.setRequestName( "1.1.1.1" );
        req0.setRequestValue( "Hello ".getBytes() );

        ExtendedRequestImpl req1 = new ExtendedRequestImpl( 5 );
        req0.setRequestName( "1.1.1.1" );
        req0.setRequestValue( "World!".getBytes() );

        assertFalse( req0.equals( req1 ) );
        assertFalse( req1.equals( req0 ) );
    }


    /**
     * Tests for equality even when another ExtendedRequest implementation is
     * used.
     */
    @Test
    public void testEqualsDiffImpl()
    {
        ExtendedRequest req0 = new ExtendedRequest()
        {
            private static final long serialVersionUID = 1L;


            public void setRequestName( String oid )
            {
            }


            public byte[] getRequestValue()
            {
                return null;
            }


            public void setRequestValue( byte[] payload )
            {
            }


            public MessageTypeEnum getResponseType()
            {
                return MessageTypeEnum.EXTENDED_RESPONSE;
            }


            public boolean hasResponse()
            {
                return true;
            }


            public MessageTypeEnum getType()
            {
                return MessageTypeEnum.EXTENDED_REQUEST;
            }


            public Map<String, Control> getControls()
            {
                return EMPTY_CONTROL_MAP;
            }


            public void addControl( Control control ) throws MessageException
            {
            }


            public void removeControl( Control control ) throws MessageException
            {
            }


            public int getMessageId()
            {
                return 5;
            }


            public Object get( Object key )
            {
                return null;
            }


            public Object put( Object key, Object value )
            {
                return null;
            }


            public ResultResponse getResultResponse()
            {
                return null;
            }


            public String getRequestName()
            {
                return null;
            }


            public void addAllControls( Control[] controls ) throws MessageException
            {
            }


            public boolean hasControl( String oid )
            {
                return false;
            }


            public Control getControl( String oid )
            {
                return null;
            }


            public void setMessageId( int messageId )
            {
            }
        };

        ExtendedRequestImpl req1 = new ExtendedRequestImpl( 5 );
        assertTrue( req1.equals( req0 ) );
        assertFalse( req0.equals( req1 ) );
    }
}
