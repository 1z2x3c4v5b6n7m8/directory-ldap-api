/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.apache.directory.shared.ldap.codec.standalone;


import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.ldap.BasicControl;

import org.apache.directory.shared.asn1.DecoderException;
import org.apache.directory.shared.asn1.EncoderException;
import org.apache.directory.shared.asn1.ber.Asn1Container;
import org.apache.directory.shared.ldap.codec.BasicControlDecorator;
import org.apache.directory.shared.ldap.codec.api.CodecControl;
import org.apache.directory.shared.ldap.codec.api.ControlFactory;
import org.apache.directory.shared.ldap.codec.api.ExtendedRequestDecorator;
import org.apache.directory.shared.ldap.codec.api.ExtendedRequestFactory;
import org.apache.directory.shared.ldap.codec.api.ExtendedResponseDecorator;
import org.apache.directory.shared.ldap.codec.api.LdapCodecService;
import org.apache.directory.shared.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.shared.ldap.codec.api.MessageDecorator;
import org.apache.directory.shared.ldap.codec.api.UnsolicitedResponseFactory;
import org.apache.directory.shared.ldap.codec.controls.cascade.CascadeFactory;
import org.apache.directory.shared.ldap.codec.controls.manageDsaIT.ManageDsaITFactory;
import org.apache.directory.shared.ldap.codec.controls.search.entryChange.EntryChangeFactory;
import org.apache.directory.shared.ldap.codec.controls.search.pagedSearch.PagedResultsFactory;
import org.apache.directory.shared.ldap.codec.controls.search.persistentSearch.PersistentSearchFactory;
import org.apache.directory.shared.ldap.codec.controls.search.subentries.SubentriesFactory;
import org.apache.directory.shared.ldap.model.message.Control;
import org.apache.directory.shared.ldap.model.message.ExtendedRequest;
import org.apache.directory.shared.ldap.model.message.ExtendedRequestImpl;
import org.apache.directory.shared.ldap.model.message.ExtendedResponse;
import org.apache.directory.shared.ldap.model.message.ExtendedResponseImpl;
import org.apache.directory.shared.ldap.model.message.Message;
import org.apache.directory.shared.ldap.model.message.controls.OpaqueControl;
import org.apache.directory.shared.util.OsgiUtils;
import org.apache.directory.shared.util.Strings;
import org.apache.directory.shared.util.exception.NotImplementedException;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The default {@link org.apache.directory.shared.ldap.codec.api.LdapCodecService} implementation.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class StandaloneLdapCodecService implements LdapCodecService
{
    /** Missing felix constants for cache directory locking */
    public static final String FELIX_CACHE_LOCKING = "felix.cache.locking";

    /** Missing felix constants for cache root directory path */
    public static final String FELIX_CACHE_ROOTDIR = "felix.cache.rootdir";

    /** A logger */
    private static final Logger LOG = LoggerFactory.getLogger( StandaloneLdapCodecService.class );
    
    /**
     * This should be constructed at class initialization time by reading the
     * Export-Package attribute of the this jar's manifest file.
     */
    private static final String[] SYSTEM_PACKAGES =
    {
        "org.slf4j; version=1.6.0",
        "org.apache.directory.shared.i18n; version=1.0.0",
        "org.apache.directory.shared.util; version=1.0.0",
        "org.apache.directory.shared.util.exception; version=1.0.0",
        "org.apache.directory.shared.asn1; version=1.0.0",
        "org.apache.directory.shared.asn1.util; version=1.0.0",
        "org.apache.directory.shared.asn1.ber; version=1.0.0",
        "org.apache.directory.shared.asn1.ber.tlv; version=1.0.0",
        "org.apache.directory.shared.asn1.ber.grammar; version=1.0.0",
        "org.apache.directory.shared.asn1.actions; version=1.0.0",
        "org.apache.directory.shared.ldap.asn1.ber; version=1.0.0",
        "org.apache.directory.shared.ldap.model; version=1.0.0",
        "org.apache.directory.shared.ldap.model.exception; version=1.0.0",
        "org.apache.directory.shared.ldap.model.filter; version=1.0.0",
        "org.apache.directory.shared.ldap.model.name; version=1.0.0",
        "org.apache.directory.shared.ldap.model.entry; version=1.0.0",
        "org.apache.directory.shared.ldap.model.schema; version=1.0.0",
        "org.apache.directory.shared.ldap.model.message; version=1.0.0",
        "org.apache.directory.shared.ldap.model.message.controls; version=1.0.0",
        "org.apache.directory.shared.ldap.codec.controls; version=1.0.0",
        "org.apache.directory.shared.ldap.codec.api; version=1.0.0",
        "org.apache.directory.shared.ldap.model.url; version=1.0.0",
        "org.apache.directory.shared.ldap.extras.controls",
        "org.apache.directory.shared.ldap.extras.extended"
    };
 
    /** System property checked if the pluginProperty is null */
    public static final String PLUGIN_DIRECTORY_PROPERTY = "codec.plugin.directory";
    
    /** The map of registered {@link org.apache.directory.shared.ldap.codec.api.ControlFactory}'s */
    private Map<String,ControlFactory<?,?>> controlFactories = new HashMap<String, ControlFactory<?,?>>();

    /** The map of registered {@link org.apache.directory.shared.ldap.codec.api.ExtendedRequestFactory}'s by request OID */
    private Map<String,ExtendedRequestFactory<?,?>> extReqFactories = new HashMap<String, ExtendedRequestFactory<?,?>>();

    /** The map of registered {@link UnsolicitedResponseFactory}'s by request OID */
    private Map<String,UnsolicitedResponseFactory<?>> unsolicitedFactories = new HashMap<String, UnsolicitedResponseFactory<?>>();
    
    /** The LDAP {@link ProtocolCodecFactory} implementation used */
    private ProtocolCodecFactory protocolCodecFactory;
    
    /** The codec's {@link BundleActivator} */
    private CodecHostActivator activator;
    
    /** The embedded {@link Felix} instance */
    private Felix felix;

    /** Felix's bundle cache directory */
    private File cacheDirectory;
    
    /** The plugin (bundle) containing directory to load codec extensions from */
    private File pluginDirectory;
    
    
    private void initPluginDirectory( File pluginDirectory )
    {
        // Use the given directory if not null, or use the default one
        if ( pluginDirectory == null )
        {
            this.pluginDirectory = getPluginDirectoryDefault();
            LOG.info( "Null plugin directory provided, using default instead: {}", this.pluginDirectory );
        }
        else
        {
            this.pluginDirectory = pluginDirectory;
            LOG.info( "Valid plugin directory provided: {}", this.pluginDirectory );
        }
        
        // Now, if we have a plugin directory, check that it's a directory and that it's readable
        if ( this.pluginDirectory != null )
        {
            if ( ( ! this.pluginDirectory.exists() ) && ! this.pluginDirectory.mkdirs() )
            {
                String msg = "The provided plugin directory is not creatable:" + this.pluginDirectory.getAbsolutePath();
                LOG.error( msg );
                throw new IllegalArgumentException( msg );
            }

            if ( ! this.pluginDirectory.isDirectory() )
            {
                String msg = "The provided plugin directory is not a directory:" + this.pluginDirectory.getAbsolutePath();
                LOG.error( msg );
                throw new IllegalArgumentException( msg );
            }
            else if ( ! this.pluginDirectory.canRead() )
            {
                String msg = "The provided plugin directory is not readable:" + this.pluginDirectory.getAbsolutePath();
                LOG.error( msg );
                throw new IllegalArgumentException( msg );
            }
        }
    }
    
    
    private void initCacheDirectory( File cacheDirectory )
    {
        // Use the given directory if not null, or use the default one
        if ( cacheDirectory == null )
        {
            this.cacheDirectory = getCacheDirectoryDefault();
            LOG.info( "Null cache directory provided, using default instead: {}", this.cacheDirectory );
        }
        else
        {
            this.cacheDirectory = cacheDirectory;
            LOG.info( "Valid cache directory provided: {}", this.cacheDirectory );
        }
        
        // Now, if we have a cache directory, check that it's a directory and that it's readable
        if ( this.cacheDirectory != null )
        {
            if ( ! this.cacheDirectory.exists() &&  ( ! this.cacheDirectory.mkdirs() ) )
            {
                String msg = "The provided cache directory can't be created:" + this.cacheDirectory.getAbsolutePath();
                LOG.error( msg );
                throw new IllegalArgumentException( msg );
            }
            
            if ( ! this.cacheDirectory.isDirectory() )
            {
                String msg = "The provided cache directory is not a directory:" + this.cacheDirectory.getAbsolutePath();
                LOG.error( msg );
                throw new IllegalArgumentException( msg );
            }
            else if ( ! this.cacheDirectory.canRead() )
            {
                String msg = "The provided cache directory is not readable:" + this.cacheDirectory.getAbsolutePath();
                LOG.error( msg );
                throw new IllegalArgumentException( msg );
            }
        }
    }
    
    
    /**
     * Creates a new instance of StandaloneLdapCodecService. Optionally checks for
     * system property {@link #PLUGIN_DIRECTORY_PROPERTY}. Intended for use by 
     * unit test running tools like Maven's surefire:
     * <pre>
     *   &lt;properties&gt;
     *     &lt;codec.plugin.directory&gt;${project.build.directory}/pluginDirectory&lt;/codec.plugin.directory&gt;
     *   &lt;/properties&gt;
     * 
     *   &lt;build&gt;
     *     &lt;plugins&gt;
     *       &lt;plugin&gt;
     *         &lt;artifactId&gt;maven-surefire-plugin&lt;/artifactId&gt;
     *         &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
     *         &lt;configuration&gt;
     *           &lt;systemPropertyVariables&gt;
     *             &lt;workingDirectory&gt;${basedir}/target&lt;/workingDirectory&gt;
     *             &lt;felix.cache.rootdir&gt;
     *               ${project.build.directory}
     *             &lt;/felix.cache.rootdir&gt;
     *             &lt;felix.cache.locking&gt;
     *               true
     *             &lt;/felix.cache.locking&gt;
     *             &lt;org.osgi.framework.storage.clean&gt;
     *               onFirstInit
     *             &lt;/org.osgi.framework.storage.clean&gt;
     *             &lt;org.osgi.framework.storage&gt;
     *               osgi-cache
     *             &lt;/org.osgi.framework.storage&gt;
     *             &lt;codec.plugin.directory&gt;
     *               ${codec.plugin.directory}
     *             &lt;/codec.plugin.directory&gt;
     *           &lt;/systemPropertyVariables&gt;
     *         &lt;/configuration&gt;
     *       &lt;/plugin&gt;
     *       
     *       &lt;plugin&gt;
     *         &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
     *         &lt;artifactId&gt;maven-dependency-plugin&lt;/artifactId&gt;
     *         &lt;executions&gt;
     *           &lt;execution&gt;
     *             &lt;id&gt;copy&lt;/id&gt;
     *             &lt;phase&gt;compile&lt;/phase&gt;
     *             &lt;goals&gt;
     *               &lt;goal&gt;copy&lt;/goal&gt;
     *             &lt;/goals&gt;
     *             &lt;configuration&gt;
     *               &lt;artifactItems&gt;
     *                 &lt;artifactItem&gt;
     *                   &lt;groupId&gt;${project.groupId}&lt;/groupId&gt;
     *                   &lt;artifactId&gt;shared-ldap-extras-codec&lt;/artifactId&gt;
     *                   &lt;version&gt;${project.version}&lt;/version&gt;
     *                   &lt;outputDirectory&gt;${codec.plugin.directory}&lt;/outputDirectory&gt;
     *                 &lt;/artifactItem&gt;
     *               &lt;/artifactItems&gt;
     *             &lt;/configuration&gt;
     *           &lt;/execution&gt;
     *         &lt;/executions&gt;
     *       &lt;/plugin&gt;
     *     &lt;/plugins&gt;
     *   &lt;/build&gt;
     * </pre>
     */
    public StandaloneLdapCodecService()
    {
        this( null, null );
    }
    
    
    /**
     * Creates a new instance of StandaloneLdapCodecService.
     * 
     * @param pluginDirectory The directory where plugins are stored
     * @param cacheDirectory The directory where the embedded Felix manages 
     * its cache
     */
    public StandaloneLdapCodecService( File pluginDirectory, File cacheDirectory )
    {
        
        // -------------------------------------------------------------------
        // Handle plugin directory
        // -------------------------------------------------------------------
        initPluginDirectory( pluginDirectory );
        
        // -------------------------------------------------------------------
        // Handle cache directory
        // -------------------------------------------------------------------
        initCacheDirectory( cacheDirectory );
        
        // Load the default controls
        loadStockControls();
        
        // Start the Felix container
        setupFelix();
        
        if ( protocolCodecFactory == null )
        {
            try
            {
                @SuppressWarnings("unchecked")
                Class<? extends ProtocolCodecFactory> clazz = ( Class<? extends ProtocolCodecFactory> ) 
                    Class.forName( DEFAULT_PROTOCOL_CODEC_FACTORY );
                protocolCodecFactory = clazz.newInstance();
            }
            catch( Exception cause )
            {
                throw new RuntimeException( "Failed to load default codec factory.", cause );
            }
        }
    }
    
    
    /**
     * Uses system properties and default considerations to create a cache 
     * directory that can be used when one is not provided.
     *
     * @see FelixConstants#FRAMEWORK_STORAGE
     * @return The cache directory default.
     */
    private File getCacheDirectoryDefault()
    {
        String frameworkStorage = System.getProperties().getProperty( FelixConstants.FRAMEWORK_STORAGE );
        LOG.info( "{}: {}", FelixConstants.FRAMEWORK_STORAGE, frameworkStorage );
        
        String felixCacheRootdir = System.getProperties().getProperty( FELIX_CACHE_ROOTDIR );
        LOG.info( "{}: {}", FELIX_CACHE_ROOTDIR, felixCacheRootdir );

        try
        {
            if ( frameworkStorage == null )
            {
                if ( felixCacheRootdir == null )
                {
                    return new File( File.createTempFile( "dummy", null ).getParentFile(), 
                        "osgi-cache-" + Integer.toString( this.hashCode() ) );
                }
                else
                {
                    return new File( new File ( felixCacheRootdir ), 
                        "osgi-cache-" + Integer.toString( this.hashCode() ) );
                }
            } 
            else if ( felixCacheRootdir == null )
            {
                return new File( frameworkStorage + "-" + Integer.toString( this.hashCode() ) );
            }
            
            // else if both are not null now
            return new File( new File ( felixCacheRootdir ), 
                frameworkStorage + "-" + Integer.toString( this.hashCode() ) );
        }
        catch ( Exception e ) 
        {
            String message = "Failure to create temporary cache directory: " + e.getMessage();
            LOG.warn( message, e );
            return null;
        }
    }
    
    
    /**
     * Gets the optional system property value for the pluginDirectory if one 
     * is provided.
     *
     * @return The path for the pluginDirectory or null if not provided.
     */
    private File getPluginDirectoryDefault()
    {
        String value = System.getProperty( StandaloneLdapCodecService.PLUGIN_DIRECTORY_PROPERTY );
        LOG.info( "{}: {}", PLUGIN_DIRECTORY_PROPERTY, value );
        
        if ( value == null )
        {
            return null;
        }
        
        return new File( value );
    }
    
    
    /**
     * Assembles the <code>org.osgi.framework.system.packages.extra</code> list
     * of system packages exported by the embedding host to interact with bundles
     * running inside {@link Felix}.
     * 
     * @return A comma delimited list of exported host packages.
     */
    private String getSystemPackages()
    {
        Set<String> pkgs = new HashSet<String>();

        // Load defaults from command line properties if it exists
        String sysProp = System.getProperty( FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA );
        
        if ( sysProp != null )
        {
            OsgiUtils.splitIntoPackages( sysProp, pkgs );
        }
        
        // Merge defaults with exports from all bundles on system path
        OsgiUtils.getAllBundleExports( null, pkgs );
        
        // Merge all now with exports listed in our properties
        Collections.addAll( pkgs, SYSTEM_PACKAGES );
        
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        
        for ( String pkg :  pkgs )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                sb.append( ',' );
            }
            
            sb.append( pkg );
            LOG.debug( "Adding system extras package: {}", pkg );
        }
        
        return sb.toString();
    }
    
    
    /**
     * Sets up a {@link Felix} instance.
     */
    private void setupFelix()
    {
        // initialize activator and setup system bundle activators
        activator = new CodecHostActivator( this );
        List<BundleActivator> activators = new ArrayList<BundleActivator>();
        activators.add( activator );
        
        // setup configuration for felix 
        Map<String, Object> config = new HashMap<String, Object>();
        config.put( FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, activators );
        config.put( FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, getSystemPackages() );
        config.put( FELIX_CACHE_ROOTDIR, this.cacheDirectory.getParent() );
        config.put( FelixConstants.FRAMEWORK_STORAGE, this.cacheDirectory.getName() );
        
        if ( System.getProperties().getProperty( FelixConstants.FRAMEWORK_STORAGE_CLEAN ) != null )
        {
            String cleanMode = System.getProperties().getProperty( FelixConstants.FRAMEWORK_STORAGE_CLEAN );
            config.put( FelixConstants.FRAMEWORK_STORAGE_CLEAN, cleanMode );
            LOG.info( "Using framework storage clean value from sytem properties: {}", cleanMode );
        }
        else
        {
            config.put( FelixConstants.FRAMEWORK_STORAGE_CLEAN, "none" );
            LOG.info( "Using framework storage clean defaults: none" );
        }

        if ( System.getProperties().getProperty( FELIX_CACHE_LOCKING ) != null )
        {
            String lockCache = System.getProperties().getProperty( FELIX_CACHE_LOCKING );
            config.put( FELIX_CACHE_LOCKING, lockCache );
            LOG.info( "Using framework cache locking setting from sytem properties: {}", lockCache );
        }
        else
        {
            config.put( FELIX_CACHE_LOCKING, "true" );
            LOG.info( "Using default for cache locking: enabled" );
        }
        
        // instantiate and start up felix
        felix = new Felix( config );
        
        try
        {
            felix.start();
        }
        catch ( BundleException e )
        {
            String message = "Failed to start embedded felix instance: " + e.getMessage();
            LOG.error( message, e );
            throw new RuntimeException( message, e );
        }
    }
    
    
    /**
     * Shuts down the codec and its embedded {@link Felix} instance.
     */
    public void shutdown()
    {
        LOG.info( "Attempt to shutdown the codec service" );
        
        try
        {
            felix.stop();
            felix.waitForStop( 0 );
        }
        catch ( Exception e )
        {
            String message = "Failed to stop embedded felix instance: " + e.getMessage();
            LOG.error( message, e );
            throw new RuntimeException( message, e );
        }
    }
    
    
    /**
     * Loads the Controls implement out of the box in the codec.
     */
    private void loadStockControls()
    {
        ControlFactory<?, ?> factory = new CascadeFactory( this );
        controlFactories.put( factory.getOid(), factory );
        LOG.info( "Registered pre-bundled control factory: {}", factory.getOid() );
        
        factory = new EntryChangeFactory( this );
        controlFactories.put( factory.getOid(), factory );
        LOG.info( "Registered pre-bundled control factory: {}", factory.getOid() );
        
        factory = new ManageDsaITFactory( this );
        controlFactories.put( factory.getOid(), factory );
        LOG.info( "Registered pre-bundled control factory: {}", factory.getOid() );
        
        factory = new PagedResultsFactory( this );
        controlFactories.put( factory.getOid(), factory );
        LOG.info( "Registered pre-bundled control factory: {}", factory.getOid() );
        
        factory = new PersistentSearchFactory( this );
        controlFactories.put( factory.getOid(), factory );
        LOG.info( "Registered pre-bundled control factory: {}", factory.getOid() );

        factory = new SubentriesFactory( this );
        controlFactories.put( factory.getOid(), factory );
        LOG.info( "Registered pre-bundled control factory: {}", factory.getOid() );
    }
    
    //-------------------------------------------------------------------------
    // LdapCodecService implementation methods
    //-------------------------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    public ControlFactory<?,?> registerControl( ControlFactory<?,?> factory )
    {
        return controlFactories.put( factory.getOid(), factory );
    }
    

    /**
     * {@inheritDoc}
     */
    public ControlFactory<?,?> unregisterControl( String oid )
    {
        return controlFactories.remove( oid );
    }

    
    /**
     * {@inheritDoc}
     */
    public Iterator<String> registeredControls()
    {
        return Collections.unmodifiableSet( controlFactories.keySet() ).iterator();
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean isControlRegistered( String oid )
    {
        return controlFactories.containsKey( oid );
    }
    

    /**
     * {@inheritDoc}
     */
    public Iterator<String> registeredExtendedRequests()
    {
        return Collections.unmodifiableSet( extReqFactories.keySet() ).iterator();
    }

    
    /**
     * {@inheritDoc}
     */
    public ExtendedRequestFactory<?, ?> registerExtendedRequest( ExtendedRequestFactory<?,?> factory )
    {
        return extReqFactories.put( factory.getOid(), factory );
    }

    
    /**
     * {@inheritDoc}
     */
    public ProtocolCodecFactory getProtocolCodecFactory()
    {
        return protocolCodecFactory;
    }

    
    /**
     * {@inheritDoc}
     */
    public ProtocolCodecFactory registerProtocolCodecFactory( ProtocolCodecFactory protocolCodecFactory )
    {
        ProtocolCodecFactory old = this.protocolCodecFactory;
        this.protocolCodecFactory = protocolCodecFactory;
        return old;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public CodecControl<? extends Control> newControl( String oid )
    {
        ControlFactory<?,?> factory = controlFactories.get( oid );
        
        if ( factory == null )
        {
            return new BasicControlDecorator<Control>( this, new OpaqueControl( oid ) );
        }
        
        return factory.newCodecControl();
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public CodecControl<? extends Control> newControl( Control control )
    {
        if ( control == null )
        {
            throw new NullPointerException( "Control argument was null." );
        }
        
        // protect agains being multiply decorated
        if ( control instanceof CodecControl )
        {
            return (org.apache.directory.shared.ldap.codec.api.CodecControl<?> )control;
        }
        
        @SuppressWarnings("rawtypes")
        ControlFactory factory = controlFactories.get( control.getOid() );
        
        if ( factory == null )
        {
            return new BasicControlDecorator<Control>( this, control ); 
        }
        
        return factory.newCodecControl( control );
    }


    /**
     * {@inheritDoc}
     */
    public javax.naming.ldap.Control toJndiControl( Control control ) throws EncoderException
    {
        CodecControl<? extends Control> decorator = newControl( control );
        ByteBuffer bb = ByteBuffer.allocate( decorator.computeLength() );
        decorator.encode( bb );
        bb.flip();
        BasicControl jndiControl = 
            new BasicControl( control.getOid(), control.isCritical(), bb.array() );
        return jndiControl;
    }


    /**
     * {@inheritDoc}
     */
    public Control fromJndiControl( javax.naming.ldap.Control control ) throws DecoderException
    {
        @SuppressWarnings("rawtypes")
        ControlFactory factory = controlFactories.get( control.getID() );
        
        if ( factory == null )
        {
            OpaqueControl ourControl = new OpaqueControl( control.getID() );
            ourControl.setCritical( control.isCritical() );
            BasicControlDecorator<Control> decorator = 
                new BasicControlDecorator<Control>( this, ourControl );
            decorator.setValue( control.getEncodedValue() );
            return decorator;
        }
        
        @SuppressWarnings("unchecked")
        CodecControl<? extends Control> ourControl = factory.newCodecControl();
        ourControl.setCritical( control.isCritical() );
        ourControl.setValue( control.getEncodedValue() );
        ourControl.decode( control.getEncodedValue() );
        
        return ourControl;
    }


    /**
     * {@inheritDoc}
     */
    public Asn1Container newMessageContainer()
    {
        return new LdapMessageContainer<MessageDecorator<? extends Message>>( this );
    }


    /**
     * Gets the plugin directory containing codec extension bundles to load. 
     * If null, the service checks to see if system properties were used to 
     * specify the plugin directory. 
     *
     * @see {@link #PLUGIN_DIRECTORY_PROPERTY}
     * @return The directory containing plugins.
     */
    public File getPluginDirectory()
    {
        return pluginDirectory;
    }


    /**
     * {@inheritDoc}
     */
    public Iterator<String> registeredUnsolicitedResponses()
    {
        return Collections.unmodifiableSet( unsolicitedFactories.keySet() ).iterator();
    }


    /**
     * {@inheritDoc}
     */
    public UnsolicitedResponseFactory<?> registerUnsolicitedResponse( UnsolicitedResponseFactory<?> factory )
    {
        return unsolicitedFactories.put( factory.getOid(), factory );
    }


    /**
     * {@inheritDoc}
     */
    public javax.naming.ldap.ExtendedResponse toJndi( final ExtendedResponse modelResponse ) throws EncoderException
    {
        throw new NotImplementedException( "Figure out how to transform" );
    }
    

    /**
     * {@inheritDoc}
     */
    public ExtendedResponse fromJndi( javax.naming.ldap.ExtendedResponse jndiResponse ) throws DecoderException
    {   
        throw new NotImplementedException( "Figure out how to transform" );
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedRequestFactory<?, ?> unregisterExtendedRequest( String oid )
    {
        return extReqFactories.remove( oid );
    }


    /**
     * {@inheritDoc}
     */
    public UnsolicitedResponseFactory<?> unregisterUnsolicitedResponse( String oid )
    {
        return unsolicitedFactories.remove( oid );
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedRequest<?> fromJndi( javax.naming.ldap.ExtendedRequest jndiRequest ) throws DecoderException
    {
        ExtendedRequestDecorator<?,?> decorator =
            ( ExtendedRequestDecorator<?, ?> ) newExtendedRequest( jndiRequest.getID(), jndiRequest.getEncodedValue() );
        return decorator;
    }


    /**
     * {@inheritDoc}
     */
    public javax.naming.ldap.ExtendedRequest toJndi( final ExtendedRequest<?> modelRequest ) throws EncoderException
    {
        final String oid = modelRequest.getRequestName();
        final byte[] value;
        
        if ( modelRequest instanceof ExtendedRequestDecorator )
        {
            ExtendedRequestDecorator<?, ?> decorator = ( ExtendedRequestDecorator<?, ?> ) modelRequest;
            value = decorator.getRequestValue();
        }
        else
        {
            // have to ask the factory to decorate for us - can't do it ourselves
            ExtendedRequestFactory<?,?> extendedRequestFactory = extReqFactories.get( modelRequest.getRequestName() );
            ExtendedRequestDecorator<?, ?> decorator = extendedRequestFactory.decorate( modelRequest );
            value = decorator.getRequestValue();
        }
        
        
        javax.naming.ldap.ExtendedRequest jndiRequest = new javax.naming.ldap.ExtendedRequest()
        {
            private static final long serialVersionUID = -4160980385909987475L;

            public String getID()
            {
                return oid;
            }

            public byte[] getEncodedValue()
            {
                return value;
            }

            public javax.naming.ldap.ExtendedResponse createExtendedResponse( String id, byte[] berValue, int offset,
                int length ) throws NamingException
            {
                ExtendedRequestFactory<?,?> factory = extReqFactories.get( modelRequest.getRequestName() );
                
                try
                {
                    final ExtendedResponseDecorator<?> resp = ( ExtendedResponseDecorator<?> ) factory.newResponse( berValue );
                    javax.naming.ldap.ExtendedResponse jndiResponse = new javax.naming.ldap.ExtendedResponse()
                    {
                        private static final long serialVersionUID = -7686354122066100703L;

                        public String getID()
                        {
                            return oid;
                        }

                        public byte[] getEncodedValue()
                        {
                            return resp.getResponseValue();
                        }
                    };
                    
                    return jndiResponse;
                }
                catch ( DecoderException e )
                {
                    NamingException ne = new NamingException( "Unable to decode encoded response value: " + 
                        Strings.dumpBytes( berValue ) );
                    ne.setRootCause( e );
                    throw ne;
                }
            }
        };

        return jndiRequest;
    }


    /**
     * {@inheritDoc}
     * @throws DecoderException 
     */
    @SuppressWarnings("unchecked")
    public <E extends ExtendedResponse> E newExtendedResponse( ExtendedRequest<E> req, byte[] serializedResponse ) throws DecoderException
    {
        ExtendedResponseDecorator<ExtendedResponse> resp;
        
        ExtendedRequestFactory<?,?> extendedRequestFactory = extReqFactories.get( req.getRequestName() );
        
        if ( extendedRequestFactory != null )
        {
            resp = ( ExtendedResponseDecorator<ExtendedResponse> ) extendedRequestFactory.newResponse( serializedResponse );
        }
        else
        {
            resp = new ExtendedResponseDecorator<ExtendedResponse>( this, 
                new ExtendedResponseImpl( req.getRequestName() ) );
            resp.setResponseValue( serializedResponse );
            resp.setResponseName( req.getRequestName() );
        }
        
        resp.setMessageId( req.getMessageId() );
        
        return ( E ) resp;
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedRequest<?> newExtendedRequest( String oid, byte[] value )
    {
        ExtendedRequest<?> req = null;
        
        ExtendedRequestFactory<?,?> extendedRequestFactory = extReqFactories.get( oid );
        
        if ( extendedRequestFactory != null )
        {
            if ( value == null )
            {
                req = extendedRequestFactory.newRequest();
            }
            else
            {
                req = extendedRequestFactory.newRequest( value );
            }
        }
        else
        {
            ExtendedRequestDecorator<ExtendedRequest<ExtendedResponse>, ExtendedResponse> decorator = 
                new ExtendedRequestDecorator<ExtendedRequest<ExtendedResponse>, ExtendedResponse>( this, 
                    new ExtendedRequestImpl() );
            decorator.setRequestName( oid );
            decorator.setRequestValue( value );
            req = decorator;
        }
        
        return req;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public ExtendedRequestDecorator<?, ?> decorate( ExtendedRequest<?> decoratedMessage )
    {
        ExtendedRequestDecorator<?,?> req = null;
        
        ExtendedRequestFactory<?,?> extendedRequestFactory = extReqFactories.get( decoratedMessage.getRequestName() );
        
        if ( extendedRequestFactory != null )
        {
            req = extendedRequestFactory.decorate( decoratedMessage );
        }
        else
        {
            req = new ExtendedRequestDecorator<ExtendedRequest<ExtendedResponse>, ExtendedResponse>( this, 
                    ( ExtendedRequest<ExtendedResponse> ) decoratedMessage );
        }
        
        return req;
    }


    /**
     * {@inheritDoc}
     */
    public ExtendedResponseDecorator<?> decorate( ExtendedResponse decoratedMessage )
    {
        ExtendedResponseDecorator<?> resp = null;
        
        UnsolicitedResponseFactory<?> unsolicitedResponseFactory = unsolicitedFactories.get( decoratedMessage.getResponseName() );
        ExtendedRequestFactory<?,?> extendedRequestFactory = extReqFactories.get( decoratedMessage.getResponseName() );
        
        if ( extendedRequestFactory != null )
        {
            resp = extendedRequestFactory.decorate( decoratedMessage );
        }
        else if ( unsolicitedResponseFactory != null )
        {
            resp = unsolicitedResponseFactory.decorate( decoratedMessage );
        }
        else
        {
            resp = new ExtendedResponseDecorator<ExtendedResponse>( this, decoratedMessage );
        }
        
        return resp;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isExtendedOperationRegistered( String oid )
    {
        return extReqFactories.containsKey( oid ) || unsolicitedFactories.containsKey( oid );
    }
}
