package cn.ussshenzhou.section31.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.ShortByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface Kernel32Helper extends StdCallLibrary {
    Kernel32Helper INSTANCE = Native.load("kernel32", Kernel32Helper.class);

    boolean IsWow64Process2(WinNT.HANDLE process, ShortByReference processMachine, ShortByReference nativeMachine);

    int GetThreadId(WinNT.HANDLE thread);

    boolean QueryThreadCycleTime(WinNT.HANDLE thread, LongByReference cycleTime);

    int GetThreadDescription(WinNT.HANDLE thread, PointerByReference description);
}
