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
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaTypeDef;
import com.airlenet.yang.compiler.datamodel.javadatamodel.YangJavaUnion;
import com.airlenet.yang.compiler.translator.exception.TranslatorException;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGenerator;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGeneratorInfo;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoTranslator;
import com.airlenet.yang.compiler.translator.tojava.TempJavaCodeFragmentFiles;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaClass;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaMethod;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.tailf.jnc.YangElement;
import com.tailf.jnc.YangException;
import com.tailf.jnc.YangUnion;

import java.io.IOException;
import java.util.stream.Collectors;

import static com.airlenet.yang.compiler.translator.tojava.GeneratedJavaFileType.GENERATE_UNION_CLASS;
import static com.airlenet.yang.compiler.translator.tojava.YangJavaModelUtils.*;
import static com.airlenet.yang.compiler.utils.io.impl.YangIoUtils.getAbsolutePackagePath;

/**
 * Represents union information extended to support java code generation.
 */
public class YangJavaUnionTranslator
        extends YangJavaUnion
        implements JavaCodeGeneratorInfo, JavaCodeGenerator {

    private static final long serialVersionUID = 806201619L;

    /**
     * File handle to maintain temporary java code fragments as per the code
     * snippet types.
     */
    private transient TempJavaCodeFragmentFiles tempFileHandle;

    /**
     * Creates an instance of YANG java union.
     */
    public YangJavaUnionTranslator() {
        super();
        setJavaFileInfo(new JavaFileInfoTranslator());
        getJavaFileInfo().setGeneratedFileTypes(GENERATE_UNION_CLASS);
    }

    /**
     * Returns the generated java file information.
     *
     * @return generated java file information
     */
    @Override
    public JavaFileInfoTranslator getJavaFileInfo() {
        if (javaFileInfo == null) {
            throw new RuntimeException("Missing java info in java datamodel node " + getName() + " in " +
                                               getLineNumber() + " at " +
                                               getCharPosition()
                                               + " in " + getFileName());
        }
        return (JavaFileInfoTranslator) javaFileInfo;
    }

    /**
     * Sets the java file info object.
     *
     * @param javaInfo java file info object
     */
    @Override
    public void setJavaFileInfo(JavaFileInfoTranslator javaInfo) {
        javaFileInfo = javaInfo;
    }

    /**
     * Returns the temporary file handle.
     *
     * @return temporary file handle
     */
    @Override
    public TempJavaCodeFragmentFiles getTempJavaCodeFragmentFiles() {
        return tempFileHandle;
    }

    /**
     * Sets temporary file handle.
     *
     * @param fileHandle temporary file handle
     */
    @Override
    public void setTempJavaCodeFragmentFiles(TempJavaCodeFragmentFiles fileHandle) {
        tempFileHandle = fileHandle;
    }

    @Override
    public void generatePackageInfo(YangPluginConfig yangPlugin) {
        if(this.getParent()!=null && ((JavaCodeGeneratorInfo)this.getParent()).getJavaFileInfo().getPackage()==null){
            ((JavaCodeGenerator)this.getParent()).generatePackageInfo(yangPlugin);
        }
        updateJNCPackageInfo(this, yangPlugin);
    }
    /**
     * Prepare the information for java code generation corresponding to YANG
     * union info.
     *
     * @param yangPlugin YANG plugin config
     * @throws TranslatorException when fails to translate
     */
    @Override
    public void generateCodeEntry(YangPluginConfig yangPlugin) throws TranslatorException {
        updateJNCPackageInfo(this,yangPlugin);
//        try {
//            if (getReferredSchema() != null) {
//                throw new InvalidNodeForTranslatorException();
//            }
            this.getTypeList().forEach(yangType -> {
                ((YangJavaTypeTranslator) yangType).updateJavaQualifiedInfo(yangPlugin.getConflictResolver());
            });
//            generateCodeOfNode(this, yangPlugin);
//        } catch (IOException e) {
//            throw new TranslatorException(
//                    "Failed to prepare generate code entry for union node " + getName() + " in " +
//                            getLineNumber() + " at " +
//                            getCharPosition()
//                            + " in " + getFileName() + " " + e.getLocalizedMessage());
//        }

    }

    /**
     * Creates a java file using the YANG union info.
     *
     * @throws TranslatorException when fails to translate
     */
    @Override
    public void generateCodeExit() throws TranslatorException {

        String classname= YangElement.normalizeClass(this.getName().replaceAll("_","-"));
        JavaFileInfoTranslator fileInfo = this.getJavaFileInfo();

        String absoluteDirPath = getAbsolutePackagePath(fileInfo.getBaseCodeGenPath(),
                fileInfo.getPackageFilePath());
//        YangJavaModule yangJavaModule = (YangJavaModule)this.getYangJavaModule();
        if(this.getParent()!=null && this.getParent() instanceof YangJavaTypeDef){
            return;
        }

        JavaClass javaClass = new JavaClass(classname, fileInfo.getPackage(),
                "Code generated by "+this.getClass().getSimpleName() +
                        "\n * <p>"+
                        "\n * See line "+fileInfo.getLineNumber()+" in" +
                        "\n * "+fileInfo.getYangFileName().replace("\\","/")+
                        "\n * "+
                        "\n * @author Auto Generated");
        if(this.getReferredSchema()!=null){
            javaClass.setExtend(YangUnion.class.getName());
        }else {
            javaClass.setExtend(YangUnion.class.getName());
        }

        javaClass.addMethod(new JavaMethod(classname,"")
                .setModifiers("public")
                .setExceptions(YangException.class.getName())
                .addParameter("String","value")
                .addLine("super(value,").addLine("\tnew String[] {")

                .addLine(
                        this.getTypeList().stream()
                                .map( yangType -> "\""+((YangJavaType)yangType).getJavaQualifiedInfo().getPkgInfo()+"."+ ((YangJavaType)yangType).getJavaQualifiedInfo().getClassInfo()+"\"").collect(Collectors.joining(","))
                )
                .addLine("}").addLine(");").addLine("check();")
        );

        javaClass.addMethod(new JavaMethod("setValue","void").setModifiers("public").setExceptions(YangException.class.getName())
                .addParameter("String","value")
                .addLine("super.setValue(value);")
                .addLine("check();")
        );
        javaClass.addMethod(new JavaMethod("check","void").setModifiers("public").setExceptions(YangException.class.getName())
                .addLine("super.check();")
        );

        try {
            javaClass.write(absoluteDirPath);
        } catch (IOException e) {
            throw new TranslatorException(e);
        }
//        try {
//            generateJava(GENERATE_UNION_CLASS, this);
//        } catch (IOException e) {
//            throw new TranslatorException("Failed to generate code for union node " + getName() + " in " +
//                                                  getLineNumber() + " at " +
//                                                  getCharPosition()
//                                                  + " in " + getFileName() + " " + e.getLocalizedMessage());
//        }
    }
}
