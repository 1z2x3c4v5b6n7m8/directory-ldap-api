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
package org.apache.directory.shared.ldap.filter;


import org.apache.directory.shared.ldap.model.entry.Value;
import org.apache.directory.shared.ldap.schema.AttributeType;


/**
 * A assertion value node for LessOrEqual.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class LessEqNode<T> extends SimpleNode<T>
{
    /**
     * Creates a new LessEqNode object.
     * 
     * @param attributeType the attributeType
     * @param value the value to test for
     */
    public LessEqNode( AttributeType attributeType, Value<T> value )
    {
        super( attributeType, value, AssertionType.LESSEQ );
    }

    
    /**
     * Creates a new LessEqNode object.
     * 
     * @param attribute the attribute name
     * @param value the value to test for
     */
    public LessEqNode( String attribute, Value<T> value )
    {
        super( attribute, value, AssertionType.LESSEQ );
    }

    
    /**
     * @see Object#toString()
     * @return A string representing the AndNode
     */
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append( '(' );
        
        if ( attributeType != null )
        {
            buf.append( attributeType.getName() );
        }
        else
        {
            buf.append( attribute );
        }
        
        buf.append( "<=" );

        Value<?> escapedValue = getEscapedValue();
        if ( !escapedValue.isNull())
        {
            buf.append( escapedValue );
        }

        buf.append( super.toString() );

        buf.append( ')' );

        return buf.toString();
    }
}
