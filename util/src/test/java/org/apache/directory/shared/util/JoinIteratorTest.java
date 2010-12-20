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
package org.apache.directory.shared.util;


import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.directory.junit.tools.Concurrent;
import org.apache.directory.junit.tools.ConcurrentJunitRunner;
import org.apache.directory.shared.util.JoinIterator;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Document this class.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@RunWith(ConcurrentJunitRunner.class)
@Concurrent()
public class JoinIteratorTest
{
    @Test
    public void testNullArgument()
    {
        try
        {
            new JoinIterator( null );
            Assert.fail("Should not be able to create a JoinIterator with null args");
        }
        catch ( IllegalArgumentException e )
        {
            Assert.assertNotNull(e);
        }
    }


    @Test
    public void testSingleArgument()
    {
        Iterator<?>[] iterators = new Iterator<?>[]
            { Collections.singleton( "foo" ).iterator() };

        try
        {
            new JoinIterator( iterators );
            Assert.fail("Should not be able to create a JoinIterator with a single Iterator");
        }
        catch ( IllegalArgumentException e )
        {
            Assert.assertNotNull(e);
        }
    }


    @Test
    public void testTwoArguments()
    {
        Iterator<?>[] iterators = new Iterator<?>[]
            { Collections.singleton( "foo" ).iterator(), Collections.singleton( "bar" ).iterator() };

        JoinIterator iterator = new JoinIterator( iterators );
        Assert.assertTrue("iterator should have an element", iterator.hasNext());
        Assert.assertEquals("foo", iterator.next());
        Assert.assertTrue("iterator should have an element", iterator.hasNext());
        Assert.assertEquals("bar", iterator.next());
        Assert.assertFalse("iterator should NOT have an element", iterator.hasNext());
    }


    @Test
    public void testSeveralArguments()
    {
        List<String> multivalued = new ArrayList<String>();
        multivalued.add( "foo1" );
        multivalued.add( "foo2" );

        Iterator<?>[] iterators = new Iterator<?>[]
            { Collections.singleton( "foo0" ).iterator(), multivalued.iterator(),
                Collections.singleton( "bar0" ).iterator(), Collections.singleton( "bar1" ).iterator() };

        JoinIterator iterator = new JoinIterator( iterators );
        Assert.assertTrue("iterator should have an element", iterator.hasNext());
        Assert.assertEquals("foo0", iterator.next());
        Assert.assertTrue("iterator should have an element", iterator.hasNext());
        Assert.assertEquals("foo1", iterator.next());
        Assert.assertTrue("iterator should have an element", iterator.hasNext());
        Assert.assertEquals("foo2", iterator.next());
        Assert.assertTrue("iterator should have an element", iterator.hasNext());
        Assert.assertEquals("bar0", iterator.next());
        Assert.assertTrue("iterator should have an element", iterator.hasNext());
        Assert.assertEquals("bar1", iterator.next());
        Assert.assertFalse("iterator should NOT have an element", iterator.hasNext());
    }
}
