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

def l = namespace(lib.LayoutTagLib);
def t = namespace(lib.JenkinsTagLib);
def f = namespace(lib.FormTagLib);
def st = namespace("jelly:stapler");

l.layout(title: my.displayName, permission: app.READ) {
    st.include(page: "sidepanel", it: app); // TODO: CLI help?
    l.main_panel {
        h1(my.displayName);
        form(method: "POST", action: "${rootURL}/${my.urlName}/") {
            def error = request.getAttribute("error");
            if (error) {
                div(class: "error") { text(error); }
            }

            input(type: "text", name: "commandLine", style: "width: 90%", placeholder: "Command", value: request.getParameter("commandLine"));
            f.submit(type: "submit", value: _("Run"));

            def stdout = request.getAttribute("stdout");
            if (stdout) {
                h2("Stdout");
                pre(style: "color: white; background-color: black; padding: 1em; font-weight: bold") {
                    text stdout;
                }
            }

            def stderr = request.getAttribute("stderr");
            if (stderr) {
                h2("Stderr");
                pre(style: "color: red; background-color: black; padding: 1em; font-weight: bold") {
                    text stderr;
                }
            }
        }
    }
}
