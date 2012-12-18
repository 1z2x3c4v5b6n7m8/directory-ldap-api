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
package org.apache.directory.shared.ldap.extras.extended.ads_impl.gracefulShutdown;


import org.apache.directory.api.asn1.ber.grammar.AbstractGrammar;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.grammar.GrammarTransition;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoder;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoderException;
import org.apache.directory.api.asn1.ber.tlv.UniversalTag;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.shared.asn1.DecoderException;
import org.apache.directory.shared.ldap.extras.extended.ads_impl.gracefulDisconnect.GracefulActionConstants;
import org.apache.directory.shared.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class implements the Graceful shutdown. All the actions are declared in
 * this class. As it is a singleton, these declaration are only done once. The
 * grammar is :
 * 
 * <pre>
 *  GracefulShutdwon ::= SEQUENCE {
 *      timeOffline INTEGER (0..720) DEFAULT 0,
 *      delay [0] INTEGER (0..86400) DEFAULT 0
 *  }
 * </pre>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public final class GracefulShutdownGrammar extends AbstractGrammar<GracefulShutdownContainer>
{
    /** The logger */
    static final Logger LOG = LoggerFactory.getLogger( GracefulShutdownGrammar.class );

    /** Speedup for logs */
    static final boolean IS_DEBUG = LOG.isDebugEnabled();

    /** The instance of grammar. GracefulShutdownGrammar is a singleton */
    private static GracefulShutdownGrammar instance = new GracefulShutdownGrammar();


    /**
     * Creates a new GracefulShutdownGrammar object.
     */
    @SuppressWarnings("unchecked")
    private GracefulShutdownGrammar()
    {
        setName( GracefulShutdownGrammar.class.getName() );

        // Create the transitions table
        super.transitions = new GrammarTransition[GracefulShutdownStatesEnum.LAST_GRACEFUL_SHUTDOWN_STATE.ordinal()][256];

        /**
         * Transition from init state to graceful shutdown
         * 
         * GracefulShutdown ::= SEQUENCE {
         *     ...
         *     
         * Creates the GracefulShutdown object
         */
        super.transitions[GracefulShutdownStatesEnum.START_STATE.ordinal()][UniversalTag.SEQUENCE.getValue()] =
            new GrammarTransition<GracefulShutdownContainer>( GracefulShutdownStatesEnum.START_STATE,
                GracefulShutdownStatesEnum.GRACEFUL_SHUTDOWN_SEQUENCE_STATE,
                UniversalTag.SEQUENCE.getValue(),
                new GrammarAction<GracefulShutdownContainer>( "Init GracefulShutdown" )
                {
                    public void action( GracefulShutdownContainer container )
                    {
                        GracefulShutdown gracefulShutdown = new GracefulShutdown();
                        container.setGracefulShutdown( gracefulShutdown );
                        container.setGrammarEndAllowed( true );
                    }
                } );

        /**
         * Transition from graceful shutdown to time offline
         *
         * GracefulShutdown ::= SEQUENCE { 
         *     timeOffline INTEGER (0..720) DEFAULT 0,
         *     ...
         *     
         * Set the time offline value into the GracefulShutdown
         * object.
         */
        super.transitions[GracefulShutdownStatesEnum.GRACEFUL_SHUTDOWN_SEQUENCE_STATE.ordinal()][UniversalTag.INTEGER
            .getValue()] =
            new GrammarTransition<GracefulShutdownContainer>(
                GracefulShutdownStatesEnum.GRACEFUL_SHUTDOWN_SEQUENCE_STATE,
                GracefulShutdownStatesEnum.TIME_OFFLINE_STATE,
                UniversalTag.INTEGER.getValue(),
                new GrammarAction<GracefulShutdownContainer>( "Set Graceful Shutdown time offline" )
                {
                    public void action( GracefulShutdownContainer container ) throws DecoderException
                    {
                        BerValue value = container.getCurrentTLV().getValue();

                        try
                        {
                            int timeOffline = IntegerDecoder.parse( value, 0, 720 );

                            if ( IS_DEBUG )
                            {
                                LOG.debug( "Time Offline = " + timeOffline );
                            }

                            container.getGracefulShutdown().setTimeOffline( timeOffline );
                            container.setGrammarEndAllowed( true );
                        }
                        catch ( IntegerDecoderException e )
                        {
                            String msg = I18n.err( I18n.ERR_04037, Strings.dumpBytes( value.getData() ) );
                            LOG.error( msg );
                            throw new DecoderException( msg );
                        }
                    }
                } );

        /**
         * Transition from time offline to delay
         * 
         * GracefulShutdown ::= SEQUENCE { 
         *     ... 
         *     delay [0] INTEGER (0..86400) DEFAULT 0 }
         * 
         * Set the delay value into the GracefulShutdown
         * object.
         */
        super.transitions[GracefulShutdownStatesEnum.TIME_OFFLINE_STATE.ordinal()][GracefulActionConstants.GRACEFUL_ACTION_DELAY_TAG] =
            new GrammarTransition<GracefulShutdownContainer>( GracefulShutdownStatesEnum.TIME_OFFLINE_STATE,
                GracefulShutdownStatesEnum.DELAY_STATE,
                GracefulActionConstants.GRACEFUL_ACTION_DELAY_TAG,

                new GrammarAction<GracefulShutdownContainer>( "Set Graceful Shutdown Delay" )
                {
                    public void action( GracefulShutdownContainer container ) throws DecoderException
                    {
                        BerValue value = container.getCurrentTLV().getValue();

                        try
                        {
                            int delay = IntegerDecoder.parse( value, 0, 86400 );

                            if ( IS_DEBUG )
                            {
                                LOG.debug( "Delay = " + delay );
                            }

                            container.getGracefulShutdown().setDelay( delay );
                            container.setGrammarEndAllowed( true );
                        }
                        catch ( IntegerDecoderException e )
                        {
                            String msg = I18n.err( I18n.ERR_04036, Strings.dumpBytes( value.getData() ) );
                            LOG.error( msg );
                            throw new DecoderException( msg );
                        }
                    }
                } );

        /**
         * Transition from graceful shutdown to delay
         * 
         * GracefulShutdown ::= SEQUENCE { 
         *     ... 
         *     delay [0] INTEGER (0..86400) DEFAULT 0 }
         * 
         * Set the delay value into the GracefulShutdown
         * object.
         */
        super.transitions[GracefulShutdownStatesEnum.GRACEFUL_SHUTDOWN_SEQUENCE_STATE.ordinal()][GracefulActionConstants.GRACEFUL_ACTION_DELAY_TAG] =
            new GrammarTransition<GracefulShutdownContainer>(
                GracefulShutdownStatesEnum.GRACEFUL_SHUTDOWN_SEQUENCE_STATE,
                GracefulShutdownStatesEnum.DELAY_STATE,
                GracefulActionConstants.GRACEFUL_ACTION_DELAY_TAG,

                new GrammarAction<GracefulShutdownContainer>( "Set Graceful Shutdown Delay" )
                {
                    public void action( GracefulShutdownContainer container ) throws DecoderException
                    {
                        GracefulShutdownContainer gracefulShutdownContainer = ( GracefulShutdownContainer ) container;
                        BerValue value = gracefulShutdownContainer.getCurrentTLV().getValue();

                        try
                        {
                            int delay = IntegerDecoder.parse( value, 0, 86400 );

                            if ( IS_DEBUG )
                            {
                                LOG.debug( "Delay = " + delay );
                            }

                            gracefulShutdownContainer.getGracefulShutdown().setDelay( delay );
                            gracefulShutdownContainer.setGrammarEndAllowed( true );
                        }
                        catch ( IntegerDecoderException e )
                        {
                            String msg = I18n.err( I18n.ERR_04036, Strings.dumpBytes( value.getData() ) );
                            LOG.error( msg );
                            throw new DecoderException( msg );
                        }
                    }
                } );
    }


    /**
     * This class is a singleton.
     * 
     * @return An instance on this grammar
     */
    public static GracefulShutdownGrammar getInstance()
    {
        return instance;
    }
}
