package com.consol.citrus.ssh.config;


import com.consol.citrus.ssh.CitrusSshClient;

/**
 * Parse for SSH-client configuration
 *
 * @author roland
 * @since 11.09.12
 */
public class SshClientParser extends AbstractSshParser {


    @Override
    protected String[] getAttributePropertyMapping() {
        return new String[] {
                "host","host",
                "port","port",
                "private-key-path","privateKeyPath",
                "private-key-password","privateKeyPassword",
                "strict-host-checking","strictHostChecking",
                "known-hosts-path","knownHosts",
                "command-timeout","commandTimeout",
                "connection-timeout","connectionTimeout",
                "user","user",
                "password","password"
        };
    }

    @Override
    protected String[] getAttributePropertyReferenceMapping() {
        return new String[] {
                "actor","actor",
                "reply-handler","replyMessageHandler"
        };
    }


    @Override
    protected Class getBeanClass() {
        return CitrusSshClient.class;
    }
}
