package org.codegeny.jakartron.jpa;

/*-
 * #%L
 * jakartron-jpa
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

import org.codegeny.jakartron.jpa.PersistenceUnitDefinitionEvent.PersistenceUnitConfigurator;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

// TODO complete
final class PersistenceUnitInfoBean implements PersistenceUnitConfigurator, PersistenceUnitInfo {

    private final List<String> classes = new ArrayList<>();
    private final Properties properties = new Properties();
    private PersistenceUnitTransactionType transactionType;
    private final String unitName;
    private DataSource jtaDataSource;
    private DataSource nonJtaDataSource;

    PersistenceUnitInfoBean(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public PersistenceUnitConfigurator classes(Class<?>... classes) {
        Stream.of(classes).map(Class::getName).forEach(this.classes::add);
        return this;
    }

    @Override
    public PersistenceUnitConfigurator classNames(String... classes) {
        this.classes.addAll(Arrays.asList(classes));
        return this;
    }

    @Override
    public PersistenceUnitConfigurator property(String name, String value) {
        properties.setProperty(name, value);
        return this;
    }

    @Override
    public PersistenceUnitConfigurator transactionType(PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    @Override
    public PersistenceUnitConfigurator jtaDataSource(DataSource dataSource) {
        jtaDataSource = dataSource;
        return this;
    }

    @Override
    public PersistenceUnitConfigurator nonJtaDataSource(DataSource dataSource) {
        nonJtaDataSource = dataSource;
        return this;
    }

    @Override
    public String getPersistenceUnitName() {
        return unitName;
    }

    @Override
    public String getPersistenceProviderClassName() {
        return null;
    }

    @Override
    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    @Override
    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    @Override
    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    @Override
    public List<String> getMappingFileNames() {
        return null;
    }

    @Override
    public List<URL> getJarFileUrls() {
        return null;
    }

    @Override
    public URL getPersistenceUnitRootUrl() {
        return null;
    }

    @Override
    public List<String> getManagedClassNames() {
        return classes;
    }

    @Override
    public boolean excludeUnlistedClasses() {
        return false;
    }

    @Override
    public SharedCacheMode getSharedCacheMode() {
        return null;
    }

    @Override
    public ValidationMode getValidationMode() {
        return null;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String getPersistenceXMLSchemaVersion() {
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return null;
    }

    @Override
    public void addTransformer(ClassTransformer classTransformer) {
    }

    @Override
    public ClassLoader getNewTempClassLoader() {
        return null;
    }
}
