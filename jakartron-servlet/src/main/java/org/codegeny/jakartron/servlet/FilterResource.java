package org.codegeny.jakartron.servlet;

/*-
 * #%L
 * jakartron-servlet
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

import org.eclipse.jetty.util.resource.Resource;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;

public class FilterResource extends Resource {

    @Override
    public boolean isContainedIn(Resource r) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {}

    @Override
    public boolean exists() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDirectory() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long lastModified() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long length() {
        throw new UnsupportedOperationException();
    }

    @Deprecated
    @Override
    public URL getURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ReadableByteChannel getReadableByteChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean delete() throws SecurityException {
        return false;
    }

    @Override
    public boolean renameTo(Resource dest) throws SecurityException {
        return false;
    }

    @Override
    public String[] list() {
        return new String[0];
    }

    @Override
    public Resource addPath(String path) {
        if (path.startsWith("/WEB-INF")) {
            return Resource.newClassPathResource("META-INF" + path.substring(8));
        }
        if (path.startsWith("WEB-INF")) {
            return Resource.newClassPathResource("META-INF" + path.substring(7));
        }
        if (path.startsWith("/")) {
            return Resource.newClassPathResource("META-INF/resources" + path);
        }
        return Resource.newClassPathResource("META-INF/resources/" + path);
    }
}
