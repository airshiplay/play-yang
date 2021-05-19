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

package com.airlenet.yang.compiler.parser.impl.listeners;

import com.airlenet.yang.compiler.datamodel.*;
import com.airlenet.yang.compiler.datamodel.exceptions.DataModelException;
import com.airlenet.yang.compiler.datamodel.utils.Parsable;
import com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser;
import com.airlenet.yang.compiler.parser.exceptions.ParserException;
import com.airlenet.yang.compiler.parser.impl.TreeWalkListener;
import com.airlenet.yang.compiler.parser.impl.parserutils.*;

import static com.airlenet.yang.compiler.datamodel.utils.GeneratedLanguage.JAVA_GENERATION;
import static com.airlenet.yang.compiler.datamodel.utils.YangConstructType.*;
import static com.airlenet.yang.compiler.parser.antlrgencode.GeneratedYangParser.RpcStatementContext;
import static com.airlenet.yang.compiler.translator.tojava.YangDataModelFactory.getYangActionNode;
import static com.airlenet.yang.compiler.translator.tojava.YangDataModelFactory.getYangRpcNode;

/*
 * Reference: RFC6020 and YANG ANTLR Grammar
 *
 * ABNF grammar as per RFC6020
 *  rpc-stmt            = rpc-keyword sep identifier-arg-str optsep
 *                        (";" /
 *                         "{" stmtsep
 *                             ;; these stmts can appear in any order
 *                             *(if-feature-stmt stmtsep)
 *                             [status-stmt stmtsep]
 *                             [description-stmt stmtsep]
 *                             [reference-stmt stmtsep]
 *                             *((typedef-stmt /
 *                                grouping-stmt) stmtsep)
 *                             [input-stmt stmtsep]
 *                             [output-stmt stmtsep]
 *                         "}")
 *
 * ANTLR grammar rule
 *  rpcStatement : RPC_KEYWORD identifier (STMTEND | LEFT_CURLY_BRACE (ifFeatureStatement | statusStatement
 *               | descriptionStatement | referenceStatement | typedefStatement | groupingStatement | inputStatement
 *               | outputStatement)* RIGHT_CURLY_BRACE);
 */

/**
 * Represents listener based call back function corresponding to the "rpc"
 * rule defined in ANTLR grammar file for corresponding ABNF rule in RFC 6020.
 */
public final class ActionListener {

    /**
     * Creates a new rpc listener.
     */
    private ActionListener() {
    }

    /**
     * It is called when parser receives an input matching the grammar rule
     * (rpc), performs validation and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processActionEntry(TreeWalkListener listener,
                                          GeneratedYangParser.ActionStatementContext ctx) {

        // Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, ACTION_DATA, ctx.identifier().getText(), ListenerErrorLocation.ENTRY);

        String identifier = ListenerUtil.getValidIdentifier(ctx.identifier().getText(), ACTION_DATA, ctx);

        // Validate sub statement cardinality.
        validateSubStatementsCardinality(ctx);

        // Check for identifier collision
        int line = ctx.getStart().getLine();
        int charPositionInLine = ctx.getStart().getCharPositionInLine();
        ListenerCollisionDetector.detectCollidingChildUtil(listener, line, charPositionInLine, identifier, ACTION_DATA);

        Parsable curData = listener.getParsedDataStack().peek();
        if (curData instanceof YangModule || curData instanceof YangSubModule
                || curData instanceof YangContainer || curData instanceof YangList || curData instanceof YangAugment) {

            YangNode curNode = (YangNode) curData;
            YangAction yangAction = getYangActionNode(JAVA_GENERATION);

            yangAction.setLineNumber(ctx.getStart().getLine());
            yangAction.setCharPosition(ctx.getStart().getCharPositionInLine());
            yangAction.setFileName(listener.getFileName());
            yangAction.setName(identifier);
//            ((RpcNotificationContainer) curData).setRpcPresent(true);
            try {
                curNode.addChild(yangAction);
            } catch (DataModelException e) {
                throw new ParserException(ListenerErrorMessageConstruction.constructExtendedListenerErrorMessage(ListenerErrorType.UNHANDLED_PARSED_DATA,
                        ACTION_DATA,
                                                                                ctx.identifier().getText(),
                                                                                ListenerErrorLocation.ENTRY, e.getMessage()));
            }
            listener.getParsedDataStack().push(yangAction);
        } else {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.INVALID_HOLDER, ACTION_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.ENTRY));
        }
    }

    /**
     * It is called when parser exits from grammar rule (rpc), it perform
     * validations and updates the data model tree.
     *
     * @param listener listener's object
     * @param ctx      context object of the grammar rule
     */
    public static void processActionExit(TreeWalkListener listener,
                                         GeneratedYangParser.ActionStatementContext ctx) {

        //Check for stack to be non empty.
        ListenerValidation.checkStackIsNotEmpty(listener, ListenerErrorType.MISSING_HOLDER, ACTION_DATA, ctx.identifier().getText(), ListenerErrorLocation.EXIT);

        if (!(listener.getParsedDataStack().peek() instanceof YangAction)) {
            throw new ParserException(ListenerErrorMessageConstruction.constructListenerErrorMessage(ListenerErrorType.MISSING_CURRENT_HOLDER, ACTION_DATA,
                                                                    ctx.identifier().getText(), ListenerErrorLocation.EXIT));
        }
        listener.getParsedDataStack().pop();
    }

    /**
     * Validates the cardinality of rpc sub-statements as per grammar.
     *
     * @param ctx context object of the grammar rule
     */
    private static void validateSubStatementsCardinality(GeneratedYangParser.ActionStatementContext ctx) {

        ListenerValidation.validateCardinalityMaxOne(ctx.statusStatement(), STATUS_DATA, ACTION_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.descriptionStatement(), DESCRIPTION_DATA, ACTION_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.referenceStatement(), REFERENCE_DATA, ACTION_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.inputStatement(), INPUT_DATA, ACTION_DATA, ctx.identifier().getText());
        ListenerValidation.validateCardinalityMaxOne(ctx.outputStatement(), OUTPUT_DATA, ACTION_DATA, ctx.identifier().getText());
    }
}
