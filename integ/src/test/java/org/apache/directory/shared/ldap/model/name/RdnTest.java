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
package org.apache.directory.shared.ldap.model.name;

import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.schema.SchemaManager;
import org.apache.directory.shared.ldap.schemamanager.impl.DefaultSchemaManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;

/**
 * Tests for the schemaAware Rdn class
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class RdnTest
{
    private static SchemaManager schemaManager;


    @BeforeClass
    public static void setup() throws Exception
    {
        schemaManager = new DefaultSchemaManager();
    }

    @Test
    public void testRdnValuesNoSchema() throws LdapException
    {
        String errors = null;
        
        Rdn rdn = new Rdn( "OU = Exemple \\+ Rdn\\C3\\A4\\ +cn= TEST" );
        
        if ( !"OU = Exemple \\+ Rdn\\C3\\A4\\ +cn= TEST".equals( rdn.getName() ) )
        {
            errors += "\nRdn.getName fails '" + rdn.getName() + "'";
        }
        
        if ( !"ou=Exemple \\+ Rdn\\C3\\A4\\ +cn=TEST" .equals( rdn.getNormName() ) )
        {
            errors = "\nRdn.getNormName fails '" + rdn.getNormName() + "'";
        }
        
        if ( !"ou".equals( rdn.getNormType() ) )
        {
            errors += "\nRdn.getNormType fails '" + rdn.getNormType() + "'";
        }
        
        if ( !"Exemple + Rdn\u00E4 ".equals( rdn.getNormValue().getString() ) )
        {
            errors += "\nRdn.getNormValue fails '" + rdn.getNormValue().getString() + "'";
        }
        
        if ( !"OU".equals( rdn.getType() ) )
        {
            errors += "\nRdn.getUpType fails '" + rdn.getType() + "'";
        }
        
        if ( !"Exemple + Rdn\u00E4".equals( rdn.getValue().getString() ) )
        {
            errors += "\nRdn.getUpValue fails '" + rdn.getValue() + "'";
        }
        
        if ( !"Exemple + Rdn\u00E4 ".equals( rdn.getValue( "ou" ) ) )
        {
            errors += "\nRdn.getValue( 'ou' ) fails '" + rdn.getValue( "ou" ) + "'";
        }
        
        if ( !"TEST".equals( rdn.getValue( "cn" ) ) )
        {
            errors += "\nRdn.getValue( 'test' ) fails '" + rdn.getValue( "cn" ) + "'";
        }
        
        if ( !"OU = Exemple \\+ Rdn\\C3\\A4\\ +cn= TEST".equals( rdn.toString() ) )
        {
            errors += "\nRdn.toString fails '" + rdn.toString() + "'";
        }
        
        assertEquals( null, errors );
    }
    
    
    @Test
    public void testRdnValuesSchemaAware() throws LdapException
    {
        String errors = null;
        
        Rdn rdn = new Rdn( schemaManager, "OU = Exemple \\+ Rdn\\C3\\A4\\ +cn= TEST" );
        
        if ( !"OU = Exemple \\+ Rdn\\C3\\A4\\ +cn= TEST".equals( rdn.getName() ) )
        {
            errors += "\nRdn.getName fails '" + rdn.getName() + "'";
        }
        
        if ( !"2.5.4.11=exemple \\+ rdn\\C3\\A4+2.5.4.3=test" .equals( rdn.getNormName() ) )
        {
            errors = "\nRdn.getNormName fails '" + rdn.getNormName() + "'";
        }
        
        if ( !"2.5.4.11".equals( rdn.getNormType() ) )
        {
            errors += "\nRdn.getNormType fails '" + rdn.getNormType() + "'";
        }
        
        if ( !"exemple + rdn\u00E4".equals( rdn.getNormValue().getString() ) )
        {
            errors += "\nRdn.getNormValue fails '" + rdn.getNormValue().getString() + "'";
        }
        
        if ( !"OU".equals( rdn.getType() ) )
        {
            errors += "\nRdn.getUpType fails '" + rdn.getType() + "'";
        }
        
        if ( !"Exemple + Rdn\u00E4".equals( rdn.getValue().getString() ) )
        {
            errors += "\nRdn.getUpValue fails '" + rdn.getValue().getString() + "'";
        }
        
        if ( !"exemple + rdn\u00E4".equals( rdn.getValue( "ou" ) ) )
        {
            errors += "\nRdn.getValue( 'ou' ) fails '" + (String)rdn.getValue( "ou" ) + "'";
        }
        
        if ( !"test".equals( rdn.getValue( "cn" ) ) )
        {
            errors += "\nRdn.getValue( 'cn' ) fails '" + (String)rdn.getValue( "cn" ) + "'";
        }
        
        if ( !"OU = Exemple \\+ Rdn\\C3\\A4\\ +cn= TEST".equals( rdn.toString() ) )
        {
            errors += "\nRdn.toString fails '" + rdn.toString() + "'";
        }
        
        assertEquals( null, errors );
    }
}
