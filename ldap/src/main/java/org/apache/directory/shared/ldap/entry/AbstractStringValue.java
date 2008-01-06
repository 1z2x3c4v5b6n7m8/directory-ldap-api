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
package org.apache.directory.shared.ldap.entry;

import javax.naming.NamingException;

import org.apache.directory.shared.ldap.schema.AttributeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A wrapper around an EntryAttribute's String value.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public abstract class AbstractStringValue implements Value<String>, Cloneable
{
    /** logger for reporting errors that might not be handled properly upstream */
    private static final Logger LOG = LoggerFactory.getLogger( AbstractStringValue.class );

    /** the wrapped string value */
    private String wrapped;


    // -----------------------------------------------------------------------
    // utility methods
    // -----------------------------------------------------------------------
    /**
     * Utility method to get some logs if an assert fails
     */
    protected String logAssert( String message )
    {
        LOG.error(  message );
        return message;
    }

    
    /**
     *  Check the attributeType member. It should not be null, 
     *  and it should contains a syntax.
     */
    protected String checkAttributeType( AttributeType attributeType )
    {
        try
        {
            if ( attributeType == null )
            {
                return "The AttributeType parameter should not be null";
            }
            
            if ( attributeType.getSyntax() == null )
            {
                return "There is no Syntax associated with this attributeType";
            }

            return null;
        }
        catch ( NamingException ne )
        {
            return "This AttributeType is incorrect";
        }
    }

    
    /**
     * Creates a new instance of StringValue with no value.
     */
    public AbstractStringValue()
    {
    }


    /**
     * Creates a new instance of StringValue with a value.
     *
     * @param wrapped the actual String value to wrap
     */
    public AbstractStringValue( String wrapped )
    {
        this.wrapped = wrapped;
    }


    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return "'" + wrapped + "'";
    }


    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        if ( wrapped != null )
        {
            return wrapped.hashCode();
        }

        return super.hashCode();
    }


    /**
     * @see Object#equals(Object)
     */
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }

        if ( obj == null )
        {
            return false;
        }

        if ( ! ( obj instanceof AbstractStringValue ) )
        {
            return false;
        }

        AbstractStringValue stringValue = ( AbstractStringValue ) obj;
        
        if ( this.wrapped == null ) 
        {
            if ( stringValue.wrapped == null )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        else if ( stringValue.wrapped == null )
        {
            return false;
        }

        return this.wrapped.equals( stringValue.wrapped );
    }


    public String get()
    {
        return wrapped;
    }


    public boolean isNull()
    {
        return wrapped == null;
    }


    public void set( String wrapped )
    {
        this.wrapped = wrapped;
    }


//    public int compareTo( Value<String> value )
//    {
//        if ( value == null && wrapped == null )
//        {
//            return 0;
//        }
//
//        if ( value != null && wrapped == null )
//        {
//            if ( value.get() == null )
//            {
//                return 0;
//            }
//            return -1;
//        }
//
//        if ( value == null || value.get() == null )
//        {
//            return 1;
//        }
//
//        return wrapped.compareTo( value.get() );
//    }
}