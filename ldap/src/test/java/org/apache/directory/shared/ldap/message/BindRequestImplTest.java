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
package org.apache.directory.shared.ldap.message;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.directory.junit.tools.Concurrent;
import org.apache.directory.junit.tools.ConcurrentJunitRunner;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.MessageException;
import org.apache.directory.shared.ldap.model.message.*;
import org.apache.directory.shared.ldap.model.message.Control;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * TestCases for the methods of the BindRequestImpl class.
 * 
 * @author <a href="mailto:dev@directory.apache.org"> Apache Directory Project</a>
 *         $Rev: 923524 $
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrent()
public class BindRequestImplTest
{
    private static final Map<String, Control> EMPTY_CONTROL_MAP = new HashMap<String, Control>();


    /**
     * Tests the same object referrence for equality.
     */
    @Test
    public void testEqualsSameObj()
    {
        BindRequestImpl req = new BindRequestImpl( 5 );
        assertTrue( req.equals( req ) );
    }


    /**
     * Tests for equality using exact copies.
     */
    @Test
    public void testEqualsExactCopy() throws LdapException
    {
        BindRequestImpl req0 = new BindRequestImpl( 5 );
        req0.setCredentials( "password".getBytes() );
        req0.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req0.setSimple( true );
        req0.setVersion3( true );

        BindRequestImpl req1 = new BindRequestImpl( 5 );
        req1.setCredentials( "password".getBytes() );
        req1.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req1.setSimple( true );
        req1.setVersion3( true );

        assertTrue( req0.equals( req1 ) );
    }


    /**
     * Test for inequality when only the IDs are different.
     */
    @Test
    public void testNotEqualDiffId() throws LdapException
    {
        BindRequestImpl req0 = new BindRequestImpl( 7 );
        req0.setCredentials( "password".getBytes() );
        req0.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req0.setSimple( true );
        req0.setVersion3( true );

        BindRequestImpl req1 = new BindRequestImpl( 5 );
        req1.setCredentials( "password".getBytes() );
        req1.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req1.setSimple( true );
        req1.setVersion3( true );

        assertFalse( req0.equals( req1 ) );
    }


    /**
     * Test for inequality when only the credentials are different.
     */
    @Test
    public void testNotEqualDiffCreds() throws LdapException
    {
        BindRequestImpl req0 = new BindRequestImpl( 5 );
        req0.setCredentials( "abcdefg".getBytes() );
        req0.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req0.setSimple( true );
        req0.setVersion3( true );

        BindRequestImpl req1 = new BindRequestImpl( 5 );
        req1.setCredentials( "password".getBytes() );
        req1.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req1.setSimple( true );
        req1.setVersion3( true );

        assertFalse( req0.equals( req1 ) );
    }


    /**
     * Test for inequality when only the Dn names are different.
     */
    @Test
    public void testNotEqualDiffName() throws LdapException
    {
        BindRequestImpl req0 = new BindRequestImpl( 5 );
        req0.setCredentials( "password".getBytes() );
        req0.setName( new Dn( "uid=akarasulu,dc=example,dc=com" ) );
        req0.setSimple( true );
        req0.setVersion3( true );

        BindRequestImpl req1 = new BindRequestImpl( 5 );
        req1.setCredentials( "password".getBytes() );
        req1.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req1.setSimple( true );
        req1.setVersion3( true );

        assertFalse( req0.equals( req1 ) );
    }


    /**
     * Test for inequality when only the auth mechanisms are different.
     */
    @Test
    public void testNotEqualDiffSimple() throws LdapException
    {
        BindRequestImpl req0 = new BindRequestImpl( 5 );
        req0.setCredentials( "password".getBytes() );
        req0.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req0.setSimple( false );
        req0.setVersion3( true );

        BindRequestImpl req1 = new BindRequestImpl( 5 );
        req1.setCredentials( "password".getBytes() );
        req1.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req1.setSimple( true );
        req1.setVersion3( true );

        assertFalse( req0.equals( req1 ) );
    }


    /**
     * Test for inequality when only the bind LDAP versions are different.
     */
    @Test
    public void testNotEqualDiffVersion() throws LdapException
    {
        BindRequestImpl req0 = new BindRequestImpl( 5 );
        req0.setCredentials( "password".getBytes() );
        req0.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req0.setSimple( true );
        req0.setVersion3( false );

        BindRequestImpl req1 = new BindRequestImpl( 5 );
        req1.setCredentials( "password".getBytes() );
        req1.setName( new Dn( "cn=admin,dc=example,dc=com" ) );
        req1.setSimple( true );
        req1.setVersion3( true );

        assertFalse( req0.equals( req1 ) );
    }


    /**
     * Tests for equality even when another BindRequest implementation is used.
     */
    @Test
    public void testEqualsDiffImpl()
    {
        BindRequest req0 = new BindRequest()
        {
            public boolean isSimple()
            {
                return true;
            }


            public boolean getSimple()
            {
                return true;
            }


            public void setSimple( boolean a_isSimple )
            {
            }


            public byte[] getCredentials()
            {
                return null;
            }


            public void setCredentials( String credentials )
            {
            }


            public void setCredentials( byte[] credentials )
            {
            }


            public Dn getName()
            {
                return null;
            }


            public void setName( Dn name )
            {
            }


            public boolean isVersion3()
            {
                return true;
            }


            public boolean getVersion3()
            {
                return true;
            }


            public void setVersion3( boolean a_isVersion3 )
            {
            }


            public MessageTypeEnum getResponseType()
            {
                return MessageTypeEnum.BIND_REQUEST;
            }


            public boolean hasResponse()
            {
                return true;
            }


            public MessageTypeEnum getType()
            {
                return MessageTypeEnum.BIND_REQUEST;
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


            public Object get( Object a_key )
            {
                return null;
            }


            public Object put( Object a_key, Object a_value )
            {
                return null;
            }


            public String getSaslMechanism()
            {
                return null;
            }


            public void setSaslMechanism( String saslMechanism )
            {
            }


            public ResultResponse getResultResponse()
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


            public void abandon()
            {
            }


            public void addAbandonListener( AbandonListener listener )
            {
            }


            public boolean isAbandoned()
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

        BindRequestImpl req1 = new BindRequestImpl( 5 );
        assertTrue( req1.equals( req0 ) );
    }
}
