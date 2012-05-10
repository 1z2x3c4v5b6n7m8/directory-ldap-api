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
package org.apache.directory.shared.ldap.model.schema.comparators;


import org.apache.directory.shared.ldap.model.schema.normalizers.CachingNormalizer;
import org.apache.directory.shared.ldap.model.schema.normalizers.DeepTrimNormalizer;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;


/**
 * TODO DeepTrimCachingNormalizingComparator.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@Component
public class DeepTrimCachingNormalizingComparator extends NormalizingComparator
{
    /** The serial version UID */
    private static final long serialVersionUID = 2L;


    @SuppressWarnings(
        { "rawtypes", "unchecked" })
    public DeepTrimCachingNormalizingComparator( @Property(name = "ads.comp.comparator.oid") String oid )
    {
        super( oid, new CachingNormalizer( new DeepTrimNormalizer( oid ), 10 ),
            new ComparableComparator( oid ) );
    }
}
