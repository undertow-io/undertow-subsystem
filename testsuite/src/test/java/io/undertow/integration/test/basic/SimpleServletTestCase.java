package io.undertow.integration.test.basic;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Stuart Douglas
 */
@RunWith(Arquillian.class)
@RunAsClient
public class SimpleServletTestCase {

    @Deployment
    public static WebArchive deploy() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class);
        return archive;
    }

    @Test
    public void test() {

    }

}
