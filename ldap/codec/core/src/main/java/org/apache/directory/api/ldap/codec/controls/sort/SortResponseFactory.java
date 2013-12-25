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
package org.apache.directory.api.ldap.codec.controls.sort;


import org.apache.directory.api.ldap.codec.api.ControlFactory;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.SortResponseControl;


/**
 * A {@link ControlFactory} for SortResponseControl.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * @version $Rev$, $Date$
 */
public class SortResponseFactory implements ControlFactory<SortResponseControl, SortResponseDecorator>
{
    /** The LDAP codec service */
    private LdapApiService codec;


    /**
     * Creates a new instance of SortResponseFactory.
     *
     * @param codec The LDAP codec.
     */
    public SortResponseFactory( LdapApiService codec )
    {
        this.codec = codec;
    }


    /**
     * {@inheritDoc}
     */
    public String getOid()
    {
        return SortResponseControl.OID;
    }


    /**
     * {@inheritDoc}
     */
    public SortResponseDecorator newCodecControl()
    {
        return new SortResponseDecorator( codec );
    }


    /**
     * {@inheritDoc}
     */
    public SortResponseDecorator newCodecControl( SortResponseControl control )
    {
        return new SortResponseDecorator( codec, control );
    }
}
