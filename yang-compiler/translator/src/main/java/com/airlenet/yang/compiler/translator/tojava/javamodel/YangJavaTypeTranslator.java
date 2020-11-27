/*
 * Copyright 2016-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.airlenet.yang.compiler.translator.tojava.javamodel;

import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaType;
import com.airlenet.yang.compiler.translator.exception.TranslatorException;
import com.airlenet.yang.compiler.translator.tojava.JavaQualifiedTypeInfoTranslator;
import com.airlenet.yang.compiler.utils.io.YangToJavaNamingConflictUtil;

/**
 * Represents java information corresponding to the YANG type.
 */
public class YangJavaTypeTranslator
        extends YangJavaType
        implements JavaQualifiedTypeResolver {

    /**
     * Create a YANG leaf object with java qualified access details.
     */
    public YangJavaTypeTranslator() {
        super();
        setJavaQualifiedInfo(new JavaQualifiedTypeInfoTranslator());
    }

    @Override
    public void updateJavaQualifiedInfo(YangToJavaNamingConflictUtil conflictResolver) {
        JavaQualifiedTypeInfoTranslator importInfo = (JavaQualifiedTypeInfoTranslator) getJavaQualifiedInfo();

        /*
         * Type is added as an attribute in the class.
         */
        String className = AttributesJavaDataType.getJavaImportClass(this, true, conflictResolver);
        if (className != null) {
            /*
             * Corresponding to the attribute type a class needs to be imported,
             * since it can be a derived type or a usage of wrapper classes.
             */
            importInfo.setClassInfo(className);
            String classPkg = AttributesJavaDataType.getJavaImportPackage(this,
                                                   true, conflictResolver);
            if (classPkg == null) {
                throw new TranslatorException("import package cannot be null when the class is used " +
                                                      getDataTypeName() + " in " +
                                                      getLineNumber() + " at " +
                                                      getCharPosition()
                                                      + " in " + getFileName());
            }
            importInfo.setPkgInfo(classPkg);
        } else {
            /*
             * The attribute does not need a class to be imported, for example
             * built in java types.
             */
            String dataTypeName = AttributesJavaDataType.getJavaDataType(this);
            if (dataTypeName == null) {
                throw new TranslatorException("not supported data type " +
                                                      getDataTypeName() + " in " +
                                                      getLineNumber() + " at " +
                                                      getCharPosition()
                                                      + " in " + getFileName());
            }
            importInfo.setClassInfo(dataTypeName);
        }
        setJavaQualifiedInfo(importInfo);
    }
}
