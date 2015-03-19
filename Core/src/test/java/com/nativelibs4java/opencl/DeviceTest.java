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
        for (CLDevice subDevice : subDevices) {
        	long count = counts[i];
            assertEquals(count, subDevice.getMaxComputeUnits());
            checkParent(device, subDevice);
            i++;
        }
    }

    @Test
    public void testSplitByAffinity() {
        if (!device.getPartitionProperties().contains(CLDevice.PartitionType.ByAffinityDomain)) return;

        CLDevice[] subDevices = device.createSubDevicesByAffinity(CLDevice.AffinityDomain.NextPartitionable);
        assertTrue(subDevices.length > 1);
        for (CLDevice subDevice : subDevices) {
            checkParent(device, subDevice);
        }
    }

    private void checkParent(CLDevice parent, CLDevice child) {
        assertSame(device, child.getParent());
        // Force a get info CL_DEVICE_PARENT_DEVICE.
        assertSame(device, new CLDevice(device.getPlatform(), null, child.getEntity(), false).getParent());
    }

}
