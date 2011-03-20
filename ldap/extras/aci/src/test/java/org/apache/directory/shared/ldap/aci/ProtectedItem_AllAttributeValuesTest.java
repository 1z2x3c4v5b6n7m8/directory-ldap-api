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
package org.apache.directory.shared.ldap.aci;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashSet;
import java.util.Set;

import com.mycila.junit.concurrent.Concurrency;
import com.mycila.junit.concurrent.ConcurrentJunitRunner;
import org.apache.directory.shared.ldap.aci.protectedItem.AllAttributeValuesItem;
import org.apache.directory.shared.ldap.model.schema.MutableAttributeTypeImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Unit tests class ProtectedItem.AllAttributeValues.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrency()
public class ProtectedItem_AllAttributeValuesTest
{
    AllAttributeValuesItem allAttributeValuesA;
    AllAttributeValuesItem allAttributeValuesACopy;
    AllAttributeValuesItem allAttributeValuesB;
    AllAttributeValuesItem allAttributeValuesC;


    /**
     * Initialize name instances
     */
    @Before
    public void initNames() throws Exception
    {
        Set<MutableAttributeTypeImpl> colA = new HashSet<MutableAttributeTypeImpl>();
        colA.add( new MutableAttributeTypeImpl( "aa" ) );
        colA.add( new MutableAttributeTypeImpl( "bb" ) );
        colA.add( new MutableAttributeTypeImpl( "cc" ) );
        Set<MutableAttributeTypeImpl> colB = new HashSet<MutableAttributeTypeImpl>();
        colB.add( new MutableAttributeTypeImpl( "aa" ) );
        colB.add( new MutableAttributeTypeImpl( "bb" ) );
        colB.add( new MutableAttributeTypeImpl( "cc" ) );
        Set<MutableAttributeTypeImpl> colC = new HashSet<MutableAttributeTypeImpl>();
        colC.add( new MutableAttributeTypeImpl( "bb" ) );
        colC.add( new MutableAttributeTypeImpl( "cc" ) );
        colC.add( new MutableAttributeTypeImpl( "dd" ) );

        allAttributeValuesA = new AllAttributeValuesItem( colA );
        allAttributeValuesACopy = new AllAttributeValuesItem( colA );
        allAttributeValuesB = new AllAttributeValuesItem( colB );
        allAttributeValuesC = new AllAttributeValuesItem( colC );
    }


    @Test
    public void testEqualsNull() throws Exception
    {
        assertFalse( allAttributeValuesA.equals( null ) );
    }


    @Test
    public void testEqualsReflexive() throws Exception
    {
        assertEquals( allAttributeValuesA, allAttributeValuesA );
    }


    @Test
    public void testHashCodeReflexive() throws Exception
    {
        assertEquals( allAttributeValuesA.hashCode(), allAttributeValuesA.hashCode() );
    }


    @Test
    public void testEqualsSymmetric() throws Exception
    {
        assertEquals( allAttributeValuesA, allAttributeValuesACopy );
        assertEquals( allAttributeValuesACopy, allAttributeValuesA );
    }


    @Test
    public void testHashCodeSymmetric() throws Exception
    {
        assertEquals( allAttributeValuesA.hashCode(), allAttributeValuesACopy.hashCode() );
        assertEquals( allAttributeValuesACopy.hashCode(), allAttributeValuesA.hashCode() );
    }


    @Test
    public void testEqualsTransitive() throws Exception
    {
        assertEquals( allAttributeValuesA, allAttributeValuesACopy );
        assertEquals( allAttributeValuesACopy, allAttributeValuesB );
        assertEquals( allAttributeValuesA, allAttributeValuesB );
    }


    @Test
    public void testHashCodeTransitive() throws Exception
    {
        assertEquals( allAttributeValuesA.hashCode(), allAttributeValuesACopy.hashCode() );
        assertEquals( allAttributeValuesACopy.hashCode(), allAttributeValuesB.hashCode() );
        assertEquals( allAttributeValuesA.hashCode(), allAttributeValuesB.hashCode() );
    }


    @Test
    public void testNotEqualDiffValue() throws Exception
    {
        assertFalse( allAttributeValuesA.equals( allAttributeValuesC ) );
        assertFalse( allAttributeValuesC.equals( allAttributeValuesA ) );
    }
}
