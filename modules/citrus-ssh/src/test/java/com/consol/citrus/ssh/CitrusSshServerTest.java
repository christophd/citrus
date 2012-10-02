/*
 * Copyright 2006-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.ssh;

import java.io.IOException;
import java.net.*;
import java.security.KeyPair;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author roland
 * @since 05.09.12
 */
public class CitrusSshServerTest {


    private CitrusSshServer server;
    private int port;

    public CitrusSshServerTest() {
        port = findFreePort();
    }

    @BeforeMethod
    public void beforeTest() {
        server = new CitrusSshServer();
        server.setPort(port);
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*user.*")
    public void noUser() {
        server.start();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*password.*allowed-key-path.*")
    public void noPasswordOrKey() {
        server.setUser("roland");
        server.start();
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*/no/such/key\\.pem.*")
    public void invalidAuthKey() {
        server.setUser("roland");
        server.setAllowedKeyPath("classpath:/no/such/key.pem");
        server.start();
    }

    @Test
    public void startupAndShutdown() throws IOException {
        for (boolean b : new boolean[] { true, false }) {
            prepareServer(b);
            server.start();
            assertTrue(server.isRunning());
            new Socket(InetAddress.getLocalHost(), port); // throws exception if it cant connect
            server.stop();
            assertFalse(server.isRunning());
        }
    }

    @Test
    public void wrongHostKey() {
        prepareServer(true);
        server.setHostKeyPath("/never/existing/directory");
        server.start();
        try {
            SshServer sshd = (SshServer) ReflectionTestUtils.getField(server, "sshd");
            KeyPairProvider prov = sshd.getKeyPairProvider();
            assertTrue(prov instanceof FileKeyPairProvider);
            KeyPair[] keys = ((FileKeyPairProvider) prov).loadKeys();
            assertEquals(keys.length,0);
        } finally {
            server.stop();
        }
    }

    @Test
    public void sshCommandFactory() {
        prepareServer(true);
        server.start();
        try {
            SshServer sshd = (SshServer) ReflectionTestUtils.getField(server, "sshd");
            CommandFactory fact = sshd.getCommandFactory();
            Command cmd = fact.createCommand("shutdown now");
            assertTrue(cmd instanceof SshCommand);
            assertEquals(((SshCommand) cmd).getCommand(),"shutdown now");
        } finally {
            server.stop();
        }
    }

    @Test(expectedExceptions = CitrusRuntimeException.class,expectedExceptionsMessageRegExp = ".*BindException.*")
    public void doubleStart() throws IOException {
        prepareServer(true);
        ServerSocket s = null;
        try {
            s = new ServerSocket(port);
            server.start();
        } finally {
            if (s != null) s.close();
        }
    }

    /**
     * Prepare server instance.
     */
    private void prepareServer(boolean withPassword) {
        server.setUser("roland");
        if (withPassword) {
            server.setPassword("consol");
        } else {
            server.setAllowedKeyPath("classpath:com/consol/citrus/ssh/allowed_test_key.pem");
        }
    }

    /**
     * Finds a free port in port range.
     */
    private int findFreePort() {
        for (int port=2234; port<3000; port++) {
            try {
                Socket socket = new Socket(InetAddress.getLocalHost(),port);
                socket.close();
            } catch (IOException e) {
                return port;
            }
        }
        
        throw new IllegalStateException("No free port between 2234 and 3000 found");
    }

}
