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
package org.apache.directory.shared.ldap.model.schema.comparators;


import org.apache.directory.shared.asn1.util.Oid;
import org.apache.directory.shared.ldap.model.schema.LdapComparator;
import org.apache.directory.shared.util.Chars;
import org.apache.directory.shared.util.Strings;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A comparator for Comparators. We compare the OIDs
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Component
@Provides
public class ObjectIdentifierFirstComponentComparator extends LdapComparator<String>
{
    /**
     * Property to specify factory type.
     * TODO:This is temporary. Will be vanished after introducing custom annotations
     */
    @Property(name = "ads.comp.type", value = "comparator")
    public String compType;

    /** The serial version UID */
    private static final long serialVersionUID = 2L;

    /** A logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger( ObjectIdentifierFirstComponentComparator.class );


    /**
     * The ObjectIdentifierFirstComponentComparator constructor. Its OID is the 
     * ObjectIdentifierFirstComponentMatch matching rule OID.
     */
    public ObjectIdentifierFirstComponentComparator( @Property(name = "ads.comp.comparator.oid") String oid )
    {
        super( oid );
    }


    /**
     * Get the OID from the SchemaObject description
     */
    private String getNumericOid( String s )
    {
        // Get the OID from the strings now
        int pos = 0;

        if ( !Strings.isCharASCII( s, pos++, '(' ) )
        {
            return null;
        }

        while ( Strings.isCharASCII( s, pos, ' ' ) )
        {
            pos++;
        }

        int start = pos;

        while ( Chars.isDigit( s, pos ) || Strings.isCharASCII( s, pos, '.' ) )
        {
            pos++;
        }

        String numericOid = s.substring( start, pos );

        if ( Oid.isOid( numericOid ) )
        {
            return numericOid;
        }
        else
        {
            return null;
        }
    }


    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare( String s1, String s2 )
    {
        LOG.debug( "comparing ObjectIdentifierFirstComponent objects '{}' with '{}'", s1, s2 );

        // -------------------------------------------------------------------
        // Handle some basis cases
        // -------------------------------------------------------------------
        if ( s1 == null )
        {
            return ( s2 == null ) ? 0 : -1;
        }

        if ( s2 == null )
        {
            return -1;
        }

        // Let's try to avoid a parse.
        if ( s1.equals( s2 ) )
        {
            return 0;
        }

        // Get the OID from the strings now
        String oid1 = getNumericOid( s1 );

        if ( oid1 == null )
        {
            return -1;
        }

        String oid2 = getNumericOid( s2 );

        if ( oid2 == null )
        {
            return -1;
        }

        if ( oid1.equals( oid2 ) )
        {
            return 0;
        }
        else
        {
            return -1;
        }
    }
}
