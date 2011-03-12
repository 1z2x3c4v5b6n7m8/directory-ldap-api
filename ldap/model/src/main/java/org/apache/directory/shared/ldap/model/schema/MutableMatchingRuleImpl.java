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
package org.apache.directory.shared.ldap.model.schema;


import java.util.List;

import javax.naming.NamingException;

import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.model.exception.LdapException;
import org.apache.directory.shared.ldap.model.exception.LdapSchemaException;
import org.apache.directory.shared.ldap.model.exception.LdapSchemaExceptionCodes;
import org.apache.directory.shared.ldap.model.schema.comparators.ComparableComparator;
import org.apache.directory.shared.ldap.model.schema.normalizers.NoOpNormalizer;
import org.apache.directory.shared.ldap.model.schema.registries.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A matchingRule definition. MatchingRules associate a comparator and a
 * normalizer, forming the basic tools necessary to assert actions against
 * attribute values. MatchingRules are associated with a specific Syntax for the
 * purpose of resolving a normalized form and for comparisons.
 * <p>
 * According to ldapbis [MODELS]:
 * </p>
 * 
 * <pre>
 *  4.1.3. Matching Rules
 *  
 *    Matching rules are used by servers to compare attribute values against
 *    assertion values when performing Search and Compare operations.  They
 *    are also used to identify the value to be added or deleted when
 *    modifying entries, and are used when comparing a purported
 *    distinguished name with the name of an entry.
 *  
 *    A matching rule specifies the syntax of the assertion value.
 * 
 *    Each matching rule is identified by an object identifier (OID) and,
 *    optionally, one or more short names (descriptors).
 * 
 *    Matching rule definitions are written according to the ABNF:
 * 
 *      MatchingRuleDescription = LPAREN WSP
 *          numericoid                ; object identifier
 *          [ SP &quot;NAME&quot; SP qdescrs ]  ; short names (descriptors)
 *          [ SP &quot;DESC&quot; SP qdstring ] ; description
 *          [ SP &quot;OBSOLETE&quot; ]         ; not active
 *          SP &quot;SYNTAX&quot; SP numericoid ; assertion syntax
 *          extensions WSP RPAREN     ; extensions
 * 
 *    where:
 *      [numericoid] is object identifier assigned to this matching rule;
 *      NAME [qdescrs] are short names (descriptors) identifying this
 *          matching rule;
 *      DESC [qdstring] is a short descriptive string;
 *      OBSOLETE indicates this matching rule is not active;
 *      SYNTAX identifies the assertion syntax by object identifier; and
 *      [extensions] describe extensions.
 * </pre>
 * 
 * @see <a href="http://www.faqs.org/rfcs/rfc2252.html">RFC 2252 Section 4.5</a>
 * @see <a
 *      href="http://www.ietf.org/internet-drafts/draft-ietf-ldapbis-models-11.txt">ldapbis
 *      [MODELS]</a>
 * @see DescriptionUtils#getDescription(MutableMatchingRuleImpl)
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class MutableMatchingRuleImpl extends AbstractMutableSchemaObject implements MatchingRule
{
    private static final long serialVersionUID = 1L;

    /** A logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger( MutableMatchingRuleImpl.class );

    /** The associated Comparator */
    protected AbstractLdapComparator<? super Object> ldapComparator;

    /** The associated Normalizer */
    protected MutableNormalizer normalizer;

    /** The associated LdapSyntax */
    protected MutableLdapSyntax ldapSyntax;

    /** The associated LdapSyntax OID */
    private String ldapSyntaxOid;


    /**
     * Creates a new instance of MatchingRule.
     *
     * @param oid The MatchingRule OID
     */
    public MutableMatchingRuleImpl( String oid )
    {
        super( SchemaObjectType.MATCHING_RULE, oid );
    }


    /**
     * Inject the MatchingRule into the registries, updating the references to
     * other SchemaObject
     *
     * @param registries The Registries
     * @exception If the addition failed
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addToRegistries( List<Throwable> errors, Registries registries ) throws LdapException
    {
        if ( registries != null )
        {
            try
            {
                // Gets the associated Comparator 
                ldapComparator = ( AbstractLdapComparator<? super Object> ) registries.getComparatorRegistry().lookup( oid );
            }
            catch ( LdapException ne )
            {
                // Default to a catch all comparator
                ldapComparator = new ComparableComparator( oid );
            }

            try
            {
                // Gets the associated Normalizer
                normalizer = registries.getNormalizerRegistry().lookup( oid );
            }
            catch ( LdapException ne )
            {
                // Default to the NoOp normalizer
                normalizer = new NoOpNormalizer( oid );
            }

            try
            {
                // Get the associated LdapSyntax
                ldapSyntax = registries.getLdapSyntaxRegistry().lookup( ldapSyntaxOid );
            }
            catch ( LdapException ne )
            {
                // The Syntax is a mandatory element, it must exist.
                String msg = I18n.err( I18n.ERR_04317 );

                LdapSchemaException ldapSchemaException = new LdapSchemaException(
                    LdapSchemaExceptionCodes.MR_NONEXISTENT_SYNTAX, msg );
                ldapSchemaException.setSourceObject( this );
                ldapSchemaException.setRelatedId( ldapSyntaxOid );
                errors.add( ldapSchemaException );
                LOG.info( msg );
            }

            /**
             * Add the MR references (using and usedBy) : 
             * MR -> C
             * MR -> N
             * MR -> S
             */
            if ( ldapComparator != null )
            {
                registries.addReference( this, ldapComparator );
            }

            if ( normalizer != null )
            {
                registries.addReference( this, normalizer );
            }

            if ( ldapSyntax != null )
            {
                registries.addReference( this, ldapSyntax );
            }

        }
    }


    /**
     * Remove the MatchingRule from the registries, updating the references to
     * other SchemaObject.
     * 
     * If one of the referenced SchemaObject does not exist (), 
     * an exception is thrown.
     *
     * @param registries The Registries
     * @exception If the MatchingRule is not valid 
     */
    public void removeFromRegistries( List<Throwable> errors, Registries registries ) throws LdapException
    {
        if ( registries != null )
        {
            /**
             * Remove the MR references (using and usedBy) : 
             * MR -> C
             * MR -> N
             * MR -> S
             */
            if ( ldapComparator != null )
            {
                registries.delReference( this, ldapComparator );
            }

            if ( ldapSyntax != null )
            {
                registries.delReference( this, ldapSyntax );
            }

            if ( normalizer != null )
            {
                registries.delReference( this, normalizer );
            }
        }
    }


    /* (non-Javadoc)
     * @see org.apache.directory.shared.ldap.model.schema.MatchingRule#getSyntax()
     */
    public MutableLdapSyntax getSyntax()
    {
        return ldapSyntax;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.shared.ldap.model.schema.MatchingRule#getSyntaxOid()
     */
    public String getSyntaxOid()
    {
        return ldapSyntaxOid;
    }


    /**
     * Sets the Syntax's OID
     *
     * @param oid The Syntax's OID
     */
    public void setSyntaxOid( String oid )
    {
        if ( locked )
        {
            throw new UnsupportedOperationException( I18n.err( I18n.ERR_04441, getName() ) );
        }

        if ( !isReadOnly )
        {
            this.ldapSyntaxOid = oid;
        }
    }


    /**
     * Sets the Syntax
     *
     * @param ldapSyntax The Syntax
     */
    public void setSyntax( MutableLdapSyntaxImpl ldapSyntax )
    {
        if ( locked )
        {
            throw new UnsupportedOperationException( I18n.err( I18n.ERR_04441, getName() ) );
        }

        if ( !isReadOnly )
        {
            this.ldapSyntax = ldapSyntax;
            this.ldapSyntaxOid = ldapSyntax.getOid();
        }
    }


    /**
     * Update the associated Syntax, even if the SchemaObject is readOnly
     *
     * @param ldapSyntax The Syntax
     */
    public void updateSyntax( MutableLdapSyntaxImpl ldapSyntax )
    {
        if ( locked )
        {
            throw new UnsupportedOperationException( I18n.err( I18n.ERR_04441, getName() ) );
        }

        this.ldapSyntax = ldapSyntax;
        this.ldapSyntaxOid = ldapSyntax.getOid();
    }


    /* (non-Javadoc)
     * @see org.apache.directory.shared.ldap.model.schema.MatchingRule#getLdapComparator()
     */
    public AbstractLdapComparator<? super Object> getLdapComparator()
    {
        return ldapComparator;
    }


    /**
     * Sets the LdapComparator
     *
     * @param ldapComparator The LdapComparator
     */
    @SuppressWarnings("unchecked")
    public void setLdapComparator( AbstractLdapComparator<?> ldapComparator )
    {
        if ( locked )
        {
            throw new UnsupportedOperationException( I18n.err( I18n.ERR_04441, getName() ) );
        }

        if ( !isReadOnly )
        {
            this.ldapComparator = ( AbstractLdapComparator<? super Object> ) ldapComparator;
        }
    }


    /**
     * Update the associated Comparator, even if the SchemaObject is readOnly
     *
     * @param ldapComparator The LdapComparator
     */
    @SuppressWarnings("unchecked")
    public void updateLdapComparator( AbstractLdapComparator<?> ldapComparator )
    {
        if ( locked )
        {
            throw new UnsupportedOperationException( I18n.err( I18n.ERR_04441, getName() ) );
        }

        this.ldapComparator = ( AbstractLdapComparator<? super Object> ) ldapComparator;
    }


    /* (non-Javadoc)
     * @see org.apache.directory.shared.ldap.model.schema.MatchingRule#getNormalizer()
     */
    public MutableNormalizer getNormalizer()
    {
        return normalizer;
    }


    /**
     * Sets the Normalizer
     *
     * @param normalizer The Normalizer
     */
    public void setNormalizer( AbstractNormalizer normalizer )
    {
        if ( locked )
        {
            throw new UnsupportedOperationException( I18n.err( I18n.ERR_04441, getName() ) );
        }

        if ( !isReadOnly )
        {
            this.normalizer = normalizer;
        }
    }


    /**
     * Update the associated Normalizer, even if the SchemaObject is readOnly
     *
     * @param normalizer The Normalizer
     */
    public void updateNormalizer( AbstractNormalizer normalizer )
    {
        if ( locked )
        {
            throw new UnsupportedOperationException( I18n.err( I18n.ERR_04441, getName() ) );
        }

        this.normalizer = normalizer;
    }


    /**
     * @see Object#toString()
     */
    public String toString()
    {
        return objectType + " " + DescriptionUtils.getDescription( this );
    }


    /* (non-Javadoc)
     * @see org.apache.directory.shared.ldap.model.schema.MatchingRule#copy()
     */
    public MatchingRule copy()
    {
        MutableMatchingRuleImpl copy = new MutableMatchingRuleImpl( oid );

        // Copy the SchemaObject common data
        copy.copy( this );

        // All the references to other Registries object are set to null.
        copy.ldapComparator = null;
        copy.ldapSyntax = null;
        copy.normalizer = null;

        // Copy the syntax OID
        copy.ldapSyntaxOid = ldapSyntaxOid;

        return copy;
    }


    /**
     * @see Object#equals()
     */
    @Override
    public boolean equals( Object o )
    {
        if ( !super.equals( o ) )
        {
            return false;
        }

        if ( !( o instanceof MutableMatchingRuleImpl ) )
        {
            return false;
        }

        MutableMatchingRuleImpl that = ( MutableMatchingRuleImpl ) o;

        // Check the Comparator
        if ( ldapComparator != null )
        {
            if ( !ldapComparator.equals( that.ldapComparator ) )
            {
                return false;
            }
        }
        else
        {
            if ( that.ldapComparator != null )
            {
                return false;
            }
        }

        // Check the Normalizer
        if ( normalizer != null )
        {
            if ( !normalizer.equals( that.normalizer ) )
            {
                return false;
            }
        }
        else
        {
            if ( that.normalizer != null )
            {
                return false;
            }
        }

        // Check the Syntax OID
        if ( !compareOid( ldapSyntaxOid, that.ldapSyntaxOid ) )
        {
            return false;
        }

        // Check the Syntax
        if ( ldapSyntax != null )
        {
            if ( !ldapSyntax.equals( that.ldapSyntax ) )
            {
                return false;
            }
        }
        else
        {
            if ( that.ldapSyntax != null )
            {
                return false;
            }
        }

        return true;
    }


    /**
     * {@inheritDoc}
     */
    public void clear()
    {
        // Clear the common elements
        super.clear();

        // Clear the references
        ldapComparator = null;
        ldapSyntax = null;
        normalizer = null;
    }
}
