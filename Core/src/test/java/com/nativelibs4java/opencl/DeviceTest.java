/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.nativelibs4java.opencl;

import static com.nativelibs4java.util.NIOUtils.directBuffer;
import static com.nativelibs4java.util.NIOUtils.get;
import static com.nativelibs4java.util.NIOUtils.put;
import static org.junit.Assert.*;

import java.nio.*;

import org.junit.*;

import com.nativelibs4java.util.NIOUtils;
import org.bridj.*;
import java.nio.ByteOrder;
import static org.bridj.Pointer.*;
import java.nio.ByteOrder;
import java.util.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author ochafik
 */
@RunWith(Parameterized.class)
public class DeviceTest {
    private final CLDevice device;

    public DeviceTest(CLDevice device) {
        this.device = device;

        System.out.println(device);
        System.out.println("\tmax sub-devices: " + device.getPartitionMaxSubDevices());
        System.out.println("\tpartition properties: " + device.getPartitionProperties());
        System.out.println("\tpartition domains: " + device.getPartitionAffinityDomains());
    }
    
    @Parameterized.Parameters
    public static List<Object[]> getDeviceParameters() {
        List<Object[]> ret = new ArrayList<Object[]>();
        for (CLPlatform platform : JavaCL.listPlatforms()) {
            for (CLDevice device : platform.listAllDevices(true)) {
                ret.add(new Object[] { device });
            }
        }
        return ret;
    }

    @Test
    public void testSplitEqually() {
        if (!device.getPartitionProperties().contains(CLDevice.PartitionType.Equally)) return;

        int computeUnits = device.getMaxComputeUnits();
        System.out.println("computeUnits = " + computeUnits);
        int subComputeUnits = computeUnits / 2;
        
        CLDevice[] subDevices = device.createSubDevicesEqually(subComputeUnits);
        for (CLDevice subDevice : subDevices) {
            assertEquals(subComputeUnits, subDevice.getMaxComputeUnits());
            checkParent(device, subDevice);
        }
    }

    @Test
    public void testSplitByCounts() {
        if (!device.getPartitionProperties().contains(CLDevice.PartitionType.ByCounts)) return;

        long[] counts = new long[] { 2, 4, 8 };
        CLDevice[] subDevices = device.createSubDevicesByCounts(counts);
        assertEquals(counts.length, subDevices.length);
        int i = 0;
        long[] actualCounts = new long[counts.length];
        for (CLDevice subDevice : subDevices) {
            actualCounts[i] = subDevice.getMaxComputeUnits();
            checkParent(device, subDevice);
            i++;
        }
        Arrays.sort(actualCounts);
        assertArrayEquals(counts, actualCounts);
    }

    public void checkSplitByAffinity(CLDevice.AffinityDomain domain) {
        if (!device.getPartitionProperties().contains(CLDevice.PartitionType.ByAffinityDomain)) {
          System.out.println("Split by affinity is not supported by device " + device);
          return;
        }
        if (!device.getPartitionAffinityDomains().contains(domain)) {
          System.out.println("Affinity domain " + domain + " not supported by device " + device);
          return;
        }

        CLDevice[] subDevices = device.createSubDevicesByAffinity(domain);
        assertTrue(subDevices.length > 1);
        for (CLDevice subDevice : subDevices) {
            checkParent(device, subDevice);
            assertEquals(domain, subDevice.getPartitionAffinityDomain());
        }
    }

    @Test
    public void testSplitByAffinity_NUMA() {
      checkSplitByAffinity(CLDevice.AffinityDomain.NUMA);
    }
    @Test
    public void testSplitByAffinity_L4Cache() {
      checkSplitByAffinity(CLDevice.AffinityDomain.L4Cache);
    }
    @Test
    public void testSplitByAffinity_L3Cache() {
      checkSplitByAffinity(CLDevice.AffinityDomain.L3Cache);
    }
    @Test
    public void testSplitByAffinity_L2Cache() {
      checkSplitByAffinity(CLDevice.AffinityDomain.L2Cache);
    }
    @Test
    public void testSplitByAffinity_L1Cache() {
      checkSplitByAffinity(CLDevice.AffinityDomain.L1Cache);
    }
    @Test
    public void testSplitByAffinity_NextPartitionable() {
      checkSplitByAffinity(CLDevice.AffinityDomain.NextPartitionable);
    }

    private void checkParent(CLDevice parent, CLDevice child) {
        assertSame(parent, child.getParent());
        // Force a get info CL_DEVICE_PARENT_DEVICE.
        assertEquals(parent, new CLDevice(device.getPlatform(), null, child.getEntity(), false).getParent());
    }

}
