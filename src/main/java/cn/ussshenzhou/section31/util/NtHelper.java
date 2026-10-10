package cn.ussshenzhou.section31.util;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;

public interface NtHelper extends StdCallLibrary {
    NtHelper INSTANCE = Native.load("ntdll", NtHelper.class);

    int NtGetNextThread(WinNT.HANDLE process, WinNT.HANDLE thread, int desiredAccess, int handleAttributes, int flags, WinNT.HANDLEByReference nextThread);
}
