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

import com.airlenet.yang.compiler.datamodel.YangLeaf;
import com.airlenet.yang.compiler.datamodel.YangLeafList;
import com.airlenet.yang.compiler.datamodel.YangNode;
import com.airlenet.yang.compiler.datamodel.javadatamodel.*;
import com.airlenet.yang.compiler.datamodel.utils.builtindatatype.YangDataTypes;
import com.airlenet.yang.compiler.translator.exception.TranslatorException;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGenerator;
import com.airlenet.yang.compiler.translator.tojava.JavaCodeGeneratorInfo;
import com.airlenet.yang.compiler.translator.tojava.JavaFileInfoTranslator;
import com.airlenet.yang.compiler.translator.tojava.TempJavaCodeFragmentFiles;
import com.airlenet.yang.compiler.translator.tojava.jnc.JNCCodeUtil;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaClass;
import com.airlenet.yang.compiler.translator.tojava.jnc.JavaMethod;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.tailf.jnc.YangElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.airlenet.yang.compiler.translator.tojava.YangJavaModelUtils.updateJNCPackageInfo;
import static com.airlenet.yang.compiler.translator.tojava.YangJavaModelUtils.updatePackageInfo;
import static com.airlenet.yang.compiler.translator.tojava.utils.TranslatorUtils.getErrorMsg;

/**
 * Represents rpc information extended to support java code generation.
 */
