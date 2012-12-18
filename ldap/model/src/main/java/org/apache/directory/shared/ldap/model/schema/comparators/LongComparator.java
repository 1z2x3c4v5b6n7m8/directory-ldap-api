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


import java.io.Serializable;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.shared.ldap.model.schema.LdapComparator;


/**
 * Compares Long keys and values within a table.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LongComparator extends LdapComparator<Long> implements Serializable
{
    /** The serial version UID */
    private static final long serialVersionUID = 2L;


    /**
     * The LongComparator constructor. Its OID is the IntegerOrderingMatch matching
     * rule OID.
     */
    public LongComparator( String oid )
    {
        super( oid );
    }


    /**
     * Compare two objects.
     * 
     * @param obj1 First object
     * @param obj2 Second object
     * @return 1 if obj1 > obj2, 0 if obj1 == obj2, -1 if obj1 < obj2
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RC_REF_COMPARISON",
        justification = "false positive")
    public int compare( Long obj1, Long obj2 )
    {
        if ( obj1 == obj2 )
        {
            return 0;
        }

        if ( obj1 == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_04219_ARGUMENT1_NULL ) );
        }

        if ( obj2 == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_04220_ARGUMENT2_NULL ) );
        }

        return obj1.compareTo( obj2 );
    }
}
