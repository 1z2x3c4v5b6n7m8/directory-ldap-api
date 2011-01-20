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
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.codec.LdapMessageContainer;
import org.apache.directory.shared.ldap.codec.util.LdapURLEncodingException;
import org.apache.directory.shared.ldap.message.Referral;
import org.apache.directory.shared.ldap.message.ReferralImpl;
import org.apache.directory.shared.ldap.message.SearchResultReference;
import org.apache.directory.shared.ldap.util.LdapURL;
import org.apache.directory.shared.ldap.util.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store a reference into a searchResultReference
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreReferenceAction extends GrammarAction
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreReferenceAction.class );

    /** Speedup for logs */
    private static final boolean IS_DEBUG = LOG.isDebugEnabled();


    /**
     * Instantiates a new store reference action.
     */
    public StoreReferenceAction()
    {
        super( "Store a reference" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( Asn1Container container ) throws DecoderException
    {

        LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

        SearchResultReference searchResultReference = ldapMessageContainer.getSearchResultReference();

        // Get the Value and store it in the BindRequest
        TLV tlv = ldapMessageContainer.getCurrentTLV();

        // Get the referral, or create it if not existing
        Referral referral = searchResultReference.getReferral();

        if ( referral == null )
        {
            referral = new ReferralImpl();
            searchResultReference.setReferral( referral );
        }

        // We have to handle the special case of a 0 length list of referrals
        LdapURL url = LdapURL.EMPTY_URL;

        if ( tlv.getLength() == 0 )
        {
            referral.addLdapUrl( "" );
        }
        else
        {
            String urlStr = StringTools.utf8ToString( tlv.getValue().getData() );

            try
            {
                url = new LdapURL( urlStr );
                referral.addLdapUrl( urlStr );
            }
            catch ( LdapURLEncodingException luee )
            {
                LOG.error( I18n.err( I18n.ERR_04021, urlStr, luee.getMessage() ) );
                throw new DecoderException( I18n.err( I18n.ERR_04016, luee.getMessage() ) );
            }
        }

        if ( IS_DEBUG )
        {
            LOG.debug( "Search reference URL found : {}", url );
        }

        // We can have an END transition
        ldapMessageContainer.setGrammarEndAllowed( true );
    }
}
