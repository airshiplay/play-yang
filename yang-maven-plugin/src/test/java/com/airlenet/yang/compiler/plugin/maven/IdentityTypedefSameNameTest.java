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

package com.airlenet.yang.compiler.plugin.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.tool.YangCompilerManager;
import com.airlenet.yang.compiler.utils.io.YangPluginConfig;
import com.airlenet.yang.compiler.utils.io.impl.YangIoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import static com.airlenet.yang.compiler.utils.io.impl.YangFileScanner.getYangFiles;

/**
 * Unit tests for identity and typedef with same name with different letter
 * combination translator.
 */
public final class IdentityTypedefSameNameTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private static final String DIR = "target/IdentityTypedefTestGenFile/";
    private static final String COMP = System.getProperty("user.dir") + File
            .separator + DIR;
    private final YangCompilerManager utilManager = new YangCompilerManager();

    /**
     * Checks identity and typedef with same name with different letter
     * combination translation should not result in any exception.
     *
     * @throws MojoExecutionException
     */
    @Test
    public void processtest() throws IOException,
            ParserException, MojoExecutionException {
        YangIoUtils.deleteDirectory(DIR);
        String searchDir = "src/test/resources/identityTypedefSameName";

        Set<Path> paths = new HashSet<>();
        for (String file : getYangFiles(searchDir)) {
            paths.add(Paths.get(file));
        }

        utilManager.createYangFileInfoSet(paths);
        utilManager.parseYangFileInfoSet();
        utilManager.createYangNodeSet();
        utilManager.resolveDependenciesUsingLinker();

        YangPluginConfig yangPluginConfig = new YangPluginConfig();
        yangPluginConfig.setCodeGenDir(DIR);
        utilManager.translateToJava(yangPluginConfig);
        YangPluginConfig.compileCode(COMP);
        YangIoUtils.deleteDirectory(DIR);
    }

    /**
     * Checks the negative scenario when an identity already present with
     * name conflict with typedef and one more identity with same name
     * with different small and caps letter combination.
     *
     * @throws MojoExecutionException
     */
    @Test
    public void processNegativetest() throws IOException,
            ParserException, MojoExecutionException {
        thrown.expect(ParserException.class);
        thrown.expectMessage("Node with name BAsE in file");
        YangIoUtils.deleteDirectory(DIR);
        String searchDir = "src/test/resources/identityTypedefSameNameNeg";

        Set<Path> paths = new HashSet<>();
        for (String file : getYangFiles(searchDir)) {
            paths.add(Paths.get(file));
        }

        utilManager.createYangFileInfoSet(paths);
        utilManager.parseYangFileInfoSet();
        utilManager.createYangNodeSet();
        utilManager.resolveDependenciesUsingLinker();

        YangPluginConfig yangPluginConfig = new YangPluginConfig();
        yangPluginConfig.setCodeGenDir(DIR);
        utilManager.translateToJava(yangPluginConfig);
        YangPluginConfig.compileCode(COMP);
        YangIoUtils.deleteDirectory(DIR);
    }
}
