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
package org.apache.directory.shared.ldap.schema.comparators;


import org.apache.directory.shared.ldap.exception.LdapException;
import org.apache.directory.shared.ldap.exception.LdapInvalidDnException;

import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.name.Dn;
import org.apache.directory.shared.ldap.schema.LdapComparator;
import org.apache.directory.shared.ldap.schema.SchemaManager;


/**
 * 
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class DnComparator extends LdapComparator<Object>
{
    /** A reference to the schema manager */ 
    private transient SchemaManager schemaManager;
    
    public DnComparator( String oid )
    {
        super( oid );
    }
    
    
    /**
     * {@inheritDoc}
     */
    public int compare( Object obj0, Object obj1 ) 
    {
        Dn dn0 = null;
        Dn dn1 = null;
        
        try 
        {
            dn0 = getDn( obj0 );
            dn1 = getDn( obj1 );
        }
        catch ( LdapException e )
        {
            // -- what do we do here ?
            return -1;
        }
        
        return dn0.compareTo( dn1 );
    }


    public Dn getDn( Object obj ) throws LdapInvalidDnException
    {
        Dn dn = null;
        
        if ( obj instanceof Dn)
        {
            dn = (Dn)obj;
            
            dn = ( dn.isNormalized() ? dn : Dn.normalize(dn, schemaManager.getNormalizerMapping()) );
        }
        else if ( obj instanceof String )
        {
            dn = new Dn( ( String ) obj, schemaManager );
        }
        else
        {
            throw new IllegalStateException( I18n.err( I18n.ERR_04218, (obj == null ? null : obj.getClass() ) ) );
        }
        
        return dn;
    }


    /**
     * {@inheritDoc}
     */
    public void setSchemaManager( SchemaManager schemaManager )
    {
        this.schemaManager = schemaManager;
    }
}
