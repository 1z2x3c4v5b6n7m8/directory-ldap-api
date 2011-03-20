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


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.model.entry.BinaryValue;
import org.apache.directory.shared.ldap.model.entry.StringValue;
import org.apache.directory.shared.ldap.model.entry.Value;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.shared.ldap.model.message.ResultCodeEnum;
import org.apache.directory.shared.ldap.model.schema.AttributeType;
import org.apache.directory.shared.ldap.model.schema.LdapComparator;
import org.apache.directory.shared.ldap.model.schema.MatchingRule;
import org.apache.directory.shared.ldap.model.schema.SchemaManager;
import org.apache.directory.shared.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Attribute Type And Value, which is the basis of all Rdn. It contains a
 * type, and a value. The type must not be case sensitive. Superfluous leading
 * and trailing spaces MUST have been trimmed before. The value MUST be in UTF8
 * format, according to RFC 2253. If the type is in OID form, then the value
 * must be a hexadecimal string prefixed by a '#' character. Otherwise, the
 * string must respect the RC 2253 grammar. No further normalization will be
 * done, because we don't have any knowledge of the Schema definition in the
 * parser.
 *
 * We will also keep a User Provided form of the atav (Attribute Type And Value),
 * called upName.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class Ava implements Externalizable, Cloneable
{
    /**
     * Declares the Serial Version Uid.
     *
     * @see <a
     *      href="http://c2.com/cgi/wiki?AlwaysDeclareSerialVersionUid">Always
     *      Declare Serial Version Uid</a>
     */
    private static final long serialVersionUID = 1L;

    /** The LoggerFactory used by this class */
    private static final Logger LOG = LoggerFactory.getLogger( Ava.class );

    /** The normalized Name type */
    private String normType;
    
    /** The attributeType if the Ava is schemaAware */
    private AttributeType attributeType;

    /** The user provided Name type */
    private String upType;

    /** The name value. It can be a String or a byte array */
    private Value<?> normValue;

    /** The name user provided value. It can be a String or a byte array */
    private Value<?> upValue;

    /** The user provided Ava */
    private String upName;

    /** the schema manager */
    private SchemaManager schemaManager;

    /**
     * Constructs an empty Ava
     */
    public Ava()
    {
        normType = null;
        upType = null;
        normValue = null;
        upValue = null;
        upName = "";
    }

    
    /**
     * Constructs an empty schema aware Ava
     */
    public Ava( SchemaManager schemaManager )
    {
        normType = null;
        upType = null;
        normValue = null;
        upValue = null;
        upName = "";
        this.schemaManager = schemaManager;
    }

    
    /**
     * Construct an Ava. The type and value are normalized :
     * <li> the type is trimmed and lowercased </li>
     * <li> the value is trimmed </li>
     * <p>
     * Note that the upValue should <b>not</b> be null or empty, or resolved
     * to an empty string after having trimmed it. 
     *
     * @param upType The User Provided type
     * @param normType The normalized type
     * @param upValue The User Provided value
     * @param normValue The normalized value
     */
    public Ava( SchemaManager schemaManager, String upType, byte[] upValue ) throws LdapInvalidDnException
    {
        if ( schemaManager != null )
        { 
            try
            {
                attributeType = schemaManager.lookupAttributeTypeRegistry( upType );
            }
            catch ( LdapException le )
            {
                String message =  I18n.err( I18n.ERR_04188 );
                LOG.error( message );
                throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, message );
            }
            
            createAva( schemaManager, upType, new BinaryValue( attributeType, upValue ) );
        }
        else
        {
            createAva( upType, new BinaryValue( upValue ) );
        }
    }

    
    /**
     * Construct an Ava. The type and value are normalized :
     * <li> the type is trimmed and lowercased </li>
     * <li> the value is trimmed </li>
     * <p>
     * Note that the upValue should <b>not</b> be null or empty, or resolved
     * to an empty string after having trimmed it. 
     *
     * @param upType The User Provided type
     * @param normType The normalized type
     * @param upValue The User Provided value
     * @param normValue The normalized value
     */
    public Ava( SchemaManager schemaManager, String upType, String upValue ) throws LdapInvalidDnException
    {
        if ( schemaManager != null )
        { 
            try
            {
                attributeType = schemaManager.lookupAttributeTypeRegistry( upType );
            }
            catch ( LdapException le )
            {
                String message =  I18n.err( I18n.ERR_04188 );
                LOG.error( message );
                throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, message );
            }
            
            createAva( schemaManager, upType, new StringValue( attributeType, upValue ) );
        }
        else
        {
            createAva( upType, new StringValue( upValue ) );
        }
    }

    
    /**
     * Construct an Ava. The type and value are normalized :
     * <li> the type is trimmed and lowercased </li>
     * <li> the value is trimmed </li>
     * <p>
     * Note that the upValue should <b>not</b> be null or empty, or resolved
     * to an empty string after having trimmed it. 
     *
     * @param upType The User Provided type
     * @param normType The normalized type
     * @param upValue The User Provided value
     * @param normValue The normalized value
     */
    private void createAva( SchemaManager schemaManager, String upType, Value<?> upValue ) throws LdapInvalidDnException
    {
        normType = attributeType.getOid();
        this.upType = upType;
            
        try
        {
            MatchingRule equalityMatchingRule = attributeType.getEquality();
            
            if ( equalityMatchingRule != null )
            {
                this.normValue = equalityMatchingRule.getNormalizer().normalize( upValue );
            }
            else
            {
                this.normValue = upValue;
            }
        }
        catch ( LdapException le )
        {
            String message =  I18n.err( I18n.ERR_04188 );
            LOG.error( message );
            throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, message );
        }

        this.upValue = upValue;
        
        upName = this.upType + '=' + ( this.upValue == null ? "" : this.upValue.getString() );
    }

    
    /**
     * Construct an Ava. The type and value are normalized :
     * <li> the type is trimmed and lowercased </li>
     * <li> the value is trimmed </li>
     * <p>
     * Note that the upValue should <b>not</b> be null or empty, or resolved
     * to an empty string after having trimmed it. 
     *
     * @param upType The User Provided type
     * @param normType The normalized type
     * @param upValue The User Provided value
     * @param normValue The normalized value
     */
    /* No qualifier */ Ava( SchemaManager schemaManager, String upType, String normType, String upValue, String normValue ) throws LdapInvalidDnException
    {
        this( schemaManager, upType, normType, new StringValue( upValue ), new StringValue( normValue ) );
    }

    
    /**
     * Construct an Ava. The type and value are normalized :
     * <li> the type is trimmed and lowercased </li>
     * <li> the value is trimmed </li>
     * <p>
     * Note that the upValue should <b>not</b> be null or empty, or resolved
     * to an empty string after having trimmed it. 
     *
     * @param upType The User Provided type
     * @param normType The normalized type
     * @param upValue The User Provided value
     * @param normValue The normalized value
     */
    /* No qualifier */ Ava( SchemaManager schemaManager, String upType, String normType, byte[] upValue, byte[] normValue ) throws LdapInvalidDnException
    {
        this( schemaManager, upType, normType, new BinaryValue( upValue ), new BinaryValue( normValue ) );
    }
    
    
    /**
     * Construct an Ava. The type and value are normalized :
     * <li> the type is trimmed and lowercased </li>
     * <li> the value is trimmed </li>
     * <p>
     * Note that the upValue should <b>not</b> be null or empty, or resolved
     * to an empty string after having trimmed it. 
     *
     * @param upType The User Provided type
     * @param normType The normalized type
     * @param upValue The User Provided value
     * @param normValue The normalized value
     */
    private void createAva( String upType, Value<?> upValue ) throws LdapInvalidDnException
    {
        String upTypeTrimmed = Strings.trim(upType);
        String normTypeTrimmed = Strings.trim(normType);
        
        if ( Strings.isEmpty(upTypeTrimmed) )
        {
            if ( Strings.isEmpty(normTypeTrimmed) )
            {
                String message =  I18n.err( I18n.ERR_04188 );
                LOG.error( message );
                throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, message );
            }
            else
            {
                // In this case, we will use the normType instead
                this.normType = Strings.lowerCaseAscii( normTypeTrimmed );
                this.upType = normType;
            }
        }
        else if ( Strings.isEmpty(normTypeTrimmed) )
        {
            // In this case, we will use the upType instead
            this.normType = Strings.lowerCaseAscii( upTypeTrimmed );
            this.upType = upType;
        }
        else
        {
            this.normType = Strings.lowerCaseAscii( normTypeTrimmed );
            this.upType = upType;
            
        }
            
        this.normValue = upValue;
        this.upValue = upValue;
        
        upName = this.upType + '=' + ( this.upValue == null ? "" : this.upValue.getString() );
    }


    /**
     * Construct an Ava. The type and value are normalized :
     * <li> the type is trimmed and lowercased </li>
     * <li> the value is trimmed </li>
     * <p>
     * Note that the upValue should <b>not</b> be null or empty, or resolved
     * to an empty string after having trimmed it. 
     *
     * @param upType The User Provided type
     * @param normType The normalized type
     * @param upValue The User Provided value
     * @param normValue The normalized value
     */
    /* No qualifier */ Ava( SchemaManager schemaManager, String upType, String normType, Value<?> upValue, Value<?> normValue ) throws LdapInvalidDnException
    {
        this.upType = upType;
        this.normType = normType;
        this.upValue = upValue;
        this.normValue = normValue;
        upName = this.upType + '=' + ( this.upValue == null ? "" : this.upValue.getString() );
        this.schemaManager = schemaManager;
        
        if ( schemaManager != null )
        {
            attributeType = schemaManager.getAttributeType( normType );
        }
    }


    /**
     * Construct an Ava. The type and value are normalized :
     * <li> the type is trimmed and lowercased </li>
     * <li> the value is trimmed </li>
     * <p>
     * Note that the upValue should <b>not</b> be null or empty, or resolved
     * to an empty string after having trimmed it. 
     *
     * @param upType The User Provided type
     * @param normType The normalized type
     * @param upValue The User Provided value
     * @param normValue The normalized value
     * @param upName The User Provided name (may be escaped)
     */
    /* No qualifier */ Ava(String upType, String normType, Value<?> upValue, Value<?> normValue, String upName)
        throws LdapInvalidDnException
    {
        String upTypeTrimmed = Strings.trim(upType);
        String normTypeTrimmed = Strings.trim(normType);

        if ( Strings.isEmpty(upTypeTrimmed) )
        {
            if ( Strings.isEmpty(normTypeTrimmed) )
            {
                String message = I18n.err( I18n.ERR_04188 );
                LOG.error( message );
                throw new LdapInvalidDnException( ResultCodeEnum.INVALID_DN_SYNTAX, message );
            }
            else
            {
                // In this case, we will use the normType instead
                this.normType = Strings.lowerCaseAscii(normTypeTrimmed);
                this.upType = normType;
            }
        }
        else if ( Strings.isEmpty(normTypeTrimmed) )
        {
            // In this case, we will use the upType instead
            this.normType = Strings.lowerCaseAscii(upTypeTrimmed);
            this.upType = upType;
        }
        else
        {
            this.normType = Strings.lowerCaseAscii(normTypeTrimmed);
            this.upType = upType;

        }

        this.normValue = normValue;
        this.upValue = upValue;

        this.upName = upName;
    }


    /**
     * Get the normalized type of a Ava
     *
     * @return The normalized type
     */
    public String getNormType()
    {
        return normType;
    }

    /**
     * Get the user provided type of a Ava
     *
     * @return The user provided type
     */
    public String getUpType()
    {
        return upType;
    }


    /**
     * Get the Value of a Ava
     *
     * @return The value
     */
    public Value<?> getNormValue()
    {
        return normValue.clone();
    }

    /**
     * Get the User Provided Value of a Ava
     *
     * @return The value
     */
    public Value<?> getUpValue()
    {
        return upValue.clone();
    }

    /**
     * Get the normalized Name of a Ava
     *
     * @return The name
     */
    public String getNormName()
    {
        return normalize();
    }


    /**
     * Get the user provided form of this attribute type and value
     *
     * @return The user provided form of this atav
     */
    public String getUpName()
    {
        return upName;
    }


    /**
     * Implements the cloning.
     *
     * @return a clone of this object
     */
    public Ava clone()
    {
        try
        {
            Ava clone = (Ava) super.clone();
            clone.upValue = upValue.clone();
            clone.normValue = normValue.clone();
            
            return clone;
        }
        catch ( CloneNotSupportedException cnse )
        {
            throw new Error( "Assertion failure" );
        }
    }


    private static final boolean[] DN_ESCAPED_CHARS = new boolean[]
        {
        true,  true,  true,  true,  true,  true,  true,  true,  // 0x00 -> 0x07
        true,  true,  true,  true,  true,  true,  true,  true,  // 0x08 -> 0x0F
        true,  true,  true,  true,  true,  true,  true,  true,  // 0x10 -> 0x17
        true,  true,  true,  true,  true,  true,  true,  true,  // 0x18 -> 0x1F
        true,  false, true,  true,  false, false, false, false, // 0x20 -> 0x27 ' ', '"', '#'
        false, false, false, true,  true,  false, false, false, // 0x28 -> 0x2F '+', ','
        false, false, false, false, false, false, false, false, // 0x30 -> 0x37 
        false, false, false, true,  true,  false, true,  false, // 0x38 -> 0x3F ';', '<', '>'
        false, false, false, false, false, false, false, false, // 0x40 -> 0x47
        false, false, false, false, false, false, false, false, // 0x48 -> 0x4F
        false, false, false, false, false, false, false, false, // 0x50 -> 0x57
        false, false, false, false, true,  false, false, false, // 0x58 -> 0x5F
        false, false, false, false, false, false, false, false, // 0x60 -> 0x67
        false, false, false, false, false, false, false, false, // 0x68 -> 0x6F
        false, false, false, false, false, false, false, false, // 0x70 -> 0x77
        false, false, false, false, false, false, false, false, // 0x78 -> 0x7F
        };
    
    
    public String normalizeValue()
    {
        // The result will be gathered in a stringBuilder
        StringBuilder sb = new StringBuilder();
        
        String normalizedValue =  normValue.getString();
        int valueLength = normalizedValue.length();

        if ( normalizedValue.length() > 0 )
        {
            char[] chars = normalizedValue.toCharArray();

            // Here, we have a char to escape. Start again the loop...
            for ( int i = 0; i < valueLength; i++ )
            {
                char c = chars[i];

                if ( ( c >= 0 ) && ( c < DN_ESCAPED_CHARS.length ) && DN_ESCAPED_CHARS[ c ] ) 
                {
                    // Some chars need to be escaped even if they are US ASCII
                    // Just prefix them with a '\'
                    // Special cases are ' ' (space), '#') which need a special
                    // treatment.
                    switch ( c )
                    {
                        case ' ' :
                            if ( ( i == 0 ) || ( i == valueLength - 1 ) )
                            {
                                sb.append( "\\ " );
                            }
                            else
                            {
                                sb.append( ' ' );
                            }
    
                            break;
                            
                        case '#' :
                            if ( i == 0 )
                            {
                                sb.append( "\\#" );
                                continue;
                            }
                            else
                            {
                                sb.append( '#' );
                            }
                        
                            break;

                        default :
                            sb.append( '\\' ).append( c );
                    }
                }
                else
                {
                    // Standard ASCII chars are just appended
                    sb.append( c );
                }
            }
        }
        
        return sb.toString();
    }
    

    /**
     * A Normalized String representation of a Ava :
     * - type is trimed and lowercased 
     * - value is trimed and lowercased, and special characters
     * are escaped if needed.
     *
     * @return A normalized string representing a Ava
     */
    public String normalize()
    {
        if ( !normValue.isBinary() )
        {
            // The result will be gathered in a stringBuilder
            StringBuilder sb = new StringBuilder();
            
            // First, store the type and the '=' char
            sb.append( normType ).append( '=' );
            
            String normalizedValue = normValue.getString();
            
            if ( normalizedValue.length() > 0 )
            {
                sb.append( normalizeValue() );
            }
            
            return sb.toString();
        }
        else
        {
            return normType + "=#"
                + Strings.dumpHexPairs( normValue .getBytes() );
        }
    }


    /**
     * Gets the hashcode of this object.
     *
     * @see java.lang.Object#hashCode()
     * @return The instance hash code
     */
    public int hashCode()
    {
        int result = 37;

        result = result*17 + ( normType != null ? normType.hashCode() : 0 );
        result = result*17 + ( normValue != null ? normValue.hashCode() : 0 );

        return result;
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
        
        if ( !( obj instanceof Ava) )
        {
            return false;
        }
        
        Ava instance = (Ava)obj;
     
        // Compare the type
        if ( normType == null )
        {
            if ( instance.normType != null )
            {
                return false;
            }
        }
        else 
        {
            if ( !normType.equals( instance.normType ) )
            {
                return false;
            }
        }
            
        // Compare the values
        if ( normValue.isNull() )
        {
            return instance.normValue.isNull();
        }
        else
        {
            if ( schemaManager != null )
            {
                MatchingRule equalityMatchingRule = attributeType.getEquality();
                
                if ( equalityMatchingRule != null )
                {
                    Object o1 = normValue.get();
                    Object o2 = instance.normValue.get();
                    LdapComparator<Object> comparator = ( LdapComparator<Object> ) equalityMatchingRule.getLdapComparator();
                    return comparator.compare( o1, o2 ) == 0;
                }
                
                return false;
            }
            else
            {
                return normValue.equals( instance.normValue );
            }
        }
    }

    
    /**
     * 
     * An Ava is composed of  a type and a value.
     * The data are stored following the structure :
     * <ul>
     *   <li>
     *     <b>upName</b> The User provided ATAV
     *   </li>
     *   <li>
     *     <b>start</b> The position of this ATAV in the Dn
     *   </li>
     *   <li>
     *     <b>length</b> The ATAV length
     *   </li>
     *   <li>
     *     <b>upType</b> The user Provided Type
     *   </li>
     *   <li>
     *     <b>normType</b> The normalized AttributeType
     *   </li>
     *   <li>
     *     <b>isHR</b> Tells if the value is a String or not
     *   </li>
     * </ul>
     * <br/>
     * if the value is a String :
     * <ul>
     *   <li>
     *     <b>upValue</b> The User Provided value
     *   </li>
     *   <li>
     *     <b>value</b> The normalized value
     *   </li>
     * </ul>
     * <br/>
     * if the value is binary :
     * <ul>
     *   <li>
     *     <b>upValueLength</b>
     *   </li>
     *   <li>
     *     <b>upValue</b> The User Provided value
     *   </li>
     *   <li>
     *     <b>valueLength</b>
     *   </li>
     *   <li>
     *     <b>value</b> The normalized value
     *   </li>
     * </ul>
     * 
     * @see Externalizable#readExternal(ObjectInput)
     */
    public void writeExternal( ObjectOutput out ) throws IOException
    {
        if ( Strings.isEmpty(upName)
            || Strings.isEmpty(upType)
            || Strings.isEmpty(normType)
            || ( upValue.isNull() )
            || ( normValue.isNull() ) )
        {
            String message = "Cannot serialize an wrong ATAV, ";
            
            if ( Strings.isEmpty(upName) )
            {
                message += "the upName should not be null or empty";
            }
            else if ( Strings.isEmpty(upType) )
            {
                message += "the upType should not be null or empty";
            }
            else if ( Strings.isEmpty(normType) )
            {
                message += "the normType should not be null or empty";
            }
            else if ( upValue.isNull() )
            {
                message += "the upValue should not be null";
            }
            else if ( normValue.isNull() )
            {
                message += "the value should not be null";
            }
                
            LOG.error( message );
            throw new IOException( message );
        }
        
        if ( upName != null )
        {
            out.writeBoolean( true );
            out.writeUTF( upName );
        }
        else
        {
            out.writeBoolean( false);
        }
        
        if ( upType != null )
        {
            out.writeBoolean( true );
            out.writeUTF( upType );
        }
        else
        {
            out.writeBoolean( false);
        }
        
        if ( normType != null )
        {
            out.writeBoolean( true );
            out.writeUTF( normType );
        }
        else
        {
            out.writeBoolean( false);
        }
        
        boolean isHR = !normValue.isBinary();
        
        out.writeBoolean( isHR );
        
        if ( isHR )
        {
            StringValue.serialize( upValue, out );
            StringValue.serialize( normValue, out );
        }
        else
        {
            BinaryValue.serialize( upValue, out );
            BinaryValue.serialize( normValue, out );
        }
    }
    
    
    /**
     * We read back the data to create a new ATAV. The structure 
     * read is exposed in the {@link Ava#writeExternal(ObjectOutput)}
     * method
     * 
     * @see Externalizable#readExternal(ObjectInput)
     */
    public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException
    {
        boolean hasUpName = in.readBoolean();
        
        if ( hasUpName )
        {
            upName = in.readUTF();
        }
        
        boolean hasUpType = in.readBoolean();

        if ( hasUpType )
        {
            upType = in.readUTF();
        }
        
        boolean hasNormType = in.readBoolean();

        if ( hasNormType )
        {
            normType = in.readUTF();
        }
        
        boolean isHR = in.readBoolean();
        
        if ( isHR )
        {
            upValue = StringValue.deserialize( schemaManager, in );
            normValue = StringValue.deserialize( schemaManager, in );
        }
        else
        {
            upValue = BinaryValue.deserialize( schemaManager, in );
            normValue = BinaryValue.deserialize( schemaManager, in );
        }
        
        if ( schemaManager != null )
        {
            attributeType = schemaManager.getAttributeType( upType );
        }
    }
    
    
    /**
     * Get the associated SchemaManager if any.
     * 
     * @return The SchemaManager
     */
    public SchemaManager getSchemaManager()
    {
        return schemaManager;
    }
    
    
    /**
     * A String representation of a Ava.
     *
     * @return A string representing a Ava
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        if ( Strings.isEmpty( normType) || Strings.isEmpty(normType.trim()) )
        {
            return "";
        }

        sb.append( upType ).append( "=" );

        if ( upValue != null )
        {
            sb.append( upValue.getString() );
        }

        return sb.toString();
    }
}
