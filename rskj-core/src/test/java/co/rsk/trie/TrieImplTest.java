/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.trie;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by ajlopez on 22/08/2016.
 */
public class TrieImplTest {
    @Test
    public void bytesToKey() {
        Assert.assertArrayEquals(new byte[] { 0x01, 0x00, 0x01, 0x00, 0x01, 0x00, 0x01, 0x00 }, TrieImpl.bytesToKey(new byte[] { (byte)0xaa }, 2));
        Assert.assertArrayEquals(new byte[] { 0x0a, 0x0a }, TrieImpl.bytesToKey(new byte[] { (byte)0xaa }, 16));
        Assert.assertArrayEquals(new byte[] { 0x02, 0x02, 0x02, 0x02 }, TrieImpl.bytesToKey(new byte[] { (byte)0xaa }, 4));
    }
}

