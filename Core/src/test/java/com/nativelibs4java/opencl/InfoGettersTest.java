/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nativelibs4java.opencl;

import static org.junit.Assert.assertFalse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author ochafik
 */
public class InfoGettersTest {
    @Before
    public void gc() {
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(InfoGettersTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.gc();
    }

    CLProgram createProgram() {
        CLProgram pg = createContext().createProgram("__kernel void f(__global int* a) {}");
        try {
            pg.build();
        } catch (CLBuildException ex) {
            assertFalse(ex.toString(), true);
        }
        return pg;
    }

    CLPlatform createPlatform() {
        return JavaCL.listPlatforms()[0];
    }

    CLDevice createDevice() {
        return createPlatform().listAllDevices(true)[0];
    }

    CLContext ctx;
    CLContext createContext() {
        if (ctx == null)
            ctx = createPlatform().createContext(null, createDevice());
        return ctx;
    }

    CLKernel createKernel() {
        try {
            return createProgram().createKernels()[0];
        } catch (CLBuildException ex) {
            throw new RuntimeException("Failed to create kernel", ex);
        }
    }

    CLEvent createEvent() {
        CLContext c = createContext();
        return c.createBuffer(CLMem.Usage.Input, Integer.class, 10).mapLater(c.createDefaultQueue(), CLMem.MapFlags.Read).getSecond();
    }

    CLSampler createSampler() {
        return createContext().createSampler(true, CLSampler.AddressingMode.ClampToEdge, CLSampler.FilterMode.Linear);
    }

    CLQueue createQueue() {
        CLContext c = createContext();
        CLDevice d = c.getDevices()[0];
        return d.createQueue(c);
    }

    @org.junit.Test
    public void CLProgramGetters() {
        testGetters(createProgram());
    }

    @org.junit.Test
    public void CLKernelGetters() {
        testGetters(createKernel());
    }

    @org.junit.Test
    public void CLMemGetters() {
        testGetters(createContext().createBuffer(CLMem.Usage.Input, Byte.class, 10));
        testGetters(createContext().createBuffer(CLMem.Usage.Output, Byte.class, 10));
    }

    @org.junit.Test
    public void CLQueueGetters() {
        testGetters(createQueue());
    }

    @org.junit.Test
    public void CLDeviceGetters() {
        testGetters(createDevice());
    }

    @org.junit.Test
    public void CLPlatformGetters() {
        testGetters(createPlatform());
    }

    @org.junit.Test
    public void CLContextGetters() {
        testGetters(createContext());
    }

    @org.junit.Test
    public void CLEventGetters() {
        testGetters(createEvent());
    }

    @org.junit.Test
    public void CLSamplerGetters() {
        testGetters(createSampler());
    }

    public static void testGetters(Object instance) {
      if (instance == null)
          return;
      Logger log = Logger.getLogger(instance.getClass().getName());
      for (Method m : instance.getClass().getDeclaredMethods()) {
        if (Modifier.isStatic(m.getModifiers()))
          continue;
        if (!Modifier.isPublic(m.getModifiers()))
          continue;
        if (m.getParameterTypes().length != 0)
          continue;

        String name = m.getName();
        if (name.contains("ProfilingCommand"))
          continue;
        
        boolean isToString = name.equals("toString");
        if (name.startsWith("get") && name.length() > 3 ||
            name.startsWith("has") && name.length() > 3 ||
            name.startsWith("is") && name.length() > 2 ||
            isToString && !Modifier.isPublic(m.getDeclaringClass().getModifiers()))
        {
          String msg = "Failed to call " + m;
          try {
            m.invoke(instance);
          } catch (IllegalAccessException ex) {
            if (!isToString)
              log.log(Level.WARNING, msg, ex);
          } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (!(cause instanceof UnsupportedOperationException)) {
              log.log(Level.SEVERE, msg, ex.getCause());
              assertFalse(msg, true);
            }
          } catch (Exception ex) {
            log.log(Level.SEVERE, msg, ex);
          }
        }
      }
    }
}
