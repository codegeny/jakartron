package org.codegeny.jakartron.jndi;

/*-
 * #%L
 * jakartron-core
 * %%
 * Copyright (C) 2018 - 2020 Codegeny
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Binding;
import javax.naming.CompoundName;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Properties;

public class CDIContext implements Context {

    private final static Properties SYNTAX = new Properties();

    static   {
        SYNTAX.put("jndi.syntax.direction", "left_to_right");
        SYNTAX.put("jndi.syntax.separator", "/");
        SYNTAX.put("jndi.syntax.ignorecase", "false");
    }

    private final BeanManager beanManager;

    public CDIContext(BeanManager beanManager) {
        this.beanManager = beanManager;
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    @Override
    public Object lookup(String name) throws NamingException {
        return beanManager.createInstance().select(JNDI.Literal.of(name)).stream().findAny().orElseThrow(() -> new NameNotFoundException("Could not find " + name));
    }

    @Override
    public void bind(Name name, Object obj) {

    }

    @Override
    public void bind(String name, Object obj) {

    }

    @Override
    public void rebind(Name name, Object obj) {

    }

    @Override
    public void rebind(String name, Object obj) {

    }

    @Override
    public void unbind(Name name) {

    }

    @Override
    public void unbind(String name) {

    }

    @Override
    public void rename(Name oldName, Name newName) {

    }

    @Override
    public void rename(String oldName, String newName) {

    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) {
        return null;
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String name) {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) {
        return null;
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String name) {
        return null;
    }

    @Override
    public void destroySubcontext(Name name) {

    }

    @Override
    public void destroySubcontext(String name) {

    }

    @Override
    public Context createSubcontext(Name name) {
        return null;
    }

    @Override
    public Context createSubcontext(String name) {
        return null;
    }

    @Override
    public Object lookupLink(Name name) {
        return null;
    }

    @Override
    public Object lookupLink(String name) {
        return null;
    }

    @Override
    public NameParser getNameParser(Name name) {
        return string -> new CompoundName(string, SYNTAX);
    }

    @Override
    public NameParser getNameParser(String name) {
        return string -> new CompoundName(string, SYNTAX);
    }

    @Override
    public Name composeName(Name name, Name prefix) {
        return null;
    }

    @Override
    public String composeName(String name, String prefix) {
        return null;
    }

    @Override
    public Object addToEnvironment(String propName, Object propVal) {
        return null;
    }

    @Override
    public Object removeFromEnvironment(String propName) {
        return null;
    }

    @Override
    public Hashtable<?, ?> getEnvironment() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public String getNameInNamespace() {
        return null;
    }
}