public class YangJavaTailfActionTranslator
        extends YangJavaTailfAction
        implements JavaCodeGenerator, JavaCodeGeneratorInfo {

    private static final long serialVersionUID = 806201622L;

    /**
     * Temporary file for code generation.
     */
    private transient TempJavaCodeFragmentFiles tempJavaCodeFragmentFiles;

    /**
     * Creates an instance of YANG java rpc.
     */
    public YangJavaTailfActionTranslator() {
        setJavaFileInfo(new JavaFileInfoTranslator());
        //getJavaFileInfo().setGeneratedFileTypes(GENERATE_RPC_COMMAND_CLASS);
    }

    /**
     * Returns the generated java file information.
     *
     * @return generated java file information
     */
    @Override
    public JavaFileInfoTranslator getJavaFileInfo() {

        if (javaFileInfo == null) {
            throw new TranslatorException("missing java info in java datamodel node " +
                    getName());
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

    @Override
    public TempJavaCodeFragmentFiles getTempJavaCodeFragmentFiles() {
        return tempJavaCodeFragmentFiles;
    }

    @Override
    public void setTempJavaCodeFragmentFiles(TempJavaCodeFragmentFiles fileHandle) {
        tempJavaCodeFragmentFiles = fileHandle;
    }

    @Override
    public void generatePackageInfo(YangPluginConfig yangPlugin) {
        if (this.getParent() != null && ((JavaCodeGeneratorInfo) this.getParent()).getJavaFileInfo().getPackage() == null) {
            ((JavaCodeGenerator) this.getParent()).generatePackageInfo(yangPlugin);
        }
        updateJNCPackageInfo(this, yangPlugin);
    }

    /**
     * Prepares the information for java code generation corresponding to YANG
     * RPC info.
     *
     * @param yangPlugin YANG plugin config
     * @throws TranslatorException translator operations fails
     */
    @Override
    public void generateCodeEntry(YangPluginConfig yangPlugin)
            throws TranslatorException {

        // Add package information for RPC and create corresponding folder.
        updatePackageInfo(this, yangPlugin);

        YangJavaInput yangJavaInput = null;

        YangJavaOutput yangJavaOutput = null;
        YangNode child = getChild();
        if(child!=null){
            if (child instanceof YangJavaInput) {
                yangJavaInput = (YangJavaInput) child;
            }else{
                yangJavaOutput = (YangJavaOutput) child;
            }
            if ( child.getNextSibling() instanceof YangJavaOutput) {
                yangJavaOutput = (YangJavaOutput) child.getNextSibling();
            }else{
                yangJavaInput = (YangJavaInput) child.getNextSibling();
            }
        }

        if (yangJavaInput != null) {
            ((JavaCodeGenerator)yangJavaInput).generateCodeEntry(yangPlugin);
        }
        if(yangJavaOutput!=null){
            ((JavaCodeGenerator)yangJavaOutput).generateCodeEntry(yangPlugin);
        }

        // TODO: code generation for each RPC
        /*try {
            generateCodeOfNode(this, yangPlugin);
        } catch (IOException e) {
            throw new TranslatorException(
                    "Failed to prepare generate code entry for RPC node " +
                            getName() + " in " +
                            getLineNumber() + " at " +
                            getCharPosition()
                            + " in " + getFileName() + " " + e.getLocalizedMessage());
        }*/

    }

    /**
     * Creates a java file using the YANG RPC info.
     *
     * @throws TranslatorException translator operations fails
     */
    @Override
    public void generateCodeExit()
            throws TranslatorException {
        // Get the parent module/sub-module.
//        YangNode parent = getParentNodeInGenCode(this);


        String classname = YangElement.normalizeClass(this.getName());
        JavaFileInfoTranslator fileInfo = this.getJavaFileInfo();
        JavaClass javaClass = new JavaClass(classname, this.getJavaFileInfo().getPackage(),
                "Code generated by " + this.getClass().getSimpleName() +
                        "\n * <p>" +
                        "\n * See line " + fileInfo.getLineNumber() + " in" +
                        "\n * " + fileInfo.getYangFileName().replace("\\", "/") +
                        "\n * " +
                        "\n * @author Auto Generated");
        YangJavaModule yangJavaModule = (YangJavaModule) this.getYangJavaModule();
        javaClass.setExtend("com.tailf.jnc.YangElement");
        javaClass.addMethod(new JavaMethod(javaClass.getName(), "").setModifiers("public").addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName())
                .addLine("super(" + yangJavaModule.getPrefixClassName() + ".NAMESPACE, \"" + this.getName() + "\");")
                .addDependency(yangJavaModule.getJavaPackage() + "." + yangJavaModule.getPrefixClassName())
                .addLine("setDefaultPrefix();")
                .addLine("setPrefix(" + yangJavaModule.getPrefixClassName() + ".PREFIX);"));

        JNCCodeUtil.keyNamesMethod(javaClass, null);
        YangJavaInput yangJavaInput = null;

        YangJavaOutput yangJavaOutput = null;
        YangNode child = getChild();
        if(child!=null){
            if (child instanceof YangJavaInput) {
                yangJavaInput = (YangJavaInput) child;
            }else{
                yangJavaOutput = (YangJavaOutput) child;
            }
            if (child.getNextSibling() instanceof YangJavaOutput) {
                yangJavaOutput = (YangJavaOutput) child.getNextSibling();
            }else{
                yangJavaInput = (YangJavaInput) child.getNextSibling();
            }
        }


//
        List<YangLeaf> listOfLeaf = new ArrayList<>();
        List<YangLeafList> listOfLeafList = new ArrayList<>();
        if (yangJavaInput != null) {
            listOfLeaf.addAll(yangJavaInput.getListOfLeaf());
            listOfLeafList.addAll(yangJavaInput.getListOfLeafList());
        }
        if (yangJavaOutput != null) {
            listOfLeaf.addAll(yangJavaOutput.getListOfLeaf());
            listOfLeafList.addAll(yangJavaOutput.getListOfLeafList());
        }
        YangNode childInput = null;
        if (yangJavaInput != null) {
            childInput = yangJavaInput.getChild();
        }

        JNCCodeUtil.childrenNamesMethod(javaClass, listOfLeaf, listOfLeafList, childInput, new ArrayList<>());

        JNCCodeUtil.cloneMethod(javaClass, null);

        JNCCodeUtil.cloneShallowMethod(javaClass, null);
        if(childInput!=null &&  (childInput instanceof YangJavaContainer)){
            JNCCodeUtil.yangJavaContainerMethod(javaClass,childInput);
            JNCCodeUtil.yangNodeMethond(javaClass,childInput,true);
        }
        for (YangLeaf yangLeaf :listOfLeaf) {

            JNCCodeUtil.yangLeafMethod(javaClass, yangJavaModule, yangLeaf);

        }
        for (YangLeafList yangLeafList : listOfLeafList) {
            JNCCodeUtil.yangLeafListMethod(javaClass, yangJavaModule, yangLeafList);
        }

        try {
            javaClass.write(this.getJavaFileInfo().getBaseCodeGenPath() + this.getJavaFileInfo().getPackageFilePath());
        } catch (IOException e) {
            throw new TranslatorException(e);
        }
        // Parent should be holder of rpc or notification.
//        if (!(parent instanceof RpcNotificationContainer)) {
//            throw new TranslatorException(getErrorMsg(INVALID_PARENT_NODE,
//                                                      this));
//        }
//
//        /*
//         * Create attribute info for input and output of rpc and add it to the
//         * parent import list.
//         */
//        TempJavaServiceFragmentFiles tempJavaFragmentFiles =
//                ((TempJavaCodeFragmentFilesContainer) getParent())
//                        .getTempJavaCodeFragmentFiles()
//                        .getServiceTempFiles();
//        JavaAttributeInfo javaAttributeInfoOfInput = null;
//        JavaAttributeInfo javaAttributeInfoOfOutput = null;
//
//        // Get the childInput input and output node and obtain create java attribute
//        // info.
//        YangNode yangNode = getChild();
//        while (yangNode != null) {
//            if (yangNode instanceof YangInput) {
//                javaAttributeInfoOfInput = tempJavaFragmentFiles
//                        .getChildNodeAsAttributeInParentService(yangNode,
//                                                                getParent(),
//                                                                getJavaClassNameOrBuiltInType());
//            } else if (yangNode instanceof YangOutput) {
//                javaAttributeInfoOfOutput = tempJavaFragmentFiles
//                        .getChildNodeAsAttributeInParentService(yangNode,
//                                                                getParent(),
//                                                                getJavaClassNameOrBuiltInType());
//            } else {
//                throw new TranslatorException(getErrorMsg(INVALID_CHILD_NODE,
//                                                          this));
//            }
//            yangNode = yangNode.getNextSibling();
//        }
//
//        /*
//         * Add the rpc information to the parent's service temp file.
//         */
//        try {
//
//            ((TempJavaCodeFragmentFilesContainer) parent)
//                    .getTempJavaCodeFragmentFiles().getServiceTempFiles()
//                    .addJavaSnippetInfoToApplicableTempFiles(
//                            javaAttributeInfoOfInput, javaAttributeInfoOfOutput,
//                            getJavaClassNameOrBuiltInType());
//        } catch (IOException e) {
//            throw new TranslatorException(getErrorMsg(FAIL_AT_EXIT, this,
//                                                      e.getLocalizedMessage()));
//        }
//
//        // generate RPC command file
//        /*try {
//            generateJava(GENERATE_RPC_COMMAND_CLASS, this);
//        } catch (IOException e) {
//            throw new TranslatorException("Failed to generate code for RPC node " +
//                                                  getName() + " in " +
//                                                  getLineNumber() + " at " +
//                                                  getCharPosition()
//                                                  + " in " + getFileName() + " " + e.getLocalizedMessage());
//        }*/
    }
}
