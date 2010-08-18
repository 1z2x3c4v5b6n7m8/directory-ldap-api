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


import org.apache.directory.shared.asn1.ber.IAsn1Container;
import org.apache.directory.shared.asn1.ber.grammar.GrammarAction;
import org.apache.directory.shared.asn1.ber.tlv.TLV;
import org.apache.directory.shared.asn1.codec.DecoderException;
import org.apache.directory.shared.i18n.I18n;
import org.apache.directory.shared.ldap.codec.LdapMessageContainer;
import org.apache.directory.shared.ldap.message.ReferralImpl;
import org.apache.directory.shared.ldap.message.internal.Referral;
import org.apache.directory.shared.ldap.message.internal.LdapResult;
import org.apache.directory.shared.ldap.message.internal.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to init referrals to a LdapTresult
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitReferralsAction extends GrammarAction
{
    /** The logger */
    private static final Logger log = LoggerFactory.getLogger( InitReferralsAction.class );

    /** Speedup for logs */
    private static final boolean IS_DEBUG = log.isDebugEnabled();


    public InitReferralsAction()
    {
        super( "Init the referrals list" );
    }


    /**
     * The initialization action
     */
    public void action( IAsn1Container container ) throws DecoderException
    {
        LdapMessageContainer ldapMessageContainer = ( LdapMessageContainer ) container;

        TLV tlv = ldapMessageContainer.getCurrentTLV();

        // If we hae a Referrals sequence, then it should not be empty
        if ( tlv.getLength() == 0 )
        {
            String msg = I18n.err( I18n.ERR_04011 );
            log.error( msg );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( msg );
        }

        ResultResponse response = ( ResultResponse ) ldapMessageContainer.getMessage();
        LdapResult ldapResult = response.getLdapResult();

        Referral referral = new ReferralImpl();
        ldapResult.setReferral( referral );

        if ( IS_DEBUG )
        {
            log.debug( "Initialising a referrals list" );
        }
    }
}
