/*
 * Copyright (c) 2023, the original author(s).
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.jline.terminal.impl;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.jline.terminal.Attributes;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.terminal.spi.SystemStream;
import org.jline.terminal.spi.TerminalProvider;

public class DumbTerminalProvider implements TerminalProvider {

    @Override
    public String name() {
        return TerminalBuilder.PROP_PROVIDER_DUMB;
    }

    @Override
    public Terminal sysTerminal(
            String name,
            String type,
            boolean ansiPassThrough,
            Charset encoding,
            boolean nativeSignals,
            Terminal.SignalHandler signalHandler,
            boolean paused,
            SystemStream systemStream)
            throws IOException {
        return new DumbTerminal(
                this,
                systemStream,
                name,
                type,
                new FileInputStream(FileDescriptor.in),
                new FileOutputStream(systemStream == SystemStream.Error ? FileDescriptor.err : FileDescriptor.out),
                encoding,
                signalHandler);
    }

    @Override
    public Terminal newTerminal(
            String name,
            String type,
            InputStream masterInput,
            OutputStream masterOutput,
            Charset encoding,
            Terminal.SignalHandler signalHandler,
            boolean paused,
            Attributes attributes,
            Size size)
            throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSystemStream(SystemStream stream) {
        return false;
    }

    @Override
    public String systemStreamName(SystemStream stream) {
        return null;
    }

    @Override
    public int systemStreamWidth(SystemStream stream) {
        return 0;
    }
}
