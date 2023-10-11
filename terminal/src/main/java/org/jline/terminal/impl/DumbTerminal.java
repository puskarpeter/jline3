/*
 * Copyright (c) 2002-2018, the original author(s).
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * https://opensource.org/licenses/BSD-3-Clause
 */
package org.jline.terminal.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.ControlChar;
import org.jline.terminal.Size;
import org.jline.terminal.spi.SystemStream;
import org.jline.terminal.spi.TerminalProvider;
import org.jline.utils.NonBlocking;
import org.jline.utils.NonBlockingInputStream;
import org.jline.utils.NonBlockingReader;

public class DumbTerminal extends AbstractTerminal {

    private final TerminalProvider provider;
    private final SystemStream systemStream;
    private final NonBlockingInputStream input;
    private final OutputStream output;
    private final NonBlockingReader reader;
    private final PrintWriter writer;
    private final Attributes attributes;
    private final Size size;

    public DumbTerminal(InputStream in, OutputStream out) throws IOException {
        this(TYPE_DUMB, TYPE_DUMB, in, out, null);
    }

    public DumbTerminal(String name, String type, InputStream in, OutputStream out, Charset encoding)
            throws IOException {
        this(null, null, name, type, in, out, encoding, SignalHandler.SIG_DFL);
    }

    @SuppressWarnings("this-escape")
    public DumbTerminal(
            TerminalProvider provider,
            SystemStream systemStream,
            String name,
            String type,
            InputStream in,
            OutputStream out,
            Charset encoding,
            SignalHandler signalHandler)
            throws IOException {
        super(name, type, encoding, signalHandler);
        this.provider = provider;
        this.systemStream = systemStream;
        NonBlockingInputStream nbis = NonBlocking.nonBlocking(getName(), in);
        this.input = new NonBlockingInputStream() {
            @Override
            public int read(long timeout, boolean isPeek) throws IOException {
                for (; ; ) {
                    int c = nbis.read(timeout, isPeek);
                    if (attributes.getLocalFlag(Attributes.LocalFlag.ISIG)) {
                        if (c == attributes.getControlChar(ControlChar.VINTR)) {
                            raise(Signal.INT);
                            continue;
                        } else if (c == attributes.getControlChar(ControlChar.VQUIT)) {
                            raise(Signal.QUIT);
                            continue;
                        } else if (c == attributes.getControlChar(ControlChar.VSUSP)) {
                            raise(Signal.TSTP);
                            continue;
                        } else if (c == attributes.getControlChar(ControlChar.VSTATUS)) {
                            raise(Signal.INFO);
                            continue;
                        }
                    }
                    if (c == '\r') {
                        if (attributes.getInputFlag(Attributes.InputFlag.IGNCR)) {
                            continue;
                        }
                        if (attributes.getInputFlag(Attributes.InputFlag.ICRNL)) {
                            c = '\n';
                        }
                    } else if (c == '\n' && attributes.getInputFlag(Attributes.InputFlag.INLCR)) {
                        c = '\r';
                    }
                    return c;
                }
            }
        };
        this.output = out;
        this.reader = NonBlocking.nonBlocking(getName(), input, encoding());
        this.writer = new PrintWriter(new OutputStreamWriter(output, encoding()));
        this.attributes = new Attributes();
        this.attributes.setControlChar(ControlChar.VERASE, (char) 127);
        this.attributes.setControlChar(ControlChar.VWERASE, (char) 23);
        this.attributes.setControlChar(ControlChar.VKILL, (char) 21);
        this.attributes.setControlChar(ControlChar.VLNEXT, (char) 22);
        this.size = new Size();
        parseInfoCmp();
    }

    public NonBlockingReader reader() {
        return reader;
    }

    public PrintWriter writer() {
        return writer;
    }

    @Override
    public InputStream input() {
        return input;
    }

    @Override
    public OutputStream output() {
        return output;
    }

    public Attributes getAttributes() {
        return new Attributes(attributes);
    }

    public void setAttributes(Attributes attr) {
        attributes.copy(attr);
    }

    public Size getSize() {
        Size sz = new Size();
        sz.copy(size);
        return sz;
    }

    public void setSize(Size sz) {
        size.copy(sz);
    }

    @Override
    public TerminalProvider getProvider() {
        return provider;
    }

    @Override
    public SystemStream getSystemStream() {
        return systemStream;
    }
}
