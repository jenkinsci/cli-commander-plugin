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

import hudson.Extension;
import hudson.Util;
import hudson.cli.CLICommand;
import hudson.model.RootAction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletException;

import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * CLI commander entry point.
 *
 * @author ogondza
 */
@Extension
@Restricted(NoExternalUse.class)
public class Commander implements RootAction {

    public String getUrlName() {
        return "clicommander";
    }

    public String getDisplayName() {
        return "CLI Commander";
    }

    public String getIconFileName() {
        return null;
    }

    public void doIndex(
            StaplerRequest req, StaplerResponse res, @QueryParameter String commandLine
    ) throws ServletException, IOException {

        if ("POST".equals(req.getMethod())) {
            try {

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {});

                CLICommand command = getCommand(commandLine);
                command.main(getArgs(commandLine), Locale.US, in, new PrintStream(out), new PrintStream(err));
                req.setAttribute("stdout", out.toString());
                req.setAttribute("stderr", err.toString());
            } catch (IllegalArgumentException ex) {
                req.setAttribute("error", ex.getMessage());
            }
        }
        req.getView(this, "_index").forward(req, res);
    }

    private CLICommand getCommand(String commandLine) throws IllegalArgumentException {
        List<String> args = Arrays.asList(commandLine.split("\\s+"));
        if (Util.fixEmptyAndTrim(commandLine) == null || args.size() == 0) {
            throw new IllegalArgumentException("No command provided");
        }

        CLICommand command = CLICommand.clone(args.get(0));
        if (command == null) throw new IllegalArgumentException("There is no such command: " + args.get(0));

        return command;
    }

    private List<String> getArgs(String commandLine) {
        List<String> args = new ArrayList<String>(Arrays.asList(commandLine.split("\\s+")));
        if (args.size() == 0) throw new IllegalArgumentException("No command provided");

        args.remove(0);
        return args;
    }
}