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
package org.apache.directory.shared.ldap.model.message;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.shared.ldap.model.entry.DefaultEntryAttribute;
import org.apache.directory.shared.ldap.model.entry.DefaultModification;
import org.apache.directory.shared.ldap.model.entry.EntryAttribute;
import org.apache.directory.shared.ldap.model.entry.Modification;
import org.apache.directory.shared.ldap.model.entry.ModificationOperation;
import org.apache.directory.shared.ldap.model.name.Dn;
import org.apache.directory.shared.util.StringConstants;


/**
 * Lockable ModifyRequest implementation.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModifyRequestImpl extends AbstractAbandonableRequest implements ModifyRequest
{
    static final long serialVersionUID = -505803669028990304L;

    /** Dn of the entry to modify or PDU's <b>object</b> field */
    private Dn name;

    /** Sequence of modifications or PDU's <b>modification</b> seqence field */
    private List<Modification> mods = new ArrayList<Modification>();

    /** The associated response */
    private ModifyResponse response;

    /**
     * Creates a ModifyRequest implementing object used to modify the
     * attributes of an entry.
     */
    public ModifyRequestImpl()
    {
        super( -1, TYPE );
    }


    /**
     * Creates a ModifyRequest implementing object used to modify the
     * attributes of an entry.
     * 
     * @param id the sequential message identifier
     */
    public ModifyRequestImpl( final int id )
    {
        super( id, TYPE );
    }


    // ------------------------------------------------------------------------
    // ModifyRequest Interface Method Implementations
    // ------------------------------------------------------------------------
    /**
     * Gets an immutable Collection of modification items representing the
     * atomic changes to perform on the candidate entry to modify.
     * 
     * @return an immutable Collection of Modification instances.
     */
    public Collection<Modification> getModifications()
    {
        return Collections.unmodifiableCollection( mods );
    }


    /**
     * Gets the distinguished name of the entry to be modified by this request.
     * This property represents the PDU's <b>object</b> field.
     * 
     * @return the Dn of the modified entry.
     */
    public Dn getName()
    {
        return name;
    }


    /**
     * Sets the distinguished name of the entry to be modified by this request.
     * This property represents the PDU's <b>object</b> field.
     * 
     * @param name the Dn of the modified entry.
     */
    public void setName( Dn name )
    {
        this.name = name;
    }


    /**
     * Adds a Modification to the set of modifications composing this modify
     * request.
     * 
     * @param mod a Modification to add
     */
    public void addModification( Modification mod )
    {
        mods.add( mod );
    }


    private void addModification( ModificationOperation modOp, String attributeName, byte[]... attributeValue )
    {
        EntryAttribute attr = new DefaultEntryAttribute( attributeName, attributeValue );
        addModification( attr, modOp );
    }


    private void addModification( ModificationOperation modOp, String attributeName, String... attributeValue )
    {
        EntryAttribute attr = new DefaultEntryAttribute( attributeName, attributeValue );
        addModification( attr, modOp );
    }


    public void addModification( EntryAttribute attr, ModificationOperation modOp )
    {
        mods.add( new DefaultModification( modOp, attr ) );
    }


    /**
     *
     * marks a given attribute for addition in the target entry with the
     * given values.
     *
     * @param attributeName name of the attribute to be added
     * @param attributeValue values of the attribute
     */
    public void add( String attributeName, String... attributeValue )
    {
        addModification( ModificationOperation.ADD_ATTRIBUTE, attributeName, attributeValue );
    }


    /**
     * @see #add(String, String...)
     */
    public void add( String attributeName, byte[]... attributeValue )
    {
        addModification( ModificationOperation.ADD_ATTRIBUTE, attributeName, attributeValue );
    }


    /**
     *
     * marks a given attribute for addition in the target entry.
     *
     * @param attr the attribute to be added
     */
    public void add( EntryAttribute attr )
    {
        addModification( attr, ModificationOperation.ADD_ATTRIBUTE );
    }


    /**
     * @see #replace(String, String...)
     */
    public void replace( String attributeName )
    {
        addModification( ModificationOperation.REPLACE_ATTRIBUTE, attributeName, StringConstants.EMPTY_STRINGS );
    }


    /**
     *
     * marks a given attribute for replacement with the given
     * values in the target entry.
     *
     * @param attributeName name of the attribute to be added
     * @param attributeValue values of the attribute
     */
    public void replace( String attributeName, String... attributeValue )
    {
        addModification( ModificationOperation.REPLACE_ATTRIBUTE, attributeName, attributeValue );
    }


    /**
     * @see #replace(String, String...)
     */
    public void replace( String attributeName, byte[]... attributeValue )
    {
        addModification( ModificationOperation.REPLACE_ATTRIBUTE, attributeName, attributeValue );
    }


    /**
     *
     * marks a given attribute for replacement in the target entry.
     *
     * @param attr the attribute to be added
     */
    public void replace( EntryAttribute attr )
    {
        addModification( attr, ModificationOperation.REPLACE_ATTRIBUTE );
    }


    /**
     * Removes a Modification to the set of modifications composing this
     * modify request.
     * 
     * @param mod a Modification to remove.
     */
    public void removeModification( Modification mod )
    {
        mods.remove( mod );
    }


    /**
     * marks a given attribute for removal with the given
     * values from the target entry.
     *
     * @param attributeName name of the attribute to be added
     * @param attributeValue values of the attribute
     */
    public void remove( String attributeName, String... attributeValue )
    {
        addModification( ModificationOperation.REMOVE_ATTRIBUTE, attributeName, attributeValue );
    }


    /**
     * @see #remove(String, String...)
     */
    public void remove( String attributeName, byte[]... attributeValue )
    {
        addModification( ModificationOperation.REMOVE_ATTRIBUTE, attributeName, attributeValue );
    }


    /**
     * marks a given attribute for removal from the target entry.
     *
     * @param attr the attribute to be added
     */
    public void remove( EntryAttribute attr )
    {
        addModification( attr, ModificationOperation.REMOVE_ATTRIBUTE );
    }


    // ------------------------------------------------------------------------
    // SingleReplyRequest Interface Method Implementations
    // ------------------------------------------------------------------------

    /**
     * Gets the protocol response message type for this request which produces
     * at least one response.
     * 
     * @return the message type of the response.
     */
    public MessageTypeEnum getResponseType()
    {
        return RESP_TYPE;
    }


    /**
     * The result containing response for this request.
     * 
     * @return the result containing response for this request
     */
    public ModifyResponse getResultResponse()
    {
        if ( response == null )
        {
            response = new ModifyResponseImpl( getMessageId() );
        }

        return response;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        int hash = 37;
        if ( name != null )
        {
            hash = hash * 17 + name.hashCode();
        }
        hash = hash * 17 + mods.size();
        for ( int i = 0; i < mods.size(); i++ )
        {
            hash = hash * 17 + ( ( DefaultModification ) mods.get( i ) ).hashCode();
        }
        hash = hash * 17 + super.hashCode();

        return hash;
    }


    /**
     * Checks to see if ModifyRequest stub equals another by factoring in checks
     * for the name and modification items of the request.
     * 
     * @param obj
     *            the object to compare this ModifyRequest to
     * @return true if obj equals this ModifyRequest, false otherwise
     */
    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }

        if ( !super.equals( obj ) )
        {
            return false;
        }

        ModifyRequest req = (ModifyRequest) obj;

        if ( name != null && req.getName() == null )
        {
            return false;
        }

        if ( name == null && req.getName() != null )
        {
            return false;
        }

        if ( name != null && req.getName() != null && !name.equals( req.getName() ) )
        {
            return false;
        }

        if ( req.getModifications().size() != mods.size() )
        {
            return false;
        }

        Iterator<Modification> list = req.getModifications().iterator();

        for ( int i = 0; i < mods.size(); i++ )
        {
            Modification item = list.next();

            if ( item == null )
            {
                if ( mods.get( i ) != null )
                {
                    return false;
                }
            }
            else

            if ( !item.equals( (DefaultModification) mods.get( i ) ) )
            {
                return false;
            }
        }

        return true;
    }


    /**
     * Get a String representation of a ModifyRequest
     * 
     * @return A ModifyRequest String
     */
    public String toString()
    {

        StringBuffer sb = new StringBuffer();

        sb.append( "    Modify Request\n" );
        sb.append( "        Object : '" ).append( name ).append( "'\n" );

        if ( mods != null )
        {

            for ( int i = 0; i < mods.size(); i++ )
            {

                DefaultModification modification = ( DefaultModification ) mods.get( i );

                sb.append( "            Modification[" ).append( i ).append( "]\n" );
                sb.append( "                Operation : " );

                switch ( modification.getOperation() )
                {
                    case ADD_ATTRIBUTE:
                        sb.append( " add\n" );
                        break;

                    case REPLACE_ATTRIBUTE:
                        sb.append( " replace\n" );
                        break;

                    case REMOVE_ATTRIBUTE:
                        sb.append( " delete\n" );
                        break;
                }

                sb.append( "                Modification\n" );
                sb.append( modification.getAttribute() );
            }
        }

        // The controls
        sb.append( super.toString() );

        return super.toString( sb.toString() );
    }
}
