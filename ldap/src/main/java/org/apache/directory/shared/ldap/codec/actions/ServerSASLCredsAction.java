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
package org.apache.directory.shared.ldap.codec.actions;


import org.apache.directory.shared.asn1.DecoderException;
import org.apache.directory.shared.asn1.ber.Asn1Container;
import org.apache.directory.shared.asn1.ber.grammar.GrammarAction;
import org.apache.directory.shared.asn1.ber.tlv.TLV;
import org.apache.directory.shared.ldap.codec.LdapMessageContainer;
import org.apache.directory.shared.ldap.message.BindResponse;
import org.apache.directory.shared.ldap.util.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store a SASL credentials
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ServerSASLCredsAction extends GrammarAction
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( ServerSASLCredsAction.class );

    /** Speedup for logs */
    private static final boolean IS_DEBUG = LOG.isDebugEnabled();


    /**
     * Instantiates a new server sasl creds action.
     */
    public ServerSASLCredsAction()
    {
        super( "Store server sasl credentials value" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( Asn1Container container ) throws DecoderException
    {
        LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

        // Get the Value and store it in the BindRequest
        TLV tlv = ldapMessageContainer.getCurrentTLV();

        // We have to handle the special case of a 0 length server
        // sasl credentials
        byte[] serverSaslCreds = null;

        if ( tlv.getLength() == 0 )
        {
            serverSaslCreds = StringTools.EMPTY_BYTES;
        }
        else
        {
            serverSaslCreds = tlv.getValue().getData();
        }

        BindResponse response = ( BindResponse ) ldapMessageContainer.getMessage();
        response.setServerSaslCreds( serverSaslCreds );

        // We can have an END transition
        ldapMessageContainer.setGrammarEndAllowed( true );

        if ( IS_DEBUG )
        {
            LOG.debug( "The SASL credentials value is : {}", StringTools.dumpBytes( serverSaslCreds ) );
        }
    }
}
