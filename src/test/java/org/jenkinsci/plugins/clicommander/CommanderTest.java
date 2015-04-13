/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.clicommander;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class CommanderTest {

    @Rule public JenkinsRule j = new JenkinsRule();

    private String out, err;
    private WebClient wc;

    @Before
    public void setUp() {
        wc = j.createWebClient();
    }

    @Test
    public void runCommand() throws Exception {
        run("who-am-i");
        assertThat(err, equalTo(null));
        assertThat(out, containsString("Authenticated as: anonymous"));
    }

    @Test
    public void csrf() throws Exception {
        j.createFreeStyleProject("delete");
        j.createFreeStyleProject("keep");

        URL url = new URL(j.getURL(), "clicommander/?commandLine=delete-job delete");
        WebRequestSettings req = new WebRequestSettings(url, HttpMethod.POST);
        wc.addCrumb(req);
        HtmlPage rsp = wc.getPage(req);
        assertNull(j.jenkins.getItem("delete"));

        url = new URL(j.getURL(), "clicommander/?commandLine=delete-job keep");
        req = new WebRequestSettings(url, HttpMethod.POST);
        wc.setThrowExceptionOnFailingStatusCode(false);
        rsp = wc.getPage(req);
        assertNotNull(j.jenkins.getItem("keep"));
    }

    @Test
    public void useCorrectAuthentication() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());

        wc.login("jdoe", "jdoe");

        run("who-am-i");
        assertThat(err, equalTo(null));
        assertThat(out, containsString("Authenticated as: jdoe Authorities: authenticated"));
    }

    public void run(String commands) throws Exception {
        HtmlPage page = wc.goTo("clicommander");
        HtmlForm form = page.getFormByName("clicommander");

        HtmlElement submit = page.createElement("button");
        submit.setAttribute("type", "submit");
        form.appendChild(submit);

        form.getInputByName("commandLine").setValueAttribute(commands);
        page = submit.click();
        final HtmlElement stdout = page.getElementById("stdout");
        final HtmlElement stderr = page.getElementById("stderr");
        out = stdout == null ? null : stdout.asText();
        err = stderr == null ? null : stderr.asText();
    }
}
