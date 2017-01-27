/**
 * Copyright (c) 2016-2017, Mihai Emil Andronache
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * Neither the name of the copyright holder nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.amihaiemil.camel;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanMap;

/**
 * An Object represented as a YamlMapping.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 1.0.0
 *
 */
public final class YamlObjectDump implements YamlDump {

    /**
     * Object to dump.
     */
    private Object obj;

    /**
     * Ctor.
     * @param obj Object to dump.
     */
    public YamlObjectDump(final Object obj) {
        this.obj = obj;
    }

    @Override
    public YamlMapping represent() {
        YamlMappingBuilder builder = new RtYamlMappingBuilder();
        Set<Map.Entry<Object, Object>> entries = new BeanMap(this.obj)
            .entrySet();
        for (final Map.Entry<Object, Object> entry : entries) {
            String key = (String) entry.getKey();
            if(!"class".equals(key)) {
                Object value = entry.getValue();
                if(this.leafProperty(value)) {
                    builder = builder
                        .add((String) entry.getKey(), value.toString());
                } else {
                    builder = builder
                        .add((String) entry.getKey(), this.yamlNode(value));
                }
            }
        }
        return builder.build();
    }

    /**
     * Check if the given property is a 'leaf'. For instance, a String is
     * considered a leaf, we don't need to go deeper to check for its
     * properties, we can directly print it.
     * @param property Tested property
     * @return Boolean
     */
    private boolean leafProperty(final Object property) {
        boolean leaf = false;
        Class<?> clazz = property.getClass();
        try {
            if(clazz.getName().startsWith("java.lang.")
                || clazz.getName().startsWith("java.util.")){
                if(clazz.getMethod("toString").getDeclaringClass()
                        .equals(clazz)
                ) {
                    leaf = true;
                }
            }
        } catch (final NoSuchMethodException nsme) {
            nsme.printStackTrace();
        } catch (final SecurityException ex) {
            ex.printStackTrace();
        }
        return leaf;
    }

    /**
     * Convert a complex property to a Yaml node.
     * @param property The property to represent as a YamlNode
     * @return YamlNode representation of
     * @todo #34:30m/DEV After YamlMapDump and YamlCollectionDump are
     *  implemented, this method should be fixed.
     */
    private YamlNode yamlNode(final Object property) {
        YamlNode node;
        if (property instanceof Map) {
            node = new Scalar("...yaml mapping...");
        } else if (property instanceof Collection<?>) {
            node = new Scalar("...yaml sequence...");
        } else {
            node = new YamlObjectDump(property).represent();
        }
        return node;
    }
}
