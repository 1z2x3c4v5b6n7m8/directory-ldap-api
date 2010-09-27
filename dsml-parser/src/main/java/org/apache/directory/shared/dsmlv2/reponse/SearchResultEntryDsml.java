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

package org.apache.directory.shared.dsmlv2.reponse;


import org.apache.directory.shared.dsmlv2.ParserUtils;
import org.apache.directory.shared.ldap.codec.MessageTypeEnum;
import org.apache.directory.shared.ldap.entry.Entry;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.entry.Value;
import org.apache.directory.shared.ldap.exception.LdapException;
import org.apache.directory.shared.ldap.message.SearchResultEntry;
import org.apache.directory.shared.ldap.message.SearchResultEntryImpl;
import org.apache.directory.shared.ldap.name.DN;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;


/**
 * DSML Decorator for SearchResultEntry
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultEntryDsml extends AbstractResponseDsml
{
    /**
     * Creates a new instance of SearchResultEntryDsml.
     */
    public SearchResultEntryDsml()
    {
        super( new SearchResultEntryImpl() );
    }


    /**
     * Creates a new instance of SearchResultEntryDsml.
     *
     * @param ldapMessage
     *      the message to decorate
     */
    public SearchResultEntryDsml( SearchResultEntry ldapMessage )
    {
        super( ldapMessage );
    }


    /**
     * {@inheritDoc}
     */
    public MessageTypeEnum getType()
    {
        return instance.getType();
    }


    /**
     * {@inheritDoc}
     */
    public Element toDsml( Element root )
    {
        Element element = root.addElement( "searchResultEntry" );
        SearchResultEntry searchResultEntry = ( SearchResultEntry ) instance;
        element.addAttribute( "dn", searchResultEntry.getObjectName().getName() );

        Entry entry = searchResultEntry.getEntry();
        for ( EntryAttribute attribute : entry )
        {

            Element attributeElement = element.addElement( "attr" );
            attributeElement.addAttribute( "name", attribute.getId() );

            for ( Value<?> value : attribute )
            {
                if ( ParserUtils.needsBase64Encoding( value.get() ) )
                {
                    Namespace xsdNamespace = new Namespace( ParserUtils.XSD, ParserUtils.XML_SCHEMA_URI );
                    Namespace xsiNamespace = new Namespace( ParserUtils.XSI, ParserUtils.XML_SCHEMA_INSTANCE_URI );
                    attributeElement.getDocument().getRootElement().add( xsdNamespace );
                    attributeElement.getDocument().getRootElement().add( xsiNamespace );

                    Element valueElement = attributeElement.addElement( "value" ).addText(
                        ParserUtils.base64Encode( value.get() ) );
                    valueElement.addAttribute( new QName( "type", xsiNamespace ), ParserUtils.XSD + ":"
                        + ParserUtils.BASE64BINARY );
                }
                else
                {
                    attributeElement.addElement( "value" ).addText( value.getString() );
                }
            }
        }

        return element;
    }


    /**
     * Get the entry DN
     * 
     * @return Returns the objectName.
     */
    public DN getObjectName()
    {
        return ( ( SearchResultEntry ) instance ).getObjectName();
    }


    /**
     * Set the entry DN
     * 
     * @param objectName The objectName to set.
     */
    public void setObjectName( DN objectName )
    {
        ( ( SearchResultEntry ) instance ).setObjectName( objectName );
    }


    /**
     * Get the entry.
     * 
     * @return Returns the entry.
     */
    public Entry getEntry()
    {
        return ( ( SearchResultEntry ) instance ).getEntry();
    }


    /**
     * Initialize the entry.
     * 
     * @param entry the entry
     */
    public void setEntry( Entry entry )
    {
        ( ( SearchResultEntry ) instance ).setEntry( entry );
    }


    /**
     * Create a new attribute.
     * 
     * @param type The attribute's name
     * @throws LdapException if the type doesn't exist
     */
    public void addAttributeType( String type ) throws LdapException
    {
        ( ( SearchResultEntry ) instance ).addAttribute( type );
    }


    /**
     * Add a new value to the current attribute.
     * 
     * @param value the added value
     */
    public void addAttributeValue( Object value )
    {
        ( ( SearchResultEntry ) instance ).addAttributeValue( value );
    }
}
