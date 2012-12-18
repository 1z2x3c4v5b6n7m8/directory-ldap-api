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
package org.apache.directory.shared.dsmlv2.request;


import org.apache.directory.api.i18n.I18n;
import org.apache.directory.shared.asn1.DecoderException;


/**
 * Not Filter Object to store the Not filter.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class NotFilter extends ConnectorFilter
{
    /**
     * Subclass the addFilterMethod, as this is specific for a NotFilter (we
     * cannot have more than one elements).
     * 
     * @param filter The Filter to add
     */
    public void addFilter( Filter filter ) throws DecoderException
    {
        if ( filterSet != null )
        {
            throw new DecoderException( I18n.err( I18n.ERR_04057 ) );
        }

        super.addFilter( filter );
    }


    /**
     * Get the NotFilter
     * 
     * @return Returns the notFilter.
     */
    public Filter getNotFilter()
    {
        return filterSet.get( 0 );
    }


    /**
     * Set the NotFilter
     * 
     * @param notFilter The notFilter to set.
     */
    public void setNotFilter( Filter notFilter ) throws DecoderException
    {
        if ( filterSet != null )
        {
            throw new DecoderException( I18n.err( I18n.ERR_04057 ) );
        }

        super.addFilter( notFilter );
    }


    /**
     * Return a string compliant with RFC 2254 representing a NOT filter
     * 
     * @return The NOT filter string
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append( '!' ).append( super.toString() );

        return sb.toString();
    }
}
