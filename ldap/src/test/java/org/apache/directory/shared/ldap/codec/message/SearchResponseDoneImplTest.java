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
package org.apache.directory.shared.ldap.codec.message;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.directory.junit.tools.Concurrent;
import org.apache.directory.junit.tools.ConcurrentJunitRunner;
import org.apache.directory.shared.ldap.codec.message.ReferralImpl;
import org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl;
import org.apache.directory.shared.ldap.exception.LdapException;
import org.apache.directory.shared.ldap.message.*;
import org.apache.directory.shared.ldap.message.control.Control;
import org.apache.directory.shared.ldap.name.DN;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * TestCases for the SearchResponseImpl class methods.
 * 
 * @author <a href="mailto:dev@directory.apache.org"> Apache Directory Project</a>
 *         $Rev: 946251 $
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrent()
public class SearchResponseDoneImplTest
{
    private static final Map<String, Control> EMPTY_CONTROL_MAP = new HashMap<String, Control>();


    /**
     * Creates and populates a SearchResponseDoneImpl stub for testing purposes.
     * 
     * @return a populated SearchResponseDoneImpl stub
     */
    private org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl createStub()
    {
        // Construct the Search response to test with results and referrals
        org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl response = new org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl( 45 );
        LdapResult result = response.getLdapResult();

        try
        {
            result.setMatchedDn( new DN( "dc=example,dc=com" ) );
        }
        catch ( LdapException ine )
        {
            // do nothing
        }

        result.setResultCode( ResultCodeEnum.SUCCESS );
        org.apache.directory.shared.ldap.codec.message.ReferralImpl refs = new org.apache.directory.shared.ldap.codec.message.ReferralImpl();
        refs.addLdapUrl( "ldap://someserver.com" );
        refs.addLdapUrl( "ldap://apache.org" );
        refs.addLdapUrl( "ldap://another.net" );
        result.setReferral( refs );
        return response;
    }


    /**
     * Tests for equality using the same object.
     */
    @Test
    public void testEqualsSameObj()
    {
        org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl resp = createStub();
        assertTrue( resp.equals( resp ) );
    }


    /**
     * Tests for equality using an exact copy.
     */
    @Test
    public void testEqualsExactCopy()
    {
        org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl resp0 = createStub();
        org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl resp1 = createStub();
        assertTrue( resp0.equals( resp1 ) );
        assertTrue( resp1.equals( resp0 ) );
    }


    /**
     * Tests for equality using different stub implementations.
     */
    @Test
    public void testEqualsDiffImpl()
    {
        org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl resp0 = createStub();
        SearchResultDone resp1 = new SearchResultDone()
        {
            public LdapResult getLdapResult()
            {
                org.apache.directory.shared.ldap.codec.message.LdapResultImpl result = new org.apache.directory.shared.ldap.codec.message.LdapResultImpl();

                try
                {
                    result.setMatchedDn( new DN( "dc=example,dc=com" ) );
                }
                catch ( Exception e )
                {
                    // Do nothing
                }
                result.setResultCode( ResultCodeEnum.SUCCESS );
                ReferralImpl refs = new org.apache.directory.shared.ldap.codec.message.ReferralImpl();
                refs.addLdapUrl( "ldap://someserver.com" );
                refs.addLdapUrl( "ldap://apache.org" );
                refs.addLdapUrl( "ldap://another.net" );
                result.setReferral( refs );

                return result;
            }


            public MessageTypeEnum getType()
            {
                return MessageTypeEnum.SEARCH_RESULT_DONE;
            }


            public Map<String, Control> getControls()
            {
                return EMPTY_CONTROL_MAP;
            }


            public void addControl( Control a_control ) throws MessageException
            {
            }


            public void removeControl( Control a_control ) throws MessageException
            {
            }


            public int getMessageId()
            {
                return 45;
            }


            public Object get( Object a_key )
            {
                return null;
            }


            public Object put( Object a_key, Object a_value )
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


            public Control getCurrentControl()
            {
                return null;
            }


            public int getControlsLength()
            {
                return 0;
            }


            public void setControlsLength( int controlsLength )
            {
            }


            public int getMessageLength()
            {
                return 0;
            }


            public void setMessageLength( int messageLength )
            {
            }


            public Control getControl( String oid )
            {
                return null;
            }


            public void setMessageId( int messageId )
            {
            }
        };

        assertTrue( resp0.equals( resp1 ) );
        assertFalse( resp1.equals( resp0 ) );
    }


    /**
     * Tests for equal hashCode using the same object.
     */
    @Test
    public void testHashCodeSameObj()
    {
        org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl resp = createStub();
        assertTrue( resp.hashCode() == resp.hashCode() );
    }


    /**
     * Tests for equal hashCode using an exact copy.
     */
    @Test
    public void testHashCodeExactCopy()
    {
        org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl resp0 = createStub();
        org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl resp1 = createStub();
        assertTrue( resp0.hashCode() == resp1.hashCode() );
    }


    /**
     * Tests inequality when messageIds are different.
     */
    @Test
    public void testNotEqualsDiffIds()
    {
        org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl resp0 = new org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl( 3 );
        SearchResultDoneImpl resp1 = new org.apache.directory.shared.ldap.codec.message.SearchResultDoneImpl( 4 );

        assertFalse( resp0.equals( resp1 ) );
        assertFalse( resp1.equals( resp0 ) );
    }
}
