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

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.PersistenceUnitTransactionType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface PersistenceUnitDefinition {

    /**
     * Returns the name of the persistence unit. Corresponds to the
     * <code>name</code> attribute in the <code>persistence.xml</code> file.
     *
     * @return the name of the persistence unit
     */
    String unitName();

    /**
     * Returns the fully qualified name of the persistence provider
     * implementation class. Corresponds to the <code>provider</code> element in
     * the <code>persistence.xml</code> file.
     *
     * @return the fully qualified name of the persistence provider
     * implementation class
     */
    String providerClassName() default "";

    /**
     * Returns the transaction type of the entity managers created by
     * the <code>EntityManagerFactory</code>. The transaction type corresponds to
     * the <code>transaction-type</code> attribute in the <code>persistence.xml</code> file.
     *
     * @return transaction type of the entity managers created
     * by the EntityManagerFactory
     */
    PersistenceUnitTransactionType transactionType() default PersistenceUnitTransactionType.JTA;

    /**
     * Returns the JTA-enabled data source to be used by the
     * persistence provider. The data source corresponds to the
     * <code>jta-data-source</code> element in the <code>persistence.xml</code> file or is
     * provided at deployment or by the container.
     *
     * @return the JTA-enabled data source to be used by the
     * persistence provider
     */
    String jtaDataSourceName() default "";

    /**
     * Returns the non-JTA-enabled data source to be used by the
     * persistence provider for accessing data outside a JTA
     * transaction. The data source corresponds to the named
     * <code>non-jta-data-source</code> element in the <code>persistence.xml</code> file or
     * provided at deployment or by the container.
     *
     * @return the non-JTA-enabled data source to be used by the
     * persistence provider for accessing data outside a JTA
     * transaction
     */
    String nonJtaDataSourceName() default "";

    /**
     * Returns the list of the names of the mapping files that the
     * persistence provider must load to determine the mappings for
     * the entity classes. The mapping files must be in the standard
     * XML mapping format, be uniquely named and be resource-loadable
     * from the application classpath.  Each mapping file name
     * corresponds to a <code>mapping-file</code> element in the
     * <code>persistence.xml</code> file.
     *
     * @return the list of mapping file names that the persistence
     * provider must load to determine the mappings for the entity
     * classes
     */
    String[] mappingFileNames() default {};

    /**
     * Returns a list of URLs for the jar files or exploded jar
     * file directories that the persistence provider must examine
     * for managed classes of the persistence unit. Each URL
     * corresponds to a <code>jar-file</code> element in the
     * <code>persistence.xml</code> file. A URL will either be a
     * file: URL referring to a jar file or referring to a directory
     * that contains an exploded jar file, or some other URL from
     * which an InputStream in jar format can be obtained.
     *
     * @return a list of URL objects referring to jar files or
     * directories
     */
    String[] jarFileUrls() default {};

    /**
     * Returns the URL for the jar file or directory that is the
     * root of the persistence unit. (If the persistence unit is
     * rooted in the WEB-INF/classes directory, this will be the
     * URL of that directory.)
     * The URL will either be a file: URL referring to a jar file
     * or referring to a directory that contains an exploded jar
     * file, or some other URL from which an InputStream in jar
     * format can be obtained.
     *
     * @return a URL referring to a jar file or directory
     */
    String unitRootUrl() default "";

    /**
     * Returns the list of the classes that the
     * persistence provider must add to its set of managed
     * classes. Each name corresponds to a named <code>class</code> element in the
     * <code>persistence.xml</code> file.
     *
     * @return the list of the classes that the
     * persistence provider must add to its set of managed
     * classes
     */
    Class<?>[] managedClasses() default {};

    /**
     * Returns the list of the names of the classes that the
     * persistence provider must add to its set of managed
     * classes. Each name corresponds to a named <code>class</code> element in the
     * <code>persistence.xml</code> file.
     *
     * @return the list of the names of the classes that the
     * persistence provider must add to its set of managed
     * classes
     */
    String[] managedClassNames() default {};

    /**
     * Returns whether classes in the root of the persistence unit
     * that have not been explicitly listed are to be included in the
     * set of managed classes. This value corresponds to the
     * <code>exclude-unlisted-classes</code> element in the <code>persistence.xml</code> file.
     *
     * @return whether classes in the root of the persistence
     * unit that have not been explicitly listed are to be
     * included in the set of managed classes
     */
    boolean excludeUnlistedClasses() default false;

    /**
     * Returns the specification of how the provider must use
     * a second-level cache for the persistence unit.
     * The result of this method corresponds to the <code>shared-cache-mode</code>
     * element in the <code>persistence.xml</code> file.
     *
     * @return the second-level cache mode that must be used by the
     * provider for the persistence unit
     * @since Java Persistence 2.0
     */
    SharedCacheMode sharedCacheMode() default SharedCacheMode.ALL;

    /**
     * Returns the validation mode to be used by the persistence
     * provider for the persistence unit.  The validation mode
     * corresponds to the <code>validation-mode</code> element in the
     * <code>persistence.xml</code> file.
     *
     * @return the validation mode to be used by the
     * persistence provider for the persistence unit
     * @since Java Persistence 2.0
     */
    ValidationMode validationMode() default ValidationMode.AUTO;

    /**
     * Returns a properties object. Each property corresponds to a
     * <code>property</code> element in the <code>persistence.xml</code> file
     * or to a property set by the container.
     *
     * @return Properties object
     */
    Property[] properties() default {};

    /**
     * Returns the schema version of the <code>persistence.xml</code> file.
     *
     * @return persistence.xml schema version
     * @since Java Persistence 2.0
     */
    String xmlSchemaVersion() default "2.0";

    @interface Property {

        String name();

        String value();
    }
}
