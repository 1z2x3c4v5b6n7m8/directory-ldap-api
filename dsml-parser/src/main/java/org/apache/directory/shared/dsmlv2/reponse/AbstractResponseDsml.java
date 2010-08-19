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


import org.apache.directory.shared.dsmlv2.DsmlDecorator;
import org.apache.directory.shared.ldap.message.Message;
import org.apache.directory.shared.ldap.message.MessageException;
import org.apache.directory.shared.ldap.message.control.Control;
import org.dom4j.Element;


/**
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractResponseDsml extends LdapResponseDecorator implements DsmlDecorator
{

    public AbstractResponseDsml( Message ldapMessage )
    {
        super( ldapMessage );
        // TODO Auto-generated constructor stub
    }


    public abstract Element toDsml( Element root );


    public void addAllControls( Control[] controls ) throws MessageException
    {
        // TODO Auto-generated method stub

    }


    public Object get( Object key )
    {
        // TODO Auto-generated method stub
        return null;
    }


    public Control getCurrentControl()
    {
        // TODO Auto-generated method stub
        return null;
    }


    public boolean hasControl( String oid )
    {
        // TODO Auto-generated method stub
        return false;
    }


    public Object put( Object key, Object value )
    {
        // TODO Auto-generated method stub
        return null;
    }


    public void removeControl( Control control ) throws MessageException
    {
        // TODO Auto-generated method stub

    }

}
