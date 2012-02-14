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
package org.apache.directory.shared.ldap.model.schema.registries;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.model.constants.MetaSchemaConstants;
import org.apache.directory.shared.ldap.model.constants.SchemaConstants;
import org.apache.directory.shared.ldap.model.entry.Entry;
import org.apache.directory.shared.ldap.model.entry.Attribute;
import org.apache.directory.shared.ldap.model.entry.Value;
import org.apache.directory.shared.ldap.model.schema.SchemaManager;
import org.apache.directory.shared.util.StringConstants;
import org.apache.directory.shared.util.Strings;


/**
 * An abstract class with a utility method and setListener() implemented.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractSchemaLoader implements SchemaLoader
{

    /** The listener. */
    protected SchemaLoaderListener listener;

    /**
     * A map of all available schema names to schema objects. This map is
     * populated when this class is created with all the schemas present in
     * the LDIF based schema repository.
     */
    protected final Map<String, Schema> schemaMap = new LowerCaseKeyMap();

    /**
     * a map implementation which converts the keys to lower case before inserting
     */
    private static class LowerCaseKeyMap extends HashMap<String, Schema>
    {
        private static final long serialVersionUID = 1L;


        @Override
        public Schema put( String key, Schema value )
        {
            return super.put( Strings.lowerCase( key ), value );
        }


        @Override
        public void putAll( Map<? extends String, ? extends Schema> map )
        {
            for ( Map.Entry<? extends String, ? extends Schema> e : map.entrySet() )
            {
                put( e.getKey(), e.getValue() );
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    public void setListener( SchemaLoaderListener listener )
    {
        this.listener = listener;
    }


    /**
     * Notify listener or registries.
     *
     * @param schema the schema
     * @param registries the registries
     */
    protected final void notifyListenerOrRegistries( Schema schema, SchemaManager schemaManager )
    {
        if ( listener != null )
        {
            listener.schemaLoaded( schema );
        }

        if ( schemaManager != listener )
        {
            schemaManager.getRegistries().schemaLoaded( schema );
        }
    }


    /**
     * {@inheritDoc}
     */
    public final Collection<Schema> getAllEnabled() throws Exception
    {
        Collection<Schema> enabledSchemas = new ArrayList<Schema>();

        for ( Schema schema : schemaMap.values() )
        {
            if ( schema.isEnabled() )
            {
                enabledSchemas.add( schema );
            }
        }

        return enabledSchemas;
    }


    /**
     * {@inheritDoc}
     */
    public final Collection<Schema> getAllSchemas() throws Exception
    {
        return schemaMap.values();
    }


    /**
     * {@inheritDoc}
     */
    public Schema getSchema( String schemaName )
    {
        return schemaMap.get( Strings.toLowerCase( schemaName ) );
    }


    /**
     * {@inheritDoc}
     */
    public void addSchema( Schema schema )
    {
        schemaMap.put( schema.getSchemaName(), schema );
    }


    /**
     * {@inheritDoc}
     */
    public void removeSchema( Schema schema )
    {
        schemaMap.remove( Strings.toLowerCase( schema.getSchemaName() ) );
    }


    /**
     * Gets the schema.
     *
     * @param entry the entry
     * @return the schema
     * @throws Exception the exception
     */
    protected Schema getSchema( Entry entry ) throws Exception
    {
        if ( entry == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_04261 ) );
        }

        Attribute objectClasses = entry.get( SchemaConstants.OBJECT_CLASS_AT );
        boolean isSchema = false;

        for ( Value<?> value : objectClasses )
        {
            if ( MetaSchemaConstants.META_SCHEMA_OC.equalsIgnoreCase( value.getString() ) )
            {
                isSchema = true;
                break;
            }
        }

        if ( !isSchema )
        {
            return null;
        }

        String name;
        String owner;
        String[] dependencies = StringConstants.EMPTY_STRINGS;
        boolean isDisabled = false;

        if ( entry.get( SchemaConstants.CN_AT ) == null )
        {
            throw new IllegalArgumentException( I18n.err( I18n.ERR_04262 ) );
        }

        name = entry.get( SchemaConstants.CN_AT ).getString();

        if ( entry.get( SchemaConstants.CREATORS_NAME_AT ) == null )
        {
            throw new IllegalArgumentException( "entry must have a valid " + SchemaConstants.CREATORS_NAME_AT
                + " attribute" );
        }

        owner = entry.get( SchemaConstants.CREATORS_NAME_AT ).getString();

        if ( entry.get( MetaSchemaConstants.M_DISABLED_AT ) != null )
        {
            String value = entry.get( MetaSchemaConstants.M_DISABLED_AT ).getString();
            value = value.toUpperCase();
            isDisabled = value.equals( "TRUE" );
        }

        if ( entry.get( MetaSchemaConstants.M_DEPENDENCIES_AT ) != null )
        {
            Set<String> depsSet = new HashSet<String>();
            Attribute depsAttr = entry.get( MetaSchemaConstants.M_DEPENDENCIES_AT );

            for ( Value<?> value : depsAttr )
            {
                depsSet.add( value.getString() );
            }

            dependencies = depsSet.toArray( StringConstants.EMPTY_STRINGS );
        }

        return new DefaultSchema( name, owner, dependencies, isDisabled );
    }


    // TODO: clean commented code

    /*
     * {@inheritDoc}
     *
    public List<Throwable> loadWithDependencies( Registries registries, boolean check, Schema... schemas ) throws Exception
    {
        // Relax the controls at first
        List<Throwable> errors = new ArrayList<Throwable>();
        boolean wasRelaxed = registries.isRelaxed();
        registries.setRelaxed( true );

        Map<String,Schema> notLoaded = new HashMap<String,Schema>();
        
        for ( Schema schema : schemas )
        {
            if ( ! registries.isSchemaLoaded( schema.getSchemaName() ) )
            {
                notLoaded.put( schema.getSchemaName(), schema );
            }
        }
        
        for ( Schema schema : notLoaded.values() )
        {
            Stack<String> beenthere = new Stack<String>();
            loadDepsFirst( schema, beenthere, notLoaded, schema, registries );
        }
        
        // At the end, check the registries if required
        if ( check )
        {
            errors = registries.checkRefInteg();
        }
        
        // Restore the Registries isRelaxed flag
        registries.setRelaxed( wasRelaxed );
        
        return errors;
    }
    
    
    /**
     * Register the comparator contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the comparator description
     * @param schema The associated schema
     * @throws Exception If the registering failed
     *
    protected LdapComparator<?> registerComparator( Registries registries, LdifEntry entry, Schema schema )
        throws Exception
    {
        return registerComparator( registries, entry.getEntry(), schema );
    }
    
    
    /**
     * Register the comparator contained in the given Entry into the registries.
     *
     * @param registries The Registries
     * @param entry The Entry containing the comparator description
     * @param schema The associated schema
     * @throws Exception If the registering failed
     *
    protected LdapComparator<?> registerComparator( Registries registries, Entry entry, Schema schema )
        throws Exception
    {
        LdapComparator<?> comparator =
            factory.getLdapComparator( entry, registries, schema.getSchemaName() );
        comparator.setOid( entry.get( MetaSchemaConstants.M_OID_AT ).getString() );

        if ( registries.isRelaxed() )
        {
            if ( registries.acceptDisabled() )
            {
                registries.register( comparator );
            }
            else if ( schema.isEnabled() && comparator.isEnabled() )
            {
                registries.register( comparator );
            }
        }
        else
        {
            if ( schema.isEnabled() && comparator.isEnabled() )
            {
                registries.register( comparator );
            }
        }
        
        return comparator;
    }
    
    
    /**
     * Register the SyntaxChecker contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the SyntaxChecker description
     * @param schema The associated schema
     * @return the created SyntaxChecker instance
     * @throws Exception If the registering failed
     *
    protected SyntaxChecker registerSyntaxChecker( Registries registries, LdifEntry entry, Schema schema)
        throws Exception
    {
        SyntaxChecker syntaxChecker =
            factory.getSyntaxChecker( entry.getEntry(), registries, schema.getSchemaName() );
        syntaxChecker.setOid( entry.get( MetaSchemaConstants.M_OID_AT ).getString() );

        if ( registries.isRelaxed() )
        {
            if ( registries.acceptDisabled() )
            {
                registries.register( syntaxChecker );
            }
            else if ( schema.isEnabled() && syntaxChecker.isEnabled() )
            {
                registries.register( syntaxChecker );
            }
        }
        else
        {
            if ( schema.isEnabled() && syntaxChecker.isEnabled() )
            {
                registries.register( syntaxChecker );
            }
        }
        
        return syntaxChecker;
    }
    
    
    /**
     * Register the Normalizer contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the Normalizer description
     * @param schema The associated schema
     * @return the created Normalizer instance
     * @throws Exception If the registering failed
     *
    protected Normalizer registerNormalizer( Registries registries, LdifEntry entry, Schema schema)
        throws Exception
    {
        Normalizer normalizer =
            factory.getNormalizer( entry.getEntry(), registries, schema.getSchemaName() );
        
        if ( registries.isRelaxed() )
        {
            if ( registries.acceptDisabled() )
            {
                registries.register( normalizer );
            }
            else if ( schema.isEnabled() && normalizer.isEnabled() )
            {
                registries.register( normalizer );
            }
        }
        else
        {
            if ( schema.isEnabled() && normalizer.isEnabled() )
            {
                registries.register( normalizer );
            }
        }
        
        return normalizer;
    }
    
    
    /**
     * Register the MatchingRule contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the MatchingRule description
     * @param schema The associated schema
     * @return the created MatchingRule instance
     * @throws Exception If the registering failed
     *
    protected MatchingRule registerMatchingRule( Registries registries, LdifEntry entry, Schema schema)
        throws Exception
    {
        MatchingRule matchingRule = factory.getMatchingRule(
            entry.getEntry(), registries, schema.getSchemaName() );

        if ( registries.isRelaxed() )
        {
            if ( registries.acceptDisabled() )
            {
                registries.register( matchingRule );
            }
            else if ( schema.isEnabled() && matchingRule.isEnabled() )
            {
                registries.register( matchingRule );
            }
        }
        else
        {
            if ( schema.isEnabled() && matchingRule.isEnabled() )
            {
                registries.register( matchingRule );
            }
        }
        
        return matchingRule;
    }
    
    
    /**
     * Register the Syntax contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the Syntax description
     * @param schema The associated schema
     * @return the created Syntax instance
     * @throws Exception If the registering failed
     *
    protected LdapSyntax registerSyntax( Registries registries, LdifEntry entry, Schema schema)
        throws Exception
    {
        LdapSyntax syntax = factory.getSyntax(
            entry.getEntry(), registries, schema.getSchemaName() );

        if ( registries.isRelaxed() )
        {
            if ( registries.acceptDisabled() )
            {
                registries.register( syntax );
            }
            else if ( schema.isEnabled() && syntax.isEnabled() )
            {
                registries.register( syntax );
            }
        }
        else
        {
            if ( schema.isEnabled() && syntax.isEnabled() )
            {
                registries.register( syntax );
            }
        }
        
        return syntax;
    }
    
    
    /**
     * Register the AttributeType contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the AttributeType description
     * @param schema The associated schema
     * @return the created AttributeType instance
     * @throws Exception If the registering failed
     *
    protected AttributeType registerAttributeType( Registries registries, LdifEntry entry, Schema schema )
        throws Exception
    {
        AttributeType attributeType = factory.getAttributeType( entry.getEntry(), registries, schema.getSchemaName() );
        
        if ( registries.isRelaxed() )
        {
            if ( registries.acceptDisabled() )
            {
                registries.register( attributeType );
            }
            else if ( schema.isEnabled() && attributeType.isEnabled() )
            {
                registries.register( attributeType );
            }
        }
        else
        {
            if ( schema.isEnabled() && attributeType.isEnabled() )
            {
                registries.register( attributeType );
            }
        }
        
        return attributeType;
    }
    
    
    /**
     * Register the MatchingRuleUse contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the MatchingRuleUse description
     * @param schema The associated schema
     * @return the created MatchingRuleUse instance
     * @throws Exception If the registering failed
     *
    protected MatchingRuleUse registerMatchingRuleUse( Registries registries, LdifEntry entry, Schema schema)
        throws Exception
    {
        throw new NotImplementedException( "Need to implement factory " +
                "method for creating a MatchingRuleUse" );
    }
    
    
    /**
     * Register the NameForm contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the NameForm description
     * @param schema The associated schema
     * @return the created NameForm instance
     * @throws Exception If the registering failed
     *
    protected NameForm registerNameForm( Registries registries, LdifEntry entry, Schema schema)
        throws Exception
    {
        throw new NotImplementedException( "Need to implement factory " +
                "method for creating a NameForm" );
    }
    
    
    /**
     * Register the DitContentRule contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the DitContentRule description
     * @param schema The associated schema
     * @return the created DitContentRule instance
     * @throws Exception If the registering failed
     *
    protected DITContentRule registerDitContentRule( Registries registries, LdifEntry entry, Schema schema)
        throws Exception
    {
        throw new NotImplementedException( "Need to implement factory " +
                "method for creating a DitContentRule" );
    }
    
    
    /**
     * Register the DitStructureRule contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the DitStructureRule description
     * @param schema The associated schema
     * @return the created DitStructureRule instance
     * @throws Exception If the registering failed
     *
    protected DITStructureRule registerDitStructureRule( Registries registries, LdifEntry entry, Schema schema)
        throws Exception
    {
        throw new NotImplementedException( "Need to implement factory " +
                "method for creating a DitStructureRule" );
    }


    /**
     * Register the ObjectClass contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The LdifEntry containing the ObjectClass description
     * @param schema The associated schema
     * @return the created ObjectClass instance
     * @throws Exception If the registering failed
     *
    protected ObjectClass registerObjectClass( Registries registries, LdifEntry entry, Schema schema)
        throws Exception
    {
        return registerObjectClass( registries, entry.getEntry(), schema );
    }


    /**
     * Register the ObjectClass contained in the given LdifEntry into the registries.
     *
     * @param registries The Registries
     * @param entry The Entry containing the ObjectClass description
     * @param schema The associated schema
     * @return the created ObjectClass instance
     * @throws Exception If the registering failed
     *
    protected ObjectClass registerObjectClass( Registries registries, Entry entry, Schema schema)
        throws Exception
    {
        ObjectClass objectClass = factory.getObjectClass( entry, registries, schema.getSchemaName() );

        if ( registries.isRelaxed() )
        {
            if ( registries.acceptDisabled() )
            {
                registries.register( objectClass );
            }
            else if ( schema.isEnabled() && objectClass.isEnabled() )
            {
                registries.register( objectClass );
            }
        }
        else
        {
            if ( schema.isEnabled() && objectClass.isEnabled() )
            {
                registries.register( objectClass );
            }
        }
        
        return objectClass;
    }
    
    
    public EntityFactory getFactory()
    {
        return factory;
    }
    */

    // TODO: is this used?
    public Object getDao()
    {
        return null;
    }


    private Schema[] buildSchemaArray( String... schemaNames ) throws Exception
    {
        Schema[] schemas = new Schema[schemaNames.length];
        int pos = 0;

        for ( String schemaName : schemaNames )
        {
            schemas[pos++] = getSchema( schemaName );
        }

        return schemas;
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadAttributeTypes( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadAttributeTypes( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadComparators( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadComparators( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadDitContentRules( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadDitContentRules( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadDitStructureRules( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadDitStructureRules( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadMatchingRules( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadMatchingRules( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadMatchingRuleUses( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadMatchingRuleUses( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadNameForms( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadNameForms( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadNormalizers( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadNormalizers( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadObjectClasses( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadObjectClasses( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadSyntaxes( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadSyntaxes( buildSchemaArray( schemaNames ) );
    }


    /**
     * {@inheritDoc}
     */
    public List<Entry> loadSyntaxCheckers( String... schemaNames ) throws Exception
    {
        if ( schemaNames == null )
        {
            return new ArrayList<Entry>();
        }

        return loadSyntaxCheckers( buildSchemaArray( schemaNames ) );
    }
}
