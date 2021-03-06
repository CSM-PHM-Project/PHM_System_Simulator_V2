/*
 * SPEARS: Simulated Physics and Environment for Autonomous Risk Studies
 * Copyright (C) 2017  Colorado School of Mines
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.spears.wrapper;

import com.spears.objects.SynchronousThread;
import com.spears.objects.ThreadItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.laughingpanda.beaninject.Inject;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;

import static org.mockito.Mockito.*;

public class GlobalsThreadTest {

    @Before
    public void resetGlobal(){
        Inject.field("singleton_instance").of(Globals.getInstance()).with(null);
        Globals.getInstance();
    }

    @Test
    public void testRegister(){
        TreeMap<String, ThreadItem> threadMap = populateThreadMap();
        SynchronousThread threadMock = mock(SynchronousThread.class);
        String name = "test";
        int delay = 500;
        Globals.getInstance().registerNewThread(name, delay, threadMock);
        assert threadMap.containsKey(name);
        Assert.assertEquals(delay, threadMap.get(name).getDelay());
    }

    @Test
    public void testCheckOut(){
        TreeMap<String, ThreadItem> threadMap = populateThreadMap();
        ThreadItem itemMock = mock(ThreadItem.class);
        threadMap.put("test", itemMock);
        Globals.getInstance().checkOutThread("test");
        assert !threadMap.containsKey("test");
        verify(itemMock, atLeastOnce()).markFinished();
    }

    @Test
    public void testKill() throws InterruptedException {
        TreeMap<String, ThreadItem> threadMap = populateThreadMap();
        ThreadItem itemMock = spy(new ThreadItem("test", 2, 0,
                new SynchronousThread(2, () -> {}, SynchronousThread.FOREVER, "test")));
        threadMap.put("test", itemMock);
        Globals.getInstance().killThread("test");
        Thread.sleep(150);
        assert !threadMap.containsKey("test");
        verify(itemMock, atLeastOnce()).killThread();
    }

    @Test
    public void testCheckInNormal(){
        Map<String, ThreadItem> threads = populateThreadMap();
        ThreadItem item = mock(ThreadItem.class);
        threads.put("test", item);
        Globals.getInstance().threadCheckIn("test");
        verify(item).markFinished();
        verify(item).advance();;
    }

    @Test
    public void testCheckInClockNotDone() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ThreadItem> threads = populateThreadMap();
        ThreadItem item1 = mock(ThreadItem.class);
        threads.put("test1", item1);
        when(item1.isFinished()).thenReturn(true);
        ThreadItem item2 = mock(ThreadItem.class);
        threads.put("test2", item2);
        when(item2.isFinished()).thenReturn(false);
        long time = Globals.getInstance().timeMillis();

        Globals.getInstance().threadCheckIn("milli-clock");
        verify(item2, atLeastOnce()).shakeThread();
        assert getMilliDone();
        verify(item1, never()).reset();
        verify(item2, never()).grantPermission();
        Assert.assertEquals(time, Globals.getInstance().timeMillis());
    }

    @Test
    public void testCheckInAllDone(){
        long time = Globals.getInstance().timeMillis();
        Map<String, ThreadItem> threads = populateThreadMap();
        ThreadItem item1 = mock(ThreadItem.class);
        threads.put("test1", item1);
        when(item1.isFinished()).thenReturn(true);
        when(item1.getNext()).thenReturn(time+1);
        ThreadItem item2 = mock(ThreadItem.class);
        threads.put("test2", item2);
        when(item2.isFinished()).thenReturn(true);
        when(item2.getNext()).thenReturn(3*(time+2));

        Globals.getInstance().threadCheckIn("milli-clock");
        Assert.assertEquals(time+1, Globals.getInstance().timeMillis());
        verify(item1).reset();
        verify(item1).grantPermission();
        verify(item2).reset();
        verify(item2, never()).grantPermission();
    }

    @Test
    public void testCheckInMilliDone(){
        Inject.field("milliDone").of(Globals.getInstance()).with(true);
        Map<String, ThreadItem> threads = populateThreadMap();
        long time = Globals.getInstance().timeMillis();
        ThreadItem item1 = mock(ThreadItem.class);
        threads.put("test1", item1);
        when(item1.isFinished()).thenReturn(true);
        when(item1.getNext()).thenReturn(time+1);
        ThreadItem item2 = mock(ThreadItem.class);
        threads.put("test2", item2);
        when(item2.isFinished()).thenReturn(true);
        when(item2.getNext()).thenReturn(3*(time+2));

        Globals.getInstance().threadCheckIn("test2");
        Assert.assertEquals(time+1, Globals.getInstance().timeMillis());
        verify(item1).reset();
        verify(item1).grantPermission();
        verify(item2).reset();
        verify(item2, never()).grantPermission();
    }

    private boolean getMilliDone() throws NoSuchFieldException, IllegalAccessException {
        Field f = Globals.class.getDeclaredField("milliDone");
        f.setAccessible(true);
        return (Boolean) f.get(Globals.getInstance());
    }

    @Test(timeout = 3000)
    public void testDelayThread() throws InterruptedException {
        testDelay(false);
    }

    @Test(timeout = 3000)
    public void testDelayThreadWithAccel() throws InterruptedException {
        testDelay(true);
    }

    private void testDelay(boolean accel) throws InterruptedException {
        TreeMap<String, ThreadItem> threadMap = populateThreadMap();
        ThreadItem itemMock = new ThreadItem("test", 5000, 0, null);
        threadMap.put("test", itemMock);
        int delay = 333;
        int tolerance = accel ? 35 : 20;
        Globals.getInstance().delayThread("test", delay);
        assert threadMap.containsKey("test-delay");
        Assert.assertEquals(ThreadItem.STATES.SUSPENDED, itemMock.getState());
        long start = Globals.getInstance().timeMillis();
        Globals.getInstance().startTime(accel);
        while (ThreadItem.STATES.SUSPENDED == itemMock.getState()) {
            Thread.sleep(0, 1);
        }
        Assert.assertEquals(delay, (Globals.getInstance().timeMillis() - start), tolerance);
        Thread.sleep(2);
        assert !threadMap.containsKey("test-delay");
        Globals.getInstance().clock.Stop();
    }

    @Test
    public void testDelayCurrent(){
        int delay = 770;
        int tolerance = 5;
        long start = System.currentTimeMillis();
        Globals.getInstance().delayThread("notReal", delay);
        Assert.assertEquals(delay, (int)(System.currentTimeMillis()-start), tolerance);
    }

    @Test
    public void testThreadPermission(){
        Map<String, ThreadItem> threads = populateThreadMap();
        ThreadItem item = new ThreadItem("test", 500, 0, null);
        threads.put("test", item);
        assert !Globals.getInstance().getThreadRunPermission("test");
        item.grantPermission();
        assert !Globals.getInstance().getThreadRunPermission("test");
        Inject.field("begun").of(Globals.getInstance()).with(true);
        assert Globals.getInstance().getThreadRunPermission("test");
        assert !Globals.getInstance().getThreadRunPermission("test");
    }

    @Test
    public void testSetRunning(){
        Map<String, ThreadItem> threads = populateThreadMap();
        ThreadItem item = new ThreadItem("test", 20, 0, null);
        threads.put("test", item);
        Assert.assertNotEquals(ThreadItem.STATES.RUNNING, item.getState());
        Globals.getInstance().threadIsRunning("test");
        Assert.assertEquals(ThreadItem.STATES.RUNNING, item.getState());
    }

    private TreeMap<String, ThreadItem> populateThreadMap(){
        TreeMap<String, ThreadItem> threadMap = new TreeMap<>();
        Inject.field("threads").of(Globals.getInstance()).with(threadMap);
        return threadMap;
    }

}
